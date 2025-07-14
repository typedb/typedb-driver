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
import { isOkResponse, QueryOptions, QueryResponse, TransactionOptions, TransactionType, TypeDBHttpDriver } from "../../../dist";
import { assertNotError } from "./params";

export let driver: TypeDBHttpDriver;
let transactionID: string;
let backgroundTransactionID: string;
let transactionOptions: TransactionOptions = {};
let queryOptions: QueryOptions = {};
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

export function setTransactionTimeoutMillis(timeout: number) {
    transactionOptions.transactionTimeoutMillis = timeout;
}

export function setTransactionSchemaLockAcquireTimeout(timeout: number) {
    transactionOptions.schemaLockAcquireTimeoutMillis = timeout;
}

export function setQueryIncludeInstanceTypes(include: boolean) {
    queryOptions.includeInstanceTypes = include;
}

export function setQueryAnswerCountLimit(limit: number) {
    queryOptions.answerCountLimit = limit;
}

export function setAnswers(response: QueryResponse) {
    answers = response;
}

export function setConcurrentAnswers(answers: QueryResponse[]) {
    concurrentAnswers = answers;
}

export const DEFAULT_USERNAME = "admin";
export const DEFAULT_PASSWORD = "password";
export const DEFAULT_HOST = "http://localhost";
export const DEFAULT_PORT = 1729;

export async function openAndTestConnection(username: string, password: string) {
    return openAndTestConnectionWithHostPort(username, password, DEFAULT_HOST, DEFAULT_PORT);
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
    driver = new TypeDBHttpDriver({username: DEFAULT_USERNAME, password: DEFAULT_PASSWORD, addresses: [`${DEFAULT_HOST}:${DEFAULT_PORT}`]});
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
