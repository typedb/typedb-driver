use std::path::Path;
use serial_test::serial;
use typedb_driver::{ConnectionSettings, Credential, TypeDBDriver};

#[test]
#[serial]
fn tmp() {
    async_std::task::block_on(async {
        let root_ca = Path::new("/Users/lolski/Repositories/typedb/typedb/typedb-all-mac-arm64/tls-3/root-ca.pem");
        // let root_ca = Path::new("/Users/lolski/Repositories/typedb/typedb/typedb-all-mac-arm64/tls-2/root-ca.pem");
        // let root_ca = Path::new("/Users/lolski/Repositories/typedb/typedb/typedb-all-mac-arm64/tls/root-ca.pem");
        let driver: TypeDBDriver = TypeDBDriver::new_core(
            "https://127.0.0.1:1729",
            Credential::new("admin", "password"),
            ConnectionSettings::new(true, None).unwrap()
        ).await.unwrap();
        // let users = driver.users();
        // users.create("user", "password").await.unwrap();
    });
}