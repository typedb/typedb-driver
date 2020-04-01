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

package grakn.client.concept;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Encapsulates relations between Thing
 * A relation which is an instance of a RelationType defines how instances may relate to one another.
 * It represents how different entities relate to one another.
 * Relation are used to model n-ary relations between instances.
 */
public interface Relation extends Thing {
    //------------------------------------- Modifiers ----------------------------------

    /**
     * Creates a relation from this instance to the provided Attribute.
     *
     * @param attribute The Attribute to which a relation is created
     * @return The instance itself
     */
    @Override
    Relation has(Attribute attribute);

    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieve the associated RelationType for this Relation.
     *
     * @return The associated RelationType for this Relation.
     * @see RelationType
     */
    @Override
    RelationType type();

    /**
     * Retrieve a list of all Instances involved in the Relation, and the Role they play.
     *
     * @return A list of all the role types and the instances playing them in this Relation.
     * @see Role
     */
    @CheckReturnValue
    Map<Role, List<Thing>> rolePlayersMap();

    /**
     * Retrieves a list of every Thing involved in the Relation, filtered by Role played.
     *
     * @param roles used to filter the returned instances only to ones that play any of the role types.
     *              If blank, returns all role players.
     * @return a list of every Thing involved in the Relation.
     */
    @CheckReturnValue
    Stream<Thing> rolePlayers(Role... roles);

    /**
     * Expands this Relation to include a new role player which is playing a specific role.
     *
     * @param role   The Role Type of the new role player.
     * @param player The new role player.
     * @return The Relation itself.
     */
    Relation assign(Role role, Thing player);

    /**
     * Removes the provided Attribute from this Relation
     *
     * @param attribute the Attribute to be removed
     * @return The Relation itself
     */
    @Override
    Relation unhas(Attribute attribute);

    /**
     * Removes the Thing which is playing a Role in this Relation.
     * If the Thing is not playing any Role in this Relation nothing happens.
     *
     * @param role   The Role being played by the Thing
     * @param player The Thing playing the Role in this Relation
     */
    void unassign(Role role, Thing player);

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Relation asRelation() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelation() {
        return true;
    }
}
