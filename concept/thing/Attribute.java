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

package grakn.client.concept.thing;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.GraknConceptException;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.ValueType;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public interface Attribute extends Thing {

    /**
     * Retrieves the value of the Attribute.
     *
     * @return The value itself
     */
    @CheckReturnValue
    D getValue();

    /**
     * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
     *
     * @return The AttributeType of which this resource is an Thing.
     */
    @Override
    AttributeType getType();

    /**
     * Retrieves the data type of this Attribute's AttributeType.
     *
     * @return The data type of this Attribute's type.
     */
    @CheckReturnValue
    ValueType getValueType();

    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default Attribute asAttribute() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default Attribute asAttribute(ValueType valueType) {
        if (!getValueType().equals(valueType)) {
            throw GraknConceptException.invalidCasting(this, valueType.getClass());
        }
        return this;
    }

    @CheckReturnValue
    @Override
    default Remote asRemote(Transaction tx) {
        return Attribute.Remote.of(tx, iid());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttribute() {
        return true;
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Local extends Thing.Local, Attribute {
    }

    /**
     * Represent a literal Attribute in the graph.
     * Acts as an Thing when relating to other instances except it has the added functionality of:
     * 1. It is unique to its AttributeType based on it's value.
     * 2. It has an AttributeType.ValueType associated with it which constrains the allowed values.
     */
    interface Remote extends Thing.Remote, Attribute {

        static Attribute.Remote of(Transaction tx, ConceptIID iid) {
            return new AttributeImpl.Remote<>(tx, iid);
        }

        /**
         * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
         *
         * @return The AttributeType of which this resource is an Thing.
         */
        @Override
        AttributeType.Remote getType();

        /**
         * Retrieves the set of all Instances that possess this Attribute.
         *
         * @return The list of all Instances that possess this Attribute.
         */
        @CheckReturnValue
        Stream<Thing.Remote> getOwners();

        /**
         * Creates a relation from this instance to the provided Attribute.
         *
         * @param attribute The Attribute to which a relation is created
         * @return The instance itself
         */
        @Override
        Attribute.Remote setHas(Attribute attribute);

        /**
         * Removes the provided Attribute from this Attribute
         *
         * @param attribute the Attribute to be removed
         * @return The Attribute itself
         */
        @Override
        Attribute.Remote unsetHas(Attribute attribute);

        @SuppressWarnings("unchecked")
        @Deprecated
        @CheckReturnValue
        @Override
        default Attribute.Remote asAttribute() {
            return this;
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default Attribute.Remote asAttribute(ValueType valueType) {
            return (Attribute.Remote) Attribute.super.asAttribute(valueType);
        }

        @Deprecated
        @CheckReturnValue
        @Override
        default boolean isAttribute() {
            return true;
        }
    }
}
