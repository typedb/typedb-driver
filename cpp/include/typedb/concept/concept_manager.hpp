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

#include "typedb/common/future.hpp"
#include "typedb/common/native.hpp"

#include "typedb/concept/type/attribute_type.hpp"
#include "typedb/concept/type/entity_type.hpp"
#include "typedb/concept/type/relation_type.hpp"

#include "typedb/concept/thing/attribute.hpp"
#include "typedb/concept/thing/entity.hpp"
#include "typedb/concept/thing/relation.hpp"

namespace TypeDB {

class Transaction;

class ConceptManager {
public:
    ConceptManager(ConceptManager&&) noexcept = delete;
    ConceptManager& operator=(ConceptManager&&) = delete;
    ConceptManager(const ConceptManager&) = delete;
    ConceptManager& operator=(const ConceptManager&) = delete;
    ~ConceptManager() = default;

    [[nodiscard]] ConceptPtrFuture<EntityType> putEntityType(const std::string& label) const;
    [[nodiscard]] ConceptPtrFuture<RelationType> putRelationType(const std::string& label) const;
    [[nodiscard]] ConceptPtrFuture<AttributeType> putAttributeType(const std::string& label, ValueType valueType) const;

    std::unique_ptr<EntityType> getRootEntityType() const;
    std::unique_ptr<RelationType> getRootRelationType() const;
    std::unique_ptr<AttributeType> getRootAttributeType() const;

    ConceptPtrFuture<EntityType> getEntityType(const std::string& label) const;
    ConceptPtrFuture<RelationType> getRelationType(const std::string& label) const;
    ConceptPtrFuture<AttributeType> getAttributeType(const std::string& label) const;

    ConceptPtrFuture<Entity> getEntity(const std::string& iid) const;
    ConceptPtrFuture<Relation> getRelation(const std::string& iid) const;
    ConceptPtrFuture<Attribute> getAttribute(const std::string& iid) const;

    std::vector<DriverException> getSchemaExceptions();

private:
    TypeDB::Transaction* const transaction;
    ConceptManager(TypeDB::Transaction*);

    friend class TypeDB::Transaction;
};

}  // namespace TypeDB
