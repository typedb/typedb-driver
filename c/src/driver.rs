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

use typedb_driver::{box_stream, Addresses, Credentials, DriverOptions, ServerReplica, TypeDBDriver};

use crate::{
    common::{
        error::{try_release, unwrap_or_default, unwrap_void},
        iterator::CIterator,
        iterators_to_map,
        memory::{borrow, free, release, release_optional, string_array_view, string_view},
    },
    server::{server_replica::ServerReplicaIterator, server_version::ServerVersion},
};
use crate::common::memory::borrow_mut;

const DRIVER_LANG: &'static str = "c";

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

/// Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
/// Closing a driver frees the underlying Rust object.
#[no_mangle]
pub extern "C" fn driver_close(driver: *mut TypeDBDriver) {
    free(driver);
}

/// Forcibly closes the driver. To be used in exceptional cases.
#[no_mangle]
pub extern "C" fn driver_force_close(driver: *mut TypeDBDriver) {
    unwrap_void(borrow(driver).force_close());
}

/// Checks whether this connection is presently open.
#[no_mangle]
pub extern "C" fn driver_is_open(driver: *const TypeDBDriver) -> bool {
    borrow(driver).is_open()
}

/// Retrieves the server version and distribution information.
#[no_mangle]
pub extern "C" fn driver_server_version(driver: *const TypeDBDriver) -> *mut ServerVersion {
    release(unwrap_or_default(borrow(driver).server_version().map(|server_version| {
        ServerVersion::new(server_version.distribution().to_string(), server_version.version().to_string())
    })))
}

/// Retrieves the server's replicas.
#[no_mangle]
pub extern "C" fn driver_replicas(driver: *const TypeDBDriver) -> *mut ServerReplicaIterator {
    release(ServerReplicaIterator(CIterator(box_stream(borrow(driver).replicas().into_iter()))))
}

/// Retrieves the server's primary replica, if exists.
#[no_mangle]
pub extern "C" fn driver_primary_replica(driver: *const TypeDBDriver) -> *mut ServerReplica {
    release_optional(borrow(driver).primary_replica())
}

/// Registers a new replica in the cluster the driver is currently connected to. The registered
/// replica will become available eventually, depending on the behavior of the whole cluster.
///
/// @param replica_id The numeric identifier of the new replica
/// @param address The address(es) of the TypeDB replica as a string
#[no_mangle]
pub extern "C" fn driver_register_replica(driver: *const TypeDBDriver, replica_id: i64, address: *const c_char) {
    unwrap_void(borrow(driver).register_replica(replica_id as u64, string_view(address).to_string()))
}

/// Deregisters a replica from the cluster the driver is currently connected to. This replica
/// will no longer play a raft role in this cluster.
///
/// @param replica_id The numeric identifier of the deregistered replica
#[no_mangle]
pub extern "C" fn driver_deregister_replica(driver: *const TypeDBDriver, replica_id: i64) {
    unwrap_void(borrow(driver).deregister_replica(replica_id as u64))
}
