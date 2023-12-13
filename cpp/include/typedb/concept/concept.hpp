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
#pragma once

#include <vector>

#include "typedb/common/future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class Annotation {
public:
    Annotation(Annotation&&) = default;
    Annotation& operator=(Annotation&&) = default;
    ~Annotation() = default;
    static Annotation key();
    static Annotation unique();
    std::string toString();

    static const std::vector<Annotation> NONE;

private:
    Annotation(_native::Annotation*);
    Annotation(const Annotation&) = delete;
    Annotation& operator=(const Annotation&) = delete;

    NativePointer<_native::Annotation> annotationNative;

    friend class ConceptFactory;
};

enum class ConceptType {
    ROOT_THING_TYPE,

    ENTITY_TYPE,
    ATTRIBUTE_TYPE,
    RELATION_TYPE,
    ROLE_TYPE,

    ENTITY,
    ATTRIBUTE,
    RELATION,

    VALUE,
};

enum class ValueType {
    OBJECT,
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING,
    DATETIME,
};

enum class Transitivity {
    EXPLICIT,
    TRANSITIVE,
};

// Forward declarations
class Transaction;

class ThingType;
class EntityType;
class AttributeType;
class RelationType;

class RoleType;

class Thing;
class Entity;
class Attribute;
class Relation;

class Value;


// forward declarations for friendship
class ConceptFactory;

class Concept {
public:
    virtual ~Concept() = default;
    Concept(const Concept&) = delete;
    Concept(Concept&&) = default;
    Concept& operator=(const Concept&) = delete;
    Concept& operator=(Concept&&) = default;

    ConceptType getConceptType();

    bool isThingType();
    bool isEntityType();
    bool isAttributeType();
    bool isRelationType();

    bool isRoleType();

    bool isThing();
    bool isEntity();
    bool isAttribute();
    bool isRelation();

    bool isValue();


    ThingType* asThingType();
    EntityType* asEntityType();
    AttributeType* asAttributeType();
    RelationType* asRelationType();

    RoleType* asRoleType();

    Thing* asThing();
    Attribute* asAttribute();
    Entity* asEntity();
    Relation* asRelation();

    Value* asValue();

    std::string toString();
    bool operator==(const Concept& other);
    static bool equals(Concept* first, Concept* second);

protected:
    ConceptType conceptType;
    NativePointer<_native::Concept> conceptNative;

    Concept(ConceptType conceptType, _native::Concept* conceptNative);

    static std::unique_ptr<Concept> ofNative(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

// ConceptFuture, ConceptIterator & RolePlayerIterator
class ConceptFutureWrapper;
class ConceptIteratorWrapper;

template <typename T>
using ConceptPtrFuture = Future<std::unique_ptr<T>, ConceptFutureWrapper>;

template <typename T>
using ConceptIterable = Iterable<ConceptIteratorWrapper, _native::Concept, std::unique_ptr<T>>;
template <typename T>
using ConceptIterator = Iterator<ConceptIteratorWrapper, _native::Concept, std::unique_ptr<T>>;


}  // namespace TypeDB
