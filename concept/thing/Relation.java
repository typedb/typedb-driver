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

package grakn.client.concept.thing;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.thing.impl.RelationImpl;
import grakn.client.concept.type.RoleType;
import grakn.client.concept.type.RelationType;

import javax.annotation.CheckReturnValue;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Encapsulates relations between Thing
 * A relation which is an instance of a RelationType defines how instances may relate to one another.
 * It represents how different entities relate to one another.
 * Relation are used to model n-ary relations between instances.
 */
public interface Relation extends Thing {
    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieve the associated RelationType for this Relation.
     *
     * @return The associated RelationType for this Relation.
     * @see RelationType
     */
    @Override
    RelationType getType();

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default Relation asRelation() {
        return this;
    }

    @Override
    default Remote asRemote(Transaction tx) {
        return Relation.Remote.of(tx, getIID());
    }

    interface Local extends Thing.Local, Relation {
    }

    /**
     * Encapsulates relations between Thing
     * A relation which is an instance of a RelationType defines how instances may relate to one another.
     * It represents how different entities relate to one another.
     * Relation are used to model n-ary relations between instances.
     */
    interface Remote extends Thing.Remote, Relation {

        static Relation.Remote of(Transaction tx, ConceptIID iid) {
            return new RelationImpl.Remote(tx, iid);
        }

        /**
         * Creates a relation from this instance to the provided Attribute.
         *
         * @param attribute The Attribute to which a relation is created
         * @return The instance itself
         */
        @Override
        Relation.Remote setHas(Attribute attribute);

        /**
         * Retrieve the associated RelationType for this Relation.
         *
         * @return The associated RelationType for this Relation.
         * @see RelationType.Remote
         */
        @Override
        RelationType.Remote getType();

        /**
         * Retrieve a list of all Instances involved in the Relation, and the RoleTypes they play.
         *
         * @return A list of all the role types and the instances playing them in this Relation.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Map<RoleType.Remote, List<Thing.Remote>> getPlayersByRoleType();

        /**
         * Retrieves a list of every Thing involved in the Relation.
         *
         * @return A list of every Thing involved in the Relation.
         */
        @CheckReturnValue
        Stream<Thing.Remote> getPlayers();

        /**
         * Retrieves a list of every Thing involved in the Relation, filtered by RoleType played.
         *
         * @param roleTypes Used to filter the returned instances only to ones that play any of the role types.
         *
         * @return A list of every Thing involved in the Relation, filtered by RoleType played.
         */
        @CheckReturnValue
        Stream<Thing.Remote> getPlayers(List<RoleType> roleTypes);

        /**
         * Expands this Relation to include a new role player which is playing a specific role.
         *
         * @param roleType   The RoleType of the new role player.
         * @param player The new role player.
         * @return The Relation itself.
         */
        void addPlayer(RoleType roleType, Thing player);

        /**
         * Removes the Thing which is playing a RoleType in this Relation.
         * If the Thing is not playing any RoleType in this Relation nothing happens.
         *
         * @param roleType The RoleType being played by the Thing
         * @param player The Thing playing the Role in this Relation
         */
        void removePlayer(RoleType roleType, Thing player);

        @Deprecated
        @CheckReturnValue
        @Override
        default Relation.Remote asRelation() {
            return this;
        }

    }
}
