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
    collections::HashSet,
    env, fs,
    path::Path,
    process::{Child, Command},
    str::FromStr,
    time::Duration,
};

use async_std::task::sleep;
use futures::{StreamExt, TryStreamExt};
use serial_test::serial;
use typedb_driver::{
    answer::ConceptRow, consistency_level::ConsistencyLevel, Address, Addresses, AvailableServerReplica, Credentials,
    DriverOptions, DriverTlsConfig, Error, Replica, ServerReplica, TransactionOptions, TransactionType, TypeDBDriver,
};
// DO NOT commit changes to this test. Use it as playground for dev.
const ADDRESSES: [&'static str; 3] = ["0.0.0.0:11729", "0.0.0.0:21729", "0.0.0.0:31729"];
const ADDRESS: &'static str = "127.0.0.1:11729";
const CLUSTERING_ADDRESSES: [&'static str; 3] = ["0.0.0.0:11730", "0.0.0.0:21730", "0.0.0.0:31730"];
const USERNAME: &'static str = "admin";
const PASSWORD: &'static str = "password";

#[test]
#[serial]
fn playground_test() {
    async_std::task::block_on(async {
        let driver = TypeDBDriver::new(
            // Try automatic replicas retrieval by connecting to only a single server!
            // Use Addresses::try_from_addresses_str() to provide multiple addresses instead.
            Addresses::try_from_address_str(ADDRESS).unwrap(),
            Credentials::new(USERNAME, PASSWORD),
            DriverOptions::new(DriverTlsConfig::enabled_with_native_root_ca()).use_replication(true),
        )
        .await
        .expect("Error while setting up the driver");

        // setup_cluster(&driver).await;

        let replicas = driver.replicas().await.unwrap();
        let addresses = replicas.iter().map(|replica| replica.address().unwrap()).collect::<Vec<_>>();
        println!("Replicas known to the driver: {addresses:?}");

        const DATABASE_NAME: &str = "clustered-test";

        if !driver.databases().contains(DATABASE_NAME).await.expect("Expected database check") {
            driver
                .databases()
                .create_with_consistency(DATABASE_NAME, ConsistencyLevel::Strong)
                .await
                .expect("Expected database creation");
        }

        let database = driver.databases().get(DATABASE_NAME).await.expect("Expected database retrieval");
        println!("Database exists: {}", database.name());

        let transaction_options = TransactionOptions::new()
            .transaction_timeout(Duration::from_secs(100))
            .read_consistency_level(ConsistencyLevel::Eventual);

        // Schema transactions are always strongly consistent
        let transaction = driver
            .transaction_with_options(DATABASE_NAME, TransactionType::Schema, transaction_options.clone())
            .await
            .expect("Expected schema transaction");

        transaction.query("define entity person;").await.expect("Expected schema query");
        transaction.query("insert $p1 isa person; $p2 isa person;").await.expect("Expected data query");
        transaction.commit().await.expect("Expected schema tx commit");

        // Read transaction will be opened using the consistency level from the options
        let transaction = driver
            .transaction_with_options(DATABASE_NAME, TransactionType::Read, transaction_options)
            .await
            .expect("Expected schema transaction");
        let answer = transaction.query("match $p isa person;").await.expect("Expected read query");
        let rows: Vec<ConceptRow> = answer.into_rows().try_collect().await.unwrap();
        println!("Persons found: {}", rows.len());

        println!("Done!");
    });
}

async fn setup_cluster(driver: &TypeDBDriver) {
    for (i, address) in CLUSTERING_ADDRESSES[1..].iter().enumerate() {
        driver.register_replica((i + 2) as u64, address.to_string()).await.unwrap();
    }
}
