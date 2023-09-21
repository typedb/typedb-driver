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

const { TypeDB, SessionType, TransactionType } = require("../../dist");

async function run() {
    const driver = await TypeDB.coreDriver();

    try {
        const dbs = await driver.databases.all();
        console.log(`get databases - SUCCESS - the databases are [${dbs}]`);
        const typedb = dbs.find(x => x.name === "typedb");
        if (typedb) {
            await typedb.delete();
            console.log(`delete database - SUCCESS - 'typedb' has been deleted`);
        }
        await driver.databases.create("typedb");
        console.log("create database - SUCCESS - 'typedb' has been created");
    } catch (err) {
        console.error(`database operations - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let session;
    try {
        session = await driver.session("typedb", SessionType.SCHEMA);
        console.log("open schema session - SUCCESS");
    } catch (err) {
        console.error(`open schema session - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    let tx;
    try {
        tx = await session.transaction(TransactionType.WRITE);
        console.log("open schema write tx - SUCCESS");
    } catch (err) {
        console.error(`open schema write tx - ERROR: ${err.stack || err}`);
        await session.close();
        await driver.close();
        process.exit(1);
    }

    try {
        await tx.close();
        console.log("close schema write tx - SUCCESS");
    } catch (err) {
        console.error(`close schema write tx - ERROR: ${err.stack || err}`);
        await session.close();
        await driver.close();
        process.exit(1);
    }

    try {
        await session.close();
        console.log("close schema session - SUCCESS");
    } catch (err) {
        console.error(`close schema session - ERROR: ${err.stack || err}`);
        await driver.close();
        process.exit(1);
    }

    try {
        await driver.close();
        console.log("driver.close - SUCCESS");
    } catch (err) {
        console.error(`driver.close - ERROR: ${err.stack || err}`);
        process.exit(1);
    }
}

run();
