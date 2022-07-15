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

mod queries {
    use std::sync::mpsc;
    use typedb_client::CoreClient;
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
        let concept_maps = tx.query.match_query("match $x sub thing; { $x type thing; } or { $x type entity; }").await.unwrap_or_else(|err| panic!("An error occurred running a Match query: {}", err));
        println!("{:#?}", concept_maps);
        let concept_maps = tx.query.match_query("match $x sub thing; { $x type thing; } or { $x type entity; };").await.unwrap_or_else(|err| panic!("An error occurred running a Match query: {}", err));
        println!("{:#?}", concept_maps);
        tx.commit().await.unwrap_or_else(|err| panic!("An error occurred committing a transaction: {}", err));
    }

    #[tokio::test]
    #[ignore]
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
                    Ok(dbs) => { sender1.send(Ok(format!("got databases {:?} from thread 1", dbs))).unwrap(); }
                    Err(err) => { sender1.send(Err(err)).unwrap(); return; }
                }
            }
        });
        let handle2 = tokio::spawn(async move {
            for _ in 0..5 {
                match client.databases.all().await {
                    Ok(dbs) => { sender.send(Ok(format!("got databases {:?} from thread 2", dbs))).unwrap(); }
                    Err(err) => { sender.send(Err(err)).unwrap(); return; }
                }
            }
        });
        handle1.await.unwrap();
        handle2.await.unwrap();
        for received in receiver {
            match received {
                Ok(msg) => { println!("{}", msg); }
                Err(err) => { panic!("{}", err.to_string()); }
            }
        }
    }

    #[tokio::test]
    #[ignore]
    async fn concurrent_queries() {
        let client = CoreClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));
        let (sender, receiver) = mpsc::channel();
        let sender2 = sender.clone();
        let session = client.session(GRAKN, Data).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
        let mut tx: Transaction = session.transaction(Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
        let mut tx2 = tx.clone();
        let handle = tokio::spawn(async move {
            for _ in 0..5 {
                match tx.query.match_query("match $x sub thing; { $x type thing; } or { $x type entity; };").await {
                    Ok(res) => { sender.send(Ok(format!("got answers {:?} from thread 1", res))).unwrap(); }
                    Err(err) => { sender.send(Err(err)).unwrap(); return; }
                }
            }
        });
        let handle2 = tokio::spawn(async move {
            for _ in 0..5 {
                match tx2.query.match_query("match $x sub thing; { $x type thing; } or { $x type entity; };").await {
                    Ok(res) => { sender2.send(Ok(format!("got answers {:?} from thread 2", res))).unwrap(); }
                    Err(err) => { sender2.send(Err(err)).unwrap(); return; }
                }
            }
        });
        handle2.await.unwrap();
        handle.await.unwrap();
        for received in receiver {
            match received {
                Ok(msg) => { println!("{}", msg); }
                Err(err) => { panic!("{}", err.to_string()); }
            }
        }
    }
}
