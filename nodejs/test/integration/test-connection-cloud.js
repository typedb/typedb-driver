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

const { TypeDB, SessionType, TransactionType, TypeDBCredential } = require("../../dist");
const assert = require("assert");

async function run() {
    try {
        const driver = await TypeDB.cloudDriver(
            {
                "localhost:1729": "localhost:11729",
                "localhost:21729": "localhost:1729",
                "localhost:31729": "localhost:31729",
            },
            new TypeDBCredential("admin", "password", process.env.ROOT_CA)
        );

        const dbs = await driver.databases.all();
        const typedb = dbs.find(x => x.name === "typedb");
        if (typedb) {
            await typedb.delete();
        }
        await driver.databases.create("typedb");
        const session = await driver.session("typedb", SessionType.DATA);
        const tx = await session.transaction(TransactionType.WRITE);

        const root = await tx.concepts.getRootEntityType();
        const subtypes = await root.getSubtypes(tx).collect();
        assert(subtypes.length === 1);

        await tx.close();
        await session.close();
        await driver.close();
    } catch (err) {
        console.error(`ERROR: ${err.stack || err}`);
        process.exit(1);
    }
}

run();

