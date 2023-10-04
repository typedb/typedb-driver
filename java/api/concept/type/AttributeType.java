/*
 * Copyright (C) 2022 Vaticle
 *
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

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.api.concept.thing.Attribute;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

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
     * Retrieves the <code>Value.Type</code> of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getValueType();
     * </pre>
     */
    @CheckReturnValue
    Value.Type getValueType();

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

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code> with the given value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, Value value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>String</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, String value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>long</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, long value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>double</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, double value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>boolean</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, boolean value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>LocalDateTime</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    Attribute put(TypeDBTransaction transaction, LocalDateTime value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, Value value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, String value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, long value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, double value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, boolean value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>None</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value);
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    @Nullable
    Attribute get(TypeDBTransaction transaction, LocalDateTime value);

    /**
     * Retrieves the regular expression that is defined for this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getRegex(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    String getRegex(TypeDBTransaction transaction);

    /**
     * Sets a regular expression as a constraint for this <code>AttributeType</code>. <code>Values</code>
     * of all <code>Attribute</code>s of this type (inserted earlier or later) should match this regex.
     * <p>Can only be applied for <code>AttributeType</code>s with a <code>string</code> value type.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.setRegex(transaction, regex);
     * </pre>
     *
     * @param transaction The current transaction
     * @param regex Regular expression
     */
    void setRegex(TypeDBTransaction transaction, String regex);

    /**
     * Removes the regular expression that is defined for this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.unsetRegex(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    void unsetRegex(TypeDBTransaction transaction);

    /**
     * Returns <code>True</code> if the value for attributes of this type is of type <code>boolean</code>.
     * Otherwise, returns <code>False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isBoolean();
     * </pre>
     */
    @CheckReturnValue
    default boolean isBoolean() {
        return getValueType() == Value.Type.BOOLEAN;
    }

    /**
     * Returns <code>True</code> if the value for attributes of this type is of type <code>long</code>.
     * Otherwise, returns <code>False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isLong();
     * </pre>
     */
    @CheckReturnValue
    default boolean isLong() {
        return getValueType() == Value.Type.LONG;
    }

    /**
     * Returns <code>True</code> if the value for attributes of this type is of type <code>double</code>.
     * Otherwise, returns <code>False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDouble();
     * </pre>
     */
    @CheckReturnValue
    default boolean isDouble() {
        return getValueType() == Value.Type.DOUBLE;
    }

    /**
     * Returns <code>True</code> if the value for attributes of this type is of type <code>string</code>.
     * Otherwise, returns <code>False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isString();
     * </pre>
     */
    @CheckReturnValue
    default boolean isString() {
        return getValueType() == Value.Type.STRING;
    }

    /**
     * Returns <code>True</code> if the value for attributes of this type is of type <code>datetime</code>.
     * Otherwise, returns <code>False</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.isDatetime();
     * </pre>
     */
    @CheckReturnValue
    default boolean isDateTime() {
        return getValueType() == Value.Type.DATETIME;
    }

    /**
     * Sets the supplied <code>AttributeType</code> as the supertype of the current <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.setSupertype(transaction, superType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to set as the supertype of this <code>AttributeType</code>
     */
    void setSupertype(TypeDBTransaction transaction, AttributeType attributeType);

    /**
     * Retrieves all direct and indirect subtypes of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getSubtypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect subtypes of this <code>AttributeType</code>
     * with given <code>Value.Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getSubtypes(transaction, valueType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param valueType <code>Value.Type</code> for retrieving subtypes
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Value.Type valueType);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of this <code>AttributeType</code>
     * with given <code>Value.Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getSubtypes(transaction, valueType, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param valueType <code>Value.Type</code> for retrieving subtypes
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Value.Type valueType,
                                                Transitivity transitivity);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @Override
    @CheckReturnValue
    Stream<? extends AttributeType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves all direct and indirect <code>Attributes</code>
     * that are instances of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getInstances(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstances(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect (or direct only) <code>Attributes</code>
     * that are instances of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getInstances(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @Override
    @CheckReturnValue
    Stream<? extends Attribute> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>
     * directly or through inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction);

    /**
     * Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>,
     * filtered by <code>Annotation</code>s, directly or through inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction, annotations);
     * </pre>
     *
     * @param transaction The current transaction
     * @param annotations Only retrieve <code>ThingTypes</code> that have an attribute of this
     *                    <code>AttributeType</code> with all given <code>Annotation</code>s
     */
    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations);

    /**
     * Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership,
     *                     <code>Transitivity.EXPLICIT</code> for direct ownership only
     */
    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieve all <code>Things</code> that own an attribute of this <code>AttributeType</code>,
     * filtered by <code>Annotation</code>s.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction, annotations, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param annotations Only retrieve <code>ThingTypes</code> that have an attribute of this
     *                    <code>AttributeType</code> with all given <code>Annotation</code>s
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership,
     *                     <code>Transitivity.EXPLICIT</code> for direct ownership only
     */
    @CheckReturnValue
    Stream<? extends ThingType> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations,
                                          Transitivity transitivity);
}
