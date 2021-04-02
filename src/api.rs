use actix_web::{App, HttpRequest, HttpResponse, HttpServer, Responder, client::Client, http::HeaderValue, middleware, web};
use mysql_async::{Conn, Pool};
use mysql_async::prelude::*;
use web::Data;
use crate::snipeit;
use crate::types::*;

use crate::{cfg::Main, prelude::*};

pub async fn lent_list(db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(),&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    
    let assets = user_assets(user.id, db, token.clone(), &client, &cfg.snipeit_url).await?;

    Ok(HttpResponse::Ok().json(assets))
}

pub async fn user_assets(user: UID, db: Data<Pool>, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<Vec<Asset>> {
    let mut conn = db.get_conn().await?;
    let lents: Vec<i32> = conn.exec_map("SELECT `asset` FROM `lent` WHERE `user` = ?", (user,), |asset|asset).await?;

    let mut assets = Vec::new();
    // TODO: cache results
    for id in lents {
        match snipeit::asset(id, token.clone(), &client, &snipeit_url).await {
            Ok(v) => assets.push(v),
            Err(e) => warn!("Skipping asset retrieval {}: {}",id,e),
        }
    }
    Ok(assets)
}

pub async fn lent_asset(data: web::Json<CheckoutRequest>, db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(),&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    snipeit::checkout(data.asset, data.user, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("INSERT INTO `lent` (`user`,`asset`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)", (user.id,data.asset)).await?;
    Ok(HttpResponse::Ok().json(SnipeitResult::success()))
}

pub async fn return_asset(data: web::Json<CheckinRequest>, db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(),&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    snipeit::checkin(data.asset, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("DELETE FROM `lent` WHERE `asset` = ?", (data.asset,)).await?;
    Ok(HttpResponse::Ok().json(SnipeitResult::success()))
}