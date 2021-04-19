use std::collections::HashMap;

use serde::Deserialize;
use serde::Serialize;

pub type UID = i32;
pub type AssetId = i32;

#[derive(Deserialize, Debug)]
pub struct ActivityList {
    pub total: usize,
    pub rows: Vec<Activity>,
}

/// Activity Report
#[derive(Deserialize, Debug)]
pub struct Activity {
    /// Activity/Report ID, not asset/item
    pub id: i32,
    /// Person that made this change
    pub admin: ActivityAdmin,
    /// Change type
    pub action_type: ActionType,
    /// Item that got changed
    pub item: ActivityItem,
}

#[derive(Deserialize, Debug)]
pub struct ActivityAdmin {
    pub id: UID,
    pub name: String,
    pub first_name: String,
    pub last_name: String,
}

#[derive(Deserialize, Debug)]
pub struct ActivityItem {
    pub id: AssetId,
    pub r#type: ItemType,
}

#[derive(Deserialize, Debug)]
pub enum ItemType {
    #[serde(rename = "asset")]
    Asset,
    #[serde(rename = "user")]
    User
}

#[derive(Deserialize, Debug, Eq, PartialEq)]
pub enum ActionType {
    #[serde(rename = "aktualisieren")]
    Update,
    #[serde(rename = "herausgeben")]
    Checkout,
    #[serde(rename = "hinzufügen")]
    Add,
    #[serde(rename = "zurücknehmen von")]
    Checkin,
    #[serde(rename = "hochgeladen")]
    Upload,
}

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
    pub custom_fields: Option<serde_json::Value>
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
#[derive(Serialize, Deserialize, Debug)]
pub struct SnipeitResult {
    pub status: SUCCESS_STATUS,
    pub messages: String,
    #[serde(default)]
    pub payload: Option<HashMap<String,serde_json::Value>>,
}

impl SnipeitResult {
    /// Check for success
    #[must_use = "has to check for errors"]
    pub fn check(self) -> crate::prelude::Result<Self> {
        match self.status {
            SUCCESS_STATUS::success => Ok(self),
            SUCCESS_STATUS::error => Err(crate::prelude::Error::Snipeit(self)),
        }
    }

    pub fn success() -> Self {       
        Self {
            status: SUCCESS_STATUS::success,
            messages: "".to_string(),
            payload: None,
        }
    }
}

#[derive(Deserialize, Debug)]
#[serde(untagged)]
pub enum SnipeItMessage {
    Many(Vec<HashMap<String,Vec<String>>>),
    Single(String)
}

#[derive(Serialize, Deserialize, Debug)]
#[allow(non_camel_case_types)]
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

#[derive(Deserialize)]
pub struct LoginData {
    pub api_key: String
}