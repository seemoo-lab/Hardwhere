use actix_session::Session;
use actix_web::{HttpResponse, client::Client, get, http::HeaderValue, web::{self, Data}};
use handlebars::Handlebars;
use mysql_async::Pool;
use serde_json::json;

use crate::{cfg::Main, prelude::*, snipeit, types::LoginData, api};

pub(crate) async fn view(db: Data<Pool>, hb: web::Data<Handlebars<'_>>, session: Session, client: Data<Client>, cfg: web::Data<Main>,) -> Result<HttpResponse> {
    let body = if let Some(api_key) = session.get::<String>("api_key")? {
        let token = HeaderValue::from_str(&api_key)?;
        let user = snipeit::user(token.clone(), &client, &cfg.snipeit_url).await?;
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

pub async fn login(params: web::Form<LoginData>, hb: web::Data<Handlebars<'_>>, session: Session, client: Data<Client>, cfg: web::Data<Main>,) -> Result<HttpResponse> {
    trace!("Login action");
    let params = params.into_inner();
    let api_token = format!("Bearer {}",params.api_key);
    let token = HeaderValue::from_str(&api_token)?;
    let user = match snipeit::user(token, &client, &cfg.snipeit_url).await {
        Ok(u) => u,
        Err(Error::Snipeit(s)) => {
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
    session.set("api_key", api_token)?;

    Ok(HttpResponse::Found().header("location", "/").body(body))
}

pub async fn logout(hb: web::Data<Handlebars<'_>>, session: Session) -> Result<HttpResponse> {
    session.purge();
    let body = hb.render("logout", &())?;
    Ok(HttpResponse::Found().header("location", "/").body(body))
}