

use actix_web::{HttpResponse, ResponseError, client::{ClientResponse, JsonPayloadError, SendRequestError}};
pub use log::*;
use thiserror::Error;

use crate::types::AssetId;
#[derive(Error, Debug)]
pub enum Error {
    #[error("IO error {0}")]
    Io(#[from] std::io::Error),
    #[error("Internal framework error {0}")]
    Actix(#[from] actix_web::error::Error),
    #[error("DB error {0}")]
    DB(#[from] mysql_async::Error),
    #[error("Config error {0}")]
    Config(#[from] toml::de::Error),
    #[error("Request invalid, missing valid authorization")]
    MissingAuthorization,
    #[error("Invalid login token")]
    InvalidAuthorization,
    #[error("Failed to send snipeit API cmd {0}")]
    ClientError(#[from] SendRequestError),
    #[error("Invalid snipeit response {0:?}")]
    Snipeit(String),
    #[error("Can't parse response from snipeit {0}")]
    SnipeitJsonResponse(#[from] JsonPayloadError),
    #[error("Invalid payload, expected {expected:?} for {key} got {found:?}")]
    SnipeitPayloadError{
        key: &'static str,
        expected: serde_json::Value,
        found: Option<serde_json::Value>,
    },
    #[error("No correct checkout activity found in the logs for {0}")]
    NoCheckoutActivity(AssetId)
}

impl ResponseError for Error {
    fn error_response(&self) -> HttpResponse {
        error!("Error {}",self);
        match self {
            MissingAuthorization => HttpResponse::BadRequest().finish(),
            InvalidAuthorization => HttpResponse::Unauthorized().finish(),
            _ => HttpResponse::InternalServerError().finish(),
        }
        
    }
}

pub type Result<T> = std::result::Result<T,Error>;