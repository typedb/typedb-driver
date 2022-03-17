/*
 * Copyright (C) 2021 Vaticle
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

extern crate grpc;
extern crate typedb_client;

use typedb_client::{CoreClient, session, transaction};
// use ::{CoreClient, session, transaction};

#[tokio::test]
async fn test_integration() {
    const GRAKN: &str = "grakn";
    let client = CoreClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));

    match client.databases.contains(GRAKN).await {
        Ok(true) => (),
        Ok(false) => { client.databases.create(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred creating database '{}': {}", GRAKN, err)); }
        Err(err) => { panic!("An error occurred checking if the database '{}' exists: {}", GRAKN, err) }
    }

    let session = client.session(GRAKN, session::Type::Schema).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
    let tx = session.transaction(transaction::Type::Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
}
