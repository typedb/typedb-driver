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
import grakn.client.concept.type.AttributeType.ValueType;
import grakn.client.concept.thing.Thing;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A ThingType represents any ontological element in the graph.
 * ThingTypes are used to model the behaviour of Thing and how they relate to each other.
 * They also aid in categorising Thing to different types.
 */
public interface ThingType extends Type {

    @Override
    Remote asRemote(Transaction tx);

    interface Local extends Type.Local, ThingType {

        @CheckReturnValue
        @Override
        default ThingType.Local asThingType() {
            return this;
        }
    }

    /**
     * A ThingType represents any ontological element in the graph.
     * ThingTypes are used to model the behaviour of Thing and how they relate to each other.
     * They also aid in categorising Thing to different types.
     */
    interface Remote extends Type.Remote, ThingType {

        /**
         * @return The direct supertype of this concept
         */
        @Override
        ThingType.Remote getSupertype();

        /**
         * @return All the the super-types of this Type
         */
        @Override
        Stream<? extends ThingType.Remote> getSupertypes();

        /**
         * Get all indirect sub-types of this type.
         * The indirect sub-types are the type itself and all indirect sub-types of direct sub-types.
         *
         * @return All the indirect sub-types of this Type
         */
        @Override
        @CheckReturnValue
        Stream<? extends ThingType.Remote> getSubtypes();

        /**
         * Get all indirect instances of this type.
         * The indirect instances are the direct instances and all indirect instances of direct sub-types.
         *
         * @return All the indirect instances of this type.
         */
        @CheckReturnValue
        Stream<? extends Thing.Remote> getInstances();

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         */
        void setLabel(String label);

        /**
         * Sets the ThingType to be abstract - which prevents it from having any instances.
         */
        void setAbstract();

        /**
         * Sets the ThingType to be not abstract - which allows it to have instances.
         */
        void unsetAbstract();

        /**
         * @param role The RoleType which the instances of this Type are allowed to play.
         */
        void setPlays(RoleType role);

        /**
         * Creates a connection which allows this type and an AttributeType to be linked.
         *
         * @param attributeType The AttributeType  which instances of this type should be allowed to play.
         */
        void setOwns(AttributeType attributeType, AttributeType otherType, boolean isKey);

        default void setOwns(AttributeType attributeType, AttributeType overriddenType) {
            setOwns(attributeType, overriddenType, false);
        }

        default void setOwns(AttributeType attributeType, boolean isKey) {
            setOwns(attributeType, null, isKey);
        }

        default void setOwns(AttributeType attributeType) {
            setOwns(attributeType, false);
        }

        /**
         * @return A list of RoleTypes which instances of this Type can indirectly play.
         */
        Stream<RoleType.Remote> getPlays();

        /**
         * @return The AttributeTypes which this Type is linked with, optionally only keys.
         * @param keysOnly If true, only returns keys.
         */
        @CheckReturnValue
        Stream<? extends AttributeType.Remote> getOwns(ValueType valueType, boolean keysOnly);

        @CheckReturnValue
        default Stream<? extends AttributeType.Remote> getOwns(ValueType valueType) {
            return getOwns(valueType, false);
        }

        @CheckReturnValue
        default Stream<? extends AttributeType.Remote> getOwns(boolean keysOnly) {
            return getOwns(null, keysOnly);
        }

        @CheckReturnValue
        default Stream<? extends AttributeType.Remote> getOwns() {
            return getOwns(false);
        }

        /**
         * Removes the ability of this Type to play a specific RoleType.
         *
         * @param role The RoleType which the Things of this Type should no longer be allowed to play.
         */
        void unsetPlays(RoleType role);

        /**
         * Removes the ability for Things of this Type to have Attributes of type AttributeType
         *
         * @param attributeType the AttributeType which this Type can no longer have
         */
        void unsetOwns(AttributeType attributeType);

        @CheckReturnValue
        @Override
        default ThingType.Remote asThingType() {
            return this;
        }
    }
}
