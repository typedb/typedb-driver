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

package com.vaticle.typedb.driver.api.concept;

import com.vaticle.typedb.driver.api.concept.thing.Attribute;
import com.vaticle.typedb.driver.api.concept.thing.Entity;
import com.vaticle.typedb.driver.api.concept.thing.Relation;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.type.EntityType;
import com.vaticle.typedb.driver.api.concept.type.RelationType;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.exception.TypeDBException;

import java.util.List;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

/**
 * Provides access for all Concept API methods.
 */
public interface ConceptManager {
    /**
     * Retrieves the root <code>EntityType</code>, “entity”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getRootEntityType();
     * </pre>
     */
    @CheckReturnValue
    EntityType getRootEntityType();

    /**
     * Retrieve the root <code>RelationType</code>, “relation”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getRootRelationType();
     * </pre>
     */
    @CheckReturnValue
    RelationType getRootRelationType();

    /**
     * Retrieve the root <code>AttributeType</code>, “attribute”.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getRootAttributeType();
     * </pre>
     */
    @CheckReturnValue
    AttributeType getRootAttributeType();

    /**
     * Retrieves an <code>EntityType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getEntityType(label);
     * </pre>
     *
     * @param label The label of the <code>EntityType</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    EntityType getEntityType(String label);

    /**
     * Retrieves a <code>RelationType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getRelationType(label);
     * </pre>
     *
     * @param label The label of the <code>RelationType</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    RelationType getRelationType(String label);

    /**
     * Retrieves an <code>AttributeType</code> by its label.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getAttributeType(label)
     * </pre>
     *
     * @param label The label of the <code>AttributeType</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    AttributeType getAttributeType(String label);

    /**
     * Creates a new <code>EntityType</code> if none exists with the given label,
     * otherwise retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().putEntityType(label);
     * </pre>
     *
     * @param label The label of the <code>EntityType</code> to create or retrieve
     */
    EntityType putEntityType(String label);

    /**
     * Creates a new <code>RelationType</code> if none exists with the given label,
     * otherwise retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().putRelationType(label);
     * </pre>
     *
     * @param label The label of the <code>RelationType</code> to create or retrieve
     */
    RelationType putRelationType(String label);

    /**
     * Creates a new <code>AttributeType</code> if none exists with the given label,
     * or retrieves the existing one.
     *
     * <h3>Examples</h3>
     * <pre>
     * await transaction.concepts().putAttributeType(label, valueType);
     * </pre>
     *
     * @param label The label of the <code>AttributeType</code> to create or retrieve
     * @param valueType The value type of the <code>AttributeType</code> to create
     */
    AttributeType putAttributeType(String label, Value.Type valueType);

    /**
     * Retrieves an <code>Entity</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getEntity(iid);
     * </pre>
     *
     * @param iid The iid of the <code>Entity</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    Entity getEntity(String iid);

    /**
     * Retrieves a <code>Relation</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getRelation(iid);
     * </pre>
     *
     * @param iid The iid of the <code>Relation</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    Relation getRelation(String iid);

    /**
     * Retrieves an <code>Attribute</code> by its iid.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getAttribute(iid);
     * </pre>
     *
     * @param iid The iid of the <code>Attribute</code> to retrieve
     */
    @Nullable
    @CheckReturnValue
    Attribute getAttribute(String iid);

    /**
     * Retrieves a list of all exceptions for the current transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.concepts().getSchemaException();
     * </pre>
     */
    @CheckReturnValue
    List<TypeDBException> getSchemaExceptions();
}
