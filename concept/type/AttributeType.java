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
import grakn.client.concept.ValueType;
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.Label;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.impl.AttributeTypeImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface AttributeType<D> extends Type<AttributeType<D>, Attribute<D>> {



    //------------------------------------- Accessors ---------------------------------
    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @Nullable
    @CheckReturnValue
    ValueType<D> valueType();

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default <T> AttributeType<T> asAttributeType() {
        return (AttributeType<T>) this;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default <T> AttributeType<T> asAttributeType(ValueType<T> valueType) {
        if (!valueType.equals(valueType())) {
            throw GraknConceptException.invalidCasting(this, valueType.getClass());
        }
        return (AttributeType<T>) this;
    }

    @Override
    default AttributeType.Remote<D> asRemote(GraknClient.Transaction tx) {
        return AttributeType.Remote.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttributeType() {
        return true;
    }

    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueType constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     *
     * @param <D> The data type of this resource type.
     *            Supported Types include: String, Long, Double, and Boolean
     */
    interface Local<D> extends Type.Local<AttributeType<D>, Attribute<D>>, AttributeType<D> {
    }

    /**
     * An ontological element which models and categorises the various Attribute in the graph.
     * This ontological element behaves similarly to Type when defining how it relates to other
     * types. It has two additional functions to be aware of:
     * 1. It has a ValueType constraining the data types of the values it's instances may take.
     * 2. Any of it's instances are unique to the type.
     * For example if you have an AttributeType modelling month throughout the year there can only be one January.
     *
     * @param <D> The data type of this resource type.
     *            Supported Types include: String, Long, Double, and Boolean
     */
    interface Remote<D> extends Type.Remote<AttributeType<D>, Attribute<D>>, AttributeType<D> {

        static <D> AttributeType.Remote<D> of(GraknClient.Transaction tx, ConceptId id) {
            return new AttributeTypeImpl.Remote<>(tx, id);
        }

        //------------------------------------- Modifiers ----------------------------------

        /**
         * Changes the Label of this Concept to a new one.
         *
         * @param label The new Label.
         * @return The Concept itself
         */
        AttributeType.Remote<D> label(Label label);

        /**
         * Sets the AttributeType to be abstract - which prevents it from having any instances.
         *
         * @param isAbstract Specifies if the AttributeType is to be abstract (true) or not (false).
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote<D> isAbstract(Boolean isAbstract);

        /**
         * Sets the supertype of the AttributeType to be the AttributeType specified.
         *
         * @param type The super type of this AttributeType.
         * @return The AttributeType itself.
         */
        AttributeType.Remote<D> sup(AttributeType<D> type);

        /**
         * Sets the Role which instances of this AttributeType may play.
         *
         * @param role The Role Type which the instances of this AttributeType are allowed to play.
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote<D> plays(Role role);

        /**
         * Removes the ability of this AttributeType to play a specific Role
         *
         * @param role The Role which the Things of this AttributeType should no longer be allowed to play.
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote<D> unplay(Role role);

        /**
         * Removes the ability for Things of this AttributeType to have Attributes of type AttributeType
         *
         * @param attributeType the AttributeType which this AttributeType can no longer have
         * @return The AttributeType itself.
         */
        @Override
        AttributeType.Remote<D> unhas(AttributeType<?> attributeType);

        /**
         * Set the regular expression that instances of the AttributeType must conform to.
         *
         * @param regex The regular expression that instances of this AttributeType must conform to.
         * @return The AttributeType itself.
         */
        AttributeType.Remote<D> regex(String regex);

        /**
         * Set the value for the Attribute, unique to its type.
         *
         * @param value A value for the Attribute which is unique to its type
         * @return new or existing Attribute of this type with the provided value.
         */
        Attribute.Remote<D> create(D value);

        /**
         * Creates a RelationType which allows this type and a resource type to be linked.
         *
         * @param attributeType The resource type which instances of this type should be allowed to play.
         * @return The Type itself.
         */
        @Override
        AttributeType.Remote<D> has(AttributeType<?> attributeType);
        @Override
        AttributeType.Remote<D> has(AttributeType<?> attributeType, boolean isKey);
        @Override
        AttributeType.Remote<D> has(AttributeType<?> attributeType, AttributeType<?> overriddenType);
        @Override
        AttributeType.Remote<D> has(AttributeType<?> attributeType, AttributeType<?> overriddenType, boolean isKey);

        //------------------------------------- Accessors ---------------------------------

        /**
         * Get the Attribute with the value provided, and its type, or return NULL
         *
         * @param value A value which an Attribute in the graph may be holding
         * @return The Attribute with the provided value and type or null if no such Attribute exists.
         * @see Attribute.Remote
         */
        @CheckReturnValue
        @Nullable
        Attribute.Remote<D> attribute(D value);

        /**
         * Returns a collection of super-types of this AttributeType.
         *
         * @return The super-types of this AttributeType
         */
        @Override
        Stream<AttributeType.Remote<D>> sups();

        /**
         * Returns a collection of subtypes of this AttributeType.
         *
         * @return The subtypes of this AttributeType
         */
        @Override
        Stream<AttributeType.Remote<D>> subs();

        /**
         * Returns a collection of all Attribute of this AttributeType.
         *
         * @return The resource instances of this AttributeType
         */
        @Override
        Stream<Attribute.Remote<D>> instances();

        /**
         * Retrieve the regular expression to which instances of this AttributeType must conform, or {@code null} if no
         * regular expression is set.
         * By default, an AttributeType does not have a regular expression set.
         *
         * @return The regular expression to which instances of this AttributeType must conform.
         */
        @CheckReturnValue
        @Nullable
        String regex();

        //------------------------------------- Other ---------------------------------
        @SuppressWarnings("unchecked")
        @Deprecated
        @CheckReturnValue
        @Override
        default <T> AttributeType.Remote<T> asAttributeType() {
            return (AttributeType.Remote<T>) this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default <T> AttributeType.Remote<T> asAttributeType(ValueType<T> valueType) {
            return (AttributeType.Remote<T>) AttributeType.super.asAttributeType(valueType);
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isAttributeType() {
            return true;
        }
    }
}
