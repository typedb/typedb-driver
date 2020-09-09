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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknException;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.protocol.ConceptProto;
import grakn.protocol.ConceptProto.Attribute.GetOwners;
import grakn.protocol.ConceptProto.ThingMethod;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Protocol.BAD_VALUE_TYPE;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.common.collection.Bytes.bytesToHexString;

public abstract class AttributeImpl {

    public abstract static class Local<VALUE> extends ThingImpl.Local implements Attribute.Local<VALUE> {

        Local(final java.lang.String iid) {
            super(iid);
        }

        public static AttributeImpl.Local<?> of(ConceptProto.Thing thingProto) {
            switch (thingProto.getValueType()) {
                case BOOLEAN:
                    return AttributeImpl.Boolean.Local.of(thingProto);
                case LONG:
                    return AttributeImpl.Long.Local.of(thingProto);
                case DOUBLE:
                    return AttributeImpl.Double.Local.of(thingProto);
                case STRING:
                    return AttributeImpl.String.Local.of(thingProto);
                case DATETIME:
                    return AttributeImpl.DateTime.Local.of(thingProto);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(BAD_VALUE_TYPE.message(thingProto.getValueType()));
            }
        }

        public abstract VALUE getValue();
    }

    public abstract static class Remote<VALUE> extends ThingImpl.Remote implements Attribute.Remote<VALUE> {

        Remote(final Grakn.Transaction transaction, final java.lang.String iid) {
            super(transaction, iid);
        }

        public static AttributeImpl.Remote<?> of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
            switch (thingProto.getValueType()) {
                case BOOLEAN:
                    return AttributeImpl.Boolean.Remote.of(transaction, thingProto);
                case LONG:
                    return AttributeImpl.Long.Remote.of(transaction, thingProto);
                case DOUBLE:
                    return AttributeImpl.Double.Remote.of(transaction, thingProto);
                case STRING:
                    return AttributeImpl.String.Remote.of(transaction, thingProto);
                case DATETIME:
                    return AttributeImpl.DateTime.Remote.of(transaction, thingProto);
                case UNRECOGNIZED:
                default:
                    throw new GraknException(BAD_VALUE_TYPE.message(thingProto.getValueType()));
            }
        }

        @Override
        public final Stream<? extends Thing.Local> getOwners() {
            return stream(
                    ThingMethod.Iter.Req.newBuilder().setAttributeGetOwnersIterReq(
                            GetOwners.Iter.Req.getDefaultInstance()).build(),
                    res -> res.getAttributeGetOwnersIterRes().getThing()
            );
        }

        @Override
        public Stream<? extends Thing.Local> getOwners(ThingType ownerType) {
            return stream(
                    ThingMethod.Iter.Req.newBuilder().setAttributeGetOwnersIterReq(
                            GetOwners.Iter.Req.newBuilder().setThingType(type(ownerType))).build(),
                    res -> res.getAttributeGetOwnersIterRes().getThing()
            );
        }

        @Override
        public AttributeType.Local getType() {
            return super.getType().asAttributeType();
        }

        public abstract VALUE getValue();
    }

    public abstract static class Boolean implements Attribute.Boolean {

        public static class Local extends AttributeImpl.Local<java.lang.Boolean> implements Attribute.Boolean.Local {

            private final java.lang.Boolean value;

            Local(final java.lang.String iid, final boolean value) {
                super(iid);
                this.value = value;
            }

            public static AttributeImpl.Boolean.Local of(final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Boolean.Local(
                        bytesToHexString(thingProto.getIid().toByteArray()),
                        thingProto.getValue().getBoolean()
                );
            }

            @Override
            public final java.lang.Boolean getValue() {
                return value;
            }

            @Override
            public final AttributeImpl.Boolean.Local asBoolean() {
                return this;
            }

            @Override
            public AttributeImpl.Boolean.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Boolean.Remote(transaction, getIID(), value);
            }
        }

        public static class Remote extends AttributeImpl.Remote<java.lang.Boolean> implements Attribute.Boolean.Remote {

            private final java.lang.Boolean value;

            Remote(final Grakn.Transaction transaction, final java.lang.String iid, final java.lang.Boolean value) {
                super(transaction, iid);
                this.value = value;
            }

            public static AttributeImpl.Boolean.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Boolean.Remote(transaction, bytesToHexString(thingProto.getIid().toByteArray()), thingProto.getValue().getBoolean());
            }

            @Override
            public Attribute.Boolean.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Boolean.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Boolean getValue() {
                return value;
            }

            @Override
            public AttributeType.Boolean.Local getType() {
                return super.getType().asBoolean();
            }
        }
    }

    public abstract static class Long implements Attribute.Long {

        public static class Local extends AttributeImpl.Local<java.lang.Long> implements Attribute.Long.Local {

            private final long value;

            Local(final java.lang.String iid, final long value) {
                super(iid);
                this.value = value;
            }

            public static AttributeImpl.Long.Local of(final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Long.Local(
                        bytesToHexString(thingProto.getIid().toByteArray()),
                        thingProto.getValue().getLong()
                );
            }

            @Override
            public AttributeImpl.Long.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Long.Remote(transaction, getIID(), value);
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

        public static class Remote extends AttributeImpl.Remote<java.lang.Long> implements Attribute.Long.Remote {

            private final long value;

            Remote(final Grakn.Transaction transaction, final java.lang.String iid, final long value) {
                super(transaction, iid);
                this.value = value;
            }

            public static AttributeImpl.Long.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Long.Remote(transaction, bytesToHexString(thingProto.getIid().toByteArray()), thingProto.getValue().getLong());
            }

            @Override
            public Attribute.Long.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Long.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Long getValue() {
                return value;
            }

            @Override
            public AttributeType.Long.Local getType() {
                return super.getType().asLong();
            }
        }
    }

    public abstract static class Double implements Attribute.Double {

        public static class Local extends AttributeImpl.Local<java.lang.Double> implements Attribute.Double.Local {

            private final double value;

            Local(final java.lang.String iid, final double value) {
                super(iid);
                this.value = value;
            }

            public static AttributeImpl.Double.Local of(final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Double.Local(
                        bytesToHexString(thingProto.getIid().toByteArray()),
                        thingProto.getValue().getDouble()
                );
            }

            @Override
            public AttributeImpl.Double.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Double.Remote(transaction, getIID(), value);
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

        public static class Remote extends AttributeImpl.Remote<java.lang.Double> implements Attribute.Double.Remote {

            private final double value;

            Remote(final Grakn.Transaction transaction, final java.lang.String iid, final double value) {
                super(transaction, iid);
                this.value = value;
            }

            public static AttributeImpl.Double.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
                return new AttributeImpl.Double.Remote(transaction, bytesToHexString(thingProto.getIid().toByteArray()), thingProto.getValue().getDouble());
            }

            @Override
            public Attribute.Double.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.Double.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Double getValue() {
                return value;
            }

            @Override
            public AttributeType.Double.Local getType() {
                return super.getType().asDouble();
            }
        }
    }

    public abstract static class String implements Attribute.String {

        public static class Local extends AttributeImpl.Local<java.lang.String> implements Attribute.String.Local {

            private final java.lang.String value;

            Local(final java.lang.String iid, final java.lang.String value) {
                super(iid);
                this.value = value;
            }

            public static AttributeImpl.String.Local of(final ConceptProto.Thing thingProto) {
                return new AttributeImpl.String.Local(
                        bytesToHexString(thingProto.getIid().toByteArray()),
                        thingProto.getValue().getString()
                );
            }

            @Override
            public AttributeImpl.String.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.String.Remote(transaction, getIID(), value);
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

        public static class Remote extends AttributeImpl.Remote<java.lang.String> implements Attribute.String.Remote {

            private final java.lang.String value;

            Remote(final Grakn.Transaction transaction, final java.lang.String iid, final java.lang.String value) {
                super(transaction, iid);
                this.value = value;
            }

            public static AttributeImpl.String.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
                return new AttributeImpl.String.Remote(transaction, bytesToHexString(thingProto.getIid().toByteArray()), thingProto.getValue().getString());
            }

            @Override
            public final java.lang.String getValue() {
                return value;
            }

            @Override
            public Attribute.String.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.String.Remote(transaction, getIID(), value);
            }

            @Override
            public AttributeType.String.Local getType() {
                return super.getType().asString();
            }
        }
    }

    public abstract static class DateTime implements Attribute.DateTime {

        private static LocalDateTime toLocalDateTime(long rpcDatetime) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(rpcDatetime), ZoneId.of("Z"));
        }

        public static class Local extends AttributeImpl.Local<LocalDateTime> implements Attribute.DateTime.Local {

            private final LocalDateTime value;

            Local(final java.lang.String iid, final LocalDateTime value) {
                super(iid);
                this.value = value;
            }

            public static AttributeImpl.DateTime.Local of(final ConceptProto.Thing thingProto) {
                return new AttributeImpl.DateTime.Local(
                        bytesToHexString(thingProto.getIid().toByteArray()),
                        toLocalDateTime(thingProto.getValue().getDatetime())
                );
            }

            @Override
            public AttributeImpl.DateTime.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeImpl.DateTime.Remote(transaction, getIID(), value);
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

        public static class Remote extends AttributeImpl.Remote<LocalDateTime> implements Attribute.DateTime.Remote {

            private final LocalDateTime value;

            Remote(final Grakn.Transaction transaction, final java.lang.String iid, final LocalDateTime value) {
                super(transaction, iid);
                this.value = value;
            }

            public static AttributeImpl.DateTime.Remote of(final Grakn.Transaction transaction, final ConceptProto.Thing thingProto) {
                return new AttributeImpl.DateTime.Remote(transaction, bytesToHexString(thingProto.getIid().toByteArray()), toLocalDateTime(thingProto.getValue().getDatetime()));
            }

            @Override
            public Attribute.DateTime.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeImpl.DateTime.Remote(transaction, getIID(), value);
            }

            @Override
            public final LocalDateTime getValue() {
                return value;
            }

            @Override
            public AttributeType.DateTime.Local getType() {
                return super.getType().asDateTime();
            }
        }
    }
}
