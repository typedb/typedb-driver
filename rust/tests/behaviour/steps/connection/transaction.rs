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

use std::collections::VecDeque;

use cucumber::gherkin::Step;
use futures::{future::join_all, FutureExt};
use macro_rules_attribute::apply;
use typedb_driver::{resolve, Result as TypeDBResult, Transaction, TransactionType, TypeDBDriver};

use crate::{generic_step, params, params::check_boolean, util::iter_table, Context};

async fn open_transaction_for_database(
    driver: &Option<TypeDBDriver>,
    database_name: impl AsRef<str>,
    transaction_type: TransactionType,
) -> TypeDBResult<Transaction> {
    driver.as_ref().unwrap().transaction(database_name, transaction_type)
}

#[apply(generic_step)]
#[step(expr = "connection open {transaction_type} transaction for database: {word}{may_error}")]
pub async fn connection_open_transaction_for_database(
    context: &mut Context,
    type_: params::TransactionType,
    database_name: String,
    may_error: params::MayError,
) {
    context.cleanup_transactions().await;
    may_error.check(context.push_transaction(
        open_transaction_for_database(&context.driver, &database_name, type_.transaction_type).await,
    ));
}

#[apply(generic_step)]
#[step(expr = "connection open transaction(s) for database: {word}, of type:")]
async fn connection_open_transactions_for_database(context: &mut Context, database_name: String, step: &Step) {
    for type_ in iter_table(step) {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        context
            .push_transaction(open_transaction_for_database(&context.driver, &database_name, transaction_type).await)
            .unwrap();
    }
}

#[apply(generic_step)]
#[step(expr = "connection open transaction(s) in parallel for database: {word}, of type:")]
pub async fn connection_open_transactions_in_parallel(context: &mut Context, database_name: String, step: &Step) {
    let transactions: VecDeque<Transaction> = join_all(iter_table(step).map(|type_| {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        open_transaction_for_database(&context.driver, &database_name, transaction_type)
    }))
    .await
    .into_iter()
    .map(|result| result.unwrap())
    .collect();
    context.set_transactions(transactions).await;
}

#[apply(generic_step)]
#[step(expr = "transaction is open: {boolean}")]
#[step(expr = "transactions( in parallel) are open: {boolean}")]
pub async fn transaction_is_open(context: &mut Context, is_open: params::Boolean) {
    let transaction = context.transaction_opt();
    check_boolean!(is_open, transaction.is_some() && transaction.unwrap().is_open());
}

#[apply(generic_step)]
#[step(expr = "transaction has type: {transaction_type}")]
pub async fn transaction_has_type(context: &mut Context, type_: params::TransactionType) {
    assert_eq!(context.transaction().type_(), type_.transaction_type);
}

#[apply(generic_step)]
#[step(expr = "transactions( in parallel) have type:")]
pub async fn transactions_have_type(context: &mut Context, step: &Step) {
    let mut current_transaction = context.transactions.iter();
    for type_ in iter_table(step) {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        assert_eq!(current_transaction.next().unwrap().type_(), transaction_type);
    }
}

#[apply(generic_step)]
#[step(expr = "transaction commits{may_error}")]
pub async fn transaction_commits(context: &mut Context, may_error: params::MayError) {
    may_error.check(resolve!(context.take_transaction().commit()));
}

#[apply(generic_step)]
#[step(expr = "transaction closes")]
pub async fn transaction_closes(context: &mut Context) {
    context.take_transaction().force_close();
}

#[apply(generic_step)]
#[step(expr = "transaction rollbacks{may_error}")]
pub async fn transaction_rollbacks(context: &mut Context, may_error: params::MayError) {
    may_error.check(resolve!(context.transaction().rollback()));
}
