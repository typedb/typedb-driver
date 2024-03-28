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

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.type.RelationType;
import com.vaticle.typedb.driver.api.concept.type.RoleType;
import com.vaticle.typedb.driver.common.Promise;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
     * Adds a new role player to play the given role in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.addPlayer(transaction, roleType, player).resolve();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to be played by the <code>player</code>
     * @param player The thing to play the role
     */
    @CheckReturnValue
    Promise<Void> addPlayer(TypeDBTransaction transaction, RoleType roleType, Thing player);

    /**
     * Removes the association of the given instance that plays the given role in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.removePlayer(transaction, roleType, player).resolve();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to no longer be played by the thing in this <code>Relation</code>
     * @param player The instance to no longer play the role in this <code>Relation</code>
     */
    @CheckReturnValue
    Promise<Void> removePlayer(TypeDBTransaction transaction, RoleType roleType, Thing player);

    /**
     * Retrieves all role players of this <code>Relation</code>, optionally filtered by given role types.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getPlayersByRoleType(transaction, roleTypes);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleTypes 0 or more role types
     */
    @CheckReturnValue
    Stream<? extends Thing> getPlayersByRoleType(TypeDBTransaction transaction, RoleType... roleTypes);

    /**
     * Retrieves a mapping of all instances involved in the <code>Relation</code> and the role each play.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getPlayers(transaction)
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Map<? extends RoleType, ? extends List<? extends Thing>> getPlayers(TypeDBTransaction transaction);

    /**
     * Retrieves all role types currently played in this <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relation.getRelating(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Stream<? extends RoleType> getRelating(TypeDBTransaction transaction);
}
