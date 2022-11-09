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
use std::fmt;
use std::fmt::{Debug, Display, Formatter};
use std::iter::once;
use std::time::Instant;
use chrono::NaiveDateTime;
use futures::{FutureExt, Stream, stream, StreamExt};
use futures::stream::FuturesUnordered;
use typedb_protocol::{attribute as attribute_proto, attribute_type as attribute_type_proto, concept as concept_proto, r#type as type_proto, thing as thing_proto};
use typedb_protocol::attribute_type::ValueType;
use typedb_protocol::r#type::Encoding;
use typedb_protocol::thing::res_part::Res;
use typedb_protocol::transaction;
use uuid::Uuid;
use crate::common::error::MESSAGES;
use crate::common::{Error, Result};
use crate::rpc::builder::thing::attribute_get_owners_req;
use crate::transaction::Transaction;

// #[enum_dispatch]
// pub trait ConceptApi {}
//
// #[enum_dispatch]
// pub trait TypeApi: ConceptApi {}
//
// pub trait ThingTypeApi: TypeApi {}
//
// pub trait EntityTypeApi: ThingTypeApi {
//     fn get_supertype(&self, tx: &Transaction) -> Result<EntityOrThingType>;
// }
//
// pub trait RelationTypeApi: ThingTypeApi {
//     fn get_supertype(&self, tx: &Transaction) -> Result<RelationOrThingType>;
// }
//
// // #[enum_dispatch]
// pub trait ThingApi: ConceptApi {
//     fn get_iid(&self) -> &Vec<u8>;
// }
//
// pub trait EntityApi: ThingApi {}
//
// pub trait RelationApi: ThingApi {}
//
// pub trait AttributeApi: ThingApi {}
//
// pub trait LongAttributeApi: AttributeApi {}
//
// pub trait StringAttributeApi: AttributeApi {}

// fn stream_things(tx: &mut Transaction, req: transaction::Req) -> impl Stream<Item = Result<thing_proto::res_part::Res>> {
//     tx.streaming_rpc(req).map(|result: Result<transaction::ResPart>| {
//         match result {
//             Ok(tx_res_part) => {
//                 match tx_res_part.res {
//                     Some(transaction::res_part::Res::ThingResPart(res_part)) => {
//                         res_part.res.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["res_part.thing_res_part"]))
//                     }
//                     _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["res_part.thing_res_part"])) }
//                 }
//             }
//             Err(err) => { Err(err) }
//         }
//     })
// }

#[derive(Clone, Debug)]
pub enum Concept {
    Type(Type),
    Thing(Thing),
}

impl Concept {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Concept) -> Result<Concept> {
        let concept = proto.concept.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["concept"]))?;
        match concept {
            concept_proto::Concept::Thing(thing) => { Ok(Self::Thing(Thing::from_proto(thing)?)) }
            concept_proto::Concept::Type(type_) => { Ok(Self::Type(Type::from_proto(type_)?)) }
        }
    }

    // pub fn as_type(&self) -> Result<&Type> {
    //     match self {
    //         Concept::Type(x) => { Ok(x) }
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn as_thing(&self) -> Result<&Thing> {
    //     match self {
    //         Concept::Thing(thing) => { Ok(thing) }
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn as_entity(&self) -> Result<&Entity> {
    //     match self {
    //         Concept::Thing(Thing::Entity(entity)) => Ok(entity),
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn as_attribute(&self) -> Result<&Attribute> {
    //     match self {
    //         Concept::Thing(Thing::Attribute(attr)) => Ok(attr),
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn is_entity(&self) -> bool {
    //     if let Concept::Thing(Thing::Entity(entity)) = self { true } else { false }
    // }
    //
    // pub fn is_attribute(&self) -> bool {
    //     if let Concept::Thing(Thing::Attribute(attribute)) = self { true } else { false }
    // }
    //
    // pub fn is_thing(&self) -> bool {
    //     match self {
    //         Concept::Thing(thing) => true,
    //         Concept::Type(type_) => false,
    //     }
    // }
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
            Encoding::ThingType => { Ok(Self::Thing(ThingType::Root(RootThingType::default()))) },
            Encoding::EntityType => { Ok(Self::Thing(ThingType::Entity(EntityType::from_proto(proto)))) },
            Encoding::RelationType => { Ok(Self::Thing(ThingType::Relation(RelationType::from_proto(proto)))) },
            Encoding::AttributeType => { Ok(Self::Thing(ThingType::Attribute(AttributeType::from_proto(proto)?))) },
            Encoding::RoleType => { Ok(Self::Role(RoleType::from_proto(proto))) }
        }
    }
}

// impl ConceptApi for Type {}

#[derive(Clone, Debug)]
pub enum ThingType {
    Root(RootThingType),
    Entity(EntityType),
    Relation(RelationType),
    Attribute(AttributeType)
}

// impl TypeApi for ThingType {}
//
// impl ConceptApi for ThingType {}
//
// impl ThingTypeApi for ThingType {}

#[derive(Debug)]
pub enum EntityOrThingType {
    EntityType(EntityType),
    RootThingType(RootThingType)
}

// impl TypeApi for EntityOrThingType {}
//
// impl ConceptApi for EntityOrThingType {}
//
// impl ThingTypeApi for EntityOrThingType {}

// #[derive(Debug)]
// pub enum RelationOrThingType {
//     RelationType(RelationType),
//     RootThingType(RootThingType)
// }

// impl TypeApi for RelationOrThingType {}
//
// impl ConceptApi for RelationOrThingType {}
//
// impl ThingTypeApi for RelationOrThingType {}

#[derive(Clone, Debug)]
pub struct RootThingType {
    pub label: String
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

// impl TypeApi for RootThingType {}
//
// impl ConceptApi for RootThingType {}
//
// impl ThingTypeApi for RootThingType {}

#[derive(Clone, Debug)]
pub struct EntityType {
    pub label: String
}

impl EntityType {
    pub fn new(label: String) -> Self {
        Self { label }
    }

    fn from_proto(proto: typedb_protocol::Type) -> Self {
        Self::new(proto.label)
    }

    // Ideally we define this in EntityTypeApi, but can't return impl Stream in a trait method
    // fn get_instances(&self, tx: &Transaction) -> impl Stream<Item = Entity> {
    //     todo!()
    // }
}

// impl ThingTypeApi for EntityType {}
//
// impl TypeApi for EntityType {}
//
// impl ConceptApi for EntityType {}
//
// impl EntityTypeApi for EntityType {
//     fn get_supertype(&self, tx: &Transaction) -> Result<EntityOrThingType> {
//         todo!()
//     }
// }

#[derive(Clone, Debug)]
pub struct RelationType {
    pub label: String
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
    DateTime(DateTimeAttributeType)
}

impl AttributeType {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Type) -> Result<AttributeType> {
        match attribute_type_proto::ValueType::from_i32(proto.value_type).unwrap() {
            ValueType::Object => { Ok(Self::Root(RootAttributeType::default())) }
            ValueType::Boolean => { Ok(Self::Boolean(BooleanAttributeType { label: proto.label })) }
            ValueType::Long => { Ok(Self::Long(LongAttributeType { label: proto.label })) }
            ValueType::Double => { Ok(Self::Double(DoubleAttributeType { label: proto.label })) }
            ValueType::String => { Ok(Self::String(StringAttributeType { label: proto.label })) }
            ValueType::Datetime => { Ok(Self::DateTime(DateTimeAttributeType { label: proto.label })) }
        }
    }
}

#[derive(Clone, Debug)]
pub struct RootAttributeType {
    pub label: String
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
    pub label: String
}

impl BooleanAttributeType {
    pub fn new(label: String) -> Self { Self { label } }
}

#[derive(Clone, Debug)]
pub struct LongAttributeType {
    pub label: String
}

impl LongAttributeType {
    pub fn new(label: String) -> Self { Self { label } }
}

#[derive(Clone, Debug)]
pub struct DoubleAttributeType {
    pub label: String
}

impl DoubleAttributeType {
    pub fn new(label: String) -> Self { Self { label } }
}

#[derive(Clone, Debug)]
pub struct StringAttributeType {
    pub label: String
}

impl StringAttributeType {
    pub fn new(label: String) -> Self { Self { label } }
}

#[derive(Clone, Debug)]
pub struct DateTimeAttributeType {
    pub label: String
}

impl DateTimeAttributeType {
    pub fn new(label: String) -> Self { Self { label } }
}

#[derive(Clone, Debug)]
pub struct RoleType {
    pub label: ScopedLabel,
}

impl RoleType {
    fn from_proto(proto: typedb_protocol::Type) -> Self {
        Self::new(ScopedLabel::new(proto.scope, proto.label))
    }

    pub fn new(label: ScopedLabel) -> Self { Self { label } }
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
        match typedb_protocol::r#type::Encoding::from_i32(proto.r#type.clone().unwrap().encoding).unwrap() {
            type_proto::Encoding::EntityType => { Ok(Self::Entity(Entity::from_proto(proto)?)) },
            type_proto::Encoding::RelationType => { Ok(Self::Relation(Relation::from_proto(proto)?)) }
            type_proto::Encoding::AttributeType => { Ok(Self::Attribute(Attribute::from_proto(proto)?)) },
            _ => { todo!() }
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
    pub type_: EntityType
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
    pub type_: RelationType
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
    DateTime(DateTimeAttribute)
}

impl Attribute {
    pub(crate) fn from_proto(mut proto: typedb_protocol::Thing) -> Result<Attribute> {
        match attribute_type_proto::ValueType::from_i32(proto.r#type.unwrap().value_type).unwrap() {
            ValueType::Object => { todo!() }
            ValueType::Boolean => {
                Ok(Self::Boolean(BooleanAttribute {
                    value: if let attribute_proto::value::Value::Boolean(value) = proto.value.unwrap().value.unwrap() { value } else { todo!() },
                    iid: proto.iid
                }))
            }
            ValueType::Long => {
                Ok(Self::Long(LongAttribute {
                    value: if let attribute_proto::value::Value::Long(value) = proto.value.unwrap().value.unwrap() { value } else { todo!() },
                    iid: proto.iid
                }))
            }
            ValueType::Double => {
                Ok(Self::Double(DoubleAttribute {
                    value: if let attribute_proto::value::Value::Double(value) = proto.value.unwrap().value.unwrap() { value } else { todo!() },
                    iid: proto.iid
                }))
            }
            ValueType::String => {
                Ok(Self::String(StringAttribute {
                    value: if let attribute_proto::value::Value::String(value) = proto.value.unwrap().value.unwrap() { value } else { todo!() },
                    iid: proto.iid
                }))
            }
            ValueType::Datetime => {
                Ok(Self::DateTime(DateTimeAttribute {
                    value: if let attribute_proto::value::Value::DateTime(value) = proto.value.unwrap().value.unwrap() {
                        NaiveDateTime::from_timestamp(value / 1000, (value % 1000) as u32)
                    } else { todo!() },
                    iid: proto.iid
                }))
            }
        }
    }

    // pub fn as_long(&self) -> Result<&LongAttribute> {
    //     match self {
    //         Attribute::Long(long_attr) => Ok(long_attr),
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn as_string(&self) -> Result<&StringAttribute> {
    //     match self {
    //         Attribute::String(string_attr) => Ok(string_attr),
    //         _ => { todo!() }
    //     }
    // }
    //
    // pub fn is_long(&self) -> bool {
    //     if let Attribute::Long(long_attr) = self { true } else { false }
    // }
    //
    // pub fn is_string(&self) -> bool {
    //     if let Attribute::String(string_attr) = self { true } else { false }
    // }
    //
    // pub fn get_iid(&self) -> &Vec<u8> {
    //     match self {
    //         Attribute::Long(x) => { &x.iid }
    //         Attribute::String(x) => { &x.iid }
    //     }
    // }
    //
    // pub fn get_owners(&self, tx: &mut Transaction) -> impl Stream<Item = Result<Thing>> {
    //     Self::get_owners_impl(self.get_iid(), tx)
    // }
    //
    // fn get_owners_impl(iid: &Vec<u8>, tx: &mut Transaction) -> impl Stream<Item = Result<Thing>> {
    //     stream_things(tx, attribute_get_owners_req(iid)).flat_map(|result: Result<thing_proto::res_part::Res>| {
    //         match result {
    //             Ok(res_part) => {
    //                 match res_part {
    //                     Res::AttributeGetOwnersResPart(x) => { stream::iter(x.things.into_iter().map(|thing| Thing::from_proto(thing))).left_stream() }
    //                     _ => stream::iter(once(Err(MESSAGES.client.missing_response_field.to_err(vec!["query_manager_res_part.match_res_part"])))).right_stream()
    //                 }
    //             }
    //             Err(err) => { stream::iter(once(Err(err))).right_stream() }
    //         }
    //     })
    // }
}

// impl ThingApi for Attribute {
//     fn get_iid(&self) -> &Vec<u8> {
//         match self {
//             Attribute::Long(x) => { x.get_iid() }
//             Attribute::String(x) => { x.get_iid() }
//         }
//     }
// }

// impl ConceptApi for Attribute {}

// impl AttributeApi for Attribute {}

#[derive(Clone, Debug)]
pub struct BooleanAttribute {
    pub iid: Vec<u8>,
    pub value: bool
}

#[derive(Clone, Debug)]
pub struct LongAttribute {
    pub iid: Vec<u8>,
    pub value: i64
}

impl LongAttribute {
    // pub fn get_owners(&self, tx: &mut Transaction) -> impl Stream<Item = Result<Thing>> {
    //     Attribute::get_owners_impl(&self.iid, tx)
    // }
}

// impl AttributeApi for LongAttribute {}

// impl ThingApi for LongAttribute {
//     fn get_iid(&self) -> &Vec<u8> {
//         &self.iid
//     }
// }

// impl ConceptApi for LongAttribute {}

// impl LongAttributeApi for LongAttribute {}

#[derive(Clone, Debug)]
pub struct DoubleAttribute {
    pub iid: Vec<u8>,
    pub value: f64
}

#[derive(Clone, Debug)]
pub struct StringAttribute {
    pub iid: Vec<u8>,
    pub value: String
}

impl StringAttribute {
    // pub fn get_owners(&self, tx: &mut Transaction) -> impl Stream<Item = Result<Thing>> {
    //     Attribute::get_owners_impl(&self.iid, tx)
    // }
}

// impl AttributeApi for StringAttribute {}

// impl ThingApi for StringAttribute {
//     fn get_iid(&self) -> &Vec<u8> {
//         &self.iid
//     }
// }

// impl ConceptApi for StringAttribute {}

// impl StringAttributeApi for StringAttribute {}

#[derive(Clone, Debug)]
pub struct DateTimeAttribute {
    pub iid: Vec<u8>,
    pub value: NaiveDateTime
}

pub mod attribute {
    #[derive(Copy, Clone, Debug)]
    pub enum ValueType {
        Object = 0,
        Boolean = 1,
        Long = 2,
        Double = 3,
        String = 4,
        DateTime = 5
    }
}

// #[derive(Clone, Debug)]
// pub enum Label {
//     Scoped(ScopedLabel),
//     Unscoped(String),
// }

#[derive(Clone, Debug)]
pub struct ScopedLabel {
    pub scope: String,
    pub name: String
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
