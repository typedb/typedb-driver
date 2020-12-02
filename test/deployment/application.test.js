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

const {GraknClient} = require("grakn-client/rpc/GraknClient");
const {Grakn} = require("grakn-client/Grakn");
const SessionType = Grakn.SessionType;
const TransactionType = Grakn.TransactionType;

jest.setTimeout(15000);

let client;

beforeEach(async () => {
    client = new GraknClient();
    await client.databases().create("thisisadatabase");
})

afterEach(async () => {
    await client.databases().delete("thisisadatabase");
    client.close();
});


describe("Basic GraknClient Tests", () => {
    test("define with concepts client", async () => {
        let session = await client.session("thisisadatabase", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.concepts().putEntityType("lion");
        await tx.commit();
        await session.close();
    });

    test("define by running query", async () => {
        let session = await client.session("thisisadatabase", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.query("define person sub entity, has name; name sub attribute, value string;");
        await tx.commit();
        await session.close();
    });

    test("match", async () => {
        let session = await client.session("thisisadatabase", SessionType.DATA);
        let tx = await session.transaction(TransactionType.WRITE);
        await tx.query("match $x sub thing; get;");
        await tx.close();
        await session.close();
    });
});