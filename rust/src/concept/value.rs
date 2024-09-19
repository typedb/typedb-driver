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

use std::collections::HashMap;
use std::fmt;
use std::fmt::{Debug, Display, Formatter};

use chrono::{DateTime, NaiveDate, NaiveDateTime};
use chrono_tz::Tz;

use crate::Error;

/// Represents the type of primitive value is held by a Value or Attribute.
#[repr(C)]
#[derive(Clone, PartialEq, Eq)]
pub enum ValueType {
    Boolean,
    Long,
    Double,
    Decimal,
    String,
    Date,
    Datetime,
    DatetimeTZ,
    Duration,
    Struct(String),
}

impl ValueType {
    pub fn name(&self) -> &str {
        match self {
            Self::Boolean => "bool",
            Self::Long => "long",
            Self::Double => "double",
            Self::Decimal => "decimal",
            Self::String => "string",
            Self::Date => "date",
            Self::Datetime => "datetime",
            Self::DatetimeTZ => "datetime_tz",
            Self::Duration => "duration",
            Self::Struct(name) => &name,
        }
    }
}

impl fmt::Display for ValueType {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        fmt::Debug::fmt(self, f)
    }
}

impl fmt::Debug for ValueType {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.name())
    }
}

#[derive(Clone, PartialEq)]
pub enum Value {
    Boolean(bool),
    Long(i64),
    Double(f64),
    Decimal(Decimal),
    String(String),
    Date(NaiveDate),
    Datetime(NaiveDateTime),
    DatetimeTZ(DateTime<Tz>),
    Duration(Duration),
    Struct(Struct, String),
}

impl Value {
    /// Retrieves the `ValueType` of this value concept.
    ///
    /// # Examples
    ///
    /// ```rust
    /// value.get_type();
    /// ```
    pub fn get_type(&self) -> ValueType {
        match self {
            Self::Boolean(_) => ValueType::Boolean,
            Self::Long(_) => ValueType::Long,
            Self::Double(_) => ValueType::Double,
            Self::String(_) => ValueType::String,
            Self::Decimal(_) => ValueType::Decimal,
            Self::Date(_) => ValueType::Date,
            Self::Datetime(_) => ValueType::Datetime,
            Self::DatetimeTZ(_) => ValueType::DatetimeTZ,
            Self::Duration(_) => ValueType::Duration,
            Self::Struct(_, struct_type_name) => ValueType::Struct(struct_type_name.clone()),
        }
    }

    /// Retrieves the name of the `ValueType` of this value concept.
    ///
    /// # Examples
    ///
    /// ```rust
    /// value.get_type_name();
    /// ```
    pub fn get_type_name(&self) -> &str {
        match self {
            Self::Boolean(_) => ValueType::Boolean.name(),
            Self::Long(_) => ValueType::Long.name(),
            Self::Double(_) => ValueType::Double.name(),
            Self::String(_) => ValueType::String.name(),
            Self::Decimal(_) => ValueType::Decimal.name(),
            Self::Date(_) => ValueType::Date.name(),
            Self::Datetime(_) => ValueType::Datetime.name(),
            Self::DatetimeTZ(_) => ValueType::DatetimeTZ.name(),
            Self::Duration(_) => ValueType::Duration.name(),
            Self::Struct(_, struct_type_name) => struct_type_name,
        }
    }

    pub fn get_boolean(&self) -> Option<bool> {
        if let Value::Boolean(bool) = self {
            Some(*bool)
        } else {
            None
        }
    }

    pub fn get_long(&self) -> Option<i64> {
        if let Value::Long(long) = self {
            Some(*long)
        } else {
            None
        }
    }

    pub fn get_double(&self) -> Option<f64> {
        if let Value::Double(double) = self {
            Some(*double)
        } else {
            None
        }
    }

    pub fn get_string(&self) -> Option<&str> {
        if let Value::String(string) = self {
            Some(&**string)
        } else {
            None
        }
    }

    pub fn get_decimal(&self) -> Option<Decimal> {
        if let Value::Decimal(decimal) = self {
            Some(*decimal)
        } else {
            None
        }
    }

    pub fn get_date(&self) -> Option<NaiveDate> {
        if let Value::Date(naive_date) = self {
            Some(*naive_date)
        } else {
            None
        }
    }

    pub fn get_datetime(&self) -> Option<NaiveDateTime> {
        if let Value::Datetime(datetime) = self {
            Some(*datetime)
        } else {
            None
        }
    }

    pub fn get_datetime_tz(&self) -> Option<DateTime<Tz>> {
        if let Value::DatetimeTZ(datetime_tz) = self {
            Some(*datetime_tz)
        } else {
            None
        }
    }

    pub fn get_duration(&self) -> Option<Duration> {
        if let Value::Duration(duration) = self {
            Some(*duration)
        } else {
            None
        }
    }

    pub fn get_struct(&self) -> Option<&Struct> {
        if let Value::Struct(struct_, _) = self {
            Some(struct_)
        } else {
            None
        }
    }
}

impl Display for Value {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        Debug::fmt(self, f)
    }
}

impl Debug for Value {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}: ", self.get_type_name())?;
        match self {
            Value::Boolean(bool) => write!(f, "{}", bool),
            Value::Long(long) => write!(f, "{}", long),
            Value::Double(double) => write!(f, "{}", double),
            Value::Decimal(decimal) => write!(f, "{}", decimal),
            Value::String(string) => write!(f, "\"{}\"", string),
            Value::Date(date) => write!(f, "{}", date),
            Value::Datetime(datetime) => write!(f, "{}", datetime),
            Value::DatetimeTZ(datetime_tz) => write!(f, "{}", datetime_tz),
            Value::Duration(duration) => write!(f, "{}", duration),
            Value::Struct(value, name) => write!(f, "{} {}", name, value)
        }
    }
}

/// A fixed-point decimal number.
/// Holds exactly 19 digits after the decimal point and a 64-bit value before the decimal point.
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct Decimal {
    integer: i64,
    fractional: u64,
}

impl Decimal {
    const FRACTIONAL_PART_DENOMINATOR_LOG10: u32 = 19;
    pub const FRACTIONAL_PART_DENOMINATOR: u64 = 10u64.pow(Decimal::FRACTIONAL_PART_DENOMINATOR_LOG10);
    pub const MIN: Self = Self::new(i64::MIN, 0);
    pub const MAX: Self = Self::new(i64::MAX, Decimal::FRACTIONAL_PART_DENOMINATOR - 1);

    pub const fn new(integer: i64, fractional: u64) -> Self {
        assert!(fractional < Decimal::FRACTIONAL_PART_DENOMINATOR);
        Self { integer, fractional }
    }

    /// Get the integer part of the decimal as normal signed 64 bit number
    pub fn integer_part(&self) -> i64 {
        self.integer
    }

    /// Get the fractional part of the decimal, in multiples of 10^-19 (Decimal::FRACTIONAL_PART_DENOMINATOR)
    /// This means, the smallest decimal representable is 10^-19, and up to 19 decimal places are supported.
    pub fn fractional_part(&self) -> u64 {
        self.fractional
    }
}

impl Display for Decimal {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        Debug::fmt(self, f)
    }
}

impl Debug for Decimal {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        // count number of tailing 0's that don't have to be represented
        let mut tail_0s = 0;
        let mut fractional = self.fractional;
        while fractional % 10 == 0 {
            tail_0s += 1;
            fractional /= 10;
        }

        // count number of leading 0's that have to be represented
        let digits = (fractional as f64).log10().floor() as u64;
        let leading_0s = Self::FRACTIONAL_PART_DENOMINATOR - digits - tail_0s;
        write!(f, "{}.{:0width$}{}", self.integer_part(), "", digits, width = leading_0s as usize)
    }
}

/// A relative duration, which contains months, days, and nanoseconds.
/// Can be used for calendar-relative durations (eg 7 days forward), or for absolute durations using the nanosecond component
/// When used as an absolute duration, convertible to chrono::Duration
#[derive(Clone, Copy, Hash, PartialEq, Eq)]
pub struct Duration {
    pub months: u32,
    pub days: u32,
    pub nanos: u64,
}

impl Duration {
    pub fn new(months: u32, days: u32, nanos: u64) -> Self {
        Self { months, days, nanos }
    }

    pub fn months(&self) -> u32 {
        self.months
    }

    pub fn days(&self) -> u32 {
        self.days
    }

    pub fn nanos(&self) -> u64 {
        self.nanos
    }
}

impl TryFrom<Duration> for chrono::Duration {
    type Error = crate::Error;

    fn try_from(duration: Duration) -> Result<Self, Self::Error> {
        if duration.months != 0 || duration.days != 0 {
            Err(Error::Other(String::from("Converting TypeDB duration to chrono::Duration is only possible when months and days are not set.")))
        } else {
            match i64::try_from(duration.nanos) {
                Ok(nanos) => Ok(chrono::Duration::nanoseconds(nanos)),
                Err(err) => Err(Error::Other(String::from("Duration u64 nanos exceeded i64 required for chrono::Duration")))
            }
        }
    }
}

impl Display for Duration {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        Debug::fmt(self, f)
    }
}

impl Debug for Duration {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "months: {}, days: {}, nanos: {}", self.months, self.days, self.nanos)
    }
}

#[derive(Clone, PartialEq)]
pub struct Struct {
    fields: HashMap<String, Value>,
}

impl Display for Struct {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        Display::fmt(self, f)
    }
}

impl Debug for Struct {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{:?}", self.fields)
    }
}
