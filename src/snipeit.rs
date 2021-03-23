use std::convert::TryInto;

use actix_web::{HttpRequest, client::{Client, ClientResponse}, http::{HeaderValue, header::AUTHORIZATION}};
use serde::Deserialize;
use serde_json::json;

use crate::prelude::*;
use crate::types::*;

const ASSIGNED_TO: &str = "assigned_to";

pub async fn user(token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<User> {
    // https://github.com/snipe/snipe-it/issues/7626#issuecomment-702354445
    let mut res = client.get(format!("{}/api/v1/users/me",snipeit_url))
        .header(AUTHORIZATION,token)
        .send().await?;
    verify_status(&res)?;
    let user: User = res.json().await?;

    Ok(user)
}

pub fn token(request: &HttpRequest) -> Result<HeaderValue> {
    let token = match request.headers().get(AUTHORIZATION) {
        Some(v) => v,
        None => return Err(Error::MissingAuthorization),
    };
    Ok(token.clone())
}

pub async fn checkout(asset: AssetId, user: UID, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<()> {
    let mut response = client.post(format!("{}/api/v1/hardware/{}/checkout",snipeit_url,asset))
        .header(AUTHORIZATION,token)
        .send_json(&AssetCheckout::new(user))
        .await?;
        // TODO: add support for more than user checkout
    verify_status(&response)?;
    // let body_inscp = response.body().await.unwrap();
    // trace!("{}",std::str::from_utf8(&body_inscp).unwrap());
    let res: SnipeitResult = response.json().await?;
    res.check()?;
    info!("{:?}",res);
    Ok(())
}

pub async fn checkin(asset: AssetId, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<()> {
    let mut response = client.post(format!("{}/api/v1/hardware/{}/checkin",snipeit_url,asset))
        .header(AUTHORIZATION,token)
        .send()
        .await?;
    verify_status(&response)?;
    // let body_inscp = response.body().await.unwrap();
    // trace!("{}",std::str::from_utf8(&body_inscp).unwrap());
    let res: SnipeitResult = response.json().await?;
    res.check()?;
    info!("{:?}",res);
    Ok(())
}

fn verify_payload(res: SnipeitResult, key: &'static str, expected: serde_json::Value) -> Result<()> {
    let mut payload = match res.payload {
        Some(v) => v,
        None => return Err(Error::Snipeit(format!("No payload, expected one with {}",key))),
    };
    if payload.get(key) != Some(&expected) {
        return Err(Error::SnipeitPayloadError{
            key: key,
            expected: expected,
            found: payload.remove(key),
        });
    }
    Ok(())
}

fn verify_status<T>(response: &ClientResponse<T>) -> Result<()>{
    if !response.status().is_success() {
        return Err(Error::Snipeit(format!("{:?}",response)));
    }
    Ok(())
}