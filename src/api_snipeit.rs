use actix_web::{HttpRequest, client::Client, http::header::AUTHORIZATION};

use crate::prelude::*;

pub async fn user_id(request: &HttpRequest, client: &Client, snipeit_url: &str) -> Result<u32> {
    let token = match request.headers().get(AUTHORIZATION) {
        Some(v) => v,
        None => return Err(Error::MissingAuthorization),
    };
    let res = client.get(format!("{}/api/users/me",snipeit_url))
        .header(AUTHORIZATION,token.clone())
        .send().await?;

    Ok(1)
}