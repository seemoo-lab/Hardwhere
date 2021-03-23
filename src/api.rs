use actix_web::{App, HttpRequest, HttpResponse, HttpServer, Responder, client::Client, middleware, web};
use mysql_async::{Conn, Pool};
use mysql_async::prelude::*;
use web::Data;
use crate::snipeit;
use crate::types::*;

use crate::{cfg::Main, prelude::*};

pub async fn lent_list(db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token,&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    let mut conn = db.get_conn().await?;
    let lents: Vec<u32> = conn.exec_map("SELECT `asset` FROM `lent` WHERE `user` = ?", (user.id,), |asset|asset).await?;
    Ok(HttpResponse::Ok().json(lents))
}

pub async fn lent_asset(data: web::Json<CheckoutRequest>, db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(),&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    snipeit::checkout(data.asset, data.user, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("INSERT INTO `lent` (`user`,`asset`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)", (user.id,data.asset)).await?;
    Ok(HttpResponse::Ok().finish())
}

pub async fn return_asset(data: web::Json<CheckinRequest>, db: Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let token = snipeit::token(&req)?;
    let user = snipeit::user(token.clone(),&client, &cfg.snipeit_url).await?;
    trace!("User: {:?}",user);
    snipeit::checkin(data.asset, token, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    conn.exec_drop("DELETE FROM `lent` WHERE `asset` = ?", (data.asset,)).await?;
    Ok(HttpResponse::Ok().finish())
}