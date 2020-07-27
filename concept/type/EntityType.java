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
import grakn.client.concept.ConceptId;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Entity;
import grakn.client.concept.type.impl.EntityTypeImpl;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * SchemaConcept used to represent categories.
 * An ontological element which represents categories instances can fall within.
 * Any instance of a Entity Type is called an Entity.
 */
public interface EntityType extends Type<EntityType, Entity> {

    //------------------------------------- Other ---------------------------------
    @Deprecated
    @CheckReturnValue
    @Override
    default EntityType asEntityType() {
        return this;
    }

    @CheckReturnValue
    @Override
    default Remote asRemote(GraknClient.Transaction tx) {
        return EntityType.Remote.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isEntityType() {
        return true;
    }

    interface Local extends Type.Local<EntityType, Entity>, EntityType {
    }

    /**
     * SchemaConcept used to represent categories.
     * An ontological element which represents categories instances can fall within.
     * Any instance of a Entity Type is called an Entity.
     */
    interface Remote extends Type.Remote<EntityType, Entity>, EntityType {

        static EntityType.Remote of(GraknClient.Transaction tx, ConceptId id) {
            return new EntityTypeImpl.Remote(tx, id);
        }

        //------------------------------------- Modifiers ----------------------------------

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        EntityType.Remote label(Label label);

        /**
         * Sets the EntityType to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the EntityType is to be abstract (true) or not (false).
         * @return The EntityType itself
         */
        @Override
        EntityType.Remote isAbstract(Boolean isAbstract);

        /**
         * Sets the Role which instances of this EntityType may play.
         *
         * @param role The Role Type which the instances of this EntityType are allowed to play.
         * @return The EntityType itself
         */
        @Override
        EntityType.Remote plays(Role role);

        /**
         * Removes the ability of this EntityType to play a specific Role
         *
         * @param role The Role which the Things of this EntityType should no longer be allowed to play.
         * @return The EntityType itself.
         */
        @Override
        EntityType.Remote unplay(Role role);

        /**
         * Removes the ability for Things of this EntityType to have Attributes of type AttributeType
         *
         * @param attributeType the AttributeType which this EntityType can no longer have
         * @return The EntityType itself.
         */
        @Override
        EntityType.Remote unhas(AttributeType<?> attributeType);

        /**
         * Creates and returns a new Entity instance, whose direct type will be this type.
         *
         * @return a new empty entity.
         * @see Entity.Remote
         */
        Entity.Remote create();

        /**
         * Sets the supertype of this instance to the given type.
         *
         * @return the new super type.
         * @see Entity.Remote This concept itself.
         */
        EntityType.Remote sup(EntityType superEntityType);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        @Override
        EntityType.Remote has(AttributeType<?> attributeType);
        @Override
        EntityType.Remote has(AttributeType<?> attributeType, boolean isKey);
        @Override
        EntityType.Remote has(AttributeType<?> attributeType, AttributeType<?> overriddenType);
        @Override
        EntityType.Remote has(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey);

        //------------------------------------- Accessors ----------------------------------

        /**
         * Returns a collection of supertypes of this EntityType.
         *
         * @return All the super classes of this EntityType
         */
        @Override
        Stream<EntityType.Remote> sups();

        /**
         * Returns a collection of subtypes of this EntityType.
         *
         * @return All the sub classes of this EntityType
         */
        @Override
        Stream<EntityType.Remote> subs();

        /**
         * Returns a collection of all Entity instances for this EntityType.
         *
         * @return All the instances of this EntityType.
         * @see Entity.Remote
         */
        @Override
        Stream<Entity.Remote> instances();

        //------------------------------------- Other ---------------------------------
        @Deprecated
        @CheckReturnValue
        @Override
        default EntityType.Remote asEntityType() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isEntityType() {
            return true;
        }
    }
}
