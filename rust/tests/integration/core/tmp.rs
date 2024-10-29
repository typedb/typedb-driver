use std::path::Path;
use serial_test::serial;
use typedb_driver::{ConnectionSettings, Credential, TypeDBDriver};

#[test]
#[serial]
fn tmp() {
    async_std::task::block_on(async {
        let driver: TypeDBDriver = TypeDBDriver::new_core(
            "127.0.0.1:1729",
            Credential::new("admin", "password"),
            ConnectionSettings::new(false, None).unwrap()
        ).await.unwrap();
        let db_mgr = driver.databases();
        println!("{:?}", db_mgr.all().await.unwrap());
        todo!("END")
    });
}