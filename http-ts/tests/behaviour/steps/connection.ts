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

import { Given, Then, When, DataTable } from "@cucumber/cucumber";
import { assertNotError, checkMayError, EXPECT_ERROR_CONTAINING, MayError } from "./params";
import {
    closeConnection, DEFAULT_HOST,
    DEFAULT_PASSWORD, DEFAULT_PORT,
    DEFAULT_USERNAME,
    driver,
    openAndTestConnection, openAndTestConnectionWithAddresses,
    openAndTestConnectionWithHostPort, openAndTestSingleConnection
} from "./context";
import assert from "assert";
import { Server } from "../../../dist/index.cjs";

Given("typedb starts", () => {});
Given("connection is open: {boolean}", (_) => {});

async function connectionOpens(username: string, password: string, mayError: MayError) {
    await openAndTestConnection(username, password).then(checkMayError(mayError));
}
Given("connection opens with username '{word}', password {string}{may_error}", connectionOpens);
Given(`connection opens with username '{word}', password {string}${EXPECT_ERROR_CONTAINING}`, connectionOpens);
Given("connection opens with default authentication", () => connectionOpens(DEFAULT_USERNAME, DEFAULT_PASSWORD, false))

async function connectionOpensSingle(username: string, password: string, mayError: MayError) {
    await openAndTestSingleConnection(username, password).then(checkMayError(mayError));
}
Given("connection opens to single server with default authentication{may_error}", (mayError: MayError) => connectionOpensSingle(DEFAULT_USERNAME, DEFAULT_PASSWORD, mayError))

When(`connection opens with a wrong host${EXPECT_ERROR_CONTAINING}`, (_: string) => {
    assert.rejects(async () => {
        await openAndTestConnectionWithHostPort(DEFAULT_USERNAME, DEFAULT_PASSWORD, "surely-not-localhost", DEFAULT_PORT);
    });
});
When("connection opens with a wrong port; fails", async () => {
    assert.rejects(async () => {
        await openAndTestConnectionWithHostPort(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_HOST, 0);
    });
});

Then("connection has {int} user(s)", async (expectedUserCount: number) => {
    const usersListRes = await driver.getUsers().then(assertNotError);
    const userCount = usersListRes.ok.users.length;
    assert.equal(userCount, expectedUserCount);
});

When("connection closes", closeConnection);

async function getServers(): Promise<Server[]> {
    const res = await driver.getServers().then(assertNotError);
    return res.ok.servers;
}

Then("connection has {int} server(s)", async (expectedCount: number) => {
    const servers = await getServers();
    assert.equal(servers.length, expectedCount, `Expected ${expectedCount} servers but got ${servers.length}`);
});

Then("connection primary server exists", async () => {
    const servers = await getServers();
    const primary = servers.find(s => s.replicaStatus?.replicaRole === "primary");
    assert.ok(primary, "No primary server found");
});

Then("connection get server\\({word}) exists", async (address: string) => {
    const servers = await getServers();
    const server = servers.find(s => s.address === address);
    assert.ok(server, `Replica with address ${address} not found`);
});

Then("connection get server\\({word}) does not exist", async (address: string) => {
    const servers = await getServers();
    const server = servers.find(s => s.address === address);
    assert.ok(!server, `Replica with address ${address} should not exist`);
});

Then("connection get server\\({word}) has term", async (address: string) => {
    const servers = await getServers();
    const server = servers.find(s => s.address === address);
    assert.ok(server, `Replica with address ${address} not found`);
    assert.ok(typeof server.replicaStatus?.term === 'number', `Replica ${address} has no term`);
});

Then("connection servers have roles:", async (dataTable: DataTable) => {
    const servers = await getServers();
    const rows = dataTable.hashes();

    for (const row of rows) {
        const expectedAddress = row['address'];
        const expectedIsPrimary = row['is_primary'] === 'true';

        const server = servers.find(s => s.address === expectedAddress);
        assert.ok(server, `Replica with address ${expectedAddress} not found`);
        const isPrimary = server.replicaStatus?.replicaRole === "primary";
        assert.equal(isPrimary, expectedIsPrimary,
            `Replica ${expectedAddress} isPrimary: expected ${expectedIsPrimary}, got ${isPrimary}`);
    }
});
