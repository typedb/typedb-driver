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

import { Database } from "../../api/connection/database/Database";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import { RequestBuilder } from "../../common/rpc/RequestBuilder";
import { ClusterDatabase, DatabaseReplica } from "../../dependencies_internal";
import { ClusterClient } from "./ClusterClient";
import CLUSTER_REPLICA_NOT_PRIMARY = ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;
import CLUSTER_UNABLE_TO_CONNECT = ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import UNABLE_TO_CONNECT = ErrorMessage.Client.UNABLE_TO_CONNECT;

const PRIMARY_REPLICA_TASK_MAX_RETRIES = 10;
const FETCH_REPLICAS_MAX_RETRIES = 10;
const WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS = 2000;

export abstract class FailsafeTask<TResult> {

    private readonly _client: ClusterClient;
    private readonly _database: string;

    protected constructor(client: ClusterClient, database: string) {
        this._client = client;
        this._database = database;
    }

    protected get client(): ClusterClient {
        return this._client;
    }

    protected get database(): string {
        return this._database;
    }

    abstract run(replica: Database.Replica): Promise<TResult>;

    rerun(replica: Database.Replica): Promise<TResult> {
        return this.run(replica);
    }

    async runPrimaryReplica(): Promise<TResult> {
        if (!this._client.clusterDatabases()[this._database]?.primaryReplica) {
            await this.seekPrimaryReplica();
        }
        let replica = this._client.clusterDatabases()[this._database].primaryReplica;
        let retries = 0;
        while (true) { // eslint-disable-line no-constant-condition
            try {
                return retries == 0 ? await this.run(replica) : await this.rerun(replica);
            } catch (e) {
                if (e instanceof TypeDBClientError && [CLUSTER_REPLICA_NOT_PRIMARY, UNABLE_TO_CONNECT].includes(e.messageTemplate)) {
                    console.info("Unable to open a session or transaction, retrying in 2s...", e);
                    await this.waitForPrimaryReplicaSelection();
                    replica = await this.seekPrimaryReplica();
                } else throw e;
            }
            if (++retries > PRIMARY_REPLICA_TASK_MAX_RETRIES) throw this.clusterNotAvailableError();
        }
    }

    async runAnyReplica(): Promise<TResult> {
        let databaseClusterRPC = this._client.clusterDatabases()[this._database];
        if (!databaseClusterRPC) databaseClusterRPC = await this.fetchDatabaseReplicas();

        // Try the preferred secondary replica first, then go through the others
        const replicas: Database.Replica[] = [databaseClusterRPC.preferredReplica]
            .concat(databaseClusterRPC.replicas.filter(rep => !rep.preferred));

        let retries = 0;
        for (const replica of replicas) {
            try {
                return retries == 0 ? await this.run(replica) : await this.rerun(replica);
            } catch (e) {
                if (e instanceof TypeDBClientError && UNABLE_TO_CONNECT === e.messageTemplate) {
                    console.info("Unable to open a session or transaction to " + replica + ". Attempting next replica.", e);
                } else throw e;
            }
            retries++;
        }
        throw this.clusterNotAvailableError();
    }

    private async seekPrimaryReplica(): Promise<DatabaseReplica> {
        let retries = 0;
        while (retries < FETCH_REPLICAS_MAX_RETRIES) {
            const databaseClusterRPC = await this.fetchDatabaseReplicas();
            if (databaseClusterRPC.primaryReplica) {
                return databaseClusterRPC.primaryReplica;
            } else {
                await this.waitForPrimaryReplicaSelection();
                retries++;
            }
        }
        throw this.clusterNotAvailableError();
    }

    private async fetchDatabaseReplicas(): Promise<ClusterDatabase> {
        for (const serverAddress of this._client.clusterServerAddresses()) {
            try {
                console.info(`Fetching replica info from ${serverAddress}`);
                const res = await this._client.stub(serverAddress).databasesClusterGet(RequestBuilder.Cluster.DatabaseManager.getReq(this._database));
                const databaseClusterRPC = ClusterDatabase.of(res.getDatabase(), this._client);
                this._client.clusterDatabases()[this._database] = databaseClusterRPC;
                return databaseClusterRPC;
            } catch (e) {
                console.info(`Failed to fetch replica info for database '${this._database}' from ${serverAddress}. Attempting next server.`, e);
            }
        }
        throw this.clusterNotAvailableError();
    }

    private waitForPrimaryReplicaSelection(): Promise<void> {
        return new Promise(resolve => setTimeout(resolve, WAIT_FOR_PRIMARY_REPLICA_SELECTION_MS));
    }

    private clusterNotAvailableError(): TypeDBClientError {
        const addresses = this._client.clusterServerAddresses().join(",");
        return new TypeDBClientError(CLUSTER_UNABLE_TO_CONNECT.message(addresses));
    }
}
