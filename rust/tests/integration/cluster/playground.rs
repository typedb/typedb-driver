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
    DriverOptions, Error, Replica, ServerReplica, TransactionOptions, TransactionType, TypeDBDriver,
};
// DO NOT commit changes to this test. Use it as playground for dev.

const ADDRESSES: [&'static str; 3] = ["0.0.0.0:11729", "0.0.0.0:21729", "0.0.0.0:31729"];
const CLUSTERING_ADDRESSES: [&'static str; 3] = ["0.0.0.0:11730", "0.0.0.0:21730", "0.0.0.0:31730"];
const USERNAME: &'static str = "admin";
const PASSWORD: &'static str = "password";

#[test]
#[serial]
fn playground_test() {
    async_std::task::block_on(async {
        println!("Building the main driver");
        let driver: TypeDBDriver = TypeDBDriver::new(
            Addresses::try_from_address_str(ADDRESSES[0]).unwrap(),
            Credentials::new(USERNAME, PASSWORD),
            DriverOptions::new().is_tls_enabled(false),
        )
        .await
        .unwrap();

        // for (i, address) in CLUSTERING_ADDRESSES[1..].iter().enumerate() {
        //     driver.register_replica((i + 2) as u64, address.to_string()).await.unwrap();
        // }

        println!("Test\n");
        //
        let replicas = driver.replicas().await.unwrap();
        println!("Replicas: {replicas:?}\n");
        //
        // println!("Primary replica: {:?}\n", driver.primary_replica().await);
        //
        // let replicas: HashSet<ServerReplica> = driver.replicas_with_consistency(ConsistencyLevel::Eventual).await.unwrap();
        // println!("Replicas EVENTUAL: {replicas:?}\n");

        // let third_replica = replicas.iter().find(|replica| replica.address().unwrap().to_string() == "0.0.0.0:31729").unwrap().clone();
        // println!("thid replica: {third_replica:?}");

        let consistency_level3 =
            ConsistencyLevel::ReplicaDependent { address: Address::from_str("0.0.0.0:31729").unwrap() };
        let transaction3 = driver
            .transaction_with_options(
                "a",
                TransactionType::Schema,
                TransactionOptions::new().consistency_level(consistency_level3),
            )
            .await
            .unwrap();
        let transaction1 = driver.transaction("a", TransactionType::Schema).await.unwrap();

        // for i in 0..10000 {
        //     let consistency_level = ConsistencyLevel::ReplicaDependent {address: Address::from_str("0.0.0.0:31729").unwrap() };
        //     // let replicas = driver.replicas_with_consistency(consistency_level.clone()).await.unwrap();
        //     // println!("Replicas TARGET ON {replica:?}: {replicas:?}\n");
        //     // let dbs = driver.databases().all_with_consistency(consistency_level.clone()).await.unwrap();
        //     // println!("Databases TARGET ON {replica:?}: {dbs:?}");
        //     sleep(Duration::from_secs(2)).await;
        //     let name = format!("kek-{}", i);
        //     println!("Name: {name}");
        //     driver.databases().create_with_consistency(name, consistency_level).await.unwrap();
        // }

        // let dbs = driver.databases().all().await.unwrap();
        // println!("Databases: {dbs:?}");

        println!("Done!");
    });
}
