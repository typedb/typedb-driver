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
use std::{path::PathBuf, process::Command, time::Duration};

use async_std::task::sleep;
use futures::TryStreamExt;
use serial_test::serial;
use typedb_driver::{
    Addresses, Credentials, DriverOptions, DriverTlsConfig, Server, TransactionOptions, TransactionType, TypeDBDriver,
    answer::ConceptRow,
};

const ADDRESSES: [&str; 3] = ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"];
const USERNAME: &str = "admin";
const PASSWORD: &str = "password";
const DATABASE_NAME: &str = "test-failover";
const FAILOVER_ITERATIONS: usize = 10;
const PRIMARY_POLL_RETRIES: usize = 20;
const PRIMARY_POLL_INTERVAL_SECS: u64 = 2;
const PRIMARY_FAILOVER_RETRIES: usize = 5;

fn cluster_server(command: &str, node_id: &str) {
    let script =
        std::env::var("CLUSTER_SERVER_SCRIPT").expect("CLUSTER_SERVER_SCRIPT environment variable must be set");
    let mut cmd = Command::new(&script);
    cmd.args([command, node_id]);
    if std::env::var("CLUSTER_DIR").is_err() {
        if let Ok(workspace) = std::env::var("BUILD_WORKSPACE_DIRECTORY") {
            cmd.env("CLUSTER_DIR", &workspace);
        }
    }
    let status = cmd.status().unwrap_or_else(|e| panic!("Failed to run {}: {}", script, e));
    assert!(status.success(), "{} {} {} failed with exit code {:?}", script, command, node_id, status.code());
}

fn ensure_all_nodes_up() {
    for i in 1..=ADDRESSES.len() {
        let id = i.to_string();
        // start is a no-op if already running; await blocks until ready
        cluster_server("start", &id);
        cluster_server("await", &id);
    }
}

fn node_id_from_address(address: &str) -> String {
    let port = address.rsplit_once(':').map(|(_, p)| p).unwrap();
    port[0..1].to_string()
}

fn tls_root_ca() -> PathBuf {
    PathBuf::from(std::env::var("ROOT_CA").expect("ROOT_CA environment variable must be set"))
}

async fn create_driver() -> TypeDBDriver {
    for attempt in 0..PRIMARY_POLL_RETRIES {
        let tls_config = DriverTlsConfig::enabled_with_root_ca(&tls_root_ca()).expect("Failed to create TLS config");
        let driver_options = DriverOptions::new(tls_config).primary_failover_retries(PRIMARY_FAILOVER_RETRIES);
        match TypeDBDriver::new(
            Addresses::try_from_addresses_str(ADDRESSES.iter()).unwrap(),
            Credentials::new(USERNAME, PASSWORD),
            driver_options,
        )
        .await
        {
            Ok(driver) => return driver,
            Err(e) => {
                if attempt < PRIMARY_POLL_RETRIES - 1 {
                    println!(
                        "  Driver creation failed (attempt {}/{}): {}. Retrying in {}s...",
                        attempt + 1,
                        PRIMARY_POLL_RETRIES,
                        e,
                        PRIMARY_POLL_INTERVAL_SECS
                    );
                    sleep(Duration::from_secs(PRIMARY_POLL_INTERVAL_SECS)).await;
                } else {
                    panic!("Failed to create driver after {} attempts: {}", PRIMARY_POLL_RETRIES, e);
                }
            }
        }
    }
    unreachable!()
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
    for attempt in 0..PRIMARY_POLL_RETRIES {
        match try_setup_database(driver).await {
            Ok(()) => return,
            Err(e) => {
                if attempt < PRIMARY_POLL_RETRIES - 1 {
                    println!(
                        "  Database setup failed (attempt {}/{}): {}. Retrying in {}s...",
                        attempt + 1,
                        PRIMARY_POLL_RETRIES,
                        e,
                        PRIMARY_POLL_INTERVAL_SECS
                    );
                    sleep(Duration::from_secs(PRIMARY_POLL_INTERVAL_SECS)).await;
                } else {
                    panic!("Database setup failed after {} attempts: {}", PRIMARY_POLL_RETRIES, e);
                }
            }
        }
    }
}

async fn try_setup_database(driver: &TypeDBDriver) -> std::result::Result<(), typedb_driver::Error> {
    if driver.databases().contains(DATABASE_NAME).await? {
        driver.databases().get(DATABASE_NAME).await?.delete().await?;
    }
    driver.databases().create(DATABASE_NAME).await?;

    let transaction =
        driver.transaction_with_options(DATABASE_NAME, TransactionType::Schema, TransactionOptions::default()).await?;
    transaction.query("define entity person;").await?;
    transaction.commit().await?;
    Ok(())
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
    let tls_config = match DriverTlsConfig::enabled_with_root_ca(&tls_root_ca()) {
        Ok(config) => config,
        Err(_) => return,
    };
    let driver_options = DriverOptions::new(tls_config).primary_failover_retries(PRIMARY_FAILOVER_RETRIES);
    if let Ok(driver) = TypeDBDriver::new(
        Addresses::try_from_addresses_str(ADDRESSES.iter()).unwrap(),
        Credentials::new(USERNAME, PASSWORD),
        driver_options,
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
    // Ensure all nodes are running (recovers from a previous failed run)
    ensure_all_nodes_up();

    async_std::task::block_on(async {
        println!("=== Cluster Failover Test ===");

        println!("Connecting driver...");
        let driver = create_driver().await;

        println!("Setting up database and schema...");
        setup_database(&driver).await;
        verify_read_query(&driver).await;
        println!("Initial setup verified.");

        for iteration in 1..=FAILOVER_ITERATIONS {
            println!("\n--- Failover iteration {}/{} ---", iteration, FAILOVER_ITERATIONS);

            let primary = get_primary_server(&driver).await;
            let primary_address = primary.address().unwrap().to_string();
            let node_id = node_id_from_address(&primary_address);
            println!("  Primary server: {} (node {})", primary_address, node_id);

            println!("  Read query before kill...");
            verify_read_query(&driver).await;

            println!("  Killing node {}...", node_id);
            cluster_server("kill", &node_id);

            println!("  Read query immediately after kill (driver auto-failover)...");
            verify_read_query(&driver).await;
            println!("  Auto-failover read succeeded.");

            println!("  Confirming new primary...");
            let new_primary = get_primary_server(&driver).await;
            println!(
                "  New primary: {} (node {})",
                new_primary.address().unwrap(),
                node_id_from_address(&new_primary.address().unwrap().to_string())
            );

            println!("  Read query on confirmed primary...");
            verify_read_query(&driver).await;
            println!("  Confirmed primary read succeeded.");

            println!("  Restarting node {}...", node_id);
            cluster_server("start", &node_id);
            cluster_server("await", &node_id);
            println!("  Node {} restarted.", node_id);
        }

        println!("\n=== All {} failover iterations passed! ===", FAILOVER_ITERATIONS);
    });

    async_std::task::block_on(async {
        cleanup_database().await;
    });
}
