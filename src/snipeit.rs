use actix_web::{HttpRequest, client::{Client, ClientResponse}, http::{HeaderValue, header::AUTHORIZATION}};


use crate::prelude::*;
use crate::types::*;

const ASSIGNED_TO: &str = "assigned_to";

/// Asset activity
pub async fn activity(item: AssetId, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<Vec<Activity>> {
    // snipeit API docs are invalid, we need URL parms, not body json stuff..

    // example
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=990&item_type=asset
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=869&item_type=asset
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=905&item_type=asset
    let mut res = client.get(format!("{}/api/v1/reports/activity?item_type=asset&item_id={}",snipeit_url,item))
        .header(AUTHORIZATION,token)
        .send().await?;
    verify_status(&res)?;
    let data: ActivityList = res.json().limit(1024*1024).await?;
    Ok(data.rows)
}

/// Get Assets unconditionally, start from offset to limit (if API limits allow this)
pub async fn assets(offset: i32, limit: i32, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<AssetList> {
    let mut res = client.get(format!("{}/api/v1/hardware?offset={}&limit={}",snipeit_url,offset,limit))
        .header(AUTHORIZATION,token)
        .send().await?;
    verify_status(&res)?;
    let data: AssetList = res.json().limit(1024*1024).await?;
    Ok(data)
}

/// Get Asset by ID
pub async fn asset(id: AssetId, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<Asset> {
    let mut res = client.get(format!("{}/api/v1/hardware/{}",snipeit_url,id))
        .header(AUTHORIZATION,token)
        .send().await?;
    verify_status(&res)?;
    let data: Asset = res.json().await?;
    Ok(data)
}

/// Get own user from API token
pub async fn user(token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<User> {
    // https://github.com/snipe/snipe-it/issues/7626#issuecomment-702354445
    let mut res = client.get(format!("{}/api/v1/users/me",snipeit_url))
        .header(AUTHORIZATION,token)
        .send().await?;
    verify_status(&res)?;
    let user: User = res.json().await?;

    Ok(user)
}

/// Get token from request
pub fn token(request: &HttpRequest) -> Result<HeaderValue> {
    let token = match request.headers().get(AUTHORIZATION) {
        Some(v) => v,
        None => return Err(Error::MissingAuthorization),
    };
    Ok(token.clone())
}

/// Checkout asset to user
pub async fn checkout(asset: AssetId, user: UID, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<()> {
    let mut response = client.post(format!("{}/api/v1/hardware/{}/checkout",snipeit_url,asset))
        .header(AUTHORIZATION,token)
        .send_json(&AssetCheckout::new(user))
        .await?;
    // TODO: add support for more than user checkout
    if !response.status().is_success() {
        if let Ok(v) = response.json::<SnipeitResult>().await {
            return Err(Error::Snipeit(v));
        } else {
            return Err(Error::SnipeitBadRequest(format!("{:?}",response)));
        }
    } else {
        let res: SnipeitResult = response.json().await?;
        res.check()?;
    }
    // let body_inscp = response.body().await.unwrap();
    // trace!("{}",std::str::from_utf8(&body_inscp).unwrap());
    Ok(())
}

/// Checkin/return asset unconditionally
pub async fn checkin(asset: AssetId, token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<()> {
    let mut response = client.post(format!("{}/api/v1/hardware/{}/checkin",snipeit_url,asset))
        .header(AUTHORIZATION,token)
        .send()
        .await?;
    if !response.status().is_success() {
        if let Ok(v) = response.json::<SnipeitResult>().await {
            return Err(Error::Snipeit(v));
        } else {
            return Err(Error::SnipeitBadRequest(format!("{:?}",response)));
        }
    } else {
        let res: SnipeitResult = response.json().await?;
        res.check()?;
    }
    // let body_inscp = response.body().await.unwrap();
    // trace!("{}",std::str::from_utf8(&body_inscp).unwrap());
    Ok(())
}

fn verify_payload(res: SnipeitResult, key: &'static str, expected: serde_json::Value) -> Result<()> {
    let mut payload = match res.payload {
        Some(v) => v,
        None => return Err(Error::SnipeitBadRequest(format!("No payload, expected one with {}",key))),
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

/// Verify generic response by http status code
fn verify_status<T>(response: &ClientResponse<T>) -> Result<()>{
    if !response.status().is_success() {
        return Err(Error::SnipeitBadRequest(format!("{:?}",response)));
    }
    Ok(())
}