use actix_web::{App, HttpRequest, HttpResponse, HttpServer, Responder, client::Client, middleware, web};
use mysql_async::{Conn, Pool};
use mysql_async::prelude::*;
use web::Data;

use crate::{cfg::Main, prelude::*};

pub async fn lent_by(db: web::Data<Pool>, client: Data<Client>, cfg: web::Data<Main>, req: HttpRequest) -> Result<HttpResponse> {
    let id = crate::api_snipeit::user_id(&req, &client, &cfg.snipeit_url).await?;
    let mut conn = db.get_conn().await?;
    let lents: Vec<u32> = conn.exec_map("SELECT `asset` FROM `lent` WHERE id = ?", (0,), |asset|asset).await?;
    Ok(HttpResponse::Ok().json(lents))
}