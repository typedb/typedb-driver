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

use typedb_driver::{BoxPromise, Promise, Result};

use crate::{
    error::{try_release, unwrap_void},
    memory::{release, take_ownership},
};

pub struct CPromise<T: 'static>(BoxPromise<'static, T>);

pub(super) fn promise_resolve<T: 'static>(promise: *mut CPromise<T>) -> *mut T {
    release(take_ownership(promise).0.resolve())
}

pub(super) fn promise_try_resolve<T: 'static>(promise: *mut CPromise<Result<T>>) -> *mut T {
    try_release(take_ownership(promise).0.resolve())
}

pub struct VoidPromise(pub BoxPromise<'static, Result<()>>);

#[no_mangle]
pub extern "C" fn void_promise_resolve(promise: *mut VoidPromise) {
    unwrap_void(take_ownership(promise).0.resolve());
}
