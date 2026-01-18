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
     * Returns the value of this value concept as a <code>long</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getInteger();
     * </pre>
     */
    long getInteger();

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
     * Returns the value of this value concept as a <code>BigDecimal</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDecimal();
     * </pre>
     */
    BigDecimal getDecimal();

    /**
     * Returns the value of this value concept as a <code>String</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getString();
     * </pre>
     */
    String getString();

    /**
     * Returns the value of this value concept as a <code>LocalDate</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDate();
     * </pre>
     */
    LocalDate getDate();

    /**
     * Returns the value of this value concept as a <code>LocalDateTime</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDatetime();
     * </pre>
     */
    LocalDateTime getDatetime();

    /**
     * Returns the value of this value concept as a <code>ZonedDateTime</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDatetimeTZ();
     * </pre>
     */
    ZonedDateTime getDatetimeTZ();

    /**
     * Returns the value of this value concept as a <code>Duration</code>.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getDuration();
     * </pre>
     */
    Duration getDuration();

    /**
     * Returns the value of this value concept as a <code>Map&lt;String, Optional&lt;Value&gt;&gt;</code>
     * representing a struct with field names mapped to values.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getStruct();
     * </pre>
     */
    Map<String, Optional<Value>> getStruct();
}
