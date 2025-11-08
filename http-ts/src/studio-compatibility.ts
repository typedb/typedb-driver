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

// Backwards compatibility of studio
import {
    QueryConstraintComparison,
    QueryConstraintFunction,
    QueryConstraintHas, QueryConstraintIid, QueryConstraintIs,
    QueryConstraintIsa,
    QueryConstraintIsaExact, QueryConstraintKind, QueryConstraintLabel,
    QueryConstraintOwns,
    QueryConstraintPlays,
    QueryConstraintRelates, QueryConstraintSpan,
    QueryConstraintSub,
    QueryConstraintSubExact, QueryConstraintValue,
    QueryVariableInfo, QueryVertexLabel, QueryVertexVariable
} from "./analyzed-conjunction";
import {ConceptRowAnswer, QueryType} from "./response";

export interface ConceptRowsQueryResponseForStudio {
    answerType: "conceptRows";
    queryType: QueryType;
    comment: string | null;
    query: QueryStructureForStudio | null;
    answers: ConceptRowAnswer[];
}

export interface QueryStructureForStudio {
    blocks: { constraints: QueryConstraintAnyForStudio[] }[],
    variables: { [name: string]: QueryVariableInfo },
    outputs: string[],
}

export type QueryConstraintAnyForStudio =
    QueryConstraintIsa
    | QueryConstraintIsaExact
    | QueryConstraintHas
    | QueryConstraintLinksForStudio
    | QueryConstraintSub
    | QueryConstraintSubExact
    | QueryConstraintOwns
    | QueryConstraintRelates
    | QueryConstraintPlays
    | QueryConstraintExpressionForStudio
    | QueryConstraintFunction
    | QueryConstraintComparison
    | QueryConstraintIs
    | QueryConstraintIid
    | QueryConstraintKind
    | QueryConstraintValue
    | QueryConstraintLabel;

export interface QueryConstraintLinksForStudio {
    tag: "links",
    textSpan: QueryConstraintSpan,

    relation: QueryVertexVariable,
    player: QueryVertexVariable,
    role: QueryVertexVariable | QueryVertexLabel,
}

export interface QueryConstraintExpressionForStudio {
    tag: "expression",
    textSpan: QueryConstraintSpan,

    text: string,
    arguments: QueryVertexVariable[],
    assigned: QueryVertexVariable[],
}
