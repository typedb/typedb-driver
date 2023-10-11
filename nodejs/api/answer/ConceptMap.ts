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

import {Concept} from "../concept/Concept";

/**
 * Contains a mapping of variables to concepts.
 */
export interface ConceptMap {
    /**
     * Produces an iterator over all variables in this <code>ConceptMap</code>.
     *
     * ### Examples
     *
     * ```ts
     * conceptMap.variables()
     * ```
     */
    variables(): IterableIterator<string>;

    /**
     * Produces an iterator over all concepts in this <code>ConceptMap</code>.
     *
     * ### Examples
     *
     * ```ts
     * conceptMap.concepts()
     * ```
     */
    concepts(): IterableIterator<Concept>

    /**
     * Retrieves a concept for a given variable name.
     *
     * ### Examples
     *
     * ```ts
     * conceptMap.get(variable)
     * ```
     *
     * @param variable - The string representation of a variable
     */
    get(variable: string): Concept;

    /**
     * The <code>Explainables</code> object for this <code>ConceptMap</code>,
     * exposing which of the concepts in this <code>ConceptMap</code> are explainable.
     */
    readonly explainables: ConceptMap.Explainables;

    /**
     * Retrieves this <code>ConceptMap</code> as JSON.
     *
     * ### Examples
     *
     * ```ts
     * conceptMap.toJSONRecord()
     * ```
     */
    toJSONRecord(): Record<string, Record<string, boolean | string | number>>;
}

export namespace ConceptMap {
    /**
     * Contains explainable objects.
     */
    export interface Explainables {
        /**
         * Retrieves the explainable relation with the given variable name.
         *
         * ### Examples
         *
         * ```ts
         * conceptMap.explainables.relation(variable)
         * ```
         *
         * @param variable - The string representation of a variable
         */
        relation(variable: string): Explainable;

        /**
         * Retrieves the explainable attribute with the given variable name.
         *
         * ### Examples
         *
         * ```ts
         * conceptMap.explainables.attribute(variable)
         * ```
         *
         * @param variable - The string representation of a variable
         */
        attribute(variable: string): Explainable;

        /**
         * Retrieves the explainable attribute ownership with the pair of (owner, attribute) variable names.
         *
         * ### Examples
         *
         * ```ts
         * conceptMap.explainables.ownership(owner, attribute)
         * ```
         *
         * @param owner - The string representation of the owner variable
         * @param attribute - The string representation of the attribute variable
         */
        ownership(owner: string, attribute: string): Explainable;

        /**
         * All of this <code>ConceptMap</code>’s explainable relations.
         */
        readonly relations: Map<string, Explainable>;

        /**
         * All of this <code>ConceptMap</code>’s explainable attributes.
         */
        readonly attributes: Map<string, Explainable>;

        /**
         * All of this <code>ConceptMap</code>’s explainable ownerships.
         */
        readonly ownerships: Map<[string, string], Explainable>;
    }

    /**
     * Contains an explainable object.
     */
    export interface Explainable {
        /**
         * The subquery of the original query that is actually being explained.
         */
        readonly conjunction: string;

        /**
         * A unique ID that identifies this <code>Explainable</code>.
         */
        readonly id: number;
    }
}
