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

#include "typedb/common/native.hpp"
#include "typedb/concept/concept.hpp"

namespace TypeDB {

class ConceptIteratorWrapper {
   public:
    virtual ~ConceptIteratorWrapper() = 0;
    virtual _native::Concept* next() = 0;
};

class ConceptIteratorWrapperSimple : public ConceptIteratorWrapper {
   public:
    ConceptIteratorWrapperSimple(_native::ConceptIterator* iterator);
    virtual ~ConceptIteratorWrapperSimple() override;
    virtual _native::Concept* next() override;

   private:
    _native::ConceptIterator* nativeIterator;
};

class ConceptIteratorWrapperExplicit : public ConceptIteratorWrapper {
   public:
    ConceptIteratorWrapperExplicit(std::initializer_list<_native::Concept*> concepts);
    virtual ~ConceptIteratorWrapperExplicit() override;
    virtual _native::Concept* next() override;

   private:
    std::vector<_native::Concept*> nativeConcepts;
};

class ConceptPromiseWrappingIterator : public ConceptIteratorWrapper {
   public:
    ConceptPromiseWrappingIterator(_native::ConceptPromise* conceptPromise);
    virtual ~ConceptPromiseWrappingIterator() override;
    virtual _native::Concept* next() override;

   private:
    _native::ConceptPromise* conceptPromiseNative;
};

class ConceptIteratorWrapperChained : public ConceptIteratorWrapper {
   public:
    ConceptIteratorWrapperChained(std::initializer_list<_native::ConceptIterator*> iterators);
    virtual ~ConceptIteratorWrapperChained() override;
    virtual _native::Concept* next() override;

   private:
    std::vector<_native::ConceptIterator*> nativeIterators;
};

}  // namespace TypeDB
