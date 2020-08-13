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

package grakn.client.concept.thing.impl;

import grakn.client.GraknClient;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.ValueType;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.concept.ValueType.staticCastValue;

public class AttributeImpl {
    /**
     * Client implementation of Attribute
     *
     * @param <D> The data type of this attribute
     */
    public static class Local<D> extends ThingImpl.Local<Attribute<D>, AttributeType<D>> implements Attribute.Local<D> {

        private final D value;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.value = ValueType.staticCastValue(concept.getValueRes().getValue());
        }

        @Override
        public final D value() {
            return value;
        }

        @Override
        public final ValueType<D> valueType() {
            return getType().valueType();
        }
    }

    /**
     * Client implementation of Attribute
     *
     * @param <D> The data type of this attribute
     */
    public static class Remote<D> extends ThingImpl.Local.Remote<Attribute<D>, AttributeType<D>> implements Attribute.Remote<D> {

        public Remote(GraknClient.Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final D value() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeValueReq(ConceptProto.Attribute.Value.Req.getDefaultInstance()).build();

            ConceptProto.ValueObject value = runMethod(method).getAttributeValueRes().getValue();
            return staticCastValue(value);
        }

        @Override
        public final Stream<Thing.Remote<?, ?>> owners() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setAttributeOwnersIterReq(ConceptProto.Attribute.Owners.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getAttributeOwnersIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public final ValueType<D> valueType() {
            return getType().valueType();
        }

        @Override
        public AttributeType.Remote<D> getType() {
            return (AttributeType.Remote<D>) super.getType();
        }

        @Override
        public Attribute.Remote<D> has(Attribute<?> attribute) {
            return (Attribute.Remote<D>) super.has(attribute);
        }

        @Override
        public Attribute.Remote<D> unhas(Attribute<?> attribute) {
            return (Attribute.Remote<D>) super.unhas(attribute);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected final Attribute.Remote<D> asCurrentBaseType(Concept.Remote<?> other) {
            return (Attribute.Remote<D>) other.asAttribute();
        }
    }
}
