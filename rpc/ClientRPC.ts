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

import { GraknClient, GraknOptions, DatabaseManagerRPC, SessionRPC, SessionType } from "../dependencies_internal";
import {ChannelCredentials, closeClient} from "@grpc/grpc-js";
import {GraknClient as GraknGrpc} from "grakn-protocol/protobuf/grakn_grpc_pb";

export const DEFAULT_URI = "localhost:1729";

export class ClientRPC implements GraknClient {

    private readonly _graknGrpc: GraknGrpc;
    private readonly _databases: DatabaseManagerRPC;
    private readonly _sessions: {[id: string]: SessionRPC};
    private _isOpen: boolean;

    constructor(address = DEFAULT_URI) {
        this._graknGrpc = new GraknGrpc(address, ChannelCredentials.createInsecure());
        this._databases = new DatabaseManagerRPC(this._graknGrpc);
        this._sessions = {}
        this._isOpen = true;
    }

    async session(database: string, type: SessionType, options?: GraknOptions): Promise<SessionRPC> {
        const session = new SessionRPC(this, database, type);
        this._sessions[session.id()] = session;
        return session.open(options);
    }

    databases(): DatabaseManagerRPC {
        return this._databases;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    close(): void {
        if (this._isOpen) {
            this._isOpen = false;
            Object.values(this._sessions).forEach(s => s.close());
            closeClient(this._graknGrpc);
        }
    }

    isCluster(): boolean {
        return false;
    }

    removeSession(session: SessionRPC): void {
        delete this._sessions[session.id()];
    }

    grpcClient(): GraknGrpc {
        return this._graknGrpc;
    }
}
