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

use std::{ffi::c_char, ptr::addr_of_mut};

use itertools::Itertools;
use typedb_driver::{
    answer::{ConceptRow, ConceptMapGroup, Explainable, ValueGroup},
    box_stream,
    logic::Explanation,
    Options, Result, Transaction,
};

use super::{
    error::try_release,
    iterator::{iterator_try_next, CIterator},
    memory::{borrow, free, string_view},
};
use crate::{common::StringIterator, concept::ConceptPromise, memory::release, promise::VoidPromise};

/// Performs a TypeQL Define query in the transaction.
#[no_mangle]
pub extern "C" fn query_define(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().define_with_options(string_view(query), *borrow(options)),
    )))
}

/// Performs a TypeQL Undefine query in the transaction.
#[no_mangle]
pub extern "C" fn query_undefine(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().undefine_with_options(string_view(query), *borrow(options)),
    )))
}

/// Performs a TypeQL Delete query in the transaction.
#[no_mangle]
pub extern "C" fn query_delete(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().delete_with_options(string_view(query), *borrow(options)),
    )))
}

/// Iterator over the <code>ConceptMap</code>s in the result of a TypeQL Get query.
pub struct ConceptMapIterator(pub CIterator<Result<ConceptRow>>);

/// Forwards the <code>ConceptMapIterator</code> and returns the next <code>ConceptMap</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn concept_map_iterator_next(it: *mut ConceptMapIterator) -> *mut ConceptRow {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ConceptMapIterator</code> object
#[no_mangle]
pub extern "C" fn concept_map_iterator_drop(it: *mut ConceptMapIterator) {
    free(it);
}

/// Performs a TypeQL Get (Get) query in the transaction.
#[no_mangle]
pub extern "C" fn query_get(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_with_options(string_view(query), *borrow(options))
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

/// Performs a TypeQL Fetch query in the transaction.
#[no_mangle]
pub extern "C" fn query_fetch(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut StringIterator {
    try_release(
        borrow(transaction)
            .query()
            .fetch_with_options(string_view(query), *borrow(options))
            .map(|it| StringIterator(CIterator(box_stream(it.map_ok(|json| json.to_string()))))),
    )
}

/// Performs a TypeQL Insert query in the transaction.
#[no_mangle]
pub extern "C" fn query_insert(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .insert_with_options(string_view(query), *borrow(options))
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

/// Performs a TypeQL Update query in the transaction.
#[no_mangle]
pub extern "C" fn query_update(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .update_with_options(string_view(query), *borrow(options))
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

/// Performs a TypeQL Get Aggregate query in the transaction.
#[no_mangle]
pub extern "C" fn query_get_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptPromise {
    release(ConceptPromise::value(
        borrow(transaction).query().get_aggregate_with_options(string_view(query), *borrow(options)),
    ))
}

/// Iterator over the <code>ConceptMapGroup</code>s in the result of the TypeQL Get Group query.
pub struct ConceptMapGroupIterator(CIterator<Result<ConceptMapGroup>>);

/// Forwards the <code>ConceptMapGroupIterator</code> and returns the next <code>ConceptMapGroup</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn concept_map_group_iterator_next(it: *mut ConceptMapGroupIterator) -> *mut ConceptMapGroup {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ConceptMapGroupIterator</code> object
#[no_mangle]
pub extern "C" fn concept_map_group_iterator_drop(it: *mut ConceptMapGroupIterator) {
    free(it);
}

/// Performs a TypeQL Get Group query in the transaction.
#[no_mangle]
pub extern "C" fn query_get_group(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_group_with_options(string_view(query), *borrow(options))
            .map(|it| ConceptMapGroupIterator(CIterator(box_stream(it)))),
    )
}

/// Iterator over the <code>ValueGroup</code>s in the result of the Get Group Aggregate query.
pub struct ValueGroupIterator(CIterator<Result<ValueGroup>>);

/// Forwards the <code>ValueGroupIterator</code> and returns the next <code>ValueGroup</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn value_group_iterator_next(it: *mut ValueGroupIterator) -> *mut ValueGroup {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ValueGroupIterator</code> object
#[no_mangle]
pub extern "C" fn value_group_iterator_drop(it: *mut ValueGroupIterator) {
    free(it);
}

/// Performs a TypeQL Get Group Aggregate query in the transaction.
#[no_mangle]
pub extern "C" fn query_get_group_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ValueGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_group_aggregate_with_options(string_view(query), *borrow(options))
            .map(|it| ValueGroupIterator(CIterator(box_stream(it)))),
    )
}

/// Iterator over the <code>Explanation</code>s in the result of the explain query.
pub struct ExplanationIterator(CIterator<Result<Explanation>>);

/// Forwards the <code>Explanation</code> and returns the next <code>Explanation</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn explanation_iterator_next(it: *mut ExplanationIterator) -> *mut Explanation {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>ExplanationIterator</code> object
#[no_mangle]
pub extern "C" fn explanation_iterator_drop(it: *mut ExplanationIterator) {
    free(it);
}

/// Performs a TypeQL Explain query in the transaction.
/// @param explainable The Explainable to be explained
///  @param options Specify query options
#[no_mangle]
pub extern "C" fn query_explain(
    transaction: *mut Transaction<'static>,
    explainable: *const Explainable,
    options: *const Options,
) -> *mut ExplanationIterator {
    try_release(
        borrow(transaction)
            .query()
            .explain_with_options(borrow(explainable), *borrow(options))
            .map(|it| ExplanationIterator(CIterator(box_stream(it)))),
    )
}
