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

import {Given, setDefaultTimeout} from "@cucumber/cucumber";
import {TypeDBDriver, TypeDBOptions, TypeDBSession, TypeDBTransaction} from "../../../dist";
import {assertThrows} from "../util/Util";
import assert = require("assert");

interface OptionSetters {
    [index: string]: (options: TypeDBOptions, value: string) => void
}

export const THREAD_POOL_SIZE = 32;

export let driverFn: (username: string, password: string) => Promise<TypeDBDriver>;
export let defaultDriverFn: () => Promise<TypeDBDriver>;

export let driver: TypeDBDriver;
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

export function setDriverFn(constructor: (username: string, password: string) => Promise<TypeDBDriver>) {
    driverFn = constructor;
}

export function setDefaultDriverFn(constructor: () => Promise<TypeDBDriver>) {
    defaultDriverFn = constructor;
}

export async function createDefaultDriver(): Promise<void> {
    driver = await defaultDriverFn();
}

export async function createDriver(username: string, password: string) {
    driver = await driverFn(username, password);
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
    if (driver.isOpen()) {
        await driver.close();
    }
    await createDefaultDriver();
    for (const db of (await driver.databases.all())) {
        await db.delete();
    }
    await driver.close();
}

Given('typedb has configuration', (table: any) => {
    // empty
})

Given('typedb starts', async () => {
    // empty
})

Given('typedb stops', async () => {
    // empty
})

Given('connection opens with default authentication', async () => {
    await createDefaultDriver();
})

Given('connection opens with authentication: {words}, {words}', async (username: string, password: string) => {
    await createDriver(username, password);
})

Given('connection closes', async () => {
    if (driver && driver.isOpen()) await driver.close();
})

Given('connection opens with authentication: {words}, {words}; throws exception', async (username: string, password: string) => {
    await assertThrows(() => createDriver(username, password));
})

Given('connection has been opened', () => {
    assert(driver);
    assert(driver.isOpen());
});
