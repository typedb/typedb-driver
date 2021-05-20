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

import {Database} from "../api/database/Database";
import {CoreDatabase} from "../core/CoreDatabase";
import {ClusterDatabase as ClusterDatabaseProto} from "typedb-protocol/cluster/cluster_database_pb";
import { ClusterClient } from "./ClusterClient";
import { FailsafeTask } from "./FailsafeTask";

export class ClusterDatabase implements Database.Cluster {

    private readonly _name: string;
    private readonly _databases: { [address: string]: CoreDatabase };
    private readonly _client: ClusterClient;
    private readonly _replicas: DatabaseReplica[];

    private constructor(client: ClusterClient, database: string) {
        this._databases = {};
        const clusterDbMgr = client.databases();
        for (const address of Object.keys(clusterDbMgr.databaseManagers())) {
            const databaseManager = clusterDbMgr.databaseManagers()[address];
            this._databases[address] = new CoreDatabase(database, databaseManager.rpcClient());
        }
        this._name = database;
        this._client = client;
        this._replicas = [];
    }

    static of(protoDB: ClusterDatabaseProto, client: ClusterClient): ClusterDatabase {
        const database = protoDB.getName();
        const databaseClusterRPC = new ClusterDatabase(client, database);
        databaseClusterRPC.replicas().push(...protoDB.getReplicasList().map(rep => DatabaseReplica.of(rep, databaseClusterRPC)));
        console.info(`Discovered database cluster: ${databaseClusterRPC}`);
        return databaseClusterRPC;
    }

    primaryReplica(): DatabaseReplica {
        const primaryReplicas = this._replicas.filter(rep => rep.isPrimary());
        if (primaryReplicas.length) return primaryReplicas.reduce((current, next) => next.term() > current.term() ? next : current);
        else return null;
    }

    preferredReplica(): DatabaseReplica {
        return this._replicas.find(rep => rep.isPreferred()) || this._replicas[0];
    }

    name(): string {
        return this._name;
    }

    async delete(): Promise<void> {
        const deleteDbTask = new DeleteDatabaseFailsafeTask(this._client, this._name, this._databases);
        await deleteDbTask.runPrimaryReplica();
    }

    async schema(): Promise<string> {
        return this._databases[Object.keys(this._databases)[0]].schema();
    }

    replicas(): DatabaseReplica[] {
        return this._replicas;
    }

    toString(): string {
        return this._name;
    }
}

export class DatabaseReplica implements Database.Replica {
    private readonly _id: ReplicaId;
    private readonly _database: ClusterDatabase;
    private readonly _isPrimary: boolean;
    private readonly _isPreferred: boolean;
    private readonly _term: number;

    private constructor(database: ClusterDatabase, address: string, term: number, isPrimary: boolean, isPreferred: boolean) {
        this._database = database;
        this._id = new ReplicaId(address, database.name());
        this._term = term;
        this._isPrimary = isPrimary;
        this._isPreferred = isPreferred;
    }

    static of(replica: ClusterDatabaseProto.Replica, database: ClusterDatabase): DatabaseReplica {
        return new DatabaseReplica(database, replica.getAddress(), replica.getTerm(), replica.getPrimary(), replica.getPreferred());
    }

    id(): ReplicaId {
        return this._id;
    }

    database(): Database.Cluster {
        return this._database;
    }

    term(): number {
        return this._term;
    }

    isPrimary(): boolean {
        return this._isPrimary;
    }

    isPreferred(): boolean {
        return this._isPreferred;
    }

    address(): string {
        return this.id().address();
    }

    async delete(): Promise<void> {
        await this._database.delete();
    }

    name(): string {
        return this.toString();
    }

    schema(): Promise<string> {
        return this._database.schema();
    }

    toString(): string {
        return `${this._id}:${this._isPrimary ? "P" : "S"}:${this._term}`;
    }
}

class ReplicaId {
    private readonly _address: string;
    private readonly _databaseName: string;

    constructor(address: string, databaseName: string) {
        this._address = address;
        this._databaseName = databaseName;
    }

    address(): string {
        return this._address;
    }

    toString(): string {
        return `${this._address}/${this._databaseName}`;
    }
}

class DeleteDatabaseFailsafeTask extends FailsafeTask<void> {

    private readonly _databases: {[key: string]: CoreDatabase};

    constructor(client: ClusterClient, database: string, databases: {[key: string]: CoreDatabase}) {
        super(client, database);
        this._databases = databases;
    }

    async run(replica: Database.Replica): Promise<void> {
        await this._databases[replica.address()].delete();
    }
}
