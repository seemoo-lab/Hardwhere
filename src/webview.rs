//! /HardWhere/ webview
use actix_session::Session;
use actix_web::dev;
use actix_web::http::header::LOCATION;
use actix_web::middleware::ErrorHandlerResponse;
use actix_web::{
    web::{self, Data},
    HttpResponse,
};
use awc::{error::HeaderValue, Client};
use handlebars::Handlebars;
use mysql_async::Pool;
use serde_json::json;
use std::str::FromStr;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use crate::types::SESSION_TTL_KEY;
use crate::{
    api,
    cfg::Main,
    prelude::*,
    snipeit,
    types::{AutoLogin, AutoLoginPrepare, LoginData, API_KEY},
    AutoLoginTokens,
};
/// Browser view
/// Displays assets lent or the login page, depending on login status
pub(crate) async fn view(
    db: Data<Pool>,
    hb: Data<Handlebars<'_>>,
    session: Session,
    client: Data<Client>,
    cfg: web::Data<Main>,
) -> Result<HttpResponse> {
    let body = if let Some(api_key) = get_api_key(&session, &cfg)? {
        let token = HeaderValue::from_str(&api_key)?;
        // request user info from snipeit
        let user = match snipeit::user(token.clone(), &client, &cfg.snipeit_url).await {
            Ok(u) => u,
            Err(Error::SnipeitBadRequest(s)) => {
                session.purge();
                info!("Login failed: {}", s);
                let data = json!({
                    "login_page": cfg.snipeit_url,
                    "error_msg": "Invalid login!",
                });
                return Ok(HttpResponse::NotFound().body(hb.render("login", &data)?));
            }
            Err(e) => return Err(e),
        };
        // request assets
        let assets =
            api::user_assets(user.id, db, token.clone(), &client, &cfg.snipeit_url).await?;
        // render page
        let data = json!({
            "user": user.first_name,
            "assets": assets,
            "url_base": cfg.snipeit_url
        });
        // renew token
        set_api_key(&session, &api_key)?;
        // render view
        hb.render("assets", &data)?
    } else {
        // Invalid user / login, show login page
        session.purge();
        let data = json!({
            "login_page": cfg.snipeit_url
        });
        hb.render("login", &data)?
    };
    Ok(HttpResponse::Ok().body(body))
}

/// Manual login handling
pub(crate) async fn login(
    params: web::Form<LoginData>,
    hb: web::Data<Handlebars<'_>>,
    session: Session,
    client: Data<Client>,
    cfg: web::Data<Main>,
) -> Result<HttpResponse> {
    trace!("Login action");
    let params = params.into_inner();
    let api_token = format!("Bearer {}", params.api_key);
    let token = HeaderValue::from_str(&api_token)?;
    let user = match snipeit::user(token, &client, &cfg.snipeit_url).await {
        Ok(u) => u,
        Err(Error::SnipeitBadRequest(s)) => {
            info!("Login failed: {}", s);
            let data = json!({
                "login_page": cfg.snipeit_url,
                "error_msg": "Invalid login!",
            });
            return Ok(HttpResponse::NotFound().body(hb.render("login", &data)?));
        }
        Err(e) => return Err(e),
    };
    let data = json!({
        "user": user.first_name
    });
    let body = hb.render("login_successful", &data)?;
    set_api_key(&session, &api_token)?;

    Ok(HttpResponse::Found()
        .append_header((LOCATION, "/HardWhere/"))
        .body(body))
}

/// Called by snipeit to setup autologin
pub(crate) async fn snipeit_autologin_prepare(
    req: web::Json<AutoLoginPrepare>,
    cfg: web::Data<Main>,
    autologin_token: AutoLoginTokens,
) -> Result<HttpResponse> {
    info!("Received autologin prepare request");
    let req = req.into_inner();
    let mut tokens = autologin_token.lock().expect("Can't lock mutex");
    tokens.insert(
        req.login_token,
        AutoLogin::new(
            req.api_token,
            Duration::from_secs(cfg.autologin_token_ttl.into()),
        ),
    );
    Ok(HttpResponse::Ok().finish())
}

/// Auto login handler
pub(crate) async fn auto_login(
    hb: web::Data<Handlebars<'_>>,
    client: Data<Client>,
    path: web::Path<(String,)>,
    session: Session,
    cfg: web::Data<Main>,
    autologin_token: AutoLoginTokens,
) -> Result<HttpResponse> {
    info!("performing auto login");
    session.clear();
    let (token,) = path.into_inner();
    // retrieve api token for autologin token
    let mut tokens = autologin_token.lock().expect("Can't lock mutex");
    if let Some(api_token) = tokens.remove(&token).and_then(|v| v.api_token()) {
        // verify it's still valid
        let api_token = format!("Bearer {}", api_token);
        match snipeit::user(
            HeaderValue::from_str(&api_token)?,
            &client,
            &cfg.snipeit_url,
        )
        .await
        {
            Ok(_u) => {
                set_api_key(&session, &api_token)?;
                return Ok(HttpResponse::TemporaryRedirect()
                    .append_header((LOCATION, "/HardWhere/"))
                    .body("Login successfull"));
            }
            Err(Error::SnipeitBadRequest(s)) => {
                info!("token invalid: {}", s);
                // fall through to end
            }
            Err(e) => return Err(e),
        };
    }

    let data = json!({
        "url_base": cfg.snipeit_url
    });
    let body = hb.render("invalid_autologin", &data)?;
    Ok(HttpResponse::NotFound().body(body))
}

/// Logout handler
pub(crate) async fn logout(
    hb: web::Data<Handlebars<'_>>,
    session: Session,
) -> Result<HttpResponse> {
    session.purge();
    let body = hb.render("logout", &())?;
    Ok(HttpResponse::Found()
        .append_header((LOCATION, "/HardWhere/"))
        .body(body))
}

// retrieve session api token if still valid
// fn decode_session_cookie(data: String) -> Option<String> {
//     if let Some((ttl,token)) = data.split_once(' ') {
//         let now = time_now_secs();
//         match u64::from_str(ttl) {
//             Ok(v) => if v < now {Some(token.to_owned())} else {None},
//             Err(_) => {
//                 info!("Ignoring session cookie, missing timestamp");
//                 None
//             },
//         }
//     } else {
//         info!("Ignoring invalid session cookie");
//         return None;
//     }
// }

/// retrieve session api token if still valid
fn get_api_key(ses: &Session, cfg: &Main) -> Result<Option<String>> {
    let key = match ses.get::<String>(API_KEY)? {
        Some(v) => v,
        None => {
            debug!("Missing API_KEY in session");
            return Ok(None);
        }
    };
    let ttl = match ses.get::<u64>(SESSION_TTL_KEY)? {
        Some(v) => v,
        None => {
            debug!("Missing SESSION_TTL_KEY in session");
            return Ok(None);
        }
    };

    // We encode the point in time the session was created.
    // We could also just encode the maximum point in time this session is valid,
    // but this way we can enforce a changed TTL for existing sessions.
    if ttl + cfg.session_ttl_secs > time_now_secs() {
        Ok(Some(key))
    } else {
        debug!("Session timed out");
        Ok(None)
    }
}

/// Encode session cookie data (api token with TTL)
fn set_api_key(ses: &Session, api_key: &str) -> Result<()> {
    ses.insert(API_KEY, api_key)?;
    ses.insert(SESSION_TTL_KEY, time_now_secs())?;

    Ok(())
}

// Retrieve current time
fn time_now_secs() -> u64 {
    // should only panic "if earlier is later than self", at which point there is nothing to do
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_secs()
}

// pub(crate) fn error_handler<B>(mut res: dev::ServiceResponse<B>) -> Result<ErrorHandlerResponse<B>> {
//     res.response_mut().set_body(format!("Error: {}",res.status()));
//     Ok(ErrorHandlerResponse::Response(res.map_into_left_body()))
// }
