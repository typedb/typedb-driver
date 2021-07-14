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


import { Database } from "../api/connection/database/Database";
import { TypeDBOptions } from "../api/connection/TypeDBOptions";
import { SessionType, TypeDBSession } from "../api/connection/TypeDBSession";
import { TransactionType, TypeDBTransaction } from "../api/connection/TypeDBTransaction";
import { ErrorMessage } from "../common/errors/ErrorMessage";
import { TypeDBClientError } from "../common/errors/TypeDBClientError";
import { RequestBuilder } from "../common/rpc/RequestBuilder";
import { TypeDBStub } from "../common/rpc/TypeDBStub";
import { RequestTransmitter } from "../stream/RequestTransmitter";
import { CoreClient } from "./core/CoreClient";
import { TypeDBTransactionImpl } from "./TypeDBTransactionImpl";
import SESSION_CLOSED = ErrorMessage.Client.SESSION_CLOSED;

export class TypeDBSessionImpl implements TypeDBSession {

    private readonly _databaseName: string;
    private readonly _type: SessionType;
    private readonly _options: TypeDBOptions;
    private readonly _client: CoreClient;
    private _sessionId: string;
    private _database: Database;
    private _isOpen: boolean;
    private _pulse: NodeJS.Timeout;
    private _networkLatencyMillis: number;
    private _transactions: Set<TypeDBTransaction.Extended>;

    constructor(database: string, type: SessionType, options: TypeDBOptions, client: CoreClient) {
        this._databaseName = database;
        this._type = type;
        this._options = options;
        this._client = client;
        this._isOpen = false;
        this._transactions = new Set();
    }

    public async open(): Promise<void> {
        const openReq = RequestBuilder.Session.openReq(this._databaseName, this._type.proto(), this._options.proto())
        this._database = await this._client.databases().get(this._databaseName);
        const start = (new Date()).getMilliseconds();
        const res = await this._client.stub().sessionOpen(openReq);
        const end = (new Date()).getMilliseconds(); // TODO will this work?
        this._sessionId = res.getSessionId_asB64();
        this._networkLatencyMillis = (end - start) - res.getServerDurationMillis();
        this._isOpen = true;
        this._pulse = setTimeout(() => this.pulse(), 5000);
    }

    public async close(): Promise<void> {
        if (this._isOpen) {
            this._isOpen = false;
            this._transactions.forEach(tx => tx.close());
            this._client.closedSession(this);
            clearTimeout(this._pulse);
            const req = RequestBuilder.Session.closeReq(this._sessionId);
            await this._client.stub().sessionClose(req);
        }
    }

    public async transaction(type: TransactionType, options?: TypeDBOptions): Promise<TypeDBTransaction> {
        if (!this.isOpen()) throw new TypeDBClientError(SESSION_CLOSED);
        if (!options) options = TypeDBOptions.core();
        const transaction = new TypeDBTransactionImpl(this, this._sessionId, type, options);
        await transaction.open();
        this._transactions.add(transaction);
        return transaction;
    }

    public database(): Database {
        return this._database;
    }

    public isOpen(): boolean {
        return this._isOpen;
    }

    public options(): TypeDBOptions {
        return this._options;
    }

    public type(): SessionType {
        return this._type;
    }

    public sessionId() {
        return this._sessionId;
    }

    public stub(): TypeDBStub {
        return this._client.stub();
    }

    public requestTransmitter(): RequestTransmitter {
        return this._client.transmitter();
    }

    public networkLatency() {
        return this._networkLatencyMillis;
    }

    private pulse(): void {
        if (!this._isOpen) return;
        const pulse = RequestBuilder.Session.pulseReq(this._sessionId);
        this._client.stub().sessionPulse(pulse, (err, res) => {
            if (err || !res.getAlive()) this._isOpen = false;
            else this._pulse = setTimeout(() => this.pulse(), 5000);
        });
    }
}
