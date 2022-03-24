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

extern crate tokio;
extern crate typedb_client;

mod queries {
    use std::future::Future;
    use std::sync::mpsc;
    use std::thread;
    use std::thread::JoinHandle;
    use typedb_client::common::Result;
    use typedb_client::{CoreClient, session, transaction};
    use typedb_client::common::error::{Error, ERRORS};
    use typedb_client::database::Database;
    use typedb_client::query::match_query;
    use typedb_client::session::Type::{Data, Schema};
    use typedb_client::transaction::Transaction;
    use typedb_client::transaction::Type::Write;

    const GRAKN: &str = "grakn";

    #[tokio::test]
    async fn basic() {
        let client = CoreClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));

        match client.databases.contains(GRAKN).await {
            Ok(true) => (),
            Ok(false) => { client.databases.create(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred creating database '{}': {}", GRAKN, err)); }
            Err(err) => { panic!("An error occurred checking if the database '{}' exists: {}", GRAKN, err) }
        }

        let session = client.session(GRAKN, Schema).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
        let mut tx: Transaction = session.transaction(Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
        // let concept_maps = tx.query.match_query("match $x sub thing;").await.unwrap_or_else(|err| panic!("An error occurred running a Match query: {}", err));
        let concept_maps = match_query(tx, "match $x sub thing;").await.unwrap_or_else(|err| panic!("An error occurred running a Match query: {}", err));
        println!("{:#?}", concept_maps);
    }

    #[tokio::test]
    async fn concurrent_db_ops() {
        let client = CoreClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));
        let (sender, receiver) = mpsc::channel();
        // This example shows that our counted refs to our gRPC client must be atomic (Arc)
        // Replacing Arc with Rc results in the error: 'Rc<...> cannot be sent between threads safely'
        let sender1 = sender.clone();
        let client1 = client.clone();
        let handle1 = tokio::spawn(async move {
            for _ in 0..5 {
                match client1.databases.all().await {
                    Ok(dbs) => { sender1.send(Ok(format!("got databases {:?} from thread 1", dbs))); }
                    Err(err) => { sender1.send(Err(err)); return; }
                }
            }
        });
        let handle2 = tokio::spawn(async move {
            for _ in 0..5 {
                match client.databases.all().await {
                    Ok(dbs) => { sender.send(Ok(format!("got databases {:?} from thread 2", dbs))); }
                    Err(err) => { sender.send(Err(err)); return; }
                }
            }
        });
        handle1.await;
        handle2.await;
        for received in receiver {
            match received {
                Ok(msg) => { println!("{}", msg); }
                Err(err) => { panic!(err.to_string()); }
            }
        }
    }

    #[tokio::test]
    async fn concurrent_queries() {
        let client = CoreClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));
        let (sender, receiver) = mpsc::channel();
        let session = client.session(GRAKN, Data).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
        let mut tx: Transaction = session.transaction(Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
        let handle2 = tokio::spawn(async move {
            for _ in 0..5 {
                match match_query(tx.clone(), "match $x sub entity;").await {
                    Ok(res) => { sender.send(Ok(format!("got answers {:?} from thread 2", res))); }
                    Err(err) => { sender.send(Err(err)); return; }
                }
            }
        });
        handle2.await;
        // ::std::thread::spawn(move || {
        //     async {
        //         tx.query.match_query("match $x sub thing").await;
        //     };
        // });
        // tokio::spawn(async move {
        //     match_query(tx, "match $x sub thing").await;
        // });
        for received in receiver {
            match received {
                Ok(msg) => { println!("{}", msg); }
                Err(err) => { panic!(err.to_string()); }
            }
        }
    }
}
