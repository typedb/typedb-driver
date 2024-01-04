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

#include "typedb/answer/value_future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"
#include "typedb/concept/concept.hpp"

namespace TypeDB {

/**
 *  \brief Contains an element of the group aggregate query result.
 */
class ValueGroup {
public:
    ValueGroup(const ValueGroup&) = delete;
    ValueGroup(ValueGroup&&) = default;
    ~ValueGroup() = default;

    ValueGroup& operator=(const ValueGroup&) = delete;
    ValueGroup& operator=(ValueGroup&&) = default;

    /**
     * Retrieves the concept that is the group owner.
     *
     * <h3>Examples</h3>
     * <pre>
     * conceptMapGroup.owner()
     * </pre>
     */
    std::unique_ptr<Concept> owner();

    /**
     * Retrieves the <code>Value</code> answer of the group.
     *
     * <h3>Examples</h3>
     * <pre>
     * valueGroup.value();
     * </pre>
     */
    AggregateResult value();

    /**
     * A string representation of this ConceptMap.
     */
    std::string toString();

private:
    ValueGroup(_native::ValueGroup*);
    NativePointer<_native::ValueGroup> valueGroupNative;

    friend class IteratorHelper<_native::ValueGroupIterator, _native::ValueGroup, TypeDB::ValueGroup>;
};

// For ValueGroupIterator
using ValueGroupIterable = Iterable<_native::ValueGroupIterator, _native::ValueGroup, TypeDB::ValueGroup>;
using ValueGroupIterator = Iterator<_native::ValueGroupIterator, _native::ValueGroup, TypeDB::ValueGroup>;

}  // namespace TypeDB
