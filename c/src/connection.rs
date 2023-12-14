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

use std::{ffi::c_char, path::Path};

use typedb_driver::{Connection, Credential};

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, free, release, string_array_view, string_view},
};

static DEFAULT_NAME: &str = "FFI";
static DRIVER_VERSION: &str = include_str!("VERSION");

#[no_mangle]
pub extern "C" fn connection_open_core(address: *const c_char) -> *mut Connection {
    try_release(Connection::new_core_with_id(string_view(address), DEFAULT_NAME, DRIVER_VERSION))
}

#[no_mangle]
pub extern "C" fn connection_open_core_with_id(
    address: *const c_char,
    driver_name: *const c_char,
    driver_version: *const c_char,
) -> *mut Connection {
    try_release(Connection::new_core_with_id(
        string_view(address),
        string_view(driver_name),
        string_view(driver_version),
    ))
}

#[no_mangle]
pub extern "C" fn connection_open_cloud(
    addresses: *const *const c_char,
    credential: *const Credential,
) -> *mut Connection {
    let addresses: Vec<&str> = string_array_view(addresses).collect();
    try_release(Connection::new_cloud_with_id(&addresses, borrow(credential).clone(), DEFAULT_NAME, DRIVER_VERSION))
}

#[no_mangle]
pub extern "C" fn connection_open_cloud_with_id(
    addresses: *const *const c_char,
    credential: *const Credential,
    driver_name: *const c_char,
    driver_version: *const c_char,
) -> *mut Connection {
    let addresses: Vec<&str> = string_array_view(addresses).collect();
    try_release(Connection::new_cloud_with_id(
        &addresses,
        borrow(credential).clone(),
        string_view(driver_name),
        string_view(driver_version),
    ))
}

#[no_mangle]
pub extern "C" fn connection_close(connection: *mut Connection) {
    free(connection);
}

#[no_mangle]
pub extern "C" fn connection_is_open(connection: *const Connection) -> bool {
    borrow(connection).is_open()
}

#[no_mangle]
pub extern "C" fn connection_force_close(connection: *mut Connection) {
    unwrap_void(borrow(connection).force_close());
}

#[no_mangle]
pub extern "C" fn credential_new(
    username: *const c_char,
    password: *const c_char,
    tls_root_ca: *const c_char,
    with_tls: bool,
) -> *mut Credential {
    let username = string_view(username);
    let password = string_view(password);
    if with_tls {
        let tls_root_ca = unsafe { tls_root_ca.as_ref().map(|str| Path::new(string_view(str))) };
        try_release(Credential::with_tls(username, password, tls_root_ca))
    } else {
        release(Credential::without_tls(username, password))
    }
}

#[no_mangle]
pub extern "C" fn credential_drop(credential: *mut Credential) {
    free(credential);
}
