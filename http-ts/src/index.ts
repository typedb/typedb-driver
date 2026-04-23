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

import { DriverParams, remoteOrigin, resolveOrigin, allOrigins } from "./params";
import {
    AnalyzeResponse,
    ApiErrorResponse,
    ApiResponse,
    DatabasesListResponse,
    isApiError,
    isMisdirectedError,
    QueryResponse,
    ServersListResponse,
    SignInResponse,
    TransactionOpenResponse,
    UsersListResponse,
    VersionResponse
} from "./response";

const HTTP_UNAUTHORIZED = 401;
const HTTP_MISDIRECTED = 421;

export * from "./analyze";
export * from "./concept";
export * from "./params";
export * from "./analyzed-conjunction";
export * from "./response";

export * from "./legacy";

export class TypeDBHttpDriver {

    private token?: string;
    private currentOrigin: string;

    constructor(private params: DriverParams) {
        this.currentOrigin = remoteOrigin(params);
    }

    getDatabases(): Promise<ApiResponse<DatabasesListResponse>> {
        return this.apiGet<DatabasesListResponse>(`/v1/databases`);
    }

    getDatabase(name: string): Promise<ApiResponse<Database>> {
        return this.apiGet<Database>(`/v1/databases/${encodeURIComponent(name)}`);
    }

    createDatabase(name: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/databases/${encodeURIComponent(name)}`, {});
    }

    deleteDatabase(name: string): Promise<ApiResponse> {
        return this.apiDelete(`/v1/databases/${encodeURIComponent(name)}`);
    }

    getDatabaseSchema(name: string): Promise<ApiResponse<string>> {
        return this.apiGetString(`/v1/databases/${encodeURIComponent(name)}/schema`);
    }

    getDatabaseTypeSchema(name: string): Promise<ApiResponse<string>> {
        return this.apiGetString(`/v1/databases/${encodeURIComponent(name)}/type-schema`);
    }

    getUsers(): Promise<ApiResponse<UsersListResponse>> {
        return this.apiGet<UsersListResponse>(`/v1/users`);
    }

    getCurrentUser(): Promise<ApiResponse<User>> {
        return this.apiGet<User>(`/v1/users/${encodeURIComponent(this.params.username)}`);
    }

    getUser(username: string): Promise<ApiResponse<User>> {
        return this.apiGet<User>(`/v1/users/${encodeURIComponent(username)}`);
    }

    createUser(username: string, password: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/users/${encodeURIComponent(username)}`, { password });
    }

    updateUser(username: string, password: string): Promise<ApiResponse> {
        return this.apiPut(`/v1/users/${encodeURIComponent(username)}`, { password });
    }

    deleteUser(username: string): Promise<ApiResponse> {
        return this.apiDelete(`/v1/users/${encodeURIComponent(username)}`);
    }

    openTransaction(databaseName: string, transactionType: TransactionType, transactionOptions?: TransactionOptions): Promise<ApiResponse<TransactionOpenResponse>> {
        return this.apiPost<TransactionOpenResponse>(`/v1/transactions/open`, { databaseName, transactionType, transactionOptions });
    }

    commitTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${encodeURIComponent(transactionId)}/commit`, {});
    }

    closeTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${encodeURIComponent(transactionId)}/close`, {});
    }

    rollbackTransaction(transactionId: string): Promise<ApiResponse> {
        return this.apiPost(`/v1/transactions/${encodeURIComponent(transactionId)}/rollback`, {});
    }

    analyze(transactionId: string, query: string, analyzeOptions?: AnalyzeOptions): Promise<ApiResponse<AnalyzeResponse>> {
        return this.apiPost<AnalyzeResponse>(`/v1/transactions/${encodeURIComponent(transactionId)}/analyze`, { query, analyzeOptions });
    }

    query(transactionId: string, query: string, queryOptions?: QueryOptions): Promise<ApiResponse<QueryResponse>> {
        return this.apiPost<QueryResponse>(`/v1/transactions/${encodeURIComponent(transactionId)}/query`, { query, queryOptions });
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

    getServers(): Promise<ApiResponse<ServersListResponse>> {
        return this.apiGet<ServersListResponse>(`/v1/servers`);
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
        if (resp.ok) return { ok: await this.stringOrNull(resp) ?? "" };
        else {
            const json = await this.jsonOrNull(resp);
            if (isApiError(json)) return { err: json, status: resp.status };
            else throw resp;
        }
    }

    private async apiReq<BODY>(method: string, path: string, body?: BODY, options?: { headers?: Record<string, string> }): Promise<ApiErrorResponse | Response> {
        // 1. Try current origin
        let result = await this.tryApiReq(method, path, body, options);

        // 2. Connection error → try fallback origins
        if (result === null) {
            result = await this.tryFallbackOrigins(method, path, body, options);
            if (result === null) {
                return { err: { code: "HDR1", message: `Cannot connect to any server` }, status: 503 };
            }
        }

        // 3. 421 Misdirected → switch origin and retry once
        if (result instanceof Response && result.status === HTTP_MISDIRECTED) {
            const switched = await this.handleMisdirected(result);
            if (switched) {
                const retry = await this.tryApiReq(method, path, body, options);
                if (retry !== null) return retry;
            }
            // If misdirected handling or retry failed, try all other origins
            const fallback = await this.tryFallbackOrigins(method, path, body, options);
            if (fallback !== null) return fallback;
            return { err: { code: "HDR1", message: `Cannot connect to any server` }, status: 503 };
        }

        // 4. Non-ok response in a multi-address cluster → try other origins
        //    Handles cases like CSV8/CSV9 errors wrapped in 400 by transaction handlers,
        //    where the server can't provide a 421 redirect.
        if (result instanceof Response && !result.ok && allOrigins(this.params).length > 1) {
            const fallback = await this.tryFallbackOrigins(method, path, body, options);
            if (fallback !== null && fallback instanceof Response && fallback.ok) {
                return fallback;
            }
            // No origin returned a success — return the original error.
            // Note: tryFallbackOrigins may have updated currentOrigin, which helps
            // subsequent calls cycle through addresses.
        }

        return result;
    }

    /** Single request attempt on currentOrigin. Returns null on connection error. */
    private async tryApiReq<BODY>(method: string, path: string, body?: BODY, options?: { headers?: Record<string, string> }): Promise<ApiErrorResponse | Response | null> {
        const url = `${this.currentOrigin}${path}`;
        let tokenResp = await this.getToken();
        if ("err" in tokenResp) {
            // Connection error during token refresh → signal as connection error for fallback
            if (tokenResp.status === 503) return null;
            return tokenResp;
        }
        let bodyString = undefined;
        if (body !== undefined) bodyString = JSON.stringify(body);
        let headers = Object.assign({ "Authorization": `Bearer ${tokenResp.ok.token}`, "Content-Type": "application/json" }, options?.headers || {});
        let resp: Response;
        try {
            resp = await fetch(url, { method, body: bodyString, headers });
        } catch {
            return null;
        }
        if (resp.status === HTTP_UNAUTHORIZED) {
            tokenResp = await this.refreshToken();
            if ("err" in tokenResp) return tokenResp;
            headers = Object.assign({ "Authorization": `Bearer ${tokenResp.ok.token}`, "Content-Type": "application/json" }, options?.headers || {});
            try {
                resp = await fetch(url, { method, body: bodyString, headers });
            } catch {
                return null;
            }
        }
        return resp;
    }

    /** Parse a 421 response body and switch currentOrigin to the indicated primary. Returns true if switch succeeded. */
    private async handleMisdirected(resp: Response): Promise<boolean> {
        let json: any;
        try {
            json = await resp.json();
        } catch {
            return false;
        }
        if (!isMisdirectedError(json)) return false;
        const newOrigin = resolveOrigin(this.params, json.primaryHttpAddress);
        if (newOrigin === this.currentOrigin) return false;
        this.currentOrigin = newOrigin;
        this.token = undefined;
        return true;
    }

    /** Try all configured origins except the one already attempted. */
    private async tryFallbackOrigins<BODY>(method: string, path: string, body?: BODY, options?: { headers?: Record<string, string> }): Promise<ApiErrorResponse | Response | null> {
        const origins = allOrigins(this.params);
        const alreadyTried = this.currentOrigin;
        for (const origin of origins) {
            if (origin === alreadyTried) continue;
            this.currentOrigin = origin;
            this.token = undefined;
            const result = await this.tryApiReq(method, path, body, options);
            if (result === null) continue;
            // Got a response — if it's a 421, follow the redirect
            if (result instanceof Response && result.status === HTTP_MISDIRECTED) {
                const switched = await this.handleMisdirected(result);
                if (switched) {
                    const retry = await this.tryApiReq(method, path, body, options);
                    if (retry !== null) return retry;
                }
                continue;
            }
            return result;
        }
        return null;
    }

    private getToken(): Promise<ApiResponse<SignInResponse>> {
        if (this.token) {
            const resp: ApiResponse<SignInResponse> = { ok: { token: this.token } };
            return Promise.resolve(resp);
        } else return this.refreshToken();
    }

    private async refreshToken(): Promise<ApiResponse<SignInResponse>> {
        const url = `${this.currentOrigin}/v1/signin`;
        const body = { username: this.params.username, password: this.params.password };
        let resp: Response;
        try {
            resp = await fetch(url, { method: "POST", body: JSON.stringify(body), headers: { "Content-Type": "application/json" } });
        } catch {
            return { err: { code: "HDR2", message: `Cannot connect to server at ${this.currentOrigin}` }, status: 503 };
        }
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
        if (!contentLengthRaw) return null;
        const contentLength = parseInt(contentLengthRaw || "");
        if (isNaN(contentLength)) throw `Received invalid Content-Length header: ${contentLengthRaw}`;
        return contentLength > 0 ? await resp.json() : null;
    }

    private async stringOrNull(resp: Response) {
        const contentLengthRaw = resp.headers.get("Content-Length");
        if (!contentLengthRaw) return null;
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
    includeQueryStructure?: boolean;
    answerCountLimit?: number;
}

export interface AnalyzeOptions {
    include_plan?: boolean,
}

export interface User {
    username: string;
}
