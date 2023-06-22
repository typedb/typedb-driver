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

mod thing;
mod type_;

pub use self::{
    thing::{Attribute, Entity, Relation, Thing, Value},
    type_::{
        Annotation, AttributeType, EntityType, RelationType, RoleType, RootThingType, ScopedLabel, ThingType, ValueType,
    },
};

#[derive(Clone, Debug, PartialEq)]
pub enum Concept {
    RootThingType(RootThingType),

    EntityType(EntityType),
    RelationType(RelationType),
    RoleType(RoleType),
    AttributeType(AttributeType),

    Entity(Entity),
    Relation(Relation),
    Attribute(Attribute),
}

impl Concept {
    pub fn type_label_cloned(&self) -> String {
        // FIXME: Add a Label type to simplify this function
        match self {
            Concept::RootThingType(_) => RootThingType::LABEL.to_owned(),
            Concept::EntityType(type_) => type_.label.clone(),
            Concept::RelationType(RelationType { label, .. }) => label.clone(),
            Concept::RoleType(RoleType { label, .. }) => format!("{label}"),
            Concept::AttributeType(AttributeType { label, .. }) => label.clone(),
            Concept::Entity(Entity { type_: EntityType { label, .. }, .. }) => label.clone(),
            Concept::Relation(Relation { type_: RelationType { label, .. }, .. }) => label.clone(),
            Concept::Attribute(Attribute { type_: AttributeType { label, .. }, .. }) => label.clone(),
        }
    }
}

#[derive(Copy, Clone, Debug, Eq, PartialEq)]
pub enum Transitivity {
    Explicit,
    Transitive,
}

#[derive(Clone, Debug)]
pub struct SchemaException {
    pub code: String,
    pub message: String,
}
