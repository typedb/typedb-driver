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

use typedb_driver::TransactionOptions;

use crate::{
    common::memory::{borrow, borrow_mut, free, release},
    server::consistency_level::{native_consistency_level, ConsistencyLevel},
};

/// Produces a new <code>TransactionOptions</code> object.
#[no_mangle]
pub extern "C" fn transaction_options_new() -> *mut TransactionOptions {
    release(TransactionOptions::new())
}

/// Frees the native Rust <code>TransactionOptions</code> object.
#[no_mangle]
pub extern "C" fn transaction_options_drop(options: *mut TransactionOptions) {
    free(options);
}

/// Explicitly sets a transaction timeout.
/// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
#[no_mangle]
pub extern "C" fn transaction_options_set_transaction_timeout_millis(
    options: *mut TransactionOptions,
    timeout_millis: i64,
) {
    borrow_mut(options).transaction_timeout = Some(Duration::from_millis(timeout_millis as u64));
}

/// Returns the value set for the transaction timeout in this <code>TransactionOptions</code> object.
/// If set, specifies a timeout for killing transactions automatically, preventing memory leaks in unclosed transactions.
#[no_mangle]
pub extern "C" fn transaction_options_get_transaction_timeout_millis(options: *const TransactionOptions) -> i64 {
    borrow(options).transaction_timeout.unwrap().as_millis() as i64
}

/// Checks whether the option for transaction timeout was explicitly set for this <code>TransactionOptions</code> object.
#[no_mangle]
pub extern "C" fn transaction_options_has_transaction_timeout_millis(options: *const TransactionOptions) -> bool {
    borrow(options).transaction_timeout.is_some()
}

/// Explicitly sets schema lock acquire timeout.
/// If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
#[no_mangle]
pub extern "C" fn transaction_options_set_schema_lock_acquire_timeout_millis(
    options: *mut TransactionOptions,
    timeout_millis: i64,
) {
    borrow_mut(options).schema_lock_acquire_timeout = Some(Duration::from_millis(timeout_millis as u64));
}

/// Returns the value set for the schema lock acquire timeout in this <code>TransactionOptions</code> object.
/// If set, specifies how long the driver should wait if opening a transaction is blocked by an exclusive schema write lock.
#[no_mangle]
pub extern "C" fn transaction_options_get_schema_lock_acquire_timeout_millis(
    options: *const TransactionOptions,
) -> i64 {
    borrow(options).schema_lock_acquire_timeout.unwrap().as_millis() as i64
}

/// Checks whether the option for schema lock acquire timeout was explicitly set for this <code>TransactionOptions</code> object.
#[no_mangle]
pub extern "C" fn transaction_options_has_schema_lock_acquire_timeout_millis(
    options: *const TransactionOptions,
) -> bool {
    borrow(options).schema_lock_acquire_timeout.is_some()
}

/// Explicitly sets read consistency level.
/// If set, specifies the requested consistency level of the transaction opening operation.
/// Affects only read transactions, as write and schema transactions require primary replicas.
#[no_mangle]
pub extern "C" fn transaction_options_set_read_consistency_level(
    options: *mut TransactionOptions,
    read_consistency_level: *const ConsistencyLevel,
) {
    // TODO: This is a hacky feature - you can unset the read consistency level if you pass a nullptr.
    // We should decide what is the general approach if we want to unset an option: whether we also
    // introduce "unset" (a little irritating to maintain) or we consider that the user just creates
    // another object of options. We hardly want to crash here if it's a nullptr.
    borrow_mut(options).read_consistency_level = native_consistency_level(read_consistency_level);
}

/// Returns the value set for the read consistency level in this <code>TransactionOptions</code> object.
/// If set, specifies the requested consistency level of the transaction opening operation.
/// Affects only read transactions, as write and schema transactions require primary replicas.
#[no_mangle]
pub extern "C" fn transaction_options_get_read_consistency_level(
    options: *const TransactionOptions,
) -> *mut ConsistencyLevel {
    release(ConsistencyLevel::from(borrow(options).read_consistency_level.clone().unwrap()))
}

/// Checks whether the option for read consistency level was explicitly set for this <code>TransactionOptions</code> object.
#[no_mangle]
pub extern "C" fn transaction_options_has_read_consistency_level(options: *const TransactionOptions) -> bool {
    borrow(options).read_consistency_level.is_some()
}
