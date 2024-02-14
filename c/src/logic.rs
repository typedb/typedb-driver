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

use typedb_driver::{
    box_stream, logic::Rule, transaction::logic::api::RuleAPI, BoxPromise, Error, Promise, Result, Transaction,
};

use super::{
    error::{try_release, try_release_optional},
    iterator::{iterator_try_next, CIterator},
    memory::{borrow, borrow_mut, free, release_string, string_view},
};
use crate::{
    memory::{release, take_ownership},
    promise::{BoolPromise, VoidPromise},
};

/// Frees the native rust <code>ReplicaInfoIterator</code> object.
#[no_mangle]
pub extern "C" fn rule_drop(rule: *mut Rule) {
    free(rule);
}

/// A string representation of this <code>Rule</code> object
#[no_mangle]
pub extern "C" fn rule_to_string(rule: *const Rule) -> *mut c_char {
    release_string(format!("{:?}", borrow(rule)))
}

/// Retrieves the unique label of the rule.
#[no_mangle]
pub extern "C" fn rule_get_label(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).label.clone())
}

/// The statements that constitute the ‘when’ of the rule.
#[no_mangle]
pub extern "C" fn rule_get_when(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).when.to_string())
}

/// The single statement that constitutes the ‘then’ of the rule.
#[no_mangle]
pub extern "C" fn rule_get_then(rule: *const Rule) -> *mut c_char {
    release_string(borrow(rule).then.to_string())
}


/// Renames the label of the rule. The new label must remain unique.
#[no_mangle]
pub extern "C" fn rule_set_label(
    transaction: *const Transaction<'static>,
    rule: *mut Rule,
    new_label: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_mut(rule).set_label(borrow(transaction), string_view(new_label).to_owned()))))
}

/// Deletes this rule.
#[no_mangle]
pub extern "C" fn rule_delete(transaction: *const Transaction<'static>, rule: *mut Rule) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_mut(rule).delete(borrow(transaction)))))
}

/// Check if this rule has been deleted.
#[no_mangle]
pub extern "C" fn rule_is_deleted(transaction: *const Transaction<'static>, rule: *mut Rule) -> *mut BoolPromise {
    release(BoolPromise(Box::new(borrow_mut(rule).is_deleted(borrow(transaction)))))
}

/// Promise object representing the result of an asynchronous operation.
/// Use \ref rule_promise_resolve(RulePromise*) to wait for and retrieve the resulting <code>Rule</code>.
pub struct RulePromise(BoxPromise<'static, Result<Option<Rule>>>);

/// Waits for and returns the result of the operation represented by the <code>RulePromise</code> object.
/// In case the operation failed, the error flag will only be set when the promise is resolved.
/// The native promise object is freed when it is resolved.
#[no_mangle]
pub extern "C" fn rule_promise_resolve(promise: *mut RulePromise) -> *mut Rule {
    try_release_optional(take_ownership(promise).0.resolve().transpose())
}

/// Creates a new Rule if none exists with the given label, or replaces the existing one.
///
/// @param label The label of the Rule to create or replace
//  @param when The when body of the rule to create
//  @param then The then body of the rule to create
#[no_mangle]
pub extern "C" fn logic_manager_put_rule(
    transaction: *mut Transaction<'static>,
    label: *const c_char,
    when: *const c_char,
    then: *const c_char,
) -> *mut RulePromise {
    let promise = (move || {
        Ok::<_, Error>(borrow(transaction).logic().put_rule(
            string_view(label).to_owned(),
            typeql::parse_pattern(string_view(when))?.into_conjunction(),
            typeql::parse_statement(string_view(then))?,
        ))
    })();
    release(RulePromise(Box::new(|| promise?.resolve().map(Some))))
}

/// Retrieves the Rule that has the given label.
#[no_mangle]
pub extern "C" fn logic_manager_get_rule(
    transaction: *mut Transaction<'static>,
    label: *const c_char,
) -> *mut RulePromise {
    release(RulePromise(Box::new(borrow(transaction).logic().get_rule(string_view(label).to_owned()))))
}

/// An iterator over <code>Rule</code>s in the database
pub struct RuleIterator(CIterator<Result<Rule>>);

/// Forwards the <code>RuleIterator</code> and returns the next <code>Rule</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn rule_iterator_next(it: *mut RuleIterator) -> *mut Rule {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

/// Frees the native rust <code>RuleIterator</code> object.
#[no_mangle]
pub extern "C" fn rule_iterator_drop(it: *mut RuleIterator) {
    free(it);
}

/// Returns a <code>RuleIterator</code> over all rules in the database for the transaction.
#[no_mangle]
pub extern "C" fn logic_manager_get_rules(transaction: *mut Transaction<'static>) -> *mut RuleIterator {
    try_release(borrow(transaction).logic().get_rules().map(|it| RuleIterator(CIterator(box_stream(it)))))
}
