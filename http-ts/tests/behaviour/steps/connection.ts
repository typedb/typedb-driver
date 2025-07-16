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

import { Given, Then, When } from "@cucumber/cucumber";
import { assertNotError, checkMayError, EXPECT_ERROR_CONTAINING, MayError } from "./params.js";
import {
    closeConnection, DEFAULT_HOST,
    DEFAULT_PASSWORD, DEFAULT_PORT,
    DEFAULT_USERNAME,
    driver,
    openAndTestConnection,
    openAndTestConnectionWithHostPort
} from "./context.js";
import assert from "assert";

Given("typedb starts", () => {});
Given("connection is open: {boolean}", (_) => {});

async function connectionOpens(username: string, password: string, mayError: MayError) {
    await openAndTestConnection(username, password).then(checkMayError(mayError));
}
Given("connection opens with username '{word}', password {string}{may_error}", connectionOpens);
Given(`connection opens with username '{word}', password {string}${EXPECT_ERROR_CONTAINING}`, connectionOpens);
Given("connection opens with default authentication", () => connectionOpens(DEFAULT_USERNAME, DEFAULT_PASSWORD, false))

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
