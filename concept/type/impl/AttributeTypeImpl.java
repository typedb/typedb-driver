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

import grakn.client.common.exception.GraknClientException;
import grakn.client.concept.Concepts;
import grakn.client.concept.thing.Attribute;
import grakn.client.concept.thing.Thing;
import grakn.client.concept.type.AttributeType;
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.Type;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientTypeWrite.ROOT_TYPE_MUTATION;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.concept.ConceptMessageWriter.attributeValue;

public abstract class AttributeTypeImpl {

    private static final java.lang.String ROOT_LABEL = "attribute";

    /**
     * Client implementation of AttributeType
     */
    public static abstract class Local extends ThingTypeImpl.Local implements AttributeType.Local {

        public Local(final ConceptProto.Type type) {
            super(type);
        }

        public static final class Root extends AttributeTypeImpl.Local {

            public Root(final ConceptProto.Type type) {
                super(type);
            }

            @Override
            public AttributeType.Remote asRemote(final Concepts concepts) {
                return new AttributeTypeImpl.Remote.Root(concepts);
            }
        }
    }

    /**
     * Client implementation of AttributeType
     */
    public static abstract class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
            super(concepts, label, isRoot);
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
            // Compare value types to ensure we can get all attribute types of a given value type using AttributeTypeImpl.{VALUE_TYPE}.Remote.Root
            // Compare labels to ensure that 'attribute' is a subtype of itself
            return super.getSubtypes().map(Type.Remote::asAttributeType).filter(x ->
                    x.getValueType() == this.getValueType() || x.getLabel().equals(this.getLabel()));
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
            return Thing.Remote.of(concepts(), runMethod(method).getAttributeTypePutRes().getAttribute()).asAttribute();
        }

        @Nullable
        protected final Attribute.Remote<?> get(Object value) {
            final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                    .setAttributeTypeGetReq(ConceptProto.AttributeType.Get.Req.newBuilder()
                            .setValue(attributeValue(value))).build();
            ConceptProto.AttributeType.Get.Res response = runMethod(method).getAttributeTypeGetRes();
            switch (response.getResCase()) {
                case ATTRIBUTE:
                    return Thing.Remote.of(concepts(), response.getAttribute()).asAttribute();
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
                return new AttributeTypeImpl.Boolean.Remote(concepts(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Boolean.class.getCanonicalName()));
        }

        @Override
        public AttributeType.Long.Remote asLong() {
            if (isRoot()) {
                return new AttributeTypeImpl.Long.Remote(concepts(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Long.class.getCanonicalName()));
        }

        @Override
        public AttributeType.Double.Remote asDouble() {
            if (isRoot()) {
                return new AttributeTypeImpl.Double.Remote(concepts(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Double.class.getCanonicalName()));
        }

        @Override
        public AttributeType.String.Remote asString() {
            if (isRoot()) {
                return new AttributeTypeImpl.String.Remote(concepts(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.String.class.getCanonicalName()));
        }

        @Override
        public AttributeType.DateTime.Remote asDateTime() {
            if (isRoot()) {
                return new AttributeTypeImpl.DateTime.Remote(concepts(), ROOT_LABEL, true);
            }
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(this, AttributeType.DateTime.class.getCanonicalName()));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributeTypeImpl.Remote)) return false;
            // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
            // because it is possible to compare a attribute root types wrapped in different type classes
            // such as: root type wrapped in AttributeTypeImpl.Root and as in AttributeType.Boolean.Root

            AttributeTypeImpl.Remote that = (AttributeTypeImpl.Remote) o;

            return this.concepts().equals(that.concepts()) &&
                    this.getLabel().equals(that.getLabel());
        }

        public static final class Root extends AttributeTypeImpl.Remote {

            public Root(final Concepts concepts) {
                super(concepts, ROOT_LABEL, true);
            }

            @Nullable
            @Override
            public AttributeType.Remote getSupertype() {
                return null;
            }

            @Override
            public Stream<? extends AttributeType.Remote> getSupertypes() {
                return Stream.of(this);
            }

            @Override
            public Stream<? extends AttributeType.Remote> getSubtypes() {
                // identical to Type.getSubtypes, but that is not accessible from this class
                final ConceptProto.TypeMethod.Iter.Req method = ConceptProto.TypeMethod.Iter.Req.newBuilder()
                        .setTypeGetSubtypesIterReq(ConceptProto.Type.GetSubtypes.Iter.Req.getDefaultInstance()).build();

                return typeStream(method, res -> res.getTypeGetSubtypesIterRes().getType()).map(Type.Remote::asAttributeType);
            }
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

            public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
                super(concepts, label, isRoot);
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

            @CheckReturnValue
            @Override
            public AttributeType.Boolean.Remote asBoolean() {
                return this;
            }

            public static final class Root extends AttributeTypeImpl.Boolean.Remote {

                public Root(final Concepts concepts) {
                    super(concepts, ROOT_LABEL, true);
                }
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

            public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
                super(concepts, label, isRoot);
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

            @CheckReturnValue
            @Override
            public AttributeType.Long.Remote asLong() {
                return this;
            }

            // TODO: do we actually need these Root types anymore, now that isRoot is a field?
            public static final class Root extends AttributeTypeImpl.Long.Remote {

                public Root(final Concepts concepts) {
                    super(concepts, ROOT_LABEL, true);
                }
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

            public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
                super(concepts, label, isRoot);
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

            @CheckReturnValue
            @Override
            public AttributeType.Double.Remote asDouble() {
                return this;
            }

            public static final class Root extends AttributeTypeImpl.Double.Remote {

                public Root(final Concepts concepts) {
                    super(concepts, ROOT_LABEL, true);
                }
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

            public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
                super(concepts, label, isRoot);
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
                if (isRoot()) {
                    throw new GraknClientException(ROOT_TYPE_MUTATION);
                }
                if (regex == null) regex = "";
                final ConceptProto.TypeMethod.Req method = ConceptProto.TypeMethod.Req.newBuilder()
                        .setAttributeTypeSetRegexReq(ConceptProto.AttributeType.SetRegex.Req.newBuilder()
                                .setRegex(regex)).build();
                runMethod(method);
            }

            @CheckReturnValue
            @Override
            public AttributeType.String.Remote asString() {
                return this;
            }

            public static final class Root extends AttributeTypeImpl.String.Remote {

                public Root(final Concepts concepts) {
                    super(concepts, ROOT_LABEL, true);
                }
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

            public Remote(final Concepts concepts, final java.lang.String label, final boolean isRoot) {
                super(concepts, label, isRoot);
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

            @CheckReturnValue
            @Override
            public AttributeType.DateTime.Remote asDateTime() {
                return this;
            }

            public static final class Root extends AttributeTypeImpl.DateTime.Remote {

                public Root(final Concepts concepts) {
                    super(concepts, ROOT_LABEL, true);
                }
            }
        }
    }
}
