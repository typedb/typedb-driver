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

import {
    DatabaseReplicas as DatabaseProto,
    DatabaseReplicasReplica as ReplicaProto
} from "typedb-protocol/proto/database";
import {Database} from "../api/connection/database/Database";
import {ServerClient, TypeDBClientImpl} from "./TypeDBClientImpl";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import UNABLE_TO_CONNECT = ErrorMessage.Client.UNABLE_TO_CONNECT;
import DATABASE_DOES_NOT_EXIST = ErrorMessage.Client.DATABASE_DOES_NOT_EXIST;
import CLUSTER_REPLICA_NOT_PRIMARY = ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;

const PRIMARY_REPLICA_TASK_MAX_RETRIES = 10;
const FETCH_REPLICAS_MAX_RETRIES = 10;
const WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;

export class TypeDBDatabaseImpl implements Database {
    private readonly _name: string;
    private readonly _client: TypeDBClientImpl;
    private _replicas: Replica[];

    private constructor(name: string, client: TypeDBClientImpl, replicas: Replica[]) {
        this._name = name;
        this._client = client;
        this._replicas = replicas;
        client._database_cache[name] = this;
    }

    static get(name: string, client: TypeDBClientImpl): Promise<TypeDBDatabaseImpl> {
        return this.fetchReplicas(name, client).then(replicas => new TypeDBDatabaseImpl(name, client, replicas));
    }

    private static async fetchReplicas(name: string, client: TypeDBClientImpl): Promise<Replica[]> {
        for (const serverClient of client.serverClients.values()) {
            try {
                const res = await serverClient.stub.databasesGet(RequestBuilder.DatabaseManager.getReq(name));
                return res.database.replicas.map(
                    proto => Replica.of(proto, new ServerDatabase(name, client.serverClients.get(proto.address)))
                );
            } catch (err: any) {
                if (!("messageTemplate" in err)) {
                    throw err;
                }
                if (err.messageTemplate === DATABASE_DOES_NOT_EXIST || err.messageTemplate === UNABLE_TO_CONNECT) {
                    console.info(`Failed to fetch the list of replicas from ${serverClient.address}, trying next one`);
                } else throw err;
            }
        }
        throw new TypeDBClientError(UNABLE_TO_CONNECT.message());
    }

    static of(protoDB: DatabaseProto, client: TypeDBClientImpl): Database {
        const name = protoDB.name;
        const replicas = protoDB.replicas.map(
            proto => Replica.of(proto, new ServerDatabase(name, client.serverClients.get(proto.address)))
        );
        return new TypeDBDatabaseImpl(name, client, replicas);
    }

    get primaryReplica(): Replica {
        const primaryReplicas = this._replicas.filter(rep => rep.primary);
        if (primaryReplicas.length) return primaryReplicas.reduce((current, next) => next.term > current.term ? next : current);
        else return null;
    }

    get preferredReplica(): Replica {
        return this._replicas.find(rep => rep.preferred) || this._replicas[0];
    }

    get name(): string {
        return this._name;
    }

    async delete(): Promise<void> {
        await this.runOnPrimaryReplica((_, db) => db.delete());
    }

    async schema(): Promise<string> {
        return this.runFailsafe((_, db) => db.schema());
    }

    async typeSchema(): Promise<string> {
        return this.runFailsafe((_, db) => db.typeSchema());
    }

    async ruleSchema(): Promise<string> {
        return this.runFailsafe((_, db) => db.ruleSchema());
    }

    get replicas(): Replica[] {
        return this._replicas;
    }

    toString(): string {
        return this._name;
    }

    async runFailsafe<T>(task: (serverClient: ServerClient, serverDatabase: ServerDatabase, isFirstRun: boolean) => Promise<T>): Promise<T> {
        try {
            return await this.runOnAnyReplica(task);
        } catch (e) {
            if (e instanceof TypeDBClientError && CLUSTER_REPLICA_NOT_PRIMARY === e.messageTemplate) {
                // debug!("Attempted to run on a non-primary replica, retrying on primary...");
                return this.runOnPrimaryReplica(task);
            } else throw e;
        }
    }

    async runOnAnyReplica<T>(task: (serverClient: ServerClient, serverDatabase: ServerDatabase, isFirstRun: boolean) => Promise<T>): Promise<T> {
        let isFirstRun = true;
        for (const replica of this.replicas) {
            try {
                return await task(this._client.serverClients.get(replica.address), replica.database, isFirstRun);
            } catch (e) {
                if (e instanceof TypeDBClientError && UNABLE_TO_CONNECT === e.messageTemplate) {
                    // TODO log
                } else throw e;
                isFirstRun = false;
            }
        }
        throw new TypeDBClientError(UNABLE_TO_CONNECT.message());
    }

    async runOnPrimaryReplica<T>(task: (serverClient: ServerClient, serverDatabase: ServerDatabase, isFirstRun: boolean) => Promise<T>): Promise<T> {
        if (!this.primaryReplica) {
            await this.seekPrimaryReplica();
        }
        let isFirstRun = true;
        for (const _ of Array(PRIMARY_REPLICA_TASK_MAX_RETRIES)) {
            try {
                return await task(this._client.serverClients.get(this.primaryReplica.address), this.primaryReplica.database, isFirstRun);
            } catch (e) {
                if (e instanceof TypeDBClientError &&
                    (UNABLE_TO_CONNECT === e.messageTemplate || CLUSTER_REPLICA_NOT_PRIMARY === e.messageTemplate)
                ) {
                    await this.waitForPrimaryReplicaSelection();
                    await this.seekPrimaryReplica();
                } else throw e;
            }
            isFirstRun = false;
        }
        throw new TypeDBClientError(UNABLE_TO_CONNECT.message());
    }

    private async seekPrimaryReplica(): Promise<Replica> {
        for (const _ of Array(FETCH_REPLICAS_MAX_RETRIES)) {
            this._replicas = await TypeDBDatabaseImpl.fetchReplicas(this._name, this._client);
            if (this.primaryReplica) {
                return this.primaryReplica;
            } else {
                console.info(`No primary replica elected yet, waiting ${WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS} ms...`);
                await this.waitForPrimaryReplicaSelection();
            }
        }
        throw new TypeDBClientError(UNABLE_TO_CONNECT.message());
    }

    private waitForPrimaryReplicaSelection(): Promise<void> {
        return new Promise(resolve => setTimeout(resolve, WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS));
    }
}

export class Replica implements Database.Replica {
    private readonly _address: string;
    private readonly _database: ServerDatabase;
    private readonly _term: number;
    private readonly _isPrimary: boolean;
    private readonly _isPreferred: boolean;

    private constructor(database: ServerDatabase, address: string, term: number, isPrimary: boolean, isPreferred: boolean) {
        this._address = address;
        this._database = database;
        this._term = term;
        this._isPrimary = isPrimary;
        this._isPreferred = isPreferred;
    }

    static of(replica: ReplicaProto, database: ServerDatabase): Replica {
        return new Replica(database, replica.address, replica.term, replica.primary, replica.preferred);
    }

    get database(): ServerDatabase {
        return this._database;
    }

    get address(): string {
        return this._address;
    }

    get databaseName(): string {
        return this._database.name;
    }

    get term(): number {
        return this._term;
    }

    get primary(): boolean {
        return this._isPrimary;
    }

    get preferred(): boolean {
        return this._isPreferred;
    }

    toString(): string {
        return `${this._address}/${this.databaseName}:${this._isPrimary ? "P" : "S"}:${this._term}`;
    }
}

class ServerDatabase {
    private readonly _name: string;
    private readonly _client: ServerClient;

    constructor(name: string, client: ServerClient) {
        this._client = client;
        this._name = name;
    }

    get name(): string {
        return this._name;
    }

    get client(): ServerClient {
        return this._client;
    }

    async delete() {
        await this._client.stub.databaseDelete(RequestBuilder.Database.deleteReq(this.name));
    }

    schema(): Promise<string> {
        return this._client.stub.databaseSchema(RequestBuilder.Database.schemaReq(this.name));
    }

    typeSchema(): Promise<string> {
        return this._client.stub.databaseTypeSchema(RequestBuilder.Database.typeSchemaReq(this.name));
    }

    ruleSchema(): Promise<string> {
        return this._client.stub.databaseRuleSchema(RequestBuilder.Database.ruleSchemaReq(this.name));
    }
}
