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


namespace TypeDB {

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

ConceptIteratorWrapperExplicit::ConceptIteratorWrapperExplicit(std::initializer_list<_native::Concept*> concepts)
    : nativeConcepts(concepts.begin(), concepts.end()) {}

ConceptIteratorWrapperExplicit::~ConceptIteratorWrapperExplicit() {
    while (!nativeConcepts.empty()) {
        _native::concept_drop(nativeConcepts.back());
        nativeConcepts.pop_back();
    }
}

_native::Concept* ConceptIteratorWrapperExplicit::next() {
    if (nativeConcepts.empty()) return nullptr;
    else {
        _native::Concept* ret = nativeConcepts.back();
        nativeConcepts.pop_back();
        return ret;
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
