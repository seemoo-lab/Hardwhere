use crate::prelude::*;
use serde::Deserialize;
use serde::Serialize;
use std::fs::read_to_string;
use std::time::Duration;
use toml;

/// Config wrapper handling loading & validation
#[derive(Serialize, Deserialize, Debug, Default)]
pub struct Cfg {
    pub main: Main,
    pub db: DB,
}

#[derive(Serialize, Deserialize, Debug, Default)]
pub struct Main {
    /// Used to crawling the state and refreshing the DB, NOT for user checkouts
    pub snipeit_system_token: String,
    pub snipeit_url: String,
    pub listen_ip: String,
    pub listen_port: u16,
    pub admin_user: String,
    pub admin_password: String,
    pub session_encryption_key: String,
    pub session_secure: bool,
    pub autologin_token_ttl: u32,
    pub default_fieldset: String,
    pub indexing_refresh_seconds: u64,
}

#[derive(Serialize, Deserialize, Debug, Default)]
pub struct DB {
    pub ip: String,
    pub user: String,
    pub password: String,
    pub name: String,
}

impl Cfg {
    pub fn load() -> Result<Self> {
        let file = read_to_string("config.toml")?;
        let mut cfg: Self = toml::from_str(&file)?;
        let trimmed = cfg.main.snipeit_url.trim_end_matches('/');
        if trimmed != cfg.main.snipeit_url {
            cfg.main.snipeit_url = trimmed.to_string();
        }
        Ok(cfg)
    }
}

#[cfg(test)]
mod test {
    use super::*;
    #[test]
    fn export_config() {
        let config = Cfg::default();
        let toml = toml::to_string(&config).unwrap();
        println!("{}", toml);
    }
}