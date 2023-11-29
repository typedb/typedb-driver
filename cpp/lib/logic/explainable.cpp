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

#include "typedb/logic/explainable.hpp"

#include "inc/macros.hpp"
#include "inc/utils.hpp"

namespace TypeDB {

Explainable::Explainable(_native::Explainable* explainableNative)
    : explainableNative(explainableNative, _native::explainable_drop) {
}

_native::Explainable* Explainable::getNative() const {
    return explainableNative.get();
}

int64_t Explainable::explainableId() {
    CHECK_NATIVE(explainableNative);
    return _native::explainable_get_id(explainableNative.get());
}

std::string Explainable::conjunction() {
    CHECK_NATIVE(explainableNative);
    return Utils::stringFromNative(_native::explainable_get_conjunction(explainableNative.get()));
}

Explainables::Explainables(_native::Explainables* explainablesNative)
    : explainablesNative(explainablesNative, _native::explainables_drop) {}

Explainables::Explainables(Explainables&& from) {
    *this = std::move(from);
}

Explainables& Explainables::operator=(Explainables&& from) {
    explainablesNative = std::move(from.explainablesNative);
    return *this;
}

Explainable Explainables::relation(std::string& variable) {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(Explainable, _native::explainables_get_relation(explainablesNative.get(), variable.c_str()));
}

Explainable Explainables::attribute(std::string& variable) {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(Explainable, _native::explainables_get_attribute(explainablesNative.get(), variable.c_str()));
}

Explainable Explainables::ownership(std::string& owner, std::string& attribute) {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(Explainable, _native::explainables_get_ownership(explainablesNative.get(), owner.c_str(), attribute.c_str()));
}

StringIterable Explainables::relations() {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(StringIterable, _native::explainables_get_relations_keys(explainablesNative.get()));
}

StringIterable Explainables::attributes() {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(StringIterable, _native::explainables_get_attributes_keys(explainablesNative.get()));
}

OwnerAttributePairIterable Explainables::ownerships() {
    CHECK_NATIVE(explainablesNative);
    WRAPPED_NATIVE_CALL(OwnerAttributePairIterable, _native::explainables_get_ownerships_keys(explainablesNative.get()));
}

OwnerAttributePair::OwnerAttributePair(_native::StringPair* stringPairNative) {
    owner = std::string(stringPairNative->_0);
    attribute = std::string(stringPairNative->_1);
    _native::string_pair_drop(stringPairNative);
}

TYPEDB_ITERATOR_HELPER(
    _native::StringPairIterator,
    _native::StringPair,
    OwnerAttributePair,
    _native::string_pair_iterator_drop,
    _native::string_pair_iterator_next,
    _native::string_pair_drop);

}  // namespace TypeDB
