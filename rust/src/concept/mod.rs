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

use chrono::NaiveDateTime;

use crate::IID;

pub use self::{
    thing::{Attribute, Entity, Relation, Thing},
    type_::{Annotation, AttributeType, EntityType, RelationType, RoleType, ScopedLabel, ThingType},
    value::{Value, ValueType},
};

pub mod thing;
pub mod type_;
pub mod value;

/// The fundamental TypeQL object. A Concept is either a Type, Thing, or Value.
/// To use subtype specific methods, the Concept must be of the expected subtype.
#[derive(Clone, Debug, PartialEq)]
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

impl Concept {
    pub fn get_iid(&self) -> Option<&IID> {
        match self {
            Concept::Entity(entity) => Some(&entity.iid),
            Concept::Relation(relation) => Some(&relation.iid),
            _ => None,
        }
    }

    pub fn get_type(&self) -> Option<&str> {
        match self {
            Concept::Entity(entity) => Some(&entity.type_.as_ref().unwrap().label),
            Concept::Relation(relation) => Some(&relation.type_.as_ref().unwrap().label),
            Concept::Attribute(attribute) => Some(&attribute.type_.as_ref().unwrap().label),
            _ => None
        }
    }

    pub fn get_variant(&self) -> &str {
        match self {
            Concept::EntityType(_) => "EntityType",
            Concept::RelationType(_) => "RelationType",
            Concept::RoleType(_) => "RoleType",
            Concept::AttributeType(_) => "AttributeType",
            Concept::Entity(_) => "Entity",
            Concept::Relation(_) => "Relation",
            Concept::Attribute(_) => "Attribute",
            Concept::Value(_) => "Value",
        }
    }

    pub fn get_value_type(&self) -> Option<ValueType> {
        match self {
            Concept::Attribute(attribute) => Some(attribute.value.get_type()),
            Concept::Value(value) => Some(value.get_type()),
            _ => None,
        }
    }

    pub fn get_value(&self) -> Option<&Value> {
        match self {
            Concept::Attribute(attribute) => Some(&attribute.value),
            Concept::Value(value) => Some(value),
            _ => None
        }
    }

    pub fn get_boolean(&self) -> Option<bool> {
        self.get_value().map(|value| value.get_boolean()).flatten()
    }

    pub fn get_long(&self) -> Option<i64> {
        self.get_value().map(|value| value.get_long()).flatten()
    }

    pub fn get_double(&self) -> Option<f64> {
        self.get_value().map(|value| value.get_double()).flatten()
    }

    pub fn get_string(&self) -> Option<&str> {
        self.get_value().map(|value| value.get_string()).flatten()
    }

    pub fn get_datetime(&self) -> Option<NaiveDateTime> {
        self.get_value().map(|value| value.get_datetime()).flatten()
    }
}

/// Represents invalid schema constructs discovered during schema validation.
#[derive(Clone, Debug)]
pub struct SchemaException {
    pub code: String,
    pub message: String,
}
