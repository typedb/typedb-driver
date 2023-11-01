/*
 * Copyright (C) 2022 Vaticle
 *
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

use std::fmt;

use super::ValueType;

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum Annotation {
    Key,
    Unique,
}

#[derive(Clone, Debug, PartialEq)]
pub enum ThingType {
    RootThingType(RootThingType),
    EntityType(EntityType),
    RelationType(RelationType),
    AttributeType(AttributeType),
}

impl ThingType {
    /// Retrieves the unique label of the `ThingType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.label()
    /// ```
    pub fn label(&self) -> &str {
        match self {
            Self::RootThingType(_) => RootThingType::LABEL,
            Self::EntityType(entity_type) => &entity_type.label,
            Self::RelationType(relation_type) => &relation_type.label,
            Self::AttributeType(attribute_type) => &attribute_type.label,
        }
    }
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RootThingType;

impl RootThingType {
    pub(crate) const LABEL: &'static str = "thing";
}

/// Entity types represent the classification of independent objects in the data model
/// of the business domain.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct EntityType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
}

impl EntityType {
    pub(crate) const ROOT_LABEL: &'static str = "entity";

    /// Returns the root `EntityType`
    pub fn root() -> Self {
        Self { label: Self::ROOT_LABEL.to_owned(), is_root: true, is_abstract: true }
    }
}

/// Relation types (or subtypes of the relation root type) represent relationships between types.
/// Relation types have roles.
///
/// Other types can play roles in relations if it’s mentioned in their definition.
///
/// A relation type must specify at least one role.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RelationType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
}

impl RelationType {
    pub(crate) const ROOT_LABEL: &'static str = "relation";

    /// Returns the root `RelationType`
    pub fn root() -> Self {
        Self { label: Self::ROOT_LABEL.to_owned(), is_root: true, is_abstract: true }
    }
}

/// Attribute types represent properties that other types can own.
///
/// Attribute types have a value type. This value type is fixed and unique for every given instance
/// of the attribute type.
///
/// Other types can own an attribute type. That means that instances of these other types can own
/// an instance of this attribute type. This usually means that an object in our domain has
/// a property with the matching value.
///
/// Multiple types can own the same attribute type, and different instances of the same type
/// or different types can share ownership of the same attribute instance.
#[derive(Clone, Debug, PartialEq)]
pub struct AttributeType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
    pub value_type: ValueType,
}

impl AttributeType {
    pub(crate) const ROOT_LABEL: &'static str = "attribute";

    /// Returns the root `AttributeType`
    pub fn root() -> Self {
        Self { label: Self::ROOT_LABEL.to_owned(), is_root: true, is_abstract: true, value_type: ValueType::Object }
    }
}

/// Roles are special internal types used by relations. We can not create an instance
/// of a role in a database. But we can set an instance of another type (role player)
/// to play a role in a particular instance of a relation type.
///
/// Roles allow a schema to enforce logical constraints on types of role players.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RoleType {
    pub label: ScopedLabel,
    pub is_root: bool,
    pub is_abstract: bool,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct ScopedLabel {
    pub scope: String,
    pub name: String,
}

impl fmt::Display for ScopedLabel {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}:{}", self.scope, self.name)
    }
}
