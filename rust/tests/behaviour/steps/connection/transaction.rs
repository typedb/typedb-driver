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

use std::time::Duration;

use cucumber::{gherkin::Step, given, then, when};
use typedb_driver::TransactionType;

use crate::{util::iter_table, Context, generic_step, params};
use macro_rules_attribute::apply;
use crate::params::check_boolean;


#[apply(generic_step)]
#[step(expr = "connection open {transaction_type} transaction for database: {word}{may_error}")]
pub async fn connection_open_transaction_for_database(context: &mut Context, type_: params::TransactionType, name: String, may_error: params::MayError) {
    may_error.check(context.push_transaction(context.driver.as_ref().unwrap().transaction(name, type_.transaction_type).await));
}

#[apply(generic_step)]
#[step(expr = "connection open transactions for database: {word}, of type:")]
async fn connection_open_transactions_for_database(context: &mut Context, name: String, step: &Step) {
    for type_ in iter_table(step) {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        context.push_transaction(context.driver.as_ref().unwrap().transaction(name.clone(), transaction_type).await).unwrap();
    }
}

// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) open transaction(s) of type; throws exception: {transaction_type}")]
// async fn for_each_session_open_transactions_of_type_throws_exception(
//     context: &mut Context,
//     type_: params::TransactionType,
// ) {
//     for transaction_tracker in &mut context.transactions {
//         assert!(transaction_tracker.open_transaction(type_.transaction_type, context.transaction_options).await.is_err());
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) open transaction(s) of type; throws exception")]
// async fn for_each_session_open_transactions_of_type_throws_exception_table(context: &mut Context, step: &Step) {
//     for type_ in iter_table(step) {
//         let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
//         for transaction_tracker in &mut context.transactions {
//             assert!(transaction_tracker.open_transaction(transaction_type, context.transaction_options).await.is_err());
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) is/are null: {word}")]
// #[step(expr = "for each session, transactions in parallel are null: {word}")]
// #[step(expr = "for each session in parallel, transactions in parallel are null: {word}")]
// async fn for_each_session_transactions_are_null(_context: &mut Context, is_null: bool) {
//     assert!(!is_null); // Rust transactions are not nullable
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) is/are open: {word}")]
// #[step(expr = "for each session, transactions in parallel are open: {word}")]
// #[step(expr = "for each session in parallel, transactions in parallel are open: {word}")]
// async fn for_each_session_transactions_are_open(context: &mut Context, is_open: bool) {
//     for transaction_tracker in &context.transactions {
//         for transaction in transaction_tracker.transactions() {
//             assert_eq!(transaction.is_open(), is_open);
//         }
//     }
// }

#[apply(generic_step)]
#[step(expr = "transaction commits{may_error}")]
pub async fn transaction_commits(context: &mut Context, may_error: params::MayError) {
    may_error.check(context.take_transaction().commit().await);
}

#[apply(generic_step)]
#[step(expr = "transaction is open: {boolean}")]
pub async fn transaction_is_open(context: &mut Context, is_open: params::Boolean) {
    check_boolean!(is_open, context.transaction().is_open());
}

// #[apply(generic_step)]
// #[step(expr = "transaction commits; throws exception containing {string}")]
// async fn transaction_commits_throws_exception(context: &mut Context, exception: String) {
//     let res = context.take_transaction().commit().await;
//     assert!(res.is_err());
//     assert!(res.unwrap_err().to_string().contains(&exception));
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) commit(s)")]
// async fn for_each_session_transactions_commit(context: &mut Context) {
//     for transaction_tracker in &mut context.transactions {
//         for transaction in transaction_tracker.transactions_mut().drain(..) {
//             transaction.commit().await.unwrap();
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) commit(s); throws exception")]
// async fn for_each_session_transactions_commits_throws_exception(context: &mut Context) {
//     for transaction_tracker in &mut context.transactions {
//         for transaction in transaction_tracker.transactions_mut().drain(..) {
//             assert!(transaction.commit().await.is_err());
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction close(s)")]
// async fn for_each_session_transaction_closes(context: &mut Context) {
//     for transaction_tracker in &mut context.transactions {
//         transaction_tracker.transactions_mut().clear();
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) has/have type: {transaction_type}")]
// async fn for_each_session_transactions_have_type(context: &mut Context, type_: params::TransactionType) {
//     let transaction_type = type_.transaction_type;
//     for transaction_tracker in &context.transactions {
//         assert_eq!(transaction_tracker.transactions().len(), 1);
//         assert_eq!(transaction_type, transaction_tracker.transaction().type_());
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "(for each )session(,) transaction(s) has/have type:")]
// #[step(expr = "for each session, transactions in parallel have type:")]
// async fn for_each_session_transactions_have_types(context: &mut Context, step: &Step) {
//     let types: Vec<TransactionType> =
//         iter_table(step).map(|s| s.parse::<params::TransactionType>().unwrap().transaction_type).collect();
//     for transaction_tracker in &context.transactions {
//         assert_eq!(types.len(), transaction_tracker.transactions().len());
//         for (type_, transaction) in types.iter().zip(transaction_tracker.transactions()) {
//             assert_eq!(*type_, transaction.type_());
//         }
//     }
// }
//
// // ===========================================//
// // sequential sessions, parallel transactions //
// // ===========================================//
//
// #[apply(generic_step)]
// #[step(expr = "for each session, open transaction(s) in parallel of type:")]
// async fn for_each_session_open_transactions_in_parallel_of_type(context: &mut Context, step: &Step) {
//     for type_ in iter_table(step) {
//         let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
//         for transaction_tracker in &mut context.transactions {
//             // FIXME parallel
//             transaction_tracker.open_transaction(transaction_type, context.transaction_options).await.unwrap();
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "set transaction option {word} to: {word}")]
// async fn set_transaction_option_to(context: &mut Context, option: String, value: String) {
//     if option == "transaction-timeout-millis" {
//         context.transaction_options.transaction_timeout = Some(Duration::from_millis(value.parse().unwrap()))
//     } else {
//         todo!("Transaction Option not recognised: {}", option);
//     }
// }
