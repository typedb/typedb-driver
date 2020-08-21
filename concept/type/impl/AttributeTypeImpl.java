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

package grakn.client.concept.type.impl;

import grakn.client.Grakn.Transaction;
import grakn.client.concept.rpc.ConceptMessage;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.Type;
import grakn.client.common.exception.GraknClientException;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class AttributeTypeImpl {

    /**
     * Client implementation of AttributeType
     */
    public abstract static class Local extends ThingTypeImpl.Local implements AttributeType.Local {

        public Local(ConceptProto.Type type) {
            super(type);
        }
    }

    /**
     * Client implementation of AttributeType
     */
    public abstract static class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        public Remote(Transaction tx, java.lang.String label) {
            super(tx, label);
        }

        @Override
        public AttributeType.Remote getSupertype() {
            return super.getSupertype().asAttributeType();
        }

        @Override
        public Stream<? extends AttributeType.Remote> getSupertypes() {
            return super.getSupertypes().map(Type.Remote::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeType.Remote> getSubtypes() {
            return super.getSubtypes().map(Type.Remote::asAttributeType);
        }

        @Override
        public Stream<? extends Attribute.Remote<?>> getInstances() {
            return super.getInstances().map(Thing.Remote::asAttribute);
        }

        protected final Attribute.Remote<?> put(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                            .setValue(ConceptMessage.attributeValue(value))).build();
            return Thing.Remote.of(tx(), runMethod(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        protected final Attribute.Remote<?> get(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                            .setValue(ConceptMessage.attributeValue(value))).build();
            ConceptProto.AttributeType.Get.Res response = runMethod(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case ATTRIBUTE:
                    return Thing.Remote.of(tx(), response.getAttribute()).asAttribute();
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        public final void setSupertype(AttributeType type) {
            super.setSupertype(type);
        }

        // TODO: can we delete this? In theory the value type should always be known
        /*@Override
        @Nullable
        public final ValueType getValueType() {
            ConceptProto.Method.Req method = ConceptProto.Method.Req.newBuilder()
                    .setAttributeTypeGetValueTypeReq(ConceptProto.AttributeType.GetValueType.Req.getDefaultInstance()).build();

            ConceptProto.AttributeType.GetValueType.Res response = runMethod(method).getAttributeTypeGetValueTypeRes();
            switch (response.getResCase()) {
                case NULL:
                    return null;
                case VALUETYPE:
                    return ConceptMessage.valueType(response.getValueType());
                default:
                    throw GraknClientException.unreachableStatement("Unexpected response " + response);
            }
        }*/

        @Override
        protected final Attribute.Remote<?> asInstance(Thing.Remote thing) {
            return thing.asAttribute();
        }
    }

    public static abstract class Boolean implements AttributeType.Boolean {

        /**
         * Client implementation of AttributeType.Boolean
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Boolean.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }
        }

        /**
         * Client implementation of AttributeType.Boolean
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.Boolean.Remote getSupertype() {
                return super.getSupertype().asBoolean();
            }

            @Override
            public final Stream<AttributeType.Boolean.Remote> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Remote::asBoolean);
            }

            @Override
            public final Stream<AttributeType.Boolean.Remote> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Remote::asBoolean);
            }

            @Override
            public final Stream<Attribute.Boolean.Remote> getInstances() {
                return super.getInstances().map(Attribute.Remote::asBoolean);
            }

            @Override
            public final void setSupertype(AttributeType.Boolean type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Boolean.Remote put(boolean value) {
                return super.put(value).asBoolean();
            }

            @Nullable
            @Override
            public final Attribute.Boolean.Remote get(boolean value) {
                final Attribute.Remote<?> attr = super.get(value);
                return attr != null ? attr.asBoolean() : null;
            }
        }
    }

    public static abstract class Long implements AttributeType.Long {

        /**
         * Client implementation of AttributeType.Long
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Long.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }
        }

        /**
         * Client implementation of AttributeType.Long
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.Long.Remote getSupertype() {
                return super.getSupertype().asLong();
            }

            @Override
            public final Stream<AttributeType.Long.Remote> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Remote::asLong);
            }

            @Override
            public final Stream<AttributeType.Long.Remote> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Remote::asLong);
            }

            @Override
            public final Stream<Attribute.Long.Remote> getInstances() {
                return super.getInstances().map(Attribute.Remote::asLong);
            }

            @Override
            public final void setSupertype(AttributeType.Long type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Long.Remote put(long value) {
                return super.put(value).asLong();
            }

            @Nullable
            @Override
            public final Attribute.Long.Remote get(long value) {
                final Attribute.Remote<?> attr = super.get(value);
                return attr != null ? attr.asLong() : null;
            }
        }
    }

    public static abstract class Double implements AttributeType.Double {

        /**
         * Client implementation of AttributeType.Double
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Double.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }
        }

        /**
         * Client implementation of AttributeType.Double
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.Double.Remote getSupertype() {
                return super.getSupertype().asDouble();
            }

            @Override
            public final Stream<AttributeType.Double.Remote> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Remote::asDouble);
            }

            @Override
            public final Stream<AttributeType.Double.Remote> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Remote::asDouble);
            }

            @Override
            public final Stream<Attribute.Double.Remote> getInstances() {
                return super.getInstances().map(Attribute.Remote::asDouble);
            }

            @Override
            public final void setSupertype(AttributeType.Double type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Double.Remote put(double value) {
                return super.put(value).asDouble();
            }

            @Nullable
            @Override
            public final Attribute.Double.Remote get(double value) {
                final Attribute.Remote<?> attr = super.get(value);
                return attr != null ? attr.asDouble() : null;
            }
        }
    }

    public static abstract class String implements AttributeType.String {

        /**
         * Client implementation of AttributeType.String
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.String.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }
        }

        /**
         * Client implementation of AttributeType.Double
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.String.Remote getSupertype() {
                return super.getSupertype().asString();
            }

            @Override
            public final Stream<AttributeType.String.Remote> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Remote::asString);
            }

            @Override
            public final Stream<AttributeType.String.Remote> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Remote::asString);
            }

            @Override
            public final Stream<Attribute.String.Remote> getInstances() {
                return super.getInstances().map(Attribute.Remote::asString);
            }

            @Override
            public final void setSupertype(AttributeType.String type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.String.Remote put(java.lang.String value) {
                return super.put(value).asString();
            }

            @Nullable
            @Override
            public final Attribute.String.Remote get(java.lang.String value) {
                final Attribute.Remote<?> attr = super.get(value);
                return attr != null ? attr.asString() : null;
            }

            @Nullable
            @Override
            public final java.lang.String getRegex() {
                final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                        .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();
                final java.lang.String regex = runMethod(method).getAttributeTypeGetRegexRes().getRegex();
                return regex.isEmpty() ? null : regex;
            }

            @Override
            public final void setRegex(java.lang.String regex) {
                if (regex == null) regex = "";
                final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                        .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                .setRegex(regex)).build();
                runMethod(method);
            }
        }
    }

    public static abstract class DateTime implements AttributeType.DateTime {

        /**
         * Client implementation of AttributeType.DateTime
         */
        public static class Local extends AttributeTypeImpl.Local implements AttributeType.DateTime.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }
        }

        /**
         * Client implementation of AttributeType.DateTime
         */
        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            public Remote(Transaction tx, java.lang.String label) {
                super(tx, label);
            }

            @Override
            public AttributeType.DateTime.Remote getSupertype() {
                return super.getSupertype().asDateTime();
            }

            @Override
            public final Stream<AttributeType.DateTime.Remote> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Remote::asDateTime);
            }

            @Override
            public final Stream<AttributeType.DateTime.Remote> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Remote::asDateTime);
            }

            @Override
            public final Stream<Attribute.DateTime.Remote> getInstances() {
                return super.getInstances().map(Attribute.Remote::asDateTime);
            }

            @Override
            public final void setSupertype(AttributeType.DateTime type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.DateTime.Remote put(LocalDateTime value) {
                return super.put(value).asDateTime();
            }

            @Nullable
            @Override
            public final Attribute.DateTime.Remote get(LocalDateTime value) {
                final Attribute.Remote<?> attr = super.get(value);
                return attr != null ? attr.asDateTime() : null;
            }
        }
    }
}
