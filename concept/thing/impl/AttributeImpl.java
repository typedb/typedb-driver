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
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.protocol.ConceptProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

public class AttributeImpl {

    /**
     * Client implementation of Attribute
     *
     * @param <VALUE> The value type of this attribute
     */
    public abstract static class Local<VALUE> extends ThingImpl.Local implements Attribute.Local {

        public Local(ConceptProto.Concept concept) {
            super(concept);
        }

        @Override
        public AttributeType getType() {
            return (AttributeType.Local) super.getType();
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

        public abstract VALUE getValue();

    }

    public abstract static class Boolean implements Attribute.Boolean {
        /**
         * Client implementation of Attribute.Boolean
         */
        public static class Local extends AttributeImpl.Local<java.lang.Boolean> implements Attribute.Boolean.Local {

            private final java.lang.Boolean value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = concept.getValueRes().getValue().getBoolean();
            }

            @Override
            public final java.lang.Boolean getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.Boolean.Local asBoolean() {
                return this;
            }
        }

        /**
         * Client implementation of Attribute.Boolean
         */
        public static class Remote extends AttributeImpl.Remote<java.lang.Boolean> implements Attribute.Boolean.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final java.lang.Boolean getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getBoolean();
            }

            @Override
            public final AttributeImpl.Boolean.Remote asBoolean() {
                return this;
            }

            @Override
            public AttributeType.Boolean.Remote getType() {
                return super.getType().asBoolean();
            }

            @Override
            public Attribute.Boolean.Remote setHas(Attribute attribute) {
                return super.setHas(attribute).asBoolean();
            }
        }
    }

    public abstract static class Long implements Attribute.Long {
        /**
         * Client implementation of Attribute.Long
         */
        public static class Local extends AttributeImpl.Local<java.lang.Long> implements Attribute.Long.Local {

            private final long value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = concept.getValueRes().getValue().getLong();
            }

            @Override
            public final java.lang.Long getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.Long.Local asLong() {
                return this;
            }
        }

        /**
         * Client implementation of Attribute.Long
         */
        public static class Remote extends AttributeImpl.Remote<java.lang.Long> implements Attribute.Long.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final java.lang.Long getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getLong();
            }

            @Override
            public final AttributeImpl.Long.Remote asLong() {
                return this;
            }

            @Override
            public AttributeType.Long.Remote getType() {
                return super.getType().asLong();
            }

            @Override
            public Attribute.Long.Remote setHas(Attribute attribute) {
                return super.setHas(attribute).asLong();
            }
        }
    }

    public abstract static class Double implements Attribute.Double {
        /**
         * Client implementation of Attribute.Double
         */
        public static class Local extends AttributeImpl.Local<java.lang.Double> implements Attribute.Double.Local {

            private final double value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = concept.getValueRes().getValue().getDouble();
            }

            @Override
            public final java.lang.Double getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.Double.Local asDouble() {
                return this;
            }
        }

        /**
         * Client implementation of Attribute.Double
         */
        public static class Remote extends AttributeImpl.Remote<java.lang.Double> implements Attribute.Double.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final java.lang.Double getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getDouble();
            }

            @Override
            public final AttributeImpl.Double.Remote asDouble() {
                return this;
            }

            @Override
            public AttributeType.Double.Remote getType() {
                return super.getType().asDouble();
            }

            @Override
            public Attribute.Double.Remote setHas(Attribute attribute) {
                return super.setHas(attribute).asDouble();
            }
        }
    }

    public abstract static class String implements Attribute.String {
        /**
         * Client implementation of Attribute.String
         */
        public static class Local extends AttributeImpl.Local<java.lang.String> implements Attribute.String.Local {

            private final java.lang.String value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = concept.getValueRes().getValue().getString();
            }

            @Override
            public final java.lang.String getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.String.Local asString() {
                return this;
            }
        }

        /**
         * Client implementation of Attribute.String
         */
        public static class Remote extends AttributeImpl.Remote<java.lang.String> implements Attribute.String.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final java.lang.String getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getString();
            }

            @Override
            public final AttributeImpl.String.Remote asString() {
                return this;
            }

            @Override
            public AttributeType.String.Remote getType() {
                return super.getType().asString();
            }

            @Override
            public Attribute.String.Remote setHas(Attribute attribute) {
                return super.setHas(attribute).asString();
            }
        }
    }

    public abstract static class DateTime implements Attribute.DateTime {

        private static LocalDateTime toLocalDateTime(long rpcDatetime) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(rpcDatetime), ZoneId.of("Z"));
        }

        /**
         * Client implementation of Attribute.DateTime
         */
        public static class Local extends AttributeImpl.Local<LocalDateTime> implements Attribute.DateTime.Local {

            private final LocalDateTime value;

            public Local(ConceptProto.Concept concept) {
                super(concept);
                this.value = toLocalDateTime(concept.getValueRes().getValue().getDatetime());
            }

            @Override
            public final LocalDateTime getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.DateTime.Local asDateTime() {
                return this;
            }
        }

        /**
         * Client implementation of Attribute.DateTime
         */
        public static class Remote extends AttributeImpl.Remote<LocalDateTime> implements Attribute.DateTime.Remote {

            public Remote(Transaction tx, ConceptIID iid) {
                super(tx, iid);
            }

            @Override
            public final LocalDateTime getValue() {
                final ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return toLocalDateTime(runMethod(method).getAttributeGetValueRes().getValue().getDatetime());
            }

            @Override
            public final AttributeImpl.DateTime.Remote asDateTime() {
                return this;
            }

            @Override
            public AttributeType.DateTime.Remote getType() {
                return super.getType().asDateTime();
            }

            @Override
            public Attribute.DateTime.Remote setHas(Attribute attribute) {
                return super.setHas(attribute).asDateTime();
            }
        }
    }
}
