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

import { Type, Value } from "./concept";

export type QueryVertexKind = "variable" | "label" | "value";

export interface QueryVertexVariable {
    tag: "variable";
    id: string,
}

export interface QueryVertexLabel {
    tag: "label";
    type: Type;
}

export interface QueryVertexValue {
    tag: "value";
    value: Value;
}

export type QueryVertex = QueryVertexVariable | QueryVertexLabel | QueryVertexValue;

export type QueryStructure = {
    blocks: { constraints: QueryConstraintAny[] }[],
    variables: {[name: string]: QueryVariableInfo },
    outputs: string[],
};

export function get_variable_name(structure: QueryStructure, variable: QueryVertexVariable) : string | null {
    return structure.variables[variable.id]?.name;
}

export type QueryVariableInfo = { name: string | null };

export type QueryConstraintAny = QueryConstraintIsa | QueryConstraintIsaExact | QueryConstraintHas | QueryConstraintLinks |
    QueryConstraintSub | QueryConstraintSubExact | QueryConstraintOwns | QueryConstraintRelates | QueryConstraintPlays |
    QueryConstraintExpression | QueryConstraintFunction | QueryConstraintComparison |
    QueryConstraintIs | QueryConstraintIid | QueryConstraintKind | QueryConstraintValue | QueryConstraintLabel;

export type QueryConstraintSpan = { begin: number, end: number };

// Instance
export interface QueryConstraintIsa {
    tag: "isa",
    textSpan: QueryConstraintSpan,

    instance: QueryVertexVariable,
    type: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintIsaExact {
    tag: "isa!",
    textSpan: QueryConstraintSpan,

    instance: QueryVertexVariable,
    type: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintHas {
    tag: "has",
    textSpan: QueryConstraintSpan,

    owner: QueryVertexVariable
    attribute: QueryVertexVariable,
}


export interface QueryConstraintLinks {
    tag: "links",
    textSpan: QueryConstraintSpan,

    relation: QueryVertexVariable,
    player: QueryVertexVariable,
    role: QueryVertexVariable | QueryVertexLabel,
}

// Type
export interface QueryConstraintSub {
    tag: "sub",
    textSpan: QueryConstraintSpan,

    subtype: QueryVertexVariable | QueryVertexLabel,
    supertype: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintSubExact {
    tag: "sub!",
    textSpan: QueryConstraintSpan,

    subtype: QueryVertexVariable | QueryVertexLabel,
    supertype: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintOwns {
    tag: "owns",
    textSpan: QueryConstraintSpan,

    owner: QueryVertexVariable | QueryVertexLabel,
    attribute: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintRelates {
    tag: "relates",
    textSpan: QueryConstraintSpan,

    relation: QueryVertexVariable | QueryVertexLabel,
    role: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintPlays {
    tag: "plays",
    textSpan: QueryConstraintSpan,

    player: QueryVertexVariable | QueryVertexLabel,
    role: QueryVertexVariable | QueryVertexLabel,
}

// Function
export interface QueryConstraintExpression {
    tag: "expression",
    textSpan: QueryConstraintSpan,

    text: string,
    arguments: QueryVertexVariable[],
    assigned: QueryVertexVariable[],
}

export interface QueryConstraintFunction {
    tag: "functionCall",
    textSpan: QueryConstraintSpan,

    name: string,
    arguments: QueryVertexVariable[],
    assigned: QueryVertexVariable[],
}

export interface QueryConstraintComparison {
    tag: "comparison",
    textSpan: QueryConstraintSpan,

    lhs: QueryVertexVariable | QueryVertexValue,
    rhs: QueryVertexVariable | QueryVertexValue,
    comparator: string,
}

export interface QueryConstraintIs {
    tag: "is",
    textSpan: QueryConstraintSpan,

    lhs: QueryVertexVariable,
    rhs: QueryVertexVariable,
}

export interface QueryConstraintIid {
    tag: "iid",
    textSpan: QueryConstraintSpan,

    concept: QueryVertexVariable,
    iid: string,
}

export interface QueryConstraintLabel {
    tag: "label",
    textSpan: QueryConstraintSpan,

    type: QueryVertexVariable,
    label: string,
}

export interface QueryConstraintValue {
    tag: "value",
    textSpan: QueryConstraintSpan,

    attributeType: QueryVertexVariable,
    valueType: string,
}

export interface QueryConstraintKind {
    tag: "kind",
    textSpan: QueryConstraintSpan,

    type: QueryVertexVariable,
    kind: string,
}
