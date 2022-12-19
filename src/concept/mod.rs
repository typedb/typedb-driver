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

#![allow(dead_code)]
#![allow(unused)]

use std::{
    convert::TryFrom,
    fmt,
    fmt::{Debug, Display, Formatter},
};

use chrono::NaiveDateTime;
use futures::{FutureExt, Stream, StreamExt};
use typedb_protocol::{
    attribute as attribute_proto, attribute_type as attribute_type_proto,
    attribute_type::ValueType, concept as concept_proto, r#type as type_proto, r#type::Encoding,
};

use crate::common::{error::ClientError, Result};

#[derive(Clone, Debug)]
pub enum Concept {
    Type(Type),
    Thing(Thing),
}

impl Concept {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Concept) -> Result<Concept> {
        let concept = proto.concept.ok_or(ClientError::MissingResponseField("concept"))?;
        match concept {
            concept_proto::Concept::Thing(thing) => Ok(Self::Thing(Thing::from_proto(thing)?)),
            concept_proto::Concept::Type(type_) => Ok(Self::Type(Type::from_proto(type_)?)),
        }
    }
}

#[derive(Clone, Debug)]
pub enum Type {
    Thing(ThingType),
    Role(RoleType),
}

impl Type {
    pub(crate) fn from_proto(proto: typedb_protocol::Type) -> Result<Type> {
        // TODO: replace unwrap() with ok_or(custom_error) throughout the module
        match type_proto::Encoding::from_i32(proto.encoding).unwrap() {
            Encoding::ThingType => Ok(Self::Thing(ThingType::Root(RootThingType::default()))),
            Encoding::EntityType => {
                Ok(Self::Thing(ThingType::Entity(EntityType::from_proto(proto))))
            }
            Encoding::RelationType => {
                Ok(Self::Thing(ThingType::Relation(RelationType::from_proto(proto))))
            }
            Encoding::AttributeType => {
                Ok(Self::Thing(ThingType::Attribute(AttributeType::from_proto(proto)?)))
            }
            Encoding::RoleType => Ok(Self::Role(RoleType::from_proto(proto))),
        }
    }
}

#[derive(Clone, Debug)]
pub enum ThingType {
    Root(RootThingType),
    Entity(EntityType),
    Relation(RelationType),
    Attribute(AttributeType),
}

#[derive(Debug)]
pub enum EntityOrThingType {
    EntityType(EntityType),
    RootThingType(RootThingType),
}

#[derive(Clone, Debug)]
pub struct RootThingType {
    pub label: String,
}

impl RootThingType {
    const LABEL: &'static str = "thing";

    pub fn new() -> Self {
        Self { label: String::from(Self::LABEL) }
    }
}

impl Default for RootThingType {
    fn default() -> Self {
        Self::new()
    }
}

#[derive(Clone, Debug)]
pub struct EntityType {
    pub label: String,
}

impl EntityType {
    pub fn new(label: String) -> Self {
        Self { label }
    }

    fn from_proto(proto: typedb_protocol::Type) -> Self {
        Self::new(proto.label)
    }
}

#[derive(Clone, Debug)]
pub struct RelationType {
    pub label: String,
}

impl RelationType {
    pub fn new(label: String) -> Self {
        Self { label }
    }

    fn from_proto(proto: typedb_protocol::Type) -> Self {
        Self::new(proto.label)
    }
}

#[derive(Clone, Debug)]
pub enum AttributeType {
    Root(RootAttributeType),
    Boolean(BooleanAttributeType),
    Long(LongAttributeType),
    Double(DoubleAttributeType),
    String(StringAttributeType),
    DateTime(DateTimeAttributeType),
}

impl AttributeType {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Type) -> Result<AttributeType> {
        match attribute_type_proto::ValueType::from_i32(proto.value_type).unwrap() {
            ValueType::Object => Ok(Self::Root(RootAttributeType::default())),
            ValueType::Boolean => Ok(Self::Boolean(BooleanAttributeType { label: proto.label })),
            ValueType::Long => Ok(Self::Long(LongAttributeType { label: proto.label })),
            ValueType::Double => Ok(Self::Double(DoubleAttributeType { label: proto.label })),
            ValueType::String => Ok(Self::String(StringAttributeType { label: proto.label })),
            ValueType::Datetime => Ok(Self::DateTime(DateTimeAttributeType { label: proto.label })),
        }
    }
}

#[derive(Clone, Debug)]
pub struct RootAttributeType {
    pub label: String,
}

impl RootAttributeType {
    const LABEL: &'static str = "attribute";

    pub fn new() -> Self {
        Self { label: String::from(Self::LABEL) }
    }
}

impl Default for RootAttributeType {
    fn default() -> Self {
        Self::new()
    }
}

#[derive(Clone, Debug)]
pub struct BooleanAttributeType {
    pub label: String,
}

impl BooleanAttributeType {
    pub fn new(label: String) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
pub struct LongAttributeType {
    pub label: String,
}

impl LongAttributeType {
    pub fn new(label: String) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
pub struct DoubleAttributeType {
    pub label: String,
}

impl DoubleAttributeType {
    pub fn new(label: String) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
pub struct StringAttributeType {
    pub label: String,
}

impl StringAttributeType {
    pub fn new(label: String) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
pub struct DateTimeAttributeType {
    pub label: String,
}

impl DateTimeAttributeType {
    pub fn new(label: String) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
pub struct RoleType {
    pub label: ScopedLabel,
}

impl RoleType {
    fn from_proto(proto: typedb_protocol::Type) -> Self {
        Self::new(ScopedLabel::new(proto.scope, proto.label))
    }

    pub fn new(label: ScopedLabel) -> Self {
        Self { label }
    }
}

#[derive(Clone, Debug)]
// #[enum_dispatch(ThingApi)]
pub enum Thing {
    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),
}

impl Thing {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Thing) -> Result<Thing> {
        match typedb_protocol::r#type::Encoding::from_i32(proto.r#type.clone().unwrap().encoding)
            .unwrap()
        {
            type_proto::Encoding::EntityType => Ok(Self::Entity(Entity::from_proto(proto)?)),
            type_proto::Encoding::RelationType => Ok(Self::Relation(Relation::from_proto(proto)?)),
            type_proto::Encoding::AttributeType => {
                Ok(Self::Attribute(Attribute::from_proto(proto)?))
            }
            _ => {
                todo!()
            }
        }
    }
}

// impl ConceptApi for Thing {}

// impl ThingApi for Thing {
//     fn get_iid(&self) -> &Vec<u8> {
//         match self {
//             Thing::Entity(x) => { x.get_iid() }
//             Thing::Relation(x) => { x.get_iid() }
//             Thing::Attribute(x) => { x.get_iid() }
//         }
//     }
// }

// TODO: Storing the Type here is *extremely* inefficient; we could be effectively creating
//       1 million copies of the same data when matching concepts of homogeneous types
#[derive(Clone, Debug)]
pub struct Entity {
    pub iid: Vec<u8>,
    pub type_: EntityType,
}

impl Entity {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Thing) -> Result<Entity> {
        Ok(Self { type_: EntityType::from_proto(proto.r#type.unwrap()), iid: proto.iid })
    }
}

// impl ThingApi for Entity {
//     // TODO: use enum_dispatch macro to avoid manually writing the duplicates of this method
//     fn get_iid(&self) -> &Vec<u8> {
//         &self.iid
//     }
// }

// impl ConceptApi for Entity {}

// impl EntityApi for Entity {}

#[derive(Clone, Debug)]
pub struct Relation {
    pub iid: Vec<u8>,
    pub type_: RelationType,
}

impl Relation {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Thing) -> Result<Relation> {
        Ok(Self { type_: RelationType::from_proto(proto.r#type.unwrap()), iid: proto.iid })
    }
}

// macro_rules! default_impl {
//     { impl $trait:ident $body:tt for $($t:ident),* $(,)? } => {
//         $(impl $trait for $t $body)*
//     }
// }
//
// default_impl! {
// impl ThingApi {
//     fn get_iid(&self) -> &Vec<u8> {
//         &self.iid
//     }
// } for Entity, Relation
// }

// impl ConceptApi for Relation {}

// impl RelationApi for Relation {}

#[derive(Clone, Debug)]
pub enum Attribute {
    Boolean(BooleanAttribute),
    Long(LongAttribute),
    Double(DoubleAttribute),
    String(StringAttribute),
    DateTime(DateTimeAttribute),
}

impl Attribute {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Thing) -> Result<Attribute> {
        match attribute_type_proto::ValueType::from_i32(proto.r#type.unwrap().value_type).unwrap() {
            ValueType::Object => {
                todo!()
            }
            ValueType::Boolean => Ok(Self::Boolean(BooleanAttribute {
                value: if let attribute_proto::value::Value::Boolean(value) =
                    proto.value.unwrap().value.unwrap()
                {
                    value
                } else {
                    todo!()
                },
                iid: proto.iid,
            })),
            ValueType::Long => Ok(Self::Long(LongAttribute {
                value: if let attribute_proto::value::Value::Long(value) =
                    proto.value.unwrap().value.unwrap()
                {
                    value
                } else {
                    todo!()
                },
                iid: proto.iid,
            })),
            ValueType::Double => Ok(Self::Double(DoubleAttribute {
                value: if let attribute_proto::value::Value::Double(value) =
                    proto.value.unwrap().value.unwrap()
                {
                    value
                } else {
                    todo!()
                },
                iid: proto.iid,
            })),
            ValueType::String => Ok(Self::String(StringAttribute {
                value: if let attribute_proto::value::Value::String(value) =
                    proto.value.unwrap().value.unwrap()
                {
                    value
                } else {
                    todo!()
                },
                iid: proto.iid,
            })),
            ValueType::Datetime => Ok(Self::DateTime(DateTimeAttribute {
                value: if let attribute_proto::value::Value::DateTime(value) =
                    proto.value.unwrap().value.unwrap()
                {
                    NaiveDateTime::from_timestamp_opt(value / 1000, (value % 1000) as u32).unwrap()
                } else {
                    todo!()
                },
                iid: proto.iid,
            })),
        }
    }
}

#[derive(Clone, Debug)]
pub struct BooleanAttribute {
    pub iid: Vec<u8>,
    pub value: bool,
}

#[derive(Clone, Debug)]
pub struct LongAttribute {
    pub iid: Vec<u8>,
    pub value: i64,
}

impl LongAttribute {}

#[derive(Clone, Debug)]
pub struct DoubleAttribute {
    pub iid: Vec<u8>,
    pub value: f64,
}

#[derive(Clone, Debug)]
pub struct StringAttribute {
    pub iid: Vec<u8>,
    pub value: String,
}

impl StringAttribute {}

#[derive(Clone, Debug)]
pub struct DateTimeAttribute {
    pub iid: Vec<u8>,
    pub value: NaiveDateTime,
}

pub mod attribute {
    #[derive(Copy, Clone, Debug)]
    pub enum ValueType {
        Object = 0,
        Boolean = 1,
        Long = 2,
        Double = 3,
        String = 4,
        DateTime = 5,
    }
}

#[derive(Clone, Debug)]
pub struct ScopedLabel {
    pub scope: String,
    pub name: String,
}

impl ScopedLabel {
    pub fn new(scope: String, name: String) -> Self {
        Self { scope, name }
    }
}

impl fmt::Display for ScopedLabel {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "{}:{}", self.scope, self.name)
    }
}
