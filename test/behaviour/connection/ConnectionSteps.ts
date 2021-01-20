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

import { Given, After, Before, setDefaultTimeout, BeforeAll } from "@cucumber/cucumber";
import { GraknClient } from "../../../dist/rpc/GraknClient";
import { Grakn } from "../../../dist/Grakn";
import Session = Grakn.Session;
import Transaction = Grakn.Transaction;
import assert = require("assert");

setDefaultTimeout(20 * 1000);
export const THREAD_POOL_SIZE = 32;

export let client: GraknClient;
export const sessions: Session[] = [];
export const sessionsToTransactions: Map<Session, Transaction[]> = new Map<Session, Transaction[]>();

export function tx(): Transaction {
    return sessionsToTransactions.get(sessions[0])[0];
}

Given("connection has been opened", () => {
    assert(client);
});

BeforeAll(() => {
    client = new GraknClient();
});

Before(async () => {
    for (const session of sessions) {
        await session.close()
    }
    const databases = await client.databases().all();
    for (const name of databases) {
        await client.databases().delete(name);
    }
    sessions.length = 0;
    sessionsToTransactions.clear();
});

After(async () => {
    for (const session of sessions) {
        await session.close()
    }
    for (const name of await client.databases().all()) {
        await client.databases().delete(name);
    }
});
