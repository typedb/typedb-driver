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

import {TypeDBClient} from "../api/connection/TypeDBClient";
import {TypeDBOptions} from "../api/connection/TypeDBOptions";
import {SessionType} from "../api/connection/TypeDBSession";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {TypeDBStub} from "../common/rpc/TypeDBStub";
import {RequestTransmitter} from "../stream/RequestTransmitter";
import {TypeDBDatabaseManagerImpl} from "./TypeDBDatabaseManagerImpl";
import {TypeDBSessionImpl} from "./TypeDBSessionImpl";
import SESSION_ID_EXISTS = ErrorMessage.Client.SESSION_ID_EXISTS;
import ILLEGAL_CAST = ErrorMessage.Internal.ILLEGAL_CAST;
import CLIENT_NOT_OPEN = ErrorMessage.Client.CLIENT_NOT_OPEN;

export abstract class TypeDBClientImpl implements TypeDBClient {

    private readonly _sessions: { [id: string]: TypeDBSessionImpl };
    private _requestTransmitter: RequestTransmitter;

    protected constructor() {
        this._sessions = {};
    }

    protected async open(): Promise<TypeDBClient> {
        this._requestTransmitter = new RequestTransmitter();
        return this;
    }

    abstract isOpen(): boolean;

    async session(database: string, type: SessionType, options?: TypeDBOptions): Promise<TypeDBSessionImpl> {
        if (!this.isOpen()) throw new TypeDBClientError(CLIENT_NOT_OPEN);
        if (!options) options = TypeDBOptions.core();
        const session = new TypeDBSessionImpl(database, type, options, this);
        await session.open();
        if (this._sessions[session.id]) throw new TypeDBClientError(SESSION_ID_EXISTS.message(session.id));
        this._sessions[session.id] = session;
        return session;
    }

    abstract get databases(): TypeDBDatabaseManagerImpl;

    abstract stub(): TypeDBStub;

    transmitter(): RequestTransmitter {
        return this._requestTransmitter;
    }

    isCluster(): boolean {
        return false;
    }

    asCluster(): TypeDBClient.Cluster {
        throw new TypeDBClientError(ILLEGAL_CAST.message(this.constructor.toString(), "ClusterClient"));
    }

    async close(): Promise<void> {
        for (const session of Object.values(Object.values(this._sessions))) {
            await session.close();
        }
        this._requestTransmitter.close();
    }

    closeSession(session: TypeDBSessionImpl): void {
        delete this._sessions[session.id];
    }
}
