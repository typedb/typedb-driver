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

export interface ConceptMap {
    variables(): IterableIterator<string>;

    concepts(): IterableIterator<Concept>

    get(variable: string): Concept;

    readonly explainables: ConceptMap.Explainables;

    toJSONRecord(): Record<string, Record<string, boolean | string | number>>;
}

export namespace ConceptMap {
    export interface Explainables {
        relation(variable: string): Explainable;

        attribute(variable: string): Explainable;

        ownership(owner: string, attribute: string): Explainable;

        readonly relations: Map<string, Explainable>;

        readonly attributes: Map<string, Explainable>;

        readonly ownerships: Map<[string, string], Explainable>;
    }

    export interface Explainable {
        readonly conjunction: string;

        readonly id: number;
    }
}
