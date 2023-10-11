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

package com.vaticle.typedb.driver.api.concept.thing;

import com.vaticle.typedb.driver.api.concept.type.EntityType;

import javax.annotation.CheckReturnValue;

/**
 * Instance of data of an entity type, representing a standalone object that exists in the data model independently.
 * Entity does not have a value. It is usually addressed by its ownership over attribute instances and/or roles
 * played in relation instances.
 */
public interface Entity extends Thing {
    /**
     * Checks if the concept is an <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * entity.isEntity();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default boolean isEntity() {
        return true;
    }

    /**
     * Casts the concept to <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * entity.asEntity();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default Entity asEntity() {
        return this;
    }

    /**
     * Retrieves the type which this <code>Entity</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * entity.getType();
     * </pre>
     */
    @Override
    @CheckReturnValue
    EntityType getType();
}
