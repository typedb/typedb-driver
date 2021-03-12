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

import { GraknClient, DatabaseRPC, ServerAddress, DatabaseManagerClusterRPC } from "../../dependencies_internal";
import DatabaseProto from "grakn-protocol/protobuf/cluster/database_pb";

export class DatabaseClusterRPC implements GraknClient.DatabaseCluster {
    private readonly _name: string;
    private readonly _databases: {[address: string]: DatabaseRPC};
    private readonly _databaseManagerCluster: DatabaseManagerClusterRPC;
    private readonly _replicas: DatabaseReplicaRPC[];

    private constructor(databaseManagerCluster: DatabaseManagerClusterRPC, database: string) {
        this._databases = {};
        for (const address of Object.keys(databaseManagerCluster.databaseManagers())) {
            const databaseManager = databaseManagerCluster.databaseManagers()[address];
            this._databases[address] = new DatabaseRPC(databaseManager.grpcClient(), database);
        }
        this._name = database;
        this._databaseManagerCluster = databaseManagerCluster;
        this._replicas = [];
    }

    static of(protoDB: DatabaseProto.Database, databaseManagerCluster: DatabaseManagerClusterRPC): DatabaseClusterRPC {
        const database = protoDB.getName();
        const databaseClusterRPC = new DatabaseClusterRPC(databaseManagerCluster, database);
        databaseClusterRPC.replicas().push(...protoDB.getReplicasList().map(rep => DatabaseReplicaRPC.of(rep, databaseClusterRPC)));
        console.info(`Discovered database cluster: ${databaseClusterRPC}`);
        return databaseClusterRPC;
    }

    primaryReplica(): DatabaseReplicaRPC {
        const primaryReplicas = this._replicas.filter(rep => rep.isPrimary());
        if (primaryReplicas.length) return primaryReplicas.reduce((current, next) => next.term() > current.term() ? next : current);
        else return null;
    }

    preferredSecondaryReplica(): DatabaseReplicaRPC {
        return this._replicas.find(rep => rep.isPreferredSecondary()) || this._replicas[0];
    }

    name(): string {
        return this._name;
    }

    async delete(): Promise<void> {
        for (const address of Object.keys(this._databases)) {
            if (await this._databaseManagerCluster.databaseManagers()[address].contains(this._name)) {
                await this._databases[address].delete();
            }
        }
    }

    replicas(): DatabaseReplicaRPC[] {
        return this._replicas;
    }

    toString(): string {
        return this._name;
    }
}

export class DatabaseReplicaRPC implements GraknClient.DatabaseReplica {
    private readonly _id: ReplicaId;
    private readonly _database: DatabaseClusterRPC;
    private readonly _isPrimary: boolean;
    private readonly _isPreferredSecondary: boolean;
    private readonly _term: number;

    private constructor(database: DatabaseClusterRPC, address: ServerAddress, term: number, isPrimary: boolean, isPreferredSecondary: boolean) {
        this._database = database;
        this._id = new ReplicaId(address, database.name());
        this._term = term;
        this._isPrimary = isPrimary;
        this._isPreferredSecondary = isPreferredSecondary;
    }

    static of(replica: DatabaseProto.Database.Replica, database: DatabaseClusterRPC): DatabaseReplicaRPC {
        return new DatabaseReplicaRPC(database, ServerAddress.parse(replica.getAddress()),
            replica.getTerm(), replica.getPrimary(), replica.getPreferredSecondary());
    }

    id(): ReplicaId {
        return this._id;
    }

    database(): DatabaseClusterRPC {
        return this._database;
    }

    term(): number {
        return this._term;
    }

    isPrimary(): boolean {
        return this._isPrimary;
    }

    isPreferredSecondary(): boolean {
        return this._isPreferredSecondary;
    }

    address(): ServerAddress {
        return this.id().address();
    }

    toString(): string {
        return `${this._id}:${this._isPrimary ? "P" : "S"}:${this._term}`;
    }
}

class ReplicaId {
    private readonly _address: ServerAddress;
    private readonly _databaseName: string;

    constructor(address: ServerAddress, databaseName: string) {
        this._address = address;
        this._databaseName = databaseName;
    }

    address(): ServerAddress {
        return this._address;
    }

    toString(): string {
        return `${this._address}/${this._databaseName}`;
    }
}
