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

#include <map>

#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"
#include "typedb/concept/concept.hpp"
#include "typedb/logic/explainable.hpp"

namespace TypeDB {

class ConceptMap {
public:
    ConceptMap(_native::ConceptMap*);
    ConceptMap(const ConceptMap&) = delete;
    ConceptMap(ConceptMap&&) = default;

    ConceptMap& operator=(const ConceptMap&) = delete;
    ConceptMap& operator=(ConceptMap&&) = default;
    ~ConceptMap() = default;

    StringIterable variables();
    ConceptIterable<Concept> concepts();
    std::map<std::string, std::unique_ptr<Concept>> map();
    Explainables explainables();
    std::unique_ptr<Concept> get(const std::string& variableName);
    std::string toString();

private:
    NativePointer<_native::ConceptMap> conceptMapNative;

    friend class IteratorHelper<_native::ConceptMapIterator, _native::ConceptMap, ConceptMap>;
};

// ConceptMapIterator
using ConceptMapIterator = Iterator<_native::ConceptMapIterator, _native::ConceptMap, TypeDB::ConceptMap>;
using ConceptMapIterable = Iterable<_native::ConceptMapIterator, _native::ConceptMap, TypeDB::ConceptMap>;

}  // namespace TypeDB
