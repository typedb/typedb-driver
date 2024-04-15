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

import {
    DatabaseReplicas as DatabaseProto,
    DatabaseReplicasReplica as ReplicaProto
} from "typedb-protocol/proto/database";
import {Database} from "../api/connection/database/Database";
import {ServerDriver, TypeDBDriverImpl} from "./TypeDBDriverImpl";
import {TypeDBDriverError} from "../common/errors/TypeDBDriverError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import UNABLE_TO_CONNECT = ErrorMessage.Driver.UNABLE_TO_CONNECT;
import DATABASE_DOES_NOT_EXIST = ErrorMessage.Driver.DATABASE_DOES_NOT_EXIST;
import CLOUD_REPLICA_NOT_PRIMARY = ErrorMessage.Driver.CLOUD_REPLICA_NOT_PRIMARY;

const PRIMARY_REPLICA_TASK_MAX_RETRIES = 10;
const FETCH_REPLICAS_MAX_RETRIES = 10;
const WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;

export class TypeDBDatabaseImpl implements Database {
    private readonly _name: string;
    private readonly _driver: TypeDBDriverImpl;
    private _replicas: Replica[];

    private constructor(name: string, driver: TypeDBDriverImpl, replicas: Replica[]) {
        this._name = name;
        this._driver = driver;
        this._replicas = replicas;
        driver._database_cache[name] = this;
    }

    static get(name: string, driver: TypeDBDriverImpl): Promise<TypeDBDatabaseImpl> {
        return this.fetchReplicas(name, driver).then(replicas => new TypeDBDatabaseImpl(name, driver, replicas));
    }

    private static async fetchReplicas(name: string, driver: TypeDBDriverImpl): Promise<Replica[]> {
        for (const serverDriver of driver.serverDrivers.values()) {
            try {
                const res = await serverDriver.stub.databasesGet(RequestBuilder.DatabaseManager.getReq(name));
                return res.database.replicas.map(
                    proto => Replica.of(proto, new ServerDatabase(name, driver.serverDrivers.get(proto.address)))
                );
            } catch (err: any) {
                if (!("messageTemplate" in err)) {
                    throw err;
                }
                if (err.messageTemplate === DATABASE_DOES_NOT_EXIST || err.messageTemplate === UNABLE_TO_CONNECT) {
                    console.info(`Failed to fetch the list of replicas from ${serverDriver.address}, trying next one`);
                } else throw err;
            }
        }
        throw new TypeDBDriverError(UNABLE_TO_CONNECT.message());
    }

    static of(protoDB: DatabaseProto, driver: TypeDBDriverImpl): Database {
        const name = protoDB.name;
        const replicas = protoDB.replicas.map(
            proto => Replica.of(proto, new ServerDatabase(name, driver.serverDrivers.get(proto.address)))
        );
        return new TypeDBDatabaseImpl(name, driver, replicas);
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

    async runFailsafe<T>(task: (serverDriver: ServerDriver, serverDatabase: ServerDatabase) => Promise<T>): Promise<T> {
        try {
            return await this.runOnAnyReplica(task);
        } catch (e) {
            if (e instanceof TypeDBDriverError && CLOUD_REPLICA_NOT_PRIMARY === e.messageTemplate) {
                return this.runOnPrimaryReplica(task);
            } else throw e;
        }
    }

    async runOnAnyReplica<T>(task: (serverDriver: ServerDriver, serverDatabase: ServerDatabase) => Promise<T>): Promise<T> {
        for (const replica of this.replicas) {
            try {
                return await task(this._driver.serverDrivers.get(replica.server), replica.database);
            } catch (e) {
                if (e instanceof TypeDBDriverError && UNABLE_TO_CONNECT === e.messageTemplate) {
                    // TODO log
                } else throw e;
            }
        }
        throw new TypeDBDriverError(UNABLE_TO_CONNECT.message());
    }

    async runOnPrimaryReplica<T>(task: (serverDriver: ServerDriver, serverDatabase: ServerDatabase) => Promise<T>): Promise<T> {
        if (!this.primaryReplica) {
            await this.seekPrimaryReplica();
        }
        for (const _ of Array(PRIMARY_REPLICA_TASK_MAX_RETRIES)) {
            try {
                return await task(this._driver.serverDrivers.get(this.primaryReplica.server), this.primaryReplica.database);
            } catch (e) {
                if (e instanceof TypeDBDriverError &&
                    (UNABLE_TO_CONNECT === e.messageTemplate || CLOUD_REPLICA_NOT_PRIMARY === e.messageTemplate)
                ) {
                    await this.waitForPrimaryReplicaSelection();
                    await this.seekPrimaryReplica();
                } else throw e;
            }
        }
        throw new TypeDBDriverError(UNABLE_TO_CONNECT.message());
    }

    private async seekPrimaryReplica(): Promise<Replica> {
        for (const _ of Array(FETCH_REPLICAS_MAX_RETRIES)) {
            this._replicas = await TypeDBDatabaseImpl.fetchReplicas(this._name, this._driver);
            if (this.primaryReplica) {
                return this.primaryReplica;
            } else {
                console.info(`No primary replica elected yet, waiting ${WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS} ms...`);
                await this.waitForPrimaryReplicaSelection();
            }
        }
        throw new TypeDBDriverError(UNABLE_TO_CONNECT.message());
    }

    private waitForPrimaryReplicaSelection(): Promise<void> {
        return new Promise(resolve => setTimeout(resolve, WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS));
    }
}

export class Replica implements Database.Replica {
    private readonly _server: string;
    private readonly _database: ServerDatabase;
    private readonly _term: number;
    private readonly _isPrimary: boolean;
    private readonly _isPreferred: boolean;

    private constructor(database: ServerDatabase, server: string, term: number, isPrimary: boolean, isPreferred: boolean) {
        this._server = server;
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

    get server(): string {
        return this._server;
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
        return `${this._server}/${this.databaseName}:${this._isPrimary ? "P" : "S"}:${this._term}`;
    }
}

class ServerDatabase {
    private readonly _name: string;
    private readonly _driver: ServerDriver;

    constructor(name: string, driver: ServerDriver) {
        this._driver = driver;
        this._name = name;
    }

    get name(): string {
        return this._name;
    }

    get driver(): ServerDriver {
        return this._driver;
    }

    async delete() {
        await this._driver.stub.databaseDelete(RequestBuilder.Database.deleteReq(this.name));
    }

    schema(): Promise<string> {
        return this._driver.stub.databaseSchema(RequestBuilder.Database.schemaReq(this.name));
    }

    typeSchema(): Promise<string> {
        return this._driver.stub.databaseTypeSchema(RequestBuilder.Database.typeSchemaReq(this.name));
    }

    ruleSchema(): Promise<string> {
        return this._driver.stub.databaseRuleSchema(RequestBuilder.Database.ruleSchemaReq(this.name));
    }
}
