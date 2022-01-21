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

import {Transaction} from "typedb-protocol/common/transaction_pb";
import {ConceptManager} from "../api/concept/ConceptManager";
import {TypeDBOptions} from "../api/connection/TypeDBOptions";
import {TransactionType, TypeDBTransaction} from "../api/connection/TypeDBTransaction";
import {LogicManager} from "../api/logic/LogicManager";
import {QueryManager} from "../api/query/QueryManager";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {Stream} from "../common/util/Stream";
import {ConceptManagerImpl} from "../concept/ConceptManagerImpl";
import {LogicManagerImpl} from "../logic/LogicManagerImpl";
import {QueryManagerImpl} from "../query/QueryManagerImpl";
import {BidirectionalStream} from "../stream/BidirectionalStream";
import {TypeDBSessionImpl} from "./TypeDBSessionImpl";
import TRANSACTION_CLOSED = ErrorMessage.Client.TRANSACTION_CLOSED;
import TRANSACTION_CLOSED_WITH_ERRORS = ErrorMessage.Client.TRANSACTION_CLOSED_WITH_ERRORS;
import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;

export class TypeDBTransactionImpl implements TypeDBTransaction.Extended {
    private readonly _session: TypeDBSessionImpl;
    private readonly _type: TransactionType;
    private readonly _options: TypeDBOptions;
    private _bidirectionalStream: BidirectionalStream;
    private _conceptManager: ConceptManager;
    private _logicManager: LogicManager;
    private _queryManager: QueryManager;

    constructor(session: TypeDBSessionImpl, type: TransactionType, options: TypeDBOptions) {
        this._session = session;
        this._type = type;
        this._options = options;
    }

    public async open(): Promise<void> {
        const rpcClient = this._session.stub;
        this._bidirectionalStream = new BidirectionalStream(rpcClient, this._session.requestTransmitter);
        await this._bidirectionalStream.open();
        this._conceptManager = new ConceptManagerImpl(this);
        this._logicManager = new LogicManagerImpl(this);
        this._queryManager = new QueryManagerImpl(this);
        const openReq = RequestBuilder.Transaction.openReq(this._session.id, this._type.proto(), this._options.proto(), this._session.networkLatency);
        await this.rpcExecute(openReq, false);
    }

    public async close(): Promise<void> {
        await this._bidirectionalStream.close();
    }

    public async commit(): Promise<void> {
        const commitReq = RequestBuilder.Transaction.commitReq();
        try {
            await this.rpcExecute(commitReq, false);
        } finally {
            await this.close();
        }
    }

    public async rollback(): Promise<void> {
        const rollbackReq = RequestBuilder.Transaction.rollbackReq();
        await this.rpcExecute(rollbackReq, false);
    }

    public get concepts(): ConceptManager {
        return this._conceptManager;
    }

    public get logic(): LogicManager {
        return this._logicManager;
    }

    public get query(): QueryManager {
        return this._queryManager;
    }

    public get options(): TypeDBOptions {
        return this._options;
    }

    public get type(): TransactionType {
        return this._type;
    }

    public isOpen(): boolean {
        return this._bidirectionalStream.isOpen();
    }

    public async rpcExecute(request: Transaction.Req, batch?: boolean): Promise<Transaction.Res> {
        if (!this.isOpen()) this.throwTransactionClosed()
        const useBatch = batch !== false;
        return this._bidirectionalStream.single(request, useBatch);
    }

    public rpcStream(request: Transaction.Req): Stream<Transaction.ResPart> {
        if (!this.isOpen()) this.throwTransactionClosed();
        return this._bidirectionalStream.stream(request);
    }

    private throwTransactionClosed(): void {
        if (this.isOpen()) throw new TypeDBClientError(ILLEGAL_STATE);
        const error = this._bidirectionalStream.getError();
        if (!error) throw new TypeDBClientError(TRANSACTION_CLOSED);
        else throw new TypeDBClientError(TRANSACTION_CLOSED_WITH_ERRORS.message(error));
    }
}
