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

package grakn.client.concept.type;

import grakn.client.api.Transaction;
import grakn.client.api.concept.type.AttributeType;
import grakn.client.common.GraknClientException;
import grakn.client.common.Proto;
import grakn.client.concept.thing.AttributeImpl;
import grakn.client.concept.thing.ThingImpl;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.ErrorMessage.Concept.BAD_VALUE_TYPE;
import static grakn.client.common.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.Proto.Thing.Attribute.attributeValueBoolean;
import static grakn.client.common.Proto.Thing.Attribute.attributeValueDateTime;
import static grakn.client.common.Proto.Thing.Attribute.attributeValueDouble;
import static grakn.client.common.Proto.Thing.Attribute.attributeValueLong;
import static grakn.client.common.Proto.Thing.Attribute.attributeValueString;
import static grakn.common.util.Objects.className;

public class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {

    private static final java.lang.String ROOT_LABEL = "attribute";

    AttributeTypeImpl(java.lang.String label, boolean isRoot) {
        super(label, isRoot);
    }

    public static AttributeTypeImpl of(ConceptProto.Type type) {
        switch (type.getValueType()) {
            case BOOLEAN:
                return new AttributeTypeImpl.Boolean(type.getLabel(), type.getRoot());
            case LONG:
                return new AttributeTypeImpl.Long(type.getLabel(), type.getRoot());
            case DOUBLE:
                return new AttributeTypeImpl.Double(type.getLabel(), type.getRoot());
            case STRING:
                return new AttributeTypeImpl.String(type.getLabel(), type.getRoot());
            case DATETIME:
                return new AttributeTypeImpl.DateTime(type.getLabel(), type.getRoot());
            case OBJECT:
                assert type.getRoot();
                return new AttributeTypeImpl(type.getLabel(), type.getRoot());
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
    public AttributeTypeImpl.Remote asRemote(Transaction transaction) {
        return new AttributeTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public AttributeTypeImpl asAttributeType() {
        return this;
    }

    @Override
    public AttributeTypeImpl.Boolean asBoolean() {
        if (isRoot()) return new Boolean(ROOT_LABEL, true);
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                className(this.getClass()), className(AttributeType.Boolean.class))
        );
    }

    @Override
    public AttributeTypeImpl.Long asLong() {
        if (isRoot()) return new Long(ROOT_LABEL, true);
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                className(this.getClass()), className(AttributeType.Long.class)
        ));
    }

    @Override
    public AttributeTypeImpl.Double asDouble() {
        if (isRoot()) return new Double(ROOT_LABEL, true);
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                className(this.getClass()), className(AttributeType.Double.class)
        ));
    }

    @Override
    public AttributeTypeImpl.String asString() {
        if (isRoot()) return new String(ROOT_LABEL, true);
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                className(this.getClass()), className(AttributeType.String.class)
        ));
    }

    @Override
    public AttributeTypeImpl.DateTime asDateTime() {
        if (isRoot()) return new DateTime(ROOT_LABEL, true);
        throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                className(this.getClass()), className(AttributeType.DateTime.class)
        ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeTypeImpl)) return false;
        // We do the above, as opposed to checking if (object == null || getClass() != object.getClass())
        // because it is possible to compare attribute root types wrapped in different type classes
        // such as: root type wrapped in AttributeTypeImpl.Root and in AttributeTypeImpl.Boolean.Root
        // We only override equals(), but not hash(), in this class, as hash() the logic from TypeImpl still applies.

        AttributeTypeImpl that = (AttributeTypeImpl) o;
        return this.getLabel().equals(that.getLabel());
    }

    public static class Remote extends ThingTypeImpl.Remote implements AttributeType.Remote {

        Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
            super(transaction, label, isRoot);
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
        public AttributeTypeImpl.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public final void setSupertype(AttributeType attributeType) {
            super.setSupertype(attributeType);
        }

        @Override
        public Stream<? extends AttributeTypeImpl> getSubtypes() {
            Stream<AttributeTypeImpl> stream = super.getSubtypes().map(TypeImpl::asAttributeType);

            if (isRoot() && getValueType() != ValueType.OBJECT) {
                // Get all attribute types of this value type
                return stream.filter(x -> x.getValueType() == this.getValueType() || x.getLabel().equals(this.getLabel()));
            }

            return stream;
        }

        @Override
        public Stream<? extends AttributeImpl<?>> getInstances() {
            return super.getInstances().map(ThingImpl::asAttribute);
        }

        @Override
        public Stream<ThingTypeImpl> getOwners() {
            return getOwners(false);
        }

        @Override
        public Stream<ThingTypeImpl> getOwners(boolean onlyKey) {
            return stream(Proto.Type.AttributeType.getOwners(getLabel(), onlyKey))
                    .flatMap(rp -> rp.getAttributeTypeGetOwnersResPart().getOwnersList().stream())
                    .map(ThingTypeImpl::of);
        }

        protected final AttributeImpl<?> put(ConceptProto.Attribute.Value value) {
            ConceptProto.Type.Res res = execute(Proto.Type.AttributeType.put(getLabel(), value));
            return AttributeImpl.of(res.getAttributeTypePutRes().getAttribute());
        }

        @Nullable
        protected final AttributeImpl<?> get(ConceptProto.Attribute.Value value) {
            ConceptProto.Type.Res res = execute(Proto.Type.AttributeType.get(getLabel(), value));
            switch (res.getAttributeTypeGetRes().getResCase()) {
                case ATTRIBUTE:
                    return AttributeImpl.of(res.getAttributeTypeGetRes().getAttribute());
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
            if (isRoot()) return new AttributeTypeImpl.Boolean.Remote(tx(), ROOT_LABEL, true);
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                    className(this.getClass()), className(AttributeType.Boolean.class)
            ));
        }

        @Override
        public AttributeTypeImpl.Long.Remote asLong() {
            if (isRoot()) return new AttributeTypeImpl.Long.Remote(tx(), ROOT_LABEL, true);
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                    className(this.getClass()), className(AttributeType.Long.class)
            ));
        }

        @Override
        public AttributeTypeImpl.Double.Remote asDouble() {
            if (isRoot()) return new AttributeTypeImpl.Double.Remote(tx(), ROOT_LABEL, true);
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                    className(this.getClass()), className(AttributeType.Double.class)
            ));
        }

        @Override
        public AttributeTypeImpl.String.Remote asString() {
            if (isRoot()) return new AttributeTypeImpl.String.Remote(tx(), ROOT_LABEL, true);
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                    className(this.getClass()), className(AttributeType.String.class)
            ));
        }

        @Override
        public AttributeTypeImpl.DateTime.Remote asDateTime() {
            if (isRoot()) return new AttributeTypeImpl.DateTime.Remote(tx(), ROOT_LABEL, true);
            throw new GraknClientException(INVALID_CONCEPT_CASTING.message(
                    className(this.getClass()), className(AttributeType.DateTime.class)
            ));
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

    public static class Boolean extends AttributeTypeImpl implements AttributeType.Boolean {

        Boolean(java.lang.String label, boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.Boolean of(ConceptProto.Type typeProto) {
            return new AttributeTypeImpl.Boolean(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @Override
        public AttributeTypeImpl.Boolean.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.Boolean.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.Boolean asBoolean() {
            return this;
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Boolean.Remote {

            public Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.BOOLEAN;
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asRemote(Transaction transaction) {
                return new AttributeTypeImpl.Boolean.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final Stream<AttributeTypeImpl.Boolean> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl::asBoolean);
            }

            @Override
            public final Stream<AttributeImpl.Boolean> getInstances() {
                return super.getInstances().map(AttributeImpl::asBoolean);
            }

            @Override
            public final void setSupertype(AttributeType.Boolean booleanAttributeType) {
                super.setSupertype(booleanAttributeType);
            }

            @Override
            public final AttributeImpl.Boolean put(boolean value) {
                return super.put(attributeValueBoolean(value)).asBoolean();
            }

            @Nullable
            @Override
            public final AttributeImpl.Boolean get(boolean value) {
                AttributeImpl<?> attr = super.get(attributeValueBoolean(value));
                return attr != null ? attr.asBoolean() : null;
            }

            @Override
            public AttributeTypeImpl.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    public static class Long extends AttributeTypeImpl implements AttributeType.Long {

        Long(java.lang.String label, boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.Long of(ConceptProto.Type typeProto) {
            return new AttributeTypeImpl.Long(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public ValueType getValueType() {
            return ValueType.LONG;
        }

        @Override
        public AttributeTypeImpl.Long.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.Long.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.Long asLong() {
            return this;
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Long.Remote {

            public Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.LONG;
            }

            @Override
            public AttributeTypeImpl.Long.Remote asRemote(Transaction transaction) {
                return new AttributeTypeImpl.Long.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final Stream<AttributeTypeImpl.Long> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl::asLong);
            }

            @Override
            public final Stream<AttributeImpl.Long> getInstances() {
                return super.getInstances().map(AttributeImpl::asLong);
            }

            @Override
            public final void setSupertype(AttributeType.Long longAttributeType) {
                super.setSupertype(longAttributeType);
            }

            @Override
            public final AttributeImpl.Long put(long value) {
                return super.put(attributeValueLong(value)).asLong();
            }

            @Nullable
            @Override
            public final AttributeImpl.Long get(long value) {
                AttributeImpl<?> attr = super.get(attributeValueLong(value));
                return attr != null ? attr.asLong() : null;
            }

            @Override
            public AttributeTypeImpl.Long.Remote asLong() {
                return this;
            }
        }
    }

    public static class Double extends AttributeTypeImpl implements AttributeType.Double {

        Double(java.lang.String label, boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.Double of(ConceptProto.Type typeProto) {
            return new AttributeTypeImpl.Double(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public ValueType getValueType() {
            return ValueType.DOUBLE;
        }

        @Override
        public AttributeTypeImpl.Double.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.Double.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.Double asDouble() {
            return this;
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.Double.Remote {

            public Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DOUBLE;
            }

            @Override
            public AttributeTypeImpl.Double.Remote asRemote(Transaction transaction) {
                return new AttributeTypeImpl.Double.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final Stream<AttributeTypeImpl.Double> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl::asDouble);
            }

            @Override
            public final Stream<AttributeImpl.Double> getInstances() {
                return super.getInstances().map(AttributeImpl::asDouble);
            }

            @Override
            public final void setSupertype(AttributeType.Double doubleAttributeType) {
                super.setSupertype(doubleAttributeType);
            }

            @Override
            public final AttributeImpl.Double put(double value) {
                return super.put(attributeValueDouble(value)).asDouble();
            }

            @Nullable
            @Override
            public final AttributeImpl.Double get(double value) {
                AttributeImpl<?> attr = super.get(attributeValueDouble(value));
                return attr != null ? attr.asDouble() : null;
            }

            @Override
            public AttributeTypeImpl.Double.Remote asDouble() {
                return this;
            }
        }
    }

    public static class String extends AttributeTypeImpl implements AttributeType.String {

        String(java.lang.String label, boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.String of(ConceptProto.Type typeProto) {
            return new AttributeTypeImpl.String(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public ValueType getValueType() {
            return ValueType.STRING;
        }

        @Override
        public AttributeTypeImpl.String.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.String.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.String asString() {
            return this;
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.String.Remote {

            public Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.STRING;
            }

            @Override
            public AttributeTypeImpl.String.Remote asRemote(Transaction transaction) {
                return new AttributeTypeImpl.String.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final Stream<AttributeTypeImpl.String> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl::asString);
            }

            @Override
            public final Stream<AttributeImpl.String> getInstances() {
                return super.getInstances().map(AttributeImpl::asString);
            }

            @Override
            public final void setSupertype(AttributeType.String stringAttributeType) {
                super.setSupertype(stringAttributeType);
            }

            @Override
            public final AttributeImpl.String put(java.lang.String value) {
                return super.put(attributeValueString(value)).asString();
            }

            @Nullable
            @Override
            public final AttributeImpl.String get(java.lang.String value) {
                AttributeImpl<?> attr = super.get(attributeValueString(value));
                return attr != null ? attr.asString() : null;
            }

            @Nullable
            @Override
            public final java.lang.String getRegex() {
                ConceptProto.Type.Res res = execute(Proto.Type.AttributeType.getRegex(getLabel()));
                java.lang.String regex = res.getAttributeTypeGetRegexRes().getRegex();
                return regex.isEmpty() ? null : regex;
            }

            @Override
            public final void setRegex(java.lang.String regex) {
                if (regex == null) regex = "";
                execute(Proto.Type.AttributeType.setRegex(getLabel(), regex));
            }

            @Override
            public AttributeTypeImpl.String.Remote asString() {
                return this;
            }
        }
    }

    public static class DateTime extends AttributeTypeImpl implements AttributeType.DateTime {

        DateTime(java.lang.String label, boolean isRoot) {
            super(label, isRoot);
        }

        public static AttributeTypeImpl.DateTime of(ConceptProto.Type typeProto) {
            return new AttributeTypeImpl.DateTime(typeProto.getLabel(), typeProto.getRoot());
        }

        @Override
        public ValueType getValueType() {
            return ValueType.DATETIME;
        }

        @Override
        public AttributeTypeImpl.DateTime.Remote asRemote(Transaction transaction) {
            return new AttributeTypeImpl.DateTime.Remote(transaction, getLabel(), isRoot());
        }

        @Override
        public AttributeTypeImpl.DateTime asDateTime() {
            return this;
        }

        public static class Remote extends AttributeTypeImpl.Remote implements AttributeType.DateTime.Remote {

            public Remote(Transaction transaction, java.lang.String label, boolean isRoot) {
                super(transaction, label, isRoot);
            }

            @Override
            public ValueType getValueType() {
                return ValueType.DATETIME;
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asRemote(Transaction transaction) {
                return new AttributeTypeImpl.DateTime.Remote(transaction, getLabel(), isRoot());
            }

            @Override
            public final Stream<AttributeTypeImpl.DateTime> getSubtypes() {
                return super.getSubtypes().map(AttributeTypeImpl::asDateTime);
            }

            @Override
            public final Stream<AttributeImpl.DateTime> getInstances() {
                return super.getInstances().map(AttributeImpl::asDateTime);
            }

            @Override
            public final void setSupertype(AttributeType.DateTime dateTimeAttributeType) {
                super.setSupertype(dateTimeAttributeType);
            }

            @Override
            public final AttributeImpl.DateTime put(LocalDateTime value) {
                return super.put(attributeValueDateTime(value)).asDateTime();
            }

            @Nullable
            @Override
            public final AttributeImpl.DateTime get(LocalDateTime value) {
                AttributeImpl<?> attr = super.get(attributeValueDateTime(value));
                return attr != null ? attr.asDateTime() : null;
            }

            @Override
            public AttributeTypeImpl.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
