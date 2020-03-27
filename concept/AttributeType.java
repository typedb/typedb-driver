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

package grakn.client.concept;

import grakn.client.GraknClient;
import grakn.client.concept.remote.RemoteAttributeType;
import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * An ontological element which models and categorises the various Attribute in the graph.
 * This ontological element behaves similarly to Type when defining how it relates to other
 * types. It has two additional functions to be aware of:
 * 1. It has a DataType constraining the data types of the values it's instances may take.
 * 2. Any of it's instances are unique to the type.
 * For example if you have an AttributeType modelling month throughout the year there can only be one January.
 *
 * @param <D> The data type of this resource type.
 *            Supported Types include: String, Long, Double, and Boolean
 */
public interface AttributeType<D> extends UserType<AttributeType<D>, Attribute<D>> {
    //------------------------------------- Accessors ---------------------------------
    /**
     * Get the data type to which instances of the AttributeType must conform.
     *
     * @return The data type to which instances of this Attribute  must conform.
     */
    @Nullable
    @CheckReturnValue
    DataType<D> dataType();

    //------------------------------------- Other ---------------------------------
    @SuppressWarnings("unchecked")
    @Deprecated
    @CheckReturnValue
    @Override
    default AttributeType<D> asAttributeType() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @CheckReturnValue
    @Override
    default RemoteAttributeType<D> asRemote(GraknClient.Transaction tx) {
        return RemoteAttributeType.of(tx, id());
    }

    @Deprecated
    @CheckReturnValue
    @Override
    default boolean isAttributeType() {
        return true;
    }


    /**
     * A class used to hold the supported data types of resources and any other concepts.
     * This is used tp constrain value data types to only those we explicitly support.
     *
     * @param <D> The data type.
     */
    class DataType<D> {
        public static final AttributeType.DataType<Boolean> BOOLEAN = new AttributeType.DataType<>(Boolean.class);
        public static final AttributeType.DataType<LocalDateTime> DATE = new AttributeType.DataType<>(LocalDateTime.class);
        public static final AttributeType.DataType<Double> DOUBLE = new AttributeType.DataType<>(Double.class);
        public static final AttributeType.DataType<Float> FLOAT = new AttributeType.DataType<>(Float.class);
        public static final AttributeType.DataType<Integer> INTEGER = new AttributeType.DataType<>(Integer.class);
        public static final AttributeType.DataType<Long> LONG = new AttributeType.DataType<>(Long.class);
        public static final AttributeType.DataType<String> STRING = new AttributeType.DataType<>(String.class);

        private final Class<D> dataClass;

        private DataType(Class<D> dataClass) {
            this.dataClass = dataClass;
        }

        @CheckReturnValue
        public Class<D> dataClass() {
            return dataClass;
        }

        @CheckReturnValue
        public String name() {
            return dataClass.getName();
        }

        @Override
        public String toString() {
            return name();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AttributeType.DataType<?> that = (AttributeType.DataType<?>) o;

            return (this.dataClass().equals(that.dataClass()));
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^=  dataClass.hashCode();
            return h;
        }

        /**
         * Obtains the value from the given value protocol and casts it to a generic Java type D.
         *
         * @param value The value protocol object.
         * @return the value cast to D Java type.
         * @throws IllegalArgumentException if the value type is not recognised or does not match the type of this
         *      DataType.
         */
        @SuppressWarnings("unchecked")
        public static <D> D staticCastValue(ConceptProto.ValueObject value) {
            try {
                switch (value.getValueCase()) {
                    case DATE:
                        return (D) LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getDate()), ZoneId.of("Z"));
                    case STRING:
                        return (D) value.getString();
                    case BOOLEAN:
                        return (D) (Boolean) value.getBoolean();
                    case INTEGER:
                        return (D) (Integer) value.getInteger();
                    case LONG:
                        return (D) (Long) value.getLong();
                    case FLOAT:
                        return (D) (Float) value.getFloat();
                    case DOUBLE:
                        return (D) (Double) value.getDouble();
                    case VALUE_NOT_SET:
                        return null;
                    default:
                        throw new IllegalArgumentException("Unexpected value for attribute: " + value);
                }
            } catch (ClassCastException ex) {
                throw new IllegalArgumentException("Value type did not match DataType ", ex);
            }
        }

        /**
         * Obtains the value from the given value protocol and casts it to this DataType's type D.
         *
         * @param value The value protocol object.
         * @return the value cast to D Java type.
         * @throws IllegalArgumentException if the value type is not recognised or does not match the type of this
         *      DataType.
         */
        D instanceCastValue(ConceptProto.ValueObject value) {
            return staticCastValue(value);
        }
    }
}
