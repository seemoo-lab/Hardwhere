use actix_web::{App, HttpRequest, HttpResponse, HttpServer, Responder, middleware, web};
use mysql_async::{Opts, OptsBuilder, Pool, prelude::*};

mod prelude {
    pub use log::*;
    pub use stable_eyre::eyre::{eyre, Result, WrapErr};
}

use prelude::*;

mod cfg;

const DB_VERSION: &str = "0.1";

#[actix_web::main]
async fn main() -> Result<()> {
    stable_eyre::install().unwrap();
    env_logger::init();

    let config = cfg::Cfg::load()?;
    let bind = format!("{}:{}",config.main.listen_ip,config.main.listen_port);
    let cfg_db = config.db;
    let mut db_opts: Opts = OptsBuilder::default()
        .ip_or_hostname(cfg_db.ip)
        .user(Some(cfg_db.user))
        .pass(Some(cfg_db.password))
        .db_name(Some(cfg_db.name))
        .into();
    let db = mysql_async::Pool::new(db_opts);
    setup_db(&db).await?;
    let config_main = web::Data::new(config.main);
    let db_c = db.clone();
    HttpServer::new(move || {
        App::new()
            //.app_data(web::Data::new(config.main))
            .app_data(db_c.clone())
            .service(web::resource("/api/lent").route(web::get().to(lent_by)))
    })
        .bind(&bind)?
        .run()
        .await?;
    // db cleanup
    db.disconnect().await?;
    Ok(())
}

async fn setup_db(pool: &Pool) -> Result<()> {
    let setup = include_str!("setup.sql");
    let mut conn = pool.get_conn().await?;
    let version: Option<String> = conn.query_first("SELECT * FROM `version`").await?;
    if let Some(v) = version {
        // TODO for upgrades
    } else {
        for table in setup.split(";") {
            conn.exec_drop(table, ()).await?;
        }
        conn.exec_drop("INSERT INTO `version` VALUES(?)",(DB_VERSION,)).await?;
    }
    Ok(())
}

async fn lent_by( db: web::Data<Pool>) -> impl Responder {
    format!("ASDF")
}