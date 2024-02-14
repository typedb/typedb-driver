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

use typedb_driver::{
    concept::{Annotation, Concept, Transitivity, ValueType},
    transaction::concept::api::{AttributeTypeAPI, EntityTypeAPI, RelationTypeAPI, RoleTypeAPI},
    Promise, Transaction,
};

use super::{
    concept::{
        borrow_as_attribute_type, borrow_as_attribute_type_mut, borrow_as_entity_type, borrow_as_entity_type_mut,
        borrow_as_relation_type, borrow_as_relation_type_mut, borrow_as_role_type, borrow_as_thing_type,
        borrow_as_thing_type_mut,
    },
    ConceptIterator, ConceptPromise,
};
use crate::{
    concept::concept::borrow_as_value,
    error::try_release,
    memory::{array_view, borrow, borrow_optional, release, release_string, string_view},
    promise::{BoolPromise, StringPromise, VoidPromise},
};

/// Checks if this type is a root type (""entity"", ""relation"", ""attribute"")
#[no_mangle]
pub extern "C" fn thing_type_is_root(thing_type: *const Concept) -> bool {
    borrow_as_thing_type(thing_type).is_root()
}

/// Checks if this thing type is prevented from having data instances (i.e., abstract).
#[no_mangle]
pub extern "C" fn thing_type_is_abstract(thing_type: *const Concept) -> bool {
    borrow_as_thing_type(thing_type).is_abstract()
}

/// Retrieves the unique label of the thing type.
#[no_mangle]
pub extern "C" fn thing_type_get_label(thing_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_thing_type(thing_type).label().to_owned())
}

/// Checks if the thing type has been deleted
#[no_mangle]
pub extern "C" fn thing_type_is_deleted(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
) -> *mut BoolPromise {
    release(BoolPromise(Box::new(borrow_as_thing_type(thing_type).is_deleted(borrow(transaction)))))
}

/// Deletes this thing type from the database.
#[no_mangle]
pub extern "C" fn thing_type_delete(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_thing_type_mut(thing_type).delete(borrow(transaction)))))
}

/// Renames the label of the type. The new label must remain unique.
#[no_mangle]
pub extern "C" fn thing_type_set_label(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    new_label: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_thing_type_mut(thing_type).set_label(borrow(transaction), string_view(new_label).to_owned()),
    )))
}

/// Set a <code>ThingType</code> to be abstract, meaning it cannot have instances.
#[no_mangle]
pub extern "C" fn thing_type_set_abstract(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_thing_type_mut(thing_type).set_abstract(borrow(transaction)))))
}

/// Set a <code>ThingType</code> to be non-abstract, meaning it can have instances.
#[no_mangle]
pub extern "C" fn thing_type_unset_abstract(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_thing_type_mut(thing_type).unset_abstract(borrow(transaction)))))
}

/// Retrieves <code>AttributeType</code> that the instances of this
/// <code>ThingType</code> are allowed to own directly or via inheritance.
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

/// Retrieves the <code>AttributeType</code>, ownership of which is overridden
///  for this <code>ThingType</code> by the specified <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn thing_type_get_owns_overridden(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    overridden_attribute_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::attribute_type(
        borrow_as_thing_type(thing_type)
            .get_owns_overridden(borrow(transaction), borrow_as_attribute_type(overridden_attribute_type).clone()),
    ))
}

/// Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
///  Optionally, overriding a previously declared ownership.
///  With the specified annotations to the ownership.
///
/// @param transaction The current transaction
/// @param thing_type The thing type which is to own the specified attribute
/// @param attribute_type The attribute type which is to be owned by the specified thing type
/// @param overridden_attribute_type Optional, The attribute whose ownership must be overridden
/// @param annotations A null-terminated array of <code>Annotation</code>s to be added to the ownership
#[no_mangle]
pub extern "C" fn thing_type_set_owns(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    attribute_type: *const Concept,
    overridden_attribute_type: *const Concept,
    annotations: *const *const Annotation,
) -> *mut VoidPromise {
    let annotations = array_view(annotations).copied().collect();
    release(VoidPromise(Box::new(borrow_as_thing_type_mut(thing_type).set_owns(
        borrow(transaction),
        borrow_as_attribute_type(attribute_type).clone(),
        borrow_optional(overridden_attribute_type).map(|at| borrow_as_attribute_type(at).clone()),
        annotations,
    ))))
}

/// Disallows the instances of this <code>ThingType</code> from owning the given <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn thing_type_unset_owns(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    attribute_type: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_thing_type_mut(thing_type)
            .unset_owns(borrow(transaction), borrow_as_attribute_type(attribute_type).clone()),
    )))
}

/// Retrieves all direct and inherited (or direct only) roles that are allowed
/// to be played by the instances of this <code>ThingType</code>.
/// Specify <code>Transitive</code> for direct and inherited roles,
/// Or <code>Explicit</code> for directly played roles only.
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

/// Retrieves the <code>RoleType</code> that is overridden by the given ``RoleType`` for this <code>ThingType</code>.
#[no_mangle]
pub extern "C" fn thing_type_get_plays_overridden(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
    overridden_role_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::role_type(
        borrow_as_thing_type(thing_type)
            .get_plays_overridden(borrow(transaction), borrow_as_role_type(overridden_role_type).clone()),
    ))
}

/// Allows the instances of this <code>ThingType</code> to play the given role.
/// Optionally, overriding the existing ability to play a role.
#[no_mangle]
pub extern "C" fn thing_type_set_plays(
    transaction: *mut Transaction<'static>,
    thing_type: *mut Concept,
    role_type: *const Concept,
    overridden_role_type: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_thing_type_mut(thing_type).set_plays(
        borrow(transaction),
        borrow_as_role_type(role_type).clone(),
        borrow_optional(overridden_role_type).map(|c| borrow_as_role_type(c).clone()),
    ))))
}

/// Disallows the instances of this <code>ThingType</code> from playing the given role.
#[no_mangle]
pub extern "C" fn thing_type_unset_plays(
    transaction: *const Transaction<'static>,
    thing_type: *mut Concept,
    role_type: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_thing_type_mut(thing_type).unset_plays(borrow(transaction), borrow_as_role_type(role_type).clone()),
    )))
}

/// Produces a TypeQL pattern for creating this <code>ThingType</code> in a <code>define</code> query.
#[no_mangle]
pub extern "C" fn thing_type_get_syntax(
    transaction: *const Transaction<'static>,
    thing_type: *const Concept,
) -> *mut StringPromise {
    let promise = borrow_as_thing_type(thing_type).get_syntax(borrow(transaction));
    release(StringPromise(Box::new(|| promise.resolve().map(Some))))
}

/// Creates and returns a new instance of this <code>EntityType</code>.
#[no_mangle]
pub extern "C" fn entity_type_create(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut ConceptPromise {
    let promise = borrow_as_entity_type(entity_type).create(borrow(transaction));
    release(ConceptPromise::entity(|| promise.resolve().map(Some)))
}

/// Retrieves the most immediate supertype of the given ``EntityType``.
#[no_mangle]
pub extern "C" fn entity_type_get_supertype(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::entity_type(borrow_as_entity_type(entity_type).get_supertype(borrow(transaction))))
}

/// Sets the supplied <code>EntityType</code> as the supertype of the current <code>EntityType</code>.
#[no_mangle]
pub extern "C" fn entity_type_set_supertype(
    transaction: *mut Transaction<'static>,
    entity_type: *mut Concept,
    supertype: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_entity_type_mut(entity_type)
            .set_supertype(borrow(transaction), borrow_as_entity_type(supertype).clone()),
    )))
}

/// Retrieves all supertypes of the given EntityType.
#[no_mangle]
pub extern "C" fn entity_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    entity_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_entity_type(entity_type).get_supertypes(borrow(transaction)).map(ConceptIterator::entity_types),
    )
}

/// Retrieves all direct and indirect (or direct only) subtypes of the given EntityType.
/// Specify <code>Transitive</code> for direct and indirect subtypes,
/// Or <code>Explicit</code> for directly subtypes only.
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

/// Retrieves all <code>Entity</code> objects that are instances of this <code>EntityType</code> or its subtypes.
/// Specify <code>Transitive</code> for instances of this EntityType and subtypes,
/// Or <code>Explicit</code> of this EntityType only
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

/// Creates and returns a new instance of this <code>RelationType</code>.
#[no_mangle]
pub extern "C" fn relation_type_create(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut ConceptPromise {
    let promise = borrow_as_relation_type(relation_type).create(borrow(transaction));
    release(ConceptPromise::relation(|| promise.resolve().map(Some)))
}

/// Retrieves the most immediate supertype of the given ``RelationType``.
#[no_mangle]
pub extern "C" fn relation_type_get_supertype(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::relation_type(borrow_as_relation_type(relation_type).get_supertype(borrow(transaction))))
}

/// Sets the supplied <code>RelationType</code> as the supertype of the current <code>RelationType</code>.
#[no_mangle]
pub extern "C" fn relation_type_set_supertype(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    supertype: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_relation_type_mut(relation_type)
            .set_supertype(borrow(transaction), borrow_as_relation_type(supertype).clone()),
    )))
}

/// Retrieves all supertypes of the given RelationType.
#[no_mangle]
pub extern "C" fn relation_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_relation_type(relation_type).get_supertypes(borrow(transaction)).map(ConceptIterator::relation_types),
    )
}

/// Retrieves all direct and indirect (or direct only) subtypes of the given RelationType.
/// Specify <code>Transitive</code> for direct and indirect subtypes,
/// Or <code>Explicit</code> for directly subtypes only.
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

/// Retrieves all <code>Relation</code> objects that are instances of this <code>RelationType</code> or its subtypes.
/// Specify <code>Transitive</code> for instances of this RelationType and subtypes,
/// Or <code>Explicit</code> of this RelationType only
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

/// Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
/// Specify <code>Transitive</code> for direct and inherited relates
/// Or <code>Explicit</code> for direct relates only
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

/// Retrieves the role with the specified label that this <code>RelationType</code> relates to, directly or via inheritance.
#[no_mangle]
pub extern "C" fn relation_type_get_relates_for_role_label(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    role_label: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::role_type(
        borrow_as_relation_type(relation_type)
            .get_relates_for_role_label(borrow(transaction), string_view(role_label).to_owned()),
    ))
}

/// Retrieves the <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
#[no_mangle]
pub extern "C" fn relation_type_get_relates_overridden(
    transaction: *mut Transaction<'static>,
    relation_type: *const Concept,
    overridden_role_label: *const c_char,
) -> *mut ConceptPromise {
    release(ConceptPromise::role_type(
        borrow_as_relation_type(relation_type)
            .get_relates_overridden(borrow(transaction), string_view(overridden_role_label).to_owned()),
    ))
}

/// Sets the new role that this <code>RelationType</code> relates to.
/// If we are setting an overriding type this way, we have to also pass overridden_role_label.
#[no_mangle]
pub extern "C" fn relation_type_set_relates(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    role_label: *const c_char,
    overridden_role_label: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_relation_type_mut(relation_type).set_relates(
        borrow(transaction),
        string_view(role_label).to_owned(),
        unsafe { overridden_role_label.as_ref().map(|p| string_view(p).to_owned()) },
    ))))
}

/// Disallows this <code>RelationType</code> from relating to the given role.
#[no_mangle]
pub extern "C" fn relation_type_unset_relates(
    transaction: *mut Transaction<'static>,
    relation_type: *mut Concept,
    role_label: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_relation_type_mut(relation_type)
            .unset_relates(borrow(transaction), string_view(role_label).to_owned()),
    )))
}

/// Retrieves the <code>ValueType</code> of this <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn attribute_type_get_value_type(attribute_type: *const Concept) -> ValueType {
    borrow_as_attribute_type(attribute_type).value_type()
}

/// Creates and returns a new instance of this <code>AttributeType</code>, with the specified ``Value``.
#[no_mangle]
pub extern "C" fn attribute_type_put(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    value: *const Concept,
) -> *mut ConceptPromise {
    let promise = borrow_as_attribute_type(attribute_type).put(borrow(transaction), borrow_as_value(value).clone());
    release(ConceptPromise::attribute(|| promise.resolve().map(Some)))
}

/// Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given ``Value``
/// if such <code>Attribute</code> exists. Otherwise, returns <code>null</code>.
#[no_mangle]
pub extern "C" fn attribute_type_get(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    value: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::attribute(
        borrow_as_attribute_type(attribute_type).get(borrow(transaction), borrow_as_value(value).clone()),
    ))
}

/// Retrieves the most immediate supertype of the given ``AttributeType``.
#[no_mangle]
pub extern "C" fn attribute_type_get_supertype(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::attribute_type(borrow_as_attribute_type(attribute_type).get_supertype(borrow(transaction))))
}

/// Sets the supplied <code>AttributeType</code> as the supertype of the current <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn attribute_type_set_supertype(
    transaction: *mut Transaction<'static>,
    attribute_type: *mut Concept,
    supertype: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_attribute_type_mut(attribute_type)
            .set_supertype(borrow(transaction), borrow_as_attribute_type(supertype).clone()),
    )))
}

/// Retrieves all supertypes of the given ``AttributeType``.
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

/// Retrieves all direct and indirect (or direct only) subtypes of the given ``AttributeType``.
/// Specify <code>Transitive</code> for direct and indirect subtypes,
/// Or <code>Explicit</code> for directly subtypes only.
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

/// Retrieves subtypes of this <code>AttributeType</code>
/// with given <code>ValueType</code>.
/// Specify <code>Transitive</code> for direct and indirect subtypes,
/// Or <code>Explicit</code> for directly subtypes only.
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

/// Retrieves all <code>Attribute</code> objects that are instances of this <code>AttributeType</code> or its subtypes.
/// Specify <code>Transitive</code> for instances of this AttributeType and subtypes,
/// Or <code>Explicit</code> of this AttributeType only
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

/// Retrieves the regular expression that is defined for this <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn attribute_type_get_regex(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut StringPromise {
    release(StringPromise(Box::new(borrow_as_attribute_type(attribute_type).get_regex(borrow(transaction)))))
}

/// Sets a regular expression as a constraint for this <code>AttributeType</code>. <code>Value</code>s
/// of all <code>Attribute</code>s of this type (inserted earlier or later) should match this regex.
/// <p>Can only be applied for <code>AttributeType</code>s with a <code>string</code> value type.
#[no_mangle]
pub extern "C" fn attribute_type_set_regex(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
    regex: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_attribute_type(attribute_type).set_regex(borrow(transaction), string_view(regex).to_owned()),
    )))
}

/// Removes the regular expression that is defined for this <code>AttributeType</code>.
#[no_mangle]
pub extern "C" fn attribute_type_unset_regex(
    transaction: *mut Transaction<'static>,
    attribute_type: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_attribute_type(attribute_type).unset_regex(borrow(transaction)))))
}

/// Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>
///  directly or through inheritance.
/// Specify <code>Transitive</code> for direct and inherited ownership,
/// or <code>Explicit</code> for direct ownership only
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

/// Checks if the role type is the root role type, ""relation:role""
#[no_mangle]
pub extern "C" fn role_type_is_root(role_type: *const Concept) -> bool {
    borrow_as_role_type(role_type).is_root
}

/// Checks if the role type is prevented from having data instances (i.e., <code>abstract</code>).
#[no_mangle]
pub extern "C" fn role_type_is_abstract(role_type: *const Concept) -> bool {
    borrow_as_role_type(role_type).is_abstract()
}

/// Check if the role type has been deleted
#[no_mangle]
pub extern "C" fn role_type_is_deleted(
    transaction: *const Transaction<'static>,
    role_type: *const Concept,
) -> *mut BoolPromise {
    release(BoolPromise(Box::new(borrow_as_role_type(role_type).is_deleted(borrow(transaction)))))
}

/// Retrieves the <code>RelationType</code> that this role is directly related to.
#[no_mangle]
pub extern "C" fn role_type_get_relation_type(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::relation_type(borrow_as_role_type(role_type).get_relation_type(borrow(transaction))))
}

/// Deletes this role type from the database.
#[no_mangle]
pub extern "C" fn role_type_delete(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(borrow_as_role_type(role_type).delete(borrow(transaction)))))
}

/// Gets the 'scope' of this role type.
/// This corresponds to the label of the <code>RelationType</code> it is directly related to.
#[no_mangle]
pub extern "C" fn role_type_get_scope(role_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_role_type(role_type).label.scope.clone())
}

/// Gets the name of this role type.
#[no_mangle]
pub extern "C" fn role_type_get_name(role_type: *const Concept) -> *mut c_char {
    release_string(borrow_as_role_type(role_type).label.name.clone())
}

/// Renames the label of the type. The new label must remain unique in the hierarchy of a relation type.
#[no_mangle]
pub extern "C" fn role_type_set_label(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
    new_label: *const c_char,
) -> *mut VoidPromise {
    release(VoidPromise(Box::new(
        borrow_as_role_type(role_type).set_label(borrow(transaction), string_view(new_label).to_owned()),
    )))
}

/// Retrieves the most immediate supertype of the given ``RoleType``.
#[no_mangle]
pub extern "C" fn role_type_get_supertype(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptPromise {
    release(ConceptPromise::role_type(borrow_as_role_type(role_type).get_supertype(borrow(transaction))))
}

/// Retrieves all supertypes of the given ``RoleType``.
#[no_mangle]
pub extern "C" fn role_type_get_supertypes(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(borrow_as_role_type(role_type).get_supertypes(borrow(transaction)).map(ConceptIterator::role_types))
}

/// Retrieves all direct and indirect (or direct only) subtypes of the given ``RoleType``.
/// Specify <code>Transitive</code> for direct and indirect subtypes,
/// Or <code>Explicit</code> for directly subtypes only.
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

/// Retrieves <code>RelationType</code>s that this role is related to (directly or indirectly).
#[no_mangle]
pub extern "C" fn role_type_get_relation_types(
    transaction: *mut Transaction<'static>,
    role_type: *const Concept,
) -> *mut ConceptIterator {
    try_release(
        borrow_as_role_type(role_type).get_relation_types(borrow(transaction)).map(ConceptIterator::relation_types),
    )
}

/// Retrieves the <code>ThingType</code>s whose instances play this role.
/// Specify <code>Transitive</code> for direct and indirect playing,
/// or <code>Explicit</code> for direct playing only
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

/// Retrieves the <code>Relation</code> instances that this role is related to.
/// Specify <code>Transitive</code> for direct and indirect relation,
/// or <code>Explicit</code> for direct relation only
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

/// Retrieves the <code>Thing</code> instances that play this role.
/// Specify <code>Transitive</code> for direct and indirect playing,
/// or <code>Explicit</code> for direct playing only
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
