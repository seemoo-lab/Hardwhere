use std::collections::HashMap;

use serde::Deserialize;
use serde::Serialize;

pub type UID = i32;
pub type AssetId = i32;

#[derive(Deserialize, Debug)]
pub struct AssetList {
    pub total: i32,
    pub rows: Vec<Asset>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Asset {
    pub id: AssetId,
    pub name: String,
    pub asset_tag: String,
    pub model: AssetModel,
    #[serde(default)]
    pub assigned_to: Option<Assignee>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct AssetModel {
    pub id: i32,
    pub name: String
}

/// Asset assigned_to enum
#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum Assignee {
    User(AssigneeUser),
}

#[derive(Serialize, Deserialize, Debug)]
/// Asset assigned_to of type user
pub struct AssigneeUser {
    pub id: UID,
    pub username: String,
    pub name: String,
    pub first_name: String,
    pub last_name: String,
}

#[derive(Deserialize, Debug)]
pub struct User {
    pub id: UID,
    pub first_name: String,
    pub last_name: String,
    pub activated: bool,
    pub permissions: Permissions,
}

#[derive(Deserialize, Debug)]
pub struct Permissions {
    // TODO: use parsing to boolean from "1"/"0"
    pub superuser: String,
}

// see https://github.com/snipe/snipe-it/blob/33cf4896d8a1252aa83ee325a4ebbac35ac94b32/tests/api/ApiCheckoutAssetsCest.php#L29-L33
/// Data wrapper for snipeit checkout assets
#[derive(Serialize, Debug)]
pub struct AssetCheckout {
    assigned_user: UID,
    checkout_to_type: &'static str,
}

impl AssetCheckout {
    pub fn new(user: UID) -> Self {
        Self {
            assigned_user: user,
            checkout_to_type: "user",
        }
    }
}

/// Snipeit request success response
#[derive(Deserialize, Debug)]
pub struct SnipeitResult {
    pub status: SUCCESS_STATUS,
    pub messages: String,
    #[serde(default)]
    pub payload: Option<HashMap<String,serde_json::Value>>,
}

impl SnipeitResult {
    /// Check for success
    #[must_use = "has to check for errors"]
    pub fn check(&self) -> crate::prelude::Result<()> {
        match self.status {
            SUCCESS_STATUS::success => Ok(()),
            SUCCESS_STATUS::error => Err(crate::prelude::Error::Snipeit(format!("{:?}",self))),
        }
    }
}

#[derive(Deserialize, Debug)]
#[serde(untagged)]
pub enum SnipeItMessage {
    Many(Vec<HashMap<String,Vec<String>>>),
    Single(String)
}

#[derive(Deserialize, Debug)]
pub enum SUCCESS_STATUS {
    success,
    error
}

#[derive(Deserialize, Debug)]
pub struct CheckoutRequest {
    pub asset: AssetId,
    pub user: UID,
}

#[derive(Deserialize, Debug)]
pub struct CheckinRequest {
    pub asset: AssetId
}