use actix_session::Session;
use actix_web::{HttpResponse, get, web};
use handlebars::Handlebars;
use serde_json::json;

use crate::prelude::*;

pub(crate) async fn view(hb: web::Data<Handlebars<'_>>, session: web::Data<Session>) -> Result<HttpResponse> {
    let body = if let Some(api_key) = session.get::<String>("api_key")? {
        println!("SESSION api key: {}", api_key);
    } else {
        let data = json!({
            "name": "Handlebars"
        });
        let body = hb.render("login", &data).unwrap();
    };

    

    Ok(HttpResponse::Ok().body(body))
}