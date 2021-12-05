use std::time::Instant;

use actix_web::{client::{Client, ClientBuilder}, http::{HeaderValue, header::{ACCEPT, CONTENT_TYPE}}};
use mysql_async::{Pool, prelude::Queryable};

use crate::{cfg::Main, prelude::*, snipeit, types::*};

const SEEN_TEMP_TABLE: &str = "CREATE TEMPORARY TABLE `t_seen` ( `asset` int NOT NULL PRIMARY KEY );";

pub async fn refresh_index(config: &Main, db: &Pool) -> Result<()>{
    let client = ClientBuilder::new().header(ACCEPT,"application/json").header(CONTENT_TYPE,"application/json").finish();
    let token = HeaderValue::from_str(&format!("Bearer {}",config.snipeit_system_token)).unwrap();
    trace!("Requesting total item..");
    let total = snipeit::assets(0, 1, token.clone(), &client, &config.snipeit_url).await?.total;
    trace!("Total: {}",total);
    let mut done = 0;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("DROP TABLE IF EXISTS `t_seen`", ()).await?;
    conn.exec_drop(SEEN_TEMP_TABLE, ()).await?;
    let limit = 200;
    let mut checkedout = 0;
    let mut failed = 0;
    let start = Instant::now();
    while done < total {
        trace!("Requesting assets {}-{}",done,done+limit);
        let data = snipeit::assets(done, limit, token.clone(), &client, &config.snipeit_url).await?;
        trace!("Received {} items",data.rows.len());
        done += data.rows.len() as i32;
        for asset in data.rows {
            conn.exec_drop("INSERT INTO `t_seen` (`asset`) VALUES(?)",(asset.id,)).await?;
            match asset.assigned_to {
                Some(MaybeAssignee::Known(Assignee::User(_u))) => {
                    match find_activity_checkout(asset.id, token.clone(), &client, &config.snipeit_url).await {
                        Ok(u) => {
                            conn.exec_drop("INSERT INTO `lent` (`asset`,`user`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)",(asset.id,u)).await?;
                            checkedout +=1;
                        }
                        Err(e) => {error!("Failed to verify checkout-admin for {}: {}",asset.id,e); failed+=1;},
                    }
                },
                Some(MaybeAssignee::UnknownAssignee(v)) => {error!("Unknown assignee type: {}",v)},
                Some(MaybeAssignee::Known(Assignee::Location(v))) => {warn!("Ignoring location assignee: {:?}",v)}
                None => {
                    conn.exec_drop("DELETE FROM `lent` WHERE asset = ?",(asset.id,)).await?;
                }
            }
        }
    }
    conn.exec_drop("DELETE FROM `lent` WHERE asset NOT IN (SELECT asset FROM `t_seen`)",()).await?;
    conn.exec_drop("DROP TABLE `t_seen`",()).await?;
    let time = start.elapsed();
    info!("Indexed {} assets, {} checked out. Failed {}. {} ms",total,checkedout,failed,time.as_millis());
    Ok(())
}

async fn find_activity_checkout(item: AssetId, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<UID> {
    let mut reports = snipeit::activity(item, token, client, snipeit_url).await?;

    reports.sort_by_key(|v|v.id);
    reports.reverse();
    debug_assert!(reports[0].id > reports[reports.len() - 1].id);
    for report in reports {
        match report.action_type {
            MaybeActionType::Known(ActionType::Checkout) => {
                return Ok(report.admin.id);
            },
            MaybeActionType::UnknownActionType(v) => {
                error!("Unknown action type: {:?}",v);
            }
            _ => (),
        }
    }
    Err(Error::NoCheckoutActivity(item))
}