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

import {ClientDuplexStream} from "@grpc/grpc-js";
import {Transaction as TransactionProto} from "typedb-protocol/common/transaction_pb";
import {RequestBuilder} from "../common/rpc/RequestBuilder";

export class BatchDispatcher {

    private static BATCH_WINDOW_SMALL_MILLIS = 1;
    private static BATCH_WINDOW_LARGE_MILLIS = 3;

    private readonly _transmitter: RequestTransmitter;
    private readonly _transactionStream: ClientDuplexStream<TransactionProto.Client, TransactionProto.Server>;
    private _bufferedRequests: TransactionProto.Req[];
    private _isRunning: boolean;

    constructor(transmitter: RequestTransmitter, transactionStream: ClientDuplexStream<TransactionProto.Client, TransactionProto.Server>) {
        this._transmitter = transmitter;
        this._transactionStream = transactionStream;
        this._bufferedRequests = new Array<TransactionProto.Req>();
        this._isRunning = false;
    }

    dispatch(req: TransactionProto.Req): void {
        this._bufferedRequests.push(req);
        this.sendScheduledBatch();
    }

    dispatchNow(req: TransactionProto.Req): void {
        this._bufferedRequests.push(req);
        this.sendNow();
    }

    close(): void {
        this._transmitter._dispatchers.delete(this);
        this._transactionStream.end();
    }

    private sendNow(): void {
        const clientRequest = RequestBuilder.Transaction.clientReq(this._bufferedRequests);
        this._transactionStream.write(clientRequest);
        this._bufferedRequests = [];
    }

    private sendScheduledBatch(): void {
        if (this._isRunning) return;
        this._isRunning = true;
        this.setSchedule(true);
    }

    private setSchedule(first: boolean) {
        const wait = first ? BatchDispatcher.BATCH_WINDOW_SMALL_MILLIS : BatchDispatcher.BATCH_WINDOW_LARGE_MILLIS;
        setTimeout(() => {
            if (this._bufferedRequests.length > 0) {
                this.sendNow();
                this.setSchedule(false);
            } else {
                this._isRunning = false;
            }
        }, wait);
    }
}

export class RequestTransmitter {

    readonly _dispatchers: Set<BatchDispatcher>;
    private _isOpen: boolean;

    constructor() {
        this._dispatchers = new Set<BatchDispatcher>();
        this._isOpen = true;
    }

    close(): void {
        if (this._isOpen) {
            this._isOpen = false;
            this._dispatchers.forEach(dispatcher => dispatcher.close());
        }
    }

    dispatcher(transactionStream: ClientDuplexStream<TransactionProto.Client, TransactionProto.Server>): BatchDispatcher {
        const dispatcher = new BatchDispatcher(this, transactionStream);
        this._dispatchers.add(dispatcher);
        return dispatcher;
    }
}
