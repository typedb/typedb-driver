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

#include <string>
#include <utility>

#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

// Forward declaration for friendship
class ConceptMap;
class QueryManager;
class Explainables;

class OwnerAttributePair {
public:
    std::string owner;
    std::string attribute;

private:
    OwnerAttributePair(_native::StringPair* stringPairNative);

    friend class IteratorHelper<_native::StringPairIterator, _native::StringPair, OwnerAttributePair>;
};

class Explainable {
public:
    Explainable(const Explainable&) = delete;
    Explainable(Explainable&&) = default;
    ~Explainable() = default;

    int64_t explainableId();
    std::string conjunction();

private:
    Explainable(_native::Explainable* explainableNative);
    Explainable& operator=(const Explainable&) = delete;
    Explainable& operator=(Explainable&&) = default;
    _native::Explainable* getNative() const;

    NativePointer<_native::Explainable> explainableNative;

    friend class Explainables;
    friend class QueryManager;
};


using OwnerAttributePairIterator = Iterator<_native::StringPairIterator, _native::StringPair, OwnerAttributePair>;
using OwnerAttributePairIterable = Iterable<_native::StringPairIterator, _native::StringPair, OwnerAttributePair>;

class Explainables {
public:
    Explainables(Explainables&&) = default;
    Explainables& operator=(Explainables&&) = default;
    ~Explainables() = default;

    Explainable relation(std::string& variable);
    Explainable attribute(std::string& variable);
    Explainable ownership(std::string& owner, std::string& attribute);
    StringIterable relations();
    StringIterable attributes();
    OwnerAttributePairIterable ownerships();
    std::string toString();

private:
    NativePointer<_native::Explainables> explainablesNative;

    Explainables(_native::Explainables*);
    Explainables(const Explainables&) = delete;
    Explainables& operator=(const Explainable&) = delete;

    friend class ConceptMap;
};

}  // namespace TypeDB
