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

package com.typedb.driver.api.concept.value;

import com.typedb.driver.api.concept.Concept;
import com.typedb.driver.common.Duration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

public interface Value extends Concept {
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
     * Retrieves the <code>String</code> describing the value type of this <code>Value</code> concept.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getType()
     * </pre>
     */
    String getType();

    /**
     * Returns an untyped <code>Object</code> value of this value concept.
     * This is useful for value equality or printing without having to switch on the actual contained value.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.get();
     * </pre>
     */
    Object get();

    /**
     * Returns a <code>boolean</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getBoolean();
     * </pre>
     */
    boolean getBoolean();

    /**
     * Returns a <code>long</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getLong();
     * </pre>
     */
    long getLong();

    /**
     * Returns a <code>double</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDouble();
     * </pre>
     */
    double getDouble();

    /**
     * Returns a <code>decimal</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDecimal();
     * </pre>
     */
    BigDecimal getDecimal();

    /**
     * Returns a <code>string</code> value of this value concept. If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getString();
     * </pre>
     */
    String getString();

    /**
     * Returns a <code>date</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDate();
     * </pre>
     */
    LocalDate getDate();

    /**
     * Returns a <code>datetime</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDatetime();
     * </pre>
     */
    LocalDateTime getDatetime();

    /**
     * Returns a <code>datetime-tz</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDatetimeTZ();
     * </pre>
     */
    ZonedDateTime getDatetimeTZ();

    /**
     * Returns a <code>duration</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDuration();
     * </pre>
     */
    Duration getDuration();

    /**
     * Returns a <code>struct</code> value of this value concept represented as a map from field names to values.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getStruct();
     * </pre>
     */
    Map<String, Optional<Value>> getStruct();
}
