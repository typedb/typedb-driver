use serial_test::serial;
use typedb_driver::TypeDBDriver;

#[test]
#[serial]
fn tmp() {
    async_std::task::block_on(async {
        let driver: TypeDBDriver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();
        let users = driver.users();
        // let x = users.contains("test").await;
        // users.create("user", "password").await.unwrap();
        // users.get("user").await.unwrap()
        users.delete("user").await.unwrap()
    });
    todo!()
}