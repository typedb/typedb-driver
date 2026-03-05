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

use typedb_driver::{box_stream, Addresses, Credentials, DriverOptions, Server, TypeDBDriver};

use crate::{
    common::{
        error::{try_release, try_release_optional, unwrap_or_default, unwrap_void},
        iterator::CIterator,
        iterators_to_map,
        memory::{borrow, free, release, string_array_view, string_view},
    },
    server::{
        server_routing::{native_server_routing, ServerRouting},
        server::ServerIterator,
        server_version::ServerVersion,
    },
};

const DRIVER_LANG: &'static str = "c";

// TODO: These constructors get out of hand. Consider driver builders based on a config!

/// Open a TypeDB C Driver to a TypeDB server available at the provided address.
///
/// @param address The address on which the TypeDB Server is running
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
#[no_mangle]
pub extern "C" fn driver_new(
    address: *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
) -> *mut TypeDBDriver {
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_address_str(string_view(address))),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        DRIVER_LANG,
    ))
}

/// Open a TypeDB Driver to a TypeDB server available at the provided address.
/// This method allows driver language specification for drivers built on top of the native C layer.
///
/// @param address The address on which the TypeDB Server is running
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
/// @param driver_lang The language of the driver connecting to the server
#[no_mangle]
pub extern "C" fn driver_new_with_description(
    address: *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
    driver_lang: *const c_char,
) -> *mut TypeDBDriver {
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_address_str(string_view(address))),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        string_view(driver_lang),
    ))
}

/// Open a TypeDB C Driver to a TypeDB cluster available at the provided addresses.
///
/// @param addresses A null-terminated array holding the server addresses on for connection
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
#[no_mangle]
pub extern "C" fn driver_new_with_addresses(
    addresses: *const *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
) -> *mut TypeDBDriver {
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_addresses_str(string_array_view(addresses))),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        DRIVER_LANG,
    ))
}

/// Open a TypeDB Driver to a TypeDB cluster available at the provided addresses.
/// This method allows driver language specification for drivers built on top of the native C layer.
///
/// @param addresses A null-terminated array holding the TypeDB cluster replica addresses on for connection
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
/// @param driver_lang The language of the driver connecting to the server
#[no_mangle]
pub extern "C" fn driver_new_with_addresses_with_description(
    addresses: *const *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
    driver_lang: *const c_char,
) -> *mut TypeDBDriver {
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_addresses_str(string_array_view(addresses))),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        string_view(driver_lang),
    ))
}

/// Open a TypeDB C Driver to a TypeDB cluster, using the provided address translation.
///
/// @param public_addresses A null-terminated array holding the replica addresses on for connection.
/// @param private_addresses A null-terminated array holding the private replica addresses, configured on the server side.
/// This array <i>must</i> have the same length as <code>public_addresses</code>.
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
#[no_mangle]
pub extern "C" fn driver_new_with_address_translation(
    public_addresses: *const *const c_char,
    private_addresses: *const *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
) -> *mut TypeDBDriver {
    let addresses = iterators_to_map(string_array_view(public_addresses), string_array_view(private_addresses));
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_translation_str(addresses)),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        DRIVER_LANG,
    ))
}

/// Open a TypeDB Driver to a TypeDB cluster, using the provided address translation.
/// This method allows driver language specification for drivers built on top of the native C layer.
///
/// @param public_addresses A null-terminated array holding the replica addresses on for connection.
/// @param private_addresses A null-terminated array holding the private replica addresses, configured on the server side.
/// This array <i>must</i> have the same length as <code>public_addresses</code>.
/// @param credentials The <code>Credentials</code> to connect with
/// @param driver_options The <code>DriverOptions</code> to connect with
/// @param driver_lang The language of the driver connecting to the server
#[no_mangle]
pub extern "C" fn driver_new_with_address_translation_with_description(
    public_addresses: *const *const c_char,
    private_addresses: *const *const c_char,
    credentials: *const Credentials,
    driver_options: *const DriverOptions,
    driver_lang: *const c_char,
) -> *mut TypeDBDriver {
    let addresses = iterators_to_map(string_array_view(public_addresses), string_array_view(private_addresses));
    try_release(TypeDBDriver::new_with_description(
        unwrap_or_default(Addresses::try_from_translation_str(addresses)),
        borrow(credentials).clone(),
        borrow(driver_options).clone(),
        string_view(driver_lang),
    ))
}

/// Closes the <code>TypeDBDriver</code>. Before instantiating a new driver, the driver that’s currently open should first be closed.
/// Closing a driver frees the underlying Rust object.
///
/// @param driver The <code>TypeDBDriver</code> object.
#[no_mangle]
pub extern "C" fn driver_close(driver: *mut TypeDBDriver) {
    free(driver);
}

/// Forcibly closes the <code>TypeDBDriver</code>. To be used in exceptional cases.
///
/// @param driver The <code>TypeDBDriver</code> object.
#[no_mangle]
pub extern "C" fn driver_force_close(driver: *mut TypeDBDriver) {
    unwrap_void(borrow(driver).force_close());
}

/// Checks whether this connection is presently open.
///
/// @param driver The <code>TypeDBDriver</code> object.
#[no_mangle]
pub extern "C" fn driver_is_open(driver: *const TypeDBDriver) -> bool {
    borrow(driver).is_open()
}

/// Retrieves the server version and distribution information.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param server_routing The server routing directive to use for the operation. Auto if null.
#[no_mangle]
pub extern "C" fn driver_server_version(
    driver: *const TypeDBDriver,
    server_routing: *const ServerRouting,
) -> *mut ServerVersion {
    let driver = borrow(driver);
    let result = match native_server_routing(server_routing) {
        Some(routing) => driver.server_version_with_routing(routing),
        None => driver.server_version(),
    };
    release(unwrap_or_default(result.map(|server_version| {
        ServerVersion::new(server_version.distribution().to_string(), server_version.version().to_string())
    })))
}

/// Retrieves the servers.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param server_routing The server routing directive to use for the operation. Auto if null.
#[no_mangle]
pub extern "C" fn driver_servers(
    driver: *const TypeDBDriver,
    server_routing: *const ServerRouting,
) -> *mut ServerIterator {
    let driver = borrow(driver);
    let result = match native_server_routing(server_routing) {
        Some(routing) => driver.servers_with_routing(routing),
        None => driver.servers(),
    };
    release(ServerIterator(CIterator(box_stream(unwrap_or_default(result).into_iter()))))
}

/// Retrieves the server's primary server, if exists.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param server_routing The server routing directive to use for the operation. Auto if null.
#[no_mangle]
pub extern "C" fn driver_primary_server(
    driver: *const TypeDBDriver,
    server_routing: *const ServerRouting,
) -> *mut Server {
    let driver = borrow(driver);
    let result = match native_server_routing(server_routing) {
        Some(routing) => driver.primary_server_with_routing(routing),
        None => driver.primary_server(),
    };
    // TODO: Return somehow else!! This will not work through SWIG
    try_release_optional(result.map(|res| res.map(|rep| Server::Available(rep))).transpose())
}

/// Registers a new server in the cluster the driver is currently connected to. The registered
/// server will become available eventually, depending on the behavior of the whole cluster.
/// To register a server, its clustering address should be passed, not the connection address.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param server_id The numeric identifier of the new server
/// @param address The address(es) of the TypeDB server as a string
#[no_mangle]
pub extern "C" fn driver_register_server(driver: *const TypeDBDriver, server_id: i64, address: *const c_char) {
    unwrap_void(borrow(driver).register_server(server_id as u64, string_view(address).to_string()))
}

/// Deregisters a server from the cluster the driver is currently connected to. This server
/// will no longer play a raft role in this cluster.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param server_id The numeric identifier of the deregistered server
#[no_mangle]
pub extern "C" fn driver_deregister_server(driver: *const TypeDBDriver, server_id: i64) {
    unwrap_void(borrow(driver).deregister_server(server_id as u64))
}

/// Updates address translation of the driver. This lets you actualize new translation
/// information without recreating the driver from scratch. Useful after registering new
/// servers requiring address translation.
/// This operation will update existing connections using the provided addresses.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param public_addresses A null-terminated array holding the server addresses for connection.
/// @param private_addresses A null-terminated array holding the private server addresses, configured on the server side.
/// This array <i>must</i> have the same length as <code>public_addresses</code>.
#[no_mangle]
pub extern "C" fn driver_update_address_translation(
    driver: *const TypeDBDriver,
    public_addresses: *const *const c_char,
    private_addresses: *const *const c_char,
) {
    let translation = iterators_to_map(string_array_view(public_addresses), string_array_view(private_addresses));
    let addresses = unwrap_or_default(Addresses::try_from_translation_str(translation));
    unwrap_void(borrow(driver).update_address_translation(addresses))
}
