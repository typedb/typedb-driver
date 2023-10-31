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

import {Stream} from "../../common/util/Stream";
import {ConceptMap} from "../answer/ConceptMap";
import {ConceptMapGroup} from "../answer/ConceptMapGroup";
import {ValueGroup} from "../answer/ValueGroup";
import {TypeDBOptions} from "../connection/TypeDBOptions";
import {Explanation} from "../logic/Explanation";
import {Value} from "../concept/value/Value";
import {JSONObject} from "../answer/JSON";

/** Provides methods for executing TypeQL queries in the transaction. */
export interface QueryManager {
    /**
     * Performs a TypeQL Get query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.get(query, options)
     * ```
     *
     * @param query - The TypeQL Get query to be executed
     * @param options - Specify query options
     */
    get(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    /**
     * Performs a TypeQL Get Aggregate query in the transaction. Returns an empty promise if the aggregate
     * was not well-defined (such as stddev of 0 elements).
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.getAggregate(query, options)
     * ```
     *
     * @param query - The TypeQL Get Aggregate query to be executed
     * @param options - Specify query options
     */
    getAggregate(query: string, options?: TypeDBOptions): Promise<Value | undefined>;

    /**
     * Performs a TypeQL Get Group query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.getGroup(query, options)
     * ```
     *
     * @param query - The TypeQL Get Group query to be executed
     * @param options - Specify query options
     */
    getGroup(query: string, options?: TypeDBOptions): Stream<ConceptMapGroup>;

    /**
     * Performs a TypeQL Get Group Aggregate query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.getGroupAggregate(query, options)
     * ```
     *
     * @param query - The TypeQL Get Group Aggregate query to be executed
     * @param options - Specify query options
     */
    getGroupAggregate(query: string, options?: TypeDBOptions): Stream<ValueGroup>;

    /**
     * Performs a TypeQL Fetch query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.fetch(query, options)
     * ```
     *
     * @param query - The TypeQL Fetch query to be executed
     * @param options - Specify query options
     */
    fetch(query: string, options?: TypeDBOptions): Stream<JSONObject>;

    /**
     * Performs a TypeQL Insert query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.insert(query, options)
     * ```
     *
     * @param query - The TypeQL Insert query to be executed
     * @param options - Specify query options
     */
    insert(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    /**
     * Performs a TypeQL Delete query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.delete(query, options)
     * ```
     *
     * @param query - The TypeQL Delete query to be executed
     * @param options - Specify query options
     */
    delete(query: string, options?: TypeDBOptions): Promise<void>;

    /**
     * Performs a TypeQL Update query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.update(query, options)
     * ```
     *
     * @param query - The TypeQL Update query to be executed
     * @param options - Specify query options
     */
    update(query: string, options?: TypeDBOptions): Stream<ConceptMap>;

    /**
     * Performs a TypeQL Define query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.define(query, options)
     * ```
     *
     * @param query - The TypeQL Define query to be executed
     * @param options - Specify query options
     */
    define(query: string, options?: TypeDBOptions): Promise<void>;

    /**
     * Performs a TypeQL Undefine query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.undefine(query, options)
     * ```
     *
     * @param query - The TypeQL Undefine query to be executed
     * @param options - Specify query options
     */
    undefine(query: string, options?: TypeDBOptions): Promise<void>;

    /**
     * Performs a TypeQL Explain query in the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.query.explain(explainable, options)
     * ```
     *
     * @param explainable - The Explainable to be explained
     * @param options - Specify query options
     */
    explain(explainable: ConceptMap.Explainable, options?: TypeDBOptions): Stream<Explanation>;
}
