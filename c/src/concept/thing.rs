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

use std::ffi::c_char;

use typedb_driver::{
    concept::{Annotation, Concept},
    transaction::concept::api::{AttributeAPI, RelationAPI},
    Transaction,
};

use super::{
    concept::{
        borrow_as_attribute, borrow_as_attribute_type, borrow_as_entity, borrow_as_relation, borrow_as_role_type,
        borrow_as_thing, borrow_as_thing_type,
    },
    ConceptIterator, RolePlayerIterator,
};
use crate::{
    error::try_release,
    memory::{array_view, borrow, release, release_string},
    promise::{BoolPromise, VoidPromise},
};

/// Retrieves the unique id of the ``Thing``.
#[no_mangle]
pub extern "C" fn thing_get_iid(thing: *mut Concept) -> *mut c_char {
    release_string(borrow_as_thing(thing).iid().to_string())
}

/// Checks if this ``Thing`` is inferred by a [Reasoning Rule].
#[no_mangle]
pub extern "C" fn thing_get_is_inferred(thing: *mut Concept) -> bool {
    borrow_as_thing(thing).is_inferred()
}

/// Retrieves the type which this ``Entity`` belongs to.
#[no_mangle]
pub extern "C" fn entity_get_type(entity: *const Concept) -> *mut Concept {
    release(Concept::EntityType(borrow_as_entity(entity).type_.clone()))
}

/// Retrieves the type which this ``Relation`` belongs to.
#[no_mangle]
pub extern "C" fn relation_get_type(relation: *const Concept) -> *mut Concept {
    release(Concept::RelationType(borrow_as_relation(relation).type_.clone()))
}

/// Retrieves the type which this ``Attribute`` belongs to.
#[no_mangle]
pub extern "C" fn attribute_get_type(attribute: *const Concept) -> *mut Concept {
    release(Concept::AttributeType(borrow_as_attribute(attribute).type_.clone()))
}

/// Retrieves the type which this ``Relation`` belongs to.
#[no_mangle]
pub extern "C" fn attribute_get_value(attribute: *const Concept) -> *mut Concept {
    release(Concept::Value(borrow_as_attribute(attribute).value.clone()))
}

/// Deletes this ``Thing``.
#[no_mangle]
pub extern "C" fn thing_delete(transaction: *mut Transaction<'static>, thing: *mut Concept) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_thing(thing).delete(borrow(transaction)))))
}

/// Checks if this ``Thing`` is deleted.
#[no_mangle]
pub extern "C" fn thing_is_deleted(transaction: *mut Transaction<'static>, thing: *const Concept) -> *mut BoolPromise {
    release(BoolPromise(Box::new(borrow_as_thing(thing).is_deleted(borrow(transaction)))))
}

/// Retrieves the ``Attribute``s that this ``Thing`` owns, optionally filtered by ``AttributeType``s.
///
/// @param transaction The current transaction
/// @param thing The thing to get owned attributes of
/// @param attribute_types a null-terminated array holding the attribute-types to include
/// @param annotations a null-terminated array of annotations - If non-empty, Only retrieves attributes with all given <code>Annotation</code>s
#[no_mangle]
pub extern "C" fn thing_get_has(
    transaction: *mut Transaction<'static>,
    thing: *const Concept,
    attribute_types: *const *const Concept,
    annotations: *const *const Annotation,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    let thing = borrow_as_thing(thing);
    let attribute_types = array_view(attribute_types).map(|at| borrow_as_attribute_type(at)).cloned().collect();
    let annotations = array_view(annotations).copied().collect();
    try_release(thing.get_has(transaction, attribute_types, annotations).map(ConceptIterator::attributes))
}

/// Assigns an ``Attribute`` to be owned by this ``Thing``.
#[no_mangle]
pub extern "C" fn thing_set_has(
    transaction: *mut Transaction<'static>,
    thing: *mut Concept,
    attribute: *const Concept,
) -> *mut VoidPromise {
    let transaction = borrow(transaction);
    let attribute = borrow_as_attribute(attribute).clone();
    release(VoidPromise(Box::new(borrow_as_thing(thing).set_has(transaction, attribute))))
}

/// Unassigns an ``Attribute`` from this ``Thing``.
#[no_mangle]
pub extern "C" fn thing_unset_has(
    transaction: *mut Transaction<'static>,
    thing: *mut Concept,
    attribute: *const Concept,
) -> *mut VoidPromise {
    let transaction = borrow(transaction);
    let attribute = borrow_as_attribute(attribute).clone();
    release(VoidPromise(Box::new(borrow_as_thing(thing).unset_has(transaction, attribute))))
}

/// Retrieves all the ``Relations`` which this ``Thing`` plays a role in,
/// optionally filtered by one or more given roles.
///
/// @param role_types: a null-terminated array of ``RoleType``s to include.
#[no_mangle]
pub extern "C" fn thing_get_relations(
    transaction: *mut Transaction<'static>,
    thing: *const Concept,
    role_types: *const *const Concept,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    let role_types = array_view(role_types).map(|rt| borrow_as_role_type(rt)).cloned().collect();
    try_release(borrow_as_thing(thing).get_relations(transaction, role_types).map(ConceptIterator::relations))
}

/// Retrieves the roles that this ``Thing`` is currently playing.
#[no_mangle]
pub extern "C" fn thing_get_playing(
    transaction: *mut Transaction<'static>,
    thing: *const Concept,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    try_release(borrow_as_thing(thing).get_playing(transaction).map(ConceptIterator::role_types))
}

/// Adds a new role player to play the given role in this ``Relation``.
#[no_mangle]
pub extern "C" fn relation_add_role_player(
    transaction: *mut Transaction<'static>,
    relation: *mut Concept,
    role_type: *const Concept,
    player: *const Concept,
) -> *mut VoidPromise {
    let transaction = borrow(transaction);
    let role_type = borrow_as_role_type(role_type).clone();
    let player = borrow_as_thing(player).to_thing_cloned();
    release(VoidPromise(Box::new(borrow_as_relation(relation).add_role_player(transaction, role_type, player))))
}

/// Removes the association of the given instance that plays the given role in this ``Relation``.
#[no_mangle]
pub extern "C" fn relation_remove_role_player(
    transaction: *mut Transaction<'static>,
    relation: *mut Concept,
    role_type: *const Concept,
    player: *const Concept,
) -> *mut VoidPromise {
    let transaction = borrow(transaction);
    let role_type = borrow_as_role_type(role_type).clone();
    let player = borrow_as_thing(player).to_thing_cloned();
    release(VoidPromise(Box::new(borrow_as_relation(relation).remove_role_player(transaction, role_type, player))))
}

/// Retrieves all role players of this ``Relation``, optionally filtered by given role types.
///
/// @param role_types: a null-terminated array of ``RoleType``s to include.
#[no_mangle]
pub extern "C" fn relation_get_players_by_role_type(
    transaction: *mut Transaction<'static>,
    relation: *const Concept,
    role_types: *const *const Concept,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    let role_types = array_view(role_types).map(|rt| borrow_as_role_type(rt)).cloned().collect();
    try_release(
        borrow_as_relation(relation).get_players_by_role_type(transaction, role_types).map(ConceptIterator::things),
    )
}

/// Retrieves all instance involved in the ``Relation``, each paired with the role it plays.
#[no_mangle]
pub extern "C" fn relation_get_role_players(
    transaction: *mut Transaction<'static>,
    relation: *const Concept,
) -> *mut RolePlayerIterator {
    let transaction = borrow(transaction);
    try_release(borrow_as_relation(relation).get_role_players(transaction).map(RolePlayerIterator::new))
}

/// Retrieves all role types currently played in this ``Relation``.
#[no_mangle]
pub extern "C" fn relation_get_relating(
    transaction: *mut Transaction<'static>,
    relation: *const Concept,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    try_release(borrow_as_relation(relation).get_relating(transaction).map(ConceptIterator::role_types))
}

/// Retrieves the instances that own this ``Attribute``.
#[no_mangle]
pub extern "C" fn attribute_get_owners(
    transaction: *mut Transaction<'static>,
    attribute: *const Concept,
    thing_type: *const Concept,
) -> *mut ConceptIterator {
    let transaction = borrow(transaction);
    let thing_type = unsafe { thing_type.as_ref().map(|t| borrow_as_thing_type(t).to_thing_type_cloned()) };
    try_release(borrow_as_attribute(attribute).get_owners(transaction, thing_type).map(ConceptIterator::things))
}
