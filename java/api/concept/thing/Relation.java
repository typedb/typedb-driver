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

package com.vaticle.typedb.driver.api.concept.thing;

import com.vaticle.typedb.driver.api.concept.type.RelationType;

import javax.annotation.CheckReturnValue;

/**
 * Relation is an instance of a relation type and can be uniquely addressed
 * by a combination of its type, owned attributes and role players.
 */
public interface Relation extends Thing {
    /**
     * Checks if the concept is a <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.isRelation();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default boolean isRelation() {
        return true;
    }

    /**
     * Casts the concept to <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.asRelation();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default Relation asRelation() {
        return this;
    }

    /**
     * Retrieves the type which this <code>Relation</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getType();
     * </pre>
     */
    @Override
    @CheckReturnValue
    RelationType getType();

    /**
     * Retrieves the unique id of the <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getIID();
     * </pre>
     */
    @CheckReturnValue
    String getIID();
}
