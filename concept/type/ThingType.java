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
import grakn.client.concept.Label;
import grakn.client.concept.ValueType;
import grakn.client.concept.thing.Thing;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * A Type represents any ontological element in the graph.
 * Types are used to model the behaviour of Thing and how they relate to each other.
 * They also aid in categorising Thing to different types.
 */
public interface ThingType<SomeType extends ThingType<SomeType, SomeThing>,
                      SomeThing extends Thing<SomeThing, SomeType>>
        extends Type<SomeType> {

    @Deprecated
    @CheckReturnValue
    @Override
    default ThingType<SomeType, SomeThing> asType() {
        return this;
    }

    @Override
    Remote<SomeType, SomeThing> asRemote(Transaction tx);

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isType() {
        return true;
    }

    interface Local<
            SomeType extends ThingType<SomeType, SomeThing>,
            SomeThing extends Thing<SomeThing, SomeType>>
            extends Type.Local<SomeType>, ThingType<SomeType, SomeThing> {
    }

    /**
     * A Type represents any ontological element in the graph.
     * Types are used to model the behaviour of Thing and how they relate to each other.
     * They also aid in categorising Thing to different types.
     */
    interface Remote<
            SomeRemoteType extends ThingType<SomeRemoteType, SomeRemoteThing>,
            SomeRemoteThing extends Thing<SomeRemoteThing, SomeRemoteType>>
            extends Type.Remote<SomeRemoteType>, ThingType<SomeRemoteType, SomeRemoteThing> {

        //------------------------------------- Modifiers ----------------------------------

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> setLabel(Label label);

        /**
         * Sets the Type to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the concept is to be abstract (true) or not (false).
         * @return The concept itself
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> isAbstract(Boolean isAbstract);

        /**
         * @param role The RoleType which the instances of this Type are allowed to play.
         * @return The Type itself.
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> setPlays(RoleType role);


        /**
         * Creates a connection which allows this type and a AttributeType to be linked.
         *
         * @param attributeType The AttributeType  which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> setOwns(AttributeType<?> attributeType, AttributeType<?> otherType, boolean isKey);

        default ThingType.Remote<SomeRemoteType, SomeRemoteThing> setOwns(AttributeType<?> attributeType, AttributeType<?> overriddenType) {
            return setOwns(attributeType, overriddenType, false);
        }

        default ThingType.Remote<SomeRemoteType, SomeRemoteThing> setOwns(AttributeType<?> attributeType, boolean isKey) {
            return setOwns(attributeType, null, isKey);
        }

        default ThingType.Remote<SomeRemoteType, SomeRemoteThing> setOwns(AttributeType<?> attributeType) {
            return setOwns(attributeType, false);
        }

        //------------------------------------- Accessors ---------------------------------

        /**
         * @return A list of RoleTypes which instances of this Type can indirectly play.
         */
        Stream<RoleType.Remote> getPlays();

        /**
         * @return The AttributeTypes which this Type is linked with, optionally only keys.
         * @param keysOnly If true, only returns keys.
         */
        @CheckReturnValue
        <D> Stream<? extends AttributeType.Remote<D>> getOwns(ValueType<D> valueType, boolean keysOnly);
        @CheckReturnValue
        default <D> Stream<? extends AttributeType.Remote<D>> attributes(ValueType<D> valueType) {
            return getOwns(valueType, false);
        }
        @CheckReturnValue
        default Stream<? extends AttributeType.Remote<?>> getOwns(boolean keysOnly) {
            return getOwns(null, keysOnly);
        }
        @CheckReturnValue
        default Stream<? extends AttributeType.Remote<?>> attributes() {
            return getOwns(false);
        }

        /**
         * @return All the the super-types of this Type
         */
        @Override
        Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> getSupertypes();

        /**
         * Get all indirect sub-types of this type.
         * The indirect sub-types are the type itself and all indirect sub-types of direct sub-types.
         *
         * @return All the indirect sub-types of this Type
         */
        @Override
        @CheckReturnValue
        Stream<? extends ThingType.Remote<SomeRemoteType, SomeRemoteThing>> getSubtypes();

        /**
         * Get all indirect instances of this type.
         * The indirect instances are the direct instances and all indirect instances of direct sub-types.
         *
         * @return All the indirect instances of this type.
         */
        @CheckReturnValue
        Stream<? extends Thing.Remote<SomeRemoteThing, SomeRemoteType>> getInstances();

        /**
         * Return if the type is set to abstract.
         * By default, types are not abstract.
         *
         * @return returns true if the type is set to be abstract.
         */
        @CheckReturnValue
        Boolean isAbstract();

        //------------------------------------- Other ----------------------------------

        /**
         * Removes the ability of this Type to play a specific RoleType.
         *
         * @param role The RoleType which the Things of this Type should no longer be allowed to play.
         * @return The Type itself.
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> unsetPlays(RoleType role);

        /**
         * Removes the ability for Things of this Type to have Attributes of type AttributeType
         *
         * @param attributeType the AttributeType which this Type can no longer have
         * @return The Type itself.
         */
        ThingType.Remote<SomeRemoteType, SomeRemoteThing> unsetOwns(AttributeType<?> attributeType);

        @Deprecated
        @CheckReturnValue
        @Override
        default ThingType.Remote<SomeRemoteType, SomeRemoteThing> asType() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isType() {
            return true;
        }
    }
}
