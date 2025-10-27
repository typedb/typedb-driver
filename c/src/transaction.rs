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

use std::{ffi::c_char, ptr::null_mut};

use typedb_driver::{Error, QueryOptions, Transaction, TransactionOptions, TransactionType, TypeDBDriver};

use super::memory::{borrow, borrow_mut, free, release, take_ownership};
use crate::{
    analyze::AnalyzedQueryPromise, answer::QueryAnswerPromise, error::try_release, memory::string_view,
    promise::VoidPromise,
};

/// Opens a transaction to perform read or write queries on the database connected to the session.
///
/// @param databases The <code>DatabaseManager</code> object on this connection.
/// @param database_name The name of the database with which the transaction connects.
/// @param type_ The type of transaction to be created (Write / Read / Schema).
/// @param options <code>TransactionOptions</code> to configure the opened transaction.
#[no_mangle]
pub extern "C" fn transaction_new(
    driver: *mut TypeDBDriver,
    database_name: *const c_char,
    type_: TransactionType,
    options: *const TransactionOptions,
) -> *mut Transaction {
    try_release(borrow(driver).transaction_with_options(string_view(database_name), type_, *borrow(options)))
}

/// Performs a TypeQL query in the transaction.
///
/// @param transaction The <code>Transaction</code> to execute the query within.
/// @param query The query string.
/// @param options <code>QueryOptions</code> to configure the executed query.
#[no_mangle]
pub extern "C" fn transaction_query(
    transaction: *mut Transaction,
    query: *const c_char,
    options: *const QueryOptions,
) -> *mut QueryAnswerPromise {
    release(QueryAnswerPromise::new(Box::new(
        borrow(transaction).query_with_options(string_view(query), *borrow(options)),
    )))
}

/// Analyzes a TypeQL query in the transaction.
///
/// @param transaction The <code>Transaction</code> to analyze the query within.
/// @param query The query string.
#[no_mangle]
pub extern "C" fn transaction_analyze(
    transaction: *mut Transaction,
    query: *const c_char,
) -> *mut AnalyzedQueryPromise {
    release(AnalyzedQueryPromise::new(Box::new(borrow(transaction).analyze(string_view(query)))))
}

/// Closes the transaction and frees the native rust object.
#[no_mangle]
pub extern "C" fn transaction_submit_close(txn: *mut Transaction) {
    free(txn);
}

/// Forcibly closes this transaction. To be used in exceptional cases.
#[no_mangle]
pub extern "C" fn transaction_close(txn: *mut Transaction) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_mut(txn).close())))
}

/// Commits the changes made via this transaction to the TypeDB database.
/// Whether or not the transaction is commited successfully, the transaction is closed after
/// the commit call and the native rust object is freed.
#[no_mangle]
pub extern "C" fn transaction_commit(txn: *mut Transaction) -> *mut VoidPromise {
    release(VoidPromise(Box::new(take_ownership(txn).commit())))
}

/// Rolls back the uncommitted changes made via this transaction.
#[no_mangle]
pub extern "C" fn transaction_rollback(txn: *const Transaction) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow(txn).rollback())))
}

/// Checks whether this transaction is open.
#[no_mangle]
pub extern "C" fn transaction_is_open(txn: *const Transaction) -> bool {
    borrow(txn).is_open()
}

/// Registers a callback function which will be executed when this transaction is closed.
///
/// @param txn The transaction on which to register the callback
/// @param callback_id The argument to be passed to the callback function when it is executed.
/// @param callback The function to be called
#[no_mangle]
pub extern "C" fn transaction_on_close(
    txn: *const Transaction,
    callback_id: usize,
    callback: extern "C" fn(usize, *mut Error),
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(txn)
            .on_close(move |error| callback(callback_id, error.map(|err| release(err.into())).unwrap_or(null_mut()))),
    )))
}
