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

package com.vaticle.typedb.driver.api.concept.type;

import javax.annotation.CheckReturnValue;

/**
 * Attribute types represent properties that other types can own.
 * <p>Attribute types have a value type. This value type is fixed and unique for every given instance
 * of the attribute type.
 * <p>Other types can own an attribute type. That means that instances of these other types can own an instance
 * of this attribute type. This usually means that an object in our domain has a property with the matching value.
 * <p>Multiple types can own the same attribute type, and different instances of the same type or different types
 * can share ownership of the same attribute instance.
 */
public interface AttributeType extends ThingType {
    /**
     * Retrieves the <code>String</code> describing the value type of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getValueType();
     * </pre>
     */
    @CheckReturnValue
    String getValueType();

    /**
     * Returns <code>True</code> if this attribute type does not have a value type.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isUntyped()
     * </pre>
     */
    boolean isUntyped();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isBoolean()
     * </pre>
     */
    boolean isBoolean();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isLong();
     * </pre>
     */
    boolean isLong();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDouble();
     * </pre>
     */
    boolean isDouble();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>decimal</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDecimal();
     * </pre>
     */
    boolean isDecimal();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isString();
     * </pre>
     */
    boolean isString();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>date</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDate();
     * </pre>
     */
    boolean isDate();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>datetime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDatetime();
     * </pre>
     */
    boolean isDatetime();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>datetime-tz</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDatetimeTZ();
     * </pre>
     */
    boolean isDatetimeTZ();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>duration</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDuration();
     * </pre>
     */
    boolean isDuration();

    /**
     * Returns <code>True</code> if this attribute type is of type <code>struct</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isStruct();
     * </pre>
     */
    boolean isStruct();

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isAttributeType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default AttributeType asAttributeType() {
        return this;
    }
}
