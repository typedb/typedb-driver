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

#include <vector>

#include "typedb/concept/concept.hpp"

#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/concept/type/entity_type.hpp"
#include "typedb/concept/type/relation_type.hpp"
#include "typedb/concept/type/role_type.hpp"
#include "typedb/concept/type/thing_type.hpp"

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"
#include "typedb/concept/thing/thing.hpp"

#include "../common/native.hpp"

namespace TypeDB {
// Forward declarations for friendship
class ConceptMap;
class ConceptMapGroup;
class Transaction;
class Value;
class ValueGroup;

std::optional<std::unique_ptr<Value>> aggregateFutureWrapper(_native::ConceptPromise* conceptPromiseNative);

class ConceptFactory {
public:
    // for concept api methods.
    static _native::Transaction* getNative(Transaction&);
    static _native::Concept* getNative(const Concept*);
    static _native::Annotation* getNative(const Annotation& annotation);
    static std::vector<const _native::Annotation*> toNativeArray(const std::vector<Annotation>& annotations);

    template <typename T>
    static std::vector<_native::Concept*> toNativeArray(const std::vector<T*>& concepts) {
        std::vector<_native::Concept*> v;
        v.reserve(concepts.size() + 1);
        for (auto& c : concepts)
            v.push_back(ConceptFactory::getNative(c));
        v.push_back(nullptr);
        return v;
    }

    template <typename T>
    static std::vector<_native::Concept*> toNativeArray(const std::vector<std::unique_ptr<T>>& concepts) {
        std::vector<_native::Concept*> v;
        v.reserve(concepts.size() + 1);
        for (auto& c : concepts)
            v.push_back(ConceptFactory::getNative(c.get()));
        v.push_back(nullptr);
        return v;
    }


    // Factory methods
    static std::unique_ptr<Concept> ofNative(_native::Concept*);

    static std::unique_ptr<EntityType> entityType(_native::Concept*);
    static std::unique_ptr<AttributeType> attributeType(_native::Concept*);
    static std::unique_ptr<RelationType> relationType(_native::Concept*);
    static std::unique_ptr<ThingType> rootThingType(_native::Concept*);
    static std::unique_ptr<RoleType> roleType(_native::Concept*);

    static std::unique_ptr<Attribute> attribute(_native::Concept*);
    static std::unique_ptr<Entity> entity(_native::Concept*);
    static std::unique_ptr<Relation> relation(_native::Concept*);

    static std::unique_ptr<Type> type(_native::Concept* conceptNative);
    static std::unique_ptr<ThingType> thingType(_native::Concept* conceptNative);
    static std::unique_ptr<Thing> thing(_native::Concept* conceptNative);

    static std::unique_ptr<Value> value(_native::Concept*);
};
}  // namespace TypeDB
