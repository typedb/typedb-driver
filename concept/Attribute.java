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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteAttribute;
import grakn.client.concept.remote.RemoteAttributeType;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;

/**
 * Represent a literal Attribute in the graph.
 * Acts as an Thing when relating to other instances except it has the added functionality of:
 * 1. It is unique to its AttributeType based on it's value.
 * 2. It has an AttributeType.DataType associated with it which constrains the allowed values.
 *
 * @param <D> The data type of this resource type.
 *            Supported Types include: String, Long, Double, and Boolean
 */
public interface Attribute<D> extends Thing<Attribute<D>, AttributeType<D>, RemoteAttribute<D>, RemoteAttributeType<D>> {
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
    AttributeType<D> type();

    /**
     * Retrieves the data type of this Attribute's AttributeType.
     *
     * @return The data type of this Attribute's type.
     */
    @CheckReturnValue
    AttributeType.DataType<D> dataType();

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default Attribute<D> asAttribute() {
        return this;
    }

    @CheckReturnValue
    @Override
    default RemoteAttribute<D> asRemote(GraknClient.Transaction tx) {
        return RemoteAttribute.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttribute() {
        return true;
    }
}
