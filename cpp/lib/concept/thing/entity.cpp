
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

#include "typedb/concept/thing/entity.hpp"
#include "typedb/common/exception.hpp"
#include "typedb/concept/type/entity_type.hpp"

#include "inc/concept_factory.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

Entity::Entity(_native::Concept* conceptNative)
    : Thing(ConceptType::ENTITY, conceptNative) {}

std::unique_ptr<EntityType> Entity::getType() {
    return ConceptFactory::entityType(getTypeNative());
}

_native::Concept* Entity::getTypeNative() {
    CHECK_NATIVE(conceptNative);
    return _native::entity_get_type(conceptNative.get());
}

}  // namespace TypeDB
