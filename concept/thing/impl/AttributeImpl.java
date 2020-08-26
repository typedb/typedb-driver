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

import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import static grakn.client.concept.ConceptMessageWriter.type;

public abstract class AttributeImpl {

    /**
     * Client implementation of Attribute
     *
     * @param <VALUE> The value type of this attribute
     */
    public abstract static class Local<VALUE> extends ThingImpl.Local implements Attribute.Local<VALUE> {

        public Local(final ConceptProto.Thing thing) {
            super(thing);
        }

        @Override
        public AttributeType.Local getType() {
            return super.getType().asAttributeType();
        }

        public abstract VALUE getValue();
    }

    /**
     * Client implementation of Attribute
     *
     * @param <VALUE> The value type of this attribute
     */
    public abstract static class Remote<VALUE> extends ThingImpl.Remote implements Attribute.Remote<VALUE> {

        public Remote(final Concepts concepts, final java.lang.String iid) {
            super(concepts, iid);
        }

        @Override
        public final Stream<Thing.Remote> getOwners() {
            ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setAttributeGetOwnersIterReq(ConceptProto.Attribute.GetOwners.Iter.Req.getDefaultInstance()).build();
            return thingStream(method, res -> res.getAttributeGetOwnersIterRes().getThing());
        }

        @Override
        public Stream<? extends Thing.Remote> getOwners(ThingType ownerType) {
            ConceptProto.ThingMethod.Iter.Req method = ConceptProto.ThingMethod.Iter.Req.newBuilder()
                    .setAttributeGetOwnersIterReq(ConceptProto.Attribute.GetOwners.Iter.Req.newBuilder()
                            .setThingType(type(ownerType))).build();
            return thingStream(method, res -> res.getAttributeGetOwnersIterRes().getThing());
        }

        @Override
        public AttributeType.Remote getType() {
            return (AttributeType.Remote) super.getType();
        }

        public abstract VALUE getValue();
    }

    public abstract static class Boolean implements Attribute.Boolean {
        /**
         * Client implementation of Attribute.Boolean
         */
        public static class Local extends AttributeImpl.Local<java.lang.Boolean> implements Attribute.Boolean.Local {

            private final java.lang.Boolean value;

            public Local(final ConceptProto.Thing thing) {
                super(thing);
                this.value = thing.getValue().getBoolean();
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

            public Remote(final Concepts concepts, final java.lang.String iid) {
                super(concepts, iid);
            }

            @Override
            public final java.lang.Boolean getValue() {
                final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getBoolean();
            }

            @Override
            public AttributeType.Boolean.Remote getType() {
                return super.getType().asBoolean();
            }
        }
    }

    public abstract static class Long implements Attribute.Long {
        /**
         * Client implementation of Attribute.Long
         */
        public static class Local extends AttributeImpl.Local<java.lang.Long> implements Attribute.Long.Local {

            private final long value;

            public Local(final ConceptProto.Thing thing) {
                super(thing);
                this.value = thing.getValue().getLong();
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

            public Remote(final Concepts concepts, final java.lang.String iid) {
                super(concepts, iid);
            }

            @Override
            public final java.lang.Long getValue() {
                final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getLong();
            }

            @Override
            public AttributeType.Long.Remote getType() {
                return super.getType().asLong();
            }
        }
    }

    public abstract static class Double implements Attribute.Double {
        /**
         * Client implementation of Attribute.Double
         */
        public static class Local extends AttributeImpl.Local<java.lang.Double> implements Attribute.Double.Local {

            private final double value;

            public Local(final ConceptProto.Thing thing) {
                super(thing);
                this.value = thing.getValue().getDouble();
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

            public Remote(final Concepts concepts, final java.lang.String iid) {
                super(concepts, iid);
            }

            @Override
            public final java.lang.Double getValue() {
                final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getDouble();
            }

            @Override
            public AttributeType.Double.Remote getType() {
                return super.getType().asDouble();
            }
        }
    }

    public abstract static class String implements Attribute.String {
        /**
         * Client implementation of Attribute.String
         */
        public static class Local extends AttributeImpl.Local<java.lang.String> implements Attribute.String.Local {

            private final java.lang.String value;

            public Local(final ConceptProto.Thing thing) {
                super(thing);
                this.value = thing.getValue().getString();
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

            public Remote(final Concepts concepts, final java.lang.String iid) {
                super(concepts, iid);
            }

            @Override
            public final java.lang.String getValue() {
                final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return runMethod(method).getAttributeGetValueRes().getValue().getString();
            }

            @Override
            public AttributeType.String.Remote getType() {
                return super.getType().asString();
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

            public Local(final ConceptProto.Thing thing) {
                super(thing);
                this.value = toLocalDateTime(thing.getValue().getDatetime());
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

            public Remote(final Concepts concepts, final java.lang.String iid) {
                super(concepts, iid);
            }

            @Override
            public final LocalDateTime getValue() {
                final ConceptProto.ThingMethod.Req method = ConceptProto.ThingMethod.Req.newBuilder()
                        .setAttributeGetValueReq(ConceptProto.Attribute.GetValue.Req.getDefaultInstance()).build();

                return toLocalDateTime(runMethod(method).getAttributeGetValueRes().getValue().getDatetime());
            }

            @Override
            public AttributeType.DateTime.Remote getType() {
                return super.getType().asDateTime();
            }
        }
    }
}
