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
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc,
    }
    ,
    time::Instant,
};

use serial_test::serial;
use typedb_driver::{
    Credentials, DriverOptions, TransactionType, TypeDBDriver,
};

// EXAMPLE END MARKER

async fn cleanup() {
    let driver = TypeDBDriver::new(
        TypeDBDriver::DEFAULT_ADDRESS,
        Credentials::new("admin", "password"),
        DriverOptions::new(false, None).unwrap(),
    )
    .await
    .unwrap();
    if driver.databases().contains("typedb").await.unwrap() {
        driver.databases().get("typedb").await.unwrap().delete().await.unwrap();
    }
}

#[test]
#[serial]
fn transaction_callback() {
    async_std::task::block_on(async {
        cleanup().await;
        let driver = TypeDBDriver::new(
            TypeDBDriver::DEFAULT_ADDRESS,
            Credentials::new("admin", "password"),
            DriverOptions::new(false, None).unwrap(),
        )
        .await
        .unwrap();

        driver.databases().create("typedb").await.unwrap();
        let database = driver.databases().get("typedb").await.unwrap();
        assert_eq!(database.name(), "typedb");

        let close_called = Arc::new(AtomicBool::new(false));
        let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
        transaction.on_close(Box::new({
            let clone = close_called.clone();
            move |error| {
                clone.store(true, Ordering::SeqCst);
            }
        }));

        drop(transaction); // TODO: drop isn't blocking... so we need to spin? or is there an alternative?

        while !close_called.load(Ordering::Acquire) {
            // Yield the current time slice to the OS scheduler.
            // This prevents the loop from consuming 100% of a CPU core.
            std::thread::yield_now();
        }
        assert!(close_called.load(Ordering::SeqCst))
    })
}
