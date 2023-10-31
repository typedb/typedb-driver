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

use itertools::Itertools;
use typedb_driver::{
    answer::{ConceptMap, ConceptMapGroup, Explainable, ValueGroup},
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

#[no_mangle]
pub extern "C" fn query_define(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().define_with_options(string_view(query), borrow(options).clone()),
    )))
}

#[no_mangle]
pub extern "C" fn query_undefine(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().undefine_with_options(string_view(query), borrow(options).clone()),
    )))
}

#[no_mangle]
pub extern "C" fn query_delete(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow(transaction).query().delete_with_options(string_view(query), borrow(options).clone()),
    )))
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
pub extern "C" fn query_get(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapIterator(CIterator(box_stream(it)))),
    )
}

#[no_mangle]
pub extern "C" fn query_fetch(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut StringIterator {
    try_release(
        borrow(transaction)
            .query()
            .fetch_with_options(string_view(query), borrow(options).clone())
            .map(|it| StringIterator(CIterator(box_stream(it.map_ok(|json| json.to_string()))))),
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
pub extern "C" fn query_get_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptPromise {
    release(ConceptPromise::value(
        borrow(transaction).query().get_aggregate_with_options(string_view(query), borrow(options).clone()),
    ))
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
pub extern "C" fn query_get_group(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ConceptMapGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_group_with_options(string_view(query), borrow(options).clone())
            .map(|it| ConceptMapGroupIterator(CIterator(box_stream(it)))),
    )
}

pub struct ValueGroupIterator(CIterator<Result<ValueGroup>>);

#[no_mangle]
pub extern "C" fn value_group_iterator_next(it: *mut ValueGroupIterator) -> *mut ValueGroup {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn value_group_iterator_drop(it: *mut ValueGroupIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn query_get_group_aggregate(
    transaction: *mut Transaction<'static>,
    query: *const c_char,
    options: *const Options,
) -> *mut ValueGroupIterator {
    try_release(
        borrow(transaction)
            .query()
            .get_group_aggregate_with_options(string_view(query), borrow(options).clone())
            .map(|it| ValueGroupIterator(CIterator(box_stream(it)))),
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
    explainable: *const Explainable,
    options: *const Options,
) -> *mut ExplanationIterator {
    try_release(
        borrow(transaction)
            .query()
            .explain_with_options(borrow(explainable), borrow(options).clone())
            .map(|it| ExplanationIterator(CIterator(box_stream(it)))),
    )
}
