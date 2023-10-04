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
mod value;

pub use self::{
    thing::{Attribute, Entity, Relation, Thing},
    type_::{Annotation, AttributeType, EntityType, RelationType, RoleType, RootThingType, ScopedLabel, ThingType},
    value::{Value, ValueType},
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

    Value(Value),
}

/// Used for specifying whether we need explicit or transitive subtyping, instances, etc.
///
/// # Examples
///
/// ```rust
/// entity_type.get_subtypes(transaction, Transitivity::Transitive);
/// relation_type.get_instances(transaction, Transitivity::Explicit);
/// ```
#[repr(C)]
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
