use actix_session::CookieSession;
use actix_web::{App, HttpRequest, HttpResponse, HttpServer, Responder, client::{Client, ClientBuilder}, http::header::{ACCEPT, CONTENT_TYPE}, middleware, web};
use handlebars::Handlebars;
use mysql_async::{Opts, OptsBuilder, Pool, prelude::*};

mod prelude;
use prelude::*;

mod cfg;
mod authentication;
mod snipeit;
mod api;
mod types;
mod indexer;
mod webview;

const DB_VERSION: &str = "0.1";

#[actix_web::main]
async fn main() -> Result<()> {
    let mut builder = env_logger::Builder::new();
    builder.filter_level(LevelFilter::Warn);
    #[cfg(debug_assertions)]
    builder.filter_module(env!("CARGO_CRATE_NAME"), LevelFilter::Trace);
    #[cfg(not(debug_assertions))]
    builder.filter_module(env!("CARGO_CRATE_NAME"), LevelFilter::Info);
    builder.parse_env("RUST_LOG");
    builder.init();

    let config = cfg::Cfg::load()?;
    let bind = format!("{}:{}",config.main.listen_ip,config.main.listen_port);
    let cfg_db = config.db;
    let db_opts: Opts = OptsBuilder::default()
        .ip_or_hostname(cfg_db.ip)
        .user(Some(cfg_db.user))
        .pass(Some(cfg_db.password))
        .db_name(Some(cfg_db.name))
        .into();
    let db = mysql_async::Pool::new(db_opts);
    setup_db(&db).await?;
    let config_main = web::Data::new(config.main);
    let db_c = db.clone();

    let mut handlebars = Handlebars::new();
    handlebars
    .register_templates_directory(".html", "./static/templates")
    .expect("Can't initialize templates!");
    let handlebars_ref = web::Data::new(handlebars);

    info!("Listening on {}",bind);
    if let Err(e) = indexer::refresh_index(&config_main, db.clone()).await {
        error!("Failed to index: {}",e);
        return Err(e);
    }
    HttpServer::new(move || {
        App::new()
            .app_data(config_main.clone())
            .app_data(handlebars_ref.clone())
            .wrap(
                CookieSession::signed(&[0; 32])
                    .domain("www.rust-lang.org")
                    .name("actix_session")
                    .path("/")
                    .secure(true)
                    .same_site(actix_web::cookie::SameSite::Strict)
                    .lazy(true)
                    .http_only(true))
            .data(db_c.clone())
            .data(ClientBuilder::new().header(ACCEPT,"application/json").header(CONTENT_TYPE,"application/json").finish())
            .service(web::resource("/api/checkedout").route(web::get().to(api::lent_list)))
            .service(web::resource("/api/checkout").route(web::post().to(api::lent_asset)))
            .service(web::resource("/api/checkin").route(web::post().to(api::return_asset)))
            .service(web::resource("/").route(web::get().to(webview::view)))
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
    let tables: Vec<String> = conn.query_map("SHOW TABLES", |v|v).await?;
    let version: Option<String> = match tables.contains(&String::from("version")) {
        true => conn.query_first("SELECT `version` FROM `version`").await?,
        false => None
    };
    if let Some(v) = version {
        debug!("DB found, version {}",v);
        // TODO for upgrades
    } else {
        info!("setting up DB");
        for table in setup.split(";") {
            let table = table.trim();
            if !table.is_empty() {
                conn.exec_drop(table, ()).await?;
            }
        }
        trace!("Setting DB version");
        conn.exec_drop("INSERT INTO `version` (`version`) VALUES(?)",(DB_VERSION,)).await?;
    }
    Ok(())
}
