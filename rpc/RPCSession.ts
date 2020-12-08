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
    ProtoBuilder,
    GraknOptions,
    RPCTransaction, GraknClientError, ErrorMessage,
} from "../dependencies_internal";
import GraknProto from "graknlabs-protocol/protobuf/grakn_grpc_pb";
import GraknGrpc = GraknProto.GraknClient;
import SessionProto from "graknlabs-protocol/protobuf/session_pb";

export class RPCSession implements Grakn.Session {
    private readonly _grpcClient: GraknGrpc;
    private readonly _database: string;
    private readonly _type: Grakn.SessionType;
    private _sessionId: string;
    private _isOpen: boolean;
    private _pulse: NodeJS.Timeout;

    constructor(grpcClient: GraknGrpc, database: string, type: Grakn.SessionType) {
        this._database = database;
        this._type = type;
        this._grpcClient = grpcClient;
    }

    async open(options: GraknOptions = new GraknOptions()): Promise<Grakn.Session> {
        const openReq = new SessionProto.Session.Open.Req()
            .setDatabase(this._database)
            .setType(sessionType(this._type))
            .setOptions(ProtoBuilder.options(options));
        this._isOpen = true;
        const res = await new Promise<SessionProto.Session.Open.Res>((resolve, reject) => {
            this._grpcClient.session_open(openReq, (err, res) => {
                if (err) reject(err);
                else resolve(res);
            });
        });
        this._sessionId = res.getSessionId_asB64();
        this._pulse = setTimeout(() => this.pulse(), 5000);
        return this;
    }

    transaction(type: Grakn.TransactionType, options: GraknOptions = new GraknOptions()): Promise<Grakn.Transaction> {
        const transaction = new RPCTransaction(this._grpcClient, type);
        return transaction.open(this._sessionId, options);
    }

    type(): Grakn.SessionType {
        return this._type;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    async close(): Promise<void> {
        if (this._isOpen) {
            this._isOpen = false;
            clearTimeout(this._pulse);
            const req = new SessionProto.Session.Close.Req().setSessionId(this._sessionId);
            await new Promise((resolve, reject) => {
                this._grpcClient.session_close(req, (err) => {
                    if (err) reject(err);
                    else resolve();
                });
            });
        }
    }

    database(): string {
        return this._database;
    }

    pulse(): void {
        if (!this._isOpen) return;
        const pulse = new SessionProto.Session.Pulse.Req().setSessionId(this._sessionId);
        this._grpcClient.session_pulse(pulse, (err) => {
            if (err) return;// Generally means the session has been closed, which is fine
            else this._pulse = setTimeout(() => this.pulse(), 5000);
        });
    }
}

function sessionType(type: Grakn.SessionType): SessionProto.Session.Type {
    switch (type) {
        case Grakn.SessionType.DATA:
            return SessionProto.Session.Type.DATA;
        case Grakn.SessionType.SCHEMA:
            return SessionProto.Session.Type.SCHEMA;
        default:
            throw new GraknClientError(ErrorMessage.Client.UNRECOGNISED_SESSION_TYPE.message())
    }
}
