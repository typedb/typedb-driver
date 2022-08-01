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

// #![feature(if_let_guard)] // only available on nightly Rust builds

mod queries {
    use futures::StreamExt;
    use typedb_client::{session, Session, transaction, TypeDBClient};
    use typedb_client::concept::{Attribute, Concept, Entity, LongAttribute, StringAttribute, Thing};
    use typedb_client::session::Type::{Data, Schema};
    use typedb_client::transaction::Transaction;
    use typedb_client::transaction::Type::{Read, Write};

    const GRAKN: &str = "grakn";

    async fn new_typedb_client() -> TypeDBClient {
        TypeDBClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err))
    }

    async fn create_db_grakn(client: &TypeDBClient) {
        match client.databases.contains(GRAKN).await {
            Ok(true) => {
                let grakn = client.databases.get(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred getting database '{}': {}", GRAKN, err));
                grakn.delete().await.unwrap_or_else(|err| panic!("An error occurred deleting database '{}': {}", GRAKN, err))
            }
            Err(err) => { panic!("An error occurred checking if the database '{}' exists: {}", GRAKN, err) }
            _ => {}
        }
        client.databases.create(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred creating database '{}': {}", GRAKN, err));
    }

    async fn new_session(client: &TypeDBClient, session_type: session::Type) -> Session {
        client.session(GRAKN, session_type).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err))
    }

    async fn new_tx(session: &Session, tx_type: transaction::Type) -> Transaction {
        session.transaction(tx_type).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err))
    }

    async fn commit_tx(tx: &Transaction) {
        tx.commit().await.unwrap_or_else(|err| panic!("An error occurred committing a transaction: {}", err))
    }

    async fn run_define_query(tx: &Transaction, query: &str) {
        tx.query().define(query).await.unwrap_or_else(|err| panic!("An error occurred running a Define query: {}", err));
    }

    #[allow(unused_must_use)]
    fn run_insert_query(tx: &Transaction, query: &str) {
        tx.query().insert(query);
    }

    // #[tokio::test]
    // async fn basic() {
    //     let client = new_typedb_client().await;
    //     create_db_grakn(&client).await;
    //     let session = new_session(&client, Data).await;
    //     let tx = new_tx(&session, Write).await;
    //     let mut answer_stream = tx.query().match_("match $x sub thing; { $x type thing; } or { $x type entity; };");
    //     while let Some(result) = answer_stream.next().await {
    //         match result {
    //             Ok(concept_map) => { println!("{:#?}", concept_map) }
    //             Err(err) => panic!("An error occurred fetching answers of a Match query: {}", err)
    //         }
    //     }
    //     commit_tx(&tx).await;
    // }

    // #[tokio::test]
    // #[ignore]
    // async fn concurrent_db_ops() {
    //     let client = TypeDBClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));
    //     let (sender, receiver) = mpsc::channel();
    //     // This example shows that our counted refs to our gRPC client must be atomic (Arc)
    //     // Replacing Arc with Rc results in the error: 'Rc<...> cannot be sent between threads safely'
    //     let sender1 = sender.clone();
    //     let client1 = client.clone();
    //     let handle1 = tokio::spawn(async move {
    //         for _ in 0..5 {
    //             match client1.databases.all().await {
    //                 Ok(dbs) => { sender1.send(Ok(format!("got databases {:?} from thread 1", dbs))).unwrap(); }
    //                 Err(err) => { sender1.send(Err(err)).unwrap(); return; }
    //             }
    //         }
    //     });
    //     let handle2 = tokio::spawn(async move {
    //         for _ in 0..5 {
    //             match client.databases.all().await {
    //                 Ok(dbs) => { sender.send(Ok(format!("got databases {:?} from thread 2", dbs))).unwrap(); }
    //                 Err(err) => { sender.send(Err(err)).unwrap(); return; }
    //             }
    //         }
    //     });
    //     handle1.await.unwrap();
    //     handle2.await.unwrap();
    //     for received in receiver {
    //         match received {
    //             Ok(msg) => { println!("{}", msg); }
    //             Err(err) => { panic!("{}", err.to_string()); }
    //         }
    //     }
    // }
    //
    // #[tokio::test]
    // #[ignore]
    // async fn concurrent_queries() {
    //     let client = TypeDBClient::new("0.0.0.0", 1729).await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err));
    //     let (sender, receiver) = mpsc::channel();
    //     let sender2 = sender.clone();
    //     let session = client.session(GRAKN, Data).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
    //     let tx: Transaction = session.transaction(Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
    //     let tx2 = tx.clone();
    //     let handle = tokio::spawn(async move {
    //         for _ in 0..5 {
    //             match tx.query().match_query("match $x sub thing; { $x type thing; } or { $x type entity; };").await {
    //                 Ok(res) => { sender.send(Ok(format!("got answers {:?} from thread 1", res))).unwrap(); }
    //                 Err(err) => { sender.send(Err(err)).unwrap(); return; }
    //             }
    //         }
    //     });
    //     let handle2 = tokio::spawn(async move {
    //         for _ in 0..5 {
    //             match tx2.query().match_query("match $x sub thing; { $x type thing; } or { $x type entity; };").await {
    //                 Ok(res) => { sender2.send(Ok(format!("got answers {:?} from thread 2", res))).unwrap(); }
    //                 Err(err) => { sender2.send(Err(err)).unwrap(); return; }
    //             }
    //         }
    //     });
    //     handle2.await.unwrap();
    //     handle.await.unwrap();
    //     for received in receiver {
    //         match received {
    //             Ok(msg) => { println!("{}", msg); }
    //             Err(err) => { panic!("{}", err.to_string()); }
    //         }
    //     }
    // }

    #[tokio::test]
    async fn concept_api() {
        let client = new_typedb_client().await;
        create_db_grakn(&client).await;
        {
            let session = new_session(&client, Schema).await;
            let tx = new_tx(&session, Write).await;
            run_define_query(&tx, "define person sub entity, owns name, owns age; name sub attribute, value string; age sub attribute, value long;").await;
            commit_tx(&tx).await;
        }
        {
            let session = new_session(&client, Data).await;
            let tx = new_tx(&session, Write).await;
            run_insert_query(&tx, "insert $x isa person, has name \"Alice\", has age 18; $y isa person, has name \"Bob\", has age 21;");
            commit_tx(&tx).await;
        }
        let session = new_session(&client, Data).await;
        let tx = new_tx(&session, Read).await;
        let mut answer_stream = tx.query().match_("match $x isa thing;");
        while let Some(result) = answer_stream.next().await {
            match result {
                Ok(concept_map) => {
                    for concept in concept_map {
                        match &concept {
                            Concept::Thing(Thing::Entity(entity)) => { describe_entity(entity).await; }
                            Concept::Thing(Thing::Attribute(attr)) => {
                                match &attr {
                                    Attribute::Long(long_attr) => { describe_long_attr(long_attr).await; }
                                    Attribute::String(str_attr) => { describe_str_attr(str_attr).await; }
                                }
                            }
                            _ => {}
                        }
                    }
                }
                Err(err) => panic!("An error occurred fetching answers of a Match query: {}", err)
            }
        }
    }

    async fn describe_entity(entity: &Entity) {
        println!("answer is an ENTITY of type {}", entity.type_.label.as_str());
    }

    async fn describe_long_attr(long_attr: &LongAttribute) {
        println!("answer is a LONG ATTRIBUTE with value {}", long_attr.value);
    }

    async fn describe_str_attr(str_attr: &StringAttribute) {
        println!("answer is a STRING ATTRIBUTE with value {}", str_attr.value);
    }
}
