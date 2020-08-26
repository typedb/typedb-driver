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

import grakn.client.concept.Concepts;
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

    /**
     * Retrieve the associated RelationType for this Relation.
     *
     * @return The associated RelationType for this Relation.
     * @see RelationType
     */
    @Override
    RelationType getType();

    @Override
    default Remote asRemote(final Concepts concepts) {
        return Relation.Remote.of(concepts, getIID());
    }

    interface Local extends Thing.Local, Relation {

        @CheckReturnValue
        @Override
        default Relation.Local asRelation() {
            return this;
        }
    }

    /**
     * Encapsulates relations between Thing
     * A relation which is an instance of a RelationType defines how instances may relate to one another.
     * It represents how different entities relate to one another.
     * Relation are used to model n-ary relations between instances.
     */
    interface Remote extends Thing.Remote, Relation {

        static Relation.Remote of(final Concepts concepts, final String iid) {
            return new RelationImpl.Remote(concepts, iid);
        }

        /**
         * Retrieve the associated RelationType for this Relation.
         *
         * @return The associated RelationType for this Relation.
         * @see RelationType.Remote
         */
        @Override
        RelationType.Remote getType();

        /**
         * Expands this Relation to include a new role player which is playing a specific role.
         *
         * @param roleType   The RoleType of the new role player.
         * @param player The new role player.
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

        /**
         * Retrieves a list of every Thing involved in the Relation, filtered by RoleType played.
         * If no RoleTypes are specified then every involved Thing is retrieved, regardless of role.
         *
         * @param roleTypes Used to filter the returned instances only to ones that play any of the role types.
         *
         * @return A list of every Thing involved in the Relation, filtered by RoleType played.
         */
        @CheckReturnValue
        Stream<? extends Thing.Remote> getPlayers(RoleType... roleTypes);

        /**
         * Retrieve a list of all Instances involved in the Relation, and the RoleTypes they play.
         *
         * @return A list of all the role types and the instances playing them in this Relation.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Map<? extends RoleType.Remote, List<? extends Thing.Remote>> getPlayersByRoleType();

        @CheckReturnValue
        @Override
        default Relation.Remote asRelation() {
            return this;
        }
    }
}
