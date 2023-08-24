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

use std::{cell::RefCell, ffi::c_char, ptr::null_mut};

use log::trace;

use super::memory::{free, release_optional, release_string};
use crate::{Error, Result};

thread_local! {
    static LAST_ERROR: RefCell<Option<Error>> = RefCell::new(None);
}

fn ok_record<T>(result: Result<T>) -> Option<T> {
    match result {
        Ok(value) => Some(value),
        Err(err) => {
            record_error(err);
            None
        }
    }
}

fn ok_record_flatten<T>(result: Option<Result<T>>) -> Option<T> {
    result.and_then(ok_record)
}

pub(super) fn try_release<T>(result: Result<T>) -> *mut T {
    release_optional(ok_record(result))
}

pub(super) fn try_release_map_optional<T, U>(result: Option<Result<T>>, f: impl FnOnce(T) -> U) -> *mut U {
    release_optional(ok_record_flatten(result).map(f))
}

pub(super) fn try_release_optional<T>(result: Option<Result<T>>) -> *mut T {
    release_optional(ok_record_flatten(result))
}

pub(super) fn try_release_string(result: Result<String>) -> *mut c_char {
    ok_record(result).map(release_string).unwrap_or_else(null_mut)
}

pub(super) fn try_release_optional_string(result: Option<Result<String>>) -> *mut c_char {
    ok_record_flatten(result).map(release_string).unwrap_or_else(null_mut)
}

pub(super) fn unwrap_or_default<T: Copy + Default>(result: Result<T>) -> T {
    ok_record(result).unwrap_or_default()
}

pub(super) fn unwrap_void(result: Result) {
    ok_record(result);
}

fn record_error(err: Error) {
    trace!("Encountered error {err} in typedb-client-rust");
    LAST_ERROR.with(|prev| *prev.borrow_mut() = Some(err));
}

#[no_mangle]
pub extern "C" fn check_error() -> bool {
    LAST_ERROR.with(|prev| prev.borrow().is_some())
}

#[no_mangle]
pub extern "C" fn get_last_error() -> *mut Error {
    LAST_ERROR.with(|prev| release_optional(prev.borrow_mut().take()))
}

#[no_mangle]
pub extern "C" fn error_drop(error: *mut Error) {
    free(error);
}

#[no_mangle]
pub extern "C" fn error_code(error: *const Error) -> *mut c_char {
    unsafe { release_string((*error).code()) }
}

#[no_mangle]
pub extern "C" fn error_message(error: *const Error) -> *mut c_char {
    unsafe { release_string((*error).message()) }
}
