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

use typedb_driver::TypeDBDriver;

use super::{
    error::{try_release, unwrap_void},
    memory::{borrow, free, string_view},
};

/// Open a TypeDB Driver to a TypeDB Core server available at the provided address.
///
/// @param address The address of the TypeDB server
#[no_mangle]
pub extern "C" fn connection_open_core(address: *const c_char) -> *mut TypeDBDriver {
    try_release(TypeDBDriver::new_core(string_view(address)))
}

///// Open a TypeDB Driver to TypeDB Cloud server(s) available at the provided addresses, using
///// the provided credential.
/////
///// @param addresses a null-terminated array holding the address(es) of the TypeDB server(s)
///// @param credential The <code>Credential</code> to connect with
// #[no_mangle]
// pub extern "C" fn connection_open_cloud(
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
// pub extern "C" fn connection_open_cloud_translated(
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
pub extern "C" fn connection_close(connection: *mut TypeDBDriver) {
    free(connection);
}

/// Checks whether this connection is presently open.
#[no_mangle]
pub extern "C" fn connection_is_open(connection: *const TypeDBDriver) -> bool {
    borrow(connection).is_open()
}

/// Forcibly closes the driver. To be used in exceptional cases.
#[no_mangle]
pub extern "C" fn connection_force_close(connection: *mut TypeDBDriver) {
    unwrap_void(borrow(connection).force_close());
}

///// Creates a new <code>Credential</code> for connecting to TypeDB Cloud.
/////
///// @param username The name of the user to connect as
///// @param password The password for the user
///// @param tls_root_ca Path to the CA certificate to use for authenticating server certificates.
///// @param with_tls Specify whether the connection to TypeDB Cloud must be done over TLS
// #[no_mangle]
// pub extern "C" fn credential_new(
//     username: *const c_char,
//     password: *const c_char,
//     tls_root_ca: *const c_char,
//     with_tls: bool,
// ) -> *mut Credential {
//     let username = string_view(username);
//     let password = string_view(password);
//     if with_tls {
//         let tls_root_ca = unsafe { tls_root_ca.as_ref().map(|str| Path::new(string_view(str))) };
//         try_release(Credential::with_tls(username, password, tls_root_ca))
//     } else {
//         release(Credential::without_tls(username, password))
//     }
// }
//
///// Frees the native rust <code>Credential</code> object
// #[no_mangle]
// pub extern "C" fn credential_drop(credential: *mut Credential) {
//     free(credential);
// }
