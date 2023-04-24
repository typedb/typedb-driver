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

import {After, Before, BeforeAll} from "@cucumber/cucumber";
import {TypeDB, TypeDBClient, TypeDBCredential, TypeDBOptions} from "../../../dist";
import {
    afterBase,
    beforeBase,
    client,
    createDefaultClient,
    setClientFn,
    setDefaultClientFn,
    setSessionOptions,
    setTransactionOptions
} from "./ConnectionStepsBase";
import assert from "assert";

BeforeAll(async () => {
    setDefaultClientFn(async () =>
        TypeDB.clusterClient([TypeDB.DEFAULT_ADDRESS], new TypeDBCredential("admin", "password", process.env.ROOT_CA))
    )
    setClientFn(async (username, password) => {
        return TypeDB.clusterClient([TypeDB.DEFAULT_ADDRESS], new TypeDBCredential(username, password, process.env.ROOT_CA))
    });
    setSessionOptions(TypeDBOptions.cluster({"infer": true}));
    setTransactionOptions(TypeDBOptions.cluster({"infer": true}));
});

Before(async () => {
    await beforeBase();
    await clearDB();
});

After(async () => {
    await afterBase();
    await clearDB()
});

async function clearDB() {
    // TODO: reset the database through the TypeDB runner once it exists
    await createDefaultClient();
    const databases = await client.databases.all();
    for (const db of databases) {
        await db.delete();
    }
    assert(client.isCluster());
    const users = await (client as TypeDBClient.Cluster).users.all();
    for (const user of users) {
        if (user.username != "admin") {
            await (client as TypeDBClient.Cluster).users.delete(user.username);
        }
    }
    await client.close();
}
