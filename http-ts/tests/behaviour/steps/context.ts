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

import { After, Before } from "@cucumber/cucumber";
import { AnalyzeResponse, isOkResponse, QueryOptions, QueryResponse, TransactionOptions, TransactionType, TypeDBHttpDriver } from "../../../dist/index.cjs";
import { assertNotError } from "./params";
import * as https from "https";
import * as http from "http";
import * as fs from "fs";
import * as path from "path";

export let driver: TypeDBHttpDriver;
let transactionID: string;
let backgroundTransactionID: string;
let transactionOptions: TransactionOptions = {};
let queryOptions: QueryOptions = {};
export let analyzed: AnalyzeResponse;
export let answers: QueryResponse;
export let concurrentAnswers: QueryResponse[];

export async function openTransaction(database: string, type: TransactionType) {
    const res = await driver.openTransaction(database, type, transactionOptions);
    if (isOkResponse(res)) transactionID = res.ok.transactionId;
    return res;
}

export async function openBackgroundTransaction(type: TransactionType, database: string) {
    const res = await driver.openTransaction(database, type, transactionOptions);
    if (isOkResponse(res)) backgroundTransactionID = res.ok.transactionId;
    return res
}

export function tx() {
    if (transactionID) return transactionID;
    else throw "Transaction ID undefined";
}

export async function makeQuery(query: string) {
    return await driver.query(tx(), query, queryOptions);
}

export async function doAnalyze(query: string) {
    return await driver.analyze(tx(), query);
}

export function setTransactionTimeoutMillis(timeout: number) {
    transactionOptions.transactionTimeoutMillis = timeout;
}

export function setTransactionSchemaLockAcquireTimeout(timeout: number) {
    transactionOptions.schemaLockAcquireTimeoutMillis = timeout;
}

export function setQueryIncludeInstanceTypes(include: boolean) {
    queryOptions.includeInstanceTypes = include;
}

export function setQueryIncludeQueryStructure(include: boolean) {
    queryOptions.includeQueryStructure = include;
}

export function setQueryAnswerCountLimit(limit: number) {
    queryOptions.answerCountLimit = limit;
}

export function setAnswers(response: QueryResponse) {
    answers = response;
}

export function setAnalyzed(response: AnalyzeResponse) {
    analyzed = response;
}

export function setConcurrentAnswers(answers: QueryResponse[]) {
    concurrentAnswers = answers;
}

export const DEFAULT_USERNAME = "admin";
export const DEFAULT_PASSWORD = "password";
export const DEFAULT_HOST = process.env.TYPEDB_HTTP_HOST || "http://127.0.0.1";
export const DEFAULT_PORT = parseInt(process.env.TYPEDB_HTTP_PORT || "8000");

export const isClusterMode = process.env.TYPEDB_CLUSTER_MODE === "true";
export const CLUSTER_ADDRESSES = [
    "https://127.0.0.1:18000",
    "https://127.0.0.1:28000",
    "https://127.0.0.1:38000"
];

function setupMtlsFetch(): void {
    const rootCaPath = process.env.ROOT_CA;
    if (!rootCaPath) throw new Error("ROOT_CA environment variable must be set");
    const certDir = path.dirname(rootCaPath);
    const ca = fs.readFileSync(rootCaPath);
    const cert = fs.readFileSync(path.join(certDir, "ext-grpc-certificate.pem"));
    const key = fs.readFileSync(path.join(certDir, "ext-grpc-private-key.pem"));

    const customFetch = async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
        const url = typeof input === "string" ? new URL(input) : input instanceof URL ? input : new URL(input.url);
        const isHttps = url.protocol === "https:";

        return new Promise((resolve, reject) => {
            const options: https.RequestOptions = {
                hostname: url.hostname,
                port: url.port || (isHttps ? 443 : 80),
                path: url.pathname + url.search,
                method: init?.method || "GET",
                headers: init?.headers as Record<string, string>,
                ca,
                cert,
                key,
                rejectUnauthorized: true,
            };

            const client = isHttps ? https : http;
            const req = client.request(options, (res) => {
                const chunks: Buffer[] = [];
                res.on("data", (chunk) => chunks.push(chunk));
                res.on("end", () => {
                    const body = Buffer.concat(chunks).toString();
                    const headers = new Headers();
                    Object.entries(res.headers).forEach(([k, value]) => {
                        if (value) headers.set(k, Array.isArray(value) ? value.join(", ") : value);
                    });
                    const status = res.statusCode || 200;
                    const isNullBodyStatus = status === 204 || status === 304;
                    resolve(new Response(isNullBodyStatus ? null : body, {
                        status,
                        statusText: res.statusMessage || "",
                        headers,
                    }));
                });
            });

            req.on("error", reject);
            if (init?.body) req.write(init.body);
            req.end();
        });
    };

    (globalThis as any).fetch = customFetch;
}

if (isClusterMode) setupMtlsFetch();

export async function openAndTestConnection(username: string, password: string) {
    if (isClusterMode) {
        return openAndTestConnectionWithAddresses(username, password, CLUSTER_ADDRESSES);
    }
    return openAndTestConnectionWithHostPort(username, password, DEFAULT_HOST, DEFAULT_PORT);
}

export async function openAndTestSingleConnection(username: string, password: string) {
    if (isClusterMode) {
        return openAndTestConnectionWithAddresses(username, password, [CLUSTER_ADDRESSES[0]]);
    }
    return openAndTestConnectionWithHostPort(username, password, DEFAULT_HOST, DEFAULT_PORT);
}

export async function openAndTestConnectionWithAddresses(username: string, password: string, addresses: string[]) {
    const newDriver = new TypeDBHttpDriver({
        username, password, addresses
    });
    const healthCheck = await newDriver.health();
    if (isOkResponse(healthCheck)) driver = newDriver;
    return healthCheck;
}

export async function openAndTestConnectionWithHostPort(username: string, password: string, host: string, port: number) {
    const newDriver = new TypeDBHttpDriver({
        username, password, addresses: [`${host}:${port}`]
    });
    const healthCheck = await newDriver.health();
    if (isOkResponse(healthCheck)) driver = newDriver;
    return healthCheck;
}

export function closeConnection() {
    if (transactionID) driver.closeTransaction(transactionID).then(assertNotError);
    if (backgroundTransactionID) driver.closeTransaction(backgroundTransactionID).then(assertNotError);
    driver = undefined;
}

export function setDefaultDriver() {
    if (isClusterMode) {
        driver = new TypeDBHttpDriver({username: DEFAULT_USERNAME, password: DEFAULT_PASSWORD, addresses: CLUSTER_ADDRESSES});
    } else {
        driver = new TypeDBHttpDriver({username: DEFAULT_USERNAME, password: DEFAULT_PASSWORD, addresses: [`${DEFAULT_HOST}:${DEFAULT_PORT}`]});
    }
}

Before(resetDB);
After(resetDB);

async function resetDB() {
    setDefaultDriver();

    if (transactionID != undefined) await driver.closeTransaction(transactionID).then(assertNotError);

    if (backgroundTransactionID != undefined) await driver.closeTransaction(backgroundTransactionID).then(assertNotError);

    const dbRes = await driver.getDatabases().then(assertNotError);
    for (const db of dbRes.ok.databases) {
        await driver.deleteDatabase(db.name).then(assertNotError);
    }

    const userRes = await driver.getUsers().then(assertNotError);
    for (const user of userRes.ok.users) {
        if (user.username != DEFAULT_USERNAME) await driver.deleteUser(user.username).then(assertNotError);
    }

    driver = undefined;
    transactionID = undefined;
    backgroundTransactionID = undefined;
    transactionOptions = {};
    queryOptions = {};
    answers = undefined;
    concurrentAnswers = undefined;
}
