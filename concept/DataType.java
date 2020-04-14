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

import grakn.protocol.session.ConceptProto;

import javax.annotation.CheckReturnValue;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A class used to hold the supported data types of resources and any other concepts.
 * This is used tp constrain value data types to only those we explicitly support.
 *
 * @param <D> The data type.
 */
public class DataType<D> {
    public static final DataType<Boolean> BOOLEAN = new DataType<>(Boolean.class);
    public static final DataType<LocalDateTime> DATE = new DataType<>(LocalDateTime.class);
    public static final DataType<Double> DOUBLE = new DataType<>(Double.class);
    public static final DataType<Float> FLOAT = new DataType<>(Float.class);
    public static final DataType<Integer> INTEGER = new DataType<>(Integer.class);
    public static final DataType<Long> LONG = new DataType<>(Long.class);
    public static final DataType<String> STRING = new DataType<>(String.class);

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

        DataType<?> that = (DataType<?>) o;

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
