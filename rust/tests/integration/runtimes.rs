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

use futures::StreamExt;
use serial_test::serial;

use typedb_driver::{DatabaseManager, TransactionType::Write};
use typedb_driver::answer::QueryAnswer;
use typedb_driver::TransactionType::Read;

use super::common;

#[test]
#[serial]
fn basic_async_std() {
    async_std::task::block_on(async {
        let connection = common::new_core_connection().await.unwrap();
        let databases = DatabaseManager::new(connection.clone());
        databases.create("testing-db").await.unwrap();

        let db = databases.get("testing-db").await.unwrap();
        db.delete().await.unwrap();
        common::create_test_database_with_schema(connection.clone(), "define entity person, owns age; attribute age, value long;").await.unwrap();
        assert!(databases.contains(common::TEST_DATABASE).await.unwrap());

        let database = databases.get(common::TEST_DATABASE).await.unwrap();

        let transaction = database.transaction(Read).await.unwrap();
        let answers = transaction.query("match entity $x;").await.unwrap();
        assert!(matches!(&answers, QueryAnswer::ConceptRowsStream(_)));
        let rows: Vec<_> = answers.into_rows().collect().await;
        assert_eq!(rows.len(), 1);
        drop(transaction);

        let transaction = database.transaction(Write).await.unwrap();
        let answers = transaction.query("insert $z isa person, has age 10; $x isa person, has age 20;").await.unwrap();
        assert!(matches!(&answers, QueryAnswer::ConceptRowsStream(_)));
        let rows: Vec<_> = answers.into_rows().collect().await;
        assert_eq!(rows.len(), 1);
        transaction.commit().await.unwrap();

        let transaction = database.transaction(Read).await.unwrap();
        let answers = transaction.query("match $x isa person;").await.unwrap();
        assert!(matches!(&answers, QueryAnswer::ConceptRowsStream(_)));
        let rows: Vec<_> = answers.into_rows().collect().await;
        assert_eq!(rows.len(), 2);
        drop(transaction);
    })
    // .unwrap();
}
//
// #[test]
// #[serial]
// fn basic_async_std() {
//     async_std::task::block_on(async {
//         let connection = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(connection);
//         assert!(databases.contains(common::TEST_DATABASE).await?);
//
//         let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
//         let transaction = session.transaction(Write).await?;
//         let answer_stream = transaction.query().get("match $x sub thing; get;")?;
//         let results: Vec<_> = answer_stream.collect().await;
//         transaction.commit().await?;
//         assert_eq!(results.len(), 5);
//         assert!(results.into_iter().all(|res| res.is_ok()));
//         Ok::<(), typedb_driver::Error>(())
//     })
//     .unwrap();
// }
//
// #[test]
// #[serial]
// fn basic_smol() {
//     smol::block_on(async {
//         let connection = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(connection);
//         assert!(databases.contains(common::TEST_DATABASE).await?);
//
//         let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
//         let transaction = session.transaction(Write).await?;
//         let answer_stream = transaction.query().get("match $x sub thing; get;")?;
//         let results: Vec<_> = answer_stream.collect().await;
//         transaction.commit().await?;
//         assert_eq!(results.len(), 5);
//         assert!(results.into_iter().all(|res| res.is_ok()));
//         Ok::<(), typedb_driver::Error>(())
//     })
//     .unwrap();
// }
//
// #[test]
// #[serial]
// fn basic_futures() {
//     futures::executor::block_on(async {
//         let connection = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(connection.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(connection);
//         assert!(databases.contains(common::TEST_DATABASE).await?);
//
//         let session = Session::new(databases.get(common::TEST_DATABASE).await?, Data).await?;
//         let transaction = session.transaction(Write).await?;
//         let answer_stream = transaction.query().get("match $x sub thing; get;")?;
//         let results: Vec<_> = answer_stream.collect().await;
//         transaction.commit().await?;
//         assert_eq!(results.len(), 5);
//         assert!(results.into_iter().all(|res| res.is_ok()));
//         Ok::<(), typedb_driver::Error>(())
//     })
//     .unwrap();
// }
