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
import grakn.client.concept.Label;
import grakn.client.concept.thing.Relation;
import grakn.client.concept.type.impl.RelationTypeImpl;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * An ontological element which categorises how Things may relate to each other.
 * A RelationType defines how Type may relate to one another.
 * They are used to model and categorise n-ary Relations.
 */
public interface RelationType extends ThingType {
    //------------------------------------- Other ---------------------------------
    @CheckReturnValue
    @Override
    default RelationType asRelationType() {
        return this;
    }

    @Override
    default Remote asRemote(Transaction tx) {
        return RelationType.Remote.of(tx, getIID());
    }

    interface Local extends ThingType.Local, RelationType {
    }

    /**
     * An ontological element which categorises how Things may relate to each other.
     * A RelationType defines how Type may relate to one another.
     * They are used to model and categorise n-ary Relations.
     */
    interface Remote extends ThingType.Remote, RelationType {

        static RelationType.Remote of(Transaction tx, ConceptIID iid) {
            return new RelationTypeImpl.Remote(tx, iid);
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
         * @return This concept itself.
         */
        RelationType.Remote setSupertype(RelationType superRelationType);

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        @Override
        RelationType.Remote setLabel(Label label);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        @Override
        RelationType.Remote setOwns(AttributeType attributeType);
        @Override
        RelationType.Remote setOwns(AttributeType attributeType, boolean isKey);
        @Override
        RelationType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType);
        @Override
        RelationType.Remote setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey);

        /**
         * Retrieve a specific RoleType.
         *
         *
         */
        default RoleType.Remote getRelates(String role) {
            return getRelates(Label.of(role));
        }

        RoleType.Remote getRelates(Label role);

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
         * @param role A new RoleType which is part of this RelationType.
         * @return The RoleType itself.
         * @see RoleType.Remote
         */
        default RoleType.Remote setRelates(String role) {
            return setRelates(Label.of(role));
        }

        RoleType.Remote setRelates(Label role);

        //---- Inherited Methods

        /**
         * Sets the RelationType to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the concept is to be abstract (true) or not (false).
         * @return The RelationType itself.
         */
        @Override
        RelationType.Remote isAbstract(Boolean isAbstract);

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

        /**
         * Sets the RoleType which instances of this RelationType may play.
         *
         * @param role The RoleType which the instances of this RelationType are allowed to play.
         * @return The RelationType itself.
         */
        @Override
        RelationType.Remote setPlays(RoleType role);

        @CheckReturnValue
        @Override
        default RelationType.Remote asRelationType() {
            return this;
        }

    }
}
