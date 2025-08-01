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

use std::{ffi::c_char, path::Path, ptr::addr_of_mut, sync::Arc};

use typedb_driver::{box_stream, info::ReplicaInfo, Database};

use super::{
    error::{try_release_string, unwrap_void},
    iterator::{iterator_next, CIterator},
    memory::{borrow, borrow_mut, free, release, release_optional, release_string, take_ownership},
};
use crate::memory::{decrement_arc, string_view, take_arc};

/// Frees the native rust <code>Database</code> object
#[no_mangle]
pub extern "C" fn database_close(database: *const Database) {
    decrement_arc(database)
}

/// The database name as a string.
#[no_mangle]
pub extern "C" fn database_get_name(database: *const Database) -> *mut c_char {
    release_string(borrow(database).name().to_owned())
}

/// Deletes this database.
#[no_mangle]
pub extern "C" fn database_delete(database: *const Database) {
    unwrap_void(take_arc(database).delete());
}

/// A full schema text as a valid TypeQL define query string.
#[no_mangle]
pub extern "C" fn database_schema(database: *const Database) -> *mut c_char {
    try_release_string(borrow(database).schema())
}

/// The types in the schema as a valid TypeQL define query string.
#[no_mangle]
pub extern "C" fn database_type_schema(database: *const Database) -> *mut c_char {
    try_release_string(borrow(database).type_schema())
}

/// Export a database into a schema definition and a data files saved to the disk.
/// This is a blocking operation and may take a significant amount of time depending on the database size.
///
/// @param database The <code>Database</code> object to export from.
/// @param schema_file The path to the schema definition file to be created.
/// @param data_file The path to the data file to be created.
#[no_mangle]
pub extern "C" fn database_export_to_file(
    database: *const Database,
    schema_file: *const c_char,
    data_file: *const c_char,
) {
    let schema_file_path = Path::new(string_view(schema_file));
    let data_file_path = Path::new(string_view(data_file));
    unwrap_void(borrow(database).export_to_file(schema_file_path, data_file_path))
}

// /// Iterator over the <code>ReplicaInfo</code> corresponding to each replica of a TypeDB cloud database.
// pub struct ReplicaInfoIterator(CIterator<ReplicaInfo>);
//
// /// Forwards the <code>ReplicaInfoIterator</code> and returns the next <code>ReplicaInfo</code> if it exists,
// /// or null if there are no more elements.
// #[no_mangle]
// pub extern "C" fn replica_info_iterator_next(it: *mut ReplicaInfoIterator) -> *mut ReplicaInfo {
//     unsafe { iterator_next(addr_of_mut!((*it).0)) }
// }
//
// /// Frees the native rust <code>ReplicaInfoIterator</code> object
// #[no_mangle]
// pub extern "C" fn replica_info_iterator_drop(it: *mut ReplicaInfoIterator) {
//     free(it);
// }
//
// /// Set of <code>Replica</code> instances for this database.
// /// <b>Only works in TypeDB Cloud</b>
// #[no_mangle]
// pub extern "C" fn database_get_replicas_info(database: *const Database) -> *mut ReplicaInfoIterator {
//     release(ReplicaInfoIterator(CIterator(box_stream(borrow(database).replicas_info().into_iter()))))
// }
//
// /// Returns the primary replica for this database.
// /// _Only works in TypeDB Cloud_
// #[no_mangle]
// pub extern "C" fn database_get_primary_replica_info(database: *const Database) -> *mut ReplicaInfo {
//     release_optional(borrow(database).primary_replica_info())
// }
//
// /// Returns the preferred replica for this database.
// /// Operations which can be run on any replica will prefer to use this replica.
// /// _Only works in TypeDB Cloud_
// #[no_mangle]
// pub extern "C" fn database_get_preferred_replica_info(database: *const Database) -> *mut ReplicaInfo {
//     release_optional(borrow(database).preferred_replica_info())
// }
//
// /// Frees the native rust <code>ReplicaInfo</code> object
// #[no_mangle]
// pub extern "C" fn replica_info_drop(replica_info: *mut ReplicaInfo) {
//     free(replica_info);
// }
//
// /// The server hosting this replica
// #[no_mangle]
// pub extern "C" fn replica_info_get_server(replica_info: *const ReplicaInfo) -> *mut c_char {
//     release_string(borrow(replica_info).server.to_string())
// }
//
// /// Checks whether this is the primary replica of the raft cluster.
// #[no_mangle]
// pub extern "C" fn replica_info_is_primary(replica_info: *const ReplicaInfo) -> bool {
//     borrow(replica_info).is_primary
// }
//
// /// Checks whether this is the preferred replica of the raft cluster.
// /// If true, Operations which can be run on any replica will prefer to use this replica.
// #[no_mangle]
// pub extern "C" fn replica_info_is_preferred(replica_info: *const ReplicaInfo) -> bool {
//     borrow(replica_info).is_preferred
// }
//
// /// The raft protocol ‘term’ of this replica.
// #[no_mangle]
// pub extern "C" fn replica_info_get_term(replica_info: *const ReplicaInfo) -> i64 {
//     borrow(replica_info).term
// }
