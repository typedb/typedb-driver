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

import {BatchDispatcher, RequestTransmitter} from "./RequestTransmitter";
import {ResponseCollector} from "./ResponseCollector";
import {ResponsePartIterator} from "./ResponsePartIterator";
import {Stream} from "../common/util/Stream";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {TypeDBStub} from "../common/rpc/TypeDBStub";
import {Transaction} from "typedb-protocol/common/transaction_pb";
import ResponseQueue = ResponseCollector.ResponseQueue;
import {ClientDuplexStream} from "@grpc/grpc-js";
import * as uuid from "uuid";
import MISSING_RESPONSE = ErrorMessage.Client.MISSING_RESPONSE;
import UNKNOWN_REQUEST_ID = ErrorMessage.Client.UNKNOWN_REQUEST_ID;


export class BidirectionalStream {

    private readonly _requestTransmitter: RequestTransmitter;
    private readonly _dispatcher: BatchDispatcher;
    private readonly _responseCollector: ResponseCollector<Transaction.Res>;
    private readonly _responsePartCollector: ResponseCollector<Transaction.ResPart>;
    private _isOpen: boolean;

    constructor(stub: TypeDBStub, requestTransmitter: RequestTransmitter) {
        this._requestTransmitter = requestTransmitter;
        this._responseCollector = new ResponseCollector();
        this._responsePartCollector = new ResponseCollector();
        const transactionStream = stub.transaction();
        this.registerObserver(transactionStream);
        this._dispatcher = requestTransmitter.dispatcher(transactionStream);
        this._isOpen = true;
    }

    async single(request: Transaction.Req, batch: boolean): Promise<Transaction.Res> {
        const requestId = uuid.v4();
        request.setReqId(uuid.parse(requestId) as Uint8Array);
        const responseQueue = this._responseCollector.queue(requestId);
        if (batch) this._dispatcher.dispatch(request);
        else this._dispatcher.dispatchNow(request);
        return (await responseQueue.take() as Transaction.Res);
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
        this._responseCollector.close(error);
        this._responsePartCollector.close(error);
        this._dispatcher.close();
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
}
