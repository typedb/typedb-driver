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
    box_stream,
    concept::{AttributeType, Concept, EntityType, RelationType, SchemaException, ValueType},
    Promise, Result, Transaction, IID,
};

use super::ConceptPromise;
use crate::{
    error::try_release,
    iterator::{iterator_try_next, CIterator},
    memory::{borrow, free, release, release_string, string_view},
};

#[no_mangle]
pub extern "C" fn concepts_get_root_entity_type() -> *mut Concept {
    release(Concept::EntityType(EntityType::root()))
}

#[no_mangle]
pub extern "C" fn concepts_get_root_relation_type() -> *mut Concept {
    release(Concept::RelationType(RelationType::root()))
}

#[no_mangle]
pub extern "C" fn concepts_get_root_attribute_type() -> *mut Concept {
    release(Concept::AttributeType(AttributeType::root()))
}

#[no_mangle]
pub extern "C" fn concepts_get_entity_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::entity_type(borrow(transaction).concept().get_entity_type(string_view(label).to_owned())))
}

#[no_mangle]
pub extern "C" fn concepts_get_relation_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::relation_type(
        borrow(transaction).concept().get_relation_type(string_view(label).to_owned()),
    ))
}

#[no_mangle]
pub extern "C" fn concepts_get_attribute_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::attribute_type(
        borrow(transaction).concept().get_attribute_type(string_view(label).to_owned()),
    ))
}

#[no_mangle]
pub extern "C" fn concepts_put_entity_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
) -> *mut ConceptPromise {
    let promise = borrow(transaction).concept().put_entity_type(string_view(label).to_owned());
    release(ConceptPromise::entity_type(|| promise.resolve().map(Some)))
}

#[no_mangle]
pub extern "C" fn concepts_put_relation_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
) -> *mut ConceptPromise {
    let promise = borrow(transaction).concept().put_relation_type(string_view(label).to_owned());
    release(ConceptPromise::relation_type(|| promise.resolve().map(Some)))
}

#[no_mangle]
pub extern "C" fn concepts_put_attribute_type(
    transaction: *const Transaction<'static>,
    label: *const c_char,
    value_type: ValueType,
) -> *mut ConceptPromise {
    let promise = borrow(transaction).concept().put_attribute_type(string_view(label).to_owned(), value_type);
    release(ConceptPromise::attribute_type(|| promise.resolve().map(Some)))
}

fn iid_from_str(str: &str) -> IID {
    (2..str.len()).step_by(2).map(|i| u8::from_str_radix(&str[i..i + 2], 16).unwrap()).collect::<Vec<u8>>().into()
}

#[no_mangle]
pub extern "C" fn concepts_get_entity(
    transaction: *const Transaction<'static>,
    iid: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::entity(borrow(transaction).concept().get_entity(iid_from_str(string_view(iid)))))
}

#[no_mangle]
pub extern "C" fn concepts_get_relation(
    transaction: *const Transaction<'static>,
    iid: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::relation(borrow(transaction).concept().get_relation(iid_from_str(string_view(iid)))))
}

#[no_mangle]
pub extern "C" fn concepts_get_attribute(
    transaction: *const Transaction<'static>,
    iid: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::attribute(borrow(transaction).concept().get_attribute(iid_from_str(string_view(iid)))))
}

pub struct SchemaExceptionIterator(CIterator<Result<SchemaException>>);

#[no_mangle]
pub extern "C" fn schema_exception_iterator_next(it: *mut SchemaExceptionIterator) -> *mut SchemaException {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn schema_exception_iterator_drop(it: *mut SchemaExceptionIterator) {
    free(it);
}

#[no_mangle]
pub extern "C" fn schema_exception_drop(schema_exception: *mut SchemaException) {
    free(schema_exception);
}

#[no_mangle]
pub extern "C" fn schema_exception_code(schema_exception: *const SchemaException) -> *mut c_char {
    unsafe { release_string((*schema_exception).code.clone()) }
}

#[no_mangle]
pub extern "C" fn schema_exception_message(schema_exception: *const SchemaException) -> *mut c_char {
    unsafe { release_string((*schema_exception).message.clone()) }
}

#[no_mangle]
pub extern "C" fn concepts_get_schema_exceptions(
    transaction: *const Transaction<'static>,
) -> *mut SchemaExceptionIterator {
    try_release(
        borrow(transaction)
            .concept()
            .get_schema_exceptions()
            .map(|e| SchemaExceptionIterator(CIterator(box_stream(e)))),
    )
}
