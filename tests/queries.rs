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

use std::sync::mpsc;
use std::thread::sleep;
use std::time::{Duration, Instant};
use futures::TryFutureExt;
use futures::StreamExt;
use typedb_client::{session, Session, transaction, TypeDBClient};
use typedb_client::answer::Numeric;
use typedb_client::concept::{Attribute, Concept, Entity, LongAttribute, StringAttribute, Thing, ThingType, Type};
use typedb_client::session::Type::{Data, Schema};
use typedb_client::transaction::Transaction;
use typedb_client::transaction::Type::{Read, Write};

const GRAKN: &str = "grakn";

async fn new_typedb_client() -> TypeDBClient {
    TypeDBClient::with_default_address().await.unwrap_or_else(|err| panic!("An error occurred connecting to TypeDB Server: {}", err))
}

async fn create_db_grakn(client: &mut TypeDBClient) {
    match client.databases.contains(GRAKN).await {
        Ok(true) => {
            let mut grakn = client.databases.get(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred getting database '{}': {}", GRAKN, err));
            grakn.delete().await.unwrap_or_else(|err| panic!("An error occurred deleting database '{}': {}", GRAKN, err))
        }
        Err(err) => { panic!("An error occurred checking if the database '{}' exists: {}", GRAKN, err) }
        _ => {}
    }
    client.databases.create(GRAKN).await.unwrap_or_else(|err| panic!("An error occurred creating database '{}': {}", GRAKN, err));
}

async fn new_session(client: &mut TypeDBClient, session_type: session::Type) -> Session {
    client.session(GRAKN, session_type).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err))
}

async fn new_tx(session: &Session, tx_type: transaction::Type) -> Transaction {
    new_tx_with_options(session, tx_type, typedb_client::Options::default()).await
}

async fn new_tx_with_options(session: &Session, tx_type: transaction::Type, options: typedb_client::Options) -> Transaction {
    session.transaction_with_options(tx_type, options).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err))
}

async fn commit_tx(tx: &mut Transaction) {
    tx.commit().await.unwrap_or_else(|err| panic!("An error occurred committing a transaction: {}", err))
}

async fn run_define_query(tx: &mut Transaction, query: &str) {
    tx.query.define(query).await.unwrap_or_else(|err| panic!("An error occurred running a Define query: {}", err));
}

#[allow(unused_must_use)]
fn run_insert_query(tx: &mut Transaction, query: &str) {
    tx.query.insert(query);
}

async fn run_match_aggregate_query(tx: &mut Transaction, query: &str) -> Numeric {
    tx.query.match_aggregate(query).await.unwrap_or_else(|err| panic!("An error occurred running a Match Aggregate query: {}", err))
}

#[tokio::test(flavor = "multi_thread")]
#[ignore]
async fn basic() {
    let mut client = new_typedb_client().await;
    create_db_grakn(&mut client).await;
    println!("{}", client.databases.all().await
        .unwrap_or_else(|err| panic!("An error occurred listing databases: {}", err))
        .iter().fold(String::new(), |acc, db| acc + db.name.as_str() + ",")
    );
    let mut session = new_session(&mut client, Data).await;
    let mut tx = new_tx(&session, Write).await;
    let mut answer_stream = tx.query.match_("match $x sub thing; { $x type thing; } or { $x type entity; };");
    while let Some(result) = answer_stream.next().await {
        match result {
            Ok(concept_map) => { println!("{:#?}", concept_map) }
            Err(err) => panic!("An error occurred fetching answers of a Match query: {}", err)
        }
    }
    commit_tx(&mut tx).await;
}

#[tokio::test(flavor = "multi_thread")]
#[ignore]
async fn concurrent_db_ops() {
    let mut client = new_typedb_client().await;
    let (sender, receiver) = mpsc::channel();
    // This example shows that our counted refs to our gRPC client must be atomic (Arc)
    // Replacing Arc with Rc results in the error: 'Rc<...> cannot be sent between threads safely'
    let sender1 = sender.clone();
    let mut databases1 = client.databases.clone();
    let handle1 = tokio::spawn(async move {
        for _ in 0..5 {
            match databases1.all().await {
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

#[tokio::test(flavor = "multi_thread")]
#[ignore]
async fn concurrent_queries() {
    let mut client = new_typedb_client().await;
    let (sender, receiver) = mpsc::channel();
    let sender2 = sender.clone();
    let session = client.session(GRAKN, Data).await.unwrap_or_else(|err| panic!("An error occurred opening a session: {}", err));
    let mut tx: Transaction = session.transaction(Write).await.unwrap_or_else(|err| panic!("An error occurred opening a transaction: {}", err));
    let mut tx2 = tx.clone();
    let handle = tokio::spawn(async move {
        for _ in 0..5 {
            let mut answer_stream = tx.query.match_("match $x sub thing; { $x type thing; } or { $x type entity; };");
            while let Some(result) = answer_stream.next().await {
                match result {
                    Ok(res) => { sender.send(Ok(format!("got answer {:?} from thread 1", res))).unwrap(); }
                    Err(err) => {
                        sender.send(Err(err)).unwrap();
                        return;
                    }
                }
            }
        }
    });
    let handle2 = tokio::spawn(async move {
        for _ in 0..5 {
            let mut answer_stream = tx2.query.match_("match $x sub thing; { $x type thing; } or { $x type entity; };");
            while let Some(result) = answer_stream.next().await {
                match result {
                    Ok(res) => { sender2.send(Ok(format!("got answer {:?} from thread 2", res))).unwrap(); }
                    Err(err) => {
                        sender2.send(Err(err)).unwrap();
                        return;
                    }
                }
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

#[tokio::test(flavor = "multi_thread")]
#[ignore]
async fn query_options() {
    let mut client = new_typedb_client().await;
    create_db_grakn(&mut client).await;
    {
        let session = new_session(&mut client, Schema).await;
        let mut tx = new_tx(&session, Write).await;
        run_define_query(
            &mut tx,
            "define person sub entity, owns name, owns age;\n\
            name sub attribute, value string;\n\
            age sub attribute, value long;\n\
            rule age-rule: when { $x isa person; } then { $x has age 25; };"
        ).await;
        commit_tx(&mut tx).await;
    }
    {
        let session = new_session(&mut client, Data).await;
        {
            let mut tx = new_tx(&session, Write).await;
            run_insert_query(&mut tx, "insert $x isa person, has name \"Alice\"; $y isa person, has name \"Bob\";");
            commit_tx(&mut tx).await;
        }
        {
            let mut tx = new_tx(&session, Read).await;
            let material_age_count = tx.query.match_aggregate("match $x isa age; count;").await.unwrap();
            println!("Ages (excluding inferred): {}", material_age_count.into_i64());
        }
        {
            let mut tx = new_tx_with_options(&session, Read, typedb_client::Options::new_core().infer(true)).await;
            let all_age_count = tx.query.match_aggregate("match $x isa age; count;").await.unwrap();
            println!("Ages (including inferred): {}", all_age_count.into_i64());
        }
    }
}

#[tokio::test(flavor = "multi_thread")]
#[ignore]
async fn many_concept_types() {
    let mut client = new_typedb_client().await;
    create_db_grakn(&mut client).await;
    {
        let session = new_session(&mut client, Schema).await;
        let mut tx = new_tx(&session, Write).await;
        run_define_query(
            &mut tx,
            "define\n\
            person sub entity, owns name, owns dob, plays friendship:friend;\n\
            name sub attribute, value string;\n\
            dob sub attribute, value datetime;\n\
            friendship sub relation, relates friend;"
        ).await;
        commit_tx(&mut tx).await;
    }
    {
        let session = new_session(&mut client, Data).await;
        {
            let mut tx = new_tx(&session, Write).await;
            run_insert_query(
                &mut tx,
                "insert\n\
                $x isa person, has name \"Alice\", has dob 1994-10-03;\n\
                $y isa person, has name \"Bob\", has dob 1993-04-17;\n\
                (friend: $x, friend: $y) isa friendship;");
            commit_tx(&mut tx).await;
        }
        {
            let mut tx = new_tx(&session, Read).await;
            let mut answer_stream = tx.query.match_("match $p isa person; $f($role: $p) isa friendship; $x has dob $dob;");
            while let Some(result) = answer_stream.next().await {
                match result {
                    Ok(concept_map) => {
                        for (_, concept) in concept_map {
                            describe_concept(&concept).await;
                        }
                    }
                    Err(err) => panic!("An error occurred fetching answers of a Match query: {}", err)
                }
            }
        }
    }
}

#[tokio::test(flavor = "multi_thread")]
async fn streaming_perf() {
    let mut client = new_typedb_client().await;
    for iteration in 0..5 {
        create_db_grakn(&mut client).await;
        {
            let session = new_session(&mut client, Schema).await;
            let mut tx = new_tx(&session, Write).await;
            run_define_query(&mut tx, "define person sub entity, owns name, owns age; name sub attribute, value string; age sub attribute, value long;").await;
            commit_tx(&mut tx).await;
        }
        {
            let start_time = Instant::now();
            let session = new_session(&mut client, Data).await;
            let mut tx = new_tx(&session, Write).await;
            for j in 0..100_000 {
                run_insert_query(&mut tx, format!("insert $x {} isa age;", j).as_str());
            }
            commit_tx(&mut tx).await;
            println!("iteration {}: inserted and committed 100k attrs in {}ms", iteration, (Instant::now() - start_time).as_millis());
        }
        {
            let mut start_time = Instant::now();
            let session = new_session(&mut client, Data).await;
            let mut tx = new_tx(&session, Read).await;
            let mut answer_stream = tx.query.match_("match $x isa attribute;");
            let mut sum: i64 = 0;
            let mut idx = 0;
            while let Some(result) = answer_stream.next().await {
                match result {
                    Ok(concept_map) => {
                        for (_, concept) in concept_map {
                            if let Concept::Thing(Thing::Attribute(Attribute::Long(long_attr))) = concept {
                                sum += long_attr.value
                            }
                        }
                    }
                    Err(err) => panic!("An error occurred fetching answers of a Match query: {}", err)
                }
                idx = idx + 1;
                if idx == 100_000 {
                    println!("iteration {}: retrieved and summed 100k attrs in {}ms", iteration, (Instant::now() - start_time).as_millis());
                    start_time = Instant::now();
                }
            }
            println!("sum is {}", sum);
        }
    }
}

async fn describe_concept(concept: &Concept) {
    match concept {
        Concept::Type(x) => { describe_type(x).await; }
        Concept::Thing(x) => { describe_thing(x).await; }
    }
}

async fn describe_type(type_: &Type) {
    match type_ {
        Type::Thing(x) => { describe_thing_type(x).await; }
        Type::Role(x) => { println!("got the ROLE TYPE '{}'", x.label) }
    }
}

async fn describe_thing_type(thing_type: &ThingType) {
    match thing_type {
        ThingType::Root(_) => { println!("got the ROOT THING TYPE 'thing'"); }
        ThingType::Entity(x) => { println!("got the ENTITY TYPE '{}'", x.label); }
        ThingType::Relation(x) => { println!("got the RELATION TYPE '{}'", x.label); }
        ThingType::Attribute(_) => { todo!() }
    }
}

async fn describe_thing(thing: &Thing) {
    match thing {
        Thing::Entity(x) => { println!("got an ENTITY of type {}", x.type_.label.as_str()); }
        Thing::Relation(x) => { println!("got a RELATION of type {}", x.type_.label.as_str()); }
        Thing::Attribute(x) => { describe_attr(x).await; }
    }
}

async fn describe_attr(attr: &Attribute) {
    match attr {
        Attribute::Long(x) => { println!("got a LONG ATTRIBUTE with value {}", x.value); }
        Attribute::String(x) => { println!("got a STRING ATTRIBUTE with value {}", x.value); }
        Attribute::Boolean(x) => { println!("got a BOOLEAN ATTRIBUTE with value {}", x.value); }
        Attribute::Double(x) => { println!("got a DOUBLE ATTRIBUTE with value {}", x.value); }
        Attribute::DateTime(x) => { println!("got a DATETIME ATTRIBUTE with value {}", x.value); }
    }
}
