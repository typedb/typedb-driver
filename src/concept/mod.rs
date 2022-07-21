/*
 * Copyright (C) 2021 Vaticle
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

#![allow(dead_code)]
#![allow(unused_variables)]

use std::fmt::{Debug, Formatter};
use std::time::Instant;
use typedb_protocol::concept::{Concept_oneof_concept, Type_Encoding};
use crate::common::error::MESSAGES;
use crate::common::Result;
use crate::transaction::Transaction;

mod api {
    use crate::common::Result;
    use crate::concept;

    pub trait Concept {}

    pub trait RemoteConcept: Concept {
        fn is_deleted(&self) -> bool;
    }

    pub trait Type: Concept {
        fn scoped_label(&self) -> &concept::ScopedLabel;
    }

    pub trait RemoteType: Type + RemoteConcept {}

    pub trait ThingType: Type {}

    pub trait RemoteThingType: ThingType + RemoteType {}

    pub trait EntityType: ThingType {}

    pub trait RemoteEntityType: EntityType + RemoteThingType {
        fn get_supertype(&self) -> Result<concept::EntityOrThingType>;

        fn get_instances(&self) -> Result<Vec<concept::Entity>>;
    }

    pub trait RelationType: ThingType {}

    pub trait RemoteRelationType: RelationType + RemoteThingType {
        fn get_supertype(&self) -> Result<concept::RelationOrThingType>;

        fn get_instances(&self) -> Result<Vec<concept::Relation>>;
    }
}

pub enum Concept {
    Type(Type),
    Thing(Thing),
}

impl Concept {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Concept) -> Result<Concept> {
        let concept = proto.concept.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match concept {
            Concept_oneof_concept::thing(_) => { todo!() }
            Concept_oneof_concept::field_type(type_concept) => Ok(Self::Type(Type::from_proto(type_concept)?))
        }
    }
}

impl Debug for Concept {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Concept::Type(type_concept) => type_concept.fmt(f),
            Concept::Thing(thing) => thing.fmt(f)
        }
    }
}

pub enum RemoteConcept {
    Type(RemoteType),
    Thing(RemoteThing),
}

pub enum Type {
    ThingType(ThingType),
    // RoleType(RoleType),
}

impl Type {
    pub(crate) fn from_proto(proto: typedb_protocol::concept::Type) -> Result<Type> {
        match proto.encoding {
            Type_Encoding::THING_TYPE => Ok(Self::ThingType(ThingType::Root(RootThingType { label: proto.label }))),
            Type_Encoding::ENTITY_TYPE => Ok(Self::ThingType(ThingType::EntityType(EntityType { label: proto.label }))),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => { todo!() }
            Type_Encoding::ROLE_TYPE => { todo!() }
        }
    }
}

impl Debug for Type {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Type::ThingType(thing_type) => thing_type.fmt(f)
        }
    }
}

pub enum RemoteType {
    ThingType(RemoteThingType),
    // RoleType(RemoteRoleType),
}

pub enum ThingType {
    Root(RootThingType),
    EntityType(EntityType),
    RelationType(RelationType),
    // AttributeType(AttributeType)
}

impl Debug for ThingType {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            ThingType::Root(x) => x.fmt(f),
            ThingType::EntityType(x) => x.fmt(f),
            ThingType::RelationType(x) => x.fmt(f),
        }
    }
}

pub enum RemoteThingType {
    Root(RemoteRootThingType),
    EntityType(RemoteEntityType),
    RelationType(RemoteRelationType),
    // AttributeType(RemoteAttributeType),
}

#[derive(Debug)]
pub enum Thing {
    Entity(Entity),
    Relation(Relation),
    // Attribute(Attribute),
}

pub enum RemoteThing {
    Entity(RemoteEntity),
    Relation(RemoteRelation),
    // Attribute(RemoteAttribute),
}

#[derive(Debug)]
pub enum EntityOrThingType {
    EntityType(EntityType),
    RootThingType(RootThingType)
}

#[derive(Debug)]
pub enum RelationOrThingType {
    RelationType(RelationType),
    RootThingType(RootThingType)
}

#[derive(Debug)]
pub struct RootThingType {
    pub label: String
}

pub struct RemoteRootThingType {
    pub label: String,
    tx: Transaction
}

#[derive(Debug)]
pub struct EntityType {
    pub label: String
}

pub struct RemoteEntityType {
    pub label: String,
    tx: Transaction
}

#[derive(Debug)]
pub struct RelationType {
    pub label: String
}

pub struct RemoteRelationType {
    pub label: String,
    tx: Transaction
}

// struct AttributeType {
//     label: String
// }

#[derive(Debug)]
pub struct Entity {
    pub iid: String
}

pub struct RemoteEntity {
    pub iid: String,
    tx: Transaction
}

#[derive(Debug)]
pub struct Relation {
    pub iid: String
}

pub struct RemoteRelation {
    pub iid: String,
    tx: Transaction
}

// struct Attribute {
//     iid: String,
//     value: AttributeValue
// }
//
// enum AttributeValue {
//     Boolean(bool),
//     Long(i64),
//     Double(f64),
//     String(String),
//     DateTime(Instant)
// }

// struct RoleType {
//     label: ScopedLabel,
// }

#[derive(Debug)]
pub struct ScopedLabel {
    pub scope: Option<String>,
    pub name: String
}

impl api::Concept for Concept {}

impl api::Concept for RemoteConcept {}

impl api::RemoteConcept for RemoteConcept {
    fn is_deleted(&self) -> bool {
        todo!()
    }
}

impl Thing {
    fn get_iid(&self) {
        match self {
            other => todo!() /*other.0.iid*/
        }
    }
}
