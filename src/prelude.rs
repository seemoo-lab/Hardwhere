
use actix_web::{HttpResponse, ResponseError, client::SendRequestError};
pub use log::*;
use thiserror::Error;
#[derive(Error, Debug)]
pub enum Error {
    #[error("IO error")]
    Io(#[from] std::io::Error),
    #[error("Internal framework error")]
    Actix(#[from] actix_web::error::Error),
    #[error("DB error")]
    DB(#[from] mysql_async::Error),
    #[error("Config error")]
    Config(#[from] toml::de::Error),
    #[error("Request invalid, missing valid authorization")]
    MissingAuthorization,
    #[error("Invalid login token")]
    InvalidAuthorization,
    #[error("Failed to send snipeit API cmd")]
    ClientError(#[from] SendRequestError)
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