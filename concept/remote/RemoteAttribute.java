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

package grakn.client.concept.remote;

import grakn.client.GraknClient;
import grakn.client.concept.Attribute;
import grakn.client.concept.AttributeType;
import grakn.client.concept.ConceptId;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Represent a literal Attribute in the graph.
 * Acts as an Thing when relating to other instances except it has the added functionality of:
 * 1. It is unique to its AttributeType based on it's value.
 * 2. It has an AttributeType.DataType associated with it which constrains the allowed values.
 *
 * @param <D> The data type of this resource type.
 *            Supported Types include: String, Long, Double, and Boolean
 */
public interface RemoteAttribute<D> extends Attribute<D>,
        RemoteThing<RemoteAttribute<D>, RemoteAttributeType<D>, Attribute<D>, AttributeType<D>> {

    static <D> RemoteAttribute<D> of(GraknClient.Transaction tx, ConceptId id) {
        return new RemoteAttributeImpl<>(tx, id);
    }

    //------------------------------------- Accessors ----------------------------------

    /**
     * Retrieves the value of the Attribute.
     *
     * @return The value itself
     */
    @CheckReturnValue
    D value();

    /**
     * Retrieves the type of the Attribute, that is, the AttributeType of which this resource is an Thing.
     *
     * @return The AttributeType of which this resource is an Thing.
     */
    @Override
    RemoteAttributeType<D> type();

    /**
     * Retrieves the data type of this Attribute's AttributeType.
     *
     * @return The data type of this Attribute's type.
     */
    @CheckReturnValue
    RemoteAttributeType.DataType<D> dataType();

    /**
     * Retrieves the set of all Instances that possess this Attribute.
     *
     * @return The list of all Instances that possess this Attribute.
     */
    @CheckReturnValue
    Stream<RemoteThing<?, ?, ?, ?>> owners();

    /**
     * Creates a relation from this instance to the provided Attribute.
     *
     * @param attribute The Attribute to which a relation is created
     * @return The instance itself
     */
    @Override
    RemoteAttribute<D> has(Attribute<?> attribute);

    /**
     * Removes the provided Attribute from this Attribute
     *
     * @param attribute the Attribute to be removed
     * @return The Attribute itself
     */
    @Override
    RemoteAttribute<D> unhas(Attribute<?> attribute);

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default RemoteAttribute<D> asAttribute() {
        return this;
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttribute() {
        return true;
    }
}
