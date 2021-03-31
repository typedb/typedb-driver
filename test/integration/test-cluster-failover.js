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

const { Grakn} = require("../../dist/Grakn");
const {GraknSession, SessionType} = require("../../dist/api/GraknSession")
const {GraknTransaction, TransactionType} = require("../../dist/api/GraknTransaction")
const assert = require("assert");
const { spawn, spawnSync } = require("child_process");

async function seekPrimaryReplica(databases) {
    for (let retryNum = 0; retryNum < 10; retryNum++) {
        console.info("Discovering replicas for database 'grakn'...");
        const db = await databases.get("grakn");
        console.info(`Discovered ${db.replicas()}`);
        if (db.primaryReplica()) return db.primaryReplica();
        retryNum++;
        console.info("There is no primary replica yet. Retrying in 2s...");
        await new Promise(resolve => setTimeout(resolve, 2000));
    }
    throw "Retry limit exceeded while seeking a primary replica.";
}

async function run() {
    const client = await Grakn.clusterClient(["localhost:11729", "localhost:21729", "localhost:31729"]);
    try {
        if (await client.databases().contains("grakn")) {
            await (await client.databases().get("grakn")).delete();
        }
        await client.databases().create("grakn");
        let primaryReplica = await seekPrimaryReplica(client.databases());
        console.info(`Performing operations against the primary replica ${primaryReplica}`);

        let session = await client.session("grakn", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        let person = await tx.concepts().putEntityType("person");
        console.info(`Put the entity type '${person.getLabel().scopedName()}'.`);
        await tx.commit();
        tx = await session.transaction(TransactionType.READ);
        person = await tx.concepts().getEntityType("person");
        console.info(`Retrieved entity type with label '${person.getLabel().scopedName()}' from primary replica.`);
        await session.close();

        for (let iteration = 1; iteration <= 10; iteration++) {
            primaryReplica = await seekPrimaryReplica(client.databases());
            console.info(`Stopping primary replica (test ${iteration}/10)...`);
            const port = primaryReplica.address().substring(10,15);
            const lsof = spawnSync("lsof", ["-i", `:${port}`], { stdio: "pipe", encoding: "utf-8" });
            const primaryReplicaServerPID = lsof.stdout.split("\n").filter(s => s.includes("LISTEN")).map(s => s.split(/\s+/)[1])[0];
            console.info(`Primary replica is hosted by server with PID ${primaryReplicaServerPID}`);
            spawnSync("kill", ["-9", primaryReplicaServerPID]);
            console.info("Primary replica stopped successfully.");
            await new Promise(resolve => setTimeout(resolve, 1000));
            session = await client.session("grakn", SessionType.SCHEMA);
            tx = await session.transaction(TransactionType.READ);
            person = await tx.concepts().putEntityType("person");
            console.info(`Retrieved entity type with label '${person.getLabel().scopedName()}' from new primary replica`);
            assert(person.getLabel().scopedName() === "person");
            const idx = primaryReplica.address()[10];
            spawn(`./${idx}/grakn`, ["server", "--data", "server/data", "--address", `127.0.0.1:${idx}1729:${idx}1730`,
                "--peer", "127.0.0.1:11729:11730", "--peer", "127.0.0.1:21729:21730", "--peer", "127.0.0.1:31729:31730"]);
            await new Promise(resolve => setTimeout(resolve, 11000));
        }
        console.info("SUCCESS - completed 10 iterations");
        client.close();
        process.exit(0);
    } catch (err) {
        console.error(`ERROR: ${err.stack || err}`);
        client.close();
        process.exit(1);
    }
}

run();
