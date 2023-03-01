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

use crate::common::{error::ConnectionError, Result};

#[derive(Clone, Debug)]
pub enum Concept {
    Type(Type),
    Thing(Thing),
}

#[derive(Clone, Debug)]
pub enum Type {
    Thing(ThingType),
    Role(RoleType),
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
}

#[derive(Clone, Debug)]
pub struct RelationType {
    pub label: String,
}

impl RelationType {
    pub fn new(label: String) -> Self {
        Self { label }
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
