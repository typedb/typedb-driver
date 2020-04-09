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

import grakn.client.concept.Concept;
import grakn.client.concept.DataType;
import grakn.client.concept.type.AttributeType;
import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of Attribute
 *
 * @param <D> The data type of this attribute
 */
public class AttributeImpl<D> extends ThingImpl<Attribute.Local<D>, AttributeType.Local<D>> implements Attribute.Local<D> {

    private final D value;

    public AttributeImpl(ConceptProto.Concept concept) {
        super(concept);
        this.value = DataType.staticCastValue(concept.getValueRes().getValue());
    }

    @Override
    public final D value() {
        return value;
    }

    @Override
    public final DataType<D> dataType() {
        return type().dataType();
    }

    @Override
    final AttributeType.Local<D> asCurrentType(Concept<?> concept) {
        return (AttributeType.Local<D>) concept.asAttributeType();
    }

    @Override
    final Attribute.Local<D> asCurrentBaseType(Concept<?> other) {
        return (Attribute.Local<D>) other.asAttribute();
    }
}
