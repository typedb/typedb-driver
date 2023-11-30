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

#include "typedb/common/exception.hpp"

#include "inc/conceptfactory.hpp"
#include "typedb/concept/concept.hpp"

namespace TypeDB {
const char* ConceptTypeNames[9] = {
    "root_thing_type",

    "entity_type",
    "attribute_type",
    "relation_type",
    "role_type",

    "entity",
    "attribute",
    "relation",

    "value",
};

// Annotation
Annotation::Annotation(_native::Annotation* annotationNative)
    : annotationNative(annotationNative, _native::annotation_drop) {}

Annotation Annotation::key() {
    return Annotation(_native::annotation_new_key());
}
Annotation Annotation::unique() {
    return Annotation(_native::annotation_new_unique());
}

const std::vector<Annotation> Annotation::NONE = {};

std::unique_ptr<Concept> Concept::ofNative(_native::Concept* conceptNative) {
    return ConceptFactory::ofNative(conceptNative);
}

Concept::Concept(ConceptType conceptType, _native::Concept* conceptNative)
    : conceptType(conceptType), conceptNative(conceptNative, _native::concept_drop) {
}

Concept::~Concept() {}

ConceptType Concept::getConceptType() {
    return conceptType;
}

bool Concept::isThingType() {
    switch (conceptType) {
        case ConceptType::ATTRIBUTE_TYPE:
        case ConceptType::ENTITY_TYPE:
        case ConceptType::RELATION_TYPE:
        case ConceptType::ROOT_THING_TYPE:
            return true;
        default:
            return false;
    }
}

bool Concept::isAttributeType() {
    return conceptType == ConceptType::ATTRIBUTE_TYPE;
}

bool Concept::isEntityType() {
    return conceptType == ConceptType::ENTITY_TYPE;
}

bool Concept::isRelationType() {
    return conceptType == ConceptType::RELATION_TYPE;
}

bool Concept::isRoleType() {
    return conceptType == ConceptType::ROLE_TYPE;
}


bool Concept::isThing() {
    switch (conceptType) {
        case ConceptType::ATTRIBUTE:
        case ConceptType::ENTITY:
        case ConceptType::RELATION:
            return true;
        default:
            return false;
    }
}

bool Concept::isAttribute() {
    return conceptType == ConceptType::ATTRIBUTE;
}

bool Concept::isEntity() {
    return conceptType == ConceptType::ENTITY;
}

bool Concept::isRelation() {
    return conceptType == ConceptType::RELATION;
}

bool Concept::isValue() {
    return conceptType == ConceptType::VALUE;
}

ThingType* Concept::asThingType() {
    if (!isThingType()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], "thingType");
    return dynamic_cast<ThingType*>(this);
}

EntityType* Concept::asEntityType() {
    if (!isEntityType()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::ENTITY_TYPE]);
    return dynamic_cast<EntityType*>(this);
}

AttributeType* Concept::asAttributeType() {
    if (!isAttributeType()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::ATTRIBUTE_TYPE]);
    return dynamic_cast<AttributeType*>(this);
}

RelationType* Concept::asRelationType() {
    if (!isRelationType()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::RELATION_TYPE]);
    return dynamic_cast<RelationType*>(this);
}

RoleType* Concept::asRoleType() {
    if (!isRoleType()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::ROLE_TYPE]);
    return dynamic_cast<RoleType*>(this);
}


Thing* Concept::asThing() {
    if (!isThing()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], "thing");
    return dynamic_cast<Thing*>(this);
}

Attribute* Concept::asAttribute() {
    if (!isAttribute()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::ATTRIBUTE]);
    return dynamic_cast<Attribute*>(this);
}

Entity* Concept::asEntity() {
    if (!isEntity()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::ENTITY]);
    return dynamic_cast<Entity*>(this);
}

Relation* Concept::asRelation() {
    if (!isRelation()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::RELATION]);
    return dynamic_cast<Relation*>(this);
}


Value* Concept::asValue() {
    if (!isValue()) throw Utils::exception(&ConceptError::INVALID_CONCEPT_CASTING, ConceptTypeNames[(int)conceptType], ConceptTypeNames[(int)ConceptType::VALUE]);
    return dynamic_cast<Value*>(this);
}

bool Concept::operator==(const Concept& other) {
    return _native::concept_equals(conceptNative.get(), ConceptFactory::getNative(&other));
}

bool Concept::equals(Concept* first, Concept* second) {
    return _native::concept_equals(ConceptFactory::getNative(first), ConceptFactory::getNative(second));
}

}  // namespace TypeDB
