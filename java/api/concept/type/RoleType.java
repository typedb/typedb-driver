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

package com.vaticle.typedb.driver.api.concept.type;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.thing.Relation;
import com.vaticle.typedb.driver.api.concept.thing.Thing;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Roles are special internal types used by relations. We can not create an instance of a role in a database. But we can set an instance of another type (role player) to play a role in a particular instance of a relation type.
 * Roles allow a schema to enforce logical constraints on types of role players.
 */
public interface RoleType extends Type {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isRoleType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default RoleType asRoleType() {
        return this;
    }

    /**
     * Retrieves the most immediate supertype of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSupertype(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @Override
    RoleType getSupertype(TypeDBTransaction transaction);

    /**
     * Retrieves all supertypes of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSupertypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @Override
    Stream<? extends RoleType> getSupertypes(TypeDBTransaction transaction);
    /**
     * Retrieves all direct and indirect subtypes of the <code>RoleType</code>.
     * Equivalent to <code>getSubtypes(transaction, Transitivity.TRANSITIVE)</code>
     *
     * @see RoleType#getSubtypes(TypeDBTransaction, Transitivity)
     */
    @Override
    Stream<? extends RoleType> getSubtypes(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @Override
    Stream<? extends RoleType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves the <code>RelationType</code> that this role is directly related to.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationType(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    RelationType getRelationType(TypeDBTransaction transaction);

    /**
     * Retrieves <code>RelationType</code>s that this role is related to (directly or indirectly).
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationTypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    Stream<? extends RelationType> getRelationTypes(TypeDBTransaction transaction);

    /**
     * Retrieves the <code>ThingType</code>s whose instances play this role.
     * Equivalent to <code>getPlayerTypes(transaction, Transitivity.TRANSITIVE)</code>.
     *
     * @see RoleType#getPlayerTypes(TypeDBTransaction, Transitivity)
     */
    Stream<? extends ThingType> getPlayerTypes(TypeDBTransaction transaction);

    /**
     * Retrieves the <code>ThingType</code>s whose instances play this role.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getPlayerTypes(transaction, transitivity)
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    Stream<? extends ThingType> getPlayerTypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves the <code>Relation</code> instances that this role is related to.
     * Equivalent to <code></code>getRelationInstances(transaction, Transitivity.TRANSITIVE)</code>
     *
     * @see RoleType#getRelationInstances(TypeDBTransaction, Transitivity)
     */
    Stream<? extends Relation> getRelationInstances(TypeDBTransaction transaction);

    /**
     * Retrieves the <code>Relation</code> instances that this role is related to.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationInstances(transaction, transitivity)
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect relation, <code>Transitivity.EXPLICIT</code> for direct relation only
     */
    Stream<? extends Relation> getRelationInstances(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves the <code>Thing</code> instances that play this role.
     *
     * @see RoleType#getPlayerTypes(TypeDBTransaction, Transitivity)
     */
    Stream<? extends Thing> getPlayerInstances(TypeDBTransaction transaction);

    /**
     * Retrieves the <code>Thing</code> instances that play this role.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getPlayerInstances(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    Stream<? extends Thing> getPlayerInstances(TypeDBTransaction transaction, Transitivity transitivity);
}
