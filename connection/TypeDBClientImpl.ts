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

import {TypeDBSessionImpl} from "./TypeDBSessionImpl";
import {TypeDBStubFactory} from "./TypeDBStubFactory";
import {TypeDBDatabaseManagerImpl} from "./TypeDBDatabaseManagerImpl";
import {TypeDBClient} from "../api/connection/TypeDBClient";
import {TypeDBOptions} from "../api/connection/TypeDBOptions";
import {SessionType} from "../api/connection/TypeDBSession";
import {RequestTransmitter} from "../stream/RequestTransmitter";
import {TypeDBStub} from "../common/rpc/TypeDBStub";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import ILLEGAL_CAST = ErrorMessage.Internal.ILLEGAL_CAST;
import SESSION_ID_EXISTS = ErrorMessage.Client.SESSION_ID_EXISTS;

export abstract class TypeDBClientImpl implements TypeDBClient {

    private readonly _stub: TypeDBStub;
    private readonly _requestTransmitter: RequestTransmitter;
    private readonly _databases: TypeDBDatabaseManagerImpl;
    private readonly _sessions: { [id: string]: TypeDBSessionImpl };
    private _isOpen: boolean;

    constructor(address: string, stubFactory: TypeDBStubFactory) {
        this._stub = stubFactory.newTypeDBStub(address);
        this._databases = new TypeDBDatabaseManagerImpl(this._stub);
        this._requestTransmitter = new RequestTransmitter();
        this._sessions = {};
        this._isOpen = true;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    isCluster(): boolean {
        return false;
    }

    asCluster(): TypeDBClient.Cluster {
        throw new TypeDBClientError(ILLEGAL_CAST.message(this.constructor.toString(), "ClusterClient"));
    }

    databases(): TypeDBDatabaseManagerImpl {
        return this._databases;
    }

    async session(database: string, type: SessionType, options?: TypeDBOptions): Promise<TypeDBSessionImpl> {
        if (!options) options = TypeDBOptions.core();
        const session = new TypeDBSessionImpl(database, type, options, this);
        await session.open();
        if (this._sessions[session.sessionId()]) throw new TypeDBClientError(SESSION_ID_EXISTS.message(session.sessionId()));
        this._sessions[session.sessionId()] = session;
        return session;
    }

    close(): void {
        if (this._isOpen) {
            this._isOpen = false;
            Object.values(this._sessions).forEach(s => s.close());
            this._requestTransmitter.close();
            this._stub.closeClient();
        }
    }

    closedSession(session: TypeDBSessionImpl): void {
        delete this._sessions[session.sessionId()];
    }

    stub(): TypeDBStub {
        return this._stub;
    }

    transmitter(): RequestTransmitter {
        return this._requestTransmitter;
    }

}
