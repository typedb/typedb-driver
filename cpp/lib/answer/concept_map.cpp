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

#include "typedb/answer/concept_map.hpp"
#include "typedb/common/error_message.hpp"
#include "typedb/common/exception.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../concept/concept_factory.hpp"
#include "../concept/iterator.hpp"

namespace TypeDB {

ConceptMap::ConceptMap(_native::ConceptMap* conceptMapNative)
    : conceptMapNative(conceptMapNative, _native::concept_map_drop) {}

StringIterable ConceptMap::variables() {
    CHECK_NATIVE(conceptMapNative);
    WRAPPED_NATIVE_CALL(StringIterable, _native::concept_map_get_variables(conceptMapNative.get()));
}

ConceptIterable<Concept> ConceptMap::concepts() {
    CHECK_NATIVE(conceptMapNative);
    WRAPPED_NATIVE_CALL(ConceptIterable<Concept>, new ConceptIteratorWrapperSimple(_native::concept_map_get_values(conceptMapNative.get())));
}

std::map<std::string, std::unique_ptr<Concept>> ConceptMap::map() {
    std::map<std::string, std::unique_ptr<Concept>> m;
    for (auto& v : variables()) {
        m.emplace(std::move(v), get(v));
    }
    return m;
}

Explainables ConceptMap::explainables() {
    CHECK_NATIVE(conceptMapNative);
    WRAPPED_NATIVE_CALL(Explainables, _native::concept_map_get_explainables(conceptMapNative.get()));
}

std::unique_ptr<Concept> ConceptMap::get(const std::string& variableName) {
    CHECK_NATIVE(conceptMapNative);
    auto p = _native::concept_map_get(conceptMapNative.get(), variableName.c_str());
    DriverException::check_and_throw();
    if (p == nullptr) {
        throw Utils::exception(QueryError::VARIABLE_DOES_NOT_EXIST, variableName.c_str());
    }
    return ConceptFactory::ofNative(p);
}

std::string ConceptMap::toString() {
    TO_STRING(conceptMapNative, _native::concept_map_to_string);
}

// ConceptMapIterator
TYPEDB_ITERATOR_HELPER(
    _native::ConceptMapIterator,
    _native::ConceptMap,
    ConceptMap,
    _native::concept_map_iterator_drop,
    _native::concept_map_iterator_next,
    _native::concept_map_drop);

}  // namespace TypeDB
