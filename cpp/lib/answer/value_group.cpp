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

#include "typedb/answer/value_group.hpp"
#include "typedb/common/exception.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../concept/concept_factory.hpp"

namespace TypeDB {

ValueGroup::ValueGroup(_native::ValueGroup* valueGroupNative)
    : valueGroupNative(valueGroupNative, _native::value_group_drop) {}

std::unique_ptr<Concept> ValueGroup::owner() {
    CHECK_NATIVE(valueGroupNative);
    WRAPPED_NATIVE_CALL(ConceptFactory::ofNative, _native::value_group_get_owner(valueGroupNative.get()));
}

AggregateResult ValueGroup::value() {
    CHECK_NATIVE(valueGroupNative);
    _native::Concept* p = _native::value_group_get_value(valueGroupNative.get());
    DriverException::check_and_throw();
    if (p != nullptr) {
        return AggregateResult(ConceptFactory::value(p));
    } else {
        return AggregateResult();
    }
}

// For ValueGroupIterator
TYPEDB_ITERATOR_HELPER(
    _native::ValueGroupIterator,
    _native::ValueGroup,
    TypeDB::ValueGroup,
    _native::value_group_iterator_drop,
    _native::value_group_iterator_next,
    _native::value_group_drop);

}  // namespace TypeDB
