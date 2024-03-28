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

use chrono::NaiveDateTime;
use typedb_driver::{
    concept::{
        Annotation, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value,
    },
    transaction::concept::api::{ThingAPI, ThingTypeAPI},
};

use crate::memory::{borrow, borrow_mut, free, release, release_string, string_view};

/// Creates a new ``Value`` object of the specified boolean value.
#[no_mangle]
pub extern "C" fn value_new_boolean(bool: bool) -> *mut Concept {
    release(Concept::Value(Value::Boolean(bool)))
}

/// Creates a new ``Value`` object of the specified long value.
#[no_mangle]
pub extern "C" fn value_new_long(long: i64) -> *mut Concept {
    release(Concept::Value(Value::Long(long)))
}

/// Creates a new ``Value`` object of the specified double value.
#[no_mangle]
pub extern "C" fn value_new_double(double: f64) -> *mut Concept {
    release(Concept::Value(Value::Double(double)))
}

/// Creates a new ``Value`` object of the specified string value.
#[no_mangle]
pub extern "C" fn value_new_string(string: *const c_char) -> *mut Concept {
    release(Concept::Value(Value::String(string_view(string).to_owned())))
}

/// Creates a new ``Value`` object of the specified datetime value.
#[no_mangle]
pub extern "C" fn value_new_date_time_from_millis(millis: i64) -> *mut Concept {
    release(Concept::Value(Value::DateTime(NaiveDateTime::from_timestamp_millis(millis).unwrap())))
}

/// Returns <code>true</code> if the value which this ``Value`` concept holds is of type <code>boolean</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_boolean(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Boolean(_))
}

/// Returns <code>true</code> if the value which this ``Value`` concept holds is of type <code>long</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_long(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Long(_))
}

/// Returns <code>true</code> if the value which this ``Value`` concept holds is of type <code>double</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_double(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Double(_))
}

/// Returns <code>true</code> if the value which this ``Value`` concept holds is of type <code>string</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_string(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::String(_))
}

/// Returns <code>true</code> if the value which this ``Value`` concept holds is of type <code>datetime</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_date_time(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::DateTime(_))
}

/// Returns a <code>boolean</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_boolean(value: *const Concept) -> bool {
    if let Value::Boolean(bool) = borrow_as_value(value) {
        *bool
    } else {
        unreachable!("Attempting to unwrap a non-boolean {:?} as boolean", borrow_as_value(value))
    }
}

/// Returns the <code>long</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_long(value: *const Concept) -> i64 {
    if let Value::Long(long) = borrow_as_value(value) {
        *long
    } else {
        unreachable!("Attempting to unwrap a non-long {:?} as long", borrow_as_value(value))
    }
}

/// Returns the <code>double</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_double(value: *const Concept) -> f64 {
    if let Value::Double(double) = borrow_as_value(value) {
        *double
    } else {
        unreachable!("Attempting to unwrap a non-double {:?} as double", borrow_as_value(value))
    }
}

/// Returns the <code>string</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_string(value: *const Concept) -> *mut c_char {
    if let Value::String(string) = borrow_as_value(value) {
        release_string(string.clone())
    } else {
        unreachable!("Attempting to unwrap a non-string {:?} as string", borrow_as_value(value))
    }
}

/// Returns the value of this datetime value concept as milliseconds since the start of the UNIX epoch.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_date_time_as_millis(value: *const Concept) -> i64 {
    if let Value::DateTime(date_time) = borrow_as_value(value) {
        date_time.timestamp_millis()
    } else {
        unreachable!("Attempting to unwrap a non-datetime {:?} as datetime", borrow_as_value(value))
    }
}

/// Produces a ``@key`` annotation.
#[no_mangle]
pub extern "C" fn annotation_new_key() -> *mut Annotation {
    release(Annotation::Key)
}

/// Produces a ``@unique`` annotation.
#[no_mangle]
pub extern "C" fn annotation_new_unique() -> *mut Annotation {
    release(Annotation::Unique)
}

/// Frees the native rust <code>Annotation</code> object
#[no_mangle]
pub extern "C" fn annotation_drop(annotation: *mut Annotation) {
    free(annotation);
}

/// A string representation of this <code>Annotation</code> object
#[no_mangle]
pub extern "C" fn annotation_to_string(annotation: *const Annotation) -> *mut c_char {
    release_string(format!("{:?}", borrow(annotation)))
}

/// Checks whether the provided <code>Annotation</code> objects are equal
#[no_mangle]
pub extern "C" fn annotation_equals(lhs: *const Annotation, rhs: *const Annotation) -> bool {
    borrow(lhs) == borrow(rhs)
}

/// Checks whether the provided <code>Annotation</code> is ``@key``
#[no_mangle]
pub extern "C" fn annotation_is_key(annotation: *const Annotation) -> bool {
    *borrow(annotation) == Annotation::Key
}

/// Checks whether the provided <code>Annotation</code> is ``@unique``
#[no_mangle]
pub extern "C" fn annotation_is_unique(annotation: *const Annotation) -> bool {
    *borrow(annotation) == Annotation::Unique
}

/// Checks whether the provided <code>Concept</code> objects are equal
#[no_mangle]
pub extern "C" fn concept_equals(lhs: *const Concept, rhs: *const Concept) -> bool {
    borrow(lhs) == borrow(rhs)
}

/// Frees the native rust <code>Concept</code> object
#[no_mangle]
pub extern "C" fn concept_drop(concept: *mut Concept) {
    free(concept);
}

/// Checks if the concept is an ``Entity``.
#[no_mangle]
pub extern "C" fn concept_is_entity(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Entity(_))
}

/// Checks if the concept is a ``Relation``.
#[no_mangle]
pub extern "C" fn concept_is_relation(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Relation(_))
}

/// Checks if the concept is an ``Attribute``.
#[no_mangle]
pub extern "C" fn concept_is_attribute(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Attribute(_))
}

/// Checks if the concept is a ``Value``.
#[no_mangle]
pub extern "C" fn concept_is_value(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Value(_))
}

/// Checks if the concept is the root ``thing`` type.
#[no_mangle]
pub extern "C" fn concept_is_root_thing_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RootThingType(_))
}

/// Checks if the concept is an ``EntityType``.
#[no_mangle]
pub extern "C" fn concept_is_entity_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::EntityType(_))
}

/// Checks if the concept is a ``RelationType``.
#[no_mangle]
pub extern "C" fn concept_is_relation_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RelationType(_))
}

/// Checks if the concept is an ``AttributeType``.
#[no_mangle]
pub extern "C" fn concept_is_attribute_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::AttributeType(_))
}

/// Checks if the concept is a ``RoleType``.
#[no_mangle]
pub extern "C" fn concept_is_role_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RoleType(_))
}

/// A string representation of this <code>Concept</code> object
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

pub(super) fn borrow_as_value(concept: *const Concept) -> &'static Value {
    match borrow(concept) {
        Concept::Value(value) => value,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_thing_type(concept: *const Concept) -> &'static dyn ThingTypeAPI {
    match borrow(concept) {
        Concept::EntityType(entity_type) => entity_type,
        Concept::RelationType(relation_type) => relation_type,
        Concept::AttributeType(attribute_type) => attribute_type,
        Concept::RootThingType(root_thing_type) => root_thing_type,
        _ => unreachable!(),
    }
}

pub(super) fn borrow_as_thing_type_mut(concept: *mut Concept) -> &'static mut dyn ThingTypeAPI {
    match borrow_mut(concept) {
        Concept::EntityType(entity_type) => entity_type,
        Concept::RelationType(relation_type) => relation_type,
        Concept::AttributeType(attribute_type) => attribute_type,
        Concept::RootThingType(root_thing_type) => root_thing_type,
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
