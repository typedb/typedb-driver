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

const { TypeDB, SessionType, TransactionType, TypeDBCredential } = require("../../dist");
const { spawn, spawnSync } = require("child_process");
const assert = require("assert");

async function seekPrimaryReplica(databases) {
    for (let retryNum = 0; retryNum < 10; retryNum++) {
        console.info("Discovering replicas for database 'typedb'...");
        try {
            const db = await databases.get("typedb");
            console.info(`Discovered ${db.replicas}`);
            if (db.primaryReplica) return db.primaryReplica;
            retryNum++;
        } catch (e) { }
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

function serverStart(idx) {
    const encryptionResourceDir = process.env.ROOT_CA.replace(/\/[^\/]*$/, "/");
    let node = spawn(`./${idx}/typedb`, ["cluster",
        "--storage.data", "server/data",
        "--server.address", `localhost:${idx}1729`,
        "--server.internal-address.zeromq", `localhost:${idx}1730`,
        "--server.internal-address.grpc", `localhost:${idx}1731`,
        "--server.peers.peer-1.address", "localhost:11729",
        "--server.peers.peer-1.internal-address.zeromq", "localhost:11730",
        "--server.peers.peer-1.internal-address.grpc", "localhost:11731",
        "--server.peers.peer-2.address", "localhost:21729",
        "--server.peers.peer-2.internal-address.zeromq", "localhost:21730",
        "--server.peers.peer-2.internal-address.grpc", "localhost:21731",
        "--server.peers.peer-3.address", "localhost:31729",
        "--server.peers.peer-3.internal-address.zeromq", "localhost:31730",
        "--server.peers.peer-3.internal-address.grpc", "localhost:31731",
        "--server.encryption.enable", "true",
        "--server.encryption.file.enable", "true",
        "--server.encryption.file.external-grpc.private-key", encryptionResourceDir + "ext-grpc-private-key.pem",
        "--server.encryption.file.external-grpc.certificate", encryptionResourceDir + "ext-grpc-certificate.pem",
        "--server.encryption.file.external-grpc.root-ca", encryptionResourceDir + "ext-grpc-root-ca.pem",
        "--server.encryption.file.internal-grpc.private-key", encryptionResourceDir + "int-grpc-private-key.pem",
        "--server.encryption.file.internal-grpc.certificate", encryptionResourceDir + "int-grpc-certificate.pem",
        "--server.encryption.file.internal-grpc.root-ca", encryptionResourceDir + "int-grpc-root-ca.pem",
        "--server.encryption.file.internal-zmq.secret-key", encryptionResourceDir + "int-zmq-private-key",
        "--server.encryption.file.internal-zmq.public-key", encryptionResourceDir + "int-zmq-public-key",
    ]);
    node.stdout.on('data', (data) => {
        console.log(`stdout: ${data}`);
    });
    node.stderr.on('data', (data) => {
        console.error(`stderr: ${data}`);
    });
    node.on('close', (code) => {
        console.log(`child process exited with code ${code}`);
    });
}

async function run() {
    console.log("root ca path: ", process.env.ROOT_CA)
    const client = await TypeDB.clusterClient(
        ["localhost:11729", "localhost:21729", "localhost:31729"],
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
            const client = await TypeDB.clusterClient(
                ["localhost:11729", "localhost:21729", "localhost:31729"],
                new TypeDBCredential("admin", "password", process.env.ROOT_CA)
            );
            primaryReplica = await seekPrimaryReplica(client.databases);
            console.info(`Stopping primary replica (test ${iteration}/10)...`);
            const port = primaryReplica.address.substring(10,15);
            const primaryReplicaServerPID = getServerPID(port);
            console.info(`Primary replica is hosted by server with PID ${primaryReplicaServerPID}`);
            spawnSync("kill", ["-9", primaryReplicaServerPID]);
            console.info("Primary replica stopped successfully.");
            await new Promise(resolve => setTimeout(resolve, 1000));
            console.info("Opening a schema session...");
            session = await client.session("typedb", SessionType.SCHEMA);
            console.info("Opening a read txn...");
            tx = await session.transaction(TransactionType.READ);
            person = await tx.concepts.getEntityType("person");
            console.info(`Retrieved entity type with label '${person.label.scopedName}' from new primary replica`);
            assert(person.label.scopedName === "person");
            await session.close();
            const idx = primaryReplica.address[10];
            serverStart(idx);
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
