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

use super::ValueType;

/// Annotations are used to specify extra schema constraints.
#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum Annotation {
    Key,
    Unique,
}

/// Entity types represent the classification of independent objects in the data model
/// of the business domain.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct EntityType {
    pub label: String,
}

impl EntityType {
    /// Retrieves the unique label of the `EntityType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.label()
    /// ```
    pub fn label(&self) -> &str {
        &self.label
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
}

impl RelationType {
    /// Retrieves the unique label of the `RelationType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.label()
    /// ```
    pub fn label(&self) -> &str {
        &self.label
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
    pub value_type: Option<ValueType>,
}

impl AttributeType {
    /// Retrieves the unique label of the `AttributeType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.label()
    /// ```
    pub fn label(&self) -> &str {
        &self.label
    }

    /// Retrieves the `ValueType` of the `AttributeType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.value_type()
    /// ```
    pub fn value_type(&self) -> Option<&ValueType> {
        self.value_type.as_ref()
    }
}

/// Roles are special internal types used by relations. We can not create an instance
/// of a role in a database. But we can set an instance of another type (role player)
/// to play a role in a particular instance of a relation type.
///
/// Roles allow a schema to enforce logical constraints on types of role players.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RoleType {
    pub label: String,
}

impl RoleType {
    /// Retrieves the unique label of the `RoleType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.label()
    /// ```
    pub fn label(&self) -> &str {
        &self.label
    }
}
