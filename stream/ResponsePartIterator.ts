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

import {TransactionResPart, TransactionStreamState} from "typedb-protocol/proto/transaction";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {BatchDispatcher} from "./RequestTransmitter";
import {ResponseCollector} from "./ResponseCollector";
import UNKNOWN_STREAM_STATE = ErrorMessage.Client.UNKNOWN_STREAM_STATE;

export class ResponsePartIterator implements AsyncIterable<TransactionResPart> {

    private readonly _requestId: string;
    private readonly _responseCollector: ResponseCollector.ResponseQueue<TransactionResPart>;
    private readonly _dispatcher: BatchDispatcher;

    constructor(requestId: string, responseCollector: ResponseCollector.ResponseQueue<TransactionResPart>,
                dispatcher: BatchDispatcher) {
        this._requestId = requestId;
        this._responseCollector = responseCollector;
        this._dispatcher = dispatcher;
    }

    async* [Symbol.asyncIterator](): AsyncIterator<TransactionResPart, any, undefined> {
        while (true) {
            const next = await this.next()
            if (next != null) yield next;
            else break;
        }
    }

    async next(): Promise<TransactionResPart> {
        const res = await this._responseCollector.take();
        if (res.has_stream_res_part) {
            switch (res.stream_res_part.state) {
                case TransactionStreamState.DONE:
                    return null;
                case TransactionStreamState.CONTINUE:
                    this._dispatcher.dispatch(RequestBuilder.Transaction.streamReq(this._requestId))
                    return this.next();
                default:
                    throw new TypeDBClientError(UNKNOWN_STREAM_STATE.message(res.stream_res_part));
            }
        } else return res;
    }
}
