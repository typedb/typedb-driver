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

use std::path::PathBuf;

use serial_test::serial;
use typedb_driver::{driver::TypeDBDriver, Credential, DatabaseManager};

use super::common;

#[tokio::test]
#[serial]
async fn address_translation() {
    let typedb_driver = TypeDBDriver::new_cloud_with_translation(
        [
            ("localhost:11729", "localhost:11729"),
            ("localhost:21729", "localhost:21729"),
            ("localhost:31729", "localhost:31729"),
        ]
        .into(),
        Credential::with_tls(
            "admin",
            "password",
            Some(&PathBuf::from(
                std::env::var("ROOT_CA").expect("ROOT_CA environment variable needs to be set for cloud tests to run"),
            )),
        )
        .unwrap(),
    )
    .unwrap();

    todo!()
    // common::create_test_database_with_schema(typedb_driver.clone(), "define person sub entity;").await.unwrap();
    // let databases = DatabaseManager::new(typedb_driver);
    // assert!(databases.contains(common::TEST_DATABASE).await.unwrap());
    //
    // let transaction = session.transaction(Write).await.unwrap();
    // let answer_stream = transaction.query().get("match $x sub thing; get;").unwrap();
    // let results: Vec<_> = answer_stream.collect().await;
    // transaction.commit().await.unwrap();
    // assert_eq!(results.len(), 5);
    // assert!(results.into_iter().all(|res| res.is_ok()));
}
