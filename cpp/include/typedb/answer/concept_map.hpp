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

#include "typedb/answer/explainable.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"
#include "typedb/concept/concept.hpp"

namespace TypeDB {


/**
 * \brief Contains a mapping of variables to concepts.
 */
class ConceptMap {
public:
    ConceptMap(const ConceptMap&) = delete;
    ConceptMap(ConceptMap&&) = default;

    ConceptMap& operator=(const ConceptMap&) = delete;
    ConceptMap& operator=(ConceptMap&&) = default;
    ~ConceptMap() = default;


    /**
     * Produces an <code>Iterator</code> stream over all variables in this <code>ConceptMap</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.variables();
     * </pre>
     */
    StringIterable variables();

    /**
     * Produces an <code>Iterator</code> over all concepts in this <code>ConceptMap</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.concepts();
     * </pre>
     */
    ConceptIterable<Concept> concepts();

    /**
     * Returns the inner <code>Map</code> where keys are query variables, and values are concepts.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.map();
     * </pre>
     */
    std::map<std::string, std::unique_ptr<Concept>> map();

    /**
     * Gets the <code>Explainables</code> object for this <code>ConceptMap</code>, exposing
     * which of the concepts in this <code>ConceptMap</code> are explainable.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables();
     * </pre>
     */
    Explainables explainables();

    /**
     * Retrieves a concept for a given variable name.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.get(variable);
     * </pre>
     *
     * @param variable The string representation of a variable
     */
    std::unique_ptr<Concept> get(const std::string& variableName);

    /**
     * A string representation of this ConceptMap.
     */
    std::string toString();

private:
    NativePointer<_native::ConceptMap> conceptMapNative;
    ConceptMap(_native::ConceptMap*);

    friend class Explanation;
    friend class IteratorHelper<_native::ConceptMapIterator, _native::ConceptMap, ConceptMap>;
};

// ConceptMapIterator
using ConceptMapIterator = Iterator<_native::ConceptMapIterator, _native::ConceptMap, TypeDB::ConceptMap>;
using ConceptMapIterable = Iterable<_native::ConceptMapIterator, _native::ConceptMap, TypeDB::ConceptMap>;

}  // namespace TypeDB
