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
    GraknClient, ClientClusterRPC, DatabaseManagerRPC, GraknClientError, ErrorMessage, DatabaseClusterRPC
} from "../../dependencies_internal";
import CLUSTER_ALL_NODES_FAILED = ErrorMessage.Client.CLUSTER_ALL_NODES_FAILED;
import DatabaseProto from "grakn-protocol/protobuf/cluster/database_pb";

export class DatabaseManagerClusterRPC implements GraknClient.DatabaseManagerCluster {
    private readonly _databaseManagers: {[serverAddress: string]: DatabaseManagerRPC};
    private readonly _client: ClientClusterRPC;

    constructor(client: ClientClusterRPC, databaseManagers: {[serverAddress: string]: DatabaseManagerRPC}) {
        this._client = client;
        this._databaseManagers = databaseManagers;
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

    async get(name: string): Promise<DatabaseClusterRPC> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                const res: DatabaseProto.Database.Get.Res = await new Promise((resolve, reject) => {
                    this._client.graknClusterRPC(address).database_get(new DatabaseProto.Database.Get.Req().setName(name), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                return DatabaseClusterRPC.of(res.getDatabase(), this);
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new GraknClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    async all(): Promise<DatabaseClusterRPC[]> {
        let errors = "";
        for (const address of Object.keys(this._databaseManagers)) {
            try {
                const res: DatabaseProto.Database.All.Res = await new Promise((resolve, reject) => {
                    this._client.graknClusterRPC(address).database_all(new DatabaseProto.Database.All.Req(), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                return res.getDatabasesList().map(db => DatabaseClusterRPC.of(db, this));
            } catch (e) {
                errors += `- ${address}: ${e}\n`;
            }
        }
        throw new GraknClientError(CLUSTER_ALL_NODES_FAILED.message(errors));
    }

    databaseManagers(): {[address: string]: DatabaseManagerRPC} {
        return this._databaseManagers;
    }
}
