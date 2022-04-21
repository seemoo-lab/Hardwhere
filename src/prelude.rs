//! Error types and default imports

use actix_web::{http::header::InvalidHeaderValue, HttpResponse, ResponseError};
use awc::error::{JsonPayloadError, SendRequestError};
use handlebars::RenderError;
pub use log::*;
use thiserror::Error;

use crate::types::{AssetId, SnipeitResult};
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
    #[error("Failed to send snipeit API cmd {0}")]
    ClientError(#[from] SendRequestError),
    #[error("Invalid snipeit request: response {0:?}")]
    SnipeitBadRequest(String),
    #[error("Invalid snipeit response with result {0:?}")]
    Snipeit(SnipeitResult),
    #[error("Can't parse response from snipeit {0}")]
    SnipeitJsonResponse(#[from] JsonPayloadError),
    #[error("No correct checkout activity found in the logs for {0}")]
    NoCheckoutActivity(AssetId),
    #[error("Invalid header value")]
    InvalidHeaderValue(#[from] InvalidHeaderValue),
    #[error("Failed to render template")]
    RenderError(#[from] RenderError),
    #[error("Failed to find fieldset with name {0}")]
    FieldsetNotFound(String),
    #[error("Failed to deserialize value {0}")]
    JsonDeserialize(#[from] serde_json::Error),
}

impl ResponseError for Error {
    fn error_response(&self) -> HttpResponse {
        error!("Error {}", self);
        match self {
            Error::MissingAuthorization => HttpResponse::BadRequest().finish(),
            Error::Snipeit(v) => HttpResponse::BadRequest().json(v),
            _ => HttpResponse::InternalServerError().finish(),
        }
    }
}

pub type Result<T> = std::result::Result<T, Error>;
