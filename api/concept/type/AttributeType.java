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

package grakn.client.api.concept.type;

import grakn.client.api.GraknTransaction;
import grakn.client.api.concept.thing.Attribute;
import grakn.client.common.GraknClientException;
import grakn.protocol.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static grakn.client.common.ErrorMessage.Concept.BAD_VALUE_TYPE;

public interface AttributeType extends ThingType {

    @CheckReturnValue
    default ValueType getValueType() {
        return ValueType.OBJECT;
    }

    @CheckReturnValue
    boolean isKeyable();

    @Override
    @CheckReturnValue
    default boolean isAttributeType() {
        return true;
    }

    @CheckReturnValue
    default boolean isBoolean() {
        return false;
    }

    @CheckReturnValue
    default boolean isLong() {
        return false;
    }

    @CheckReturnValue
    default boolean isDouble() {
        return false;
    }

    @CheckReturnValue
    default boolean isString() {
        return false;
    }

    @CheckReturnValue
    default boolean isDateTime() {
        return false;
    }

    @Override
    @CheckReturnValue
    AttributeType.Remote asRemote(GraknTransaction transaction);

    @CheckReturnValue
    AttributeType.Boolean asBoolean();

    @CheckReturnValue
    AttributeType.Long asLong();

    @CheckReturnValue
    AttributeType.Double asDouble();

    @CheckReturnValue
    AttributeType.String asString();

    @CheckReturnValue
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

        @CheckReturnValue
        public static ValueType of(Class<?> valueClass) {
            for (ValueType t : ValueType.values()) {
                if (t.valueClass == valueClass) {
                    return t;
                }
            }
            throw new GraknClientException(BAD_VALUE_TYPE);
        }

        @CheckReturnValue
        public Class<?> valueClass() {
            return valueClass;
        }

        @CheckReturnValue
        public boolean isWritable() {
            return isWritable;
        }

        @CheckReturnValue
        public boolean isKeyable() {
            return isKeyable;
        }

        @CheckReturnValue
        public ConceptProto.AttributeType.ValueType proto() {
            switch (this) {
                case OBJECT:
                    return ConceptProto.AttributeType.ValueType.OBJECT;
                case BOOLEAN:
                    return ConceptProto.AttributeType.ValueType.BOOLEAN;
                case LONG:
                    return ConceptProto.AttributeType.ValueType.LONG;
                case DOUBLE:
                    return ConceptProto.AttributeType.ValueType.DOUBLE;
                case STRING:
                    return ConceptProto.AttributeType.ValueType.STRING;
                case DATETIME:
                    return ConceptProto.AttributeType.ValueType.DATETIME;
                default:
                    return ConceptProto.AttributeType.ValueType.UNRECOGNIZED;
            }
        }
    }

    interface Remote extends ThingType.Remote, AttributeType {

        void setSupertype(AttributeType attributeType);

        @Override
        @CheckReturnValue
        Stream<? extends AttributeType> getSubtypes();

        @Override
        @CheckReturnValue
        Stream<? extends Attribute<?>> getInstances();

        @CheckReturnValue
        Stream<? extends ThingType> getOwners();

        @CheckReturnValue
        Stream<? extends ThingType> getOwners(boolean onlyKey);

        @Override
        @CheckReturnValue
        AttributeType.Remote asAttributeType();

        @Override
        @CheckReturnValue
        AttributeType.Boolean.Remote asBoolean();

        @Override
        @CheckReturnValue
        AttributeType.Long.Remote asLong();

        @Override
        @CheckReturnValue
        AttributeType.Double.Remote asDouble();

        @Override
        @CheckReturnValue
        AttributeType.String.Remote asString();

        @Override
        @CheckReturnValue
        AttributeType.DateTime.Remote asDateTime();
    }

    interface Boolean extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @Override
        @CheckReturnValue
        default boolean isBoolean() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Boolean.Remote asRemote(GraknTransaction transaction);

        interface Remote extends AttributeType.Boolean, AttributeType.Remote {

            Attribute.Boolean put(boolean value);

            @Nullable
            @CheckReturnValue
            Attribute.Boolean get(boolean value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Boolean> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Boolean> getSubtypes();

            void setSupertype(AttributeType.Boolean booleanAttributeType);
        }
    }

    interface Long extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.LONG;
        }

        @Override
        @CheckReturnValue
        default boolean isLong() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Long.Remote asRemote(GraknTransaction transaction);

        interface Remote extends AttributeType.Long, AttributeType.Remote {

            Attribute.Long put(long value);

            @Nullable
            @CheckReturnValue
            Attribute.Long get(long value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Long> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Long> getSubtypes();

            void setSupertype(AttributeType.Long longAttributeType);
        }
    }

    interface Double extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.DOUBLE;
        }

        @Override
        @CheckReturnValue
        default boolean isDouble() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.Double.Remote asRemote(GraknTransaction transaction);

        interface Remote extends AttributeType.Double, AttributeType.Remote {

            Attribute.Double put(double value);

            @Nullable
            @CheckReturnValue
            Attribute.Double get(double value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.Double> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.Double> getSubtypes();

            void setSupertype(AttributeType.Double doubleAttributeType);
        }
    }

    interface String extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.STRING;
        }

        @Override
        @CheckReturnValue
        default boolean isString() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.String.Remote asRemote(GraknTransaction transaction);

        interface Remote extends AttributeType.String, AttributeType.Remote {

            Attribute.String put(java.lang.String value);

            @Nullable
            @CheckReturnValue
            Attribute.String get(java.lang.String value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.String> getInstances();

            @Nullable
            @CheckReturnValue
            java.lang.String getRegex();

            void setRegex(java.lang.String regex);

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.String> getSubtypes();

            void setSupertype(AttributeType.String stringAttributeType);
        }
    }

    interface DateTime extends AttributeType {

        @Override
        @CheckReturnValue
        default ValueType getValueType() {
            return ValueType.DATETIME;
        }

        @Override
        @CheckReturnValue
        default boolean isDateTime() {
            return true;
        }

        @Override
        @CheckReturnValue
        AttributeType.DateTime.Remote asRemote(GraknTransaction transaction);

        interface Remote extends AttributeType.DateTime, AttributeType.Remote {

            Attribute.DateTime put(LocalDateTime value);

            @Nullable
            @CheckReturnValue
            Attribute.DateTime get(LocalDateTime value);

            @Override
            @CheckReturnValue
            Stream<? extends Attribute.DateTime> getInstances();

            @Override
            @CheckReturnValue
            Stream<? extends AttributeType.DateTime> getSubtypes();

            void setSupertype(AttributeType.DateTime dateTimeAttributeType);
        }
    }
}
