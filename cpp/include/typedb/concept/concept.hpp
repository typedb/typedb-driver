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

/**
 * \brief Annotations are used to specify extra schema constraints.
 */
class Annotation {
public:
    Annotation(Annotation&&) = default;
    Annotation& operator=(Annotation&&) = default;
    ~Annotation() = default;

    /**
     * Produces a <code>@key</code> annotation.
     *
     * <h3>Examples</h3>
     * <pre>
     * ThingType.Annotation.key();
     * </pre>
     */
    static Annotation key();

    /**
     * Produces a <code>@unique</code> annotation.
     *
     * <h3>Examples</h3>
     * <pre>
     * Annotation.unique();
     * </pre>
     */
    static Annotation unique();

    /**
     * Checks if this <code>Annotation</code> is a <code>@key</code> annotation.
     *
     * <h3>Examples</h3>
     * <pre>
     * annotation.isKey();
     * </pre>
     */
    bool isKey();

    /**
     * Checks if this <code>Annotation</code> is a <code>@unique</code> annotation.
     *
     * <h3>Examples</h3>
     * <pre>
     * annotation.isUnique();
     * </pre>
     */
    bool isUnique();

    /**
     * A string representation of this Annotation.
     */
    std::string toString();

private:
    Annotation(_native::Annotation*);
    Annotation(const Annotation&) = delete;
    Annotation& operator=(const Annotation&) = delete;

    NativePointer<_native::Annotation> annotationNative;

    friend class ConceptFactory;
};

/**
 * The exact type of a Concept object. Use for downcasting to the appropriate type.
 */
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

/**
 * What type of primitive value is held by a Value or Attribute.
 */
enum class ValueType {
    OBJECT,

    BOOLEAN,
    LONG,
    DOUBLE,
    STRING,
    DATETIME,
};

/**
 * Used in ConceptAPI to specify whether to query only explicit schema constraints
 * or also include transitive ones
 */
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

/**
 * \brief The fundamental TypeQL object. A Concept is either a Type, Thing, or Value.
 */
class Concept {
public:
    virtual ~Concept() = default;
    Concept(const Concept&) = delete;
    Concept(Concept&&) = default;
    Concept& operator=(const Concept&) = delete;
    Concept& operator=(Concept&&) = default;

    /**
     * Returns the ConceptType of this concept.
     *
     * <h3>Examples</h3>
     * <pre>
     * switch(concept.getConceptType()) { ... }
     * </pre>
     */
    ConceptType getConceptType();

    /**
     * Checks if the concept is a <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isThingType();
     * </pre>
     */
    bool isThingType();

    /**
     * Checks if the concept is an <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntityType();
     * </pre>
     */
    bool isEntityType();

    /**
     * Checks if the concept is an <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isAttributeType();
     * </pre>
     */
    bool isAttributeType();

    /**
     * Checks if the concept is a <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelationType();
     * </pre>
     */
    bool isRelationType();

    /**
     * Checks if the concept is a <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRoleType();
     * </pre>
     */
    bool isRoleType();

    /**
     * Checks if the concept is a <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isThing();
     * </pre>
     */
    bool isThing();

    /**
     * Checks if the concept is an <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntity();
     * </pre>
     */
    bool isEntity();

    /**
     * Checks if the concept is a <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelation();
     * </pre>
     */
    bool isAttribute();

    /**
     * Checks if the concept is a <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isValue();
     * </pre>
     */
    bool isRelation();

    /**
     * Checks if the concept is a <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isValue();
     * </pre>
     */
    bool isValue();


    /**
     * Casts the concept to <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asThingType();
     * </pre>
     */
    ThingType* asThingType();

    /**
     * Casts the concept to <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntityType();
     * </pre>
     */
    EntityType* asEntityType();

    /**
     * Casts the concept to <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntityType();
     * </pre>
     */
    AttributeType* asAttributeType();

    /**
     * Casts the concept to <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelationType();
     * </pre>
     */
    RelationType* asRelationType();

    /**
     * Casts the concept to <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRoleType();
     * </pre>
     */
    RoleType* asRoleType();

    /**
     * Casts the concept to <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asThing();
     * </pre>
     */
    Thing* asThing();

    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asAttribute();
     * </pre>
     */
    Attribute* asAttribute();

    /**
     * Casts the concept to <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntity();
     * </pre>
     */
    Entity* asEntity();

    /**
     * Casts the concept to <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelation();
     * </pre>
     */
    Relation* asRelation();

    /**
     * Casts the concept to <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asValue();
     * </pre>
     */
    Value* asValue();

    /**
     * A string representation of this Concept.
     */
    std::string toString();

    /**
     * Checks equality with the <code>other</code> concept.
     */
    bool operator==(const Concept& other);

    /**
     * Checks equality of two concepts.
     */
    static bool equals(Concept* first, Concept* second);

protected:
    /// \private
    ConceptType conceptType;
    /// \private
    NativePointer<_native::Concept> conceptNative;

    Concept(ConceptType conceptType, _native::Concept* conceptNative);

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
