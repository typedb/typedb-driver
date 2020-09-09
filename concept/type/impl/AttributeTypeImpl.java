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
import grakn.client.concept.thing.impl.AttributeImpl;
import grakn.client.concept.thing.impl.ThingImpl;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Protocol.BAD_VALUE_TYPE;
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
                    throw new GraknException(BAD_VALUE_TYPE.message(type.getValueType()));
            }
        }

        @Override
        public AttributeTypeImpl.Remote asRemote(final Grakn.Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, label, isRoot);
        }

        @Override
        public AttributeType.Boolean.Local asBoolean() {
            if (isRoot()) {
                return new AttributeTypeImpl.Boolean.Local(ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Boolean.class)));
        }

        @Override
        public AttributeType.Long.Local asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Local(ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Long.class)));
        }

        @Override
        public AttributeType.Double.Local asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Local(ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Double.class)));
        }

        @Override
        public AttributeType.String.Local asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Local(ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.String.class)));
        }

        @Override
        public AttributeType.DateTime.Local asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Local(ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.DateTime.class)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeTypeImpl.Local)) return false;
            // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
            // because it is possible to compare a attribute root types wrapped in different type classes
            // such as: root type wrapped in AttributeTypeImpl.Root and as in AttributeType.Boolean.Root
            // We only override equals(), but not hash(), in this class, as hash() the logic from TypeImpl still applies.

            AttributeTypeImpl.Local that = (AttributeTypeImpl.Local) o;
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
                    throw new GraknException(BAD_VALUE_TYPE.message(typeProto.getValueType()));
            }
        }

        @Override
        public AttributeTypeImpl.Remote asRemote(Grakn.Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, label, isRoot);
        }

        public final void setSupertype(AttributeType type) {
            this.setSupertypeExecute(type);
        }

        @Nullable
        @Override
        public AttributeType.Local getSupertype() {
            return getSupertypeExecute(Type.Local::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeType.Local> getSupertypes() {
            return super.getSupertypes().map(Type.Local::asAttributeType);
        }

        @Override
        public Stream<? extends AttributeType.Local> getSubtypes() {
            final Stream<? extends AttributeType.Local> stream = super.getSubtypes().map(Type.Local::asAttributeType);

            if (isRoot() && getValueType() != ValueType.OBJECT) {
                // Get all attribute types of this value type
                return stream.filter(x -> x.getValueType() == this.getValueType() || x.getLabel().equals(this.getLabel()));
            }

            return stream;
        }

        @Override
        public Stream<? extends Attribute.Local<?>> getInstances() {
            return super.getInstances(AttributeImpl.Local::of);
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

            return stream(method, res -> res.getAttributeTypeGetOwnersIterRes().getOwner()).map(Type.Local::asThingType);
        }

        protected final Attribute.Local<?> put(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypePutReq(ConceptProto.AttributeType.Put.Req.newBuilder()
                                                    .setValue(attributeValue(value))).build();
            return ThingImpl.Local.of(execute(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        protected final Attribute.Local<?> get(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                                                    .setValue(attributeValue(value))).build();
            ConceptProto.AttributeType.Get.Res response = execute(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case ATTRIBUTE:
                    return ThingImpl.Local.of(response.getAttribute()).asAttribute();
                default:
                case RES_NOT_SET:
                    return null;
            }
        }

        @Override
        public AttributeType.Boolean.Remote asBoolean() {
            if (isRoot()) {
                return new AttributeTypeImpl.Boolean.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Boolean.class)));
        }

        @Override
        public AttributeType.Long.Remote asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Long.class)));
        }

        @Override
        public AttributeType.Double.Remote asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.Double.class)));
        }

        @Override
        public AttributeType.String.Remote asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.String.class)));
        }

        @Override
        public AttributeType.DateTime.Remote asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Remote(tx(), ROOT_LABEL, true);
            }
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, className(AttributeType.DateTime.class)));
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

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, label, isRoot);
            }

            @Override
            public AttributeType.Boolean.Local asBoolean() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Boolean.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asBoolean());
            }

            @Override
            public final Stream<AttributeType.Boolean.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Local::asBoolean);
            }

            @Override
            public final Stream<AttributeType.Boolean.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Local::asBoolean);
            }

            @Override
            public final Stream<Attribute.Boolean.Local> getInstances() {
                return super.getInstances().map(Attribute.Local::asBoolean);
            }

            @Override
            public final void setSupertype(AttributeType.Boolean type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Boolean.Local put(boolean value) {
                return super.put(value).asBoolean();
            }

            @Nullable
            @Override
            public final Attribute.Boolean.Local get(boolean value) {
                final Attribute.Local<?> attr = super.get(value);
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

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, label, isRoot);
            }

            @Override
            public AttributeType.Long.Local asLong() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Long.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asLong());
            }

            @Override
            public final Stream<AttributeType.Long.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Local::asLong);
            }

            @Override
            public final Stream<AttributeType.Long.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Local::asLong);
            }

            @Override
            public final Stream<Attribute.Long.Local> getInstances() {
                return super.getInstances().map(Attribute.Local::asLong);
            }

            @Override
            public final void setSupertype(AttributeType.Long type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Long.Local put(long value) {
                return super.put(value).asLong();
            }

            @Nullable
            @Override
            public final Attribute.Long.Local get(long value) {
                final Attribute.Local<?> attr = super.get(value);
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

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, label, isRoot);
            }

            @Override
            public AttributeType.Double.Local asDouble() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.Double.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asDouble());
            }

            @Override
            public final Stream<AttributeType.Double.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Local::asDouble);
            }

            @Override
            public final Stream<AttributeType.Double.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Local::asDouble);
            }

            @Override
            public final Stream<Attribute.Double.Local> getInstances() {
                return super.getInstances().map(Attribute.Local::asDouble);
            }

            @Override
            public final void setSupertype(AttributeType.Double type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.Double.Local put(double value) {
                return super.put(value).asDouble();
            }

            @Nullable
            @Override
            public final Attribute.Double.Local get(double value) {
                final Attribute.Local<?> attr = super.get(value);
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

            Local(final java.lang.String label, final boolean isRoot) {
                super(label, isRoot);
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, label, isRoot);
            }

            @Override
            public AttributeType.String.Local asString() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.String.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asString());
            }

            @Override
            public final Stream<AttributeType.String.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Local::asString);
            }

            @Override
            public final Stream<AttributeType.String.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Local::asString);
            }

            @Override
            public final Stream<Attribute.String.Local> getInstances() {
                return super.getInstances().map(Attribute.Local::asString);
            }

            @Override
            public final void setSupertype(AttributeType.String type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.String.Local put(java.lang.String value) {
                return super.put(value).asString();
            }

            @Nullable
            @Override
            public final Attribute.String.Local get(java.lang.String value) {
                final Attribute.Local<?> attr = super.get(value);
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
            public AttributeType.String.Remote asString() {
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
            public AttributeTypeImpl.DateTime.Remote asRemote(final Grakn.Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, label, isRoot);
            }

            @Override
            public AttributeType.DateTime.Local asDateTime() {
                return this;
            }
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            public Remote(final Grakn.Transaction transaction, final java.lang.String label, final boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asRemote(Grakn.Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, label, isRoot);
            }

            @Override
            public final AttributeType.DateTime.Local getSupertype() {
                return getSupertypeExecute(t -> t.asAttributeType().asDateTime());
            }

            @Override
            public final Stream<AttributeType.DateTime.Local> getSupertypes() {
                return super.getSupertypes().map(AttributeType.Local::asDateTime);
            }

            @Override
            public final Stream<AttributeType.DateTime.Local> getSubtypes() {
                return super.getSubtypes().map(AttributeType.Local::asDateTime);
            }

            @Override
            public final Stream<Attribute.DateTime.Local> getInstances() {
                return super.getInstances().map(Attribute.Local::asDateTime);
            }

            @Override
            public final void setSupertype(AttributeType.DateTime type) {
                super.setSupertype(type);
            }

            @Override
            public final Attribute.DateTime.Local put(LocalDateTime value) {
                return super.put(value).asDateTime();
            }

            @Nullable
            @Override
            public final Attribute.DateTime.Local get(LocalDateTime value) {
                final Attribute.Local<?> attr = super.get(value);
                return attr != null ? attr.asDateTime() : null;
            }

            @Override
            public AttributeType.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
