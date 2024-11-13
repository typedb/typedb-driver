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

use std::{
    collections::HashMap,
    fmt,
    ops::{Add, Neg, Sub},
    str::FromStr,
};

use chrono::{DateTime, FixedOffset, MappedLocalTime, NaiveDate, NaiveDateTime};
use chrono_tz::Tz;

use crate::Error;

/// Represents the type of primitive value is held by a Value or Attribute.
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
    pub(crate) const NONE_STR: &'static str = "none";
    pub(crate) const BOOLEAN_STR: &'static str = "boolean";
    pub(crate) const LONG_STR: &'static str = "long";
    pub(crate) const DOUBLE_STR: &'static str = "double";
    pub(crate) const DECIMAL_STR: &'static str = "decimal";
    pub(crate) const STRING_STR: &'static str = "string";
    pub(crate) const DATE_STR: &'static str = "date";
    pub(crate) const DATETIME_STR: &'static str = "datetime";
    pub(crate) const DATETIME_TZ_STR: &'static str = "datetime-tz";
    pub(crate) const DURATION_STR: &'static str = "duration";

    pub fn name(&self) -> &str {
        match self {
            Self::Boolean => Self::BOOLEAN_STR,
            Self::Long => Self::LONG_STR,
            Self::Double => Self::DOUBLE_STR,
            Self::Decimal => Self::DECIMAL_STR,
            Self::String => Self::STRING_STR,
            Self::Date => Self::DATE_STR,
            Self::Datetime => Self::DATETIME_STR,
            Self::DatetimeTZ => Self::DATETIME_TZ_STR,
            Self::Duration => Self::DURATION_STR,
            Self::Struct(name) => name,
        }
    }
}

impl fmt::Display for ValueType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        fmt::Debug::fmt(self, f)
    }
}

impl fmt::Debug for ValueType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
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
    DatetimeTZ(DateTime<TimeZone>),
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

    pub fn get_datetime_tz(&self) -> Option<DateTime<TimeZone>> {
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

impl fmt::Display for Value {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Boolean(bool) => write!(f, "{}", bool),
            Self::Long(long) => write!(f, "{}", long),
            Self::Double(double) => write!(f, "{}", double),
            Self::String(string) => write!(f, "\"{}\"", string),
            Self::Decimal(decimal) => write!(f, "{}", decimal),
            Self::Date(date) => write!(f, "{}", date.format("%Y-%m-%d")),
            Self::Datetime(datetime) => write!(f, "{}", datetime.format("%FT%T%.9f")),
            Self::DatetimeTZ(datetime_tz) => match datetime_tz.timezone() {
                TimeZone::IANA(tz) => write!(f, "{} {}", datetime_tz.format("%FT%T%.9f"), tz.name()),
                TimeZone::Fixed(_) => write!(f, "{}", datetime_tz.format("%FT%T%.9f%:z")),
            },
            Self::Duration(duration) => write!(f, "{}", duration),
            Self::Struct(value, name) => write!(f, "{} {}", name, value),
        }
    }
}

impl fmt::Debug for Value {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
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
            Value::Struct(value, name) => write!(f, "{} {}", name, value),
        }
    }
}

/// A fixed-point decimal number.
/// Holds exactly 19 digits after the decimal point and a 64-bit value before the decimal point.
#[repr(C)]
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord, Hash, Default)]
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

impl Neg for Decimal {
    type Output = Self;

    fn neg(self) -> Self::Output {
        Self::default() - self
    }
}

impl Add for Decimal {
    type Output = Self;

    fn add(self, rhs: Self) -> Self::Output {
        let lhs = self;
        let (fractional, carry) = match lhs.fractional.overflowing_add(rhs.fractional) {
            (frac, false) if frac < Self::FRACTIONAL_PART_DENOMINATOR => (frac, 0),
            (frac, true) if frac < Self::FRACTIONAL_PART_DENOMINATOR => {
                (frac + 0u64.wrapping_sub(Self::FRACTIONAL_PART_DENOMINATOR), 1)
            }
            (frac, false) => (frac - Self::FRACTIONAL_PART_DENOMINATOR, 1),
            (_, true) => unreachable!(),
        };
        let integer = lhs.integer + rhs.integer + carry;

        Self::new(integer, fractional)
    }
}

impl Sub for Decimal {
    type Output = Self;

    fn sub(self, rhs: Self) -> Self::Output {
        let lhs = self;
        let (fractional, carry) = match lhs.fractional.overflowing_sub(rhs.fractional) {
            (frac, false) => (frac, 0),
            (frac, true) => (frac.wrapping_add(Self::FRACTIONAL_PART_DENOMINATOR), 1),
        };
        let integer = lhs.integer - rhs.integer - carry;

        Self::new(integer, fractional)
    }
}

impl fmt::Display for Decimal {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        fmt::Debug::fmt(self, f)
    }
}

impl fmt::Debug for Decimal {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        if self.fractional == 0 {
            write!(f, "{}.0", self.integer_part())?;
        } else {
            // count number of tailing 0's that don't have to be represented
            let mut tail_0s = 0;
            let mut fractional = self.fractional;
            while fractional % 10 == 0 {
                tail_0s += 1;
                fractional /= 10;
            }

            let fractional_width = Self::FRACTIONAL_PART_DENOMINATOR_LOG10 - tail_0s;
            write!(f, "{}.{:0width$}", self.integer_part(), fractional, width = fractional_width as usize)?;
        }
        Ok(())
    }
}

/// Offset for datetime-tz. Can be retrieved from an IANA Tz or a FixedOffset.
#[derive(Copy, Clone, Debug)]
pub enum Offset {
    IANA(<Tz as chrono::TimeZone>::Offset),
    Fixed(FixedOffset),
}

impl chrono::Offset for Offset {
    fn fix(&self) -> FixedOffset {
        match self {
            Self::IANA(inner) => inner.fix(),
            Self::Fixed(inner) => inner.fix(),
        }
    }
}

impl fmt::Display for Offset {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::IANA(inner) => fmt::Display::fmt(inner, f),
            Self::Fixed(inner) => fmt::Display::fmt(inner, f),
        }
    }
}

/// TimeZone for datetime-tz. Can be represented as an IANA Tz or as a FixedOffset.
#[derive(Copy, Clone, Debug)]
pub enum TimeZone {
    IANA(Tz),
    Fixed(FixedOffset),
}

impl Default for TimeZone {
    fn default() -> Self {
        Self::IANA(Tz::default())
    }
}

impl chrono::TimeZone for TimeZone {
    type Offset = Offset;

    fn from_offset(offset: &Self::Offset) -> Self {
        match offset {
            Offset::IANA(offset) => Self::IANA(Tz::from_offset(offset)),
            Offset::Fixed(offset) => Self::Fixed(FixedOffset::from_offset(offset)),
        }
    }

    fn offset_from_local_date(&self, local: &NaiveDate) -> MappedLocalTime<Self::Offset> {
        match self {
            Self::IANA(inner) => inner.offset_from_local_date(local).map(Offset::IANA),
            Self::Fixed(inner) => inner.offset_from_local_date(local).map(Offset::Fixed),
        }
    }

    fn offset_from_local_datetime(&self, local: &NaiveDateTime) -> MappedLocalTime<Self::Offset> {
        match self {
            Self::IANA(inner) => inner.offset_from_local_datetime(local).map(Offset::IANA),
            Self::Fixed(inner) => inner.offset_from_local_datetime(local).map(Offset::Fixed),
        }
    }

    fn offset_from_utc_date(&self, utc: &NaiveDate) -> Self::Offset {
        match self {
            TimeZone::IANA(inner) => Offset::IANA(inner.offset_from_utc_date(utc)),
            TimeZone::Fixed(inner) => Offset::Fixed(inner.offset_from_utc_date(utc)),
        }
    }

    fn offset_from_utc_datetime(&self, utc: &NaiveDateTime) -> Self::Offset {
        match self {
            TimeZone::IANA(inner) => Offset::IANA(inner.offset_from_utc_datetime(utc)),
            TimeZone::Fixed(inner) => Offset::Fixed(inner.offset_from_utc_datetime(utc)),
        }
    }
}

/// A relative duration, which contains months, days, and nanoseconds.
/// Can be used for calendar-relative durations (eg 7 days forward), or for absolute durations using the nanosecond component
/// When used as an absolute duration, convertible to chrono::Duration
#[repr(C)]
#[derive(Clone, Copy, Hash, PartialEq, Eq)]
pub struct Duration {
    pub months: u32,
    pub days: u32,
    pub nanos: u64,
}

impl Duration {
    const NANOS_PER_SEC: u64 = 1_000_000_000;
    const NANOS_PER_MINUTE: u64 = 60 * Self::NANOS_PER_SEC;
    const NANOS_PER_HOUR: u64 = 60 * 60 * Self::NANOS_PER_SEC;
    const DAYS_PER_WEEK: u32 = 7;
    const MONTHS_PER_YEAR: u32 = 12;

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

    fn is_empty(&self) -> bool {
        self.months == 0 && self.days == 0 && self.nanos == 0
    }
}

impl TryFrom<Duration> for chrono::Duration {
    type Error = crate::Error;

    fn try_from(duration: Duration) -> Result<Self, Self::Error> {
        if duration.months != 0 || duration.days != 0 {
            Err(Error::Other(String::from(
                "Converting TypeDB duration to chrono::Duration is only possible when months and days are not set.",
            )))
        } else {
            match i64::try_from(duration.nanos) {
                Ok(nanos) => Ok(chrono::Duration::nanoseconds(nanos)),
                Err(_) => {
                    Err(Error::Other(String::from("Duration u64 nanos exceeded i64 required for chrono::Duration")))
                }
            }
        }
    }
}

// TODO: Duration parsing is basically a copy of TypeDB server's code, can be made a dependency.
#[derive(Debug)]
pub struct DurationParseError;

struct Segment {
    number: u64,
    symbol: u8,
    number_len: usize,
}

fn read_u32(str: &str) -> Result<(Segment, &str), DurationParseError> {
    let mut i = 0;
    while i + 1 < str.len() && str.as_bytes()[i].is_ascii_digit() {
        i += 1;
    }
    if i == 0 {
        return Err(DurationParseError);
    }
    let value = str[..i].parse().map_err(|_| DurationParseError)?;
    Ok((Segment { number: value, symbol: str.as_bytes()[i], number_len: i }, &str[i + 1..]))
}

impl FromStr for Duration {
    type Err = DurationParseError;

    fn from_str(mut str: &str) -> Result<Self, Self::Err> {
        let mut months = 0;
        let mut days = 0;
        let mut nanos = 0;

        if str.as_bytes()[0] != b'P' {
            return Err(DurationParseError);
        }
        str = &str[1..];

        let mut parsing_time = false;
        let mut previous_symbol = None;
        while !str.is_empty() {
            if str.as_bytes()[0] == b'T' {
                parsing_time = true;
                str = &str[1..];
            }

            let (Segment { number, symbol, number_len }, tail) = read_u32(str)?;
            str = tail;

            if previous_symbol == Some(b'.') && symbol != b'S' {
                return Err(DurationParseError);
            }

            match symbol {
                b'Y' => months += number as u32 * Self::MONTHS_PER_YEAR,
                b'M' if !parsing_time => months += number as u32,

                b'W' => days += number as u32 * Self::DAYS_PER_WEEK,
                b'D' => days += number as u32,

                b'H' => nanos += number * Self::NANOS_PER_HOUR,
                b'M' if parsing_time => nanos += number * Self::NANOS_PER_MINUTE,
                b'.' => nanos += number * Self::NANOS_PER_SEC,
                b'S' if previous_symbol != Some(b'.') => nanos += number * Self::NANOS_PER_SEC,
                b'S' if previous_symbol == Some(b'.') => nanos += number * 10u64.pow(9 - number_len as u32),
                _ => return Err(DurationParseError),
            }
            previous_symbol = Some(symbol);
        }

        Ok(Self { months, days, nanos })
    }
}

/// ISO-8601 compliant representation of a duration
impl fmt::Display for Duration {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        if self.is_empty() {
            f.write_str("PT0S")?;
            return Ok(());
        }

        write!(f, "P")?;

        if self.months > 0 || self.days > 0 {
            let years = self.months / Self::MONTHS_PER_YEAR;
            let months = self.months % Self::MONTHS_PER_YEAR;
            let days = self.days;
            if years > 0 {
                write!(f, "{years}Y")?;
            }
            if months > 0 {
                write!(f, "{months}M")?;
            }
            if days > 0 {
                write!(f, "{days}D")?;
            }
        }

        if self.nanos > 0 {
            write!(f, "T")?;

            let hours = self.nanos / Self::NANOS_PER_HOUR;
            let minutes = (self.nanos % Self::NANOS_PER_HOUR) / Self::NANOS_PER_MINUTE;
            let seconds = (self.nanos % Self::NANOS_PER_MINUTE) / Self::NANOS_PER_SEC;
            let nanos = self.nanos % Self::NANOS_PER_SEC;

            if hours > 0 {
                write!(f, "{hours}H")?;
            }
            if minutes > 0 {
                write!(f, "{minutes}M")?;
            }
            if seconds > 0 && nanos == 0 {
                write!(f, "{seconds}S")?;
            } else if nanos > 0 {
                write!(f, "{seconds}.{nanos:09}S")?;
            }
        }

        Ok(())
    }
}

impl fmt::Debug for Duration {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "months: {}, days: {}, nanos: {}", self.months, self.days, self.nanos)
    }
}

#[derive(Clone, PartialEq)]
pub struct Struct {
    pub(crate) fields: HashMap<String, Option<Value>>,
}

impl Struct {
    pub fn fields(&self) -> &HashMap<String, Option<Value>> {
        &self.fields
    }
}

impl fmt::Display for Struct {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        fmt::Debug::fmt(self, f)
    }
}

impl fmt::Debug for Struct {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{:?}", self.fields)
    }
}
