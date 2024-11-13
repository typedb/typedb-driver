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

use chrono::{DateTime, NaiveTime, TimeZone as ChronoTimeZone};
use typedb_driver::{
    box_stream,
    concept::{
        value::{Decimal, Duration, TimeZone},
        Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value, ValueType,
    },
};

use crate::{
    iterator::CIterator,
    memory::{borrow, borrow_mut, free, release, release_optional, release_string, string_free},
};

/// A <code>DatetimeInNanos</code> used to represent datetime as a pair of seconds part and
/// a number of nanoseconds since the last seconds boundary.
#[repr(C)]
#[derive(Clone, Copy, Hash, PartialEq, Eq)]
pub struct DatetimeInNanos {
    seconds: i64,
    subsec_nanos: u32,
}

impl DatetimeInNanos {
    pub fn new<TZ: ChronoTimeZone>(datetime: &DateTime<TZ>) -> Self {
        Self { seconds: datetime.timestamp(), subsec_nanos: datetime.timestamp_subsec_nanos() }
    }

    pub fn get_seconds(self) -> i64 {
        self.seconds
    }

    pub fn get_subsec_nanos(self) -> u32 {
        self.subsec_nanos
    }
}

/// A <code>DatetimeAndTimeZone</code> used to represent time zoned datetime in FFI.
/// Time zone can be represented either as an IANA <code>Tz</code> or as a <code>FixedOffset</code>.
/// Either the zone_name (is_fixed_offset == false) or offset (is_fixed_offset == true) is set.
#[repr(C)]
pub struct DatetimeAndTimeZone {
    datetime_in_nanos: DatetimeInNanos,
    zone_name: *mut c_char,
    local_minus_utc_offset: i32,
    is_fixed_offset: bool,
}

impl DatetimeAndTimeZone {
    pub fn new(datetime_tz: &DateTime<TimeZone>) -> Self {
        let (zone_name, offset, is_fixed_offset) = {
            match datetime_tz.timezone() {
                TimeZone::IANA(tz) => (tz.name().to_owned(), 0, false),
                TimeZone::Fixed(offset) => ("".to_owned(), offset.local_minus_utc(), true),
            }
        };
        Self {
            datetime_in_nanos: DatetimeInNanos::new(datetime_tz),
            zone_name: release_string(zone_name),
            local_minus_utc_offset: offset,
            is_fixed_offset,
        }
    }

    pub fn get_datetime_in_nanos(self) -> DatetimeInNanos {
        self.datetime_in_nanos
    }

    pub fn get_zone_name(self) -> *mut c_char {
        self.zone_name
    }

    pub fn get_local_minus_utc_offset(self) -> i32 {
        self.local_minus_utc_offset
    }

    pub fn get_is_fixed_offset(self) -> bool {
        self.is_fixed_offset
    }
}

impl Drop for DatetimeAndTimeZone {
    fn drop(&mut self) {
        string_free(self.zone_name);
    }
}

/// Frees the native rust <code>DatetimeAndTimeZone</code> object
#[no_mangle]
pub extern "C" fn datetime_and_time_zone_drop(datetime_tz: *mut DatetimeAndTimeZone) {
    free(datetime_tz);
}

/// Iterator over the <code>StringAndOptValue</code>s representing struct value {field_name: value?} info.
pub struct StringAndOptValueIterator(pub CIterator<StringAndOptValue>);

/// Forwards the <code>StringAndOptValueIterator</code> and returns the next <code>StringAndOptValue</code> if it exists,
/// or null if there are no more elements.
#[no_mangle]
pub extern "C" fn string_and_opt_value_iterator_next(it: *mut StringAndOptValueIterator) -> *mut StringAndOptValue {
    release_optional(borrow_mut(it).0 .0.next())
}

/// Frees the native rust <code>StringAndOptValueIterator</code> object
#[no_mangle]
pub extern "C" fn string_and_opt_value_iterator_drop(it: *mut StringAndOptValueIterator) {
    free(it);
}

/// A <code>StringAndOptValue</code> used to represent the pair of variables involved in an ownership.
/// <code>_0</code> and <code>_1</code> are the owner and attribute variables respectively.
#[repr(C)]
pub struct StringAndOptValue {
    string: *mut c_char,
    value: *mut Concept,
}

impl From<(String, Option<Value>)> for StringAndOptValue {
    fn from((field_name, value): (String, Option<Value>)) -> Self {
        Self { string: release_string(field_name), value: release_optional(value.map(Concept::Value)) }
    }
}

impl Drop for StringAndOptValue {
    fn drop(&mut self) {
        string_free(self.string);
        concept_drop(self.value);
    }
}

/// Frees the native rust <code>StringAndOptValue</code> object
#[no_mangle]
pub extern "C" fn string_and_opt_value_drop(string_and_opt_value: *mut StringAndOptValue) {
    free(string_and_opt_value);
}

/// Returns <code>true</code> if the attribute type does not have a value type.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_untyped(attribute_type: *const Concept) -> bool {
    borrow_as_attribute_type(attribute_type).value_type.is_none()
}

/// Returns <code>true</code> if the attribute type is of type <code>boolean</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_boolean(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Boolean))
}

/// Returns <code>true</code> if the attribute type is of type <code>long</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_long(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Long))
}

/// Returns <code>true</code> if the attribute type is of type <code>double</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_double(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Double))
}

/// Returns <code>true</code> if the attribute type is of type <code>decimal</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_decimal(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Decimal))
}

/// Returns <code>true</code> if the attribute type is of type <code>string</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_string(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::String))
}

/// Returns <code>true</code> if the attribute type is of type <code>date</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_date(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Date))
}

/// Returns <code>true</code> if the attribute type is of type <code>datetime</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_datetime(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Datetime))
}

/// Returns <code>true</code> if the attribute type is of type <code>datetime-tz</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_datetime_tz(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::DatetimeTZ))
}

/// Returns <code>true</code> if the attribute type is of type <code>duration</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_duration(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Duration))
}

/// Returns <code>true</code> if the attribute type is of type <code>struct</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn attribute_type_is_struct(attribute_type: *const Concept) -> bool {
    matches!(borrow_as_attribute_type(attribute_type).value_type, Some(ValueType::Struct(_)))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>boolean</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_boolean(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Boolean(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>long</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_long(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Long(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>double</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_double(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Double(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>decimal</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_decimal(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Decimal(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>string</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_string(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::String(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>date</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_date(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Date(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>datetime</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_datetime(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Datetime(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>datetime-tz</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_datetime_tz(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::DatetimeTZ(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>duration</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_duration(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Duration(_))
}

/// Returns <code>true</code> if the value which this <code>Value</code> concept holds is of type <code>struct</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn value_is_struct(value: *const Concept) -> bool {
    matches!(borrow_as_value(value), Value::Struct(_, _))
}

/// Returns a <code>boolean</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_boolean(value: *const Concept) -> bool {
    if let &Value::Boolean(bool) = borrow_as_value(value) {
        bool
    } else {
        unreachable!("Attempting to unwrap a non-boolean {:?} as boolean", borrow_as_value(value))
    }
}

/// Returns the <code>long</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_long(value: *const Concept) -> i64 {
    if let &Value::Long(long) = borrow_as_value(value) {
        long
    } else {
        unreachable!("Attempting to unwrap a non-long {:?} as long", borrow_as_value(value))
    }
}

/// Returns the <code>double</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_double(value: *const Concept) -> f64 {
    if let &Value::Double(double) = borrow_as_value(value) {
        double
    } else {
        unreachable!("Attempting to unwrap a non-double {:?} as double", borrow_as_value(value))
    }
}

/// Returns the <code>decimal</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_decimal(value: *const Concept) -> Decimal {
    if let &Value::Decimal(decimal) = borrow_as_value(value) {
        decimal
    } else {
        unreachable!("Attempting to unwrap a non-decimal {:?} as decimal", borrow_as_value(value))
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

/// Returns the value of this date value concept as seconds since the start of the UNIX epoch.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_date_as_seconds(value: *const Concept) -> i64 {
    if let Value::Date(date) = borrow_as_value(value) {
        date.and_time(NaiveTime::MIN).and_utc().timestamp()
    } else {
        unreachable!("Attempting to unwrap a non-date {:?} as date", borrow_as_value(value))
    }
}

/// Returns the value of this datetime value concept as seconds and nanoseconds parts since the start of the UNIX epoch.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_datetime(value: *const Concept) -> DatetimeInNanos {
    if let Value::Datetime(date_time) = borrow_as_value(value) {
        DatetimeInNanos::new(&date_time.and_utc())
    } else {
        unreachable!("Attempting to unwrap a non-datetime {:?} as datetime", borrow_as_value(value))
    }
}

/// Returns the value of this datetime-tz value concept as seconds and nanoseconds parts since the start of the UNIX epoch and an IANA TZ name.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_datetime_tz(value: *const Concept) -> DatetimeAndTimeZone {
    if let Value::DatetimeTZ(datetime_tz) = borrow_as_value(value) {
        DatetimeAndTimeZone::new(datetime_tz)
    } else {
        unreachable!("Attempting to unwrap a non-datetime-tz {:?} as datetime-tz", borrow_as_value(value))
    }
}

/// Returns the value of this duration value.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_duration(value: *const Concept) -> Duration {
    if let &Value::Duration(duration) = borrow_as_value(value) {
        duration
    } else {
        unreachable!("Attempting to unwrap a non-duration {:?} as duration", borrow_as_value(value))
    }
}

/// Returns the value of this struct value concept represented as an iterator of {field_name: value?} pairs.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn value_get_struct(value: *const Concept) -> *mut StringAndOptValueIterator {
    if let Value::Struct(struct_value, _) = borrow_as_value(value) {
        release(StringAndOptValueIterator(CIterator(box_stream(
            struct_value.fields().clone().into_iter().map(|pair| pair.into()),
        ))))
    } else {
        unreachable!("Attempting to unwrap a non-duration {:?} as duration", borrow_as_value(value))
    }
}

/// Gets the string representation of the value type of this attribute type.
#[no_mangle]
pub extern "C" fn value_get_value_type(value: *const Concept) -> *mut c_char {
    release_string(borrow_as_value(value).get_type().name().to_owned())
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

/// Checks if the concept is an <code>Entity</code>.
#[no_mangle]
pub extern "C" fn concept_is_entity(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Entity(_))
}

/// Checks if the concept is a <code>Relation</code>.
#[no_mangle]
pub extern "C" fn concept_is_relation(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Relation(_))
}

/// Checks if the concept is an <code>Attribute</code>.
#[no_mangle]
pub extern "C" fn concept_is_attribute(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Attribute(_))
}

/// Checks if the concept is a <code>Value</code>.
#[no_mangle]
pub extern "C" fn concept_is_value(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::Value(_))
}

/// Checks if the concept is an <code>EntityType</code>.
#[no_mangle]
pub extern "C" fn concept_is_entity_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::EntityType(_))
}

/// Checks if the concept is a <code>RelationType</code>.
#[no_mangle]
pub extern "C" fn concept_is_relation_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RelationType(_))
}

/// Checks if the concept is an <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn concept_is_attribute_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::AttributeType(_))
}

/// Checks if the concept is a <code>RoleType</code>.
#[no_mangle]
pub extern "C" fn concept_is_role_type(concept: *const Concept) -> bool {
    matches!(borrow(concept), Concept::RoleType(_))
}

/// Gets the 'label' of this <code>Concept</code> object.
#[no_mangle]
pub extern "C" fn concept_get_label(concept: *const Concept) -> *mut c_char {
    release_string(borrow(concept).get_label().to_owned())
}

/// A string representation of this <code>Concept</code> object.
#[no_mangle]
pub extern "C" fn concept_to_string(concept: *const Concept) -> *mut c_char {
    release_string(format!("{:?}", borrow(concept)))
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
