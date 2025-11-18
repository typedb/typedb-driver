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

// Backwards compatibility for server versions < 3.7.0
import {
    ConstraintVertexLabel,
    ConstraintVertexVariable,
    ConstraintSpan,
    VariableInfo, ConstraintAny,
} from "./analyzed-conjunction";
import {ConceptRowAnswer, QueryType} from "./response";

/// Returned if server version < 3.7.0.
/// Explicitly cast to either ConceptRowsQueryResponse or ConceptRowsQueryResponseLegacy.
export interface ConceptRowsQueryResponseLegacy {
    answerType: "conceptRows";
    queryType: QueryType;
    comment: string | null;
    query: QueryStructureLegacy | null;
    answers: ConceptRowAnswer[];
}

export interface QueryConjunctionLegacy {
    constraints: ConstraintAny[] | ConstraintExpressionLegacy | ConstraintLinksLegacy
}

export interface QueryStructureLegacy {
    blocks: QueryConjunctionLegacy[],
    variables: { [name: string]: VariableInfo },
    outputs: string[],
}

export interface ConstraintLinksLegacy {
    tag: "links",
    textSpan: ConstraintSpan,

    relation: ConstraintVertexVariable,
    player: ConstraintVertexVariable,
    role: ConstraintVertexVariable | ConstraintVertexLabel,
}

export interface ConstraintExpressionLegacy {
    tag: "expression",
    textSpan: ConstraintSpan,

    text: string,
    arguments: ConstraintVertexVariable[],
    assigned: ConstraintVertexVariable[],
}
