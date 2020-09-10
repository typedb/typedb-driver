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
import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.BAD_VALUE_TYPE;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.concept.proto.ConceptProtoBuilder.attributeValue;
import static grakn.common.util.Objects.className;

public abstract class AttributeTypeImpl {

    private static final java.lang.String ROOT_LABEL = "attribute";

    public static class Local extends ThingTypeImpl.Local implements AttributeType.Local {

        Local(final java.lang.String label, final boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.Local of(ConceptProto.Type type) {
            switch (type.getValueType()) {
                case BOOLEAN:
                    return new AttributeTypeImpl.Boolean.Local(type.getLabel(), type.getRoot());
                case LONG:
                    return new AttributeTypeImpl.Long.Local(type.getLabel(), type.getRoot());
                case DOUBLE:
                    return new AttributeTypeImpl.Double.Local(type.getLabel(), type.getRoot());
                case STRING:
                    return new AttributeTypeImpl.String.Local(type.getLabel(), type.getRoot());
                case DATETIME:
                    return new AttributeTypeImpl.DateTime.Local(type.getLabel(), type.getRoot());
                case OBJECT:
                    assert type.getRoot();
                    return new AttributeTypeImpl.Local(type.getLabel(), type.getRoot());
                case UNRECOGNIZED:
                default:
                    throw new GraknClientException(BAD_VALUE_TYPE.message(type.getValueType()));
            }
        }

        @Override
        public ValueType getValueType() {
            return ValueType.OBJECT;
        }

        @Override
        public final boolean isKeyable() {
            return getValueType().isKeyable();
        }

        @Override
        public AttributeTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.Local asAttributeType() {
            return this;
        }

        @Override
        public AttributeTypeImpl.Boolean.Local asBoolean() {
            if (isRoot()) {
                return new AttributeTypeImpl.Boolean.Local(ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Boolean.class)));
        }

        @Override
        public AttributeTypeImpl.Long.Local asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Local(ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Long.class)));
        }

        @Override
        public AttributeTypeImpl.Double.Local asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Local(ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Double.class)));
        }

        @Override
        public AttributeTypeImpl.String.Local asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Local(ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.String.class)));
        }

        @Override
        public AttributeTypeImpl.DateTime.Local asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Local(ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.DateTime.class)));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeTypeImpl.Local)) return false;
            // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
            // because it is possible to compare a attribute root types wrapped in different type classes
            // such as: root type wrapped in AttributeTypeImpl.Root and as in AttributeType.Boolean.Root
            // We only override equals(), but not hash(), in this class, as hash() the logic from TypeImpl still applies.

            final AttributeTypeImpl.Local that = (AttributeTypeImpl.Local) o;
            return this.getLabel().equals(that.getLabel());
        }
    }

    public static class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
            super(transaction, label, isRoot);
        }

        public static AttributeTypeImpl.Remote of(Grakn.Transaction transaction, ConceptProto.Type typeProto) {
            switch (typeProto.getValueType()) {
                case BOOLEAN:
                    return new AttributeTypeImpl.Boolean.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case LONG:
                    return new AttributeTypeImpl.Long.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case DOUBLE:
                    return new AttributeTypeImpl.Double.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case STRING:
                    return new AttributeTypeImpl.String.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case DATETIME:
                    return new AttributeTypeImpl.DateTime.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case OBJECT:
                    assert typeProto.getRoot();
                    return new AttributeTypeImpl.Remote(transaction, typeProto.getLabel(), typeProto.getRoot());
                case UNRECOGNIZED:
                default:
                    throw new GraknClientException(BAD_VALUE_TYPE.message(typeProto.getValueType()));
            }
        }

        @Override
        public ValueType getValueType() {
            return ValueType.OBJECT;
        }

        @Override
        public final boolean isKeyable() {
            return getValueType().isKeyable();
        }

        @Override
        public AttributeTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        public final void setSupertype(final AttributeType type) {
            this.setSupertypeExecute(type);
        }

        @Nullable
        @Override
        public AttributeTypeImpl.Local getSupertype() {
            return getSupertypeExecute(TypeImpl.Local::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeTypeImpl.Local> getSupertypes() {
            return super.getSupertypes().map(TypeImpl.Local::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeTypeImpl.Local> getSubtypes() {
            final Stream<AttributeTypeImpl.Local> stream = super.getSubtypes().map(TypeImpl.Local::asAttributeType);

            if (isRoot() && getValueType() != ValueType.OBJECT) {
                // Get all attribute types of this value type
                return stream.filter(x -> x.getValueType() == this.getValueType() || x.getLabel().equals(this.getLabel()));
            }

            return stream;
        }

        @Override
        public Stream<? extends AttributeImpl.Local<?>> getInstances() {
            return super.getInstances(AttributeImpl.Local::of);
        }

        @Override
        public Stream<ThingTypeImpl.Local> getOwners() {
            return getOwners(false);
        }

        @Override
        public Stream<ThingTypeImpl.Local> getOwners(final boolean onlyKey) {
            final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                    .setAttributeTypeGetOwnersIterReq(ConceptProto.AttributeType.GetOwners.Iter.Req.newBuilder()
                            .setOnlyKey(onlyKey)).build();

            return stream(method, res -> res.getAttributeTypeGetOwnersIterRes().getOwner()).map(TypeImpl.Local::asThingType);
        }

        protected final AttributeImpl.Local<?> put(final Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                            .setValue(attributeValue(value))).build();
            return ThingImpl.Local.of(execute(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        protected final AttributeImpl.Local<?> get(final Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                            .setValue(attributeValue(value))).build();
            final ConceptProto.AttributeType.Get.Res response = execute(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case ATTRIBUTE:
                    return ThingImpl.Local.of(response.getAttribute()).asAttribute();
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public AttributeTypeImpl.Remote asAttributeType() {
            return this;
        }

        @Override
        public AttributeTypeImpl.Boolean.Remote asBoolean() {
            if (isRoot()) {
                return new AttributeTypeImpl.Boolean.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Boolean.class)));
        }

        @Override
        public AttributeTypeImpl.Long.Remote asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Long.class)));
        }

        @Override
        public AttributeTypeImpl.Double.Remote asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Double.class)));
        }

        @Override
        public AttributeTypeImpl.String.Remote asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.String.class)));
        }

        @Override
        public AttributeTypeImpl.DateTime.Remote asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.DateTime.class)));
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeTypeImpl.Remote)) return false;
            // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
            // because it is possible to compare a attribute root types wrapped in different type classes
            // such as: root type wrapped in AttributeTypeImpl.Root and as in AttributeType.Boolean.Root
            // We only override equals(), but not hash(), in this class, as hash() the logic from TypeImpl still applies.

            final AttributeTypeImpl.Remote that = (AttributeTypeImpl.Remote) o;
            return (this.tx().equals(that.tx()) && this.getLabel().equals(that.getLabel()));
        }
    }

    public static abstract class Boolean implements AttributeType.Boolean {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Boolean.Local {

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.BOOLEAN;
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public AttributeTypeImpl.Boolean.Local asBoolean() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.BOOLEAN;
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final AttributeTypeImpl.Boolean.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asBoolean());
            }

            @Override
            public final Stream<AttributeTypeImpl.Boolean.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeTypeImpl.Local::asBoolean);
            }

            @Override
            public final Stream<AttributeTypeImpl.Boolean.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl.Local::asBoolean);
            }

            @Override
            public final Stream<AttributeImpl.Boolean.Local> getInstances() {
                return super.getInstances().map(AttributeImpl.Local::asBoolean);
            }

            @Override
            public final void setSupertype(final AttributeType.Boolean type) {
                super.setSupertype(type);
            }

            @Override
            public final AttributeImpl.Boolean.Local put(final boolean value) {
                return super.put(value).asBoolean();
            }

            @Nullable
            @Override
            public final AttributeImpl.Boolean.Local get(final boolean value) {
                final AttributeImpl.Local<?> attr = super.get(value);
                return attr != null ? attr.asBoolean() : null;
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    public static abstract class Long implements AttributeType.Long {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Long.Local {

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.LONG;
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public AttributeTypeImpl.Long.Local asLong() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.LONG;
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final AttributeTypeImpl.Long.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asLong());
            }

            @Override
            public final Stream<AttributeTypeImpl.Long.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeTypeImpl.Local::asLong);
            }

            @Override
            public final Stream<AttributeTypeImpl.Long.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl.Local::asLong);
            }

            @Override
            public final Stream<AttributeImpl.Long.Local> getInstances() {
                return super.getInstances().map(AttributeImpl.Local::asLong);
            }

            @Override
            public final void setSupertype(final AttributeType.Long type) {
                super.setSupertype(type);
            }

            @Override
            public final AttributeImpl.Long.Local put(final long value) {
                return super.put(value).asLong();
            }

            @Nullable
            @Override
            public final AttributeImpl.Long.Local get(final long value) {
                final AttributeImpl.Local<?> attr = super.get(value);
                return attr != null ? attr.asLong() : null;
            }

            @Override
            public AttributeTypeImpl.Long.Remote asLong() {
                return this;
            }
        }
    }

    public static abstract class Double implements AttributeType.Double {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.Double.Local {

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DOUBLE;
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public AttributeTypeImpl.Double.Local asDouble() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DOUBLE;
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final AttributeTypeImpl.Double.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asDouble());
            }

            @Override
            public final Stream<AttributeTypeImpl.Double.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeTypeImpl.Local::asDouble);
            }

            @Override
            public final Stream<AttributeTypeImpl.Double.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl.Local::asDouble);
            }

            @Override
            public final Stream<AttributeImpl.Double.Local> getInstances() {
                return super.getInstances().map(AttributeImpl.Local::asDouble);
            }

            @Override
            public final void setSupertype(final AttributeType.Double type) {
                super.setSupertype(type);
            }

            @Override
            public final AttributeImpl.Double.Local put(final double value) {
                return super.put(value).asDouble();
            }

            @Nullable
            @Override
            public final AttributeImpl.Double.Local get(final double value) {
                final AttributeImpl.Local<?> attr = super.get(value);
                return attr != null ? attr.asDouble() : null;
            }

            @Override
            public AttributeTypeImpl.Double.Remote asDouble() {
                return this;
            }
        }
    }

    public static abstract class String implements AttributeType.String {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.String.Local {

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.STRING;
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public AttributeTypeImpl.String.Local asString() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.STRING;
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final AttributeTypeImpl.String.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asString());
            }

            @Override
            public final Stream<AttributeTypeImpl.String.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeTypeImpl.Local::asString);
            }

            @Override
            public final Stream<AttributeTypeImpl.String.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl.Local::asString);
            }

            @Override
            public final Stream<AttributeImpl.String.Local> getInstances() {
                return super.getInstances().map(AttributeImpl.Local::asString);
            }

            @Override
            public final void setSupertype(final AttributeType.String type) {
                super.setSupertype(type);
            }

            @Override
            public final AttributeImpl.String.Local put(final java.lang.String value) {
                return super.put(value).asString();
            }

            @Nullable
            @Override
            public final AttributeImpl.String.Local get(final java.lang.String value) {
                final AttributeImpl.Local<?> attr = super.get(value);
                return attr != null ? attr.asString() : null;
            }

            @Nullable
            @Override
            public final java.lang.String getRegex() {
                final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                        .setAttributeTypeGetRegexReq(ConceptProto.AttributeType.GetRegex.Req.getDefaultInstance()).build();
                final java.lang.String regex = execute(method).getAttributeTypeGetRegexRes().getRegex();
                return regex.isEmpty() ? null : regex;
            }

            @Override
            public final void setRegex(java.lang.String regex) {
                if (regex == null) regex = "";
                final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                        .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                .setRegex(regex)).build();
                execute(method);
            }

            @Override
            public AttributeTypeImpl.String.Remote asString() {
                return this;
            }
        }
    }

    public static abstract class DateTime implements AttributeType.DateTime {

        public static class Local extends AttributeTypeImpl.Local implements AttributeType.DateTime.Local {

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DATETIME;
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public AttributeTypeImpl.DateTime.Local asDateTime() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DATETIME;
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final AttributeTypeImpl.DateTime.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asDateTime());
            }

            @Override
            public final Stream<AttributeTypeImpl.DateTime.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeTypeImpl.Local::asDateTime);
            }

            @Override
            public final Stream<AttributeTypeImpl.DateTime.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl.Local::asDateTime);
            }

            @Override
            public final Stream<AttributeImpl.DateTime.Local> getInstances() {
                return super.getInstances().map(AttributeImpl.Local::asDateTime);
            }

            @Override
            public final void setSupertype(final AttributeType.DateTime type) {
                super.setSupertype(type);
            }

            @Override
            public final AttributeImpl.DateTime.Local put(final LocalDateTime value) {
                return super.put(value).asDateTime();
            }

            @Nullable
            @Override
            public final AttributeImpl.DateTime.Local get(final LocalDateTime value) {
                final AttributeImpl.Local<?> attr = super.get(value);
                return attr != null ? attr.asDateTime() : null;
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
