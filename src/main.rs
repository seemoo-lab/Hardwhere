use std::{collections::HashMap, sync::Mutex};

use actix_files::Files;
use actix_session::CookieSession;
use actix_web::{
    http::{
        header::{ACCEPT, CONTENT_TYPE},
        StatusCode,
    },
    middleware::{ErrorHandlers, Logger},
    rt::spawn,
    web, App, HttpServer,
};
use awc::ClientBuilder;
use handlebars::Handlebars;
use mysql_async::{prelude::*, Opts, OptsBuilder, Pool};

mod prelude;
use prelude::*;

use crate::types::AutoLoginTokens;

mod api;
mod cfg;
mod indexer;
mod snipeit;
mod types;
mod webview;

const DB_VERSION: &str = "0.1";

#[actix_web::main]
async fn main() -> Result<()> {
    // setup logging
    let mut builder = env_logger::Builder::new();
    builder.filter_level(LevelFilter::Warn);
    #[cfg(debug_assertions)]
    {
        builder.filter_module(env!("CARGO_CRATE_NAME"), LevelFilter::Trace);
        builder.filter_module("actix_web", LevelFilter::Debug);
    }
    #[cfg(not(debug_assertions))]
    {
        builder.filter_module(env!("CARGO_CRATE_NAME"), LevelFilter::Info);
        builder.filter_module("actix_web", LevelFilter::Warn);
    }
    builder.parse_env("RUST_LOG");
    builder.init();

    // config
    let config = cfg::Cfg::load()?;
    // setup DB connection
    let bind = format!("{}:{}", config.main.listen_ip, config.main.listen_port);
    let cfg_db = config.db;
    let db_opts: Opts = OptsBuilder::default()
        .ip_or_hostname(cfg_db.ip)
        .user(Some(cfg_db.user))
        .pass(Some(cfg_db.password))
        .db_name(Some(cfg_db.name))
        .into();
    let db = mysql_async::Pool::new(db_opts);
    setup_db(&db).await?;
    // setup session decryption key
    let key = config.main.session_encryption_key.as_bytes();
    let mut key = Vec::from(key);
    if key.len() < 32 {
        key.resize(32, 0);
        warn!("0-padding session encryption key, not long enough.")
    }
    let session_key: &'static [u8] = key.leak();

    let config_main = web::Data::new(config.main);
    // template engine
    let mut handlebars = Handlebars::new();
    handlebars
        .register_templates_directory(".html", "./static/templates")
        .expect("Can't initialize templates!");
    let handlebars_ref = web::Data::new(handlebars);

    let db_c = db.clone();
    let config_main_c = config_main.clone();
    spawn(async move {
        let wait_time = std::time::Duration::from_secs(config_main_c.indexing_refresh_seconds);
        let mut interval = actix_web::rt::time::interval(wait_time);
        // first tick doesn't trigger wait
        interval.tick().await;
        loop {
            if let Err(e) = indexer::refresh_index(&config_main_c, &db_c).await {
                error!("Failed to index: {}", e);
            }

            if let Err(e) = indexer::check_default_fieldset(&config_main_c).await {
                error!("Failed to check default fieldsets: {}", e);
            }

            // wait for next tick
            interval.tick().await;
        }
    });

    let auto_login_tokens: AutoLoginTokens = web::Data::new(Mutex::new(HashMap::new()));

    let db_c = db.clone();
    info!("Listening on {}", bind);
    // setup webserver
    HttpServer::new(move || {
        // client constructed inside, one per worker thread
        let web_client = web::Data::new(
            ClientBuilder::new()
                .add_default_header((ACCEPT, "application/json"))
                .add_default_header((CONTENT_TYPE, "application/json"))
                .finish(),
        );

        App::new()
            .app_data(config_main.clone())
            .app_data(handlebars_ref.clone())
            .wrap(
                CookieSession::private(&session_key)
                    .name("actix_session")
                    .path("/")
                    .secure(config_main.session_secure)
                    .same_site(actix_web::cookie::SameSite::Strict)
                    .lazy(true)
                    .http_only(true),
            )
            //.wrap(ErrorHandlers::new().handler(StatusCode::INTERNAL_SERVER_ERROR, error_handler))
            .wrap(Logger::default())
            .app_data(web::Data::new(db_c.clone()))
            .app_data(auto_login_tokens.clone())
            .app_data(web_client)
            .service(
                web::scope("/HardWhere")
                    .service(web::resource("/api/checkedout").route(web::get().to(api::lent_list)))
                    .service(web::resource("/api/checkout").route(web::post().to(api::lent_asset)))
                    .service(web::resource("/api/checkin").route(web::post().to(api::return_asset)))
                    .service(
                        web::resource("/internal/autologin")
                            .route(web::post().to(webview::snipeit_autologin_prepare)),
                    )
                    .service(web::resource("").route(web::get().to(webview::view)))
                    .service(web::resource("/").route(web::get().to(webview::view)))
                    .service(web::resource("/login").route(web::post().to(webview::login)))
                    .service(web::resource("/logout").route(web::get().to(webview::logout)))
                    .service(
                        web::resource("/autologin/{auth_token}")
                            .route(web::get().to(webview::auto_login)),
                    )
                    .service(Files::new("/static", "static/").show_files_listing()),
            )
    })
    .bind(&bind)?
    .run()
    .await?;
    // db cleanup
    db.disconnect().await?;
    Ok(())
}

/// Setup database connection and tables
async fn setup_db(pool: &Pool) -> Result<()> {
    let setup = include_str!("setup.sql");
    let mut conn = pool.get_conn().await?;
    let tables: Vec<String> = conn.query_map("SHOW TABLES", |v| v).await?;
    let version: Option<String> = match tables.contains(&String::from("version")) {
        true => conn.query_first("SELECT `version` FROM `version`").await?,
        false => None,
    };
    if let Some(v) = version {
        debug!("DB found, version {}", v);
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
        conn.exec_drop("INSERT INTO `version` (`version`) VALUES(?)", (DB_VERSION,))
            .await?;
    }
    Ok(())
}
