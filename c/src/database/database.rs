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

use crate::common::{
    error::{try_release_string, unwrap_void},
    memory::{borrow, decrement_arc, release_string, string_view, take_arc},
};

/// Frees the native rust <code>Database</code> object.
#[unsafe(no_mangle)]
pub extern "C" fn database_close(database: *const Database) {
    decrement_arc(database)
}

/// The <code>Database</code> name as a string.
#[unsafe(no_mangle)]
pub extern "C" fn database_get_name(database: *const Database) -> *mut c_char {
    release_string(borrow(database).name().to_owned())
}

/// Deletes this database.
///
/// @param database The <code>Database</code> to delete.
#[unsafe(no_mangle)]
pub extern "C" fn database_delete(database: *const Database) {
    unwrap_void(take_arc(database).delete());
}

/// A full schema text as a valid TypeQL define query string.
///
/// @param database The <code>Database</code> to get the schema from.
#[unsafe(no_mangle)]
pub extern "C" fn database_schema(database: *const Database) -> *mut c_char {
    try_release_string(borrow(database).schema())
}

/// The types in the schema as a valid TypeQL define query string.
///
/// @param database The <code>Database</code> to get the type schema from.
#[unsafe(no_mangle)]
pub extern "C" fn database_type_schema(database: *const Database) -> *mut c_char {
    try_release_string(borrow(database).type_schema())
}

/// Export a database into a schema definition and a data files saved to the disk.
/// This is a blocking operation and may take a significant amount of time depending on the database size.
///
/// @param database The <code>Database</code> object to export from.
/// @param schema_file The path to the schema definition file to be created.
/// @param data_file The path to the data file to be created.
#[unsafe(no_mangle)]
pub extern "C" fn database_export_to_file(
    database: *const Database,
    schema_file: *const c_char,
    data_file: *const c_char,
) {
    let database = borrow(database);
    let schema_file_path = Path::new(string_view(schema_file));
    let data_file_path = Path::new(string_view(data_file));
    unwrap_void(database.export_to_file(schema_file_path, data_file_path))
}
