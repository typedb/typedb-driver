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
#![allow(unused)]

use std::convert::TryFrom;
use std::fmt::{Debug, Formatter};
use std::time::Instant;
use protobuf::SingularPtrField;
use typedb_protocol::concept::{Attribute_Value, AttributeType_ValueType, Concept_oneof_concept, Type_Encoding};
use uuid::Uuid;
use crate::common::error::MESSAGES;
use crate::common::{Error, Result};
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

    pub trait Thing: Concept {
        fn is_thing(&self) -> bool {
            true
        }
    }
}

pub enum Concept {
    Type(Type),
    Thing(Thing),
}

impl Concept {
    pub(crate) fn from_proto(mut proto: typedb_protocol::concept::Concept) -> Result<Concept> {
        let concept = proto.concept.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match concept {
            Concept_oneof_concept::thing(thing) => { Ok(Self::Thing(Thing::from_proto(thing)?)) }
            Concept_oneof_concept::field_type(type_concept) => Ok(Self::Type(Type::from_proto(&type_concept)?))
        }
    }

    pub fn as_type(&self) -> Result<&Type> {
        match self {
            Concept::Type(x) => { Ok(x) }
            _ => { todo!() }
        }
    }

    pub fn as_thing(&self) -> Result<&Thing> {
        match self {
            Concept::Thing(thing) => { Ok(thing) }
            _ => { todo!() }
        }
    }

    pub fn as_entity(&self) -> Result<&Entity> {
        match self {
            Concept::Thing(Thing::Entity(entity)) => Ok(entity),
            _ => { todo!() }
        }
    }

    pub fn as_attribute(&self) -> Result<&Attribute> {
        match self {
            Concept::Thing(Thing::Attribute(attr)) => Ok(attr),
            _ => { todo!() }
        }
    }

    pub fn is_entity(&self) -> bool {
        if let Concept::Thing(Thing::Entity(entity)) = self { true } else { false }
    }

    pub fn is_attribute(&self) -> bool {
        if let Concept::Thing(Thing::Attribute(attribute)) = self { true } else { false }
    }

    pub fn is_thing(&self) -> bool {
        match self {
            Concept::Thing(thing) => true,
            Concept::Type(type_) => false,
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
    Thing(ThingType),
    // Role(RoleType),
}

impl Type {
    // TODO: split into From<proto::Type> and From<&proto::Type>
    pub(crate) fn from_proto(proto: &typedb_protocol::concept::Type) -> Result<Type> {
        match proto.encoding {
            Type_Encoding::THING_TYPE => Ok(Self::Thing(ThingType::Root(RootThingType { label: proto.label.clone() }))),
            Type_Encoding::ENTITY_TYPE => Ok(Self::Thing(ThingType::Entity(EntityType::from_proto(proto)))),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => { todo!() }
            Type_Encoding::ROLE_TYPE => { todo!() }
        }
    }
}

impl Debug for Type {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Type::Thing(thing_type) => thing_type.fmt(f)
        }
    }
}

// impl From<Concept> for Type {
//     fn from(value: Concept) -> Self {
//         Type::try_from(value).unwrap()
//     }
// }

// impl TryFrom<Concept> for Type {
//     type Error = Error;
//
//     fn try_from(value: Concept) -> std::result::Result<Self, Self::Error> {
//         match value {
//             Concept::Type(x) => { Ok(x) }
//             _ => { todo!() }
//         }
//     }
// }

pub enum RemoteType {
    ThingType(RemoteThingType),
    // RoleType(RemoteRoleType),
}

pub enum ThingType {
    Root(RootThingType),
    Entity(EntityType),
    Relation(RelationType),
    // Attribute(AttributeType)
}

impl Debug for ThingType {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            ThingType::Root(x) => x.fmt(f),
            ThingType::Entity(x) => x.fmt(f),
            ThingType::Relation(x) => x.fmt(f),
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
    Attribute(Attribute),
}

impl Thing {
    pub(crate) fn from_proto(mut proto: typedb_protocol::concept::Thing) -> Result<Thing> {
        match proto.get_field_type().encoding {
            Type_Encoding::ENTITY_TYPE => Ok(Self::Entity(Entity { iid: Thing::iid_from_bytes(proto.get_iid()), type_: EntityType::from_proto(proto.get_field_type()) })),
            Type_Encoding::RELATION_TYPE => { todo!() }
            Type_Encoding::ATTRIBUTE_TYPE => Ok(Self::Attribute(Attribute::from_proto(proto)?)),
            _ => { todo!() }
        }
    }

    fn iid_from_bytes(bytes: &[u8]) -> String {
        format!("{:02x?}", bytes)
        // String::from(std::str::from_utf8(bytes).unwrap())
        // Uuid::from_slice(bytes).unwrap().to_string()
    }
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

impl EntityType {
    fn from_proto(proto: &typedb_protocol::concept::Type) -> EntityType {
        EntityType { label: proto.label.clone() }
    }
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
    pub iid: String,
    pub type_: EntityType
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

#[derive(Debug)]
pub enum Attribute {
    // Boolean(BooleanAttribute),
    Long(LongAttribute),
    // Double(DoubleAttribute),
    String(StringAttribute),
    // DateTime(DateTimeAttribute)
}

impl Attribute {
    pub(crate) fn from_proto(mut proto: typedb_protocol::concept::Thing) -> Result<Attribute> {
        match proto.get_field_type().get_value_type() {
            AttributeType_ValueType::BOOLEAN => { todo!() }
            AttributeType_ValueType::LONG => { Ok(Self::Long(LongAttribute { iid: Thing::iid_from_bytes(proto.get_iid()), value: proto.get_value().get_long() })) }
            AttributeType_ValueType::DOUBLE => { todo!() }
            AttributeType_ValueType::STRING => { Ok(Self::String(StringAttribute { iid: Thing::iid_from_bytes(proto.get_iid()), value: proto.take_value().take_string() }))}
            AttributeType_ValueType::DATETIME => { todo!() }
            _ => { todo!() }
        }
    }

    pub fn as_long(&self) -> Result<&LongAttribute> {
        match self {
            Attribute::Long(long_attr) => Ok(long_attr),
            _ => { todo!() }
        }
    }

    pub fn as_string(&self) -> Result<&StringAttribute> {
        match self {
            Attribute::String(string_attr) => Ok(string_attr),
            _ => { todo!() }
        }
    }

    pub fn is_long(&self) -> bool {
        if let Attribute::Long(long_attr) = self { true } else { false }
    }

    pub fn is_string(&self) -> bool {
        if let Attribute::String(string_attr) = self { true } else { false }
    }
}

#[derive(Debug)]
pub struct LongAttribute {
    pub iid: String,
    pub value: i64
}

#[derive(Debug)]
pub struct StringAttribute {
    pub iid: String,
    pub value: String
}

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
