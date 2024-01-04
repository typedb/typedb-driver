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
#include "typedb/concept/type/thing_type.hpp"

namespace TypeDB {

class Entity;

/**
 * \brief Entity types represent the classification of independent objects in the data model of the business domain.
 */
class EntityType : public ThingType {
public:
    /**
     * Creates and returns a new instance of this <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * entityType.create(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    [[nodiscard]] ConceptPtrFuture<Entity> create(Transaction& transaction);

    /**
     * Sets the supplied <code>EntityType</code> as the supertype of the current <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * entityType.setSupertype(transaction, entityType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param superEntityType The <code>EntityType</code> to set as the supertype of this <code>EntityType</code>
     */
    [[nodiscard]] VoidFuture setSupertype(Transaction& transaction, EntityType* superEntityType);

    /**
     * Retrieves <code>Entity</code> objects that are instances of this exact <code>EntityType</code> OR
     * this <code>EntityType</code> and any of its subtypes.
     *
     * <h3>Examples</h3>
     * <pre>
     * entityType.getInstances(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::EXPLICIT</code> for direct instances only,
     *                     <code>Transitivity::TRANSITIVE</code> to include subtypes
     */
    ConceptIterable<Entity> getInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * entityType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<EntityType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);  // Mimic overriding from Type

private:
    EntityType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
