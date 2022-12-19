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

use std::{sync::mpsc, time::Instant};

use chrono::{NaiveDate, NaiveDateTime};
use futures::{StreamExt, TryFutureExt};
use serial_test::serial;
use typedb_client::{
    common::{
        SessionType::{Data, Schema},
        TransactionType::{Read, Write},
    },
    concept::{Attribute, Concept, DateTimeAttribute, StringAttribute, Thing},
    core, server,
};

const TEST_DATABASE: &str = "test";

#[tokio::test(flavor = "multi_thread")]
#[serial]
async fn basic() {
    let mut client = core::Client::with_default_address().await.unwrap();
    create_test_database_with_schema(&mut client, "define person sub entity;").await.unwrap();
    assert!(client.databases().contains(TEST_DATABASE).await.unwrap());

    let session = client.session(TEST_DATABASE, Data).await.unwrap();
    let mut transaction = session.transaction(Write).await.unwrap();
    let mut answer_stream = transaction.query.match_("match $x sub thing;");
    while let Some(result) = answer_stream.next().await {
        assert!(result.is_ok())
    }
    transaction.commit().await.unwrap();
}

#[tokio::test(flavor = "multi_thread")]
#[serial]
async fn concurrent_queries() {
    let mut client = core::Client::with_default_address().await.unwrap();
    create_test_database_with_schema(&mut client, "define person sub entity;").await.unwrap();

    let session = client.session(TEST_DATABASE, Data).await.unwrap();
    let transaction = session.transaction(Write).await.unwrap();

    let (sender, receiver) = mpsc::channel();

    for _ in 0..5 {
        let sender = sender.clone();
        let mut transaction = transaction.clone();
        tokio::spawn(async move {
            for _ in 0..5 {
                let mut answer_stream = transaction.query.match_("match $x sub thing;");
                while let Some(result) = answer_stream.next().await {
                    sender.send(result).unwrap();
                }
            }
        });
    }
    drop(sender); // receiver expects data while any sender is live

    for received in receiver {
        assert!(received.is_ok());
    }
}

#[tokio::test(flavor = "multi_thread")]
#[serial]
async fn query_options() {
    let mut client = core::Client::with_default_address().await.unwrap();
    let schema = r#"define
        person sub entity,
            owns name,
            owns age;
        name sub attribute, value string;
        age sub attribute, value long;
        rule age-rule: when { $x isa person; } then { $x has age 25; };"#;
    create_test_database_with_schema(&mut client, schema).await.unwrap();

    let session = client.session(TEST_DATABASE, Data).await.unwrap();
    let mut transaction = session.transaction(Write).await.unwrap();
    let data = "insert $x isa person, has name 'Alice'; $y isa person, has name 'Bob';";
    let _ = transaction.query.insert(data);
    transaction.commit().await.unwrap();

    let mut transaction = session.transaction(Read).await.unwrap();
    let age_count = transaction.query.match_aggregate("match $x isa age; count;").await.unwrap();
    assert_eq!(age_count.into_i64(), 0);

    let with_inference = core::Options::new_core().infer(true);
    let mut transaction = session.transaction_with_options(Read, with_inference).await.unwrap();
    let age_count = transaction.query.match_aggregate("match $x isa age; count;").await.unwrap();
    assert_eq!(age_count.into_i64(), 1);
}

#[tokio::test(flavor = "multi_thread")]
#[serial]
async fn many_concept_types() {
    let mut client = core::Client::with_default_address().await.unwrap();
    let schema = r#"define
        person sub entity,
            owns name,
            owns date-of-birth,
            plays friendship:friend;
        name sub attribute, value string;
        date-of-birth sub attribute, value datetime;
        friendship sub relation,
            relates friend;"#;
    create_test_database_with_schema(&mut client, schema).await.unwrap();

    let session = client.session(TEST_DATABASE, Data).await.unwrap();
    let mut transaction = session.transaction(Write).await.unwrap();
    let data = r#"insert
        $x isa person, has name "Alice", has date-of-birth 1994-10-03;
        $y isa person, has name "Bob", has date-of-birth 1993-04-17;
        (friend: $x, friend: $y) isa friendship;"#;
    let _ = transaction.query.insert(data);
    transaction.commit().await.unwrap();

    let mut transaction = session.transaction(Read).await.unwrap();
    let mut answer_stream = transaction.query.match_(
        r#"match
        $p isa person, has name $name, has date-of-birth $date-of-birth;
        $f($role: $p) isa friendship;"#,
    );

    while let Some(result) = answer_stream.next().await {
        assert!(result.is_ok());
        let mut result = result.unwrap().map;
        let name = unwrap_string(result.remove("name").unwrap());
        let date_of_birth = unwrap_date_time(result.remove("date-of-birth").unwrap()).date();
        match name.as_str() {
            "Alice" => assert_eq!(date_of_birth, NaiveDate::from_ymd_opt(1994, 10, 3).unwrap()),
            "Bob" => assert_eq!(date_of_birth, NaiveDate::from_ymd_opt(1993, 4, 17).unwrap()),
            _ => unreachable!(),
        }
    }
}

#[tokio::test(flavor = "multi_thread")]
#[serial]
#[ignore]
async fn streaming_perf() {
    let mut client = core::Client::with_default_address().await.unwrap();
    for i in 0..5 {
        let schema = r#"define
            person sub entity, owns name, owns age;
            name sub attribute, value string;
            age sub attribute, value long;"#;
        create_test_database_with_schema(&mut client, schema).await.unwrap();

        let start_time = Instant::now();
        let session = client.session(TEST_DATABASE, Data).await.unwrap();
        let mut transaction = session.transaction(Write).await.unwrap();
        for j in 0..100_000 {
            let _ = transaction.query.insert(format!("insert $x {j} isa age;").as_str());
        }
        transaction.commit().await.unwrap();
        println!(
            "iteration {i}: inserted and committed 100k attrs in {}ms",
            (Instant::now() - start_time).as_millis()
        );

        let mut start_time = Instant::now();
        let session = client.session(TEST_DATABASE, Data).await.unwrap();
        let mut transaction = session.transaction(Read).await.unwrap();
        let mut answer_stream = transaction.query.match_("match $x isa attribute;");
        let mut sum: i64 = 0;
        let mut idx = 0;
        while let Some(result) = answer_stream.next().await {
            match result {
                Ok(concept_map) => {
                    for (_, concept) in concept_map {
                        if let Concept::Thing(Thing::Attribute(Attribute::Long(long_attr))) =
                            concept
                        {
                            sum += long_attr.value
                        }
                    }
                }
                Err(err) => {
                    panic!("An error occurred fetching answers of a Match query: {}", err)
                }
            }
            idx = idx + 1;
            if idx == 100_000 {
                println!(
                    "iteration {i}: retrieved and summed 100k attrs in {}ms",
                    (Instant::now() - start_time).as_millis()
                );
                start_time = Instant::now();
            }
        }
        println!("sum is {}", sum);
    }
}

async fn create_test_database_with_schema(
    client: &mut core::Client,
    schema: &str,
) -> typedb_client::Result {
    if client.databases().contains(TEST_DATABASE).await.unwrap() {
        client.databases().get(TEST_DATABASE).and_then(server::Database::delete).await.unwrap();
    }
    client.databases().create(TEST_DATABASE).await.unwrap();

    let mut session = client.session(TEST_DATABASE, Schema).await.unwrap();
    let mut transaction = session.transaction(Write).await.unwrap();
    transaction.query.define(schema).await.unwrap();
    transaction.commit().await.unwrap();
    session.close().await;

    Ok(())
}

// Concept helpers
// FIXME should be removed after concept API is implemented
fn unwrap_date_time(concept: Concept) -> NaiveDateTime {
    match concept {
        Concept::Thing(Thing::Attribute(Attribute::DateTime(DateTimeAttribute {
            value, ..
        }))) => value,
        _ => unreachable!(),
    }
}

fn unwrap_string(concept: Concept) -> String {
    match concept {
        Concept::Thing(Thing::Attribute(Attribute::String(StringAttribute { value, .. }))) => value,
        _ => unreachable!(),
    }
}
