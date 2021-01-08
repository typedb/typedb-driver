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

import { When, Then, Given } from "@cucumber/cucumber";
import { client, THREAD_POOL_SIZE } from "../ConnectionSteps";
import * as assert from "assert";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import { assertThrows } from "../../util/Util";

When("connection create database: {word}", async (name: string) => {
    await client.databases().create(name)
});

When("connection create database(s):", async (names: DataTable) => {
    for (const name of names.raw()) {await client.databases().create(name[0])}
});

When("connection create databases in parallel:", async (names: DataTable) => {
    assert.ok(THREAD_POOL_SIZE >= names.raw().length);
    const creations: Promise<void>[] = [];
    for (const name of names.raw()) {
        creations.push(client.databases().create(name[0]));
    }
    await Promise.all(creations);
});

When("connection delete database: {word}", async (name: string) => {
    await client.databases().delete(name);
});

When("connection delete database(s):", async (names: DataTable) => {
    for (const name of names.raw()) {
        await client.databases().delete(name[0]);
    }
});

Then("connection delete database; throws exception: {word}", async (name: string) => {
    await assertThrows(async () => await client.databases().delete(name));
});


Then("connection delete database(s); throws exception", async (names: DataTable) => {
    for (const name of names.raw()) {
        await assertThrows(async () => await client.databases().delete(name[0]));
    }
});

When("connection delete databases in parallel:", async (names: DataTable) => {
    assert.ok(THREAD_POOL_SIZE >= names.raw().length);
    const deletions: Promise<void>[] = [];
    for (const name of names.raw()) {
        deletions.push(client.databases().delete(name[0]));
    }
    await Promise.all(deletions);
});

When("connection delete all databases", async () => {
    const databases = await client.databases().all();
    for (const name of databases) {
        await client.databases().delete(name);
    }
});

Then("connection has database: {word}", async (name: string) => {
    const databases = await client.databases().all();
    assert.ok(databases.includes(name));
});

Then("connection has database(s):", async (names: DataTable) => {
    const databases = await client.databases().all();
    names.raw().forEach(name => {
        assert.ok(databases.includes(name[0]));
    });
});

Then("connection does not have database: {word}", async (name: string) => {
    const databases = await client.databases().all();
    assert.ok(!databases.includes(name));
});

Then("connection does not have database(s):", async (names: DataTable) => {
    const databases = await client.databases().all();
    names.raw().forEach(name => {
        assert.ok(!databases.includes(name[0]));
    });
});

Given("connection does not have any database", async () => {
    const databases = await client.databases().all();
    assert.ok(databases.length === 0)
});
