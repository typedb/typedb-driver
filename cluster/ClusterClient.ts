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


import {GraknClient} from "../api/GraknClient";
import {GraknClusterOptions, GraknOptions} from "../api/GraknOptions";
import {Database} from "../api/database/Database";
import {SessionType} from "../api/GraknSession";
import {CoreClient} from "../core/CoreClient";
import {ClusterDatabaseManager} from "./ClusterDatabaseManager";
import {ClusterDatabase} from "./ClusterDatabase";
import {ClusterSession} from "./ClusterSession";
import {FailsafeTask} from "./FailsafeTask";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {GraknClientError} from "../common/errors/GraknClientError";
import {GraknClusterClient} from "grakn-protocol/cluster/cluster_service_grpc_pb";
import {ServerManager} from "grakn-protocol/cluster/cluster_server_pb";
import {ChannelCredentials} from "@grpc/grpc-js";
import CLUSTER_UNABLE_TO_CONNECT = ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;

export class ClusterClient implements GraknClient.Cluster {

    private _coreClients: { [serverAddress: string]: CoreClient };
    private _graknClusterRPCs: { [serverAddress: string]: GraknClusterClient };
    private _databaseManagers: ClusterDatabaseManager;
    private _clusterDatabases: { [db: string]: ClusterDatabase };
    private _isOpen: boolean;

    async open(addresses: string[]): Promise<this> {
        const serverAddresses = await this.fetchClusterServers(addresses);
        this._coreClients = {}
        serverAddresses.forEach((addr) => {
            this._coreClients[addr] = new CoreClient(addr);
        });
        this._graknClusterRPCs = {};
        serverAddresses.forEach((addr) => {
            this._graknClusterRPCs[addr] = new GraknClusterClient(addr, ChannelCredentials.createInsecure());
        });

        this._databaseManagers = new ClusterDatabaseManager(this);
        this._clusterDatabases = {};
        this._isOpen = true;
        return this;
    }

    session(database: string, type: SessionType, options: GraknClusterOptions = GraknOptions.cluster()): Promise<ClusterSession> {
        if (options.readAnyReplica) {
            return this.sessionAnyReplica(database, type, options);
        } else {
            return this.sessionPrimaryReplica(database, type, options);
        }
    }

    private sessionPrimaryReplica(database: string, type: SessionType, options: GraknClusterOptions): Promise<ClusterSession> {
        return new OpenSessionFailsafeTask(database, type, options, this).runPrimaryReplica();
    }

    private sessionAnyReplica(database: string, type: SessionType, options: GraknClusterOptions): Promise<ClusterSession> {
        return new OpenSessionFailsafeTask(database, type, options, this).runAnyReplica();
    }

    databases(): ClusterDatabaseManager {
        return this._databaseManagers;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    close(): void {
        if (this._isOpen) {
            this._isOpen = false;
            Object.values(this._coreClients).forEach(client => client.close());
        }
    }

    isCluster(): boolean {
        return true;
    }

    clusterDatabases(): { [db: string]: ClusterDatabase } {
        return this._clusterDatabases;
    }

    clusterMembers(): string[] {
        return Object.keys(this._coreClients);
    }

    coreClient(address: string): GraknClient {
        return this._coreClients[address];
    }

    coreClients() {
        return this._coreClients;
    }

    graknClusterRPC(address: string): GraknClusterClient {
        return this._graknClusterRPCs[address];
    }

    private async fetchClusterServers(addresses: string[]): Promise<string[]> {
        for (const address of addresses) {
            const client = new CoreClient(address);
            try {
                console.info(`Fetching list of cluster servers from ${address}...`);
                const grpcClusterClient = new GraknClusterClient(address, ChannelCredentials.createInsecure());
                const res = await new Promise<ServerManager.All.Res>((resolve, reject) => {
                    grpcClusterClient.servers_all(RequestBuilder.Cluster.ServerManager.allReq(), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                const members = res.getServersList().map(x => x.getAddress());
                console.info(`The cluster servers are ${members}`);
                return members;
            } catch (e) {
                console.error(`Fetching cluster servers from ${address} failed.`, e);
            } finally {
                client.close();
            }
        }
        throw new GraknClientError(CLUSTER_UNABLE_TO_CONNECT.message(addresses.join(",")));
    }

    asCluster(): GraknClient.Cluster {
        return this;
    }
}

class OpenSessionFailsafeTask extends FailsafeTask<ClusterSession> {
    private readonly _type: SessionType;
    private readonly _options: GraknClusterOptions;

    constructor(database: string, type: SessionType, options: GraknClusterOptions, client: ClusterClient) {
        super(client, database);
        this._type = type;
        this._options = options;
    }

    run(replica: Database.Replica): Promise<ClusterSession> {
        const session = new ClusterSession(this.client, replica.address());
        return session.open(replica.address(), this.database, this._type, this._options);
    }
}
