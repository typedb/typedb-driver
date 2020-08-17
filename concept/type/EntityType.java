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
    default Remote asRemote(Transaction tx) {
        return EntityType.Remote.of(tx, getLabel());
    }

    interface Local extends ThingType.Local, EntityType {

        @CheckReturnValue
        @Override
        default EntityType.Local asEntityType() {
            return this;
        }
    }

    /**
     * Type used to represent entities.
     * An ontological element which represents entity instances can fall within.
     * Any instance of a Entity Type is called an Entity.
     */
    interface Remote extends ThingType.Remote, EntityType {

        static EntityType.Remote of(Transaction tx, String label) {
            return new EntityTypeImpl.Remote(tx, label);
        }

        /**
         * Creates and returns a new Entity instance, whose direct type will be this type.
         *
         * @return a new empty entity.
         * @see Entity.Remote
         */
        Entity.Remote create();

        /**
         * Sets the supertype of this instance to the given type.
         */
        void setSupertype(EntityType superEntityType);

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
