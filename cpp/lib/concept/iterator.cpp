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
#include "inc/conceptiterator.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

// ConceptIterator Helper
void conceptIteratorWrapperDrop(ConceptIteratorWrapper* it) {
    delete it;
}

_native::Concept* conceptIteratorWrapperNext(ConceptIteratorWrapper* it) {
    return it->next();
}

#define CONCEPT_ITERATOR_HELPER(T, CONCEPT_FACTORY_METHOD) \
    TYPEDB_ITERATOR_HELPER_1(ConceptIteratorWrapper, _native::Concept, std::unique_ptr<T>, conceptIteratorWrapperDrop, conceptIteratorWrapperNext, _native::concept_drop, CONCEPT_FACTORY_METHOD)

CONCEPT_ITERATOR_HELPER(Concept, ConceptFactory::ofNative);
CONCEPT_ITERATOR_HELPER(Type, ConceptFactory::type);
CONCEPT_ITERATOR_HELPER(RoleType, ConceptFactory::roleType);

CONCEPT_ITERATOR_HELPER(ThingType, ConceptFactory::thingType);
CONCEPT_ITERATOR_HELPER(AttributeType, ConceptFactory::attributeType);
CONCEPT_ITERATOR_HELPER(EntityType, ConceptFactory::entityType);
CONCEPT_ITERATOR_HELPER(RelationType, ConceptFactory::relationType);

CONCEPT_ITERATOR_HELPER(Thing, ConceptFactory::thing);
CONCEPT_ITERATOR_HELPER(Attribute, ConceptFactory::attribute);
CONCEPT_ITERATOR_HELPER(Entity, ConceptFactory::entity);
CONCEPT_ITERATOR_HELPER(Relation, ConceptFactory::relation);


// Wrapper implementations:
ConceptIteratorWrapperSimple::ConceptIteratorWrapperSimple(_native::ConceptIterator* iterator)
    : nativeIterator(iterator) {}

ConceptIteratorWrapperSimple::~ConceptIteratorWrapperSimple() {
    _native::concept_iterator_drop(nativeIterator);
}

_native::Concept* ConceptIteratorWrapperSimple::next() {
    auto p = _native::concept_iterator_next(nativeIterator);
    TypeDBDriverException::check_and_throw();
    return p;
}

ConceptPromiseWrappingIterator::ConceptPromiseWrappingIterator(_native::ConceptPromise* conceptPromise)
    : conceptPromiseNative(conceptPromise) {}

ConceptPromiseWrappingIterator::~ConceptPromiseWrappingIterator() {
    if (nullptr != conceptPromiseNative) _native::concept_promise_resolve(conceptPromiseNative);
}

_native::Concept* ConceptPromiseWrappingIterator::next() {
    if (nullptr == conceptPromiseNative) return nullptr;
    else {
        auto p = conceptPromiseNative;
        conceptPromiseNative = nullptr;
        return _native::concept_promise_resolve(p);
    }
}

ConceptIteratorWrapperChained::ConceptIteratorWrapperChained(std::initializer_list<_native::ConceptIterator*> iterators)
    : nativeIterators(iterators.begin(), iterators.end()) {}

ConceptIteratorWrapperChained::~ConceptIteratorWrapperChained() {
    while (!nativeIterators.empty()) {
        _native::concept_iterator_drop(nativeIterators.back());
        nativeIterators.pop_back();
    }
}

_native::Concept* ConceptIteratorWrapperChained::next() {
    _native::Concept* nextConcept = nullptr;
    while (!nativeIterators.empty() && nullptr == (nextConcept = _native::concept_iterator_next(nativeIterators.back()))) {
        TypeDBDriverException::check_and_throw();
        _native::concept_iterator_drop(nativeIterators.back());
        nativeIterators.pop_back();
    }
    TypeDBDriverException::check_and_throw();
    return nextConcept;
}

}  // namespace TypeDB
