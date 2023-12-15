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

    static std::string formatDateTime(DateTime t);
    static DateTime parseDateTime(const std::string& s);

    static std::unique_ptr<Value> of(bool value);
    static std::unique_ptr<Value> of(int64_t value);
    static std::unique_ptr<Value> of(double value);
    static std::unique_ptr<Value> of(const std::string& value);
    static std::unique_ptr<Value> of(DateTime value);

private:
    Value(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
