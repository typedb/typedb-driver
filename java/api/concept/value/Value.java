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

package com.vaticle.typedb.driver.api.concept.value;

import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.Duration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface Value extends Concept {
    int DECIMAL_FRACTIONAL_PART_DENOMINATOR_LOG10 = 19;
    int DECIMAL_SCALE = DECIMAL_FRACTIONAL_PART_DENOMINATOR_LOG10 - 1;

    DateTimeFormatter ISO_LOCAL_DATE_TIME_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isValue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default Value asValue() {
        return this;
    }

    /**
     * Retrieves the string representation of the type of this value concept.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getType()
     * </pre>
     */
    String getType();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isBoolean()
     * </pre>
     */
    boolean isBoolean();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isLong();
     * </pre>
     */
    boolean isLong();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDouble();
     * </pre>
     */
    boolean isDouble();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>decimal</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDecimal();
     * </pre>
     */
    boolean isDecimal();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isString();
     * </pre>
     */
    boolean isString();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>date</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDate();
     * </pre>
     */
    boolean isDate();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>datetime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDatetime();
     * </pre>
     */
    boolean isDatetime();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>datetime-tz</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDatetimeTZ();
     * </pre>
     */
    boolean isDatetimeTZ();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>duration</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDuration();
     * </pre>
     */
    boolean isDuration();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>struct</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isStruct();
     * </pre>
     */
    boolean isStruct();

    /**
     * Returns an untyped <code>Object</code> value of this value concept.
     * This is useful for value equality or printing without having to switch on the actual contained value.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asUntyped();
     * </pre>
     */
    Object asUntyped();

    /**
     * Returns a <code>boolean</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asBoolean();
     * </pre>
     */
    boolean asBoolean();

    /**
     * Returns a <code>long</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asLong();
     * </pre>
     */
    long asLong();

    /**
     * Returns a <code>double</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDouble();
     * </pre>
     */
    double asDouble();

    /**
     * Returns a <code>decimal</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDecimal();
     * </pre>
     */
    BigDecimal asDecimal();

    /**
     * Returns a <code>string</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asString();
     * </pre>
     */
    String asString();

    /**
     * Returns a <code>date</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDate();
     * </pre>
     */
    LocalDate asDate();

    /**
     * Returns a <code>datetime</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDatetime();
     * </pre>
     */
    LocalDateTime asDatetime();

    /**
     * Returns a <code>datetime-tz</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDatetimeTZ();
     * </pre>
     */
    ZonedDateTime asDatetimeTZ();

    /**
     * Returns a <code>duration</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDuration();
     * </pre>
     */
    Duration asDuration();

//    /**
//     * Returns a <code>struct</code> value of this value concept.
//     * If the value has another type, raises an exception.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * value.asStruct();
//     * </pre>
//     */
//    LocalDateTime asStruct();

    // TODO: Not sure if it's needed (and efficient) to address jni.ValueType.XXX directly now. Use "isXXX" on concepts and attribute types for now...
//    /**
//     * Used to specify the type of the value.
//     *
//     * <h3>Examples</h3>
//     * <pre>
//     * // TODO: Example
//     * </pre>
//     */
//    enum Type {
//        OBJECT(Object.class, com.vaticle.typedb.driver.jni.ValueType.Object),
//        BOOLEAN(Boolean.class, com.vaticle.typedb.driver.jni.ValueType.Boolean),
//        LONG(Long.class,  com.vaticle.typedb.driver.jni.ValueType.Long),
//        DOUBLE(Double.class, com.vaticle.typedb.driver.jni.ValueType.Double),
//        DECIMAL(Double.class, com.vaticle.typedb.driver.jni.ValueType.Decimal), // TODO: use correct value class
//        STRING(String.class, com.vaticle.typedb.driver.jni.ValueType.String),
//        DATE(LocalDateTime.class, com.vaticle.typedb.driver.jni.ValueType.Date), // TODO: use correct value class
//        DATETIME(LocalDateTime.class, com.vaticle.typedb.driver.jni.ValueType.Datetime),
//        DATETIME_TZ(LocalDateTime.class, com.vaticle.typedb.driver.jni.ValueType.DatetimeTZ), // TODO: use correct value class
//        DURATION(LocalDateTime.class, com.vaticle.typedb.driver.jni.ValueType.Duration), // TODO: use correct value class
//        STRUCT(LocalDateTime.class, com.vaticle.typedb.driver.jni.ValueType.Struct); // TODO: use correct value class
//
//        private final Class<?> valueClass;
//        public final com.vaticle.typedb.driver.jni.ValueType nativeObject;
//
//        Type(Class<?> valueClass, com.vaticle.typedb.driver.jni.ValueType nativeObject) {
//            this.valueClass = valueClass;
//            this.nativeObject = nativeObject;
//        }
//
//        @CheckReturnValue
//        public static Type of(com.vaticle.typedb.driver.jni.ValueType valueType) {
//            for (Type type : Type.values()) {
//                if (type.nativeObject == valueType) {
//                    return type;
//                }
//            }
//            throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
//        }
//
//        /**
//         * Returns a <code>Class</code> equivalent of this value concept for this programming language.
//         *
//         * <h3>Examples</h3>
//         * <pre>
//         * valueType.valueClass();
//         * </pre>
//         */
//        @CheckReturnValue
//        public Class<?> valueClass() {
//            return valueClass;
//        }
//    }
}
