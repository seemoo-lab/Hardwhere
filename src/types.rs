//! Type definitions
use std::collections::HashMap;
use std::sync::Mutex;
use std::time::Duration;
use std::time::Instant;

use serde::Deserialize;
use serde::Serialize;

pub type UID = i32;
pub type AssetId = i32;

pub const API_KEY: &'static str = "api_key";
/// Cookie session TTL, encoded into encrypted data
pub const SESSION_TTL_KEY: &'static str = "session_ttl";

pub struct AutoLogin {
    api_token: String,
    valid_till: Instant,
}

impl AutoLogin {
    pub fn new(api_token: String, ttl: Duration) -> Self {
        Self {
            api_token,
            valid_till: Instant::now() + ttl,
        }
    }

    pub fn api_token(self) -> Option<String> {
        if Instant::now() < self.valid_till {
            Some(self.api_token)
        } else {
            None
        }
    }
}

pub type AutoLoginTokens = actix_web::web::Data<Mutex<HashMap<String, AutoLogin>>>;

#[derive(Debug, Deserialize)]
pub struct AutoLoginPrepare {
    pub api_token: String,
    pub login_token: String,
}

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
    pub action_type: MaybeActionType,
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
    User,
}

#[derive(Deserialize, Debug, Eq, PartialEq)]
#[serde(untagged)]
pub enum MaybeActionType {
    Known(ActionType),
    UnknownActionType(serde_json::value::Value),
}

// Snipeit API returns localized types for the audit history (v5.X)
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
pub struct AssetModelList {
    pub total: usize,
    pub rows: Vec<AssetModel>,
}

#[derive(Deserialize, Debug)]
pub struct AssetModel {
    pub id: i32,
    pub name: String,
    pub fieldset: Option<Fieldset>,
}

#[derive(Deserialize, Debug)]
pub struct FieldsetList {
    pub total: usize,
    pub rows: Vec<Fieldset>,
}

#[derive(Deserialize, Debug)]
pub struct Fieldset {
    pub id: i32,
    pub name: String,
    // other fields ignored
}

#[derive(Deserialize, Debug)]
pub struct AssetList {
    pub total: usize,
    pub rows: Vec<Asset>,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct Asset {
    pub id: AssetId,
    pub name: String,
    pub asset_tag: String,
    pub model: AssetModelLight,
    #[serde(default)]
    pub assigned_to: Option<MaybeAssignee>,
    pub custom_fields: Option<serde_json::Value>,
    pub expected_checkin: Option<DateField>,
}

/// Date representation by snipeit
#[derive(Serialize, Deserialize, Debug)]
pub struct DateField {
    date: String,
    formatted: String,
}

#[derive(Serialize, Deserialize, Debug)]
pub struct AssetModelLight {
    pub id: i32,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum MaybeAssignee {
    Known(Assignee),
    UnknownAssignee(serde_json::value::Value),
}

/// Asset assigned_to enum
#[derive(Serialize, Deserialize, Debug)]
#[serde(untagged)]
pub enum Assignee {
    User(AssigneeUser),
    Location(AssigneeLocation),
}

#[derive(Serialize, Deserialize, Debug)]
pub struct AssigneeLocation {
    id: UID,
    name: String,
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
    pub payload: Option<HashMap<String, serde_json::Value>>,
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
}

#[derive(Deserialize, Debug)]
#[serde(untagged)]
pub enum SnipeItMessage {
    Many(Vec<HashMap<String, Vec<String>>>),
    Single(String),
}

#[derive(Serialize, Deserialize, Debug)]
#[allow(non_camel_case_types)]
pub enum SUCCESS_STATUS {
    success,
    error,
}

#[derive(Deserialize, Debug)]
pub struct CheckoutRequest {
    pub asset: AssetId,
    pub user: UID,
}

#[derive(Deserialize, Debug)]
pub struct CheckinRequest {
    pub asset: AssetId,
}

#[derive(Deserialize)]
pub struct LoginData {
    pub api_key: String,
}

/// Send only patch data for models
#[derive(Serialize, Default)]
pub struct ModelPatch {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub fieldset_id: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub name: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub manufacturer_id: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub category_id: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub depreciation_id: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub notes: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub requestable: Option<bool>,
}
