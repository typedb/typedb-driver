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
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.impl.AttributeTypeImpl;
import grakn.protocol.ConceptProto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_VALUE_TYPE;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.concept.proto.ConceptProtoBuilder.type;
import static grakn.common.collection.Bytes.bytesToHexString;
import static grakn.common.util.Objects.className;

public abstract class AttributeImpl<VALUE> extends ThingImpl implements Attribute<VALUE> {

    AttributeImpl(java.lang.String iid) {
        super(iid);
    }

    public static AttributeImpl<?> of(ConceptProto.Thing thingProto) {
        switch (thingProto.getType().getValueType()) {
            case BOOLEAN:
                return AttributeImpl.Boolean.of(thingProto);
            case LONG:
                return AttributeImpl.Long.of(thingProto);
            case DOUBLE:
                return AttributeImpl.Double.of(thingProto);
            case STRING:
                return AttributeImpl.String.of(thingProto);
            case DATETIME:
                return AttributeImpl.DateTime.of(thingProto);
            case UNRECOGNIZED:
            default:
                throw new GraknClientException(BAD_VALUE_TYPE.message(thingProto.getType().getValueType()));
        }
    }

    @Override
    public AttributeImpl<VALUE> asAttribute() {
        return this;
    }

    @Override
    public AttributeImpl.Boolean asBoolean() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Boolean.class)));
    }

    @Override
    public AttributeImpl.Long asLong() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Long.class)));
    }

    @Override
    public AttributeImpl.Double asDouble() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Double.class)));
    }

    @Override
    public AttributeImpl.String asString() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.String.class)));
    }

    @Override
    public AttributeImpl.DateTime asDateTime() {
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.DateTime.class)));
    }

    public abstract VALUE getValue();

    public abstract static class Remote<VALUE> extends ThingImpl.Remote implements Attribute.Remote<VALUE> {

        Remote(GraknClient.Transaction transaction, java.lang.String iid) {
            super(transaction, iid);
        }

        @Override
        public final Stream<ThingImpl> getOwners() {
            return thingStream(
                    ConceptProto.Thing.Req.newBuilder().setAttributeGetOwnersReq(
                            ConceptProto.Attribute.GetOwners.Req.getDefaultInstance()),
                    res -> res.getAttributeGetOwnersRes().getThingsList()
            );
        }

        @Override
        public Stream<ThingImpl> getOwners(ThingType ownerType) {
            return thingStream(
                    ConceptProto.Thing.Req.newBuilder().setAttributeGetOwnersReq(
                            ConceptProto.Attribute.GetOwners.Req.newBuilder().setThingType(type(ownerType))),
                    res -> res.getAttributeGetOwnersRes().getThingsList()
            );
        }

        @Override
        public AttributeTypeImpl getType() {
            return super.getType().asAttributeType();
        }

        @Override
        public AttributeImpl.Remote<VALUE> asAttribute() {
            return this;
        }

        @Override
        public AttributeImpl.Boolean.Remote asBoolean() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Boolean.class)));
        }

        @Override
        public AttributeImpl.Long.Remote asLong() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Long.class)));
        }

        @Override
        public AttributeImpl.Double.Remote asDouble() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.Double.class)));
        }

        @Override
        public AttributeImpl.String.Remote asString() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.String.class)));
        }

        @Override
        public AttributeImpl.DateTime.Remote asDateTime() {
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(className(this.getClass()), className(Attribute.DateTime.class)));
        }

        public abstract VALUE getValue();
    }

    public static class Boolean extends AttributeImpl<java.lang.Boolean> implements Attribute.Boolean {

        private final java.lang.Boolean value;

        Boolean(java.lang.String iid, boolean value) {
            super(iid);
            this.value = value;
        }

        public static AttributeImpl.Boolean of(ConceptProto.Thing thingProto) {
            return new AttributeImpl.Boolean(
                    bytesToHexString(thingProto.getIid().toByteArray()),
                    thingProto.getValue().getBoolean()
            );
        }

        @Override
        public final java.lang.Boolean getValue() {
            return value;
        }

        @Override
        public final AttributeImpl.Boolean asBoolean() {
            return this;
        }

        @Override
        public AttributeImpl.Boolean.Remote asRemote(GraknClient.Transaction transaction) {
            return new AttributeImpl.Boolean.Remote(transaction, getIID(), value);
        }

        public static class Remote extends AttributeImpl.Remote<java.lang.Boolean> implements Attribute.Boolean.Remote {

            private final java.lang.Boolean value;

            Remote(GraknClient.Transaction transaction, java.lang.String iid, java.lang.Boolean value) {
                super(transaction, iid);
                this.value = value;
            }

            @Override
            public Attribute.Boolean.Remote asRemote(GraknClient.Transaction transaction) {
                return new AttributeImpl.Boolean.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Boolean getValue() {
                return value;
            }

            @Override
            public AttributeTypeImpl.Boolean getType() {
                return super.getType().asBoolean();
            }

            @Override
            public final AttributeImpl.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    public static class Long extends AttributeImpl<java.lang.Long> implements Attribute.Long {

        private final long value;

        Long(java.lang.String iid, long value) {
            super(iid);
            this.value = value;
        }

        public static AttributeImpl.Long of(ConceptProto.Thing thingProto) {
            return new AttributeImpl.Long(
                    bytesToHexString(thingProto.getIid().toByteArray()),
                    thingProto.getValue().getLong()
            );
        }

        @Override
        public AttributeImpl.Long.Remote asRemote(GraknClient.Transaction transaction) {
            return new AttributeImpl.Long.Remote(transaction, getIID(), value);
        }

        @Override
        public final java.lang.Long getValue() {
            return value;
        }

        @Override
        public final AttributeImpl.Long asLong() {
            return this;
        }

        public static class Remote extends AttributeImpl.Remote<java.lang.Long> implements Attribute.Long.Remote {

            private final long value;

            Remote(GraknClient.Transaction transaction, java.lang.String iid, long value) {
                super(transaction, iid);
                this.value = value;
            }

            @Override
            public Attribute.Long.Remote asRemote(GraknClient.Transaction transaction) {
                return new AttributeImpl.Long.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Long getValue() {
                return value;
            }

            @Override
            public AttributeTypeImpl.Long getType() {
                return super.getType().asLong();
            }

            @Override
            public final AttributeImpl.Long.Remote asLong() {
                return this;
            }
        }
    }

    public static class Double extends AttributeImpl<java.lang.Double> implements Attribute.Double {

        private final double value;

        Double(java.lang.String iid, double value) {
            super(iid);
            this.value = value;
        }

        public static AttributeImpl.Double of(ConceptProto.Thing thingProto) {
            return new AttributeImpl.Double(
                    bytesToHexString(thingProto.getIid().toByteArray()),
                    thingProto.getValue().getDouble()
            );
        }

        @Override
        public AttributeImpl.Double.Remote asRemote(GraknClient.Transaction transaction) {
            return new AttributeImpl.Double.Remote(transaction, getIID(), value);
        }

        @Override
        public final java.lang.Double getValue() {
            return value;
        }

        @Override
        public final AttributeImpl.Double asDouble() {
            return this;
        }

        public static class Remote extends AttributeImpl.Remote<java.lang.Double> implements Attribute.Double.Remote {

            private final double value;

            Remote(GraknClient.Transaction transaction, java.lang.String iid, double value) {
                super(transaction, iid);
                this.value = value;
            }

            @Override
            public Attribute.Double.Remote asRemote(GraknClient.Transaction transaction) {
                return new AttributeImpl.Double.Remote(transaction, getIID(), value);
            }

            @Override
            public final java.lang.Double getValue() {
                return value;
            }

            @Override
            public AttributeTypeImpl.Double getType() {
                return super.getType().asDouble();
            }

            @Override
            public final AttributeImpl.Double.Remote asDouble() {
                return this;
            }
        }
    }

    public static class String extends AttributeImpl<java.lang.String> implements Attribute.String {

        private final java.lang.String value;

        String(java.lang.String iid, java.lang.String value) {
            super(iid);
            this.value = value;
        }

        public static AttributeImpl.String of(ConceptProto.Thing thingProto) {
            return new AttributeImpl.String(
                    bytesToHexString(thingProto.getIid().toByteArray()),
                    thingProto.getValue().getString()
            );
        }

        @Override
        public AttributeImpl.String.Remote asRemote(GraknClient.Transaction transaction) {
            return new AttributeImpl.String.Remote(transaction, getIID(), value);
        }

        @Override
        public final java.lang.String getValue() {
            return value;
        }

        @Override
        public final AttributeImpl.String asString() {
            return this;
        }

        public static class Remote extends AttributeImpl.Remote<java.lang.String> implements Attribute.String.Remote {

            private final java.lang.String value;

            Remote(GraknClient.Transaction transaction, java.lang.String iid, java.lang.String value) {
                super(transaction, iid);
                this.value = value;
            }

            @Override
            public final java.lang.String getValue() {
                return value;
            }

            @Override
            public Attribute.String.Remote asRemote(GraknClient.Transaction transaction) {
                return new AttributeImpl.String.Remote(transaction, getIID(), value);
            }

            @Override
            public AttributeTypeImpl.String getType() {
                return super.getType().asString();
            }

            @Override
            public final AttributeImpl.String.Remote asString() {
                return this;
            }
        }
    }

    public static class DateTime extends AttributeImpl<LocalDateTime> implements Attribute.DateTime {

        private final LocalDateTime value;

        DateTime(java.lang.String iid, LocalDateTime value) {
            super(iid);
            this.value = value;
        }

        public static AttributeImpl.DateTime of(ConceptProto.Thing thingProto) {
            return new AttributeImpl.DateTime(
                    bytesToHexString(thingProto.getIid().toByteArray()),
                    toLocalDateTime(thingProto.getValue().getDateTime())
            );
        }

        @Override
        public AttributeImpl.DateTime.Remote asRemote(GraknClient.Transaction transaction) {
            return new AttributeImpl.DateTime.Remote(transaction, getIID(), value);
        }

        @Override
        public final LocalDateTime getValue() {
            return value;
        }

        @Override
        public final AttributeImpl.DateTime asDateTime() {
            return this;
        }

        private static LocalDateTime toLocalDateTime(long rpcDatetime) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(rpcDatetime), ZoneId.of("Z"));
        }

        public static class Remote extends AttributeImpl.Remote<LocalDateTime> implements Attribute.DateTime.Remote {

            private final LocalDateTime value;

            Remote(GraknClient.Transaction transaction, java.lang.String iid, LocalDateTime value) {
                super(transaction, iid);
                this.value = value;
            }

            @Override
            public Attribute.DateTime.Remote asRemote(GraknClient.Transaction transaction) {
                return new AttributeImpl.DateTime.Remote(transaction, getIID(), value);
            }

            @Override
            public final LocalDateTime getValue() {
                return value;
            }

            @Override
            public AttributeTypeImpl.DateTime getType() {
                return super.getType().asDateTime();
            }

            @Override
            public final AttributeImpl.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
