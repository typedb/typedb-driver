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

use std::ffi::c_char;

use typedb_driver::{BoxPromise, Promise, Result};

use crate::{
    error::{try_release_optional_string, unwrap_or_default, unwrap_void},
    memory::take_ownership,
};

pub struct VoidPromise(pub BoxPromise<'static, Result<()>>);

#[no_mangle]
pub extern "C" fn void_promise_resolve(promise: *mut VoidPromise) {
    unwrap_void(take_ownership(promise).0.resolve());
}

pub struct BoolPromise(pub BoxPromise<'static, Result<bool>>);

#[no_mangle]
pub extern "C" fn bool_promise_resolve(promise: *mut BoolPromise) -> bool {
    unwrap_or_default(take_ownership(promise).0.resolve())
}

pub struct StringPromise(pub BoxPromise<'static, Result<Option<String>>>);

#[no_mangle]
pub extern "C" fn string_promise_resolve(promise: *mut StringPromise) -> *mut c_char {
    try_release_optional_string(take_ownership(promise).0.resolve().transpose())
}
