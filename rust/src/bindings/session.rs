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

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, borrow_mut, free, release_string, take_ownership},
};
use crate::{Database, Options, Session, SessionType};

#[no_mangle]
pub extern "C" fn session_new(
    database: *mut Database,
    session_type: SessionType,
    options: *const Options,
) -> *mut Session {
    try_release(Session::new_with_options(take_ownership(database), session_type, borrow(options).clone()))
}

#[no_mangle]
pub extern "C" fn session_drop(session: *mut Session) {
    free(session);
}

#[no_mangle]
pub extern "C" fn session_get_database_name(session: *const Session) -> *mut c_char {
    release_string(borrow(session).database_name().to_owned())
}

#[no_mangle]
pub extern "C" fn session_is_open(session: *const Session) -> bool {
    borrow(session).is_open()
}

#[no_mangle]
pub extern "C" fn session_force_close(session: *mut Session) {
    unwrap_void(borrow_mut(session).force_close())
}

#[no_mangle]
pub extern "C" fn session_on_close(session: *const Session, callback_id: usize, callback: extern "C" fn(usize)) {
    borrow(session).on_close(move || callback(callback_id));
}
