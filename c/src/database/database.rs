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

use typedb_driver::Database;

use crate::{
    common::{
        error::{try_release_string, unwrap_void},
        memory::{borrow, decrement_arc, release_string, string_view, take_arc},
    },
    server::consistency_level::{native_consistency_level, ConsistencyLevel},
};

/// Frees the native rust <code>Database</code> object.
#[no_mangle]
pub extern "C" fn database_close(database: *const Database) {
    decrement_arc(database)
}

/// The <code>Database</code> name as a string.
#[no_mangle]
pub extern "C" fn database_get_name(database: *const Database) -> *mut c_char {
    release_string(borrow(database).name().to_owned())
}

/// Deletes this <code>Database</code>.
#[no_mangle]
pub extern "C" fn database_delete(database: *const Database) {
    unwrap_void(take_arc(database).delete());
}

/// A full schema text as a valid TypeQL define query string.
///
/// @param database The <code>Database</code> to get the schema from.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn database_schema(
    database: *const Database,
    consistency_level: *const ConsistencyLevel,
) -> *mut c_char {
    let database = borrow(database);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => database.schema_with_consistency(consistency_level),
        None => database.schema(),
    };
    try_release_string(result)
}

/// The types in the schema as a valid TypeQL define query string.
///
/// @param database The <code>Database</code> to get the type schema from.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn database_type_schema(
    database: *const Database,
    consistency_level: *const ConsistencyLevel,
) -> *mut c_char {
    let database = borrow(database);
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => database.type_schema_with_consistency(consistency_level),
        None => database.type_schema(),
    };
    try_release_string(result)
}

/// Export a database into a schema definition and a data files saved to the disk.
/// This is a blocking operation and may take a significant amount of time depending on the database size.
///
/// @param database The <code>Database</code> object to export from.
/// @param schema_file The path to the schema definition file to be created.
/// @param data_file The path to the data file to be created.
/// @param consistency_level The consistency level to use for the operation. Strongest possible if null.
#[no_mangle]
pub extern "C" fn database_export_to_file(
    database: *const Database,
    schema_file: *const c_char,
    data_file: *const c_char,
    consistency_level: *const ConsistencyLevel,
) {
    let database = borrow(database);
    let schema_file_path = Path::new(string_view(schema_file));
    let data_file_path = Path::new(string_view(data_file));
    let result = match native_consistency_level(consistency_level) {
        Some(consistency_level) => {
            database.export_to_file_with_consistency(schema_file_path, data_file_path, consistency_level)
        }
        None => database.export_to_file(schema_file_path, data_file_path),
    };
    unwrap_void(result)
}
