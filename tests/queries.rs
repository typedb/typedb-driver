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

mod common;

use std::{sync::Arc, time::Instant};

use chrono::{NaiveDate, NaiveDateTime};
use futures::StreamExt;
use serial_test::serial;
use tokio::sync::mpsc;
use typedb_client::{
    concept::{Attribute, Concept, DateTimeAttribute, StringAttribute, Thing},
    error::ConnectionError,
    Connection, DatabaseManager, Error, Options, Session,
    SessionType::Data,
    TransactionType::{Read, Write},
};

macro_rules! test_for_each_arg {
    {
        $perm_args:tt
        $( $( #[ $extra_anno:meta ] )* $async:ident fn $test:ident $args:tt -> $ret:ty $test_impl:block )+
    } => {
        test_for_each_arg!{ @impl $( $async fn $test $args $ret $test_impl )+ }
        test_for_each_arg!{ @impl_per $perm_args  { $( $( #[ $extra_anno ] )* $async fn $test )+ } }
    };

    { @impl $( $async:ident fn $test:ident $args:tt $ret:ty $test_impl:block )+ } => {
        mod _impl {
            use super::*;
            $( pub $async fn $test $args -> $ret $test_impl )+
        }
    };

    { @impl_per { $($mod:ident => $arg:expr),+ $(,)? } $fns:tt } => {
        $(test_for_each_arg!{ @impl_mod { $mod => $arg } $fns })+
    };

    { @impl_mod { $mod:ident => $arg:expr } { $( $( #[ $extra_anno:meta ] )* async fn $test:ident )+ } } => {
        mod $mod {
            use super::*;
        $(
            #[tokio::test]
            #[serial($mod)]
            $( #[ $extra_anno ] )*
            pub async fn $test() {
                _impl::$test($arg).await.unwrap();
            }
        )+
        }
    };
}

test_for_each_arg! {
    {
        core => common::new_core_connection().unwrap(),
        cluster => common::new_cluster_connection().unwrap(),
    }

    async fn basic(connection: Connection) -> typedb_client::Result {
        common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
        let databases = DatabaseManager::new(connection);
        assert!(databases.contains(common::TEST_DATABASE).await?);

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let answer_stream = transaction.query().match_("match $x sub thing;")?;
        let results: Vec<_> = answer_stream.collect().await;
        transaction.commit().await?;
        assert_eq!(results.len(), 5);
        assert!(results.into_iter().all(|res| res.is_ok()));

        Ok(())
    }

    async fn query_error(connection: Connection) -> typedb_client::Result {
        common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
        let databases = DatabaseManager::new(connection);

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let answer_stream = transaction.query().match_("match $x sub nonexistent-type;")?;
        let results: Vec<_> = answer_stream.collect().await;
        assert_eq!(results.len(), 1);
        assert!(results.into_iter().all(|res| res.unwrap_err().to_string().contains("[TYR03]")));

        Ok(())
    }

    async fn concurrent_transactions(connection: Connection) -> typedb_client::Result {
        common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
        let databases = DatabaseManager::new(connection);

        let session = Arc::new(Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?);

        let (sender, mut receiver) = mpsc::channel(5 * 5 * 8);

        for _ in 0..8 {
            let sender = sender.clone();
            let session = session.clone();
            tokio::spawn(async move {
                for _ in 0..5 {
                    let transaction = session.transaction(Read).await.unwrap();
                    let mut answer_stream = transaction.query().match_("match $x sub thing;").unwrap();
                    while let Some(result) = answer_stream.next().await {
                        sender.send(result).await.unwrap();
                    }
                }
            });
        }
        drop(sender); // receiver expects data while any sender is live

        let mut results = Vec::with_capacity(5 * 5 * 8);
        while let Some(result) = receiver.recv().await {
            results.push(result);
        }
        assert_eq!(results.len(), 5 * 5 * 8);
        assert!(results.into_iter().all(|res| res.is_ok()));

        Ok(())
    }

    async fn query_options(connection: Connection) -> typedb_client::Result {
        let schema = r#"define
            person sub entity,
                owns name,
                owns age;
            name sub attribute, value string;
            age sub attribute, value long;
            rule age-rule: when { $x isa person; } then { $x has age 25; };"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;
        let databases = DatabaseManager::new(connection);

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = "insert $x isa person, has name 'Alice'; $y isa person, has name 'Bob';";
        let _ = transaction.query().insert(data);
        transaction.commit().await?;

        let transaction = session.transaction(Read).await?;
        let age_count = transaction.query().match_aggregate("match $x isa age; count;").await?;
        assert_eq!(age_count.into_i64(), 0);

        let with_inference = Options::new().infer(true);
        let transaction = session.transaction_with_options(Read, with_inference).await?;
        let age_count = transaction.query().match_aggregate("match $x isa age; count;").await?;
        assert_eq!(age_count.into_i64(), 1);

        Ok(())
    }

    async fn many_concept_types(connection: Connection) -> typedb_client::Result {
        let schema = r#"define
            person sub entity,
                owns name,
                owns date-of-birth,
                plays friendship:friend;
            name sub attribute, value string;
            date-of-birth sub attribute, value datetime;
            friendship sub relation,
                relates friend;"#;
        common::create_test_database_with_schema(connection.clone(), schema).await?;
        let databases = DatabaseManager::new(connection);

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        let transaction = session.transaction(Write).await?;
        let data = r#"insert
            $x isa person, has name "Alice", has date-of-birth 1994-10-03;
            $y isa person, has name "Bob", has date-of-birth 1993-04-17;
            (friend: $x, friend: $y) isa friendship;"#;
        let _ = transaction.query().insert(data);
        transaction.commit().await?;

        let transaction = session.transaction(Read).await?;
        let mut answer_stream = transaction.query().match_(
            r#"match
            $p isa person, has name $name, has date-of-birth $date-of-birth;
            $f($role: $p) isa friendship;"#,
        )?;

        while let Some(result) = answer_stream.next().await {
            assert!(result.is_ok());
            let mut result = result?.map;
            let name = unwrap_string(result.remove("name").unwrap());
            let date_of_birth = unwrap_date_time(result.remove("date-of-birth").unwrap()).date();
            match name.as_str() {
                "Alice" => assert_eq!(date_of_birth, NaiveDate::from_ymd_opt(1994, 10, 3).unwrap()),
                "Bob" => assert_eq!(date_of_birth, NaiveDate::from_ymd_opt(1993, 4, 17).unwrap()),
                _ => unreachable!(),
            }
        }

        Ok(())
    }

    async fn force_close_connection(connection: Connection) -> typedb_client::Result {
        common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
        let databases = DatabaseManager::new(connection.clone());

        let database = databases.get(common::TEST_DATABASE).await?;
        assert!(database.schema().await.is_ok());

        let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
        connection.clone().force_close()?;

        let schema = database.schema().await;
        assert!(schema.is_err());
        assert!(matches!(schema, Err(Error::Connection(ConnectionError::ConnectionIsClosed()))));

        let database2 = databases.get(common::TEST_DATABASE).await;
        assert!(database2.is_err());
        assert!(matches!(database2, Err(Error::Connection(ConnectionError::ConnectionIsClosed()))));

        let transaction = session.transaction(Write).await;
        assert!(transaction.is_err());
        assert!(matches!(transaction, Err(Error::Connection(ConnectionError::ConnectionIsClosed()))));

        let session = Session::new(database, Data).await;
        assert!(session.is_err());
        assert!(matches!(session, Err(Error::Connection(ConnectionError::ConnectionIsClosed()))));

        Ok(())
    }

    async fn force_close_session(connection: Connection) -> typedb_client::Result {
        common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
        let databases = DatabaseManager::new(connection.clone());

        let session = Arc::new(Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?);
        let transaction = session.transaction(Write).await?;

        let session2 = session.clone();
        session2.force_close()?;

        let answer_stream = transaction.query().match_("match $x sub thing;");
        assert!(answer_stream.is_err());
        assert!(transaction.query().match_("match $x sub thing;").is_err());

        let transaction = session.transaction(Write).await;
        assert!(transaction.is_err());
        assert!(matches!(transaction, Err(Error::Connection(ConnectionError::SessionIsClosed()))));

        assert!(Session::new(databases.get(common::TEST_DATABASE).await?, Data).await.is_ok());

        Ok(())
    }

    #[ignore]
    async fn streaming_perf(connection: Connection) -> typedb_client::Result {
        for i in 0..5 {
            let schema = r#"define
                person sub entity, owns name, owns age;
                name sub attribute, value string;
                age sub attribute, value long;"#;
            common::create_test_database_with_schema(connection.clone(), schema).await?;
            let databases = DatabaseManager::new(connection.clone());

            let start_time = Instant::now();
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
            let transaction = session.transaction(Write).await?;
            for j in 0..100_000 {
                drop(transaction.query().insert(format!("insert $x {j} isa age;").as_str())?);
            }
            transaction.commit().await?;
            println!("iteration {i}: inserted and committed 100k attrs in {}ms", start_time.elapsed().as_millis());

            let mut start_time = Instant::now();
            let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
            let transaction = session.transaction(Read).await?;
            let mut answer_stream = transaction.query().match_("match $x isa attribute;")?;
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
                    Err(err) => {
                        panic!("An error occurred fetching answers of a Match query: {}", err)
                    }
                }
                idx = idx + 1;
                if idx == 100_000 {
                    println!("iteration {i}: retrieved and summed 100k attrs in {}ms", start_time.elapsed().as_millis());
                    start_time = Instant::now();
                }
            }
            println!("sum is {}", sum);
        }

        Ok(())
    }
}

// Concept helpers
// FIXME: should be removed after concept API is implemented
fn unwrap_date_time(concept: Concept) -> NaiveDateTime {
    match concept {
        Concept::Thing(Thing::Attribute(Attribute::DateTime(DateTimeAttribute { value, .. }))) => value,
        _ => unreachable!(),
    }
}

fn unwrap_string(concept: Concept) -> String {
    match concept {
        Concept::Thing(Thing::Attribute(Attribute::String(StringAttribute { value, .. }))) => value,
        _ => unreachable!(),
    }
}
