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

use super::{AttributeType, EntityType, RelationType, Value};
use crate::common::IID;

#[derive(Clone, Debug, PartialEq)]
pub enum Thing {
    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),
}

impl Thing {
    pub fn iid(&self) -> &IID {
        match self {
            Self::Entity(entity) => &entity.iid,
            Self::Relation(relation) => &relation.iid,
            Self::Attribute(attribute) => &attribute.iid,
        }
    }
}

// TODO: Storing the Type here is *extremely* inefficient; we could be effectively creating
//       1 million copies of the same data when matching concepts of homogeneous types
/// Instance of data of an entity type, representing a standalone object
/// that exists in the data model independently.
/// Entity does not have a value. It is usually addressed by its ownership over attribute instances
/// and/or roles played in relation instances.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Entity {
    pub iid: IID,
    pub type_: EntityType,
    pub is_inferred: bool,
}

/// Relation is an instance of a relation type and can be uniquely addressed by
/// a combination of its type, owned attributes and role players.
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Relation {
    pub iid: IID,
    pub type_: RelationType,
    pub is_inferred: bool,
}

/// Attribute is an instance of the attribute type and has a value.
/// This value is fixed and unique for every given instance of the attribute type.
/// Attribute type can be uniquely addressed by its type and value.
#[derive(Clone, Debug, PartialEq)]
pub struct Attribute {
    pub iid: IID,
    pub type_: AttributeType,
    pub value: Value,
    pub is_inferred: bool,
}
