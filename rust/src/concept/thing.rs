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

use super::{AttributeType, EntityType, RelationType, Value};
use crate::common::IID;

// TODO: Storing the Type here is *extremely* inefficient; we could be effectively creating
//       1 million copies of the same data when matching concepts of homogeneous types
/// Instance of data of an entity type, representing a standalone object
/// that exists in the data model independently.
/// Entity does not have a value. It is usually addressed by its ownership over attribute instances
/// and/or roles played in relation instances.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Entity {
    /// The unique id of this Entity
    pub iid: IID,
    /// The label of the entity type this instance belongs to
    pub type_: Option<EntityType>,
}

impl Entity {
    /// Retrieves the unique id of the `Entity`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity.iid();
    /// ```
    pub fn iid(&self) -> &IID {
        &self.iid
    }

    pub fn type_(&self) -> Option<&EntityType> {
        self.type_.as_ref()
    }
}

/// Relation is an instance of a relation type and can be uniquely addressed by
/// a combination of its type, owned attributes and role players.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Relation {
    /// The unique id of this Relation
    pub iid: IID,
    /// The label of the relation type this instance belongs to
    pub type_: Option<RelationType>,
}

impl Relation {
    /// Retrieves the unique id of the `Relation`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation.iid();
    /// ```
    pub fn iid(&self) -> &IID {
        &self.iid
    }

    pub fn type_(&self) -> Option<&RelationType> {
        self.type_.as_ref()
    }
}

/// Attribute is an instance of the attribute type and has a value.
/// This value is fixed and unique for every given instance of the attribute type.
/// Attributes can be uniquely addressed by their type and value.
#[derive(Clone, Debug, PartialEq)]
pub struct Attribute {
    /// The unique id of this Attribute (internal use only)
    pub iid: IID,
    /// The (dataful) value of this attribute
    pub value: Value,
    /// The type which this Attribute belongs to
    /// The label of the attribute type this instance belongs to
    pub type_: Option<AttributeType>,
}

impl Attribute {
    pub fn type_(&self) -> Option<&AttributeType> {
        self.type_.as_ref()
    }
}
