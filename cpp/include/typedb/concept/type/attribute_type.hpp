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

#pragma once
#include "typedb/common/native.hpp"

#include "typedb/concept/type/thing_type.hpp"
#include "typedb/concept/value/value.hpp"

namespace TypeDB {

class Attribute;


/**
 * \brief Attribute types represent properties that other types can own.
 *
 * <p>Attribute types have a value type. This value type is fixed and unique for every given instance
 * of the attribute type.
 * <p>Other types can own an attribute type. That means that instances of these other types can own an instance
 * of this attribute type. This usually means that an object in our domain has a property with the matching value.
 * <p>Multiple types can own the same attribute type, and different instances of the same type or different types
 * can share ownership of the same attribute instance.
 */
class AttributeType : public ThingType {
public:
    /**
     * Retrieves the <code>Value.Type</code> of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getValueType();
     * </pre>
     */
    ValueType getValueType();

    /**
     * Sets the supplied <code>AttributeType</code> as the supertype of the current <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.setSupertype(transaction, superType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to set as the supertype of this <code>AttributeType</code>
     */
    [[nodiscard]] VoidFuture setSupertype(Transaction& transaction, AttributeType* attributeType);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code> with the given value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, Value* value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>String</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, const std::string& value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>long</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, int64_t value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>double</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, double value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>bool</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, bool value);

    /**
     * Adds and returns an <code>Attribute</code> of this <code>AttributeType</code>
     * with the given <code>DateTime</code> value.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.put(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value New <code>Attribute</code>’s value
     */
    [[nodiscard]] ConceptPtrFuture<Attribute> put(Transaction& transaction, DateTime value);

    /**
     * Sets a regular expression as a constraint for this <code>AttributeType</code>. <code>Values</code>
     * of all <code>Attribute</code>s of this type (inserted earlier or later) should match this regex.
     * <p>Can only be applied for <code>AttributeType</code>s with a <code>string</code> value type.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.setRegex(transaction, regex).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param regex Regular expression
     */
    [[nodiscard]] VoidFuture setRegex(Transaction& transaction, const std::string& regex);

    /**
     * Removes the regular expression that is defined for this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.unsetRegex(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    [[nodiscard]] VoidFuture unsetRegex(Transaction& transaction);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, Value* value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, const std::string& value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, int64_t value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, double value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, bool value);

    /**
     * Retrieves an <code>Attribute</code> of this <code>AttributeType</code> with the given value
     * if such <code>Attribute</code> exists. Otherwise, returns <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.get(transaction, value).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param value <code>Attribute</code>’s value
     */
    ConceptPtrFuture<Attribute> get(Transaction& transaction, DateTime value);

    /**
     * Retrieves the regular expression that is defined for this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getRegex(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    OptionalStringFuture getRegex(Transaction& transaction);

    /**
     * Retrieves all direct and indirect subtypes of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getSubtypes(transaction);
     * attributeType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<AttributeType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);  // Mimic overriding from Type

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
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<AttributeType> getSubtypes(Transaction& transaction, ValueType valueType, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves all direct and indirect <code>Attributes</code>
     * that are instances of this <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getInstances(transaction);
     * attributeType.getInstances(transaction, transitivity);
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<Attribute> getInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);


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
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<ThingType> getOwners(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Variant of \ref getOwners(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE)
     * for convenience
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction, {Annotation::key()}, transitivity);
     */
    ConceptIterable<ThingType> getOwners(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

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
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<ThingType> getOwners(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

private:
    AttributeType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
