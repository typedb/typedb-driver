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

import { Database, User } from "./index";
import { Concept } from "./concept";
import { QueryStructure } from "./query-structure";

export interface SignInResponse {
    token: string;
}

export type Distribution = `TypeDB Cluster` | `TypeDB CE`;

export interface VersionResponse {
    distribution: Distribution;
    version: string;
}

export interface DatabasesListResponse {
    databases: Database[];
}

export interface UsersListResponse {
    users: User[];
}

export interface TransactionOpenResponse {
    transactionId: string;
}

export type QueryType = "read" | "write" | "schema";

export type AnswerType = "ok" | "conceptRows" | "conceptDocuments";

export interface ConceptRow {
    [varName: string]: Concept | undefined;
}

export interface ConceptRowAnswer {
    involvedBlocks: number[];
    data: ConceptRow;
}

export type ConceptDocument = Object;

export type Answer = ConceptRowAnswer | ConceptDocument;

export interface QueryResponseBase {
    answerType: AnswerType;
    queryType: QueryType;
    comment: string | null;
    query: QueryStructure | null;
}

export interface OkQueryResponse extends QueryResponseBase {
    answerType: "ok";
}

export interface ConceptRowsQueryResponse extends QueryResponseBase {
    answerType: "conceptRows";
    answers: ConceptRowAnswer[];
}

export interface ConceptDocumentsQueryResponse extends QueryResponseBase {
    answerType: "conceptDocuments";
    answers: ConceptDocument[];
}

export type QueryResponse = OkQueryResponse | ConceptRowsQueryResponse | ConceptDocumentsQueryResponse;

export type ApiOkResponse<OK_RES = {}> = { ok: OK_RES };

export type ApiError = { code: string; message: string };

export interface ApiErrorResponse {
    err: ApiError;
    status: number;
}

export function isApiError(err: any): err is ApiError {
    return err != null && typeof err.code === "string" && typeof err.message === "string";
}

export type ApiResponse<OK_RES = {} | null> = ApiOkResponse<OK_RES> | ApiErrorResponse;

export function isOkResponse<OK_RES>(res: ApiResponse<OK_RES>): res is ApiOkResponse<OK_RES> {
    return "ok" in res;
}

export function isApiErrorResponse(res: ApiResponse): res is ApiErrorResponse {
    return "err" in res;
}
