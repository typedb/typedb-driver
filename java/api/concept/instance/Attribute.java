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

package com.typedb.driver.api.concept.instance;

import com.typedb.driver.api.concept.type.AttributeType;
import com.typedb.driver.api.concept.value.Value;

import javax.annotation.CheckReturnValue;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * <p>Attribute is an instance of the attribute type and has a value.
 * This value is fixed and unique for every given instance of the attribute type.</p>
 * <p>Attributes can be uniquely addressed by their type and value.</p>
 */
public interface Attribute extends Instance {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isAttribute() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default Attribute asAttribute() {
        return this;
    }

    /**
     * Retrieves the type which this <code>Attribute</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getType();
     * </pre>
     */
    @Override
    @CheckReturnValue
    AttributeType getType();

    /**
     * Retrieves the value which the <code>Attribute</code> instance holds.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getValue();
     * </pre>
     */
    @CheckReturnValue
    Value getValue();

    /**
     * Retrieves the description of the value type of the value which the <code>Attribute</code> instance holds.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getValueType();
     * </pre>
     */
    @CheckReturnValue
    String getValueType();

    /**
     * Returns a <code>boolean</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getBoolean();
     * </pre>
     */
    boolean getBoolean();

    /**
     * Returns a <code>long</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getLong();
     * </pre>
     */
    long getLong();

    /**
     * Returns a <code>double</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDouble();
     * </pre>
     */
    double getDouble();

    /**
     * Returns a <code>decimal</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDecimal();
     * </pre>
     */
    BigDecimal getDecimal();

    /**
     * Returns a <code>string</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getString();
     * </pre>
     */
    String getString();

    /**
     * Returns a <code>date</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDate();
     * </pre>
     */
    LocalDate getDate();

    /**
     * Returns a <code>datetime</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDatetime();
     * </pre>
     */
    LocalDateTime getDatetime();

    /**
     * Returns a <code>datetime-tz</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDatetimeTZ();
     * </pre>
     */
    ZonedDateTime getDatetimeTZ();

    /**
     * Returns a <code>duration</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getDuration();
     * </pre>
     */
    com.typedb.driver.common.Duration getDuration();

    /**
     * Returns a <code>struct</code> value of the value concept that this attribute holds
     * represented as a map from field names to values.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getStruct();
     * </pre>
     */
    Map<String, Optional<Value>> getStruct();
}
