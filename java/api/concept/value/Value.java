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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Value extends Concept {
    int DECIMAL_SCALE = 19;

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

    /**
     * Returns a <code>struct</code> value of this value concept represented as a map from field names to values.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asStruct();
     * </pre>
     */
    Map<String, Optional<Value>> asStruct();
}
