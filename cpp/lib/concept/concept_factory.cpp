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

#include "./concept_factory.hpp"

#include "typedb/common/exception.hpp"
#include "typedb/connection/transaction.hpp"

#include "typedb/concept/type/type.hpp"

#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/concept/type/entity_type.hpp"
#include "typedb/concept/type/relation_type.hpp"
#include "typedb/concept/type/role_type.hpp"
#include "typedb/concept/type/thing_type.hpp"

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"
#include "typedb/concept/thing/thing.hpp"

#include "typedb/concept/value/value.hpp"

#include "../common/macros.hpp"

#define CHECK_NATIVE_CONCEPT(PTR)                                     \
    {                                                                 \
        if (PTR == nullptr) {                                         \
            throw Utils::exception(InternalError::NULL_NATIVE_VALUE); \
        }                                                             \
    }

namespace TypeDB {

_native::Transaction* ConceptFactory::getNative(Transaction& transaction) {
    return transaction.getNative();
}

_native::Concept* ConceptFactory::getNative(const Concept* concept) {
    CHECK_NATIVE(concept->conceptNative);
    return concept->conceptNative.get();
}

_native::Annotation* ConceptFactory::getNative(const Annotation& annotation) {
    CHECK_NATIVE(annotation.annotationNative);
    return annotation.annotationNative.get();
}

std::vector<const _native::Annotation*> ConceptFactory::toNativeArray(const std::vector<Annotation>& annotations) {
    std::vector<const _native::Annotation*> nativeAnnotations;
    nativeAnnotations.reserve(annotations.size() + 1);
    for (auto& annotation : annotations)
        nativeAnnotations.push_back(ConceptFactory::getNative(annotation));
    nativeAnnotations.push_back(nullptr);
    return nativeAnnotations;
}

std::unique_ptr<Concept> ConceptFactory::ofNative(_native::Concept* conceptNative) {
    if (_native::concept_is_entity_type(conceptNative)) return ConceptFactory::entityType(conceptNative);
    else if (_native::concept_is_relation_type(conceptNative)) return ConceptFactory::relationType(conceptNative);
    else if (_native::concept_is_attribute_type(conceptNative)) return ConceptFactory::attributeType(conceptNative);
    else if (_native::concept_is_role_type(conceptNative)) return ConceptFactory::roleType(conceptNative);
    else if (_native::concept_is_root_thing_type(conceptNative)) return ConceptFactory::rootThingType(conceptNative);

    else if (_native::concept_is_entity(conceptNative)) return ConceptFactory::entity(conceptNative);
    else if (_native::concept_is_relation(conceptNative)) return ConceptFactory::relation(conceptNative);
    else if (_native::concept_is_attribute(conceptNative)) return ConceptFactory::attribute(conceptNative);

    else if (_native::concept_is_value(conceptNative)) return ConceptFactory::value(conceptNative);

    else throw Utils::exception(InternalError::UNEXPECTED_NATIVE_VALUE);
}

std::unique_ptr<EntityType> ConceptFactory::entityType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    EntityType* concept = new EntityType(conceptNative);
    return std::unique_ptr<EntityType>(concept);  // Can't use std::make_unique because the constructor is private.
}
std::unique_ptr<AttributeType> ConceptFactory::attributeType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    AttributeType* concept = new AttributeType(conceptNative);
    return std::unique_ptr<AttributeType>(concept);
}
std::unique_ptr<RelationType> ConceptFactory::relationType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    RelationType* concept = new RelationType(conceptNative);
    return std::unique_ptr<RelationType>(concept);
}

std::unique_ptr<ThingType> ConceptFactory::rootThingType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    ThingType* concept = new RootThingType(conceptNative);
    return std::unique_ptr<ThingType>(concept);
}

std::unique_ptr<RoleType> ConceptFactory::roleType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    RoleType* concept = new RoleType(conceptNative);
    return std::unique_ptr<RoleType>(concept);
}

std::unique_ptr<Attribute> ConceptFactory::attribute(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    Attribute* concept = new Attribute(conceptNative);
    return std::unique_ptr<Attribute>(concept);
}
std::unique_ptr<Entity> ConceptFactory::entity(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    Entity* concept = new Entity(conceptNative);
    return std::unique_ptr<Entity>(concept);
}
std::unique_ptr<Relation> ConceptFactory::relation(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    Relation* concept = new Relation(conceptNative);
    return std::unique_ptr<Relation>(concept);
}

std::unique_ptr<Value> ConceptFactory::value(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    Value* concept = new Value(conceptNative);
    return std::unique_ptr<Value>(concept);
}

// non-primitive
std::unique_ptr<Thing> ConceptFactory::thing(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    if (_native::concept_is_entity(conceptNative)) return ConceptFactory::entity(conceptNative);
    else if (_native::concept_is_relation(conceptNative)) return ConceptFactory::relation(conceptNative);
    else if (_native::concept_is_attribute(conceptNative)) return ConceptFactory::attribute(conceptNative);
    else throw Utils::exception(InternalError::UNEXPECTED_NATIVE_VALUE);
}

std::unique_ptr<Type> ConceptFactory::type(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    if (_native::concept_is_entity_type(conceptNative)) return ConceptFactory::entityType(conceptNative);
    else if (_native::concept_is_relation_type(conceptNative)) return ConceptFactory::relationType(conceptNative);
    else if (_native::concept_is_attribute_type(conceptNative)) return ConceptFactory::attributeType(conceptNative);
    else if (_native::concept_is_role_type(conceptNative)) return ConceptFactory::roleType(conceptNative);
    else if (_native::concept_is_root_thing_type(conceptNative)) return ConceptFactory::rootThingType(conceptNative);
    else throw Utils::exception(InternalError::UNEXPECTED_NATIVE_VALUE);
}

std::unique_ptr<ThingType> ConceptFactory::thingType(_native::Concept* conceptNative) {
    CHECK_NATIVE_CONCEPT(conceptNative);
    if (_native::concept_is_entity_type(conceptNative)) return ConceptFactory::entityType(conceptNative);
    else if (_native::concept_is_relation_type(conceptNative)) return ConceptFactory::relationType(conceptNative);
    else if (_native::concept_is_attribute_type(conceptNative)) return ConceptFactory::attributeType(conceptNative);
    else if (_native::concept_is_root_thing_type(conceptNative)) return ConceptFactory::rootThingType(conceptNative);
    else throw Utils::exception(InternalError::UNEXPECTED_NATIVE_VALUE);
}

};  // namespace TypeDB

#undef CHECK_NATIVE_CONCEPT
