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

import grakn.client.Grakn;
import grakn.client.common.exception.GraknException;
import grakn.client.concept.thing.Attribute;
import grakn.protocol.ConceptProto;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.exception.ErrorMessage.ClientInternal.UNRECOGNISED_VALUE;
import static grakn.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static grakn.client.common.exception.ErrorMessage.Protocol.UNRECOGNISED_FIELD;

public interface AttributeType extends ThingType {

    default ValueType getValueType() {
        return ValueType.OBJECT;
    }

    default boolean isKeyable() {
        return getValueType().isKeyable();
    }

    @Override
    AttributeType.Remote asRemote(Grakn.Transaction transaction);

    AttributeType.Boolean asBoolean();

    AttributeType.Long asLong();

    AttributeType.Double asDouble();

    AttributeType.String asString();

    AttributeType.DateTime asDateTime();

    enum ValueType {

        OBJECT(Object.class, false, false),
        BOOLEAN(Boolean.class, true, false),
        LONG(Long.class, true, true),
        DOUBLE(Double.class, true, false),
        STRING(String.class, true, true),
        DATETIME(LocalDateTime.class, true, true);

        private final Class<?> valueClass;
        private final boolean isWritable;
        private final boolean isKeyable;

        ValueType(Class<?> valueClass, boolean isWritable, boolean isKeyable) {
            this.valueClass = valueClass;
            this.isWritable = isWritable;
            this.isKeyable = isKeyable;
        }

        public static ValueType of(Class<?> valueClass) {
            for (final ValueType t : ValueType.values()) {
                if (t.valueClass == valueClass) {
                    return t;
                }
            }
            throw new GraknException(UNRECOGNISED_VALUE);
        }

        public static ValueType of(ConceptProto.AttributeType.VALUE_TYPE valueType) {
            switch (valueType) {
                case STRING:
                    return AttributeType.ValueType.STRING;
                case BOOLEAN:
                    return AttributeType.ValueType.BOOLEAN;
                case LONG:
                    return AttributeType.ValueType.LONG;
                case DOUBLE:
                    return AttributeType.ValueType.DOUBLE;
                case DATETIME:
                    return AttributeType.ValueType.DATETIME;
                default:
                case UNRECOGNIZED:
                    throw new GraknException(UNRECOGNISED_FIELD.message(ConceptProto.AttributeType.VALUE_TYPE.class.getCanonicalName(), valueType));
            }
        }

        public Class<?> valueClass() {
            return valueClass;
        }

        public boolean isWritable() {
            return isWritable;
        }

        public boolean isKeyable() {
            return isKeyable;
        }

        @Override
        public java.lang.String toString() {
            return valueClass.getName();
        }
    }

    interface Local extends ThingType.Local, AttributeType {

        @Override
        AttributeType.Remote asRemote(Grakn.Transaction transaction);

        @Override
        default AttributeType.Local asAttributeType() {
            return this;
        }

        @Override
        default AttributeType.Boolean.Local asBoolean() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Boolean.class.getCanonicalName()));
        }

        @Override
        default AttributeType.Long.Local asLong() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Long.class.getCanonicalName()));
        }

        @Override
        default AttributeType.Double.Local asDouble() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.Double.class.getCanonicalName()));
        }

        @Override
        default AttributeType.String.Local asString() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.String.class.getCanonicalName()));
        }

        @Override
        default AttributeType.DateTime.Local asDateTime() {
            throw new GraknException(INVALID_CONCEPT_CASTING.message(this, AttributeType.DateTime.class.getCanonicalName()));
        }
    }

    interface Remote extends ThingType.Remote, AttributeType {

        void setSupertype(AttributeType type);

        @Override
        default AttributeType.Remote asRemote(Grakn.Transaction transaction) {
            return this;
        }

        @Override
        default AttributeType.Remote asAttributeType() {
            return this;
        }

        @Override
        AttributeType.Boolean.Remote asBoolean();

        @Override
        AttributeType.Long.Remote asLong();

        @Override
        AttributeType.Double.Remote asDouble();

        @Override
        AttributeType.String.Remote asString();

        @Override
        AttributeType.DateTime.Remote asDateTime();

        @Override
        Stream<? extends Attribute.Remote<?>> getInstances();

        Stream<? extends ThingType> getOwners();

        Stream<? extends ThingType> getOwners(boolean onlyKey);
    }

    interface Boolean extends AttributeType {

        @Override
        default ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @Override
        AttributeType.Boolean.Remote asRemote(Grakn.Transaction transaction);

        interface Local extends AttributeType.Boolean, AttributeType.Local {

            @Override
            default AttributeType.Boolean.Local asBoolean() {
                return this;
            }
        }

        interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            @Override
            AttributeType.Boolean.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Boolean.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Boolean.Remote> getInstances();

            void setSupertype(AttributeType.Boolean type);

            Attribute.Boolean.Remote put(boolean value);

            @Nullable
            Attribute.Boolean.Remote get(boolean value);

            @Override
            default AttributeType.Boolean.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default AttributeType.Boolean.Remote asBoolean() {
                return this;
            }
        }
    }

    interface Long extends AttributeType {

        @Override
        default ValueType getValueType() {
            return ValueType.LONG;
        }

        @Override
        AttributeType.Long.Remote asRemote(Grakn.Transaction transaction);

        interface Local extends AttributeType.Long, AttributeType.Local {

            @Override
            default AttributeType.Long.Local asLong() {
                return this;
            }
        }

        interface Remote extends AttributeType.Long, AttributeType.Remote {

            @Override
            AttributeType.Long.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Long.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Long.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Long.Remote> getInstances();

            void setSupertype(AttributeType.Long type);

            Attribute.Long.Remote put(long value);

            @Nullable
            Attribute.Long.Remote get(long value);

            @Override
            default AttributeType.Long.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default AttributeType.Long.Remote asLong() {
                return this;
            }
        }
    }

    interface Double extends AttributeType {

        @Override
        default ValueType getValueType() {
            return ValueType.DOUBLE;
        }

        @Override
        AttributeType.Double.Remote asRemote(Grakn.Transaction transaction);

        interface Local extends AttributeType.Double, AttributeType.Local {

            @Override
            default AttributeType.Double.Local asDouble() {
                return this;
            }
        }

        interface Remote extends AttributeType.Double, AttributeType.Remote {

            @Override
            AttributeType.Double.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.Double.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.Double.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.Double.Remote> getInstances();

            void setSupertype(AttributeType.Double type);

            Attribute.Double.Remote put(double value);

            @Nullable
            Attribute.Double.Remote get(double value);

            @Override
            default AttributeType.Double.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default AttributeType.Double.Remote asDouble() {
                return this;
            }
        }
    }

    interface String extends AttributeType {

        @Override
        default ValueType getValueType() {
            return ValueType.STRING;
        }

        @Override
        AttributeType.String.Remote asRemote(Grakn.Transaction transaction);

        interface Local extends AttributeType.String, AttributeType.Local {

            @Override
            default AttributeType.String.Local asString() {
                return this;
            }
        }

        interface Remote extends AttributeType.String, AttributeType.Remote {

            @Override
            AttributeType.String.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.String.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.String.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.String.Remote> getInstances();

            void setSupertype(AttributeType.String type);

            Attribute.String.Remote put(java.lang.String value);

            @Nullable
            Attribute.String.Remote get(java.lang.String value);

            @Nullable
            java.lang.String getRegex();

            void setRegex(java.lang.String regex);

            @Override
            default AttributeType.String.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default AttributeType.String.Remote asString() {
                return this;
            }
        }
    }

    interface DateTime extends AttributeType {

        @Override
        default ValueType getValueType() {
            return ValueType.DATETIME;
        }

        @Override
        AttributeType.DateTime.Remote asRemote(Grakn.Transaction transaction);

        interface Local extends AttributeType.DateTime, AttributeType.Local {

            @Override
            default AttributeType.DateTime.Local asDateTime() {
                return this;
            }
        }

        interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            @Override
            AttributeType.DateTime.Remote getSupertype();

            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSupertypes();

            @Override
            Stream<? extends AttributeType.DateTime.Remote> getSubtypes();

            @Override
            Stream<? extends Attribute.DateTime.Remote> getInstances();

            void setSupertype(AttributeType.DateTime type);

            Attribute.DateTime.Remote put(LocalDateTime value);

            @Nullable
            Attribute.DateTime.Remote get(LocalDateTime value);

            @Override
            default AttributeType.DateTime.Remote asRemote(Grakn.Transaction transaction) {
                return this;
            }

            @Override
            default AttributeType.DateTime.Remote asDateTime() {
                return this;
            }
        }
    }
}
