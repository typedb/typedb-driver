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

import {
    GraknClient, ConceptManager, OptionsProtoBuilder, GraknOptions, QueryManager, uuidv4, BlockingQueue,
    Stream, GraknClientError, ErrorMessage, LogicManager, TransactionType
} from "../dependencies_internal";
import TransactionProto from "grakn-protocol/protobuf/transaction_pb";
import GraknProto from "grakn-protocol/protobuf/grakn_grpc_pb";
import GraknGrpc = GraknProto.GraknClient;
import { ClientDuplexStream } from "@grpc/grpc-js";

export class TransactionRPC implements GraknClient.Transaction {
    private readonly _type: TransactionType;
    private readonly _conceptManager: ConceptManager;
    private readonly _logicManager: LogicManager;
    private readonly _queryManager: QueryManager;
    private readonly _collectors: ResponseCollectors;
    private readonly _grpcClient: GraknGrpc;

    private _stream: ClientDuplexStream<TransactionProto.Transaction.Req, TransactionProto.Transaction.Res>;
    private _options: GraknOptions;
    private _isOpen: boolean;
    private _networkLatencyMillis: number;

    constructor(grpcClient: GraknGrpc, type: TransactionType) {
        this._type = type;
        this._conceptManager = new ConceptManager(this);
        this._logicManager = new LogicManager(this);
        this._queryManager = new QueryManager(this);
        this._collectors = new ResponseCollectors(this);
        this._isOpen = false;
        this._grpcClient = grpcClient;
    }

    async open(sessionId: string, options?: GraknOptions): Promise<TransactionRPC> {
        this.openTransactionStream();
        this._options = options;
        this._isOpen = true;
        const openRequest = new TransactionProto.Transaction.Req()
            .setOpenReq(
                new TransactionProto.Transaction.Open.Req()
                    .setSessionId(sessionId)
                    .setType(this._type === TransactionType.READ ? TransactionProto.Transaction.Type.READ : TransactionProto.Transaction.Type.WRITE)
                    .setOptions(OptionsProtoBuilder.options(options))
            );
        const startTime = new Date().getTime();
        const res = await this.execute(openRequest, res => res.getOpenRes());
        const endTime = new Date().getTime();
        this._networkLatencyMillis = endTime - startTime - res.getProcessingTimeMillis();
        return this;
    }

    type(): TransactionType {
        return this._type;
    }

    options(): GraknOptions {
        return this._options;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    concepts(): ConceptManager {
        return this._conceptManager;
    }

    logic(): LogicManager {
        return this._logicManager;
    }

    query(): QueryManager {
        return this._queryManager;
    }

    async commit(): Promise<void> {
        const commitReq = new TransactionProto.Transaction.Req()
            .setCommitReq(new TransactionProto.Transaction.Commit.Req());
        try {
            await this.execute(commitReq);
        } finally {
            await this.close();
        }
    }

    async rollback(): Promise<void> {
        const rollbackReq = new TransactionProto.Transaction.Req()
            .setRollbackReq(new TransactionProto.Transaction.Rollback.Req());
        await this.execute(rollbackReq);
    }

    async close(): Promise<void> {
        if (this._isOpen) {
            this._isOpen = false;
            this._collectors.clearWithError(new ErrorResponse(new GraknClientError(ErrorMessage.Client.TRANSACTION_CLOSED)));
            this._stream.end();
        }
    }

    execute<T>(request: TransactionProto.Transaction.Req, transformResponse: (res: TransactionProto.Transaction.Res) => T = () => null): Promise<T> {
        const responseCollector = new ResponseCollector();
        const requestId = uuidv4();
        request.setId(requestId);
        this._collectors.put(requestId, responseCollector);
        this._stream.write(request);
        return responseCollector.take().then(transformResponse);
    }

    stream<T>(request: TransactionProto.Transaction.Req, transformResponse: (res: TransactionProto.Transaction.Res) => T[]): Stream<T> {
        const responseCollector = new ResponseCollector();
        const requestId = uuidv4();
        request.setId(requestId);
        request.setLatencyMillis(this._networkLatencyMillis);
        this._collectors.put(requestId, responseCollector);
        this._stream.write(request);
        return new Stream<T>(requestId, this._stream, responseCollector, transformResponse);
    }

    private openTransactionStream() {
        this._stream = this._grpcClient.transaction();

        this._stream.on("data", (res) => {
            const requestId = res.getId();
            const collector = this._collectors.get(requestId);
            if (!collector) throw new GraknClientError(ErrorMessage.Client.UNKNOWN_REQUEST_ID.message(requestId));
            collector.add(new OkResponse(res));
        });

        this._stream.on("error", (err) => {
            this._collectors.clearWithError(new ErrorResponse(err));
            this.close();
        });

        this._stream.on("end", () => {
            this.close();
        });
        // TODO: look into _stream.on(status) + any other events
    }
}

class ResponseCollectors {
    private readonly _map: { [requestId: string]: ResponseCollector };
    private readonly _transaction: TransactionRPC;
    constructor(transaction: TransactionRPC) {
        this._map = {};
        this._transaction = transaction;
    }

    get(uuid: string) {
        return this._map[uuid];
    }

    put(uuid: string, collector: ResponseCollector) {
        if (!this._transaction.isOpen()) throw new GraknClientError(ErrorMessage.Client.TRANSACTION_CLOSED);
        this._map[uuid] = collector;
    }

    clearWithError(error: ErrorResponse) {
        Object.keys(this._map).forEach((requestId) => this._map[requestId].add(error));
        for (const requestId in this._map) delete this._map[requestId];
    }
}

export class ResponseCollector {
    private _responseBuffer: BlockingQueue<Response>;

    constructor() {
        this._responseBuffer = new BlockingQueue<Response>();
    }

    add(response: Response): void {
        this._responseBuffer.add(response);
    }

    async take(): Promise<TransactionProto.Transaction.Res> {
        const response = await this._responseBuffer.take();
        return response.read();
    }
}

abstract class Response {
    abstract read(): TransactionProto.Transaction.Res;
}

class OkResponse extends Response {
    private readonly _res: TransactionProto.Transaction.Res;

    constructor(res: TransactionProto.Transaction.Res) {
        super()
        this._res = res;
    }

    read(): TransactionProto.Transaction.Res {
        return this._res;
    }

    toString(): string {
        return "OkResponse {" + this._res.toString() + "}";
    }
}

class ErrorResponse extends Response {
    private readonly _error: Error | string;

    constructor(error: Error | string) {
        super();
        this._error = error;
    }

    read(): TransactionProto.Transaction.Res {
        throw this._error;
    }

    toString(): string {
        return "ErrorResponse {" + this._error.toString() + "}";
    }
}
