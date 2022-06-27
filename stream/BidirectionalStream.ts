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

import {ClientDuplexStream} from "@grpc/grpc-js";
import {Transaction} from "typedb-protocol/common/transaction_pb";
import * as uuid from "uuid";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {TypeDBStub} from "../common/rpc/TypeDBStub";
import {Stream} from "../common/util/Stream";
import {BatchDispatcher, RequestTransmitter} from "./RequestTransmitter";
import {ResponseCollector} from "./ResponseCollector";
import {ResponsePartIterator} from "./ResponsePartIterator";
import MISSING_RESPONSE = ErrorMessage.Client.MISSING_RESPONSE;
import UNKNOWN_REQUEST_ID = ErrorMessage.Client.UNKNOWN_REQUEST_ID;
import ResponseQueue = ResponseCollector.ResponseQueue;

export class BidirectionalStream {

    private readonly _requestTransmitter: RequestTransmitter;
    private _dispatcher: BatchDispatcher;
    private readonly _responseCollector: ResponseCollector<Transaction.Res>;
    private readonly _responsePartCollector: ResponseCollector<Transaction.ResPart>;
    private _stub: TypeDBStub;
    private _isOpen: boolean;
    private _error: Error | string;

    constructor(stub: TypeDBStub, requestTransmitter: RequestTransmitter) {
        this._requestTransmitter = requestTransmitter;
        this._responseCollector = new ResponseCollector();
        this._responsePartCollector = new ResponseCollector();
        this._stub = stub;
    }

    async open() {
        const transactionStream = await this._stub.transaction();
        this.registerObserver(transactionStream);
        this._dispatcher = this._requestTransmitter.dispatcher(transactionStream);
        this._isOpen = true;
    }

    async single(request: Transaction.Req, batch: boolean): Promise<Transaction.Res> {
        const requestId = uuid.v4();
        request.setReqId(uuid.parse(requestId) as Uint8Array);
        const responseQueue = this._responseCollector.queue(requestId);
        if (batch) this._dispatcher.dispatch(request);
        else this._dispatcher.dispatchNow(request);
        return await responseQueue.take();
    }

    stream(request: Transaction.Req): Stream<Transaction.ResPart> {
        const requestId = uuid.v4();
        request.setReqId(uuid.parse(requestId) as Uint8Array);
        const responseQueue = this._responsePartCollector.queue(requestId) as ResponseQueue<Transaction.ResPart>;
        const responseIterator = new ResponsePartIterator(requestId, responseQueue, this._dispatcher);
        this._dispatcher.dispatch(request);
        return Stream.iterable(responseIterator);
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    async close(error?: Error | string): Promise<void> {
        this._isOpen = false;
        this._error = error;
        this._responseCollector.close(error);
        this._responsePartCollector.close(error);
        this._dispatcher.close();
    }

    registerObserver(transactionStream: ClientDuplexStream<Transaction.Client, Transaction.Server>): void {
        transactionStream.on("data", (res: Transaction.Server) => {
            if (!this.isOpen()) {
                return;
            }

            switch (res.getServerCase()) {
                case Transaction.Server.ServerCase.RES:
                    this.collectRes(res.getRes());
                    return;
                case Transaction.Server.ServerCase.RES_PART:
                    this.collectResPart(res.getResPart());
                    return;
                case Transaction.Server.ServerCase.SERVER_NOT_SET:
                default:
                    throw new TypeDBClientError(MISSING_RESPONSE.message(res));
            }

        });

        transactionStream.on("error", (err) => {
            this.close(err);
        });

        transactionStream.on("done", () => {
            this.close();
        });
    }

    private collectRes(res: Transaction.Res): void {
        const requestId = res.getReqId();
        const queue = this._responseCollector.get(uuid.stringify(requestId as Uint8Array));
        if (!queue) throw new TypeDBClientError(UNKNOWN_REQUEST_ID.message(requestId));
        queue.put(res);
    }

    private collectResPart(res: Transaction.ResPart): void {
        const requestId = res.getReqId();
        const queue = this._responsePartCollector.get(uuid.stringify(requestId as Uint8Array));
        if (!queue) throw new TypeDBClientError(UNKNOWN_REQUEST_ID.message(requestId));
        queue.put(res);
    }

    dispatcher(): BatchDispatcher {
        return this._dispatcher;
    }

    getError(): Error | string {
        return this._error;
    }
}
