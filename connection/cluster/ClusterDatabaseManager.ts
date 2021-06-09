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

import {ClusterServerStub} from "./ClusterServerStub";
import {ClusterDatabase} from "./ClusterDatabase";
import {ClusterClient} from "./ClusterClient";
import {FailsafeTask} from "./FailsafeTask";
import {TypeDBDatabaseManagerImpl} from "../TypeDBDatabaseManagerImpl";
import {Database} from "../../api/connection/database/Database";
import {DatabaseManager} from "../../api/connection/database/DatabaseManager";
import {RequestBuilder} from "../../common/rpc/RequestBuilder";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {ClusterDatabaseManager as ClusterDatabaseManagerProto} from "typedb-protocol/cluster/cluster_database_pb";
import CLUSTER_ALL_NODES_FAILED = ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;
import CLUSTER_REPLICA_NOT_PRIMARY = ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import DB_DOES_NOT_EXIST = ErrorMessage.Client.DB_DOES_NOT_EXIST;

export class ClusterDatabaseManager implements DatabaseManager.Cluster {

    private readonly _databaseManagers: { [serverAddress: string]: TypeDBDatabaseManagerImpl };
    private readonly _client: ClusterClient;

    constructor(client: ClusterClient) {
        this._client = client;
        this._databaseManagers = Object.entries(this._client.clusterServerClients()).reduce((obj: { [address: string]: TypeDBDatabaseManagerImpl }, [addr, client]) => {
            obj[addr] = client.databases();
            return obj;
        }, {});
    }

    async contains(name: string): Promise<boolean> {
        return await this.failsafeTask(name, ((stub, dbMgr) => dbMgr.contains(name)));
    }

    async create(name: string): Promise<void> {
        return await this.failsafeTask(name, ((stub, dbMgr) => dbMgr.create(name)));
    }

    async get(name: string): Promise<Database.Cluster> {
        return await this.failsafeTask(name, (async (stub, dbMgr) => {
            if (await this.contains(name)) {
                const res: ClusterDatabaseManagerProto.Get.Res = await stub.databasesClusterGet(RequestBuilder.Cluster.DatabaseManager.getReq(name));
                return ClusterDatabase.of(res.getDatabase(), this._client);
            }
            throw new TypeDBClientError(DB_DOES_NOT_EXIST.message(name));
        }));
    }

    async all(): Promise<Database.Cluster[]> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                const res = await this._client.stub(address).databasesClusterAll(RequestBuilder.Cluster.DatabaseManager.allReq());
                return res.getDatabasesList().map(db => ClusterDatabase.of(db, this._client));
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new TypeDBClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    databaseManagers(): { [address: string]: TypeDBDatabaseManagerImpl } {
        return this._databaseManagers;
    }

    rpcClient() {
        return this._client;
    }

    private async failsafeTask<T>(name: string, task: (stub: ClusterServerStub, dbMgr: DatabaseManager) => Promise<T>): Promise<T> {
        const failsafeTask = new DatabaseManagerFailsafeTask(this._client, name, task);
        try {
            return await failsafeTask.runAnyReplica();
        } catch (e) {
            if (e instanceof TypeDBClientError && CLUSTER_REPLICA_NOT_PRIMARY === e.errorMessage()) {
                return await failsafeTask.runPrimaryReplica();
            } else throw e;
        }
    }
}

class DatabaseManagerFailsafeTask<T> extends FailsafeTask<T> {

    private readonly _task: (stub: ClusterServerStub, dbMgr: DatabaseManager) => Promise<T>;

    constructor(client: ClusterClient, database: string, task: (stub: ClusterServerStub, dbMgr: DatabaseManager) => Promise<T>) {
        super(client, database);
        this._task = task;
    }

    async run(replica: Database.Replica): Promise<T> {
        return this._task(this.client.stub(replica.address()), this.client.clusterServerClient(replica.address()).databases());
    }
}
