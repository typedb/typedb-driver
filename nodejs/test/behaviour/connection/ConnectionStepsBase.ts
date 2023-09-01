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

import {Given, setDefaultTimeout} from "@cucumber/cucumber";
import {TypeDBClient, TypeDBOptions, TypeDBSession, TypeDBTransaction} from "../../../dist";
import {assertThrows} from "../util/Util";
import assert = require("assert");

interface OptionSetters {
    [index: string]: (options: TypeDBOptions, value: string) => void
}

export const THREAD_POOL_SIZE = 32;

export let clientFn: (username: string, password: string) => Promise<TypeDBClient>;
export let defaultClientFn: () => Promise<TypeDBClient>;

export let client: TypeDBClient;
export const sessions: TypeDBSession[] = [];
export const sessionsToTransactions: Map<TypeDBSession, TypeDBTransaction[]> = new Map<TypeDBSession, TypeDBTransaction[]>();
export let sessionOptions: TypeDBOptions;
export let transactionOptions: TypeDBOptions;
export const optionSetters: OptionSetters = {
    'session-idle-timeout-millis': (options: TypeDBOptions, value: string) => options.sessionIdleTimeoutMillis = Number(value),
    'transaction-timeout-millis': (options: TypeDBOptions, value: string) => options.transactionTimeoutMillis = Number(value)
};


setDefaultTimeout(20000); // Some steps may take longer than the default limit of 5s, eg create parallel dbs

export function tx(): TypeDBTransaction {
    return sessionsToTransactions.get(sessions[0])[0];
}

export function setClientFn(constructor: (username: string, password: string) => Promise<TypeDBClient>) {
    clientFn = constructor;
}

export function setDefaultClientFn(constructor: () => Promise<TypeDBClient>) {
    defaultClientFn = constructor;
}

export async function createDefaultClient(): Promise<void> {
    client = await defaultClientFn();
}

export async function createClient(username: string, password: string) {
    client = await clientFn(username, password);
}

export function setSessionOptions(options: TypeDBOptions) {
    sessionOptions = options;
}

export function setTransactionOptions(options: TypeDBOptions) {
    transactionOptions = options;
}

export async function beforeBase(): Promise<void> {
    sessions.length = 0;
    sessionsToTransactions.clear();
}

export async function afterBase() {
    if (client.isOpen()) {
        await client.close();
    }
}

Given('typedb has configuration', (table: any) => {
    // TODO: prepare a configuration through the TypeDB runner once it exists
})

Given('typedb starts', async () => {
    // TODO: start TypeDB through the TypeDB runner once it exists
})

Given('typedb stops', async () => {
    // TODO: stop TypeDB through the TypeDB runner once it exists
})

Given('connection opens with default authentication', async () => {
    await createDefaultClient();
})

Given('connection opens with authentication: {words}, {words}', async (username: string, password: string) => {
    await createClient(username, password);
})

Given('connection closes', async () => {
    if (client && client.isOpen()) await client.close();
})

Given('connection opens with authentication: {words}, {words}; throws exception', async (username: string, password: string) => {
    await assertThrows(() => createClient(username, password));
})

Given('connection has been opened', () => {
    assert(client);
    assert(client.isOpen());
});
