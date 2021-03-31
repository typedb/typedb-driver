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

import {Database} from "../api/database/Database";
import {DatabaseManager} from "../api/database/DatabaseManager";
import {ClusterDatabase} from "./ClusterDatabase";
import {ClusterClient} from "./ClusterClient";
import {CoreDatabaseManager} from "../core/CoreDatabaseManager";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {GraknClientError} from "../common/errors/GraknClientError";
import {ClusterDatabaseManager as ClusterDatabaseManagerProto} from "grakn-protocol/cluster/cluster_database_pb";
import CLUSTER_ALL_NODES_FAILED = ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;

export class ClusterDatabaseManager implements DatabaseManager.Cluster {

    private readonly _databaseManagers: { [serverAddress: string]: CoreDatabaseManager };
    private readonly _client: ClusterClient;

    constructor(client: ClusterClient) {
        this._client = client;
        this._databaseManagers = Object.entries(this._client.coreClients()).reduce((obj: { [address: string]: CoreDatabaseManager }, [addr, client]) => {
            obj[addr] = client.databases();
            return obj;
        }, {});
    }

    async contains(name: string): Promise<boolean> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                return await this._databaseManagers[address].contains(name);
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new GraknClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    async create(name: string): Promise<void> {
        for (const databaseManager of Object.values(this._databaseManagers)) {
            if (!(await databaseManager.contains(name))) {
                await databaseManager.create(name);
            }
        }
    }

    async get(name: string): Promise<Database.Cluster> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                const res: ClusterDatabaseManagerProto.Get.Res = await new Promise((resolve, reject) => {
                    this._client.graknClusterRPC(address).databases_get(RequestBuilder.Cluster.DatabaseManager.getReq(name), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                return ClusterDatabase.of(res.getDatabase(), this);
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new GraknClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    async all(): Promise<Database.Cluster[]> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                const res: ClusterDatabaseManagerProto.All.Res = await new Promise((resolve, reject) => {
                    this._client.graknClusterRPC(address).databases_all(RequestBuilder.Cluster.DatabaseManager.allReq(), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                return res.getDatabasesList().map(db => ClusterDatabase.of(db, this));
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new GraknClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    databaseManagers(): { [address: string]: CoreDatabaseManager } {
        return this._databaseManagers;
    }

    rpcClient() {
        return this._client;
    }
}