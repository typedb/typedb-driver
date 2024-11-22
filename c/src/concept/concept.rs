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
        Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value,
    },
};

use crate::{
    iterator::CIterator,
    memory::{
        borrow, borrow_mut, free, release, release_optional, release_optional_string, release_string, string_free,
    },
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

/// Retrieves the unique id (IID) of this <code>Concept</code>.
/// If this is an Entity or Relation Instance, returns the IID of the instance.
/// Otherwise, returns null.
#[no_mangle]
pub extern "C" fn concept_try_get_iid(thing: *mut Concept) -> *mut c_char {
    release_optional_string(borrow(thing).try_get_iid().map(|iid| iid.to_string()))
}

/// Retrieves the label of this <code>Concept</code>.
/// If this is an <code>Instance</code>, returns the label of the type of this instance ("unknown" if type fetching is disabled).
/// If this is a <code>Value</code>, returns the label of the value type of the value.
/// If this is a <code>Type</code>, returns the label of the type.
#[no_mangle]
pub extern "C" fn concept_get_label(concept: *const Concept) -> *mut c_char {
    release_string(borrow(concept).get_label().clone().to_owned())
}

/// Retrieves the optional label of this <code>Concept</code>.
/// If this is an <code>Instance</code>, returns the label of the type of this instance (None if type fetching is disabled).
/// If this is a <code>Value</code>, returns the label of the value type of the value.
/// If this is a <code>Type</code>, returns the label of the type.
#[no_mangle]
pub extern "C" fn concept_try_get_label(concept: *const Concept) -> *mut c_char {
    release_optional_string(borrow(concept).try_get_label().map(|str| str.clone().to_owned()))
}

/// Retrieves the value type of this <code>Concept</code>, if it exists.
/// If this is an <code>Attribute</code> instance, returns the value type of this instance.
/// If this is a <code>Value</code>, returns its value type.
/// If this is an <code>AttributeType</code>, returns the value type that the schema permits for the attribute type, if one is defined.
/// Otherwise, returns null.
#[no_mangle]
pub extern "C" fn concept_try_get_value_type(concept: *const Concept) -> *mut c_char {
    release_optional_string(borrow(concept).try_get_value_label().map(|str| str.clone().to_owned()))
}

/// Retrieves the value of this <code>Concept</code>, if it exists.
/// If this is an <code>Attribute</code> instance, returns the value of this instance.
/// If this a <code>Value</code>, returns the value.
/// Otherwise, returns null.
#[no_mangle]
pub extern "C" fn concept_try_get_value(concept: *const Concept) -> *mut Concept {
    release_optional(borrow(concept).try_get_value().map(|value| Concept::Value(value.clone())))
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>boolean</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_boolean(concept: *const Concept) -> bool {
    borrow(concept).is_boolean()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>long</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_long(concept: *const Concept) -> bool {
    borrow(concept).is_long()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>double</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_double(concept: *const Concept) -> bool {
    borrow(concept).is_double()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>decimal</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_decimal(concept: *const Concept) -> bool {
    borrow(concept).is_decimal()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>string</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_string(concept: *const Concept) -> bool {
    borrow(concept).is_string()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>date</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_date(concept: *const Concept) -> bool {
    borrow(concept).is_date()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>datetime</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_datetime(concept: *const Concept) -> bool {
    borrow(concept).is_datetime()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>datetime-tz</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_datetime_tz(concept: *const Concept) -> bool {
    borrow(concept).is_datetime_tz()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>duration</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_duration(concept: *const Concept) -> bool {
    borrow(concept).is_duration()
}

/// Returns <code>true</code> if the value which this <code>Concept</code> holds is of type <code>struct</code>.
/// Otherwise, returns <code>false</code>.
#[no_mangle]
pub extern "C" fn concept_is_struct(concept: *const Concept) -> bool {
    borrow(concept).is_struct()
}

/// Returns a <code>boolean</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_boolean(concept: *const Concept) -> bool {
    match borrow(concept).try_get_boolean() {
        Some(value) => value,
        None => unreachable!("Attempting to unwrap a non-boolean {:?} as boolean", borrow(concept)),
    }
}

/// Returns the <code>long</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_long(concept: *const Concept) -> i64 {
    match borrow(concept).try_get_long() {
        Some(value) => value,
        None => unreachable!("Attempting to unwrap a non-long {:?} as long", borrow(concept)),
    }
}

/// Returns the <code>double</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_double(concept: *const Concept) -> f64 {
    match borrow(concept).try_get_double() {
        Some(value) => value,
        None => unreachable!("Attempting to unwrap a non-double {:?} as double", borrow(concept)),
    }
}

/// Returns the <code>decimal</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_decimal(concept: *const Concept) -> Decimal {
    match borrow(concept).try_get_decimal() {
        Some(value) => value,
        None => unreachable!("Attempting to unwrap a non-decimal {:?} as decimal", borrow(concept)),
    }
}

/// Returns the <code>string</code> value of this value concept.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_string(concept: *const Concept) -> *mut c_char {
    match borrow(concept).try_get_string() {
        Some(value) => release_string(value.clone().to_owned()),
        None => unreachable!("Attempting to unwrap a non-string {:?} as string", borrow(concept)),
    }
}

/// Returns the value of this date value concept as seconds since the start of the UNIX epoch.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_date_as_seconds(concept: *const Concept) -> i64 {
    match borrow(concept).try_get_date() {
        Some(value) => value.and_time(NaiveTime::MIN).and_utc().timestamp(),
        None => unreachable!("Attempting to unwrap a non-date {:?} as date", borrow(concept)),
    }
}

/// Returns the value of this datetime value concept as seconds and nanoseconds parts since the start of the UNIX epoch.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_datetime(concept: *const Concept) -> DatetimeInNanos {
    match borrow(concept).try_get_datetime() {
        Some(value) => DatetimeInNanos::new(&value.and_utc()),
        None => unreachable!("Attempting to unwrap a non-datetime {:?} as datetime", borrow(concept)),
    }
}

/// Returns the value of this datetime-tz value concept as seconds and nanoseconds parts since the start of the UNIX epoch and timezone information.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_datetime_tz(concept: *const Concept) -> DatetimeAndTimeZone {
    match borrow(concept).try_get_datetime_tz() {
        Some(value) => DatetimeAndTimeZone::new(&value),
        None => unreachable!("Attempting to unwrap a non-datetime-tz {:?} as datetime-tz", borrow(concept)),
    }
}

/// Returns the value of this duration value.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_duration(concept: *const Concept) -> Duration {
    match borrow(concept).try_get_duration() {
        Some(value) => value,
        None => unreachable!("Attempting to unwrap a non-duration {:?} as duration", borrow(concept)),
    }
}

/// Returns the value of this struct value concept represented as an iterator of {field_name: value?} pairs.
/// If the value has another type, the error is set.
#[no_mangle]
pub extern "C" fn concept_get_struct(concept: *const Concept) -> *mut StringAndOptValueIterator {
    match borrow(concept).try_get_struct() {
        Some(value) => release(StringAndOptValueIterator(CIterator(box_stream(
            value.fields().clone().into_iter().map(|pair| pair.into()),
        )))),
        None => unreachable!("Attempting to unwrap a non-struct {:?} as struct", borrow(concept)),
    }
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

/// Checks if this <code>Concept</code> is an <code>EntityType</code>.
#[no_mangle]
pub extern "C" fn concept_is_entity_type(concept: *const Concept) -> bool {
    borrow(concept).is_entity_type()
}

/// Checks if this <code>Concept</code> is a <code>RelationType</code>.
#[no_mangle]
pub extern "C" fn concept_is_relation_type(concept: *const Concept) -> bool {
    borrow(concept).is_relation_type()
}

/// Checks if this <code>Concept</code> is an <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn concept_is_attribute_type(concept: *const Concept) -> bool {
    borrow(concept).is_attribute_type()
}

/// Checks if this <code>Concept</code> is a <code>RoleType</code>.
#[no_mangle]
pub extern "C" fn concept_is_role_type(concept: *const Concept) -> bool {
    borrow(concept).is_role_type()
}

/// Checks if this <code>Concept</code> is an <code>Entity</code>.
#[no_mangle]
pub extern "C" fn concept_is_entity(concept: *const Concept) -> bool {
    borrow(concept).is_entity()
}

/// Checks if this <code>Concept</code> is a <code>Relation</code>.
#[no_mangle]
pub extern "C" fn concept_is_relation(concept: *const Concept) -> bool {
    borrow(concept).is_relation()
}

/// Checks if this <code>Concept</code> is an <code>Attribute</code>.
#[no_mangle]
pub extern "C" fn concept_is_attribute(concept: *const Concept) -> bool {
    borrow(concept).is_attribute()
}

/// Checks if this <code>Concept</code> is a <code>Value</code>.
#[no_mangle]
pub extern "C" fn concept_is_value(concept: *const Concept) -> bool {
    borrow(concept).is_value()
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
