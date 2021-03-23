use crate::prelude::*;
use serde::Deserialize;
use serde::Serialize;
use std::{cmp::Ordering, fs::read_to_string, net::Ipv4Addr, path::PathBuf, time::Duration};
use toml;

/// Config wrapper handling loading & validation
#[derive(Serialize, Deserialize, Debug, Default)]
pub struct Cfg {
    pub main: Main,
    pub db: DB,
}

#[derive(Serialize, Deserialize, Debug, Default)]
pub struct Main {
    pub snipeit_url: String,
    pub listen_ip: String,
    pub listen_port: u16,
    pub admin_user: String,
    pub admin_password: String,
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
        let cfg: Self = toml::from_str(&file)?;
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