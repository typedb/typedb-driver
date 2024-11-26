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

use std::{ffi::c_char, path::Path};

use typedb_driver::{ConnectionSettings, Credential, TypeDBDriver};

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, free, string_view},
};
use crate::memory::release;

/// Open a TypeDB Driver to a TypeDB Core server available at the provided address.
///
/// @param address The address (host:port) on which the TypeDB Server is running
/// @param driver_lang The language of the driver connecting to the server
#[no_mangle]
pub extern "C" fn driver_open_core(
    address: *const c_char,
    credential: *const Credential,
    connection_settings: *const ConnectionSettings,
    driver_lang: *const c_char,
) -> *mut TypeDBDriver {
    // TODO: Add a separate entry point for C with a provided "c" driver_lang!
    try_release(TypeDBDriver::new_core_with_description(
        string_view(address),
        borrow(credential).clone(),
        borrow(connection_settings).clone(),
        string_view(driver_lang),
    ))
}

///// Open a TypeDB Driver to TypeDB Cloud server(s) available at the provided addresses, using
///// the provided credential.
/////
///// @param addresses a null-terminated array holding the address(es) of the TypeDB server(s)
///// @param credential The <code>Credential</code> to connect with
// #[no_mangle]
// pub extern "C" fn driver_open_cloud(
//     addresses: *const *const c_char,
//     credential: *const Credential,
// ) -> *mut TypeDBDriver {
//     let addresses: Vec<&str> = string_array_view(addresses).collect();
//     try_release(TypeDBDriver::new_cloud(&addresses, borrow(credential).clone()))
// }

///// Open a TypeDB Driver to TypeDB Cloud server(s), using provided address translation, with
///// the provided credential.
/////
///// @param public_addresses A null-terminated array holding the address(es) of the TypeDB server(s)
///// the driver will connect to. This array <i>must</i> have the same length as <code>advertised_addresses</code>
///// @param private_addresses A null-terminated array holding the address(es) the TypeDB server(s)
///// are configured to advertise
///// @param credential The <code>Credential</code> to connect with
// #[no_mangle]
// pub extern "C" fn driver_open_cloud_translated(
//     public_addresses: *const *const c_char,
//     private_addresses: *const *const c_char,
//     credential: *const Credential,
// ) -> *mut TypeDBDriver {
//     let addresses = string_array_view(public_addresses).zip_eq(string_array_view(private_addresses)).collect();
//     try_release(TypeDBDriver::new_cloud_with_translation(addresses, borrow(credential).clone()))
// }

/// Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
/// Closing a connction frees the underlying rust object.
#[no_mangle]
pub extern "C" fn driver_close(driver: *mut TypeDBDriver) {
    free(driver);
}

/// Checks whether this connection is presently open.
#[no_mangle]
pub extern "C" fn driver_is_open(driver: *const TypeDBDriver) -> bool {
    borrow(driver).is_open()
}

/// Forcibly closes the driver. To be used in exceptional cases.
#[no_mangle]
pub extern "C" fn driver_force_close(driver: *mut TypeDBDriver) {
    unwrap_void(borrow(driver).force_close());
}

// Creates a new <code>Credential</code> for connecting to TypeDB Server.
//
// @param username The name of the user to connect as
// @param password The password for the user
#[no_mangle]
pub extern "C" fn credential_new(username: *const c_char, password: *const c_char) -> *mut Credential {
    release(Credential::new(string_view(username), string_view(password)))
}

// Frees the native rust <code>Credential</code> object
#[no_mangle]
pub extern "C" fn credential_drop(credential: *mut Credential) {
    free(credential);
}

// Creates a new <code>ConnectionSettings</code> for connecting to TypeDB Server.
//
// @param tls_root_ca Path to the CA certificate to use for authenticating server certificates.
// @param with_tls Specify whether the connection to TypeDB Cloud must be done over TLS
#[no_mangle]
pub extern "C" fn connection_settings_new(is_tls_enabled: bool, tls_root_ca: *const c_char) -> *mut ConnectionSettings {
    let tls_root_ca_path = unsafe { tls_root_ca.as_ref().map(|str| Path::new(string_view(str))) };
    try_release(ConnectionSettings::new(is_tls_enabled, tls_root_ca_path))
}

// Frees the native rust <code>ConnectionSettings</code> object
#[no_mangle]
pub extern "C" fn connection_settings_drop(connection_settings: *mut ConnectionSettings) {
    free(connection_settings);
}
