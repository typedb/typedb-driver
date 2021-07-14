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
import { TypeDBClusterOptions, TypeDBOptions } from "../../api/connection/TypeDBOptions";
import { SessionType, TypeDBSession } from "../../api/connection/TypeDBSession";
import { TransactionType, TypeDBTransaction } from "../../api/connection/TypeDBTransaction";
import { TypeDBSessionImpl } from "../TypeDBSessionImpl";
import { ClusterClient } from "./ClusterClient";
import { ClusterServerClient } from "./ClusterServerClient";
import { FailsafeTask } from "./FailsafeTask";

export class ClusterSession implements TypeDBSession {

    private readonly _clusterClient: ClusterClient;
    private _options: TypeDBClusterOptions;

    constructor(clusterClient: ClusterClient, serverAddress: string) {
        this._clusterClient = clusterClient;
        this._clusterServerClient = clusterClient.clusterServerClient(serverAddress.toString());
    }

    private _clusterServerClient: ClusterServerClient;

    get clusterServerClient(): ClusterServerClient {
        return this._clusterServerClient;
    }

    set clusterServerClient(client: ClusterServerClient) {
        this._clusterServerClient = client;
    }

    private _typeDBSession: TypeDBSessionImpl;

    get typeDBSession(): TypeDBSessionImpl {
        return this._typeDBSession;
    }

    set typeDBSession(session: TypeDBSessionImpl) {
        this._typeDBSession = session;
    }

    async open(serverAddress: string, database: string, type: SessionType, options: TypeDBClusterOptions): Promise<ClusterSession> {
        console.info(`Opening a session to '${serverAddress}'`);
        this._typeDBSession = await this._clusterServerClient.session(database, type, options);
        this._options = options;
        return this;
    }

    transaction(type: TransactionType, options: TypeDBClusterOptions = TypeDBOptions.cluster()): Promise<TypeDBTransaction> {
        if (options.readAnyReplica) {
            return this.transactionAnyReplica(type, options);
        } else {
            return this.transactionPrimaryReplica(type, options);
        }
    }

    type(): SessionType {
        return this._typeDBSession.type();
    }

    isOpen(): boolean {
        return this._typeDBSession.isOpen();
    }

    options(): TypeDBClusterOptions {
        return this._options;
    }

    close(): Promise<void> {
        return this._typeDBSession.close();
    }

    database(): Database {
        return this._typeDBSession.database();
    }

    clusterClient(): ClusterClient {
        return this._clusterClient;
    }

    private transactionPrimaryReplica(type: TransactionType, options: TypeDBClusterOptions): Promise<TypeDBTransaction> {
        return new TransactionFailsafeTask(this, type, options).runPrimaryReplica();
    }

    private transactionAnyReplica(type: TransactionType, options: TypeDBClusterOptions): Promise<TypeDBTransaction> {
        return new TransactionFailsafeTask(this, type, options).runAnyReplica();
    }
}

class TransactionFailsafeTask extends FailsafeTask<TypeDBTransaction> {
    private readonly _clusterSession: ClusterSession;
    private readonly _type: TransactionType;
    private readonly _options: TypeDBClusterOptions;

    constructor(clusterSession: ClusterSession, type: TransactionType, options: TypeDBClusterOptions) {
        super(clusterSession.clusterClient(), clusterSession.database().name());
        this._clusterSession = clusterSession;
        this._type = type;
        this._options = options;
    }

    run(replica: Database.Replica): Promise<TypeDBTransaction> {
        return this._clusterSession.typeDBSession.transaction(this._type, this._options);
    }

    async rerun(replica: Database.Replica): Promise<TypeDBTransaction> {
        if (this._clusterSession.typeDBSession) await this._clusterSession.typeDBSession.close();
        this._clusterSession.clusterServerClient = this._clusterSession.clusterClient().clusterServerClient(replica.address());
        this._clusterSession.typeDBSession = await this._clusterSession.clusterServerClient.session(this.database, this._clusterSession.type(), this._clusterSession.options());
        return await this._clusterSession.typeDBSession.transaction(this._type, this._options);
    }
}
