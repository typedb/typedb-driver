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

use std::ptr::addr_of_mut;

use typedb_driver::{BoxPromise, Promise, Result, answer::ConceptRow, concept::Concept};

use crate::common::{
    error::try_release_optional,
    iterator::{iterator_try_next, CIterator},
    memory::{free, take_ownership},
};

mod concept;
mod instance;

/// Iterator over the <code>ConceptRow</code>s returned by an API method or query.
pub struct ConceptRowIterator(pub CIterator<Result<ConceptRow>>);

/// Forwards the <code>ConceptRowIterator</code> and returns the next <code>ConceptRow</code> if it exists,
/// or null if there are no more elements.
#[unsafe(no_mangle)]
pub extern "C" fn concept_row_iterator_next(it: *mut ConceptRowIterator) -> *mut ConceptRow {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ConceptRowIterator</code> object
#[unsafe(no_mangle)]
pub extern "C" fn concept_row_iterator_drop(it: *mut ConceptRowIterator) {
    free(it);
}

/// Iterator over the <code>Concepts</code>s returned by an API method or query.
pub struct ConceptIterator(pub CIterator<Result<Concept>>);

/// Forwards the <code>ConceptIterator</code> and returns the next <code>Concept</code> if it exists,
/// or null if there are no more elements.
#[unsafe(no_mangle)]
pub extern "C" fn concept_iterator_next(it: *mut ConceptIterator) -> *mut Concept {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ConceptIterator</code> object
#[unsafe(no_mangle)]
pub extern "C" fn concept_iterator_drop(it: *mut ConceptIterator) {
    free(it);
}
