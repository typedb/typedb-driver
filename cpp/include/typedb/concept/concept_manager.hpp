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

/**
 * \brief Provides access for all Concept API methods.
 */
class ConceptManager {
public:
    ConceptManager(ConceptManager&&) noexcept = delete;
    ConceptManager& operator=(ConceptManager&&) = delete;
    ConceptManager(const ConceptManager&) = delete;
    ConceptManager& operator=(const ConceptManager&) = delete;
    ~ConceptManager() = default;

    /**
     * Creates a new <code>EntityType</code> if none exists with the given label,
     * otherwise retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.putEntityType(label).get();
     * </pre>
     *
     * @param label The label of the <code>EntityType</code> to create or retrieve
     */
    [[nodiscard]] ConceptPtrFuture<EntityType> putEntityType(const std::string& label) const;

    /**
     * Creates a new <code>RelationType</code> if none exists with the given label,
     * otherwise retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.putRelationType(label).get();
     * </pre>
     *
     * @param label The label of the <code>RelationType</code> to create or retrieve
     */
    [[nodiscard]] ConceptPtrFuture<RelationType> putRelationType(const std::string& label) const;

    /**
     * Creates a new <code>AttributeType</code> if none exists with the given label,
     * or retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.putAttributeType(label, valueType).get();
     * </pre>
     *
     * @param label The label of the <code>AttributeType</code> to create or retrieve
     * @param valueType The value type of the <code>AttributeType</code> to create
     */
    [[nodiscard]] ConceptPtrFuture<AttributeType> putAttributeType(const std::string& label, ValueType valueType) const;

    /**
     * Retrieves the root <code>EntityType</code>, “entity”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getRootEntityType();
     * </pre>
     */
    std::unique_ptr<EntityType> getRootEntityType() const;

    /**
     * Retrieve the root <code>RelationType</code>, “relation”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getRootRelationType();
     * </pre>
     */
    std::unique_ptr<RelationType> getRootRelationType() const;

    /**
     * Retrieve the root <code>AttributeType</code>, “attribute”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getRootAttributeType();
     * </pre>
     */
    std::unique_ptr<AttributeType> getRootAttributeType() const;

    /**
     * Retrieves an <code>EntityType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getEntityType(label).get();
     * </pre>
     *
     * @param label The label of the <code>EntityType</code> to retrieve
     */
    ConceptPtrFuture<EntityType> getEntityType(const std::string& label) const;

    /**
     * Retrieves a <code>RelationType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getRelationType(label).get();
     * </pre>
     *
     * @param label The label of the <code>RelationType</code> to retrieve
     */
    ConceptPtrFuture<RelationType> getRelationType(const std::string& label) const;

    /**
     * Retrieves an <code>AttributeType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getAttributeType(label).get();
     * </pre>
     *
     * @param label The label of the <code>AttributeType</code> to retrieve
     */
    ConceptPtrFuture<AttributeType> getAttributeType(const std::string& label) const;

    /**
     * Retrieves an <code>Entity</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getEntity(iid).get();
     * </pre>
     *
     * @param iid The iid of the <code>Entity</code> to retrieve
     */
    ConceptPtrFuture<Entity> getEntity(const std::string& iid) const;

    /**
     * Retrieves a <code>Relation</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getRelation(iid).get();
     * </pre>
     *
     * @param iid The iid of the <code>Relation</code> to retrieve
     */
    ConceptPtrFuture<Relation> getRelation(const std::string& iid) const;

    /**
     * Retrieves an <code>Attribute</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getAttribute(iid).get();
     * </pre>
     *
     * @param iid The iid of the <code>Attribute</code> to retrieve
     */
    ConceptPtrFuture<Attribute> getAttribute(const std::string& iid) const;

    /**
     * Retrieves a list of all schema exceptions for the current transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts.getSchemaExceptions();
     * </pre>
     */
    std::vector<DriverException> getSchemaExceptions();

private:
    TypeDB::Transaction* const transaction;
    ConceptManager(TypeDB::Transaction*);

    friend class TypeDB::Transaction;
};

}  // namespace TypeDB
