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
#include "inc/conceptfuture.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

#define CONCEPT_FUTURE_HELPER(T, INSTANTIATE)                                                                           \
    template <>                                                                                                         \
    std::unique_ptr<T> FutureHelper<std::unique_ptr<T>, ConceptFutureWrapper>::resolve(ConceptFutureWrapper* wrapper) { \
        if (wrapper != nullptr) {                                                                                       \
            auto p = wrapper->resolve();                                                                                \
            if (p == nullptr) return std::unique_ptr<T>();                                                              \
            return INSTANTIATE(p);                                                                                      \
        } else {                                                                                                        \
            throw Utils::exception(InternalError::INVALID_NATIVE_HANDLE);                                              \
        }                                                                                                               \
    }

CONCEPT_FUTURE_HELPER(Type, ConceptFactory::type);
CONCEPT_FUTURE_HELPER(RoleType, ConceptFactory::roleType);

CONCEPT_FUTURE_HELPER(ThingType, ConceptFactory::thingType);
CONCEPT_FUTURE_HELPER(AttributeType, ConceptFactory::attributeType);
CONCEPT_FUTURE_HELPER(EntityType, ConceptFactory::entityType);
CONCEPT_FUTURE_HELPER(RelationType, ConceptFactory::relationType);

CONCEPT_FUTURE_HELPER(Thing, ConceptFactory::thing);
CONCEPT_FUTURE_HELPER(Attribute, ConceptFactory::attribute);
CONCEPT_FUTURE_HELPER(Entity, ConceptFactory::entity);
CONCEPT_FUTURE_HELPER(Relation, ConceptFactory::relation);

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
    auto t = nativeConcept;
    nativeConcept = nullptr;
    return t;
}

}  // namespace TypeDB
