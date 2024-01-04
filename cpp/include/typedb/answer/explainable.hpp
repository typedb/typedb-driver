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

/**
 * \brief Simple class holding the owner concept & owned attribute identifying an explainable ownership.
 */
class OwnerAttributePair {
public:
    /// The owner concept
    std::string owner;
    /// The owned attribute
    std::string attribute;

private:
    OwnerAttributePair(_native::StringPair* stringPairNative);

    friend class IteratorHelper<_native::StringPairIterator, _native::StringPair, OwnerAttributePair>;
};


/**
 * \brief Contains an explainable object.
 */
class Explainable {
public:
    Explainable(const Explainable&) = delete;
    Explainable(Explainable&&) = default;
    ~Explainable() = default;

    /**
     * Retrieves the unique ID that identifies this <code>Explainable</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * explainable.id();
     * </pre>
     */
    int64_t explainableId();

    /**
     * Retrieves the subquery of the original query that is actually being explained.
     *
     * <h3>Examples</h3>
     * <pre>
     * explainable.conjunction();
     * </pre>
     */
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

/**
 * \brief Contains explainable objects.
 */
class Explainables {
public:
    Explainables(Explainables&&) = default;
    Explainables& operator=(Explainables&&) = default;
    ~Explainables() = default;

    /**
     * Retrieves the explainable relation with the given variable name.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().relation(variable);
     * </pre>
     *
     * @param variable The string representation of a variable
     */
    Explainable relation(std::string& variable);

    /**
     * Retrieves the explainable attribute with the given variable name.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().attribute(variable);
     * </pre>
     *
     * @param variable The string representation of a variable
     */
    Explainable attribute(std::string& variable);

    /**
     * Retrieves the explainable attribute ownership with the pair of (owner, attribute) variable names.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().ownership(owner, attribute);
     * </pre>
     *
     * @param owner The string representation of the owner variable
     * @param attribute The string representation of the attribute variable
     */
    Explainable ownership(std::string& owner, std::string& attribute);

    /**
     * Retrieves all of this <code>ConceptMap</code>’s explainable relations.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().relations();
     * </pre>
     */
    StringIterable relations();

    /**
     * Retrieves all of this <code>ConceptMap</code>’s explainable attributes.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().attributes();
     * </pre>
     */
    StringIterable attributes();

    /**
     * Retrieves all of this <code>ConceptMap</code>’s explainable ownerships.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMap.explainables().ownerships();
     * </pre>
     */
    OwnerAttributePairIterable ownerships();

    /**
     * A string representation of this object.
     */
    std::string toString();

private:
    NativePointer<_native::Explainables> explainablesNative;

    Explainables(_native::Explainables*);
    Explainables(const Explainables&) = delete;
    Explainables& operator=(const Explainable&) = delete;

    friend class ConceptMap;
};

}  // namespace TypeDB
