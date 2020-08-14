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

import grakn.client.Grakn.Transaction;
import grakn.client.concept.Concept;
import grakn.client.concept.ConceptIID;
import grakn.client.concept.ValueTypeOld;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.protocol.ConceptProto;

import java.util.stream.Stream;

import static grakn.client.concept.ValueTypeOld.staticCastValue;

public class AttributeImpl {

    /**
     * Client implementation of Attribute
     *
     * @param <VALUE> The value type of this attribute
     */
    public abstract static class Local<VALUE> extends ThingImpl.Local implements Attribute.Local {

        private final VALUE value;

        public Local(ConceptProto.Concept concept) {
            super(concept);
            this.value = ValueTypeOld.staticCastValue(concept.getValueRes().getValue());
        }

        @Override
        public AttributeType getType() {
            return (AttributeType) super.getType();
        }

        public abstract VALUE getValue();
    }

    /**
     * Client implementation of Attribute
     *
     * @param <VALUE> The value type of this attribute
     */
    public abstract static class Remote<VALUE> extends ThingImpl.Remote implements Attribute.Remote {

        public Remote(Transaction tx, ConceptIID iid) {
            super(tx, iid);
        }

        @Override
        public final Stream<Thing.Remote> getOwners() {
            ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                    .setAttributeGetOwnersIterReq(ConceptProto.Attribute.GetOwners.Iter.Req.getDefaultInstance()).build();
            return conceptStream(method, res -> res.getAttributeGetOwnersIterRes().getThing()).map(Concept.Remote::asThing);
        }

        @Override
        public AttributeType.Remote getType() {
            return (AttributeType.Remote) super.getType();
        }

        @Override
        public Attribute.Remote setHas(Attribute attribute) {
            return (Attribute.Remote) super.setHas(attribute);
        }

        @Override
        protected final Attribute.Remote asCurrentBaseType(Concept.Remote other) {
            return other.asAttribute();
        }
    }

    public abstract static class Boolean extends AttributeImpl<java.lang.Boolean> implements Attribute.Boolean {
        /**
         * Client implementation of Attribute.Boolean
         */
        public static class Local<VALUE> extends AttributeImpl<VALUE>.Local implements Attribute.Local {

            private final D value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = ValueTypeOld.staticCastValue(concept.getValueRes().getValue());
            }

            @Override
            public final D getValue() {
                return value;
            }

        }

        /**
         * Client implementation of Attribute
         *
         * @param <VALUE> The data type of this attribute
         */
        public static class Remote<D> extends ThingImpl.Remote implements Attribute.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final VALUE getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                final ConceptProto.ValueObject value = runMethod(method).getAttributeGetValueRes().getValue();
                return staticCastValue(value);
            }

            @Override
            public final Stream<Thing.Remote> getOwners() {
                ConceptProto.Method.Iter.Req method = ConceptProto.Method.Iter.Req.newBuilder()
                        .setAttributeGetOwnersIterReq(ConceptProto.Attribute.GetOwners.Iter.Req.getDefaultInstance()).build();
                return conceptStream(method, res -> res.getAttributeGetOwnersIterRes().getThing()).map(Concept.Remote::asThing);
            }

            @Override
            public AttributeType.Remote getType() {
                return (AttributeType.Remote) super.getType();
            }

            @Override
            public Attribute.Remote setHas(Attribute attribute) {
                return (Attribute.Remote) super.setHas(attribute);
            }

            @Override
            protected final Attribute.Remote asCurrentBaseType(Concept.Remote other) {
                return other.asAttribute();
            }
        }
    }
}
