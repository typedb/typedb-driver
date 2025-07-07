/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
use std::{
    env, io,
    process::{Child, Command, ExitStatus},
    str::FromStr,
    time::Duration,
};

use async_std::task::sleep;
use futures::{StreamExt, TryStreamExt};
use serial_test::serial;
use typedb_driver::{
    answer::ConceptRow, Address, Addresses, Credentials, DriverOptions, Error, ServerReplica, TransactionOptions,
    TransactionType, TypeDBDriver,
};

const ADDRESSES: [&'static str; 3] = ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"];
const USERNAME: &'static str = "admin";
const PASSWORD: &'static str = "password";

async fn remove_test_database() {
    let driver = TypeDBDriver::new(
        Addresses::try_from_addresses_str(ADDRESSES.iter()).unwrap(),
        Credentials::new(USERNAME, PASSWORD),
        DriverOptions::new(),
    )
    .await
    .unwrap();
    if driver.databases().contains("typedb").await.unwrap() {
        driver.databases().get("typedb").await.unwrap().delete().await.unwrap();
    }
}

async fn kill_servers() -> Result<(), TestError> {
    for address in &ADDRESSES {
        let (_, port) = address.rsplit_once(':').unwrap();
        kill_server_replica_from_parts(port).await?;
    }
    Ok(())
}

#[test]
#[serial]
fn primary_reelection_read() {
    async_std::task::block_on(async {
        kill_servers().await.ok();

        for (i, address) in ADDRESSES.iter().enumerate() {
            let (_, port) = address.rsplit_once(':').unwrap();
            start_server_replica_from_parts(&(i + 1).to_string(), port).await;
        }
        sleep(Duration::from_secs(10)).await;

        remove_test_database().await;

        println!("Building the main driver");
        let driver = TypeDBDriver::new(
            Addresses::try_from_address_str(ADDRESSES[0]).unwrap(),
            Credentials::new(USERNAME, PASSWORD),
            DriverOptions::new(),
        )
        .await
        .unwrap();

        for (i, address) in ADDRESSES[1..].iter().enumerate() {
            driver.register_replica((i + 1) as u64, address.to_string()).await;
        }

        // sleep(Duration::from_secs(5)).await;
        // TODO: Does it need to return all the current replicas? Or is it not needed?
        // It's currently handled automatically, so we won't see the new replicas now after registering them!!!
        // let replicas = driver.replicas();

        println!("Registered replicas. Creating the database");
        driver.databases().create("typedb").await.unwrap();
        println!("Retrieving the database...");
        let database = driver.databases().get("typedb").await.unwrap();
        assert_eq!(database.name(), "typedb");

        println!("Created database {}. Initializing schema", database.name());
        {
            let transaction = driver
                .transaction_with_options(database.name(), TransactionType::Schema, TransactionOptions::default())
                .await
                .unwrap();
            transaction.query("define entity person;").await.unwrap();
            transaction.commit().await.unwrap();
        }

        verify_defined(&driver, database.name()).await;

        for iteration in 0..10 {
            let primary_replica = get_primary_replica(&driver).await;
            println!("Stopping primary replica (iteration {}). Testing retrieval from other replicas", iteration);
            kill_server_replica(&primary_replica).await.unwrap();

            sleep(Duration::from_secs(5)).await;
            verify_defined(&driver, database.name()).await;

            start_server_replica(&primary_replica).await;
        }

        println!("Done!");
    });

    async_std::task::block_on(async {
        println!("Cleanup!");
        remove_test_database().await;
        kill_servers().await.unwrap();
        println!("Successfully cleaned up!");
    })
}

fn start_server(index: &str) -> Child {
    // Command::new(format!("../{index}/typedb"))
    // TODO: Temporary, should be called in a better way
    Command::new(format!(
        "{}/tool/test/temp-cluster-server/typedb_cluster_server_bin",
        env::current_dir().unwrap().to_string_lossy()
    ))
    .args([
        "--server.address",
        &format!("127.0.0.1:{index}1729"),
        "--server-clustering-id",
        &format!("{index}"),
        "--server-clustering-address",
        &format!("127.0.0.1:{index}1730"),
        "--diagnostics.deployment-id",
        "test",
        "--server.encryption.enabled",
        "false",
        "--diagnostics.monitoring.port",
        &format!("{index}1731"),
        "--development-mode.enabled",
        "true",
    ])
    .spawn()
    .expect("Failed to start TypeDB server")
}

async fn get_primary_replica(driver: &TypeDBDriver) -> ServerReplica {
    for _ in 0..10 {
        if let Some(replica) = driver.primary_replica() {
            return replica;
        }
        println!("No primary replica yet. Retrying in 2s...");
        sleep(Duration::from_secs(2)).await;
    }
    panic!("Retry limit exceeded while seeking a primary replica.");
}

async fn verify_defined(driver: &TypeDBDriver, database_name: impl AsRef<str>) {
    let transaction = driver
        .transaction_with_options(database_name, TransactionType::Read, TransactionOptions::default())
        .await
        .unwrap();
    let answer = transaction.query("match entity $p;").await.unwrap();
    let rows: Vec<ConceptRow> = answer.into_rows().try_collect().await.unwrap();
    assert_eq!(rows.len(), 1);
    let row = rows.get(0).unwrap();
    let entity_type = row.get("p").unwrap().unwrap();
    assert_eq!(entity_type.is_entity_type(), true);
    assert_eq!(entity_type.get_label(), "person");
}

async fn start_server_replica(server_replica: &ServerReplica) {
    let address_parts = ServerReplicaAddressParts::from_str(&server_replica.address().to_string()).unwrap();
    start_server_replica_from_parts(&address_parts.replica_id, &address_parts.port).await
}

async fn start_server_replica_from_parts(replica_id: &str, port: &str) {
    println!("Starting server replica from parts: {replica_id}, port: {port}");
    let _child = start_server(replica_id);
    let mut attempts = 0;
    while attempts < 60 {
        sleep(Duration::from_secs(1)).await;
        let check = Command::new("lsof").args(["-i", &format!(":{}", port)]).output();
        if check.is_ok() {
            break;
        }
        attempts += 1;
    }
}

async fn kill_server_replica(server_replica: &ServerReplica) -> Result<(), TestError> {
    let address_parts = ServerReplicaAddressParts::from_str(&server_replica.address().to_string()).unwrap();
    kill_server_replica_from_parts(&address_parts.port).await
}

async fn kill_server_replica_from_parts(port: &str) -> Result<(), TestError> {
    let lsof = Command::new("lsof").args(["-i", &format!(":{}", port)]).output().expect("Failed to run lsof");

    let stdout = String::from_utf8_lossy(&lsof.stdout);
    let pid = stdout
        .lines()
        .find(|line| line.contains("LISTEN"))
        .and_then(|line| line.split_whitespace().nth(1))
        .ok_or(TestError::NoServerPid)?;

    let res = Command::new("kill").args(["-9", pid]).status();
    println!("Replica on port {port} killed");
    res.map(|_| ()).map_err(|_| TestError::NoServerProcess)
}

struct ServerReplicaAddressParts {
    replica_id: String,
    port: String,
}

impl FromStr for ServerReplicaAddressParts {
    type Err = Error;

    fn from_str(address: &str) -> typedb_driver::Result<Self> {
        let port = address.rsplit_once(':').map(|(_, port)| port.to_string()).unwrap();
        let replica_id = port[0..1].to_string();
        Ok(Self { replica_id, port })
    }
}

#[derive(Debug)]
enum TestError {
    NoServerPid,
    NoServerProcess,
}
