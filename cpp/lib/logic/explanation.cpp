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

#include "typedb/logic/explanation.hpp"

#include "../common/macros.hpp"

namespace TypeDB {
Explanation::Explanation(_native::Explanation* explanationNative)
    : explanationNative(explanationNative, _native::explanation_drop) {}

Rule Explanation::rule() {
    CHECK_NATIVE(explanationNative);
    WRAPPED_NATIVE_CALL(Rule, _native::explanation_get_rule(explanationNative.get()));
}

ConceptMap Explanation::conclusion() {
    CHECK_NATIVE(explanationNative);
    WRAPPED_NATIVE_CALL(ConceptMap, _native::explanation_get_conclusion(explanationNative.get()));
}

ConceptMap Explanation::condition() {
    CHECK_NATIVE(explanationNative);
    WRAPPED_NATIVE_CALL(ConceptMap, _native::explanation_get_condition(explanationNative.get()));
}

TYPEDB_ITERATOR_HELPER(
    _native::ExplanationIterator,
    _native::Explanation,
    Explanation,
    _native::explanation_iterator_drop,
    _native::explanation_iterator_next,
    _native::explanation_drop);

}  // namespace TypeDB
