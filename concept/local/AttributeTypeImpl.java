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

package grakn.client.concept.local;

import grakn.client.concept.Concept;
import grakn.client.concept.DataType;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.AttributeType;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.session.ConceptProto;

import javax.annotation.Nullable;

/**
 * Client implementation of AttributeType
 *
 * @param <D> The data type of this attribute type
 */
public class AttributeTypeImpl<D> extends TypeImpl<AttributeType.Local<D>, Attribute.Local<D>> implements AttributeType.Local<D> {

    private final DataType<D> dataType;

    public AttributeTypeImpl(ConceptProto.Concept concept) {
        super(concept);
        this.dataType = RequestBuilder.ConceptMessage.dataType(concept.getDataTypeRes().getDataType());
    }

    @Override
    @Nullable
    public DataType<D> dataType() {
        return dataType;
    }

    @Override
    final Local<D> asCurrentBaseType(Concept<?> other) {
        return (Local<D>) other.asAttributeType();
    }

    @Override
    final boolean equalsCurrentBaseType(Concept<?> other) {
        return other.isAttributeType();
    }

    @Override
    protected Attribute.Local<D> asInstance(Concept<?> concept) {
        return (Attribute.Local<D>) concept.asAttribute();
    }

}
