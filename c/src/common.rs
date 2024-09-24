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

use std::{ffi::c_char, ptr::null_mut};

use typedb_driver::Result;

use super::{
    iterator::CIterator,
    memory::{borrow_mut, free},
};
use crate::error::try_release_string;

/// Iterator over the strings in the result of a request or a TypeQL Fetch query.
pub struct StringIterator(pub CIterator<Result<String>>);

/// Forwards the <code>StringIterator</code> and returns the next string if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn string_iterator_next(it: *mut StringIterator) -> *mut c_char {
    borrow_mut(it).0 .0.next().map(try_release_string).unwrap_or_else(null_mut)
}

/// Frees the native rust <code>StringIterator</code> object
#[no_mangle]
pub extern "C" fn string_iterator_drop(it: *mut StringIterator) {
    free(it);
}
