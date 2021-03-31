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
import {GraknSession, SessionType} from "../api/GraknSession";
import {GraknTransaction, TransactionType} from "../api/GraknTransaction";
import {ClusterClient} from "./ClusterClient";
import {FailsafeTask} from "./FailsafeTask";

export class ClusterSession implements GraknSession {

    private readonly _clusterClient: ClusterClient;
    private _coreClient: GraknClient;
    private _coreSession: GraknSession;
    private _options: GraknClusterOptions;

    constructor(clusterClient: ClusterClient, serverAddress: string) {
        this._clusterClient = clusterClient;
        this._coreClient = clusterClient.coreClient(serverAddress.toString());
    }

    async open(serverAddress: string, database: string, type: SessionType, options: GraknClusterOptions): Promise<ClusterSession> {
        console.info(`Opening a session to '${serverAddress}'`);
        this._coreSession = await this._coreClient.session(database, type, options);
        this._options = options;
        return this;
    }

    transaction(type: TransactionType, options: GraknClusterOptions = GraknOptions.cluster()): Promise<GraknTransaction> {
        if (options.readAnyReplica) {
            return this.transactionAnyReplica(type, options);
        } else {
            return this.transactionPrimaryReplica(type, options);
        }
    }

    private transactionPrimaryReplica(type: TransactionType, options: GraknClusterOptions): Promise<GraknTransaction> {
        return new TransactionFailsafeTask(this, type, options).runPrimaryReplica();
    }

    private transactionAnyReplica(type: TransactionType, options: GraknClusterOptions): Promise<GraknTransaction> {
        return new TransactionFailsafeTask(this, type, options).runAnyReplica();
    }

    type(): SessionType {
        return this._coreSession.type();
    }

    isOpen(): boolean {
        return this._coreSession.isOpen();
    }

    options(): GraknClusterOptions {
        return this._options;
    }

    close(): Promise<void> {
        return this._coreSession.close();
    }

    database(): Database {
        return this._coreSession.database();
    }

    clusterClient(): ClusterClient {
        return this._clusterClient;
    }

    get coreClient(): GraknClient {
        return this._coreClient;
    }

    set coreClient(client: GraknClient) {
        this._coreClient = client;
    }

    get coreSession(): GraknSession {
        return this._coreSession;
    }

    set coreSession(session: GraknSession) {
        this._coreSession = session;
    }
}

class TransactionFailsafeTask extends FailsafeTask<GraknTransaction> {
    private readonly _clusterSession: ClusterSession;
    private readonly _type: TransactionType;
    private readonly _options: GraknClusterOptions;

    constructor(clusterSession: ClusterSession, type: TransactionType, options: GraknClusterOptions) {
        super(clusterSession.clusterClient(), clusterSession.database().name());
        this._clusterSession = clusterSession;
        this._type = type;
        this._options = options;
    }

    run(replica: Database.Replica): Promise<GraknTransaction> {
        return this._clusterSession.coreSession.transaction(this._type, this._options);
    }

    async rerun(replica: Database.Replica): Promise<GraknTransaction> {
        if (this._clusterSession.coreSession) await this._clusterSession.coreSession.close();
        this._clusterSession.coreClient = this._clusterSession.clusterClient().coreClient(replica.address());
        this._clusterSession.coreSession = await this._clusterSession.coreClient.session(this.database, this._clusterSession.type(), this._clusterSession.options());
        return await this._clusterSession.coreSession.transaction(this._type, this._options);
    }
}
