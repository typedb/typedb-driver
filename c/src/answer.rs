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

use typedb_driver::{
    answer::{ConceptRow, QueryAnswer, QueryType, ValueGroup},
    box_stream,
    concept::Concept,
    BoxPromise, Promise, Result,
};

use super::{
    concept::ConceptIterator,
    iterator::CIterator,
    memory::{borrow, free, release, release_optional, release_string, string_view},
};
use crate::{common::StringIterator, concept::ConceptRowIterator, error::try_release, memory::take_ownership};

/// Promise object representing the result of an asynchronous operation.
/// Use \ref query_answer_promise_resolve(QueryAnswerPromise*) to wait for and retrieve the resulting boolean value.
pub struct QueryAnswerPromise(BoxPromise<'static, Result<QueryAnswer>>);

impl QueryAnswerPromise {
    pub fn new(promise: impl Promise<'static, Result<QueryAnswer>>) -> Self {
        Self(Box::new(|| promise.resolve()))
    }
}

/// Waits for and returns the result of the operation represented by the <code>QueryAnswer</code> object.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn query_answer_promise_resolve(promise: *mut QueryAnswerPromise) -> *mut QueryAnswer {
    try_release(take_ownership(promise).0.resolve())
}

/// Retrieve the executed query's type of the <code>QueryAnswer</code>.
#[no_mangle]
pub extern "C" fn query_answer_get_query_type(query_answer: *const QueryAnswer) -> QueryType {
    borrow(query_answer).get_query_type()
}

/// Checks if the query answer is an <code>Ok</code>.
#[no_mangle]
pub extern "C" fn query_answer_is_ok(query_answer: *const QueryAnswer) -> bool {
    borrow(query_answer).is_ok()
}

/// Checks if the query answer is a <code>ConceptRowStream</code>.
#[no_mangle]
pub extern "C" fn query_answer_is_concept_row_stream(query_answer: *const QueryAnswer) -> bool {
    borrow(query_answer).is_row_stream()
}

/// Checks if the query answer is a <code>ConceptDocumentStream</code>.
#[no_mangle]
pub extern "C" fn query_answer_is_concept_document_stream(query_answer: *const QueryAnswer) -> bool {
    borrow(query_answer).is_document_stream()
}

/// Produces an <code>Iterator</code> over all <code>ConceptRow</code>s in this <code>QueryAnswer</code>.
#[no_mangle]
pub extern "C" fn query_answer_into_rows(query_answer: *mut QueryAnswer) -> *mut ConceptRowIterator {
    release(ConceptRowIterator(CIterator(take_ownership(query_answer).into_rows())))
}

/// Produces an <code>Iterator</code> over all JSON <code>ConceptDocument</code>s in this <code>QueryAnswer</code>.
#[no_mangle]
pub extern "C" fn query_answer_into_documents(query_answer: *mut QueryAnswer) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(
        take_ownership(query_answer)
            .into_documents()
            .map(|result| result.map(|document| document.into_json().to_string())),
    ))))
}

/// Frees the native rust <code>QueryAnswer</code> object.
#[no_mangle]
pub extern "C" fn query_answer_drop(query_answer: *mut QueryAnswer) {
    free(query_answer);
}

/// Frees the native rust <code>ConceptRow</code> object.
#[no_mangle]
pub extern "C" fn concept_row_drop(concept_row: *mut ConceptRow) {
    free(concept_row);
}

/// Produces an <code>Iterator</code> over all <code>String</code> column names of the <code>ConceptRow</code>'s header.
#[no_mangle]
pub extern "C" fn concept_row_get_column_names(concept_row: *const ConceptRow) -> *mut StringIterator {
    release(StringIterator(CIterator(box_stream(borrow(concept_row).get_column_names().iter().cloned().map(Ok)))))
}

/// Retrieve the executed query's type of the <code>ConceptRow</code>'s header.
#[no_mangle]
pub extern "C" fn concept_row_get_query_type(concept_row: *const ConceptRow) -> QueryType {
    borrow(concept_row).get_query_type()
}

/// Produces an <code>Iterator</code> over all <code>Concepts</code> in this <code>ConceptRow</code>.
#[no_mangle]
pub extern "C" fn concept_row_get_concepts(concept_row: *const ConceptRow) -> *mut ConceptIterator {
    release(ConceptIterator(CIterator(box_stream(borrow(concept_row).get_concepts().cloned().map(Ok)))))
}

/// Retrieves a concept for a given column name.
///
#[no_mangle]
pub extern "C" fn concept_row_get(concept_row: *const ConceptRow, column_name: *const c_char) -> *mut Concept {
    release_optional(borrow(concept_row).get(string_view(column_name)).cloned())
}

/// Retrieves a concept for a given column index.
///
#[no_mangle]
pub extern "C" fn concept_row_get_index(concept_row: *const ConceptRow, column_index: usize) -> *mut Concept {
    release_optional(borrow(concept_row).get_index(column_index).cloned())
}

/// Checks whether the provided <code>ConceptRow</code> objects are equal
#[no_mangle]
pub extern "C" fn concept_row_equals(lhs: *const ConceptRow, rhs: *const ConceptRow) -> bool {
    borrow(lhs) == borrow(rhs)
}

/// A string representation of this ConceptRow.
#[no_mangle]
pub extern "C" fn concept_row_to_string(concept_row: *const ConceptRow) -> *mut c_char {
    release_string(format!("{:?}", borrow(concept_row)))
}

/// Frees the native rust <code>ValueGroup</code> object
#[no_mangle]
pub extern "C" fn value_group_drop(value_group: *mut ValueGroup) {
    free(value_group);
}

/// A string representation of this <code>ValueGroup</code> object
#[no_mangle]
pub extern "C" fn value_group_to_string(value_group: *const ValueGroup) -> *mut c_char {
    release_string(format!("{:?}", borrow(value_group)))
}

/// Checks whether the provided <code>ValueGroup</code> objects are equal
#[no_mangle]
pub extern "C" fn value_group_equals(lhs: *const ValueGroup, rhs: *const ValueGroup) -> bool {
    borrow(lhs) == borrow(rhs)
}

/// Retrieves the concept that is the group owner.
#[no_mangle]
pub extern "C" fn value_group_get_owner(value_group: *mut ValueGroup) -> *mut Concept {
    release(borrow(value_group).owner.clone())
}

/// Retrieves the <code>Value</code> answer of the group.
#[no_mangle]
pub extern "C" fn value_group_get_value(value_group: *mut ValueGroup) -> *mut Concept {
    release_optional(borrow(value_group).value.clone().map(Concept::Value))
}
