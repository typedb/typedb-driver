/*
 * Copyright (C) 2021 Vaticle
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

import { After, Before, Given, setDefaultTimeout } from "@cucumber/cucumber";
import { TypeDBClient, TypeDBSession, TypeDBTransaction } from "../../../dist";
import assert = require("assert");

export const THREAD_POOL_SIZE = 32;

export let client: TypeDBClient;
export const sessions: TypeDBSession[] = [];
export const sessionsToTransactions: Map<TypeDBSession, TypeDBTransaction[]> = new Map<TypeDBSession, TypeDBTransaction[]>();

setDefaultTimeout(20000); // Some steps may take longer than the default limit of 5s, eg create parallel dbs

export function tx(): TypeDBTransaction {
    return sessionsToTransactions.get(sessions[0])[0];
}

export function setClient(value: TypeDBClient) {
    client = value;
}

Given("connection has been opened", () => {
    assert(client);
});

Before(async () => {
    for (const session of sessions) {
        await session.close()
    }
    const databases = await client.databases().all();
    for (const db of databases) {
        await db.delete();
    }
    sessions.length = 0;
    sessionsToTransactions.clear();
});

After(async () => {
    for (const session of sessions) {
        await session.close()
    }
    for (const db of await client.databases().all()) {
        await db.delete();
    }
});
