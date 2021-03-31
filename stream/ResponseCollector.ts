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

import {BlockingQueue} from "../common/util/BlockingQueue";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {GraknClientError} from "../common/errors/GraknClientError";

export class ResponseCollector<T> {

    private readonly _collectors: { [requestId: string]: ResponseCollector.ResponseQueue<T> };

    constructor() {
        this._collectors = {};
    }

    queue(uuid: string): ResponseCollector.ResponseQueue<T> {
        const queue = new ResponseCollector.ResponseQueue<T>();
        this._collectors[uuid] = queue;
        return queue;
    }

    get(uuid: string) {
        return this._collectors[uuid];
    }

    close(error?: Error | string) {
        Object.values(this._collectors).forEach(collector => collector.close(error));
        for (const requestId in this._collectors) delete this._collectors[requestId];
    }

}

export namespace ResponseCollector {

    import TRANSACTION_CLOSED = ErrorMessage.Client.TRANSACTION_CLOSED;

    export class ResponseQueue<T> {

        private readonly _queue: BlockingQueue<QueueElement>;

        constructor() {
            this._queue = new BlockingQueue<QueueElement>()
        }

        async take(): Promise<T> {
            const element = await this._queue.take();
            if (element.isResponse()) return (element as Response<T>).value();
            else {
                if ((element as Done).hasError()) {
                    throw new GraknClientError((element as Done).error());
                } else {
                    throw new GraknClientError(TRANSACTION_CLOSED);
                }
            }
        }

        put(element: T): void {
            this._queue.add(new Response(element));
        }

        close(error?: Error | string): void {
            this._queue.add(new Done(error));
        }

    }

    class QueueElement {

        isResponse(): boolean {
            return false;
        }

        isDone(): boolean {
            return false;
        }

    }

    class Response<R> extends QueueElement {

        private readonly _value: R;

        constructor(value: R) {
            super();
            this._value = value;
        }

        value(): R {
            return this._value;
        }

        isResponse(): boolean {
            return true;
        }
    }

    class Done extends QueueElement {
        private readonly _error?: Error | string;

        constructor(error?: Error | string) {
            super();
            this._error = error;
        }

        hasError(): boolean {
            return this._error != null;
        }

        error(): Error | string {
            return this._error;
        }

        isDone(): boolean {
            return true;
        }
    }

}
