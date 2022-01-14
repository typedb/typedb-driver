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

const { TypeDB, SessionType, TransactionType, TypeDBCredential } = require("../../dist");
const { spawn, spawnSync } = require("child_process");
const assert = require("assert");

async function seekPrimaryReplica(databases) {
    for (let retryNum = 0; retryNum < 10; retryNum++) {
        console.info("Discovering replicas for database 'typedb'...");
        const db = await databases.get("typedb");
        console.info(`Discovered ${db.replicas}`);
        if (db.primaryReplica) return db.primaryReplica;
        retryNum++;
        console.info("There is no primary replica yet. Retrying in 2s...");
        await new Promise(resolve => setTimeout(resolve, 2000));
    }
    throw "Retry limit exceeded while seeking a primary replica.";
}

function getServerPID(port) {
    const lsof = spawnSync("lsof", ["-i", `:${port}`], { stdio: "pipe", encoding: "utf-8" });
    const serverPID = lsof.stdout.split("\n").filter(s => s.includes("LISTEN")).map(s => s.split(/\s+/)[1]);
    if (serverPID.length === 1) return serverPID[0];
    else if (serverPID.length === 0) return undefined;
    else throw new Error("Found multiple PIDs: '" + serverPID + " for port: '" + port + "'");
}

async function run() {
    console.log("root ca path: ", process.env.ROOT_CA)
    const client = await TypeDB.clusterClient(
        ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"],
        new TypeDBCredential("admin", "password", process.env.ROOT_CA)
    );
    try {
        if (await client.databases.contains("typedb")) {
            await (await client.databases.get("typedb")).delete();
        }
        await client.databases.create("typedb");
        let primaryReplica = await seekPrimaryReplica(client.databases);
        console.info(`Performing operations against the primary replica ${primaryReplica}`);

        let session = await client.session("typedb", SessionType.SCHEMA);
        let tx = await session.transaction(TransactionType.WRITE);
        let person = await tx.concepts.putEntityType("person");
        console.info(`Put the entity type '${person.label.scopedName}'.`);
        await tx.commit();
        tx = await session.transaction(TransactionType.READ);
        person = await tx.concepts.getEntityType("person");
        console.info(`Retrieved entity type with label '${person.label.scopedName}' from primary replica.`);
        await session.close();

        for (let iteration = 1; iteration <= 10; iteration++) {
            primaryReplica = await seekPrimaryReplica(client.databases);
            console.info(`Stopping primary replica (test ${iteration}/10)...`);
            const port = primaryReplica.address.substring(10,15);
            const primaryReplicaServerPID = getServerPID(port);
            console.info(`Primary replica is hosted by server with PID ${primaryReplicaServerPID}`);
            spawnSync("kill", ["-9", primaryReplicaServerPID]);
            console.info("Primary replica stopped successfully.");
            await new Promise(resolve => setTimeout(resolve, 1000));
            session = await client.session("typedb", SessionType.SCHEMA);
            tx = await session.transaction(TransactionType.READ);
            person = await tx.concepts.putEntityType("person");
            console.info(`Retrieved entity type with label '${person.label.scopedName}' from new primary replica`);
            assert(person.label.scopedName === "person");
            const idx = primaryReplica.address[10];
            spawn(`./${idx}/typedb`, ["cluster", "--data", "server/data", "--address", `127.0.0.1:${idx}1729:${idx}1730:${idx}1731`,
                "--peer", "127.0.0.1:11729:11730:11731", "--peer", "127.0.0.1:21729:21730:21731", "--peer", "127.0.0.1:31729:31730:31731", "--encryption-enabled=true"]);
            await new Promise(resolve => setTimeout(resolve, 20000));
            const spawned = getServerPID(`${idx}1729`);
            if (spawned === undefined) throw new Error("Failed to spawn/wait for start of server at port: " + `${idx}1729`);
        }
        console.info("SUCCESS - completed 10 iterations");
        await client.close();
        process.exit(0);
    } catch (err) {
        console.error(`ERROR: ${err.stack || err}`);
        await client.close();
        process.exit(1);
    }
}

run();
