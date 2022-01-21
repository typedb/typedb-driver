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

import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBClientError} from "../common/errors/TypeDBClientError";
import {BlockingQueue} from "../common/util/BlockingQueue";

export class ResponseCollector<T> {

    private readonly _response_queues: { [requestId: string]: ResponseCollector.ResponseQueue<T> };

    constructor() {
        this._response_queues = {};
    }

    queue(uuid: string): ResponseCollector.ResponseQueue<T> {
        const queue = new ResponseCollector.ResponseQueue<T>();
        this._response_queues[uuid] = queue;
        return queue;
    }

    get(uuid: string) {
        return this._response_queues[uuid];
    }

    remove(requestId: string) {
        delete this._response_queues[requestId];
    }

    close(error?: Error | string) {
        Object.values(this._response_queues).forEach(collector => collector.close(error));
    }
}

export namespace ResponseCollector {

    import TRANSACTION_CLOSED = ErrorMessage.Client.TRANSACTION_CLOSED;
    import ILLEGAL_STATE = ErrorMessage.Internal.ILLEGAL_STATE;
    import TRANSACTION_CLOSED_WITH_ERRORS = ErrorMessage.Client.TRANSACTION_CLOSED_WITH_ERRORS;

    export class ResponseQueue<T> {
        private readonly _queue: BlockingQueue<QueueElement>;
        private _error: string | Error = null;

        constructor() {
            this._queue = new BlockingQueue<QueueElement>()
        }

        async take(): Promise<T> {
            const element = await this._queue.take();
            if (element.isValue()) return (element as Value<T>).value;
            else if (element.isDone() && !this._error) throw new TypeDBClientError(TRANSACTION_CLOSED);
            else if (element.isDone() && this._error) throw new TypeDBClientError(TRANSACTION_CLOSED_WITH_ERRORS.message(this._error));
            else throw new TypeDBClientError(ILLEGAL_STATE);
        }

        put(element: T): void {
            this._queue.add(new Value(element));
        }

        close(error?: Error | string): void {
            this._error = error;
            this._queue.add(new Done());
        }
    }

    class QueueElement {

        isValue(): boolean {
            return false;
        }

        isDone(): boolean {
            return false;
        }

    }

    class Value<R> extends QueueElement {

        private readonly _value: R;

        constructor(value: R) {
            super();
            this._value = value;
        }

        get value(): R {
            return this._value;
        }

        isValue(): boolean {
            return true;
        }
    }

    class Done extends QueueElement {

        constructor() {
            super();
        }

        isDone(): boolean {
            return true;
        }
    }

}
