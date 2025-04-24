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

use std::{collections::VecDeque, time::Duration};
use cucumber::{gherkin::Step, given, then, when};
use futures::{future::join_all, FutureExt};
use macro_rules_attribute::apply;
use typedb_driver::{Result as TypeDBResult, Transaction, TransactionOptions, TransactionType, TypeDBDriver};

use crate::{generic_step, in_background, in_oneshot_background, params, params::check_boolean, util::iter_table, Context};

async fn open_transaction_for_database(
    driver: &TypeDBDriver,
    database_name: impl AsRef<str>,
    transaction_type: TransactionType,
    transaction_options: Option<TransactionOptions>,
) -> TypeDBResult<Transaction> {
    match transaction_options {
        None => driver.transaction(database_name, transaction_type).await,
        Some(options) => {
            driver.transaction_with_options(database_name, transaction_type, options).await
        }
    }
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
    may_error.check(
        context.push_transaction(
            open_transaction_for_database(
                context.driver.as_ref().unwrap(),
                &database_name,
                type_.transaction_type,
                context.transaction_options,
            )
            .await,
        ),
    );
}

#[apply(generic_step)]
#[step(expr = "connection open transaction(s) for database: {word}, of type:")]
async fn connection_open_transactions_for_database(context: &mut Context, database_name: String, step: &Step) {
    for type_ in iter_table(step) {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        context
            .push_transaction(
                open_transaction_for_database(
                    context.driver.as_ref().unwrap(),
                    &database_name,
                    transaction_type,
                    context.transaction_options,
                )
                .await,
            )
            .unwrap();
    }
}

#[apply(generic_step)]
#[step(expr = "connection open transaction(s) in parallel for database: {word}, of type:")]
pub async fn connection_open_transactions_in_parallel(context: &mut Context, database_name: String, step: &Step) {
    let transactions: VecDeque<Transaction> = join_all(iter_table(step).map(|type_| {
        let transaction_type = type_.parse::<params::TransactionType>().unwrap().transaction_type;
        open_transaction_for_database(context.driver.as_ref().unwrap(), &database_name, transaction_type, context.transaction_options)
    }))
    .await
    .into_iter()
    .map(|result| result.unwrap())
    .collect();
    context.set_transactions(transactions).await;
}

#[apply(generic_step)]
#[step(expr = "in background, connection open {transaction_type} transaction for database: {word}{may_error}")]
pub async fn in_background_connection_open_transaction_for_database(
    context: &mut Context,
    type_: params::TransactionType,
    database_name: String,
    may_error: params::MayError,
) {
    in_background!(context, |background| {
        may_error.check(
            context.push_background_transaction(
                open_transaction_for_database(
                    &background,
                    &database_name,
                    type_.transaction_type,
                    context.transaction_options,
                )
                    .await,
            ),
        );
    });
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
    may_error.check(context.take_transaction().commit().await);
}

#[apply(generic_step)]
#[step(expr = "transaction closes")]
pub async fn transaction_closes(context: &mut Context) {
    context.take_transaction().force_close();
}

#[apply(generic_step)]
#[step(expr = "transaction rollbacks{may_error}")]
pub async fn transaction_rollbacks(context: &mut Context, may_error: params::MayError) {
    may_error.check(context.transaction().rollback().await);
}

#[apply(generic_step)]
#[step(expr = "set transaction option transaction_timeout_millis to: {int}")]
pub async fn set_transaction_option_transaction_timeout_millis(context: &mut Context, value: u64) {
    context.init_transaction_options_if_needed();
    context.transaction_options.as_mut().unwrap().transaction_timeout = Some(Duration::from_millis(value));
}

#[apply(generic_step)]
#[step(expr = "set transaction option schema_lock_acquire_timeout_millis to: {int}")]
pub async fn set_transaction_option_schema_lock_acquire_timeout_millis(context: &mut Context, value: u64) {
    context.init_transaction_options_if_needed();
    context.transaction_options.as_mut().unwrap().schema_lock_acquire_timeout = Some(Duration::from_millis(value));
}
