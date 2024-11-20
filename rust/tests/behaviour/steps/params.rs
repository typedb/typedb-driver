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

use std::{borrow::Borrow, convert::Infallible, fmt, str::FromStr};

use chrono::{FixedOffset, NaiveDate, NaiveDateTime, NaiveTime};
use cucumber::Parameter;
use typedb_driver::{
    answer::QueryType as TypeDBQueryType,
    concept::{Value as TypeDBValue, ValueType as TypeDBValueType},
    TransactionType as TypeDBTransactionType,
};

#[derive(Debug, Parameter)]
#[param(name = "containment", regex = r"(?:do not )?contain")]
pub struct ContainmentParam(bool);

impl ContainmentParam {
    pub fn assert<T, U>(&self, actuals: &[T], item: U)
    where
        T: Comparable<U> + fmt::Debug,
        U: PartialEq + fmt::Debug,
    {
        if self.0 {
            assert!(actuals.iter().any(|actual| actual.equals(&item)), "{item:?} not found in {actuals:?}")
        } else {
            assert!(actuals.iter().all(|actual| !actual.equals(&item)), "{item:?} found in {actuals:?}")
        }
    }
}

impl FromStr for ContainmentParam {
    type Err = Infallible;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(Self(s == "contain"))
    }
}

pub trait Comparable<U: ?Sized> {
    fn equals(&self, item: &U) -> bool;
}

impl<T: Borrow<U>, U: PartialEq + ?Sized> Comparable<&U> for T {
    fn equals(&self, item: &&U) -> bool {
        self.borrow() == *item
    }
}

impl<'a, T1, T2, U1, U2> Comparable<(&'a U1, &'a U2)> for (T1, T2)
where
    T1: Comparable<&'a U1>,
    T2: Comparable<&'a U2>,
{
    fn equals(&self, (first, second): &(&'a U1, &'a U2)) -> bool {
        self.0.equals(first) && self.1.equals(second)
    }
}

#[derive(Debug, Default, Parameter, Clone)]
#[param(name = "value", regex = ".*?")]
pub(crate) struct Value {
    raw_value: String,
}

impl Value {
    const DATETIME_FORMATS: [&'static str; 8] = [
        "%Y-%m-%dT%H:%M:%S%.9f",
        "%Y-%m-%d %H:%M:%S%.9f",
        "%Y-%m-%dT%H:%M:%S",
        "%Y-%m-%d %H:%M:%S",
        "%Y-%m-%dT%H:%M",
        "%Y-%m-%d %H:%M",
        "%Y-%m-%dT%H",
        "%Y-%m-%d %H",
    ];
    const DATE_FORMAT: &'static str = "%Y-%m-%d";

    const FRACTIONAL_ZEROES: usize = 19;

    pub fn into_typedb(self, value_type: TypeDBValueType) -> TypeDBValue {
        match value_type {
            TypeDBValueType::Boolean => TypeDBValue::Boolean(self.raw_value.parse().unwrap()),
            TypeDBValueType::Long => TypeDBValue::Long(self.raw_value.parse().unwrap()),
            TypeDBValueType::Double => TypeDBValue::Double(self.raw_value.parse().unwrap()),
            TypeDBValueType::Decimal => {
                let (integer, fractional) = if let Some(split) = self.raw_value.split_once(".") {
                    split
                } else {
                    (self.raw_value.as_str(), "0")
                };

                let integer_parsed: i64 = integer.trim().parse().unwrap();
                let integer_parsed_abs = integer_parsed.abs();
                let fractional_parsed = Self::parse_decimal_fraction_part(fractional);

                TypeDBValue::Decimal(match integer.starts_with('-') {
                    false => Decimal::new(integer_parsed_abs, fractional_parsed),
                    true => -Decimal::new(integer_parsed_abs, fractional_parsed),
                })
            }
            TypeDBValueType::Date => {
                TypeDBValue::Date(NaiveDate::parse_from_str(&self.raw_value, Self::DATE_FORMAT).unwrap())
            }
            TypeDBValueType::Datetime => {
                let (datetime, remainder) = Self::parse_date_time_and_remainder(self.raw_value.as_str());
                assert!(
                    remainder.is_empty(),
                    "Unexpected remainder when parsing {:?} with result of {:?}",
                    self.raw_value,
                    datetime
                );
                TypeDBValue::Datetime(datetime)
            }
            TypeDBValueType::DatetimeTZ => {
                let (datetime, timezone) = Self::parse_date_time_and_remainder(self.raw_value.as_str());

                if timezone.is_empty() {
                    TypeDBValue::DatetimeTZ(datetime.and_local_timezone(TimeZone::default()).unwrap())
                } else if timezone.starts_with('+') || timezone.starts_with('-') {
                    let hours: i32 = timezone[1..3].parse().unwrap();
                    let minutes: i32 = timezone[3..].parse().unwrap();
                    let total_minutes = hours * 60 + minutes;
                    let fixed_offset = if &timezone[0..1] == "+" {
                        FixedOffset::east_opt(total_minutes * 60)
                    } else {
                        FixedOffset::west_opt(total_minutes * 60)
                    };
                    TypeDBValue::DatetimeTZ(
                        datetime.and_local_timezone(TimeZone::Fixed(fixed_offset.unwrap())).unwrap(),
                    )
                } else {
                    TypeDBValue::DatetimeTZ(
                        datetime.and_local_timezone(TimeZone::IANA(timezone.parse().unwrap())).unwrap(),
                    )
                }
            }
            TypeDBValueType::Duration => TypeDBValue::Duration(self.raw_value.parse().unwrap()),
            TypeDBValueType::String => TypeDBValue::String(Self::remove_string_quotes_and_unescape(&self.raw_value)),
            TypeDBValueType::Struct(_) => {
                // Compare string representations
                TypeDBValue::String(Self::remove_string_quotes_and_unescape(&self.raw_value))
            }
        }
    }

    fn remove_string_quotes_and_unescape(string: &str) -> String {
        let unescaped = string.replace("\\\"", "\"");
        if unescaped.starts_with('"') && unescaped.ends_with('"') {
            unescaped[1..&unescaped.len() - 1].to_owned()
        } else {
            unescaped
        }
    }

    fn parse_decimal_fraction_part(value: &str) -> u64 {
        assert!(Self::FRACTIONAL_ZEROES >= value.len());
        10_u64.pow((Self::FRACTIONAL_ZEROES - value.len()) as u32) * value.trim().parse::<u64>().unwrap()
    }

    fn parse_date_time_and_remainder(value: &str) -> (NaiveDateTime, &str) {
        for format in Self::DATETIME_FORMATS {
            if let Ok((datetime, remainder)) = NaiveDateTime::parse_and_remainder(value, format) {
                return (datetime, remainder.trim());
            }
        }
        if let Ok((date, remainder)) = NaiveDate::parse_and_remainder(value, Self::DATE_FORMAT) {
            return (date.and_time(NaiveTime::default()), remainder.trim());
        }
        panic!(
            "Cannot parse DateTime: none of the formats {:?} or {:?} fits for {:?}",
            Self::DATETIME_FORMATS,
            Self::DATE_FORMAT,
            value
        )
    }
}

impl FromStr for Value {
    type Err = Infallible;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(Self { raw_value: s.to_owned() })
    }
}

#[derive(Clone, Debug, Parameter)]
#[param(name = "value_type", regex = r"boolean|long|double|decimal|string|date|datetime|datetime-tz|duration|struct")]
pub struct ValueType {
    pub value_type: TypeDBValueType,
}

impl FromStr for ValueType {
    type Err = Infallible;

    fn from_str(type_: &str) -> Result<Self, Self::Err> {
        Ok(match type_ {
            "boolean" => Self { value_type: TypeDBValueType::Boolean },
            "long" => Self { value_type: TypeDBValueType::Long },
            "double" => Self { value_type: TypeDBValueType::Double },
            "decimal" => Self { value_type: TypeDBValueType::Decimal },
            "string" => Self { value_type: TypeDBValueType::String },
            "date" => Self { value_type: TypeDBValueType::Date },
            "datetime" => Self { value_type: TypeDBValueType::Datetime },
            "datetime-tz" => Self { value_type: TypeDBValueType::DatetimeTZ },
            "duration" => Self { value_type: TypeDBValueType::Duration },
            "struct" => Self { value_type: TypeDBValueType::Struct("unknown".to_string()) },
            _ => unreachable!("`{type_}` is not a valid value type"),
        })
    }
}

#[derive(Clone, Copy, Debug, Parameter)]
#[param(name = "transaction_type", regex = r"write|read|schema")]
pub struct TransactionType {
    pub transaction_type: TypeDBTransactionType,
}

impl FromStr for TransactionType {
    type Err = Infallible;

    fn from_str(type_: &str) -> Result<Self, Self::Err> {
        Ok(match type_ {
            "write" => Self { transaction_type: TypeDBTransactionType::Write },
            "read" => Self { transaction_type: TypeDBTransactionType::Read },
            "schema" => Self { transaction_type: TypeDBTransactionType::Schema },
            _ => unreachable!("`{type_}` is not a valid transaction type"),
        })
    }
}

#[derive(Clone, Copy, Debug, Parameter)]
#[param(name = "query_type", regex = r"write|read|schema")]
pub struct QueryType {
    pub query_type: TypeDBQueryType,
}

impl FromStr for QueryType {
    type Err = Infallible;

    fn from_str(type_: &str) -> Result<Self, Self::Err> {
        Ok(match type_ {
            "write" => Self { query_type: TypeDBQueryType::WriteQuery },
            "read" => Self { query_type: TypeDBQueryType::ReadQuery },
            "schema" => Self { query_type: TypeDBQueryType::SchemaQuery },
            _ => unreachable!("`{type_}` is not a valid query type"),
        })
    }
}

#[derive(Clone, Debug, Default, Parameter)]
#[param(name = "var", regex = r".*")]
pub struct Var {
    pub name: String,
}

impl FromStr for Var {
    type Err = Infallible;

    fn from_str(name: &str) -> Result<Self, Self::Err> {
        Ok(Self { name: name.to_owned() })
    }
}

#[derive(Debug, Parameter)]
#[param(name = "boolean", regex = "(true|false)")]
pub(crate) enum Boolean {
    False,
    True,
}

macro_rules! check_boolean {
    ($boolean:ident, $expr:expr) => {
        match $boolean {
            $crate::params::Boolean::True => assert!($expr),
            $crate::params::Boolean::False => assert!(!$expr),
        }
    };
}
pub(crate) use check_boolean;
use typedb_driver::concept::{
    value::{Decimal, TimeZone},
    Concept,
};

impl FromStr for Boolean {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            "true" => Self::True,
            "false" => Self::False,
            invalid => return Err(format!("Invalid `Boolean`: {invalid}")),
        })
    }
}

#[derive(Debug, Clone, Parameter)]
#[param(name = "may_error", regex = "(|; fails|; parsing fails|; fails with a message containing: \".*\")")]
pub(crate) enum MayError {
    False,
    True(Option<String>),
}

impl MayError {
    pub fn check<T: fmt::Debug, E: fmt::Debug + ToString>(&self, res: Result<T, E>) -> Option<E> {
        match self {
            MayError::False => {
                res.unwrap();
                None
            }
            MayError::True(None) => Some(res.unwrap_err()),
            MayError::True(Some(expected_message)) => {
                let actual_error = res.unwrap_err();
                let actual_message = actual_error.to_string();

                if actual_message.contains(expected_message) {
                    Some(actual_error)
                } else {
                    panic!(
                        "Expected error message containing: '{}', but got error message: '{}'",
                        expected_message, actual_message
                    );
                }
            }
        }
    }

    pub fn expects_error(&self) -> bool {
        match self {
            MayError::True(_) => true,
            MayError::False => false,
        }
    }
}

impl FromStr for MayError {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        if s.is_empty() {
            Ok(MayError::False)
        } else if s == "; fails" || s == "; parsing fails" {
            Ok(MayError::True(None))
        } else if let Some(message) =
            s.strip_prefix("; fails with a message containing: \"").and_then(|suffix| suffix.strip_suffix("\""))
        {
            Ok(MayError::True(Some(message.to_string())))
        } else {
            Err(format!("Invalid `MayError`: {}", s))
        }
    }
}

#[derive(Debug, Parameter)]
#[param(name = "is_or_not", regex = "(is|is not)")]
pub(crate) enum IsOrNot {
    Is,
    IsNot,
}

impl IsOrNot {
    pub fn check(&self, real_is: bool) {
        match self {
            Self::Is => {
                assert!(real_is)
            }
            Self::IsNot => {
                assert!(!real_is)
            }
        };
    }

    pub fn compare<T: PartialEq + fmt::Debug>(&self, left: T, right: T) {
        match self {
            Self::Is => {
                assert_eq!(left, right, "Expected '{left:?}' is '{right:?}'")
            }
            Self::IsNot => {
                assert_ne!(left, right, "Expected '{left:?}' is not '{right:?}'")
            }
        };
    }
}

impl FromStr for IsOrNot {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            "is" => Self::Is,
            "is not" => Self::IsNot,
            invalid => return Err(format!("Invalid `IsOrNot`: {invalid}")),
        })
    }
}

#[derive(Debug, Parameter)]
#[param(name = "contains_or_doesnt", regex = "(contains|does not contain)")]
pub(crate) enum ContainsOrDoesnt {
    Contains,
    DoesNotContain,
}

impl ContainsOrDoesnt {
    pub fn check<T: fmt::Debug>(&self, scrutinee: &Option<T>, message: &str) {
        match (self, scrutinee) {
            (Self::Contains, Some(_)) | (Self::DoesNotContain, None) => (),
            (Self::Contains, None) => panic!("Expected to contain, not found: {message}"),
            (Self::DoesNotContain, Some(value)) => panic!("Expected not to contain, {value:?} is found: {message}"),
        }
    }

    pub fn check_result<T: fmt::Debug, E>(&self, scrutinee: &Result<T, E>, message: &str) {
        let option = match scrutinee {
            Ok(result) => Some(result),
            Err(_) => None,
        };
        self.check(&option, message)
    }

    pub fn check_bool(&self, contains: bool, message: &str) {
        match self {
            ContainsOrDoesnt::Contains => assert!(contains, "Expected to contain, not found: {message}"),
            ContainsOrDoesnt::DoesNotContain => assert!(!contains, "Expected not to contain, but found: {message}"),
        }
    }
}

impl FromStr for ContainsOrDoesnt {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            "contains" => Self::Contains,
            "does not contain" => Self::DoesNotContain,
            invalid => return Err(format!("Invalid `ContainsOrDoesnt`: {invalid}")),
        })
    }
}

#[derive(Debug, Parameter)]
#[param(name = "is_by_var_index", regex = "(| by index of variable)")]
pub(crate) enum IsByVarIndex {
    Is,
    IsNot,
}

impl FromStr for IsByVarIndex {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            " by index of variable" => Self::Is,
            "" => Self::IsNot,
            invalid => return Err(format!("Invalid `IsByVarIndex`: {invalid}")),
        })
    }
}

#[derive(Debug, Clone, Copy, Parameter)]
#[param(name = "query_answer_type", regex = "(ok|concept rows|concept documents)")]
pub(crate) enum QueryAnswerType {
    Ok,
    ConceptRows,
    ConceptDocuments,
}

impl FromStr for QueryAnswerType {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            "ok" => Self::Ok,
            "concept rows" => Self::ConceptRows,
            "concept documents" => Self::ConceptDocuments,
            invalid => return Err(format!("Invalid `QueryAnswerType`: {invalid}")),
        })
    }
}

impl fmt::Display for QueryAnswerType {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            QueryAnswerType::Ok => write!(f, "Ok"),
            QueryAnswerType::ConceptRows => write!(f, "ConceptRows"),
            QueryAnswerType::ConceptDocuments => write!(f, "ConceptTrees"),
        }
    }
}

#[derive(Debug, Parameter)]
#[param(
    name = "concept_kind",
    regex = "(concept|variable|type|instance|entity type|relation type|attribute type|role type|entity|relation|attribute|value)"
)]
pub(crate) enum ConceptKind {
    Concept,
    Type,
    Instance,
    EntityType,
    RelationType,
    AttributeType,
    RoleType,
    Entity,
    Relation,
    Attribute,
    Value,
}

impl ConceptKind {
    pub(crate) fn matches_concept(&self, concept: &Concept) -> bool {
        match self {
            ConceptKind::Concept => true,
            ConceptKind::Type => match concept {
                Concept::EntityType(_)
                | Concept::RelationType(_)
                | Concept::AttributeType(_)
                | Concept::RoleType(_) => true,
                _ => false,
            },
            ConceptKind::Instance => match concept {
                Concept::Entity(_) | Concept::Relation(_) | Concept::Attribute(_) => true,
                _ => false,
            },
            ConceptKind::EntityType => matches!(concept, Concept::EntityType(_)),
            ConceptKind::RelationType => matches!(concept, Concept::RelationType(_)),
            ConceptKind::AttributeType => matches!(concept, Concept::AttributeType(_)),
            ConceptKind::RoleType => matches!(concept, Concept::RoleType(_)),
            ConceptKind::Entity => matches!(concept, Concept::Entity(_)),
            ConceptKind::Relation => matches!(concept, Concept::Relation(_)),
            ConceptKind::Attribute => matches!(concept, Concept::Attribute(_)),
            ConceptKind::Value => matches!(concept, Concept::Value(_)),
        }
    }
}

impl FromStr for ConceptKind {
    type Err = String;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        Ok(match s {
            "concept" | "variable" => Self::Concept,
            "type" => Self::Type,
            "instance" => Self::Instance,
            "entity type" => Self::EntityType,
            "relation type" => Self::RelationType,
            "attribute type" => Self::AttributeType,
            "role type" => Self::RoleType,
            "entity" => Self::Entity,
            "relation" => Self::Relation,
            "attribute" => Self::Attribute,
            "value" => Self::Value,
            invalid => return Err(format!("Invalid `ConceptKind`: {invalid}")),
        })
    }
}

impl fmt::Display for ConceptKind {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::Concept => write!(f, "Concept"),
            Self::Type => write!(f, "Type"),
            Self::Instance => write!(f, "Instance"),
            Self::EntityType => write!(f, "EntityType"),
            Self::RelationType => write!(f, "RelationType"),
            Self::AttributeType => write!(f, "AttributeType"),
            Self::RoleType => write!(f, "RoleType"),
            Self::Entity => write!(f, "Entity"),
            Self::Relation => write!(f, "Relation"),
            Self::Attribute => write!(f, "Attribute"),
            Self::Value => write!(f, "Value"),
        }
    }
}
