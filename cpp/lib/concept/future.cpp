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
#include "typedb/common/native.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/conceptresultwrapper.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

// ConceptFuture
template <typename T>
std::unique_ptr<T> resolveConceptPtrPromise(ConceptFutureWrapper* wrapper) {
    if (wrapper != nullptr) {
        auto p = wrapper->resolve();
        if (p == nullptr) return std::unique_ptr<T>();  // nullptr
        if constexpr (std::is_same_v<T, Type>) return ConceptFactory::type(p);
        else if constexpr (std::is_same_v<T, RoleType>) return ConceptFactory::roleType(p);

        else if constexpr (std::is_same_v<T, ThingType>) return ConceptFactory::thingType(p);
        else if constexpr (std::is_same_v<T, AttributeType>) return ConceptFactory::attributeType(p);
        else if constexpr (std::is_same_v<T, EntityType>) return ConceptFactory::entityType(p);
        else if constexpr (std::is_same_v<T, RelationType>) return ConceptFactory::relationType(p);

        else if constexpr (std::is_same_v<T, Thing>) return ConceptFactory::thing(p);
        else if constexpr (std::is_same_v<T, Attribute>) return ConceptFactory::attribute(p);
        else if constexpr (std::is_same_v<T, Entity>) return ConceptFactory::entity(p);
        else if constexpr (std::is_same_v<T, Relation>) return ConceptFactory::relation(p);

        else if constexpr (std::is_same_v<T, Value>) return ConceptFactory::value(p);
        else if constexpr (std::is_same_v<T, Concept>) return ConceptFactory::ofNative(p);
        else {
            static_assert(std::is_same_v<T, Concept>);  // Fail to compile with a nice message
        }

    } else {
        throw Utils::exception(&InternalError::INVALID_NATIVE_HANDLE);
    }
}

#define CONCEPT_FUTURE_HELPER(T) \
    TYPEDB_FUTURE_HELPER(std::unique_ptr<T>, ConceptFutureWrapper, resolveConceptPtrPromise<T>);


CONCEPT_FUTURE_HELPER(Type);
CONCEPT_FUTURE_HELPER(RoleType);

CONCEPT_FUTURE_HELPER(ThingType);
CONCEPT_FUTURE_HELPER(AttributeType);
CONCEPT_FUTURE_HELPER(EntityType);
CONCEPT_FUTURE_HELPER(RelationType);

CONCEPT_FUTURE_HELPER(Thing);
CONCEPT_FUTURE_HELPER(Attribute);
CONCEPT_FUTURE_HELPER(Entity);
CONCEPT_FUTURE_HELPER(Relation);

// Wrapper implementations:
ConceptIteratorWrapper::~ConceptIteratorWrapper() {}

ConceptFutureWrapperSimple::ConceptFutureWrapperSimple(_native::ConceptPromise* promise)
    : nativePromise(promise) {}

_native::Concept* ConceptFutureWrapperSimple::resolve() {
    auto p = _native::concept_promise_resolve(nativePromise);
    TypeDBDriverException::check_and_throw();
    return p;
}

ConceptFutureWrapperExplicit::ConceptFutureWrapperExplicit(_native::Concept* concept)
    : nativeConcept(concept) {}

_native::Concept* ConceptFutureWrapperExplicit::resolve() {
    if (nativeConcept == nullptr) {
        throw Utils::exception(&InternalError::INVALID_NATIVE_HANDLE);
    }
    auto t = nativeConcept;
    nativeConcept = nullptr;
    return t;
}

}  // namespace TypeDB
