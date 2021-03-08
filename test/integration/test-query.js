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

const { GraknClient, SessionType, TransactionType } = require("../../dist/GraknClient");
const { GraknOptions } = require("../../dist/GraknOptions");
const assert = require("assert");

async function run() {
    const client = GraknClient.core();
    try {
        const dbs = await client.databases().all();
        console.log(`get databases - SUCCESS - the databases are [${dbs}]`);
        const grakn = dbs.find(x => x.name() === "grakn");
        if (grakn) {
            await grakn.delete();
            console.log(`delete database - SUCCESS - 'grakn' has been deleted`);
        }
        await client.databases().create("grakn");
        console.log("create database - SUCCESS - 'grakn' has been created");
    } catch (err) {
        console.error(`database operations - ERROR: ${err.stack || err}`);
        client.close();
        process.exit(1);
    }

    let session;
    let tx;
    try {
        session = await client.session("grakn", SessionType.SCHEMA);
        tx = await session.transaction(TransactionType.WRITE);
        console.log("open schema write tx - SUCCESS");
    } catch (err) {
        console.error(`open schema write tx - ERROR: ${err.stack || err}`);
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await tx.query().define("define name sub attribute, value string;")
        await tx.query().define("define rank sub attribute, value string;")
        await tx.query().define("define power-level sub attribute, value double;")
        console.log("define attributes query - SUCCESS");
    } catch (err) {
        console.error(`define attributes query - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await tx.commit();
        console.log("commit schema write tx - SUCCESS");
    } catch (err) {
        console.error(`commit schema write tx - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await tx.close();
        console.log("close data write tx - SUCCESS");
    } catch (err) {
        console.error(`close data write tx - ERROR: ${err.stack || err}`);
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await tx.query().define("define lionfight sub relation, relates victor, relates loser;")
        await tx.commit();
        await tx.close();
        console.log("define relationship query - SUCCESS");
    } catch (err) {
        console.error(`define relationship query - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await tx.query().define("define lion sub entity, owns name, owns rank, owns power-level, plays lionfight:victor, plays lionfight:loser;")
        await tx.commit();
        await tx.close();
        console.log("define entity query - SUCCESS");
    } catch (err) {
        console.error(`define entity query - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        await tx.query().define("define giraffe sub entity, owns name, plays lionfight:victor;")
        await tx.query().undefine("undefine giraffe plays lionfight:victor;")
        await tx.commit();
        await tx.close();
        console.log("define/undefine entity query - SUCCESS");
    } catch (err) {
        console.error(`define/undefine entity query - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await session.close();
        console.log("close schema session - SUCCESS");
    } catch (err) {
        console.error(`close schema session - ERROR: ${err.stack || err}`);
        client.close();
        process.exit(1);
    }
    try {
        session = await client.session("grakn", SessionType.DATA, GraknOptions.core({
            sessionIdleTimeoutMillis: 3600000,
        }));
        console.log("open data session - SUCCESS");
    } catch (err) {
        console.error(`open data session - ERROR: ${err.stack || err}`);
        client.close();
        process.exit(1);
    }

    try {
        tx = await session.transaction(TransactionType.WRITE);
        console.log("open data transaction - SUCCESS");
    } catch (err) {
        console.error(`open data write transaction - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        let firstLionStream = await tx.query().insert("insert $x isa lion, has name \"Steve\", has rank \"Duke\", has power-level 12;");
        let lionCollection = await firstLionStream.collect()
        assert(lionCollection.length === 1);
        await tx.query().insert("insert $x isa lion, has name \"Chandra\", has rank \"Baron\", has power-level 7;");
        await tx.query().insert("insert $x isa lion, has name \"Asuka\", has rank \"Duchess\", has power-level 3;");
        await tx.query().insert("insert $x isa lion, has name \"Sergey\", has rank \"Lowborn\", has power-level 13;");
        await tx.query().insert("insert $x isa lion, has name \"Amélie\", has rank \"Marchioness\", has power-level 20;");
        let lionType = await tx.concepts().getEntityType("lion");
        let nameType = await tx.concepts().getAttributeType("name");
        let lionNames = [];
        for await (let lion of lionType.asRemote(tx).getInstances()) {
            for await (let lionName of lion.asRemote(tx).getHas(nameType)) {
                lionNames.push(lionName.getValue());
            }
        }
        // await tx.commit();
        // await tx.close();
        // tx = await session.transaction(TransactionType.WRITE);
        // const matchresult = await tx.query().match("match $x isa lion, has name $y, has rank $z;");
        // for await (const match of matchresult) {
        //     console.log(match);
        // }
        assert(JSON.stringify(lionNames.sort()) === JSON.stringify(["Amélie", "Asuka", "Chandra", "Sergey", "Steve"]))
        console.log("insert entity query - SUCCESS");
    } catch (err) {
        console.error(`insert entity query - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await tx.commit()
        console.log("commit data write transaction - SUCCESS");
    } catch (err) {
        console.error(`commit data write transaction - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await tx.close()
        console.log("close data write transaction - SUCCESS");
    } catch (err) {
        console.error(`close data write transaction - ERROR: ${err.stack || err}`);
        await tx.close();
        await session.close();
        client.close();
        process.exit(1);
    }

    try {
        await session.close();
        console.log("close data session - SUCCESS");
    } catch (err) {
        console.error(`close data session - ERROR: ${err.stack || err}`);
        client.close();
        process.exit(1);
    }

    try {
        client.close();
        console.log("client.close - SUCCESS");
    } catch (err) {
        console.error(`client.close - ERROR: ${err.stack || err}`);
        process.exit(1);
    }
}

run();
