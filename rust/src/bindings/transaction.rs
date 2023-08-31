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

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, borrow_mut, free, release, take_ownership},
};
use crate::{Error, Options, Session, Transaction, TransactionType};

#[no_mangle]
pub extern "C" fn transaction_new(
    session: *const Session,
    type_: TransactionType,
    options: *const Options,
) -> *mut Transaction<'static> {
    try_release(borrow(session).transaction_with_options(type_, borrow(options).clone()))
}

#[no_mangle]
pub extern "C" fn transaction_drop(txn: *mut Transaction<'static>) {
    free(txn);
}

#[no_mangle]
pub extern "C" fn transaction_force_close(txn: *mut Transaction<'static>) {
    borrow_mut(txn).force_close();
}

#[no_mangle]
pub extern "C" fn transaction_commit(txn: *mut Transaction<'static>) {
    unwrap_void(take_ownership(txn).commit());
}

#[no_mangle]
pub extern "C" fn transaction_rollback(txn: *const Transaction<'static>) {
    unwrap_void(borrow(txn).rollback());
}

#[no_mangle]
pub extern "C" fn transaction_is_open(txn: *const Transaction<'static>) -> bool {
    borrow(txn).is_open()
}

#[no_mangle]
pub extern "C" fn transaction_on_close(
    txn: *const Transaction<'static>,
    callback_id: usize,
    callback: extern "C" fn(usize, *mut Error),
) {
    borrow(txn).on_close(move |error| callback(callback_id, release(error.into())));
}
