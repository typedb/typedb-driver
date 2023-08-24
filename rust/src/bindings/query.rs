/*
 * Copyright (C) 2022 Vaticle
 *
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

use super::{
    error::{try_release, unwrap_void},
    iterator::{iterator_try_next, CIterator},
    memory::{borrow, free, string_view},
};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
    common::box_stream,
    logic::Explanation,
    Options, Result, Transaction,
};

#[no_mangle]
pub extern "C" fn query_define(transaction: *mut Transaction<'static>, query: *const c_char, options: *const Options) {
    unwrap_void(borrow(transaction).query().define_with_options(string_view(query), borrow(options).clone()))
}

#[no_mangle]
pub extern "C" fn query_undefine(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) {
    unwrap_void(borrow(transaction).query().undefine_with_options(string_view(query), borrow(options).clone()))
}

#[no_mangle]
pub extern "C" fn query_delete(transaction: *mut Transaction<'static>, query: *const c_char, options: *const Options) {
    unwrap_void(borrow(transaction).query().delete_with_options(string_view(query), borrow(options).clone()))
}

pub struct ConceptMapIterator(pub CIterator<Result<ConceptMap>>);

#[no_mangle]
pub extern "C" fn concept_map_iterator_next(it: *mut ConceptMapIterator) -> *mut ConceptMap {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn concept_map_iterator_drop(it: *mut ConceptMapIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn query_match(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .match_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

#[no_mangle]
pub extern "C" fn query_insert(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .insert_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

#[no_mangle]
pub extern "C" fn query_update(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .update_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

#[no_mangle]
pub extern "C" fn query_match_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut Numeric {
    try_release(borrow(transaction).query().match_aggregate_with_options(string_view(query), borrow(options).clone()))
}

pub struct ConceptMapGroupIterator(CIterator<Result<ConceptMapGroup>>);

#[no_mangle]
pub extern "C" fn concept_map_group_iterator_next(it: *mut ConceptMapGroupIterator) -> *mut ConceptMapGroup {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn concept_map_group_iterator_drop(it: *mut ConceptMapGroupIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn query_match_group(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .match_group_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapGroupIterator(CIterator(box_stream(it)))),
    )
}

pub struct NumericGroupIterator(CIterator<Result<NumericGroup>>);

#[no_mangle]
pub extern "C" fn numeric_group_iterator_next(it: *mut NumericGroupIterator) -> *mut NumericGroup {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn numeric_group_iterator_drop(it: *mut NumericGroupIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn query_match_group_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut NumericGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .match_group_aggregate_with_options(string_view(query), borrow(options).clone())
            .map(|it| NumericGroupIterator(CIterator(box_stream(it)))),
    )
}

pub struct ExplanationIterator(CIterator<Result<Explanation>>);

#[no_mangle]
pub extern "C" fn explanation_iterator_next(it: *mut ExplanationIterator) -> *mut Explanation {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn explanation_iterator_drop(it: *mut ExplanationIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn query_explain(
    transaction: *mut Transaction<'static>,
    explainable_id: i64,
    options: *const Options,
) -> *mut ExplanationIterator {
    try_release(
        borrow(transaction)
            .query()
            .explain_with_options(explainable_id, borrow(options).clone())
            .map(|it| ExplanationIterator(CIterator(box_stream(it)))),
    )
}
