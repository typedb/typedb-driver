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

use std::ffi::c_char;

use chrono::NaiveDateTime;

use crate::{
    bindings::memory::{borrow, borrow_mut, free, release, release_string, string_view},
    concept::{
        Annotation, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value,
    },
    transaction::concept::api::{ThingAPI, ThingTypeAPI},
};

#[no_mangle]
pub extern "C" fn value_new_boolean(bool: bool) -> *mut Value {
    release(Value::Boolean(bool))
}

#[no_mangle]
pub extern "C" fn value_new_long(long: i64) -> *mut Value {
    release(Value::Long(long))
}

#[no_mangle]
pub extern "C" fn value_new_double(double: f64) -> *mut Value {
    release(Value::Double(double))
}

#[no_mangle]
pub extern "C" fn value_new_string(string: *const c_char) -> *mut Value {
    release(Value::String(string_view(string).to_owned()))
}

#[no_mangle]
pub extern "C" fn value_new_date_time_from_millis(millis: i64) -> *mut Value {
    release(Value::DateTime(NaiveDateTime::from_timestamp_millis(millis).unwrap()))
}

#[no_mangle]
pub extern "C" fn value_drop(value: *mut Value) {
    free(value);
}

#[no_mangle]
pub extern "C" fn value_is_boolean(value: *const Value) -> bool {
    matches!(borrow(value), Value::Boolean(_))
}

#[no_mangle]
pub extern "C" fn value_is_long(value: *const Value) -> bool {
    matches!(borrow(value), Value::Long(_))
}

#[no_mangle]
pub extern "C" fn value_is_double(value: *const Value) -> bool {
    matches!(borrow(value), Value::Double(_))
}

#[no_mangle]
pub extern "C" fn value_is_string(value: *const Value) -> bool {
    matches!(borrow(value), Value::String(_))
}

#[no_mangle]
pub extern "C" fn value_is_date_time(value: *const Value) -> bool {
    matches!(borrow(value), Value::DateTime(_))
}

#[no_mangle]
pub extern "C" fn value_get_boolean(value: *const Value) -> bool {
    if let Value::Boolean(bool) = borrow(value) {
        *bool
    } else {
        unreachable!()
    }
}

#[no_mangle]
pub extern "C" fn value_get_long(value: *const Value) -> i64 {
    if let Value::Long(long) = borrow(value) {
        *long
    } else {
        unreachable!()
    }
}

#[no_mangle]
pub extern "C" fn value_get_double(value: *const Value) -> f64 {
    if let Value::Double(double) = borrow(value) {
        *double
    } else {
        unreachable!()
    }
}

#[no_mangle]
pub extern "C" fn value_get_string(value: *const Value) -> *mut c_char {
    if let Value::String(string) = borrow(value) {
        release_string(string.clone())
    } else {
        unreachable!()
    }
}

#[no_mangle]
pub extern "C" fn value_get_date_time_as_millis(value: *const Value) -> i64 {
    if let Value::DateTime(date_time) = borrow(value) {
        date_time.timestamp_millis()
    } else {
        unreachable!()
    }
}

#[no_mangle]
pub extern "C" fn value_equals(lhs: *const Value, rhs: *const Value) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn annotation_new_key() -> *mut Annotation {
    release(Annotation::Key)
}

#[no_mangle]
pub extern "C" fn annotation_new_unique() -> *mut Annotation {
    release(Annotation::Unique)
}

#[no_mangle]
pub extern "C" fn annotation_drop(annotation: *mut Annotation) {
    free(annotation);
}

#[no_mangle]
pub extern "C" fn annotation_to_string(annotation: *const Annotation) -> *mut c_char {
    release_string(format!("{:?}", borrow(annotation)))
}

#[no_mangle]
pub extern "C" fn annotation_equals(lhs: *const Annotation, rhs: *const Annotation) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn annotation_is_key(annotation: *const Annotation) -> bool {
    *borrow(annotation) == Annotation::Key
}

#[no_mangle]
pub extern "C" fn annotation_is_unique(annotation: *const Annotation) -> bool {
    *borrow(annotation) == Annotation::Unique
}

#[no_mangle]
pub extern "C" fn concept_equals(lhs: *const Concept, rhs: *const Concept) -> bool {
    borrow(lhs) == borrow(rhs)
}

#[no_mangle]
pub extern "C" fn concept_drop(concept: *mut Concept) {
    free(concept);
}

#[no_mangle]
pub extern "C" fn concept_is_entity(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Entity(_))
}

#[no_mangle]
pub extern "C" fn concept_is_relation(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Relation(_))
}

#[no_mangle]
pub extern "C" fn concept_is_attribute(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Attribute(_))
}

#[no_mangle]
pub extern "C" fn concept_is_root_thing_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RootThingType(_))
}

#[no_mangle]
pub extern "C" fn concept_is_entity_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::EntityType(_))
}

#[no_mangle]
pub extern "C" fn concept_is_relation_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RelationType(_))
}

#[no_mangle]
pub extern "C" fn concept_is_attribute_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::AttributeType(_))
}

#[no_mangle]
pub extern "C" fn concept_is_role_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RoleType(_))
}

#[no_mangle]
pub extern "C" fn concept_to_string(concept: *const Concept) -> *mut c_char {
    release_string(format!("{:?}", borrow(concept)))
}

pub(super) fn borrow_as_thing(concept: *const Concept) -> &'static dyn ThingAPI {
    match borrow(concept) {
        Concept::Entity(entity) => entity,
        Concept::Relation(relation) => relation,
        Concept::Attribute(attribute) => attribute,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_entity(concept: *const Concept) -> &'static Entity {
    match borrow(concept) {
        Concept::Entity(entity) => entity,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_relation(concept: *const Concept) -> &'static Relation {
    match borrow(concept) {
        Concept::Relation(relation) => relation,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_attribute(concept: *const Concept) -> &'static Attribute {
    match borrow(concept) {
        Concept::Attribute(attribute) => attribute,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_thing_type(concept: *const Concept) -> &'static dyn ThingTypeAPI {
    match borrow(concept) {
        Concept::EntityType(entity_type) => entity_type,
        Concept::RelationType(relation_type) => relation_type,
        Concept::AttributeType(attribute_type) => attribute_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_thing_type_mut(concept: *mut Concept) -> &'static mut dyn ThingTypeAPI {
    match borrow_mut(concept) {
        Concept::EntityType(entity_type) => entity_type,
        Concept::RelationType(relation_type) => relation_type,
        Concept::AttributeType(attribute_type) => attribute_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_entity_type(concept: *const Concept) -> &'static EntityType {
    match borrow(concept) {
        Concept::EntityType(entity_type) => entity_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_entity_type_mut(concept: *mut Concept) -> &'static mut EntityType {
    match borrow_mut(concept) {
        Concept::EntityType(entity_type) => entity_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_relation_type(concept: *const Concept) -> &'static RelationType {
    match borrow(concept) {
        Concept::RelationType(relation_type) => relation_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_relation_type_mut(concept: *mut Concept) -> &'static mut RelationType {
    match borrow_mut(concept) {
        Concept::RelationType(relation_type) => relation_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_attribute_type(concept: *const Concept) -> &'static AttributeType {
    match borrow(concept) {
        Concept::AttributeType(attribute_type) => attribute_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_attribute_type_mut(concept: *mut Concept) -> &'static mut AttributeType {
    match borrow_mut(concept) {
        Concept::AttributeType(attribute_type) => attribute_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_role_type(concept: *const Concept) -> &'static RoleType {
    match borrow(concept) {
        Concept::RoleType(role_type) => role_type,
        _ => unreachable!(),
    }
}
