use actix_web::{client::ClientBuilder, http::{HeaderValue, header::{ACCEPT, CONTENT_TYPE}}};
use mysql_async::{Pool, prelude::Queryable};

use crate::{cfg::Main, prelude::*, snipeit, types::Assignee};


pub async fn refresh_index(config: &Main, db: Pool) -> Result<()>{
    let client = ClientBuilder::new().header(ACCEPT,"application/json").header(CONTENT_TYPE,"application/json").finish();
    let token = HeaderValue::from_str(&format!("Bearer {}",config.snipeit_system_token)).unwrap();
    trace!("Requesting total item..");
    let total = snipeit::assets(0, 1, token.clone(), &client, &config.snipeit_url).await?.total;
    trace!("Total: {}",total);
    let mut done = 0;
    let mut conn = db.get_conn().await?;
    let limit = 200;
    while done < total {
        trace!("Requesting assets {}-{}",done,done+limit);
        let data = snipeit::assets(done, limit, token.clone(), &client, &config.snipeit_url).await?;
        trace!("Received {} items",data.rows.len());
        done += data.rows.len() as i32;
        for asset in data.rows {
            match asset.assigned_to {
                Some(Assignee::User(u)) => {
                    conn.exec_drop("INSERT INTO `lent` (`asset`,`user`) VALUES(?,?) ON DUPLICATE KEY UPDATE `user`=VALUES(`user`)",(asset.id,u.id)).await?;
                },
                Some(v) => warn!("Unsupported assignee! asset {}: {:?}",asset.id,v),
                None => {
                    conn.exec_drop("DELETE FROM `lent` WHERE asset = ?",(asset.id,)).await?;
                }
            }
        }
    }
    Ok(())
}