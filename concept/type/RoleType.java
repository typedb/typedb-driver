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

package grakn.client.concept.type;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.type.impl.RoleTypeImpl;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A Type which defines a role which can be played in a RelationType.
 * This ontological element defines the RoleTypes which make up a RelationType.
 * It behaves similarly to Type when relating to other types.
 */
public interface RoleType extends Type {

    @CheckReturnValue
    @Override
    default RoleType asRoleType() {
        return this;
    }

    @Override
    default Remote asRemote(Transaction tx) {
        return RoleType.Remote.of(tx, getIID());
    }

    interface Local extends Type.Local, RoleType {
    }

    /**
     * A Type which defines a role which can be played in a RelationType.
     * This ontological element defines the RoleTypes which make up a RelationType.
     * It behaves similarly to Type when relating to other types.
     */
    interface Remote extends Type.Remote, RoleType {

        static RoleType.Remote of(Transaction tx, ConceptIID iid) {
            return new RoleTypeImpl.Remote(tx, iid);
        }

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         */
        void setLabel(String label);

        /**
         * Sets the supertype of this RoleType.
         *
         * @param type The supertype of this RoleType.
         * @return The RoleType itself.
         */
        RoleType.Remote setSupertype(RoleType type);

        /**
         * Get the label of this RoleType scoped to its relation.
         *
         * @return The scoped label
         */
        String getScopedLabel();

        /**
         * @return All the supertypes of this RoleType.
         */
        @Override
        Stream<? extends RoleType.Remote> getSupertypes();

        /**
         * Returns the subtype of this RoleType.
         *
         * @return The subtype of this RoleType.
         */
        @Override
        Stream<? extends RoleType.Remote> getSubtypes();

        /**
         * Returns the RelationTypes that this RoleType takes part in.
         *
         * @return The RelationType which this RoleType takes part in.
         * @see RelationType.Remote
         */
        @CheckReturnValue
        Stream<? extends RelationType.Remote> getRelations();

        /**
         * Returns a collection of the Types that can play this RoleType.
         *
         * @return A list of all the Types which can play this RoleType.
         * @see ThingType.Remote
         */
        @CheckReturnValue
        Stream<? extends ThingType.Remote> getPlayers();

        //------------------------------------- Other ---------------------------------
        @CheckReturnValue
        @Override
        default RoleType.Remote asRoleType() {
            return this;
        }
    }
}
