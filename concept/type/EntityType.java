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
import grakn.client.concept.thing.Entity;
import grakn.client.concept.type.impl.EntityTypeImpl;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Type used to represent entities.
 * An ontological element which represents entity instances can fall within.
 * Any instance of a Entity Type is called an Entity.
 */
public interface EntityType extends ThingType {

    @CheckReturnValue
    @Override
    default EntityType asEntityType() {
        return this;
    }

    @CheckReturnValue
    @Override
    default Remote asRemote(Transaction tx) {
        return EntityType.Remote.of(tx, getIID());
    }

    interface Local extends ThingType.Local, EntityType {
    }

    /**
     * Type used to represent entities.
     * An ontological element which represents entity instances can fall within.
     * Any instance of a Entity Type is called an Entity.
     */
    interface Remote extends ThingType.Remote, EntityType {

        static EntityType.Remote of(Transaction tx, ConceptIID iid) {
            return new EntityTypeImpl.Remote(tx, iid);
        }

        //------------------------------------- Modifiers ----------------------------------

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         */
        void setLabel(String label);

        /**
         * Sets the EntityType to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the EntityType is to be abstract (true) or not (false).
         */
        @Override
        void setAbstract(boolean isAbstract);

        /**
         * Sets a RoleType which instances of this EntityType may play.
         *
         * @param role The RoleType which the instances of this EntityType are allowed to play.
         */
        @Override
        void setPlays(RoleType role);

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
        EntityType.Remote setSupertype(EntityType superEntityType);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         */
        @Override
        void setOwns(AttributeType attributeType);
        @Override
        void setOwns(AttributeType attributeType, boolean isKey);
        @Override
        void setOwns(AttributeType attributeType, AttributeType overriddenType);
        @Override
        void setOwns(AttributeType attributeType, AttributeType overriddenType, boolean isKey);

        //------------------------------------- Accessors ----------------------------------

        /**
         * Returns a collection of supertypes of this EntityType.
         *
         * @return All the super classes of this EntityType
         */
        @Override
        Stream<? extends EntityType.Remote> getSupertypes();

        /**
         * Returns a collection of subtypes of this EntityType.
         *
         * @return All the sub classes of this EntityType
         */
        @Override
        Stream<? extends EntityType.Remote> getSubtypes();

        /**
         * Returns a collection of all Entity instances for this EntityType.
         *
         * @return All the instances of this EntityType.
         * @see Entity.Remote
         */
        @Override
        Stream<? extends Entity.Remote> getInstances();

        @CheckReturnValue
        @Override
        default EntityType.Remote asEntityType() {
            return this;
        }

    }
}
