//! Snipeit API functions
use actix_web::{http::header::AUTHORIZATION, HttpRequest};
use awc::ClientResponse;
use awc::{error::HeaderValue, Client};

use crate::prelude::*;
use crate::types::*;

/// Asset activity
pub async fn activity(
    item: AssetId,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<Vec<Activity>> {
    // snipeit API docs are invalid, we need URL parms, not body json stuff..

    // example
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=990&item_type=asset
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=869&item_type=asset
    // https://inventory.seemoo.tu-darmstadt.de/api/v1/reports/activity?item_id=905&item_type=asset
    let mut res = client
        .get(format!(
            "{}/api/v1/reports/activity?item_type=asset&item_id={}",
            snipeit_url, item
        ))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    verify_status(&res)?;
    let data: ActivityList = res.json().limit(1024 * 1024).await?;
    Ok(data.rows)
}

/// Asset Models
pub async fn models(
    offset: usize,
    limit: usize,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<AssetModelList> {
    let mut res = client
        .get(format!(
            "{}/api/v1/models?offset={}&limit={}",
            snipeit_url, offset, limit
        ))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    verify_status(&res)?;
    let data: AssetModelList = res.json().limit(1024 * 1024).await?;
    Ok(data)
}

/// Set model fields as specified
pub async fn patch_model(
    model: i32,
    data: &ModelPatch,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<()> {
    let mut resp = client
        .patch(format!("{}/api/v1/models/{}", snipeit_url, model))
        .append_header((AUTHORIZATION, token))
        .send_json(data)
        .await?;
    if !resp.status().is_success() {
        if let Ok(v) = resp.json::<SnipeitResult>().await {
            return Err(Error::Snipeit(v));
        } else {
            return Err(Error::SnipeitBadRequest(format!("{:?}", resp)));
        }
    } else {
        let data: serde_json::Value = resp.json().await?;
        debug!("parsing response for model patching {:?}", data);
        let res: SnipeitResult = serde_json::from_value(data)?;
        res.check()?;
        Ok(())
    }
}

/// Custom field sets
pub async fn fieldsets(
    offset: usize,
    limit: usize,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<FieldsetList> {
    let mut res = client
        .get(format!(
            "{}/api/v1/fieldsets?offset={}&limit={}",
            snipeit_url, offset, limit
        ))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    verify_status(&res)?;
    let data: FieldsetList = res.json().limit(1024 * 1024).await?;
    Ok(data)
}

/// Get Assets unconditionally, start from offset to limit (if API limits allow this)
pub async fn assets(
    offset: usize,
    limit: usize,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<AssetList> {
    let mut res = client
        .get(format!(
            "{}/api/v1/hardware?offset={}&limit={}",
            snipeit_url, offset, limit
        ))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    verify_status(&res)?;
    let data: AssetList = res.json().limit(1024 * 1024).await?;
    Ok(data)
}

/// Get Asset by ID
pub async fn asset(
    id: AssetId,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<Asset> {
    let mut res = client
        .get(format!("{}/api/v1/hardware/{}", snipeit_url, id))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    verify_status(&res)?;
    let data: Asset = res.json().await?;
    Ok(data)
}

/// Get own user from API token
pub async fn user(token: HeaderValue, client: &Client, snipeit_url: &str) -> Result<User> {
    // https://github.com/snipe/snipe-it/issues/7626#issuecomment-702354445
    let mut res = client
        .get(format!("{}/api/v1/users/me", snipeit_url))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
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
pub async fn checkout(
    asset: AssetId,
    user: UID,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<SnipeitResult> {
    let mut response = client
        .post(format!(
            "{}/api/v1/hardware/{}/checkout",
            snipeit_url, asset
        ))
        .append_header((AUTHORIZATION, token))
        .send_json(&AssetCheckout::new(user))
        .await?;
    // TODO: add support for more than user checkout
    if !response.status().is_success() {
        if let Ok(v) = response.json::<SnipeitResult>().await {
            return Err(Error::Snipeit(v));
        } else {
            return Err(Error::SnipeitBadRequest(format!("{:?}", response)));
        }
    } else {
        let res: SnipeitResult = response.json().await?;
        res.check()
    }
}

/// Checkin/return asset unconditionally
pub async fn checkin(
    asset: AssetId,
    token: HeaderValue,
    client: &Client,
    snipeit_url: &str,
) -> Result<SnipeitResult> {
    let mut response = client
        .post(format!("{}/api/v1/hardware/{}/checkin", snipeit_url, asset))
        .append_header((AUTHORIZATION, token))
        .send()
        .await?;
    if !response.status().is_success() {
        if let Ok(v) = response.json::<SnipeitResult>().await {
            return Err(Error::Snipeit(v));
        } else {
            return Err(Error::SnipeitBadRequest(format!("{:?}", response)));
        }
    } else {
        let res: SnipeitResult = response.json().await?;
        res.check()
    }
}

/// Verify generic response by http status code
fn verify_status<T>(response: &ClientResponse<T>) -> Result<()> {
    if !response.status().is_success() {
        return Err(Error::SnipeitBadRequest(format!("{:?}", response)));
    }
    Ok(())
}
