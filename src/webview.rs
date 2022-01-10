use std::time::Duration;

use actix_session::Session;
use actix_web::{HttpResponse, client::Client, http::HeaderValue, web::{self, Data}};
use handlebars::Handlebars;
use mysql_async::Pool;
use serde_json::json;
use actix_web::http::header::LOCATION;

use crate::{cfg::Main, prelude::*, snipeit, types::{LoginData, API_KEY, AutoLoginPrepare, AutoLogin}, api, AutoLoginTokens};

pub(crate) async fn view(db: Data<Pool>, hb: web::Data<Handlebars<'_>>, session: Session, client: Data<Client>, cfg: web::Data<Main>) -> Result<HttpResponse> {
    let body = if let Some(api_key) = session.get::<String>(API_KEY)? {
        let token = HeaderValue::from_str(&api_key)?;
        let user = match snipeit::user(token.clone(), &client, &cfg.snipeit_url).await {
            Ok(u) => u,
            Err(Error::SnipeitBadRequest(s)) => {
                session.purge();
                info!("Login failed: {}",s);
                let data = json!({
                    "login_page": cfg.snipeit_url,
                    "error_msg": "Invalid login!",
                });
                return Ok(HttpResponse::NotFound().body(hb.render("login", &data)?));
            },
            Err(e) => return Err(e),
        };
        let assets = api::user_assets(user.id, db, token.clone(), &client, &cfg.snipeit_url).await?;

        let data = json!({
            "user": user.first_name,
            "assets": assets,
            "url_base": cfg.snipeit_url
        });
        hb.render("assets", &data)?
    } else {
        let data = json!({
            "login_page": cfg.snipeit_url
        });
        hb.render("login", &data)?
    };
    Ok(HttpResponse::Ok().body(body))
}

pub(crate) async fn login(params: web::Form<LoginData>, hb: web::Data<Handlebars<'_>>, session: Session, client: Data<Client>, cfg: web::Data<Main>) -> Result<HttpResponse> {
    trace!("Login action");
    let params = params.into_inner();
    let api_token = format!("Bearer {}",params.api_key);
    let token = HeaderValue::from_str(&api_token)?;
    let user = match snipeit::user(token, &client, &cfg.snipeit_url).await {
        Ok(u) => u,
        Err(Error::SnipeitBadRequest(s)) => {
            info!("Login failed: {}",s);
            let data = json!({
                "login_page": cfg.snipeit_url,
                "error_msg": "Invalid login!",
            });
            return Ok(HttpResponse::NotFound().body(hb.render("login", &data)?));
        },
        Err(e) => return Err(e),
    };
    let data = json!({
        "user": user.first_name
    });
    let body = hb.render("login_successful", &data)?;
    session.set(API_KEY, api_token)?;

    Ok(HttpResponse::Found().header(LOCATION, "/HardWhere/").body(body))
}

/// Called by snipeit to setup autologin
pub(crate) async fn snipeit_autologin_prepare(req: web::Json<AutoLoginPrepare>, cfg: web::Data<Main>, autologin_token: web::Data<AutoLoginTokens>) -> Result<HttpResponse> {
    info!("Received autologin prepare request");
    let req = req.into_inner();
    let mut tokens = autologin_token.lock().expect("Can't lock mutex");
    tokens.insert(req.login_token,AutoLogin::new(req.api_token,Duration::from_secs(cfg.autologin_token_ttl.into())));
    Ok(HttpResponse::Ok().finish())
}

pub(crate) async fn auto_login(hb: web::Data<Handlebars<'_>>, client: Data<Client>, path: web::Path<(String,)>, session: Session, cfg: web::Data<Main>, autologin_token: web::Data<AutoLoginTokens>) -> Result<HttpResponse> {
    info!("performing auto login");
    session.purge();
    let (token,) = path.into_inner();
    // retrieve api token for autologin token
    let mut tokens = autologin_token.lock().expect("Can't lock mutex");
    if let Some(api_token) = tokens.remove(&token).and_then(|v|v.api_token()) {
        // verify it's still valid
        let api_token = format!("Bearer {}",api_token);
        match snipeit::user(HeaderValue::from_str(&api_token)?, &client, &cfg.snipeit_url).await {
            Ok(_u) => {
                session.set(API_KEY, api_token)?;
                return Ok(HttpResponse::TemporaryRedirect().header(LOCATION, "/HardWhere/").body("Login successfull"));
            },
            Err(Error::SnipeitBadRequest(s)) => {
                info!("token invalid: {}",s);
                // fall through to end
            },
            Err(e) => return Err(e),
        };
    }

    let data = json!({
        "url_base": cfg.snipeit_url
    });
    let body = hb.render("invalid_autologin", &data)?;
    Ok(HttpResponse::NotFound().body(body))
}

pub(crate) async fn logout(hb: web::Data<Handlebars<'_>>, session: Session) -> Result<HttpResponse> {
    session.purge();
    let body = hb.render("logout", &())?;
    Ok(HttpResponse::Found().header(LOCATION, "/HardWhere/").body(body))
}