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

import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.type.impl.RelationTypeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * An ontological element which categorises how Things may relate to each other.
 * A RelationType defines how Type may relate to one another.
 * They are used to model and categorise n-ary Relations.
 */
public interface RelationType extends ThingType {

    @CheckReturnValue
    @Override
    RelationType.Remote asRemote(Concepts concepts);

    interface Local extends ThingType.Local, RelationType {

        @CheckReturnValue
        @Override
        default RelationType.Local asRelationType() {
            return this;
        }

        @Override
        default RelationType.Remote asRemote(final Concepts concepts) {
            return RelationType.Remote.of(concepts, getLabel(), isRoot());
        }
    }

    /**
     * An ontological element which categorises how Things may relate to each other.
     * A RelationType defines how Type may relate to one another.
     * They are used to model and categorise n-ary Relations.
     */
    interface Remote extends ThingType.Remote, RelationType {

        static RelationType.Remote of(final Concepts concepts, final String label, final boolean isRoot) {
            return new RelationTypeImpl.Remote(concepts, label, isRoot);
        }

        /**
         * Create a relation of this relation type.
         *
         * @return The newly created relation.
         */
        Relation.Remote create();

        /**
         * Set the super type of this relation type.
         *
         * @param superRelationType The super type to set.
         */
        void setSupertype(RelationType superRelationType);

        /**
         * Retrieve a specific RoleType.
         */
        @Nullable
        @CheckReturnValue
        RoleType.Remote getRelates(String roleLabel);

        /**
         * Retrieves a list of the RoleTypes that make up this RelationType.
         *
         * @return A list of the RoleTypes which make up this RelationType.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Stream<RoleType.Remote> getRelates();

        /**
         * Creates a new RoleType for this RelationType.
         *
         * @param roleLabel The label of a new RoleType which is part of this RelationType.
         * @see RoleType.Remote
         */
        void setRelates(String roleLabel);

        /**
         * Creates a new RoleType override for this RelationType.
         *
         * @param roleLabel The label of a new RoleType which is part of this RelationType.
         * @param overriddenLabel The label of the RoleType that is to be overridden.
         * @see RoleType.Remote
         */
        void setRelates(String roleLabel, String overriddenLabel);

        /**
         * Removes a RoleType from this RelationType.
         *
         * @param roleLabel The label of a RoleType which is part of this RelationType.
         * @see RoleType.Remote
         */
        void unsetRelates(String roleLabel);

        /**
         * Returns a collection of supertypes of this RelationType.
         *
         * @return All the supertypes of this RelationType
         */
        @Override
        Stream<? extends RelationType.Remote> getSupertypes();

        /**
         * Returns a collection of subtypes of this RelationType.
         *
         * @return All the sub types of this RelationType
         */
        @Override
        Stream<? extends RelationType.Remote> getSubtypes();

        /**
         * Retrieve all the Relation instances of this RelationType
         *
         * @return All the Relation instances of this RelationType
         * @see Relation.Remote
         */
        @Override
        Stream<? extends Relation.Remote> getInstances();

        @CheckReturnValue
        @Override
        default RelationType.Remote asRemote(Concepts concepts) {
            return this;
        }

        @CheckReturnValue
        @Override
        default RelationType.Remote asRelationType() {
            return this;
        }
    }
}
