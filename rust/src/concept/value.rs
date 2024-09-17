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
use std::fmt::Formatter;

use chrono::{DateTime, NaiveDate, NaiveDateTime};
use chrono_tz::Tz;

/// Represents the type of primitive value is held by a Value or Attribute.
#[repr(C)]
#[derive(Clone, PartialEq, Eq)]
pub enum ValueType {
    Boolean,
    Long,
    Double,
    String,
    Decimal,
    Date,
    DateTime,
    DateTimeTZ,
    Duration,
    Struct(String),
}

impl ValueType {
    pub fn name(&self) -> &str {
        match self {
            Self::Boolean => "bool",
            Self::Long => "long",
            Self::Double => "double",
            Self::String => "string",
            Self::Decimal => "decimal",
            Self::Date => "date",
            Self::DateTime => "datetime",
            Self::DateTimeTZ => "datetime_tz",
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

#[derive(Clone, Debug, PartialEq)]
pub enum Value {
    Boolean(bool),
    Long(i64),
    Double(f64),
    String(String),
    Decimal(Decimal),
    Date(NaiveDate),
    DateTime(NaiveDateTime),
    DateTimeTZ(DateTime<Tz>),
    Duration(Duration),
    Struct(Struct, String),
}

impl Value {
    /// Retrieves the `ValueType` of this value concept.
    ///
    /// # Examples
    ///
    /// ```rust
    /// value.get_value_type();
    /// ```
    pub fn get_type(&self) -> ValueType {
        match self {
            Self::Boolean(_) => ValueType::Boolean,
            Self::Long(_) => ValueType::Long,
            Self::Double(_) => ValueType::Double,
            Self::String(_) => ValueType::String,
            Self::Decimal(_) => ValueType::Decimal,
            Self::Date(_) => ValueType::Date,
            Self::DateTime(_) => ValueType::DateTime,
            Self::DateTimeTZ(_) => ValueType::DateTimeTZ,
            Self::Duration(_) => ValueType::Duration,
            Self::Struct(_, struct_type_name) => ValueType::Struct(struct_type_name.clone()),
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
        if let Value::DateTime(datetime) = self {
            Some(*datetime)
        } else {
            None
        }
    }

    pub fn get_datetime_tz(&self) -> Option<DateTime<Tz>> {
        if let Value::DateTimeTZ(datetime_tz) = self {
            Some(*datetime_tz)
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


pub const FRACTIONAL_PART_DENOMINATOR_LOG10: u32 = 19;
const FRACTIONAL_PART_DENOMINATOR: u64 = 10u64.pow(FRACTIONAL_PART_DENOMINATOR_LOG10);

#[allow(clippy::assertions_on_constants)]
const _ASSERT: () = {
    assert!(FRACTIONAL_PART_DENOMINATOR > u64::MAX / 10);
};

#[derive(Clone, Copy, Debug, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct Decimal {
    integer: i64,
    fractional: u64,
}

impl Decimal {
    pub const MIN: Self = Self::new(i64::MIN, 0);
    pub const MAX: Self = Self::new(i64::MAX, FRACTIONAL_PART_DENOMINATOR - 1);

    pub const fn new(integer: i64, fractional: u64) -> Self {
        assert!(fractional < FRACTIONAL_PART_DENOMINATOR);
        Self { integer, fractional }
    }

    pub fn integer_part(&self) -> i64 {
        self.integer
    }

    pub fn fractional_part(&self) -> u64 {
        self.fractional
    }
}


// TODO: see what the most user-friendly interface is here!
#[derive(Clone, Copy, Debug, Hash, PartialEq, Eq)]
pub struct Duration {
    pub months: u32,
    pub days: u32,
    pub nanos: u64,
}

impl Duration {
    pub fn new(months: u32, days: u32, nanos: u64) -> Self {
        Self { months, days, nanos }
    }
}

#[derive(Debug, Clone, PartialEq)]
pub struct Struct {
    fields: HashMap<String, Value>,
}
