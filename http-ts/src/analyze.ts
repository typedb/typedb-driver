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

import {QueryConstraintAny, QueryVariableInfo} from "./query-structure";
import {Type, ValueType} from "./concept";


type VariableId = string;
export interface PipelineStructure {
    conjunctions: QueryConstraintAny[][],
    variables: {[name: VariableId]: QueryVariableInfo },
    pipeline: PipelineStage[],
    outputs: string[],
}

type ConjunctionIndex = number;
export type PipelineStage =
    { tag: "match",  block: ConjunctionIndex } |
    { tag: "insert",  block: ConjunctionIndex } |
    { tag: "delete",  block: ConjunctionIndex, deletedVariables: VariableId[] } |
    { tag: "put",  block: ConjunctionIndex } |
    { tag: "update",  block: ConjunctionIndex } |
    { tag: "select",  variables: VariableId[] } |
    { tag: "sort",  variables: VariableId[] } |
    { tag: "require",  variables: VariableId } |
    { tag: "offset",  offset: number } |
    { tag: "limit",  limit: number } |
    { tag: "distinct" } |
    { tag: "reduce",  reducers: { assigned: VariableId, reducer: Reducer }[] };

export interface FunctionStructure {
    arguments: VariableId[],
    body: PipelineStructure,
    "return": FunctionReturnStructure,
}

export type Reducer = { reducer: string, variable: VariableId[] };
export type FunctionSingleReturnSelector = "first" | "last" ;
export type FunctionReturnStructure =
    { tag: "single", variables: VariableId[], selector: FunctionSingleReturnSelector } |
    { tag: "stream", variables: VariableId[] } |
    { tag: "check" } |
    { tag: "reduce", reducers: Reducer[] }

export type VariableAnnotations =
    { tag: "thing", annotations: Type[] } |
    { tag: "type", annotations: Type[] } |
    { tag: "value", valueTypes: ValueType[] };

export interface PipelineAnnotations {
    annotationsByConjunction: {
        variableAnnotations: {[name: VariableId]: VariableAnnotations }
    }[]
}

export interface FunctionAnnotations {
    arguments: VariableAnnotations[],
    returns: { tag: "single" | "stream", annotations: VariableAnnotations[] },
    body: PipelineAnnotations,
}

export type FetchAnnotations =
    { tag:  "list", elements: FetchAnnotations } |
    { tag : "object", "possibleFields": FetchAnnotationFieldEntry[] } |
    { tag : "value", valueTypes: ValueType[] };

export type FetchAnnotationFieldEntry = FetchAnnotations & { key: string };
