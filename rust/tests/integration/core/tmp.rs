use serial_test::serial;
use typedb_driver::TypeDBDriver;

#[test]
#[serial]
fn tmp() {
    async_std::task::block_on(async {
        let driver: TypeDBDriver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();
        let users = driver.users();
        users.create("user", "password").await.unwrap();
    });
}