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
    error::{try_release, try_release_optional, unwrap_or_default, unwrap_void},
    iterator::{iterator_try_next, CIterator},
    memory::{borrow, borrow_mut, free, release_string, string_view},
};
use crate::{common::box_stream, logic::Rule, transaction::logic::api::RuleAPI, Result, Transaction};

#[no_mangle]
pub extern "C" fn rule_drop(rule: *mut Rule) {
    free(rule);
}

#[no_mangle]
pub extern "C" fn rule_to_string(rule: *const Rule) -> *mut c_char {
    release_string(format!("{:?}", borrow(rule)))
}

#[no_mangle]
pub extern "C" fn rule_get_label(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).label.clone())
}

#[no_mangle]
pub extern "C" fn rule_get_when(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).when.to_string())
}

#[no_mangle]
pub extern "C" fn rule_get_then(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).then.to_string())
}

#[no_mangle]
pub extern "C" fn rule_set_label(
    transaction: *const crate::Transaction<'static>,
    rule: *mut Rule,
    new_label: *const c_char,
) {
    unwrap_void(borrow_mut(rule).set_label(borrow(transaction), string_view(new_label).to_owned()));
}

#[no_mangle]
pub extern "C" fn rule_delete(transaction: *const crate::Transaction<'static>, rule: *mut Rule) {
    unwrap_void(borrow_mut(rule).delete(borrow(transaction)));
}

#[no_mangle]
pub extern "C" fn rule_is_deleted(transaction: *const crate::Transaction<'static>, rule: *mut Rule) -> bool {
    unwrap_or_default(borrow_mut(rule).is_deleted(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn logic_manager_put_rule(
    transaction: *mut Transaction<'static>,
    label: *const c_char,
    when: *const c_char,
    then: *const c_char,
) -> *mut Rule {
    try_release((|| {
        borrow(transaction).logic().put_rule(
            string_view(label).to_owned(),
            typeql_lang::parse_pattern(string_view(when))?.into_conjunction(),
            typeql_lang::parse_variable(string_view(then))?,
        )
    })())
}

#[no_mangle]
pub extern "C" fn logic_manager_get_rule(transaction: *mut Transaction<'static>, label: *mut c_char) -> *mut Rule {
    try_release_optional(borrow(transaction).logic().get_rule(string_view(label).to_owned()).transpose())
}

pub struct RuleIterator(CIterator<Result<Rule>>);

#[no_mangle]
pub extern "C" fn rule_iterator_next(it: *mut RuleIterator) -> *mut Rule {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn rule_iterator_drop(it: *mut RuleIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn logic_manager_get_rules(transaction: *mut Transaction<'static>) -> *mut RuleIterator {
    try_release(borrow(transaction).logic().get_rules().map(|it| RuleIterator(CIterator(box_stream(it)))))
}
