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
import grakn.protocol.session.ConceptProto;

/**
 * Client implementation of Attribute
 *
 * @param <D> The data type of this attribute
 */
class AttributeImpl<D> extends ThingImpl<LocalAttribute<D>, LocalAttributeType<D>> implements LocalAttribute<D> {

    private final D value;

    AttributeImpl(ConceptProto.Concept concept) {
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
    final LocalAttributeType<D> asCurrentType(Concept<?> concept) {
        return (LocalAttributeType<D>) concept.asAttributeType();
    }

    @Override
    final LocalAttribute<D> asCurrentBaseType(Concept<?> other) {
        return (LocalAttribute<D>) other.asAttribute();
    }
}
