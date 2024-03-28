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

import {After, Before, BeforeAll} from "@cucumber/cucumber";
import {TypeDB, TypeDBOptions} from "../../../dist";
import {
    afterBase,
    beforeBase,
    driver,
    createDefaultDriver,
    setDriverFn,
    setDefaultDriverFn,
    setSessionOptions,
    setTransactionOptions
} from "./ConnectionStepsBase";

BeforeAll(() => {
    setDriverFn(async (username, password) => {
        throw new Error("Core driver does not support authentication");
    });
    setDefaultDriverFn(async () => TypeDB.coreDriver());
    setSessionOptions(new TypeDBOptions({"infer": true}));
    setTransactionOptions(new TypeDBOptions({"infer": true}));
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
    await createDefaultDriver();
    const databases = await driver.databases.all();
    for (const db of databases) {
        await db.delete();
    }
    await driver.close();
}
