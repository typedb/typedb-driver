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

import { ClientWritableStream } from "@grpc/grpc-js";
import TransactionProto from "graknlabs-grpc-protocol/protobuf/transaction_pb";
import {
    ResponseCollector,
} from "../dependencies_internal";

export class Stream<T> implements AsyncIterable<T> {

    private readonly _requestId: string;
    private readonly _writableStream: ClientWritableStream<TransactionProto.Transaction.Req>;
    private readonly _responseCollector: ResponseCollector;
    private readonly _transformResponse: (res: TransactionProto.Transaction.Res) => T[];

    private _receivedAnswers: T[];

    public constructor(requestId: string, writableStream: ClientWritableStream<TransactionProto.Transaction.Req>,
                       responseCollector: ResponseCollector, transformResponse: (res: TransactionProto.Transaction.Res) => T[]) {
        this._requestId = requestId;
        this._transformResponse = transformResponse;
        this._writableStream = writableStream;
        this._responseCollector = responseCollector;
    }

    async* [Symbol.asyncIterator](): AsyncIterator<T, any, undefined> {
        while (true) {
            const next = await this.next()
            if (next != null) yield next;
            else break;
        }
    }

    async next(): Promise<T> {
        if (this._receivedAnswers?.length) {
            return this._receivedAnswers.shift();
        }

        const res = await this._responseCollector.take();
        switch (res.getResCase()) {
            case TransactionProto.Transaction.Res.ResCase.CONTINUE:
                const continueReq = new TransactionProto.Transaction.Req()
                    .setId(this._requestId).setContinue(true);
                this._writableStream.write(continueReq);
                return this.next();
            case TransactionProto.Transaction.Res.ResCase.DONE:
                return undefined;
            case TransactionProto.Transaction.Res.ResCase.RES_NOT_SET:
                throw "Missing response";
            default:
                this._receivedAnswers = this._transformResponse(res);
                return this.next();
        }
    }

    async collect(): Promise<T[]> {
        const answers: T[] = [];
        for await (const answer of this) {
            answers.push(answer);
        }
        return answers;
    }

    map<TResult>(callbackFn: (value: T) => TResult): Stream<TResult> {
        return new Stream(this._requestId, this._writableStream, this._responseCollector, res => this._transformResponse(res).map(callbackFn));
    }
}
