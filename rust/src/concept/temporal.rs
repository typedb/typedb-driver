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

//! TypeDB temporal values. Calendar/time-zone rules come from maintained upstream
//! libraries; this module owns TypeDB's civil, leap-second and wire semantics.
use crate::{Error, Result};
use std::{fmt, str::FromStr};
use time::{Date, Month, PrimitiveDateTime, UtcOffset};
use tz::{UtcDateTime, timezone::TimeZoneRef};
fn invalid(reason: impl fmt::Display) -> Error {
    Error::Other(format!("Invalid temporal value: {reason}"))
}

/// A timezone-free civil datetime, retaining TypeDB's full range and leap seconds.
#[derive(Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct Datetime(UtcDateTime);
impl Datetime {
    pub fn new(year: i32, month: u8, day: u8, hour: u8, minute: u8, second: u8, nanos: u32) -> Result<Self> {
        if !(-262143..=262142).contains(&year) {
            return Err(invalid("year outside TypeDB's supported range"));
        }
        UtcDateTime::new(year, month, day, hour, minute, second, nanos).map(Self).map_err(invalid)
    }
    /// Decode the existing protobuf representation, including Chrono-era leap
    /// seconds (second 59 plus nanoseconds in 1_000_000_000..2_000_000_000).
    pub fn from_timestamp(seconds: i64, nanos: u32) -> Result<Self> {
        if nanos >= 2_000_000_000 || nanos >= 1_000_000_000 && seconds.rem_euclid(60) != 59 {
            return Err(invalid("invalid nanosecond component"));
        }
        let value = UtcDateTime::from_timespec(seconds, nanos % 1_000_000_000).map_err(invalid)?;
        Self::new(
            value.year(),
            value.month(),
            value.month_day(),
            value.hour(),
            value.minute(),
            if nanos >= 1_000_000_000 { 60 } else { value.second() },
            value.nanoseconds(),
        )
    }
    pub fn wire_parts(self) -> (i64, u32) {
        (
            self.0.unix_time() - i64::from(self.second() == 60),
            self.nanosecond() + if self.second() == 60 { 1_000_000_000 } else { 0 },
        )
    }
    pub fn year(self) -> i32 {
        self.0.year()
    }
    pub fn month(self) -> u32 {
        u32::from(self.0.month())
    }
    pub fn day(self) -> u32 {
        u32::from(self.0.month_day())
    }
    pub fn hour(self) -> u32 {
        u32::from(self.0.hour())
    }
    pub fn minute(self) -> u32 {
        u32::from(self.0.minute())
    }
    pub fn second(self) -> u32 {
        u32::from(self.0.second())
    }
    pub fn nanosecond(self) -> u32 {
        self.0.nanoseconds()
    }
    pub fn date(self) -> Date {
        Date::from_calendar_date(self.year(), Month::try_from(self.0.month()).unwrap(), self.0.month_day()).unwrap()
    }
    pub fn weekday(self) -> time::Weekday {
        self.date().weekday()
    }
    pub(crate) fn fmt_typeql(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        self.fmt_parts(f, 'T', true)
    }
    fn fmt_parts(&self, f: &mut fmt::Formatter<'_>, sep: char, exact: bool) -> fmt::Result {
        write!(f, "{}{sep}{:02}:{:02}:{:02}", self.date(), self.hour(), self.minute(), self.second())?;
        let nanos = self.nanosecond();
        if exact {
            write!(f, ".{nanos:09}")
        } else if nanos == 0 {
            Ok(())
        } else if nanos % 1_000_000 == 0 {
            write!(f, ".{:03}", nanos / 1_000_000)
        } else if nanos % 1_000 == 0 {
            write!(f, ".{:06}", nanos / 1_000)
        } else {
            write!(f, ".{nanos:09}")
        }
    }
}
impl std::hash::Hash for Datetime {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        std::hash::Hash::hash(&self.wire_parts(), state);
    }
}
impl fmt::Display for Datetime {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        self.fmt_parts(f, ' ', false)
    }
}
impl fmt::Debug for Datetime {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        self.fmt_parts(f, 'T', false)
    }
}
impl TryFrom<PrimitiveDateTime> for Datetime {
    type Error = Error;
    fn try_from(d: PrimitiveDateTime) -> Result<Self> {
        Self::new(d.year(), d.month() as u8, d.day(), d.hour(), d.minute(), d.second(), d.nanosecond())
    }
}
impl FromStr for Datetime {
    type Err = Error;
    fn from_str(s: &str) -> Result<Self> {
        let (date, clock) =
            s.split_once('T').or_else(|| s.split_once(' ')).ok_or_else(|| invalid("expected date and time"))?;
        let date = Date::parse(
            date,
            &time::format_description::parse_borrowed::<2>("[year sign:automatic]-[month]-[day]").map_err(invalid)?,
        )
        .map_err(invalid)?;
        let mut parts = clock.split(':');
        let hour = parts.next().ok_or_else(|| invalid("missing hour"))?.parse::<u8>().map_err(invalid)?;
        let minute = parts.next().unwrap_or("0").parse::<u8>().map_err(invalid)?;
        let seconds = parts.next().unwrap_or("0");
        if parts.next().is_some() {
            return Err(invalid("extra time component"));
        }
        let (second, fraction) = seconds.split_once('.').unwrap_or((seconds, ""));
        if fraction.len() > 9 || !fraction.bytes().all(|c| c.is_ascii_digit()) {
            return Err(invalid("invalid fractional seconds"));
        }
        let nanos = if fraction.is_empty() {
            0
        } else {
            fraction.parse::<u32>().map_err(invalid)? * 10u32.pow(9 - fraction.len() as u32)
        };
        Self::new(date.year(), date.month() as u8, date.day(), hour, minute, second.parse().map_err(invalid)?, nanos)
    }
}

/// Validated static IANA zone name; aliases retain their identity on the wire.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub struct NamedTimeZone {
    name: &'static str,
}
impl NamedTimeZone {
    pub fn name(self) -> &'static str {
        self.name
    }
    pub fn get(name: &str) -> Result<Self> {
        Self::lookup(name, false)
    }
    pub fn get_case_insensitive(name: &str) -> Result<Self> {
        Self::lookup(name, true)
    }
    fn lookup(name: &str, insensitive: bool) -> Result<Self> {
        tzdb::TZ_NAMES
            .iter()
            .find(|n| if insensitive { n.eq_ignore_ascii_case(name) } else { **n == name })
            .map(|&name| Self { name })
            .ok_or_else(|| invalid("unknown IANA timezone"))
    }
    fn zone(self) -> TimeZoneRef<'static> {
        tzdb::tz_by_name(self.name).expect("validated embedded timezone")
    }
}
impl FromStr for NamedTimeZone {
    type Err = Error;
    fn from_str(s: &str) -> Result<Self> {
        Self::get(s)
    }
}
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum TimeZone {
    IANA(NamedTimeZone),
    Fixed(UtcOffset),
}
impl Default for TimeZone {
    fn default() -> Self {
        Self::IANA(NamedTimeZone { name: "UTC" })
    }
}
impl TimeZone {
    pub fn iana(name: &str) -> Result<Self> {
        NamedTimeZone::get(name).map(Self::IANA)
    }
    pub fn fixed(seconds: i32) -> Result<Self> {
        if !(-86399..=86399).contains(&seconds) {
            return Err(invalid("UTC offset must be less than 24 hours"));
        }
        UtcOffset::from_whole_seconds(seconds).map(Self::Fixed).map_err(invalid)
    }
    pub fn from_utc_datetime(self, utc: &Datetime) -> Result<DatetimeTZ> {
        let result = DatetimeTZ { utc: *utc, zone: self };
        result.offset()?;
        Ok(result)
    }
    /// Return every valid UTC candidate for a local civil time. Gaps return an
    /// empty list and folds return both instants; no DST choice is implicit.
    pub fn local_candidates(self, local: Datetime) -> Result<Vec<DatetimeTZ>> {
        let offsets: Vec<i32> = match self {
            Self::Fixed(offset) => vec![offset.whole_seconds()],
            Self::IANA(zone) => tz::DateTime::find(
                local.year(),
                local.0.month(),
                local.0.month_day(),
                local.0.hour(),
                local.0.minute(),
                local.0.second(),
                local.nanosecond(),
                zone.zone(),
            )
            .map_err(invalid)?
            .into_inner()
            .into_iter()
            .filter_map(|value| match value {
                tz::datetime::FoundDateTimeKind::Normal(d) => Some(d.local_time_type().ut_offset()),
                _ => None,
            })
            .collect(),
        };
        offsets
            .into_iter()
            .map(|offset| {
                let (seconds, mut nanos) = local.wire_parts();
                let mut utc_seconds = seconds - i64::from(offset);
                if nanos >= 1_000_000_000 && offset % 60 != 0 {
                    utc_seconds += 1;
                    nanos -= 1_000_000_000;
                }
                self.from_utc_datetime(&Datetime::from_timestamp(utc_seconds, nanos)?)
            })
            .collect()
    }
    /// Require a unique local time. Use `local_candidates` to choose a fold.
    pub fn from_local_datetime(self, local: Datetime) -> Result<DatetimeTZ> {
        let mut values = self.local_candidates(local)?;
        if values.len() != 1 {
            return Err(invalid("ambiguous or nonexistent local datetime"));
        }
        Ok(values.remove(0))
    }
}
/// Zoned datetime equality retains the existing instant-based semantics.
#[derive(Clone, Copy)]
pub struct DatetimeTZ {
    utc: Datetime,
    zone: TimeZone,
}
impl PartialEq for DatetimeTZ {
    fn eq(&self, other: &Self) -> bool {
        self.utc == other.utc
    }
}
impl Eq for DatetimeTZ {}
impl std::hash::Hash for DatetimeTZ {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        std::hash::Hash::hash(&self.utc, state);
    }
}
impl PartialOrd for DatetimeTZ {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        Some(self.cmp(other))
    }
}
impl Ord for DatetimeTZ {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.utc.cmp(&other.utc)
    }
}
impl DatetimeTZ {
    pub fn timezone(self) -> TimeZone {
        self.zone
    }
    pub fn naive_utc(self) -> Datetime {
        self.utc
    }
    pub fn wire_parts(self) -> (i64, u32) {
        self.utc.wire_parts()
    }
    pub fn offset(self) -> Result<UtcOffset> {
        match self.zone {
            TimeZone::Fixed(o) if (-86399..=86399).contains(&o.whole_seconds()) => Ok(o),
            TimeZone::Fixed(_) => Err(invalid("UTC offset must be less than 24 hours")),
            TimeZone::IANA(z) => UtcOffset::from_whole_seconds(
                z.zone().find_local_time_type(self.wire_parts().0).map_err(invalid)?.ut_offset(),
            )
            .map_err(invalid),
        }
    }
    pub fn local_datetime(self) -> Result<Datetime> {
        let (seconds, nanos) = self.utc.wire_parts();
        let offset = self.offset()?.whole_seconds();
        // Resolve the offset at the UTC instant, never guess across DST folds/gaps.
        let leap = nanos >= 1_000_000_000;
        let aligned = offset % 60 == 0;
        let local = UtcDateTime::from_timespec(
            seconds + i64::from(offset) + i64::from(leap && !aligned),
            nanos % 1_000_000_000,
        )
        .map_err(invalid)?;
        UtcDateTime::new(
            local.year(),
            local.month(),
            local.month_day(),
            local.hour(),
            local.minute(),
            if leap && aligned { 60 } else { local.second() },
            local.nanoseconds(),
        )
        .map(Datetime)
        .map_err(invalid)
    }
    pub(crate) fn fmt_typeql(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        self.local_datetime().map_err(|_| fmt::Error)?.fmt_typeql(f)?;
        match self.zone {
            TimeZone::IANA(z) => write!(f, " {}", z.name()),
            TimeZone::Fixed(o) => {
                let secs = o.whole_seconds();
                let minutes = (secs.unsigned_abs() + 30) / 60;
                write!(f, "{}{:02}:{:02}", if secs < 0 { '-' } else { '+' }, minutes / 60, minutes % 60)
            }
        }
    }
}
impl fmt::Display for DatetimeTZ {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{} ", self.local_datetime().map_err(|_| fmt::Error)?)?;
        match self.zone {
            TimeZone::Fixed(o) => {
                let n = o.whole_seconds().unsigned_abs();
                write!(f, "{}{:02}:{:02}", if o.whole_seconds() < 0 { '-' } else { '+' }, n / 3600, n % 3600 / 60)?;
                if n % 60 != 0 {
                    write!(f, ":{:02}", n % 60)?;
                }
                Ok(())
            }
            TimeZone::IANA(z) => write!(
                f,
                "{}",
                z.zone().find_local_time_type(self.wire_parts().0).map_err(|_| fmt::Error)?.time_zone_designation()
            ),
        }
    }
}
impl fmt::Debug for DatetimeTZ {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        self.fmt_typeql(f)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::concept::Value;
    #[test]
    fn legacy_golden_vectors() {
        let vectors: serde_json::Value = serde_json::from_str(LEGACY_GOLDEN).unwrap();
        for v in vectors.as_array().unwrap() {
            let seconds = v["seconds"].as_i64().unwrap();
            let nanos = v["nanos"].as_u64().unwrap() as u32;
            let zone = if let Some(offset) = v["offset"].as_i64() {
                TimeZone::fixed(offset as i32).unwrap()
            } else {
                TimeZone::iana(v["zone"].as_str().unwrap()).unwrap()
            };
            let utc = Datetime::from_timestamp(seconds, nanos).unwrap();
            let dt = zone.from_utc_datetime(&utc).unwrap();
            assert_eq!(dt.wire_parts(), (seconds, nanos));
            assert_eq!(Value::DatetimeTZ(dt).to_string(), v["display"].as_str().unwrap(), "{v}");
            assert_eq!(dt.to_string(), v["debug"].as_str().unwrap(), "{v}");
            assert_eq!(Value::Datetime(dt.local_datetime().unwrap()).to_string(), v["local"].as_str().unwrap(), "{v}");
        }
    }
    #[test]
    fn folds_gaps_and_zone_identity() {
        let zone = TimeZone::iana("America/New_York").unwrap();
        let fold: Datetime = "2024-11-03T01:30:00".parse().unwrap();
        let values = zone.local_candidates(fold).unwrap();
        assert_eq!(values.len(), 2);
        assert_eq!(values[1].wire_parts().0 - values[0].wire_parts().0, 3600);
        assert!(zone.from_local_datetime(fold).is_err());
        assert!(zone.local_candidates("2024-03-10T02:30:00".parse().unwrap()).unwrap().is_empty());
        assert!(NamedTimeZone::get("america/new_york").is_err());
        assert_eq!(NamedTimeZone::get_case_insensitive("america/new_york").unwrap().name(), "America/New_York");
        let alias = TimeZone::iana("US/Eastern").unwrap();
        assert_eq!(alias.from_utc_datetime(&values[0].naive_utc()).unwrap(), values[0]);
        assert_ne!(alias, zone);
    }
    #[test]
    fn range_precision_and_invalid_values() {
        for year in [-262143, -1, 0, 9999, 10000, 262142] {
            let d = Datetime::new(year, 1, 1, 0, 0, 0, 123456789).unwrap();
            assert_eq!(Datetime::from_timestamp(d.wire_parts().0, d.wire_parts().1).unwrap(), d);
        }
        assert!(Datetime::new(262143, 1, 1, 0, 0, 0, 0).is_err());
        for (s, n) in [(0, 1_000_000_000), (59, 2_000_000_000), (i64::MAX, 0), (i64::MIN, 0)] {
            assert!(Datetime::from_timestamp(s, n).is_err());
        }
        let leap: Datetime = "2016-12-31T23:59:60.123456789".parse().unwrap();
        assert_eq!(leap.wire_parts(), (1483228799, 1_123_456_789));
        assert!("2016-12-31T23:59:60.1234567890".parse::<Datetime>().is_err());
        assert_eq!(TimeZone::fixed(0).unwrap().from_local_datetime(leap).unwrap().naive_utc(), leap);
    }
}

#[cfg(test)]
include!("temporal_golden.rs");
