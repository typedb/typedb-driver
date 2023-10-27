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

mod concept;
mod manager;
mod thing;
mod type_;

use std::ptr::addr_of_mut;

use itertools::Itertools;
use typedb_driver::{
    box_stream,
    concept::{
        Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Thing, ThingType,
        Value,
    },
    BoxPromise, BoxStream, Promise, Result,
};

use super::{
    iterator::iterator_try_next,
    memory::{borrow, free, release},
};
use crate::{error::try_release_optional, iterator::CIterator, memory::take_ownership};

pub struct ConceptPromise(BoxPromise<'static, Result<Option<Concept>>>);

impl ConceptPromise {
    fn entity(promise: impl Promise<'static, Result<Option<Entity>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::Entity))))
    }

    fn relation(promise: impl Promise<'static, Result<Option<Relation>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::Relation))))
    }

    fn attribute(promise: impl Promise<'static, Result<Option<Attribute>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::Attribute))))
    }

    pub(super) fn value(promise: impl Promise<'static, Result<Option<Value>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::Value))))
    }

    fn entity_type(promise: impl Promise<'static, Result<Option<EntityType>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::EntityType))))
    }

    fn relation_type(promise: impl Promise<'static, Result<Option<RelationType>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::RelationType))))
    }

    fn attribute_type(promise: impl Promise<'static, Result<Option<AttributeType>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::AttributeType))))
    }

    fn role_type(promise: impl Promise<'static, Result<Option<RoleType>>>) -> Self {
        Self(Box::new(|| Ok(promise.resolve()?.map(Concept::RoleType))))
    }
}

#[no_mangle]
pub extern "C" fn concept_promise_resolve(promise: *mut ConceptPromise) -> *mut Concept {
    try_release_optional(take_ownership(promise).0.resolve().transpose())
}

pub struct ConceptIterator(pub CIterator<Result<Concept>>);

impl ConceptIterator {
    fn things(it: BoxStream<'static, Result<Thing>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(|thing| match thing {
            Thing::Entity(entity) => Concept::Entity(entity),
            Thing::Relation(relation) => Concept::Relation(relation),
            Thing::Attribute(attribute) => Concept::Attribute(attribute),
        }))))
    }

    fn entities(it: BoxStream<'static, Result<Entity>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::Entity))))
    }

    fn relations(it: BoxStream<'static, Result<Relation>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::Relation))))
    }

    fn attributes(it: BoxStream<'static, Result<Attribute>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::Attribute))))
    }

    fn thing_types(it: BoxStream<'static, Result<ThingType>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(|thing_type| match thing_type {
            ThingType::EntityType(entity_type) => Concept::EntityType(entity_type),
            ThingType::RelationType(relation_type) => Concept::RelationType(relation_type),
            ThingType::AttributeType(attribute_type) => Concept::AttributeType(attribute_type),
            ThingType::RootThingType(root_thing_type) => Concept::RootThingType(root_thing_type),
        }))))
    }

    fn entity_types(it: BoxStream<'static, Result<EntityType>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::EntityType))))
    }

    fn relation_types(it: BoxStream<'static, Result<RelationType>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::RelationType))))
    }

    fn attribute_types(it: BoxStream<'static, Result<AttributeType>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::AttributeType))))
    }

    fn role_types(it: BoxStream<'static, Result<RoleType>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(Concept::RoleType))))
    }
}

#[no_mangle]
pub extern "C" fn concept_iterator_next(it: *mut ConceptIterator) -> *mut Concept {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn concept_iterator_drop(it: *mut ConceptIterator) {
    free(it);
}

type RolePlayerIteratorInner = CIterator<Result<RolePlayer>>;

pub struct RolePlayerIterator(RolePlayerIteratorInner);

impl RolePlayerIterator {
    fn new(it: BoxStream<'static, Result<(RoleType, Thing)>>) -> Self {
        Self(CIterator(box_stream(it.map_ok(|(role_type, thing)| RolePlayer {
            role_type: Concept::RoleType(role_type),
            player: match thing {
                Thing::Entity(entity) => Concept::Entity(entity),
                Thing::Relation(relation) => Concept::Relation(relation),
                Thing::Attribute(attribute) => Concept::Attribute(attribute),
            },
        }))))
    }
}

#[no_mangle]
pub extern "C" fn role_player_iterator_next(it: *mut RolePlayerIterator) -> *mut RolePlayer {
    unsafe { iterator_try_next(addr_of_mut!((*it).0)) }
}

#[no_mangle]
pub extern "C" fn role_player_iterator_drop(it: *mut RolePlayerIterator) {
    free(it);
}

pub struct RolePlayer {
    role_type: Concept,
    player: Concept,
}

#[no_mangle]
pub extern "C" fn role_player_drop(role_player: *mut RolePlayer) {
    free(role_player);
}

#[no_mangle]
pub extern "C" fn role_player_get_role_type(role_player: *const RolePlayer) -> *mut Concept {
    release(borrow(role_player).role_type.clone())
}

#[no_mangle]
pub extern "C" fn role_player_get_player(role_player: *const RolePlayer) -> *mut Concept {
    release(borrow(role_player).player.clone())
}
