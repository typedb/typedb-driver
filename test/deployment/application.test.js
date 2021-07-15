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

const { TypeDB, SessionType, TransactionType } = require("typedb-client");

jest.setTimeout(15000);

let client;

beforeEach(async () => {
    client = TypeDB.coreClient();
    await client.databases.create("typedb");
})

afterEach(async () => {
    const db = await client.databases.get("typedb");
    await db.delete();
    client.close();
});

describe("Basic TypeDBClient Tests", () => {
    test("define with concepts client", async () => {
        let session = await client.session("typedb", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.concepts.putEntityType("lion");
        await tx.commit();
        return await session.close();
    });

    test("define by running query", async () => {
        let session = await client.session("typedb", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.query.define("define person sub entity, owns name; name sub attribute, value string;");
        await tx.commit();
        return await session.close();
    });

    test("match", async () => {
        let session = await client.session("typedb", SessionType.DATA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.query.match("match $x sub thing;");
        await tx.close();
        return await session.close();
    });
});
