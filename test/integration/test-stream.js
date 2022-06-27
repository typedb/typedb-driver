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

const { TypeDB, SessionType, TransactionType, TypeDBOptions } = require("../../dist");
const TYPEDB = "typedb";

async function run() {
    const client = TypeDB.coreClient();
    let session, tx;
    try {
        const typedb = (await client.databases.all()).find(x => x.name === TYPEDB);
        if (typedb) await typedb.delete();
        await client.databases.create(TYPEDB);

        session = await client.session(TYPEDB, SessionType.SCHEMA);
        tx = await session.transaction(TransactionType.WRITE);

        for (let i = 0; i < 51; i++) {
            await tx.query.define(`define person sub entity, owns name${i}; name${i} sub attribute, value string;`);
        }
        await tx.commit();
        await session.close();

        const txOptions = TypeDBOptions.core({ prefetch: true, prefetchSize: 50 });
        for (let i = 0; i < 50; i++) {
            session = await client.session(TYPEDB, SessionType.DATA);
            tx = await session.transaction(TransactionType.READ, txOptions);
            const personType = (await tx.concepts.getEntityType("person")).asRemote(tx);
            await personType.getOwns(false).collect();
            await tx.query.match("match $x sub thing; limit 1;").first();
        }
        console.log("SUCCESS - completed 50 test runs");
    } catch (err) {
        console.error(`ERROR: ${err.stack || err}`);
        process.exit(1);
    } finally {
        await client.close();
    }
}

run();
