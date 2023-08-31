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

import {Then} from "@cucumber/cucumber";
import {User} from "../../../../dist"
import {client} from "../ConnectionStepsBase";
import {assertThrows} from "../../util/Util";

Then("get connected user", async () => {
    await client.user();
});

Then("users contains: {words}", async (username: string) => {
    const users = await client.users.all();
    users.map((user: User) => user.username).includes(username);
});

Then("users contains: {words}; throws exception", async (username: string) => {
    await assertThrows(() => client.users.all());
});

Then("users not contains: {words}", async (username: string) => {
    const users = await client.users.all();
    !users.map((user: User) => user.username).includes(username);
});

Then("users create: {words}, {words}", async (username: string, password: string) => {
    await client.users.create(username, password);
});

Then("users create: {words}, {words}; throws exception", async (username: string, password: string) => {
    await assertThrows(() => client.users.create(username, password));
});

Then('users get all', async () => {
    await client.users.all();
});

Then('users get all; throws exception', async () => {
    await assertThrows(() => client.users.all());
});

Then('users get user: {words}', async (username: string) => {
    await client.users.get(username);
});

Then('users get user: {words}; throws exception', async (username: string) => {
    await assertThrows(() => client.users.get(username));
});

Then("users delete: {words}", async (username: string) => {
    await client.users.delete(username);
});

Then("users delete: {words}; throws exception", async (username: string) => {
    await assertThrows(() => client.users.delete(username));
});

Then("users password set: {words}, {words}", async (username: string, password: string) => {
    await client.users.passwordSet(username, password);
});

Then("users password set: {words}, {words}; throws exception", async (username: string, password: string) => {
    await assertThrows(() => client.users.passwordSet(username, password));
});

Then("user password update: {word}, {word}, {word}", async (username: string, passwordOld: string, passwordNew: string) => {
    const user = await client.users.get(username);
    await user.passwordUpdate(passwordOld, passwordNew);
});
