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

use std::ffi::c_char;

use typedb_driver::{BoxPromise, Promise, Result};

use crate::{
    error::{try_release_optional_string, unwrap_or_default, unwrap_void},
    memory::take_ownership,
};

/// Promise object representing the result of an asynchronous operation.
/// A VoidPromise does not return a value, but must be resolved using \ref void_promise_resolve(VoidPromise*)
/// to ensure the operation has completed, or for a failed operation to set the error.
pub struct VoidPromise(pub BoxPromise<'static, Result<()>>);

/// Waits for the operation represented by the <code>VoidPromise</code> to complete.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn void_promise_resolve(promise: *mut VoidPromise) {
    unwrap_void(take_ownership(promise).0.resolve());
}

/// Frees the native rust <code>VoidPromise</code> object.
#[no_mangle]
pub extern "C" fn void_promise_drop(promise: *mut VoidPromise) {
    drop(take_ownership(promise))
}

/// Promise object representing the result of an asynchronous operation.
/// Use \ref bool_promise_resolve(BoolPromise*) to wait for and retrieve the resulting boolean value.
pub struct BoolPromise(pub BoxPromise<'static, Result<bool>>);

/// Waits for and returns the result of the operation represented by the <code>BoolPromise</code> object.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn bool_promise_resolve(promise: *mut BoolPromise) -> bool {
    unwrap_or_default(take_ownership(promise).0.resolve())
}

/// Frees the native rust <code>BoolPromise</code> object.
#[no_mangle]
pub extern "C" fn bool_promise_drop(promise: *mut BoolPromise) {
    drop(take_ownership(promise))
}

/// Promise object representing the result of an asynchronous operation.
/// Use \ref string_promise_resolve(StringPromise*) to wait for and retrieve the resulting string.
pub struct StringPromise(pub BoxPromise<'static, Result<Option<String>>>);

/// Waits for and returns the result of the operation represented by the <code>BoolPromise</code> object.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn string_promise_resolve(promise: *mut StringPromise) -> *mut c_char {
    try_release_optional_string(take_ownership(promise).0.resolve().transpose())
}

/// Frees the native rust <code>StringPromise</code> object.
#[no_mangle]
pub extern "C" fn string_promise_drop(promise: *mut StringPromise) {
    drop(take_ownership(promise))
}
