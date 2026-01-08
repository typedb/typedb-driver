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

use typedb_driver::{box_stream, Database, TypeDBDriver};

use super::{
    error::{try_release, unwrap_or_default, unwrap_void},
    iterator::CIterator,
    memory::{borrow_mut, free, release_arc, string_view},
};
use crate::iterator::iterator_boxed_arc_next;

/// An <code>Iterator</code> over databases present on the TypeDB server.
pub struct DatabaseIterator(CIterator<Arc<Database>>);

/// Forwards the <code>DatabaseIterator</code> and returns the next <code>Database</code> if it exists,
/// or null if there are no more elements. Returns a unique boxed Arc handle.
#[no_mangle]
pub extern "C" fn database_iterator_next(it: *mut DatabaseIterator) -> *const Database {
    unsafe { iterator_boxed_arc_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>DatabaseIterator</code> object.
#[no_mangle]
pub extern "C" fn database_iterator_drop(it: *mut DatabaseIterator) {
    free(it);
}

/// Returns a <code>DatabaseIterator</code> over all databases present on the TypeDB server.
#[no_mangle]
pub extern "C" fn databases_all(driver: *mut TypeDBDriver) -> *mut DatabaseIterator {
    try_release(
        borrow_mut(driver).databases().all().map(|dbs| DatabaseIterator(CIterator(box_stream(dbs.into_iter())))),
    )
}

/// Create a database with the given name.
#[no_mangle]
pub extern "C" fn databases_create(driver: *mut TypeDBDriver, name: *const c_char) {
    unwrap_void(borrow_mut(driver).databases().create(string_view(name)));
}

/// Create a database with the given name based on previously exported another database's data
/// loaded from a file.
/// This is a blocking operation and may take a significant amount of time depending on the database
/// size.
///
/// @param driver The <code>TypeDBDriver</code> object.
/// @param name The name of the database to be created.
/// @param schema The schema definition query string for the database.
/// @param data_file The exported database file path to import the data from.
#[no_mangle]
pub extern "C" fn databases_import_from_file(
    driver: *mut TypeDBDriver,
    name: *const c_char,
    schema: *const c_char,
    data_file: *const c_char,
) {
    let data_file_path = Path::new(string_view(data_file));
    unwrap_void(borrow_mut(driver).databases().import_from_file(string_view(name), string_view(schema), data_file_path))
}

/// Checks if a database with the given name exists.
#[no_mangle]
pub extern "C" fn databases_contains(driver: *mut TypeDBDriver, name: *const c_char) -> bool {
    unwrap_or_default(borrow_mut(driver).databases().contains(string_view(name)))
}

/// Retrieve the database with the given name. Returns a unique boxed Arc handle.
#[no_mangle]
pub extern "C" fn databases_get(driver: *mut TypeDBDriver, name: *const c_char) -> *const Database {
    use std::ptr::null;
    match borrow_mut(driver).databases().get(string_view(name)) {
        Ok(arc) => release_arc(arc),
        Err(err) => {
            crate::error::record_error(err);
            null()
        }
    }
}
