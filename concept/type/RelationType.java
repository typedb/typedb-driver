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

import grakn.client.GraknClient;
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
public interface RelationType extends ThingType<RelationType, Relation> {
    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default RelationType asRelationType() {
        return this;
    }

    @Override
    default Remote asRemote(GraknClient.Transaction tx) {
        return RelationType.Remote.of(tx, iid());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isRelationType() {
        return true;
    }

    interface Local extends ThingType.Local<RelationType, Relation>, RelationType {
    }

    /**
     * An ontological element which categorises how Things may relate to each other.
     * A RelationType defines how Type may relate to one another.
     * They are used to model and categorise n-ary Relations.
     */
    interface Remote extends ThingType.Remote<RelationType, Relation>, RelationType {

        static RelationType.Remote of(GraknClient.Transaction tx, ConceptIID iid) {
            return new RelationTypeImpl.Remote(tx, iid);
        }

        //------------------------------------- Modifiers ----------------------------------

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
        RelationType.Remote sup(RelationType superRelationType);

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        @Override
        RelationType.Remote label(Label label);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        @Override
        RelationType.Remote has(AttributeType<?> attributeType);
        @Override
        RelationType.Remote has(AttributeType<?> attributeType, boolean isKey);
        @Override
        RelationType.Remote has(AttributeType<?> attributeType, AttributeType<?> overriddenType);
        @Override
        RelationType.Remote has(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey);

        //------------------------------------- Accessors ----------------------------------

        /**
         * Retrieve a specific role.
         *
         *
         */
        default RoleType.Remote role(String role) {
            return role(Label.of(role));
        }
        RoleType.Remote role(Label role);

        /**
         * Retrieves a list of the RoleTypes that make up this RelationType.
         *
         * @return A list of the RoleTypes which make up this RelationType.
         * @see RoleType.Remote
         */
        @CheckReturnValue
        Stream<RoleType.Remote> roles();

        //------------------------------------- Edge Handling ----------------------------------

        /**
         * Creates a new Role for this RelationType.
         *
         * @param role A new role which is part of this relation.
         * @return The Role itself.
         * @see RoleType.Remote
         */
        default RoleType.Remote relates(String role) {
            return relates(Label.of(role));
        }
        RoleType.Remote relates(Label role);

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
        Stream<RelationType.Remote> sups();

        /**
         * Returns a collection of subtypes of this RelationType.
         *
         * @return All the sub types of this RelationType
         */
        @Override
        Stream<RelationType.Remote> subs();

        /**
         * Sets the Role which instances of this RelationType may play.
         *
         * @param role The Role which the instances of this Type are allowed to play.
         * @return The RelationType itself.
         */
        @Override
        RelationType.Remote plays(RoleType role);

        /**
         * Removes the ability of this RelationType to play a specific Role
         *
         * @param role The Role which the Things of this Rule should no longer be allowed to play.
         * @return The Rule itself.
         */
        @Override
        RelationType.Remote unplay(RoleType role);

        /**
         * Removes the ability for Things of this RelationType to have Attributes of type AttributeType
         *
         * @param attributeType the AttributeType which this RelationType can no longer have
         * @return The RelationType itself.
         */
        @Override
        RelationType.Remote unhas(AttributeType<?> attributeType);

        /**
         * Retrieve all the Relation instances of this RelationType
         *
         * @return All the Relation instances of this RelationType
         * @see Relation.Remote
         */
        @Override
        Stream<Relation.Remote> instances();

        //------------------------------------- Other ---------------------------------
        @Deprecated
        @CheckReturnValue
        @Override
        default RelationType.Remote asRelationType() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isRelationType() {
            return true;
        }
    }
}
