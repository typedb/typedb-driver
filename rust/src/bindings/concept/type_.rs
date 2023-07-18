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

use std::ffi::c_char;

use super::{
    concept::{
        borrow_as_attribute_type, borrow_as_attribute_type_mut, borrow_as_entity_type, borrow_as_entity_type_mut,
        borrow_as_relation_type, borrow_as_relation_type_mut, borrow_as_role_type, borrow_as_thing_type,
        borrow_as_thing_type_mut,
    },
    ConceptIterator,
};
use crate::{
    bindings::{
        concept::concept::borrow_as_value,
        error::{
            try_release, try_release_map_optional, try_release_optional_string, try_release_string, unwrap_or_default,
            unwrap_void,
        },
        memory::{array_view, borrow, borrow_optional, release_string, string_view},
    },
    concept::{Annotation, Concept, Transitivity, ValueType},
    transaction::concept::api::{AttributeTypeAPI, EntityTypeAPI, RelationTypeAPI, RoleTypeAPI},
    Transaction,
};

#[no_mangle]
pub extern "C" fn thing_type_is_root(thing_type: *const Concept) -> bool {
    borrow_as_thing_type(thing_type).is_root()
}

#[no_mangle]
pub extern "C" fn thing_type_is_abstract(thing_type: *const Concept) -> bool {
    borrow_as_thing_type(thing_type).is_abstract()
}

#[no_mangle]
pub extern "C" fn thing_type_get_label(thing_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_thing_type(thing_type).label().to_owned())
}

#[no_mangle]
pub extern "C" fn thing_type_is_deleted(transaction: *const Transaction<'static>, thing_type: *const Concept) -> bool {
    unwrap_or_default(borrow_as_thing_type(thing_type).is_deleted(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn thing_type_delete(transaction: *const Transaction<'static>, thing_type: *mut Concept) {
    unwrap_void(borrow_as_thing_type_mut(thing_type).delete(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn thing_type_set_label(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    new_label: *const c_char,
) {
    unwrap_void(borrow_as_thing_type_mut(thing_type).set_label(borrow(transaction), string_view(new_label).to_owned()))
}

#[no_mangle]
pub extern "C" fn thing_type_set_abstract(transaction: *const Transaction<'static>, thing_type: *mut Concept) {
    unwrap_void(borrow_as_thing_type_mut(thing_type).set_abstract(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn thing_type_unset_abstract(transaction: *const Transaction<'static>, thing_type: *mut Concept) {
    unwrap_void(borrow_as_thing_type_mut(thing_type).unset_abstract(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn thing_type_get_owns(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    value_type: *const ValueType,
    transitivity: Transitivity,
    annotations: *const *const Annotation,
) -> *mut ConceptIterator {
    let annotations = array_view(annotations).copied().collect();
    try_release(
        borrow_as_thing_type(thing_type)
            .get_owns(borrow(transaction), borrow_optional(value_type).copied(), transitivity, annotations)
            .map(ConceptIterator::attribute_types),
    )
}

#[no_mangle]
pub extern "C" fn thing_type_get_owns_overridden(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    overridden_attribute_type: *const Concept,
) -> *mut Concept {
    try_release_map_optional(
        borrow_as_thing_type(thing_type)
            .get_owns_overridden(borrow(transaction), borrow_as_attribute_type(overridden_attribute_type).clone())
            .transpose(),
        Concept::AttributeType,
    )
}

#[no_mangle]
pub extern "C" fn thing_type_set_owns(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    attribute_type: *const Concept,
    overridden_attribute_type: *const Concept,
    annotations: *const *const Annotation,
) {
    let annotations = array_view(annotations).copied().collect();
    unwrap_void(borrow_as_thing_type_mut(thing_type).set_owns(
        borrow(transaction),
        borrow_as_attribute_type(attribute_type).clone(),
        borrow_optional(overridden_attribute_type).map(|at| borrow_as_attribute_type(at).clone()),
        annotations,
    ))
}

#[no_mangle]
pub extern "C" fn thing_type_unset_owns(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    attribute_type: *const Concept,
) {
    unwrap_void(
        borrow_as_thing_type_mut(thing_type)
            .unset_owns(borrow(transaction), borrow_as_attribute_type(attribute_type).clone()),
    )
}

#[no_mangle]
pub extern "C" fn thing_type_get_plays(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_thing_type(thing_type).get_plays(borrow(transaction), transitivity).map(ConceptIterator::role_types),
    )
}

#[no_mangle]
pub extern "C" fn thing_type_get_plays_overridden(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    overridden_role_type: *const Concept,
) -> *mut Concept {
    try_release_map_optional(
        borrow_as_thing_type(thing_type)
            .get_plays_overridden(borrow(transaction), borrow_as_role_type(overridden_role_type).clone())
            .transpose(),
        Concept::RoleType,
    )
}

#[no_mangle]
pub extern "C" fn thing_type_set_plays(
    transaction: *mut Transaction<'static>,
    thing_type: *mut Concept,
    role_type: *const Concept,
    overridden_role_type: *const Concept,
) {
    unwrap_void(borrow_as_thing_type_mut(thing_type).set_plays(
        borrow(transaction),
        borrow_as_role_type(role_type).clone(),
        borrow_optional(overridden_role_type).map(|c| borrow_as_role_type(c).clone()),
    ))
}

#[no_mangle]
pub extern "C" fn thing_type_unset_plays(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    role_type: *const Concept,
) {
    unwrap_void(
        borrow_as_thing_type_mut(thing_type).unset_plays(borrow(transaction), borrow_as_role_type(role_type).clone()),
    )
}

#[no_mangle]
pub extern "C" fn thing_type_get_syntax(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
) -> *mut c_char {
    try_release_string(borrow_as_thing_type(thing_type).get_syntax(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn entity_type_create(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_entity_type(entity_type).create(borrow(transaction)).map(Concept::Entity))
}

#[no_mangle]
pub extern "C" fn entity_type_get_supertype(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_entity_type(entity_type).get_supertype(borrow(transaction)).map(Concept::EntityType))
}

#[no_mangle]
pub extern "C" fn entity_type_set_supertype(
    transaction: *mut Transaction<'static>,
    entity_type: *mut Concept,
    supertype: *const Concept,
) {
    unwrap_void(
        borrow_as_entity_type_mut(entity_type)
            .set_supertype(borrow(transaction), borrow_as_entity_type(supertype).clone()),
    )
}

#[no_mangle]
pub extern "C" fn entity_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_entity_type(entity_type).get_supertypes(borrow(transaction)).map(ConceptIterator::entity_types),
    )
}

#[no_mangle]
pub extern "C" fn entity_type_get_subtypes(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_entity_type(entity_type)
            .get_subtypes(borrow(transaction), transitivity)
            .map(ConceptIterator::entity_types),
    )
}

#[no_mangle]
pub extern "C" fn entity_type_get_instances(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_entity_type(entity_type)
            .get_instances(borrow(transaction), transitivity)
            .map(ConceptIterator::entities),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_create(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_relation_type(relation_type).create(borrow(transaction)).map(Concept::Relation))
}

#[no_mangle]
pub extern "C" fn relation_type_get_supertype(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_relation_type(relation_type).get_supertype(borrow(transaction)).map(Concept::RelationType))
}

#[no_mangle]
pub extern "C" fn relation_type_set_supertype(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    supertype: *const Concept,
) {
    unwrap_void(
        borrow_as_relation_type_mut(relation_type)
            .set_supertype(borrow(transaction), borrow_as_relation_type(supertype).clone()),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_relation_type(relation_type).get_supertypes(borrow(transaction)).map(ConceptIterator::relation_types),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_subtypes(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_relation_type(relation_type)
            .get_subtypes(borrow(transaction), transitivity)
            .map(ConceptIterator::relation_types),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_instances(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_relation_type(relation_type)
            .get_instances(borrow(transaction), transitivity)
            .map(ConceptIterator::relations),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_relates(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_relation_type(relation_type)
            .get_relates(borrow(transaction), transitivity)
            .map(ConceptIterator::role_types),
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_relates_for_role_label(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    role_label: *const c_char,
) -> *mut Concept {
    try_release_map_optional(
        borrow_as_relation_type(relation_type)
            .get_relates_for_role_label(borrow(transaction), string_view(role_label).to_owned())
            .transpose(),
        Concept::RoleType,
    )
}

#[no_mangle]
pub extern "C" fn relation_type_get_relates_overridden(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    overridden_role_label: *const c_char,
) -> *mut Concept {
    try_release_map_optional(
        borrow_as_relation_type(relation_type)
            .get_relates_overridden(borrow(transaction), string_view(overridden_role_label).to_owned())
            .transpose(),
        Concept::RoleType,
    )
}

#[no_mangle]
pub extern "C" fn relation_type_set_relates(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    role_label: *const c_char,
    overridden_role_label: *const c_char,
) {
    unsafe {
        unwrap_void(borrow_as_relation_type_mut(relation_type).set_relates(
            borrow(transaction),
            string_view(role_label).to_owned(),
            overridden_role_label.as_ref().map(|p| string_view(p).to_owned()),
        ))
    }
}

#[no_mangle]
pub extern "C" fn relation_type_unset_relates(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    role_label: *const c_char,
) {
    unwrap_void(
        borrow_as_relation_type_mut(relation_type)
            .unset_relates(borrow(transaction), string_view(role_label).to_owned()),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_value_type(attribute_type: *const Concept) -> ValueType {
    borrow_as_attribute_type(attribute_type).value_type()
}

#[no_mangle]
pub extern "C" fn attribute_type_put(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    value: *const Concept,
) -> *mut Concept {
    try_release(
        borrow_as_attribute_type(attribute_type)
            .put(borrow(transaction), borrow_as_value(value).clone())
            .map(Concept::Attribute),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    value: *const Concept,
) -> *mut Concept {
    try_release_map_optional(
        borrow_as_attribute_type(attribute_type).get(borrow(transaction), borrow_as_value(value).clone()).transpose(),
        Concept::Attribute,
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_supertype(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_attribute_type(attribute_type).get_supertype(borrow(transaction)).map(Concept::AttributeType))
}

#[no_mangle]
pub extern "C" fn attribute_type_set_supertype(
    transaction: *mut Transaction<'static>,
    attribute_type: *mut Concept,
    supertype: *const Concept,
) {
    unwrap_void(
        borrow_as_attribute_type_mut(attribute_type)
            .set_supertype(borrow(transaction), borrow_as_attribute_type(supertype).clone()),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_attribute_type(attribute_type)
            .get_supertypes(borrow(transaction))
            .map(ConceptIterator::attribute_types),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_subtypes(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_attribute_type(attribute_type)
            .get_subtypes(borrow(transaction), transitivity)
            .map(ConceptIterator::attribute_types),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_subtypes_with_value_type(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    value_type: ValueType,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_attribute_type(attribute_type)
            .get_subtypes_with_value_type(borrow(transaction), value_type, transitivity)
            .map(ConceptIterator::attribute_types),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_instances(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_attribute_type(attribute_type)
            .get_instances(borrow(transaction), transitivity)
            .map(ConceptIterator::attributes),
    )
}

#[no_mangle]
pub extern "C" fn attribute_type_get_regex(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut c_char {
    try_release_optional_string(borrow_as_attribute_type(attribute_type).get_regex(borrow(transaction)).transpose())
}

#[no_mangle]
pub extern "C" fn attribute_type_set_regex(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    regex: *const c_char,
) {
    unwrap_void(borrow_as_attribute_type(attribute_type).set_regex(borrow(transaction), string_view(regex).to_owned()))
}

#[no_mangle]
pub extern "C" fn attribute_type_unset_regex(transaction: *mut Transaction<'static>, attribute_type: *const Concept) {
    unwrap_void(borrow_as_attribute_type(attribute_type).unset_regex(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn attribute_type_get_owners(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    transitivity: Transitivity,
    annotations: *const *const Annotation,
) -> *mut ConceptIterator {
    let annotations = array_view(annotations).copied().collect();
    try_release(
        borrow_as_attribute_type(attribute_type)
            .get_owners(borrow(transaction), transitivity, annotations)
            .map(ConceptIterator::thing_types),
    )
}

#[no_mangle]
pub extern "C" fn role_type_is_abstract(role_type: *const Concept) -> bool {
    borrow_as_role_type(role_type).is_abstract()
}

#[no_mangle]
pub extern "C" fn role_type_is_deleted(transaction: *const Transaction<'static>, role_type: *const Concept) -> bool {
    unwrap_or_default(borrow_as_role_type(role_type).is_deleted(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn role_type_get_relation_type(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut Concept {
    try_release(
        borrow_as_role_type(role_type)
            .get_relation_type(borrow(transaction))
            .transpose()
            .unwrap()
            .map(Concept::RelationType),
    )
}

#[no_mangle]
pub extern "C" fn role_type_delete(transaction: *mut Transaction<'static>, role_type: *const Concept) {
    unwrap_void(borrow_as_role_type(role_type).delete(borrow(transaction)))
}

#[no_mangle]
pub extern "C" fn role_type_get_scope(role_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_role_type(role_type).label.scope.clone())
}

#[no_mangle]
pub extern "C" fn role_type_get_name(role_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_role_type(role_type).label.name.clone())
}

#[no_mangle]
pub extern "C" fn role_type_is_root(role_type: *const Concept) -> bool {
    borrow_as_role_type(role_type).is_root
}

#[no_mangle]
pub extern "C" fn role_type_set_label(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    new_label: *const c_char,
) {
    unwrap_void(borrow_as_role_type(role_type).set_label(borrow(transaction), string_view(new_label).to_owned()))
}

#[no_mangle]
pub extern "C" fn role_type_get_supertype(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut Concept {
    try_release(borrow_as_role_type(role_type).get_supertype(borrow(transaction)).map(Concept::RoleType))
}

#[no_mangle]
pub extern "C" fn role_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(borrow_as_role_type(role_type).get_supertypes(borrow(transaction)).map(ConceptIterator::role_types))
}

#[no_mangle]
pub extern "C" fn role_type_get_subtypes(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type).get_subtypes(borrow(transaction), transitivity).map(ConceptIterator::role_types),
    )
}

#[no_mangle]
pub extern "C" fn role_type_get_relation_types(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type).get_relation_types(borrow(transaction)).map(ConceptIterator::relation_types),
    )
}

#[no_mangle]
pub extern "C" fn role_type_get_player_types(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type)
            .get_player_types(borrow(transaction), transitivity)
            .map(ConceptIterator::thing_types),
    )
}

#[no_mangle]
pub extern "C" fn role_type_get_relation_instances(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type)
            .get_relation_instances(borrow(transaction), transitivity)
            .map(ConceptIterator::relations),
    )
}

#[no_mangle]
pub extern "C" fn role_type_get_player_instances(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    transitivity: Transitivity,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type)
            .get_player_instances(borrow(transaction), transitivity)
            .map(ConceptIterator::things),
    )
}
