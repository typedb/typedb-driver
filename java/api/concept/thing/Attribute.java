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

package com.typedb.driver.api.concept.thing;

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
public interface Attribute extends Thing {
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
     * Returns <code>True</code> if this attribute holds a value of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isBoolean()
     * </pre>
     */
    boolean isBoolean();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isLong();
     * </pre>
     */
    boolean isLong();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDouble();
     * </pre>
     */
    boolean isDouble();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>decimal</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDecimal();
     * </pre>
     */
    boolean isDecimal();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isString();
     * </pre>
     */
    boolean isString();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>date</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDate();
     * </pre>
     */
    boolean isDate();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>datetime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDatetime();
     * </pre>
     */
    boolean isDatetime();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>datetime-tz</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDatetimeTZ();
     * </pre>
     */
    boolean isDatetimeTZ();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>duration</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isDuration();
     * </pre>
     */
    boolean isDuration();

    /**
     * Returns <code>True</code> if this attribute holds a value of type <code>struct</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isStruct();
     * </pre>
     */
    boolean isStruct();
    
    /**
     * Returns an untyped <code>Object</code> value of the value concept that this attribute holds.
     * This is useful for value equality or printing without having to switch on the actual contained value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asUntyped();
     * </pre>
     */
    Object asUntyped();

    /**
     * Returns a <code>boolean</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asBoolean();
     * </pre>
     */
    boolean asBoolean();

    /**
     * Returns a <code>long</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asLong();
     * </pre>
     */
    long asLong();

    /**
     * Returns a <code>double</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDouble();
     * </pre>
     */
    double asDouble();

    /**
     * Returns a <code>decimal</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDecimal();
     * </pre>
     */
    BigDecimal asDecimal();

    /**
     * Returns a <code>string</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asString();
     * </pre>
     */
    String asString();

    /**
     * Returns a <code>date</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDate();
     * </pre>
     */
    LocalDate asDate();

    /**
     * Returns a <code>datetime</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDatetime();
     * </pre>
     */
    LocalDateTime asDatetime();

    /**
     * Returns a <code>datetime-tz</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDatetimeTZ();
     * </pre>
     */
    ZonedDateTime asDatetimeTZ();

    /**
     * Returns a <code>duration</code> value of the value concept that this attribute holds.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asDuration();
     * </pre>
     */
    com.typedb.driver.common.Duration asDuration();

    /**
     * Returns a <code>struct</code> value of the value concept that this attribute holds
     * represented as a map from field names to values.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asStruct();
     * </pre>
     */
    Map<String, Optional<Value>> asStruct();

    /**
     * Checks if the concept is an <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.isAttribute();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default boolean isAttribute() {
        return true;
    }

    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.asAttribute();
     * </pre>
     */
    @Override
    @CheckReturnValue
    default Attribute asAttribute() {
        return this;
    }

    /**
     * Retrieves the value which the <code>Attribute</code> instance holds.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getValue();
     * </pre>
     */
    Value getValue();
}
