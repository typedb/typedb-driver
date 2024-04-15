/*
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

#include "typedb/answer/concept_map_group.hpp"
#include "typedb/common/exception.hpp"

#include "../common/macros.hpp"
#include "../common/native.hpp"
#include "../concept/concept_factory.hpp"

namespace TypeDB {

ConceptMapGroup::ConceptMapGroup(_native::ConceptMapGroup* conceptMapGroupNative)
    : conceptMapGroupNative(conceptMapGroupNative, _native::concept_map_group_drop) {}

std::unique_ptr<Concept> ConceptMapGroup::owner() {
    CHECK_NATIVE(conceptMapGroupNative);
    WRAPPED_NATIVE_CALL(ConceptFactory::ofNative, _native::concept_map_group_get_owner(conceptMapGroupNative.get()));
}

ConceptMapIterable ConceptMapGroup::conceptMaps() {
    CHECK_NATIVE(conceptMapGroupNative);
    WRAPPED_NATIVE_CALL(ConceptMapIterable, _native::concept_map_group_get_concept_maps(conceptMapGroupNative.get()));
}

std::string ConceptMapGroup::toString() {
    TO_STRING(conceptMapGroupNative, _native::concept_map_group_to_string);
}

// For ConceptMapGroupIterator
TYPEDB_ITERATOR_HELPER(
    _native::ConceptMapGroupIterator,
    _native::ConceptMapGroup,
    ConceptMapGroup,
    _native::concept_map_group_iterator_drop,
    _native::concept_map_group_iterator_next,
    _native::concept_map_group_drop);

}  // namespace TypeDB
