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

import { Given, Then } from "@cucumber/cucumber";
import { TypeDB, TypeDBClient, TypeDBCredential, User } from "../../../../dist"
import { client } from "../ConnectionStepsBase";
import assert = require("assert");

Given("users contains: {word}", async (username: string) => {
    const users = await getClient().users.all();
    users.map((user: User) => user.username).includes(username);
});

Then("users not contains: {word}", async (username: string) => {
    const users = await getClient().users.all();
    !users.map((user: User) => user.username).includes(username);
});

Then("users create: {word}, {word}", async (username: string, password: string) => {
    await getClient().users.create(username, password);
});

Then("user password: {word}, {word}", async (username: string, password: string) => {
    const user = await getClient().users.get(username);
    await user.password(password);
});

Then("user connect: {word}, {word}", async (username: string, password: string) => {
    const client = await TypeDB.clusterClient([TypeDB.DEFAULT_ADDRESS], new TypeDBCredential(username, password, process.env.ROOT_CA));
    await client.databases.all()
})

Then("user delete: {word}", async (username: string) => {
    const user = await getClient().users.get(username);
    await user.delete();
});

function getClient(): TypeDBClient.Cluster {
    assert(client.isCluster());
    return client as TypeDBClient.Cluster;
}
