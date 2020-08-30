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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknException;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Protocol.ILLEGAL_COMBINATION_OF_FIELDS;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;
import static grakn.client.concept.proto.ConceptProtoBuilder.attributeValue;

public abstract class AttributeTypeImpl {

    private static final java.lang.String ROOT_LABEL = "attribute";

    public static class Local extends ThingTypeImpl.Local implements AttributeType.Local {

        private Local(final ConceptProto.Type type) {
            super(type);
        }

        public static AttributeTypeImpl.Local of(ConceptProto.Type type) {
            switch (type.getValueType()) {
                case BOOLEAN:
                    return new AttributeTypeImpl.Boolean.Local(type);
                case LONG:
                    return new AttributeTypeImpl.Long.Local(type);
                case DOUBLE:
                    return new AttributeTypeImpl.Double.Local(type);
                case STRING:
                    return new AttributeTypeImpl.String.Local(type);
                case DATETIME:
                    return new AttributeTypeImpl.DateTime.Local(type);
                case OBJECT:
                    if (type.getRoot()) {
                        return new AttributeTypeImpl.Local(type);
                    } else {
                        throw new GraknException(ILLEGAL_COMBINATION_OF_FIELDS.message(
                                "valueType", ConceptProto.AttributeType.VALUE_TYPE.OBJECT,
                                "root", false));
                    }
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(
                            ConceptProto.AttributeType.VALUE_TYPE.class.getSimpleName(), type.getValueType())
                    );
            }
        }

        @Override
        public AttributeTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, getLabel(), isRoot());
        }
    }

    public static class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        public static AttributeTypeImpl.Remote of(Grakn.Transaction transaction, ConceptProto.Type type, java.lang.String label) {
            switch (type.getValueType()) {
                case BOOLEAN:
                    return new AttributeTypeImpl.Boolean.Remote(transaction, label, type.getRoot());
                case LONG:
                    return new AttributeTypeImpl.Long.Remote(transaction, label, type.getRoot());
                case DOUBLE:
                    return new AttributeTypeImpl.Double.Remote(transaction, label, type.getRoot());
                case STRING:
                    return new AttributeTypeImpl.String.Remote(transaction, label, type.getRoot());
                case DATETIME:
                    return new AttributeTypeImpl.DateTime.Remote(transaction, label, type.getRoot());
                case OBJECT:
                    if (type.getRoot()) {
                        return new AttributeTypeImpl.Remote(transaction, label, true);
                    } else {
                        throw new GraknException(ILLEGAL_COMBINATION_OF_FIELDS.message(
                                "valueType", ConceptProto.AttributeType.VALUE_TYPE.OBJECT,
                                "root", false));
                    }
                case UNRECOGNIZED:
                default:
                    throw new GraknException(UNRECOGNISED_FIELD.message(
                            ConceptProto.AttributeType.VALUE_TYPE.class.getSimpleName(), type.getValueType())
                    );
            }
        }

        @Nullable
        @Override
        public AttributeType.Remote getSupertype() {
            return getSupertypeInternal(Type.Remote::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeType.Remote> getSupertypes() {
            return super.getSupertypes().map(Type.Remote::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeType.Remote> getSubtypes() {
            final Stream<? extends AttributeType.Remote> stream = super.getSubtypes().map(Type.Remote::asAttributeType);

            if (isRoot() && getValueType() != ValueType.OBJECT) {
                // Get all attribute types of this value type
                return stream.filter(x -> x.getValueType() == this.getValueType() || x.getLabel().equals(this.getLabel()));
            }

            return stream;
        }

        @Override
        public Stream<? extends Attribute.Remote<?>> getInstances() {
            return super.getInstances().map(Thing.Remote::asAttribute);
        }

        @Override
        public Stream<? extends ThingType> getOwners() {
            return getOwners(false);
        }

        @Override
        public Stream<? extends ThingType> getOwners(boolean onlyKey) {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setAttributeTypeGetOwnersIterReq(ConceptProto.AttributeType.GetOwners.Iter.Req.newBuilder()
                                                              .setOnlyKey(onlyKey)).build();

            return typeStream(method, res -> res.getAttributeTypeGetOwnersIterRes().getOwner()).map(Type.Remote::asThingType);
        }

        protected final Attribute.Remote<?> put(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                                                    .setValue(attributeValue(value))).build();
            return ThingImpl.Remote.of(tx(), runMethod(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        protected final Attribute.Remote<?> get(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                                                    .setValue(attributeValue(value))).build();
            ConceptProto.AttributeType.Get.Res response = runMethod(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case ATTRIBUTE:
                    return ThingImpl.Remote.of(tx(), response.getAttribute()).asAttribute();
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        public final void setSupertype(AttributeType type) {
            setSupertypeInternal(type);
        }

        @Override
        public AttributeType.Boolean.Remote asBoolean() {
            if (isRoot()) {
                return new AttributeTypeImpl.Boolean.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Boolean.class.getCanonicalName()));
        }

        @Override
        public AttributeType.Long.Remote asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Long.class.getCanonicalName()));
        }

        @Override
        public AttributeType.Double.Remote asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Double.class.getCanonicalName()));
        }

        @Override
        public AttributeType.String.Remote asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.String.class.getCanonicalName()));
        }

        @Override
        public AttributeType.DateTime.Remote asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.DateTime.class.getCanonicalName()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeTypeImpl.Remote)) return false;
            // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
            // because it is possible to compare a attribute root types wrapped in different type classes
            // such as: root type wrapped in AttributeTypeImpl.Root and as in AttributeType.Boolean.Root
            // We only override equals(), but not hash(), in this class, as hash() the logic from TypeImpl still applies.

            AttributeTypeImpl.Remote that = (AttributeTypeImpl.Remote) o;
            return (this.tx().equals(that.tx()) && this.getLabel().equals(that.getLabel()));
        }
    }

    public static abstract class Boolean implements AttributeType.Boolean {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Boolean.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, getLabel(), isRoot());
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Boolean.Remote getSupertype() {
                return getSupertypeInternal(t -> t.asAttributeType().asBoolean());
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

            @Override
            public AttributeType.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    public static abstract class Long implements AttributeType.Long {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Long.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, getLabel(), isRoot());
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Long.Remote getSupertype() {
                return getSupertypeInternal(t -> t.asAttributeType().asLong());
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

            @Override
            public AttributeType.Long.Remote asLong() {
                return this;
            }
        }
    }

    public static abstract class Double implements AttributeType.Double {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Double.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, getLabel(), isRoot());
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Double.Remote getSupertype() {
                return getSupertypeInternal(t -> t.asAttributeType().asDouble());
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

            @Override
            public AttributeType.Double.Remote asDouble() {
                return this;
            }
        }
    }

    public static abstract class String implements AttributeType.String {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.String.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, getLabel(), isRoot());
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.String.Remote getSupertype() {
                return getSupertypeInternal(t -> t.asAttributeType().asString());
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

            @Override
            public AttributeType.String.Remote asString() {
                return this;
            }
        }
    }

    public static abstract class DateTime implements AttributeType.DateTime {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.DateTime.Local {

            public Local(ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, getLabel(), isRoot());
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.DateTime.Remote getSupertype() {
                return getSupertypeInternal(t -> t.asAttributeType().asDateTime());
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

            @Override
            public AttributeType.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
