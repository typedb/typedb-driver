/*
 * Copyright (C) 2022 Vaticle
 *
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

use futures::{StreamExt, TryFutureExt};
use serial_test::serial;
use typedb_client::{
    cluster,
    common::{Credential, SessionType::Data, TransactionType::Write},
};

const TEST_DATABASE: &str = "test";

#[tokio::test(flavor = "multi_thread")]
#[serial]
async fn basic() {
    let mut client = cluster::Client::new(
        &["localhost:11729", "localhost:21729", "localhost:31729"],
        Credential::with_tls(
            "admin",
            "password",
            Some(&PathBuf::from(std::env::var("ROOT_CA").unwrap())),
        ),
    )
    .await
    .unwrap();

    if client.databases().contains(TEST_DATABASE).await.unwrap() {
        client.databases().get(TEST_DATABASE).and_then(|db| db.delete()).await.unwrap();
    }
    client.databases().create(TEST_DATABASE).await.unwrap();

    assert!(client.databases().contains(TEST_DATABASE).await.unwrap());

    let mut session = client.session(TEST_DATABASE, Data).await.unwrap();
    let mut transaction = session.transaction(Write).await.unwrap();
    let mut answer_stream = transaction.query.match_("match $x sub thing;");
    while let Some(result) = answer_stream.next().await {
        assert!(result.is_ok())
    }
    transaction.commit().await.unwrap();
}
