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

use std::{borrow::Borrow, convert::Infallible, fmt, ops::Not, str::FromStr};

use chrono::NaiveDateTime;
use cucumber::Parameter;
use typedb_driver::{
    concept::{Annotation, Value, ValueType},
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

#[derive(Clone, Debug, Parameter)]
#[param(name = "value", regex = r".+")]
pub struct ValueParam(String);

impl ValueParam {
    pub fn into_value(self, value_type: ValueType) -> Value {
        match value_type {
            ValueType::Boolean => Value::Boolean(self.0.parse().unwrap()),
            ValueType::Double => Value::Double(self.0.parse().unwrap()),
            ValueType::Long => Value::Long(self.0.parse().unwrap()),
            ValueType::String => Value::String(self.0),
            ValueType::Datetime => {
                Value::Datetime(NaiveDateTime::parse_from_str(&self.0, "%Y-%m-%d %H:%M:%S").unwrap())
            }
            ValueType::Decimal => {
                todo!()
            }
            ValueType::Date => {
                todo!()
            }
            ValueType::DatetimeTZ => {
                todo!()
            }
            ValueType::Duration => {
                todo!()
            }
            ValueType::Struct(_) => {
                todo!()
            }
        }
    }
}

impl FromStr for ValueParam {
    type Err = Infallible;

    fn from_str(value: &str) -> Result<Self, Self::Err> {
        Ok(Self(value.to_owned()))
    }
}

#[derive(Clone, Debug, Parameter)]
#[param(name = "value_type", regex = r"boolean|long|double|decimal|string|date|datetime|datetime_tz|duration|struct")] // TODO: Probably any string value instead of struct
pub struct ValueTypeParam {
    pub value_type: ValueType,
}

impl FromStr for ValueTypeParam {
    type Err = Infallible;

    fn from_str(type_: &str) -> Result<Self, Self::Err> {
        Ok(match type_ {
            "boolean" => Self { value_type: ValueType::Boolean },
            "long" => Self { value_type: ValueType::Long },
            "double" => Self { value_type: ValueType::Double },
            "decimal" => Self { value_type: ValueType::Decimal },
            "string" => Self { value_type: ValueType::String },
            "date" => Self { value_type: ValueType::Date },
            "datetime" => Self { value_type: ValueType::Datetime },
            "datetime-tz" => Self { value_type: ValueType::DatetimeTZ },
            "duration" => Self { value_type: ValueType::Duration },
            "struct" => Self { value_type: ValueType::Struct("idk todo".to_string()) }, // TODO: Decide what to do
            _ => unreachable!("`{type_}` is not a valid value type"),
        })
    }
}

#[derive(Clone, Debug, Parameter)]
#[param(
    name = "optional_value_type",
    regex = r" as\((boolean|long|double|decimal|string|date|datetime|datetime-tz|duration|struct)\)|()"
)]
pub struct OptionalAsValueTypeParam {
    pub value_type: Option<ValueType>,
}

impl FromStr for OptionalAsValueTypeParam {
    type Err = Infallible;

    fn from_str(type_: &str) -> Result<Self, Self::Err> {
        Ok(Self { value_type: type_.is_empty().not().then(|| type_.parse::<ValueTypeParam>().unwrap().value_type) })
    }
}

#[derive(Clone, Copy, Debug, Parameter)]
#[param(name = "transaction_type", regex = r"read|write|schema")]
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

#[derive(Clone, Debug, Default, Parameter)]
#[param(name = "var", regex = r"(\$[\w_-]+)")]
pub struct VarParam {
    pub name: String,
}

impl FromStr for VarParam {
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
        } else if let Some(message) = s.strip_prefix("; fails with a message containing: \"").and_then(|suffix| suffix.strip_suffix("\"")) {
            Ok(MayError::True(Some(message.to_string())))
        } else {
            Err(format!("Invalid `MayError`: {}", s))
        }
    }
}
