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

import { DriverParams, remoteOrigin } from "./params";
import {
    ApiErrorResponse,
    ApiResponse,
    DatabasesListResponse,
    isApiError,
    QueryResponse,
    SignInResponse,
    TransactionOpenResponse,
    UsersListResponse,
    VersionResponse
} from "./response";

const HTTP_UNAUTHORIZED = 401;

export * from "./concept";
export * from "./params";
export * from "./query-structure";
export * from "./response";

export class TypeDBHttpDriver {

    private token?: string;

    constructor(private params: DriverParams) {}

    getDatabases(): Promise<ApiResponse<DatabasesListResponse>> {
        return this.apiGet<DatabasesListResponse>(`/v1/databases`);
    }

    getDatabase(name: String): Promise<ApiResponse<Database>> {
        return this.apiGet<Database>(`/v1/databases/${name}`);
    }

    createDatabase(name: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/databases/${name}`, {});
    }

    deleteDatabase(name: string): Promise<ApiResponse> {
        return this.apiDelete(`/v1/databases/${name}`);
    }

    getDatabaseSchema(name: string): Promise<ApiResponse<string>> {
        return this.apiGetString(`/v1/databases/${name}/schema`);
    }

    getDatabaseTypeSchema(name: string): Promise<ApiResponse<string>> {
        return this.apiGetString(`/v1/databases/${name}/type-schema`);
    }

    getUsers(): Promise<ApiResponse<UsersListResponse>> {
        return this.apiGet<UsersListResponse>(`/v1/users`);
    }

    getUser(username: string): Promise<ApiResponse<User>> {
        return this.apiGet<User>(`/v1/users/${username}`);
    }

    createUser(username: string, password: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/users/${username}`, { password });
    }

    updateUser(username: string, password: string): Promise<ApiResponse> {
        return this.apiPut(`/v1/users/${username}`, { password });
    }

    deleteUser(username: string): Promise<ApiResponse> {
        return this.apiDelete(`/v1/users/${username}`);
    }

    openTransaction(databaseName: string, transactionType: TransactionType, transactionOptions?: TransactionOptions): Promise<ApiResponse<TransactionOpenResponse>> {
        return this.apiPost<TransactionOpenResponse>(`/v1/transactions/open`, { databaseName, transactionType, transactionOptions });
    }

    commitTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${transactionId}/commit`, {});
    }

    closeTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${transactionId}/close`, {});
    }

    rollbackTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${transactionId}/rollback`, {});
    }

    query(transactionId: string, query: string, queryOptions?: QueryOptions): Promise<ApiResponse<QueryResponse>> {
        return this.apiPost<QueryResponse>(`/v1/transactions/${transactionId}/query`, { query, queryOptions });
    }

    oneShotQuery(query: string, commit: boolean, databaseName: string, transactionType: TransactionType, transactionOptions?: TransactionOptions, queryOptions?: QueryOptions) {
        return this.apiPost<QueryResponse>(`/v1/query`, { query, commit, databaseName, transactionType, transactionOptions, queryOptions });
    }

    health(): Promise<ApiResponse> {
        return this.apiGet(`/v1/health`);
    }

    version(): Promise<ApiResponse<VersionResponse>> {
        return this.apiGet(`/v1/version`);
    }

    private async apiGetString(path: string, options?: { headers?: Record<string, string> }): Promise<ApiResponse<string>> {
        return this.stringApiReq("GET", path, options);
    }

    private async apiGet<RES = Object>(path: string, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        return this.jsonApiReqWithoutBody("GET", path, options);
    }

    private async apiDelete<RES = Object>(path: string, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        return this.jsonApiReqWithoutBody("DELETE", path, options);
    }

    private async apiPost<RES = Object, BODY = Object>(path: string, body: BODY, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        return this.jsonApiReq<RES, BODY>("POST", path, body, options);
    }

    private async apiPut<RES = Object, BODY = Object>(path: string, body: BODY, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        return this.jsonApiReq<RES, BODY>("PUT", path, body, options);
    }

    private async jsonApiReqWithoutBody<RES>(method: string, path: string, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        return this.jsonApiReq<RES, Object>(method, path, undefined, options);
    }

    private async jsonApiReq<RES, BODY>(method: string, path: string, body?: BODY, options?: { headers?: Record<string, string> }): Promise<ApiResponse<RES>> {
        const resp = await this.apiReq(method, path, body, options);
        if ("err" in resp) return resp;
        const json = await this.jsonOrNull(resp);
        if (resp.ok) return { ok: json as RES };
        else if (isApiError(json)) return { err: json, status: resp.status };
        else throw resp;
    }

    private async stringApiReq(method: string, path: string, options?: { headers?: Record<string, string> }): Promise<ApiResponse<string>> {
        const resp = await this.apiReq(method, path, undefined, options);
        if ("err" in resp) return resp;
        if (resp.ok) return { ok: await this.stringOrNull(resp) };
        else {
            const json = await this.jsonOrNull(resp);
            if (isApiError(json)) return { err: json, status: resp.status };
            else throw resp;
        }
    }

    private async apiReq<BODY>(method: string, path: string, body?: BODY, options?: { headers?: Record<string, string> }): Promise<ApiErrorResponse | Response> {
        const url = `${remoteOrigin(this.params)}${path}`;
        let tokenResp = await this.getToken();
        if ("err" in tokenResp) return tokenResp;
        let bodyString = undefined;
        if (body !== undefined) bodyString = JSON.stringify(body)
        let headers = Object.assign({ "Authorization": `Bearer ${tokenResp.ok.token}`, "Content-Type": "application/json" }, options?.headers || {});
        let resp = await fetch(url, { method, body: bodyString, headers });
        if (resp.status === HTTP_UNAUTHORIZED) {
            tokenResp = await this.refreshToken();
            if ("err" in tokenResp) return tokenResp;
            headers = Object.assign({ "Authorization": `Bearer ${tokenResp.ok.token}`, "Content-Type": "application/json" }, options?.headers || {});
            resp = await fetch(url, { method, body: bodyString, headers });
        }
        return resp;
    }

    private getToken(): Promise<ApiResponse<SignInResponse>> {
        if (this.token) {
            const resp: ApiResponse<SignInResponse> ={ ok: { token: this.token } };
            return Promise.resolve(resp);
        } else return this.refreshToken();
    }

    private async refreshToken(): Promise<ApiResponse<SignInResponse>> {
        const url = `${remoteOrigin(this.params)}/v1/signin`;
        const body = { username: this.params.username, password: this.params.password };
        const resp = await fetch(url, { method: "POST", body: JSON.stringify(body), headers: { "Content-Type": "application/json" } });
        const json = await this.jsonOrNull(resp);
        if (resp.ok) {
            this.token = (json as SignInResponse).token;
            return { ok: json };
        } else if (isApiError(json)) {
            return { err: json, status: resp.status };
        } else throw resp;
    }

    private async jsonOrNull(resp: Response) {
        const contentLengthRaw = resp.headers.get("Content-Length");
        const contentLength = parseInt(contentLengthRaw || "");
        if (isNaN(contentLength)) throw `Received invalid Content-Length header: ${contentLengthRaw}`;
        return contentLength > 0 ? await resp.json() : null;
    }

    private async stringOrNull(resp: Response) {
        const contentLengthRaw = resp.headers.get("Content-Length");
        const contentLength = parseInt(contentLengthRaw || "");
        if (isNaN(contentLength)) throw `Received invalid Content-Length header: ${contentLengthRaw}`;
        return contentLength > 0 ? await resp.text() : null;
    }
}

export interface Database {
    name: string;
}

export type TransactionType = "read" | "write" | "schema";

export interface TransactionOptions {
    schemaLockAcquireTimeoutMillis?: number;
    transactionTimeoutMillis?: number;
}

export interface QueryOptions {
    includeInstanceTypes?: boolean;
    answerCountLimit?: number;
}

export interface User {
    username: string;
}
