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

use std::{ptr::null, sync::Arc};

use typedb_driver::{BoxStream, Result};

use super::{
    error::try_release_optional,
    memory::{borrow_mut, release_arc, release_optional},
};

pub struct CIterator<T: 'static>(pub(super) BoxStream<'static, T>);

pub(super) fn iterator_next<T: 'static>(it: *mut CIterator<T>) -> *mut T {
    release_optional(borrow_mut(it).0.next())
}

pub(super) fn iterator_try_next<T: 'static>(it: *mut CIterator<Result<T>>) -> *mut T {
    try_release_optional(borrow_mut(it).0.next())
}

/// Iterates over Arc elements, returning unique boxed Arc handles.
/// The returned pointer is typed as *const T for FFI compatibility, but actually
/// points to a Box<Arc<T>>. It must be passed to take_arc or decrement_arc exactly once.
pub(crate) fn iterator_boxed_arc_next<T: 'static>(it: *mut CIterator<Arc<T>>) -> *const T {
    borrow_mut(it).0.next().map(release_arc).unwrap_or_else(null)
}
