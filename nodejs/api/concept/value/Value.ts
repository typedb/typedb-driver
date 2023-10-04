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


import {Concept} from "../Concept";
import {RequestBuilder} from "../../../common/rpc/RequestBuilder";

export interface Value extends Concept {
    /** The <code>ValueType</code> of this value concept */
    readonly valueType: Concept.ValueType;

    /** Retrieves the value which this value concept holds. */
    readonly value: boolean | string | number | Date;

    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>boolean</code>. Otherwise, returns <code>False</code>.
     *
     * ### Examples
     *
     * ```ts
     * value.isBoolean()
     * ```
     */
    isBoolean(): boolean;
    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>long</code>. Otherwise, returns <code>False</code>.
     *
     * ### Examples
     *
     * ```ts
     * value.isLong()
     * ```
     */
    isLong(): boolean;
    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>double</code>.
     * Otherwise, returns <code>False</code>.
     *
     * ### Examples
     *
     * ```ts
     * value.isDouble()
     * ```
     */
    isDouble(): boolean;
    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>string</code>. Otherwise, returns <code>False</code>.
     *
     * ### Examples
     *
     * ```ts
     * value.isString()
     * ```
     */
    isString(): boolean;
    /**
     * Returns <code>True</code> if the value which this value concept holds is of type <code>datetime</code>. Otherwise, returns <code>False</code>.
     *
     * ### Examples
     *
     * ```ts
     * value.isDatetime()
     * ```
     */
    isDateTime(): boolean;

    /**
     * Returns a <code>boolean</code> value of this value concept. If the value has another type, raises an exception.
     *
     * ### Examples
     *
     * ```ts
     * value.asBoolean()
     * ```
     */
    asBoolean(): boolean;
    /**
     * Returns a <code>number</code> value of this value concept. If the value has another type, raises an exception.
     *
     * ### Examples
     *
     * ```ts
     * value.asLong()
     * ```
     */
    asLong(): number;
    /**
     * Returns a <code>number</code> value of this value concept. If the value has another type, raises an exception.
     *
     * ### Examples
     *
     * ```ts
     * value.asDouble()
     * ```
     */
    asDouble(): number;
    /**
     * Returns a <code>string</code> value of this value concept. If the value has another type, raises an exception.
     *
     * ### Examples
     *
     * ```ts
     * value.asString()
     * ```
     */
    asString(): string;
    /**
     * Returns a <code>datetime</code> value of this value concept. If the value has another type, raises an exception.
     *
     * ### Examples
     *
     * ```ts
     * value.asDatetime()
     * ```
     */
    asDateTime(): Date;
}

export namespace Value {
    export function proto(value: Value) {
        return RequestBuilder.Value.protoValue(value.valueType.proto(), value.value);
    }
}
