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
    Grakn,
    ConceptManager,
    ProtoBuilder,
    GraknOptions,
    QueryManager,
    uuidv4,
    BlockingQueue,
    Stream,
} from "../dependencies_internal";
import TransactionProto from "graknlabs-grpc-protocol/protobuf/transaction_pb";
import GraknProto from "graknlabs-grpc-protocol/protobuf/grakn_grpc_pb";
import GraknGrpc = GraknProto.GraknClient;
import { ClientDuplexStream } from "@grpc/grpc-js";

export class RPCTransaction implements Grakn.Transaction {
    private readonly _type: Grakn.TransactionType;
    private readonly _conceptManager: ConceptManager;
    private readonly _queryManager: QueryManager;
    private readonly _collectors: ResponseCollectors;
    private readonly _grpcClient: GraknGrpc;

    private _stream: ClientDuplexStream<TransactionProto.Transaction.Req, TransactionProto.Transaction.Res>;
    private _streamIsOpen: boolean;
    private _transactionWasOpened: boolean;
    private _transactionWasClosed: boolean;
    private _networkLatencyMillis: number;

    constructor(grpcClient: GraknGrpc, type: Grakn.TransactionType) {
        this._type = type;
        this._conceptManager = new ConceptManager(this);
        this._queryManager = new QueryManager(this);
        this._collectors = new ResponseCollectors(this);
        this._transactionWasClosed = false;
        this._transactionWasOpened = false;
        this._streamIsOpen = false;
        this._grpcClient = grpcClient;
    }

    async open(sessionId: string, options?: GraknOptions): Promise<RPCTransaction> {
        this.openTransactionStream();
        this._streamIsOpen = true;

        const openRequest = new TransactionProto.Transaction.Req()
            .setOpenReq(
                new TransactionProto.Transaction.Open.Req()
                    .setSessionId(sessionId)
                    .setType(this._type === Grakn.TransactionType.READ ? TransactionProto.Transaction.Type.READ : TransactionProto.Transaction.Type.WRITE)
                    .setOptions(ProtoBuilder.options(options))
            );
        const startTime = new Date().getTime();
        const res = await this.execute(openRequest, res => res.getOpenRes());
        const endTime = new Date().getTime();
        this._networkLatencyMillis = endTime - startTime - res.getProcessingTimeMillis();
        this._transactionWasOpened = true;
        return this;
    }

    public type(): Grakn.TransactionType {
        return this._type;
    }

    public isOpen(): boolean {
        return this._transactionWasOpened && !this._transactionWasClosed;
    }

    public concepts(): ConceptManager {
        return this._conceptManager;
    }

    public query(): QueryManager {
        return this._queryManager;
    }

    public async commit(): Promise<void> {
        const commitReq = new TransactionProto.Transaction.Req()
            .setCommitReq(new TransactionProto.Transaction.Commit.Req());
        await this.execute(commitReq);
    }

    public async rollback(): Promise<void> {
        const rollbackReq = new TransactionProto.Transaction.Req()
            .setRollbackReq(new TransactionProto.Transaction.Rollback.Req());
        await this.execute(rollbackReq);
    }

    async close(): Promise<void> {
        if (this._streamIsOpen) {
            this._streamIsOpen = false;
            // TODO: close stream, somehow?
        }
        if (!this._transactionWasClosed) {
            this._transactionWasClosed = true;
            this._collectors.clearWithError(new ErrorResponse("Transaction closed."))
        }
    }

    execute<T>(request: TransactionProto.Transaction.Req, transformResponse: (res: TransactionProto.Transaction.Res) => T = () => null): Promise<T> {
        const responseCollector = new ResponseCollector();
        const requestId = uuidv4();
        request.setId(requestId);
        this._collectors.put(requestId, responseCollector);
        // TODO: we can optionally inject the callback here - perhaps that would be cleaner than using ResponseCollectors?
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
            if (!collector) throw "Unknown request ID " + requestId;
            collector.add(new OkResponse(res));
        });

        this._stream.on("error", (err) => {
            console.error(err);
        });

        this._stream.on("end", () => {
            this._streamIsOpen = false;
            this.close();
        });
        // TODO: look into _stream.on(status) + any other events
    }
}

class ResponseCollectors {
    private readonly _map: { [requestId: string]: ResponseCollector };
    private readonly _transaction: RPCTransaction;
    constructor(transaction: RPCTransaction) {
        this._map = {};
        this._transaction = transaction;
    }

    get(uuid: string) {
        return this._map[uuid];
    }

    put(uuid: string, collector: ResponseCollector) {
        if (this._transaction["_transactionWasClosed"]) throw "The transaction has been closed and no further operation is allowed."
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
