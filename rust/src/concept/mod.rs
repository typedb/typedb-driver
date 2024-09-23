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

use std::fmt::{Debug, Display, Formatter};

use chrono::{DateTime, NaiveDate, NaiveDateTime};
use chrono_tz::Tz;

pub use self::{
    thing::{Attribute, Entity, Relation},
    type_::{Annotation, AttributeType, EntityType, RelationType, RoleType},
    value::{Value, ValueType},
};
use crate::{
    concept::value::{Decimal, Duration},
    IID,
};

pub mod thing;
pub mod type_;
pub mod value;

/// The fundamental TypeQL object.
#[derive(Clone, PartialEq)]
pub enum Concept {
    EntityType(EntityType),
    RelationType(RelationType),
    RoleType(RoleType),
    AttributeType(AttributeType),

    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),

    Value(Value),
}

pub enum ConceptCategory {
    EntityType,
    RelationType,
    RoleType,
    AttributeType,
    Entity,
    Relation,
    Attribute,
    Value,
}

impl ConceptCategory {
    pub const fn name(&self) -> &'static str {
        match self {
            ConceptCategory::EntityType => "EntityType",
            ConceptCategory::RelationType => "RelationType",
            ConceptCategory::RoleType => "RoleType",
            ConceptCategory::AttributeType => "AttriuteType",
            ConceptCategory::Entity => "Entity",
            ConceptCategory::Relation => "Relation",
            ConceptCategory::Attribute => "Attribute",
            ConceptCategory::Value => "Value",
        }
    }
}

impl Display for ConceptCategory {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(self, f)
    }
}

impl Debug for ConceptCategory {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}", self.name())
    }
}

impl Concept {
    /// Get the IID of this concept, if it exists.
    /// If this is an Entity or Relation Instance, return the IID of the instance.
    /// Otherwise, return empty
    pub fn get_iid(&self) -> Option<&IID> {
        match self {
            Self::Entity(entity) => Some(&entity.iid),
            Self::Relation(relation) => Some(&relation.iid),
            _ => None,
        }
    }

    /// Get the label of the concept.
    /// If this is an Instance, return the label of the type of this instance.
    /// If this is a Value, return the label of the value type of the value.
    /// If this is a Type, return the label of the type.
    fn get_label(&self) -> &str {
        match self {
            Self::EntityType(entity_type) => entity_type.label(),
            Self::RelationType(relation_type) => relation_type.label(),
            Self::AttributeType(attribute_type) => attribute_type.label(),
            Self::RoleType(role_type) => role_type.label(),
            Self::Entity(entity) => entity.type_().map(|type_| type_.label()).unwrap(),
            Self::Relation(relation) => relation.type_().map(|type_| type_.label()).unwrap(),
            Self::Attribute(attribute) => attribute.type_().map(|type_| type_.label()).unwrap(),
            Self::Value(value) => value.get_type_name(),
        }
    }

    /// Get the label of the value type of the concept, if it exists.
    /// If this is an Attribute Instance, return the label of the value of this instance.
    /// If this is a Value, return the label of the value.
    /// If this is an Attribute Type, it returns the label of the value type that the schema permits for the attribute type, if one is defined.
    /// Otherwise, return empty.
    fn get_value_label(&self) -> Option<&str> {
        match self {
            Self::AttributeType(attribute_type) => attribute_type.value_type().map(|value_type| value_type.name()),
            Self::Attribute(attribute) => Some(attribute.value.get_type_name()),
            Self::Value(value) => Some(value.get_type_name()),
            _ => None,
        }
    }

    /// Get the value type enum of the concept, if it exists
    /// If this is an Attribute Instance, return the value type of the value of this instance.
    /// If this is a Value, return the value type of the value.
    /// If this is an Attribute Type, it returns value type that the schema permits for the attribute type, if one is defined.
    /// Otherwise, return empty.
    pub fn get_value_type(&self) -> Option<ValueType> {
        match self {
            Self::AttributeType(attribute_type) => attribute_type.value_type().cloned(),
            Self::Attribute(attribute) => Some(attribute.value.get_type()),
            Self::Value(value) => Some(value.get_type()),
            _ => None,
        }
    }

    /// Get the value of this concept, if it exists.
    /// If this is an Attribute Instance, return the value of this instance.
    /// If this a Value, return the value.
    /// Otherwise, return empty.
    pub fn get_value(&self) -> Option<&Value> {
        match self {
            Self::Attribute(attribute) => Some(&attribute.value),
            Self::Value(value) => Some(value),
            _ => None,
        }
    }

    /// Get the boolean value of this concept, if it exists.
    /// If this is a boolean-valued Attribute Instance, return the boolean value of this instance.
    /// If this a boolean-valued Value, return the boolean value.
    /// Otherwise, return empty.
    pub fn get_boolean(&self) -> Option<bool> {
        self.get_value().map(|value| value.get_boolean()).flatten()
    }

    /// Get the long value of this concept, if it exists.
    /// If this is a long-valued Attribute Instance, return the long value of this instance.
    /// If this a long-valued Value, return the long value.
    /// Otherwise, return empty.
    pub fn get_long(&self) -> Option<i64> {
        self.get_value().map(|value| value.get_long()).flatten()
    }

    /// Get the double value of this concept, if it exists.
    /// If this is a double-valued Attribute Instance, return the double value of this instance.
    /// If this a double-valued Value, return the double value.
    /// Otherwise, return empty.
    pub fn get_double(&self) -> Option<f64> {
        self.get_value().map(|value| value.get_double()).flatten()
    }

    /// Get the fixed-decimal value of this concept, if it exists.
    /// If this is a fixed-decimal valued Attribute Instance, return the fixed-decimal value of this instance.
    /// If this a fixed-decimal valued Value, return the fixed-decimal value.
    /// Otherwise, return empty.
    pub fn get_decimal(&self) -> Option<Decimal> {
        self.get_value().map(|value| value.get_decimal()).flatten()
    }

    /// Get the string value of this concept, if it exists.
    /// If this is a string-valued Attribute Instance, return the string value of this instance.
    /// If this a string-valued Value, return the string value.
    /// Otherwise, return empty.
    pub fn get_string(&self) -> Option<&str> {
        self.get_value().map(|value| value.get_string()).flatten()
    }

    /// Get the date value of this concept, if it exists.
    /// If this is a date-valued Attribute Instance, return the date value of this instance.
    /// If this a date-valued Value, return the date value.
    /// Otherwise, return empty.
    pub fn get_date(&self) -> Option<NaiveDate> {
        self.get_value().map(|value| value.get_date()).flatten()
    }

    /// Get the datetime value of this concept, if it exists.
    /// If this is a datetime-valued Attribute Instance, return the datetime value of this instance.
    /// If this a datetime-valued Value, return the datetime value.
    /// Otherwise, return empty.
    pub fn get_datetime(&self) -> Option<NaiveDateTime> {
        self.get_value().map(|value| value.get_datetime()).flatten()
    }

    /// Get the timezoned-datetime value of this concept, if it exists.
    /// If this is a timezoned-datetime valued Attribute Instance, return the timezoned-datetime value of this instance.
    /// If this a timezoned-datetime valued Value, return the timezoned-datetime value.
    /// Otherwise, return empty.
    pub fn get_datetime_tz(&self) -> Option<DateTime<Tz>> {
        self.get_value().map(|value| value.get_datetime_tz()).flatten()
    }

    /// Get the duration value of this concept, if it exists.
    /// If this is a duration-valued Attribute Instance, return the duration value of this instance.
    /// If this a duration-valued Value, return the duration value.
    /// Otherwise, return empty.
    pub fn get_duration(&self) -> Option<Duration> {
        self.get_value().map(|value| value.get_duration()).flatten()
    }

    /// Get the category of this concept
    pub fn get_category(&self) -> ConceptCategory {
        match self {
            Self::EntityType(_) => ConceptCategory::EntityType,
            Self::RelationType(_) => ConceptCategory::RelationType,
            Self::RoleType(_) => ConceptCategory::RoleType,
            Self::AttributeType(_) => ConceptCategory::AttributeType,
            Self::Entity(_) => ConceptCategory::Entity,
            Self::Relation(_) => ConceptCategory::Relation,
            Self::Attribute(_) => ConceptCategory::Attribute,
            Self::Value(_) => ConceptCategory::Value,
        }
    }

    /// Check if this concept represents a Type from the schema of the database.
    /// These are exactly: Entity Types, Relation Types, Role Types, and Attribute Types
    ///
    /// Equivalent to:
    /// ```
    /// concept.is_entity_type() || concept.is_relation_type() || concept.is_role_type() || concept.is_attribute_type()
    /// ```
    pub fn is_type(&self) -> bool {
        match self {
            Self::EntityType(_) | Self::RelationType(_) | Self::RoleType(_) | Self::AttributeType(_) => true,
            _ => false,
        }
    }

    /// Check if this concept represents an Entity Type from the schema of the database
    pub fn is_entity_type(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::EntityType)
    }

    /// Check if this concept represents a Relation Type from the schema of the database
    pub fn is_relation_type(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::RelationType)
    }

    /// Check if this concept represents a Role Type from the schema of the database
    pub fn is_role_type(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::RoleType)
    }

    /// Check if this concept represents an Attribute Type from the schema of the database
    pub fn is_attribute_type(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::AttributeType)
    }

    /// Check if this concept represents a stored database instance from the database.
    /// These are exactly: Entity, Relation, and Attribute
    ///
    /// Equivalent to:
    /// ```
    /// concept.is_entity() || concept.is_relation() ||  concept.is_attribute()
    /// ```
    pub fn is_instance(&self) -> bool {
        match self {
            Self::Entity(_) | Self::Relation(_) | Self::Attribute(_) => true,
            _ => false,
        }
    }

    /// Check if this concept represents an Entity instance from the database
    pub fn is_entity(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::Entity)
    }

    /// Check if this concept represents an Relation instance from the database
    pub fn is_relation(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::Relation)
    }

    /// Check if this concept represents an Attribute instance from the database
    pub fn is_attribute(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::Attribute)
    }

    /// Check if this concept represents a Value returned by the database
    pub fn is_value(&self) -> bool {
        matches!(self.get_category(), ConceptCategory::Value)
    }

    /// Check if this concept holds a boolean, either in an Attribute or as a Value
    pub fn is_boolean(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Boolean))
    }

    /// Check if this concept holds a long, either in an Attribute or as a Value
    pub fn is_long(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Long))
    }

    /// Check if this concept holds a fixed-decimal, either in an Attribute or as a Value
    pub fn is_decimal(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Decimal))
    }

    /// Check if this concept holds a double, either in an Attribute or as a Value
    pub fn is_double(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Double))
    }

    /// Check if this concept holds a string, either in an Attribute or as a Value
    pub fn is_string(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::String))
    }

    /// Check if this concept holds a date, either in an Attribute or as a Value
    pub fn is_date(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Date))
    }

    /// Check if this concept holds a datetime, either in an Attribute or as a Value
    pub fn is_datetime(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Datetime))
    }

    /// Check if this concept holds a timezoned-datetime, either in an Attribute or as a Value
    pub fn is_datetime_tz(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::DatetimeTZ))
    }

    /// Check if this concept holds a duration, either in an Attribute or as a Value
    pub fn is_duration(&self) -> bool {
        matches!(self.get_value_type(), Some(ValueType::Duration))
    }
}

impl Display for Concept {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(self, f)
    }
}

impl Debug for Concept {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        if self.is_type() {
            write!(f, "{}({})", self.get_category(), self.get_label())
        } else {
            write!(f, "{}({}", self.get_category(), self.get_label())?;
            if self.get_iid().is_some() {
                write!(f, ": {}", self.get_iid().unwrap())?;
                if self.get_value().is_some() {
                    write!(f, ", {}", self.get_value().unwrap())?;
                }
            } else if self.get_value().is_some() {
                write!(f, ": {}", self.get_value().unwrap())?;
            } else {
                // shouldn't be reachable?
            }
            write!(f, ")")
        }
    }
}
