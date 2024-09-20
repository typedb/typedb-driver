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

use futures::{StreamExt, TryStreamExt};

use itertools::Itertools;
use serial_test::serial;

use typedb_driver::{DatabaseManager, TransactionType, TransactionType::Write, TypeDBDriver};
use typedb_driver::answer::{ConceptRow, QueryAnswer};
use typedb_driver::TransactionType::Read;

use super::common;

#[test]
#[serial]
fn basic_async_std() {
    async_std::task::block_on(async {
// Connection has been replaced with Driver:
let driver = TypeDBDriver::new_core("127.0.0.1:1729").await.unwrap();

driver.databases().create("db-name").await.unwrap();

// Sessions have been removed. Now there are Read, Write, and Schema transactions only
let transaction = driver.transaction("db-name", TransactionType::Schema).await.unwrap();

// All queries just use a simple .query() API
let result = transaction.query("define entity person, owns age; attribute age, value long;").await;
let answer = result.unwrap(); // result could contain an error returned by the server
// Define queries return OK answers
assert!(matches!(answer, QueryAnswer::Ok()));

transaction.commit().await.unwrap();

let transaction = driver.transaction("db-name", TransactionType::Write).await.unwrap();

let result = transaction.query("insert $x isa person, has age 10;").await;
let answer = result.unwrap(); // result could contain an error returned by the server
assert!(matches!(answer, QueryAnswer::ConceptRowsStream(_)));

let mut rows_stream = answer.into_rows();
while let Some(Ok(row)) = rows_stream.next().await {
    println!("{:?}", &row);
    println!("{:?}", row.get("x"));
}

        //
        //
        //
        // let driver = common::new_core_driver().await.unwrap();
        // driver.databases().create("testing-db").await.unwrap();
        //
        // let db = driver.databases().get("testing-db").await.unwrap();
        // db.delete().await.unwrap();
        // common::create_test_database_with_schema(&driver, r#"
        //     define
        //         entity person,
        //             owns age,
        //             owns name @card(0..);
        //         attribute age,
        //             value long;
        //         attribute name, value string;
        //         attribute bday, value datetime;
        // "#).await.unwrap();
        //
        // assert!(driver.databases().contains(common::TEST_DATABASE).await.unwrap());
        //
        // let transaction = driver.transaction(common::TEST_DATABASE, Read).await.unwrap();
        // let rows: Vec<ConceptRow> = transaction.query("match entity $x;").await.unwrap().into_rows().try_collect().await.unwrap();
        // assert_eq!(rows.len(), 1);
        //
        // let transaction_2 = driver.transaction(common::TEST_DATABASE, Read).await.unwrap();
        // let rows_2: Vec<ConceptRow> = transaction_2.query("match entity $x;").await.unwrap().into_rows().try_collect().await.unwrap();
        // assert_eq!(rows_2.len(), 1);
        //
        // let transaction_3 = driver.transaction(common::TEST_DATABASE, Read).await.unwrap();
        // let rows_3: Vec<ConceptRow> = transaction_3.query("match entity $x;").await.unwrap().into_rows().try_collect().await.unwrap();
        // assert_eq!(rows_2.len(), 1);
        //
        // let transaction_4 = driver.transaction(common::TEST_DATABASE, Write).await.unwrap();
        //
        // driver.force_close().unwrap();
        //
        // // TODO: need transaction timeout for write queries, while read queries are running
        //
        // let rows_4: Vec<ConceptRow> = transaction_4.query(r#"
        //     insert
        //         $x 2010-01-01T00:00:00 isa bday;
        //         $y isa person, has age 10, has age 20;
        //     "#).await.unwrap().into_rows().try_collect().await.unwrap();
        // transaction_4.commit().await.unwrap();
        //
        // drop(transaction);
        //
        // let transaction = driver.transaction(common::TEST_DATABASE, Write).await.unwrap();
        // let answers = transaction.query("insert $z isa person, has age 10; $x isa person, has age 20;").await.unwrap();
        // assert!(matches!(&answers, QueryAnswer::ConceptRowsStream(_)));
        // let rows: Vec<_> = answers.into_rows().collect().await;
        // assert_eq!(rows.len(), 1);
        // transaction.commit().await.unwrap();
        // //
        // let transaction = driver.transaction(common::TEST_DATABASE, Read).await.unwrap();
        // let answers = transaction.query("match $x isa person, has age $a;").await.unwrap();
        // assert!(matches!(&answers, QueryAnswer::ConceptRowsStream(_)));
        // let mut iter = answers.into_rows();
        //
        // let mut rows_count = 0;
        // while let Some(row) = iter.next().await {
        //     println!("{}", row.unwrap());
        //     rows_count += 1;
        // }
        // assert_eq!(rows_count, 2);
    })
    // .unwrap();
}

//
// #[test]
// #[serial]
// fn basic_async_std() {
//     async_std::task::block_on(async {
//         let typedb_driver = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(typedb_driver.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(typedb_driver);
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
//         let typedb_driver = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(typedb_driver.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(typedb_driver);
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
//         let typedb_driver = common::new_cloud_connection()?;
//         common::create_test_database_with_schema(typedb_driver.clone(), "define person sub entity;").await?;
//         let databases = DatabaseManager::new(typedb_driver);
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
