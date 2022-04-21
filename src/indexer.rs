//! Lent-to-by indexer and default fieldset checker
use std::time::Instant;

use actix_web::http::header::{ACCEPT, CONTENT_TYPE};
use awc::{error::HeaderValue, Client, ClientBuilder};
use mysql_async::{prelude::Queryable, Pool};

use crate::{cfg::Main, prelude::*, snipeit, types::*};

const SEEN_TEMP_TABLE: &str =
    "CREATE TEMPORARY TABLE `t_seen` ( `asset` int NOT NULL PRIMARY KEY );";

const RETRIEVE_LIMIT: usize = 200;

/// Refresh index of who-lent-to-whom of assets
pub async fn refresh_index(config: &Main, db: &Pool) -> Result<()> {
    let client = ClientBuilder::new()
        .add_default_header((ACCEPT, "application/json"))
        .add_default_header((CONTENT_TYPE, "application/json"))
        .finish();
    let token = HeaderValue::from_str(&format!("Bearer {}", config.snipeit_system_token)).unwrap();

    trace!("Requesting total item..");
    let total = snipeit::assets(0, 1, token.clone(), &client, &config.snipeit_url)
        .await?
        .total;
    trace!("Total: {}", total);

    let mut done = 0;
    let mut conn = db.get_conn().await?;
    // setup temporary table
    conn.exec_drop("DROP TABLE IF EXISTS `t_seen`", ()).await?;
    conn.exec_drop(SEEN_TEMP_TABLE, ()).await?;
    let mut checkedout = 0;
    let mut failed = 0;
    let start = Instant::now();
    // iterate over all assets and check checkout status + history
    while done < total {
        trace!("Requesting assets {}-{}", done, done + RETRIEVE_LIMIT);
        let data = snipeit::assets(
            done,
            RETRIEVE_LIMIT,
            token.clone(),
            &client,
            &config.snipeit_url,
        )
        .await?;
        trace!("Received {} items", data.rows.len());
        done += data.rows.len();

        if data.rows.len() == 0 {
            error!(
                "Got 0 rows, was at {}/{} entries, failsafe break",
                done, total
            );
            break;
        }

        for asset in data.rows {
            conn.exec_drop("INSERT INTO `t_seen` (`asset`) VALUES(?)", (asset.id,))
                .await?;
            match asset.assigned_to {
                Some(MaybeAssignee::Known(Assignee::User(_u))) => {
                    match find_activity_checkout(
                        asset.id,
                        token.clone(),
                        &client,
                        &config.snipeit_url,
                    )
                    .await
                    {
                        Ok(u) => {
                            conn.exec_drop("INSERT INTO `lent` (`asset`,`user`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)",(asset.id,u)).await?;
                            checkedout += 1;
                        }
                        Err(e) => {
                            error!("Failed to verify checkout-admin for {}: {}", asset.id, e);
                            failed += 1;
                        }
                    }
                }
                Some(MaybeAssignee::UnknownAssignee(v)) => {
                    error!("Unknown assignee type: {}", v)
                }
                Some(MaybeAssignee::Known(Assignee::Location(v))) => {
                    warn!(
                        "Ignoring location assignee: {:?} of asset id {}",
                        v, asset.id
                    );
                }
                None => {
                    conn.exec_drop("DELETE FROM `lent` WHERE asset = ?", (asset.id,))
                        .await?;
                }
            }
        }
    }
    // drop assets not seen in temp table (deleted)
    conn.exec_drop(
        "DELETE FROM `lent` WHERE asset NOT IN (SELECT asset FROM `t_seen`)",
        (),
    )
    .await?;
    // cleanup temp table
    conn.exec_drop("DROP TABLE `t_seen`", ()).await?;
    let time = start.elapsed();
    info!(
        "Indexed {} assets, {} checked out. Failed {}. {} ms",
        total,
        checkedout,
        failed,
        time.as_millis()
    );
    Ok(())
}

/// Find latest checkout entry in asset history/activity
async fn find_activity_checkout(
    item: AssetId,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<UID> {
    let mut reports = snipeit::activity(item, token, client, snipeit_url).await?;

    reports.sort_by_key(|v| v.id);
    reports.reverse();
    debug_assert!(reports[0].id > reports[reports.len() - 1].id);
    for report in reports {
        match report.action_type {
            MaybeActionType::Known(ActionType::Checkout) => {
                return Ok(report.admin.id);
            }
            MaybeActionType::UnknownActionType(v) => {
                error!("Unknown action type: {:?}", v);
            }
            _ => (),
        }
    }
    Err(Error::NoCheckoutActivity(item))
}

/// Scan models for missing fieldsets and set the default specified in the config
pub async fn check_default_fieldset(config: &Main) -> Result<()> {
    let client = ClientBuilder::new()
        .add_default_header((ACCEPT, "application/json"))
        .add_default_header((CONTENT_TYPE, "application/json"))
        .finish();
    let token = HeaderValue::from_str(&format!("Bearer {}", config.snipeit_system_token)).unwrap();

    let fieldset = fieldset_by_name(
        &config.default_fieldset,
        &token,
        &client,
        &config.snipeit_url,
    )
    .await?;

    let total = snipeit::models(0, 1, token.clone(), &client, &config.snipeit_url)
        .await?
        .total;
    let mut done = 0;

    let mut fixed = 0;

    let patch_data = ModelPatch {
        fieldset_id: Some(fieldset.id),
        ..Default::default()
    };
    // iterate over all models and search for missing custom fieldsets
    while done < total {
        let data = snipeit::models(
            done,
            RETRIEVE_LIMIT,
            token.clone(),
            &client,
            &config.snipeit_url,
        )
        .await?;
        done += data.rows.len();

        if data.rows.len() == 0 {
            error!(
                "Got 0 rows, was at {}/{} entries, failsafe break",
                done, total
            );
            break;
        }

        for model in data.rows {
            if model.fieldset.is_none() {
                info!("Patching {}", model.name);
                match snipeit::patch_model(
                    model.id,
                    &patch_data,
                    token.clone(),
                    &client,
                    &config.snipeit_url,
                )
                .await
                {
                    Ok(_) => fixed += 1,
                    Err(e) => error!("Failed to patch model {}: {}", model.id, e),
                }
            } else {
                trace!("Has fieldset: {}", model.name);
            }
        }
    }

    debug!("Fixed up {}/{} model fieldsets", fixed, total);

    Ok(())
}

/// Retrieve fieldset ID by its name
async fn fieldset_by_name(
    name: &str,
    token: &HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<Fieldset> {
    let total = snipeit::fieldsets(0, 1, token.clone(), client, snipeit_url)
        .await?
        .total as usize;
    let mut retrieved = 0;
    // iterate over all custom fieldsets untill we find the matching one
    while retrieved < total {
        let data = snipeit::fieldsets(
            retrieved,
            RETRIEVE_LIMIT,
            token.clone(),
            client,
            snipeit_url,
        )
        .await?;
        retrieved += data.rows.len();

        if data.rows.len() == 0 {
            error!(
                "Got 0 rows, was at {}/{} entries, failsafe break",
                retrieved, total
            );
            break;
        }

        if let Some(fieldset) = data.rows.into_iter().find(|e| e.name == name) {
            return Ok(fieldset);
        }
    }

    Err(Error::FieldsetNotFound(name.to_string()))
}
