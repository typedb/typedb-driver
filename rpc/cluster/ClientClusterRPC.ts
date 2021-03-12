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
    ClientRPC,
    GraknClientCluster,
    SessionType,
    GraknOptions,
    GraknClusterOptions,
    GraknClientError,
    ErrorMessage,
    ServerAddress,
    DatabaseManagerClusterRPC,
    DatabaseClusterRPC,
    SessionClusterRPC,
    FailsafeTask,
    DatabaseReplicaRPC,
    DatabaseManagerRPC
} from "../../dependencies_internal";
import { ChannelCredentials } from "@grpc/grpc-js";
import { GraknClusterClient as GraknClusterGrpc } from "grakn-protocol/protobuf/cluster/grakn_cluster_grpc_pb";
import ClusterProto from "grakn-protocol/protobuf/cluster/cluster_pb";
import CLUSTER_UNABLE_TO_CONNECT = ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;

export class ClientClusterRPC implements GraknClientCluster {
    private _coreClients: {[serverAddress: string]: ClientRPC};
    private _graknClusterRPCs: {[serverAddress: string]: GraknClusterGrpc};
    private _databaseManagers: DatabaseManagerClusterRPC;
    private _clusterDatabases: {[db: string]: DatabaseClusterRPC};
    private _isOpen: boolean;

    async open(addresses: string[]): Promise<this> {
        const serverAddresses = await this.fetchClusterServers(addresses);
        this._coreClients = serverAddresses.reduce((obj: {[address: string]: ClientRPC}, addr: ServerAddress) => {
            obj[addr.toString()] = new ClientRPC(addr.external());
            return obj;
        }, {});
        this._graknClusterRPCs = serverAddresses.reduce((obj: {[address: string]: GraknClusterGrpc}, addr: ServerAddress) => {
            obj[addr.toString()] = new GraknClusterGrpc(addr.external(), ChannelCredentials.createInsecure());
            return obj;
        }, {});
        const databaseManagers = Object.entries(this._coreClients).reduce((obj: {[address: string]: DatabaseManagerRPC}, [addr, client]) => {
            obj[addr] = client.databases();
            return obj;
        }, {});
        this._databaseManagers = new DatabaseManagerClusterRPC(this, databaseManagers);
        this._clusterDatabases = {};
        this._isOpen = true;
        return this;
    }

    session(database: string, type: SessionType, options: GraknClusterOptions = GraknOptions.cluster()): Promise<SessionClusterRPC> {
        if (options.readAnyReplica) {
            return this.sessionAnyReplica(database, type, options);
        } else {
            return this.sessionPrimaryReplica(database, type, options);
        }
    }

    private sessionPrimaryReplica(database: string, type: SessionType, options: GraknClusterOptions): Promise<SessionClusterRPC> {
        return new OpenSessionFailsafeTask(database, type, options, this).runPrimaryReplica();
    }

    private sessionAnyReplica(database: string, type: SessionType, options: GraknClusterOptions): Promise<SessionClusterRPC> {
        return new OpenSessionFailsafeTask(database, type, options, this).runAnyReplica();
    }

    databases(): DatabaseManagerClusterRPC {
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

    clusterDatabases(): {[db: string]: DatabaseClusterRPC} {
        return this._clusterDatabases;
    }

    clusterMembers(): string[] {
        return Object.keys(this._coreClients);
    }

    coreClient(address: string): ClientRPC {
        return this._coreClients[address];
    }

    graknClusterRPC(address: string): GraknClusterGrpc {
        return this._graknClusterRPCs[address];
    }

    private async fetchClusterServers(addresses: string[]): Promise<ServerAddress[]> {
        for (const address of addresses) {
            const client = new ClientRPC(address);
            try {
                console.info(`Fetching list of cluster servers from ${address}...`);
                const grpcClusterClient = new GraknClusterGrpc(address, ChannelCredentials.createInsecure());
                const res = await new Promise<ClusterProto.Cluster.Servers.Res>((resolve, reject) => {
                    grpcClusterClient.cluster_servers(new ClusterProto.Cluster.Servers.Req(), (err, res) => {
                        if (err) reject(new GraknClientError(err));
                        else resolve(res);
                    });
                });
                const members = res.getServersList().map(x => ServerAddress.parse(x));
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
}

class OpenSessionFailsafeTask extends FailsafeTask<SessionClusterRPC> {
    private readonly _type: SessionType;
    private readonly _options: GraknClusterOptions;

    constructor(database: string, type: SessionType, options: GraknClusterOptions, client: ClientClusterRPC) {
        super(client, database);
        this._type = type;
        this._options = options;
    }

    run(replica: DatabaseReplicaRPC): Promise<SessionClusterRPC> {
        const session = new SessionClusterRPC(this.client, replica.address());
        return session.open(replica.address(), this.database, this._type, this._options);
    }
}
