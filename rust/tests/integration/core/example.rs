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

// EXAMPLE START MARKER
use futures::{StreamExt, TryStreamExt};
// EXAMPLE END MARKER
use serial_test::serial;
// EXAMPLE START MARKER
use typedb_driver::{
    answer::{ConceptRow, QueryAnswer},
    concept::{Concept, ValueType},
    Error, TransactionType, TypeDBDriver,
};

// EXAMPLE END MARKER

async fn cleanup() {
    let driver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();
    if driver.databases().contains("typedb").await.unwrap() {
        driver.databases().get("typedb").await.unwrap().delete().await.unwrap();
    }
}
//
// #[test]
// #[serial]
// // EXAMPLE START MARKER
// fn example() {
//     async_std::task::block_on(async {
//         // EXAMPLE END MARKER
//         cleanup().await;
//         // EXAMPLE START MARKER
//         // Open a driver connection
//         let driver = TypeDBDriver::new_core(TypeDBDriver::DEFAULT_ADDRESS).await.unwrap();
//
//         // Create a database
//         driver.databases().create("typedb").await.unwrap();
//         let database = driver.databases().get("typedb").await.unwrap();
//         assert_eq!(database.name(), "typedb");
//
//         // Dropped transactions are closed
//         {
//             // Open transactions of 3 types
//             let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
//
//             // Execute any TypeDB query using TypeQL. Wrong queries are rejected with an explicit error
//             let result = transaction.query("define entity i-cannot-be-defined-in-read-transactions;").await;
//             match result {
//                 Ok(_) => println!("This line will not be printed"),
//                 // Handle errors
//                 Err(error) => match error {
//                     Error::Connection(connection) => println!("Could not connect: {connection}"),
//                     Error::Server(server) => {
//                         println!("Received a detailed server error regarding the executed query: {server}")
//                     }
//                     Error::Internal(_) => panic!("Unexpected internal error"),
//                     _ => println!("Received an unexpected external error: {error}"),
//                 },
//             }
//         }
//
//         // Open a schema transaction to make schema changes
//         let transaction = driver.transaction(database.name(), TransactionType::Schema).await.unwrap();
//         let answer = transaction.query("define entity person, owns age; attribute age, value long;").await.unwrap();
//
//         // Work with the driver's enums in a classic way or using helper methods
//         if answer.is_ok() && matches!(answer, QueryAnswer::Ok()) {
//             println!("OK results do not give any extra interesting information, but they mean that the query is successfully executed!");
//         }
//         assert!(answer.is_ok());
//
//         // Commit automatically closes the transaction (don't forget to await the result!)
//         transaction.commit().await.unwrap();
//
//         // Open a read transaction to safely read anything without database modifications
//         let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
//         let answer = transaction.query("match entity $x;").await.unwrap();
//         assert!(answer.is_row_stream());
//
//         // Collect concept rows that represent the answer as a table
//         let rows: Vec<ConceptRow> = answer.into_rows().try_collect().await.unwrap();
//         assert_eq!(rows.len(), 1);
//         let row = rows.get(0).unwrap();
//
//         // Retrieve column names to get concepts by index if the variable names are lost
//         let column_names = row.get_column_names();
//         assert_eq!(column_names.len(), 1);
//
//         let column_name = column_names.get(0).unwrap();
//         assert_eq!(column_name.as_str(), "x");
//
//         // Get concept by the variable name (column name)
//         let concept_by_name = row.get(column_name).unwrap();
//
//         // Get concept by the header's index
//         let concept_by_index = row.get_index(0).unwrap();
//         assert_eq!(concept_by_name, concept_by_index);
//
//         // Check if it's an entity type
//         if concept_by_name.is_entity_type() {
//             print!("Getting concepts by variable names and indexes is equally correct. ");
//             println!(
//                 "Both represent the defined entity type: '{}' (in case of a doubt: '{}')",
//                 concept_by_name.get_label(),
//                 concept_by_index.get_label()
//             );
//         }
//         assert!(concept_by_name.is_entity_type());
//         assert!(concept_by_name.is_type());
//         assert_eq!(concept_by_name.get_label(), "person");
//         assert_ne!(concept_by_name.get_label(), "not person");
//         assert_ne!(concept_by_name.get_label(), "age");
//
//         // Continue querying in the same transaction if needed
//         let answer = transaction.query("match attribute $a;").await.unwrap();
//         assert!(answer.is_row_stream());
//
//         // Concept rows can be used as a stream of results
//         let mut rows_stream = answer.into_rows();
//         while let Some(Ok(row)) = rows_stream.next().await {
//             let mut column_names_iter = row.get_column_names().into_iter();
//             let column_name = column_names_iter.next().unwrap();
//             assert_eq!(column_names_iter.next(), None);
//
//             let concept_by_name = row.get(column_name).unwrap();
//
//             // Check if it's an attribute type to safely retrieve its value type
//             if concept_by_name.is_attribute_type() {
//                 println!(
//                     "Defined attribute type's label: '{}', value type: '{}'",
//                     concept_by_name.get_label(),
//                     concept_by_name.get_value_type().unwrap()
//                 );
//             }
//
//             println!("It is also possible to just print the concept itself: '{}'", concept_by_name);
//             assert!(concept_by_name.is_attribute_type());
//             assert!(concept_by_name.is_type());
//             assert!(concept_by_name.is_long());
//             assert_eq!(concept_by_name.get_value_type(), Some(ValueType::Long));
//             assert_eq!(concept_by_name.get_label(), "age");
//             assert_ne!(concept_by_name.get_label(), "person");
//             assert_ne!(concept_by_name.get_label(), "person:age");
//         }
//
//         // Open a write transaction to insert data
//         let transaction = driver.transaction(database.name(), TransactionType::Write).await.unwrap();
//         let answer = transaction.query("insert $z isa person, has age 10; $x isa person, has age 20;").await.unwrap();
//         assert!(answer.is_row_stream());
//
//         // Insert queries also return concept rows
//         let mut rows = Vec::new();
//         let mut rows_stream = answer.into_rows();
//         while let Some(Ok(row)) = rows_stream.next().await {
//             rows.push(row);
//         }
//         assert_eq!(rows.len(), 1);
//         let row = rows.get(0).unwrap();
//
//         for column_name in row.get_column_names() {
//             let inserted_concept = row.get(column_name).unwrap();
//             println!("Successfully inserted ${}: {}", column_name, inserted_concept);
//             if inserted_concept.is_entity() {
//                 println!("This time, it's an entity, not a type!");
//             }
//         }
//
//         // It is possible to ask for the column names again
//         let column_names = row.get_column_names();
//         assert_eq!(column_names.len(), 2);
//         assert!(column_names.contains(&"x".to_owned()));
//         assert!(column_names.contains(&"z".to_owned()));
//
//         let x = row.get_index(column_names.iter().position(|r| r == "x").unwrap()).unwrap();
//         if let Some(iid) = x.get_iid() {
//             println!("Each entity receives a unique IID. It can be retrieved directly: {}", iid);
//         }
//
//         // Do not forget to commit if the changes should be persisted
//         transaction.commit().await.unwrap();
//
//         // Open a read transaction to verify that the inserted data is saved
//         let transaction = driver.transaction(database.name(), TransactionType::Read).await.unwrap();
//         let var = "x";
//         let answer = transaction.query(format!("match ${} isa person;", var)).await.unwrap();
//         assert!(answer.is_row_stream());
//
//         // Match queries always return concept rows
//         let mut count = 0;
//         let mut stream = answer.into_rows().map(|result| result.unwrap());
//         while let Some(row) = stream.next().await {
//             let x = row.get(var).unwrap();
//             assert!(x.is_entity());
//             assert!(!x.is_entity_type());
//             assert!(!x.is_attribute());
//             assert!(!x.is_type());
//             assert!(x.is_instance());
//             assert_eq!(x.get_label(), "person");
//             match x {
//                 Concept::Entity(x_entity) => {
//                     let x_type = x_entity.type_().unwrap();
//                     assert_eq!(x_type.label(), "person");
//                     assert_ne!(x_type.label(), "not person");
//                     count += 1;
//                     println!("Found a person {} of type {}", x, x_type);
//                 }
//                 _ => unreachable!("An entity is expected"),
//             }
//         }
//         assert_eq!(count, 2);
//         println!("Total persons found: {}", count);
//
//         // TODO: We can add a fetch example here!
//         // EXAMPLE END MARKER
//     });
//
//     async_std::task::block_on(async {
//         cleanup().await;
//         // EXAMPLE START MARKER
//         println!("More examples can be found in the API reference and the documentation.\nWelcome to TypeDB!");
//     })
// }
// // EXAMPLE END MARKER

// TODO: Temporary test for fetch
#[test]
#[serial]
fn fetch_base() {
    async_std::task::block_on(async {
        cleanup().await;

        let driver = TypeDBDriver::new_core("127.0.0.1:1729").await.unwrap();

        if driver.databases().contains("db-name").await.unwrap() {
            let db = driver.databases().get("db-name").await.unwrap();
            db.delete().await.unwrap();
        }

        driver.databases().create("db-name").await.unwrap();

        let define_query = r#"
        define
          attribute name value string;
          attribute age value long;
          relation friendship relates friend @card(0..);
          entity person owns name @card(0..), owns age, plays friendship:friend @card(0..);
        "#;

        let transaction = driver.transaction("db-name", TransactionType::Schema).await.unwrap();

        let result = transaction.query(define_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::Ok()));

        transaction.commit().await.unwrap();

        let insert_query = r#"
        insert
          $x isa person, has age 10, has name "Alice", has name "Alicia", has name "Jones";
          $y isa person, has age 11, has name "Bob";
          $z isa person, has age 12, has name "Charlie";
          $p isa person, has age 13, has name "Dixie";
          $q isa person, has age 14, has name "Ellie";
          (friend: $x, friend: $y) isa friendship;
          (friend: $y, friend: $z) isa friendship;
          (friend: $z, friend: $p) isa friendship;
          (friend: $p, friend: $q) isa friendship;
        "#;

        let transaction = driver.transaction("db-name", TransactionType::Write).await.unwrap();

        let result = transaction.query(insert_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptRowStream(_)));
        transaction.commit().await.unwrap();

        let fetch_query = r#"
        match
            $x isa person, has $a;
            $a isa! $t; $t label age;
            $a == 10;
        fetch {
            "single type": $t,
            "single attr": $a,
            "single-card attributes": $x.age,
        #    "single value expression": $a + 1,
        #    "single answer block": (
        #        match
        #        $x has name $name;
        #        return first $name;
        #    ),
        #    "reduce answer block": (
        #        match
        #        $x has name $name;
        #        return count($name);
        #    ),
        #    "list positional return block": [
        #        match
        #        $x has name $n,
        #            has age $a;
        #        return { $n, $a };
        #    ],
            "list pipeline": [
                match
                $x has name $n,
                    has age $a;
                fetch {
                    "name": $n
                };
            ],
            "list higher-card attributes": [ $x.name ],
        #    "list attributes": $x.name[],
            "all attributes": { $x.* }
        };"#;

        let transaction = driver.transaction("db-name", TransactionType::Read).await.unwrap();

        let result = transaction.query(fetch_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptDocumentStream(_)));
        let mut documents_stream = answer.into_documents();
        println!("Printing documents:");
        while let Some(Ok(document)) = documents_stream.next().await {
            println!("DOC: {:?}", &document);
            println!("JSON: {}", document.into_json());
        }
    });

    async_std::task::block_on(async {
        cleanup().await;
    })
}

#[test]
#[serial]
fn fetch_attribute() {
    async_std::task::block_on(async {
        cleanup().await;

        let driver = TypeDBDriver::new_core("127.0.0.1:1729").await.unwrap();

        if driver.databases().contains("db-name").await.unwrap() {
            let db = driver.databases().get("db-name").await.unwrap();
            db.delete().await.unwrap();
        }

        driver.databases().create("db-name").await.unwrap();

        let define_query = r#"
        define
          attribute non-owned value long;
          attribute age value long;
          attribute name value string;
          attribute is-new value boolean;
          attribute success value double;
          attribute balance value decimal;
          attribute birth-date value date;
          attribute birth-time value datetime;
          attribute current-time value datetime-tz;
          attribute current-time-off value datetime-tz;
          attribute expiration value duration;
        
          entity person 
            owns age @card(0..),
            owns name @card(0..),
            owns is-new @card(0..),
            owns success @card(0..),
            owns balance @card(0..),
            owns birth-date @card(0..),
            owns birth-time @card(0..),
            owns current-time @card(0..),
            owns current-time-off @card(0..),
            owns expiration @card(0..);

          entity empty-person;
          entity non-existing-person;
        "#;

        let transaction = driver.transaction("db-name", TransactionType::Schema).await.unwrap();

        let result = transaction.query(define_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::Ok()));

        transaction.commit().await.unwrap();

        let insert_query = r#"
        insert
          $x isa person,
            has age 25,
            has name "John",
            has is-new true,
            has success 66.6,
            has balance 1234567890.0001234567890,
            has birth-date 2024-09-20,
            has birth-time 1999-02-26T12:15:05,
            has current-time 2024-09-20T16:40:05 Europe/London,
            has current-time-off 2024-09-20T16:40:05.028129323+0545,
            has expiration P1Y10M7DT15H44M5.00394892S;

          $e isa empty-person;
        "#;

        let transaction = driver.transaction("db-name", TransactionType::Write).await.unwrap();

        let result = transaction.query(insert_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptRowStream(_)));
        transaction.commit().await.unwrap();

        let fetch_query = r#"
        match
            $x isa person, has $a;
            $a isa! $t;
        fetch {
            # "single type": $t,
            "single attr": $a,
            # "all attributes": { $x.* },
        };"#;

        let transaction = driver.transaction("db-name", TransactionType::Read).await.unwrap();

        let result = transaction.query(fetch_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptDocumentStream(_)));
        let mut documents_stream = answer.into_documents();
        println!("Printing documents:");
        while let Some(Ok(document)) = documents_stream.next().await {
            // println!("DOC: {:?}", &document);
            println!("{}", document.into_json());
        }

        let fetch_query = r#"
        match
            $x isa! empty-person;
        fetch {
             $x.*
        };"#;

        let transaction = driver.transaction("db-name", TransactionType::Read).await.unwrap();

        let result = transaction.query(fetch_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptDocumentStream(_)));
        let mut documents_stream = answer.into_documents();
        println!("Printing documents AGAIN:");
        while let Some(Ok(document)) = documents_stream.next().await {
            println!("DOC: {:?}", &document);
            println!("JSON: {}", document.into_json());
        }

        let fetch_query = r#"
        match
            $x isa! non-existing-person;
        fetch {
             $x.*
        };"#;

        let transaction = driver.transaction("db-name", TransactionType::Read).await.unwrap();

        let result = transaction.query(fetch_query).await;
        let answer = result.unwrap();
        assert!(matches!(answer, QueryAnswer::ConceptDocumentStream(_)));
        let mut documents_stream = answer.into_documents();
        println!("Printing documents AGAIN:");
        while let Some(Ok(document)) = documents_stream.next().await {
            println!("DOC: {:?}", &document);
            println!("JSON: {}", document.into_json());
        }
    });

    async_std::task::block_on(async {
        cleanup().await;
    })
}
