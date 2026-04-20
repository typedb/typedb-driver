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
use std::{process::Command, time::Duration};

use async_std::task::sleep;
use futures::TryStreamExt;
use serial_test::serial;
use typedb_driver::{
    answer::ConceptRow, Addresses, Credentials, DriverOptions, DriverTlsConfig, Server, TransactionOptions,
    TransactionType, TypeDBDriver,
};

const ADDRESSES: [&str; 3] = ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"];
const USERNAME: &str = "admin";
const PASSWORD: &str = "password";
const DATABASE_NAME: &str = "test-failover";
const FAILOVER_ITERATIONS: usize = 10;
const POST_KILL_WAIT_SECS: u64 = 5;
const PRIMARY_POLL_RETRIES: usize = 20;
const PRIMARY_POLL_INTERVAL_SECS: u64 = 2;
const CLUSTER_NODE_SCRIPT: &str = "tool/test/cluster-server.sh";

fn cluster_node(command: &str, node_id: &str) {
    let status = Command::new(CLUSTER_NODE_SCRIPT)
        .args([command, node_id])
        .status()
        .unwrap_or_else(|e| panic!("Failed to run {}: {}", CLUSTER_NODE_SCRIPT, e));
    assert!(
        status.success(),
        "{} {} {} failed with exit code {:?}",
        CLUSTER_NODE_SCRIPT,
        command,
        node_id,
        status.code()
    );
}

fn node_id_from_address(address: &str) -> String {
    let port = address.rsplit_once(':').map(|(_, p)| p).unwrap();
    port[0..1].to_string()
}

async fn create_driver() -> TypeDBDriver {
    TypeDBDriver::new(
        Addresses::try_from_addresses_str(ADDRESSES.iter()).unwrap(),
        Credentials::new(USERNAME, PASSWORD),
        DriverOptions::new(DriverTlsConfig::disabled()),
    )
    .await
    .expect("Failed to create driver")
}

async fn get_primary_server(driver: &TypeDBDriver) -> Server {
    for attempt in 0..PRIMARY_POLL_RETRIES {
        if let Ok(Some(server)) = driver.primary_server().await {
            return Server::Available(server);
        }
        if attempt < PRIMARY_POLL_RETRIES - 1 {
            println!(
                "  No primary server found (attempt {}/{}). Retrying in {}s...",
                attempt + 1,
                PRIMARY_POLL_RETRIES,
                PRIMARY_POLL_INTERVAL_SECS
            );
            sleep(Duration::from_secs(PRIMARY_POLL_INTERVAL_SECS)).await;
        }
    }
    panic!("Retry limit exceeded while seeking a primary server.");
}

async fn setup_database(driver: &TypeDBDriver) {
    if driver.databases().contains(DATABASE_NAME).await.unwrap() {
        driver.databases().get(DATABASE_NAME).await.unwrap().delete().await.unwrap();
    }
    driver.databases().create(DATABASE_NAME).await.unwrap();

    let transaction = driver
        .transaction_with_options(DATABASE_NAME, TransactionType::Schema, TransactionOptions::default())
        .await
        .unwrap();
    transaction.query("define entity person;").await.unwrap();
    transaction.commit().await.unwrap();
}

async fn verify_read_query(driver: &TypeDBDriver) {
    let transaction = driver
        .transaction_with_options(DATABASE_NAME, TransactionType::Read, TransactionOptions::default())
        .await
        .unwrap();
    let answer = transaction.query("match entity $t;").await.unwrap();
    let rows: Vec<ConceptRow> = answer.into_rows().try_collect().await.unwrap();
    assert!(!rows.is_empty(), "Expected at least one entity type in read query results");
}

async fn cleanup_database() {
    if let Ok(driver) = TypeDBDriver::new(
        Addresses::try_from_addresses_str(ADDRESSES.iter()).unwrap(),
        Credentials::new(USERNAME, PASSWORD),
        DriverOptions::new(DriverTlsConfig::disabled()),
    )
    .await
    {
        if driver.databases().contains(DATABASE_NAME).await.unwrap_or(false) {
            let _ = driver.databases().get(DATABASE_NAME).await.unwrap().delete().await;
        }
    }
}

#[test]
#[serial]
fn primary_failover() {
    async_std::task::block_on(async {
        println!("=== Cluster Failover Test ===");

        // Connect driver (cluster must already be running via start-cluster-servers.sh)
        println!("Connecting driver...");
        let driver = create_driver().await;

        // Setup database with schema
        println!("Setting up database and schema...");
        setup_database(&driver).await;
        verify_read_query(&driver).await;
        println!("Initial setup verified.");

        // Failover loop
        for iteration in 1..=FAILOVER_ITERATIONS {
            println!("\n--- Failover iteration {}/{} ---", iteration, FAILOVER_ITERATIONS);

            let primary = get_primary_server(&driver).await;
            let primary_address = primary.address().unwrap().to_string();
            let node_id = node_id_from_address(&primary_address);
            println!("  Primary server: {} (node {})", primary_address, node_id);

            println!("  Killing node {}...", node_id);
            cluster_node("kill", &node_id);

            println!("  Waiting {}s for re-election...", POST_KILL_WAIT_SECS);
            sleep(Duration::from_secs(POST_KILL_WAIT_SECS)).await;

            println!("  Verifying read query on new primary...");
            verify_read_query(&driver).await;
            println!("  Read query succeeded.");

            println!("  Restarting node {}...", node_id);
            cluster_node("start", &node_id);
            cluster_node("await", &node_id);
            println!("  Node {} restarted.", node_id);
        }

        println!("\n=== All {} failover iterations passed! ===", FAILOVER_ITERATIONS);
    });

    async_std::task::block_on(async {
        cleanup_database().await;
    });
}
