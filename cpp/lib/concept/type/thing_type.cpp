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

#include <variant>

#include "typedb/concept/type/thing_type.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "../../common/macros.hpp"
#include "../../common/native.hpp"
#include "../../common/utils.hpp"
#include "../concept_factory.hpp"

namespace TypeDB {

VoidFuture setOwnsNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, std::variant<const std::vector<Annotation>*, const std::initializer_list<Annotation>*> annotations);
ConceptIterable<AttributeType> getOwnsNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, ValueType* valueType, std::variant<const std::vector<Annotation>*, const std::initializer_list<Annotation>*> annotations, Transitivity transitivity);

ThingType::ThingType(ConceptType conceptType, _native::Concept* conceptNative)
    : Type(conceptType, conceptNative) {}

std::string ThingType::getLabel() {
    CHECK_NATIVE(conceptNative);
    return Utils::stringFromNative(_native::thing_type_get_label(conceptNative.get()));
}

VoidFuture ThingType::deleteType(Transaction& transaction) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_delete(ConceptFactory::getNative(transaction), conceptNative.get()));
}

BoolFuture ThingType::isDeleted(Transaction& transaction) {
    CONCEPTAPI_CALL(BoolFuture, _native::thing_type_is_deleted(ConceptFactory::getNative(transaction), conceptNative.get()));
}

bool ThingType::isRoot() {
    return conceptType == ConceptType::ROOT_THING_TYPE;
}

bool ThingType::isAbstract() {
    CHECK_NATIVE(conceptNative);
    return _native::thing_type_is_abstract(conceptNative.get());
}

VoidFuture ThingType::setLabel(Transaction& transaction, const std::string& newLabel) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_set_label(ConceptFactory::getNative(transaction), conceptNative.get(), newLabel.c_str()));
}

VoidFuture ThingType::setAbstract(Transaction& transaction) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_set_abstract(ConceptFactory::getNative(transaction), conceptNative.get()));
}

VoidFuture ThingType::unsetAbstract(Transaction& transaction) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_unset_abstract(ConceptFactory::getNative(transaction), conceptNative.get()));
}

VoidFuture ThingType::setPlays(Transaction& transaction, RoleType* roleType) {
    return setPlays(transaction, roleType, nullptr);
}

VoidFuture ThingType::setPlays(Transaction& transaction, RoleType* roleType, RoleType* overriddenRoleType) {
    auto nativeOverriddenRoleType = overriddenRoleType != nullptr ? ConceptFactory::getNative(overriddenRoleType) : nullptr;
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_set_plays(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(roleType), nativeOverriddenRoleType));
}

VoidFuture ThingType::setOwns(Transaction& transaction, AttributeType* attributeType, const std::initializer_list<Annotation>& annotations) {
    return setOwnsNative(conceptNative, transaction, attributeType, nullptr, &annotations);
}

VoidFuture ThingType::setOwns(Transaction& transaction, AttributeType* attributeType, const std::vector<Annotation>& annotations) {
    return setOwnsNative(conceptNative, transaction, attributeType, nullptr, &annotations);
}

VoidFuture ThingType::setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::initializer_list<Annotation>& annotations) {
    return setOwnsNative(conceptNative, transaction, attributeType, overriddenType, &annotations);
}

VoidFuture ThingType::setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::vector<Annotation>& annotations) {
    return setOwnsNative(conceptNative, transaction, attributeType, overriddenType, &annotations);
}

ConceptIterable<RoleType> ThingType::getPlays(Transaction& transaction, Transitivity transitivity) {
    CONCEPTAPI_ITER(RoleType, _native::thing_type_get_plays(ConceptFactory::getNative(transaction), conceptNative.get(), (_native::Transitivity)transitivity));
}

ConceptPtrFuture<RoleType> ThingType::getPlaysOverridden(Transaction& transaction, RoleType* roleType) {
    CONCEPTAPI_FUTURE(RoleType, _native::thing_type_get_plays_overridden(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(roleType)));
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, nullptr, {}, transitivity);
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, nullptr, &annotations, transitivity);
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, nullptr, &annotations, transitivity);
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, ValueType valueType, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, &valueType, {}, transitivity);
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, ValueType valueType, const std::initializer_list<Annotation>& annotations, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, &valueType, &annotations, transitivity);
}

ConceptIterable<AttributeType> ThingType::getOwns(Transaction& transaction, ValueType valueType, const std::vector<Annotation>& annotations, Transitivity transitivity) {
    return getOwnsNative(conceptNative, transaction, &valueType, &annotations, transitivity);
}

ConceptPtrFuture<AttributeType> ThingType::getOwnsOverridden(Transaction& transaction, AttributeType* attributeType) {
    CONCEPTAPI_FUTURE(AttributeType, _native::thing_type_get_owns_overridden(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(attributeType)));
}
VoidFuture ThingType::unsetOwns(Transaction& transaction, AttributeType* attributeType) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_unset_owns(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(attributeType)));
}
VoidFuture ThingType::unsetPlays(Transaction& transaction, RoleType* roleType) {
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_unset_plays(ConceptFactory::getNative(transaction), conceptNative.get(), ConceptFactory::getNative(roleType)));
}

StringFuture ThingType::getSyntax(Transaction& transaction) {
    CONCEPTAPI_CALL(StringFuture, _native::thing_type_get_syntax(ConceptFactory::getNative(transaction), conceptNative.get()));
}

ConceptPtrFuture<ThingType> ThingType::getSupertype(Transaction& transaction) {
    return ConceptPtrFuture<ThingType>(getSuperTypeFutureNative(transaction));
}

ConceptIterable<ThingType> ThingType::getSupertypes(Transaction& transaction) {
    return ConceptIterable<ThingType>(getSupertypesIteratorNative(transaction));
}

ConceptIterable<ThingType> ThingType::getSubtypes(Transaction& transaction, Transitivity transitivity) {
    return ConceptIterable<ThingType>(getSubtypesIteratorNative(transaction, transitivity));
}

// private
VoidFuture setOwnsNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, std::variant<const std::vector<Annotation>*, const std::initializer_list<Annotation>*> annotations) {
    auto overriddenTypeNative = overriddenType != nullptr ? ConceptFactory::getNative(overriddenType) : nullptr;
    std::vector<const _native::Annotation*> nativeAnnotations = (0 == annotations.index()) ? ConceptFactory::nativeAnnotationArray(std::get<0>(annotations)) : ConceptFactory::nativeAnnotationArray(std::get<1>(annotations));
    CONCEPTAPI_CALL(VoidFuture, _native::thing_type_set_owns(ConceptFactory::getNative(transaction), conceptNative.get(),
                                                             ConceptFactory::getNative(attributeType), overriddenTypeNative, nativeAnnotations.data()));
}

ConceptIterable<AttributeType> getOwnsNative(const NativePointer<_native::Concept>& conceptNative, Transaction& transaction, ValueType* valueType, std::variant<const std::vector<Annotation>*, const std::initializer_list<Annotation>*> annotations, Transitivity transitivity) {
    std::vector<const _native::Annotation*> nativeAnnotations = (0 == annotations.index()) ? ConceptFactory::nativeAnnotationArray(std::get<0>(annotations)) : ConceptFactory::nativeAnnotationArray(std::get<1>(annotations));
    CONCEPTAPI_ITER(
        AttributeType,
        _native::thing_type_get_owns(ConceptFactory::getNative(transaction), conceptNative.get(),
                                     (_native::ValueType*)valueType, (_native::Transitivity)transitivity, nativeAnnotations.data()));
}

// protected
RootThingType::RootThingType(_native::Concept* conceptNative)
    : ThingType(ConceptType::ROOT_THING_TYPE, conceptNative) {}

}  // namespace TypeDB
