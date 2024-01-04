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

#include <chrono>

#include "typedb/concept/concept.hpp"

namespace TypeDB {

typedef std::chrono::time_point<std::chrono::system_clock, std::chrono::milliseconds> DateTime;
/**
 * \brief A primitive value. Holds the value of an attribute, or the result of an expression in a query.
 */
class Value : public Concept {
public:
    /**
     * Retrieves the <code>ValueType</code> of this value concept.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.getType()
     * </pre>
     */
    ValueType valueType();

    /**
     * Returns <code>true</code> if the value which this value concept holds is of type <code>boolean</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isBoolean()
     * </pre>
     */
    bool isBoolean();

    /**
     * Returns <code>true</code> if the value which this value concept holds is of type <code>long</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isLong()
     * </pre>
     */
    bool isLong();

    /**
     * Returns <code>true</code> if the value which this value concept holds is of type <code>double</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDouble();
     * </pre>
     */
    bool isDouble();

    /**
     * Returns <code>true</code> if the value which this value concept holds is of type <code>string</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isString();
     * </pre>
     */
    bool isString();

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>DateTime</code>.
     * Otherwise, returns <code>false</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.isDatetime();
     * </pre>
     */
    bool isDateTime();


    /**
     * Returns a <code>boolean</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asBoolean();
     * </pre>
     */
    bool asBoolean();

    /**
     * Returns a <code>long</code> value of this value concept.
     *  If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asLong();
     * </pre>
     */
    int64_t asLong();

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
     * Returns a <code>string</code> value of this value concept.
     *  If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asString();
     * </pre>
     */
    std::string asString();

    /**
     * Returns a <code>DateTime</code> value of this value concept.
     * If the value has another type, raises an exception.
     *
     * <h3>Examples</h3>
     * <pre>
     * value.asDatetime();
     * </pre>
     */
    DateTime asDateTime();

    /**
     * Returns a string in the TypeQL DateTime format corresponding to the specified DateTime
     * (yyyy-mm-dd'T'HH:MM:SS)
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::formatDateTime(datetime);
     * </pre>
     */
    static std::string formatDateTime(DateTime t);
    /**
     * Parses a DateTime from a string in the TypeQL DateTime format (yyyy-mm-dd'T'HH:MM:SS)
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::parseDateTime(str);
     * </pre>
     */
    static DateTime parseDateTime(const std::string& s);

    /**
     * Creates a new Value object of the specified boolean value.
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::of(value);
     * </pre>
     */
    static std::unique_ptr<Value> of(bool value);

    /**
     * Creates a new Value object of the specified long value.
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::of(value);
     * </pre>
     */
    static std::unique_ptr<Value> of(int64_t value);

    /**
     * Creates a new Value object of the specified double value.
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::of(value);
     * </pre>
     */
    static std::unique_ptr<Value> of(double value);

    /**
     * Creates a new Value object of the specified string value.
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::of(value);
     * </pre>
     */
    static std::unique_ptr<Value> of(const std::string& value);

    /**
     * Creates a new Value object of the specified DateTime value.
     *
     * <h3>Examples</h3>
     * <pre>
     * Value::of(value);
     * </pre>
     */
    static std::unique_ptr<Value> of(DateTime value);

private:
    Value(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
