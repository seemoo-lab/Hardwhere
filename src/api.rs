//! HardWhere App API
use crate::snipeit;
use crate::types::*;
use actix_web::{web, HttpRequest, HttpResponse};
use awc::error::HeaderValue;
use awc::Client;
use mysql_async::prelude::*;
use mysql_async::Pool;
use web::Data;

use crate::{cfg::Main, prelude::*};

/// Returns list of currently lent assets (to others)
pub async fn lent_list(
    db: Data<Pool>,
    client: Data<Client>,
    cfg: web::Data<Main>,
    req: HttpRequest,
) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(), &client, &cfg.snipeit_url).await?;
    trace!("User: {:?}", user);

    let assets = user_assets(user.id, db, token.clone(), &client, &cfg.snipeit_url).await?;

    Ok(HttpResponse::Ok().json(assets))
}

/// List of assets a user has lent
pub async fn user_assets(
    user: UID,
    db: Data<Pool>,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<Vec<Asset>> {
    let mut conn = db.get_conn().await?;
    let lents: Vec<i32> = conn
        .exec_map(
            "SELECT `asset` FROM `lent` WHERE `user` = ?",
            (user,),
            |asset| asset,
        )
        .await?;

    let mut assets = Vec::new();
    // TODO: cache results
    for id in lents {
        match snipeit::asset(id, token.clone(), &client, &snipeit_url).await {
            Ok(v) => assets.push(v),
            Err(e) => warn!("Skipping asset retrieval {}: {}", id, e),
        }
    }
    Ok(assets)
}
/// Checkout asset to user
pub async fn lent_asset(
    data: web::Json<CheckoutRequest>,
    db: Data<Pool>,
    client: Data<Client>,
    cfg: web::Data<Main>,
    req: HttpRequest,
) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(), &client, &cfg.snipeit_url).await?;
    trace!("User: {:?}", user);
    let response =
        snipeit::checkout(data.asset, data.user, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("INSERT INTO `lent` (`user`,`asset`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)", (user.id,data.asset)).await?;
    Ok(HttpResponse::Ok().json(response))
}
/// Checkin asset
pub async fn return_asset(
    data: web::Json<CheckinRequest>,
    db: Data<Pool>,
    client: Data<Client>,
    cfg: web::Data<Main>,
    req: HttpRequest,
) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(), &client, &cfg.snipeit_url).await?;
    trace!("User: {:?}", user);
    let response = snipeit::checkin(data.asset, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("DELETE FROM `lent` WHERE `asset` = ?", (data.asset,))
        .await?;
    Ok(HttpResponse::Ok().json(response))
}
