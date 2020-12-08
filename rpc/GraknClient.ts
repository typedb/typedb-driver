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
    GraknOptions,
    RPCDatabaseManager,
    RPCSession,
} from "../dependencies_internal";
import {ChannelCredentials, closeClient} from "@grpc/grpc-js";
import {GraknClient as GraknGrpc} from "graknlabs-protocol/protobuf/grakn_grpc_pb";

export const DEFAULT_URI = "localhost:1729";

export class GraknClient implements Grakn.Client {
    private readonly _databases: Grakn.DatabaseManager;
    private readonly _graknGrpc: GraknGrpc;

    constructor(address = DEFAULT_URI) {
        this._graknGrpc = new GraknGrpc(address, ChannelCredentials.createInsecure());
        this._databases = new RPCDatabaseManager(this._graknGrpc);
    }

    async session(database: string, type: Grakn.SessionType, options?: GraknOptions): Promise<Grakn.Session> {
        const session = new RPCSession(this._graknGrpc, database, type);
        return session.open(options);
    }

    databases(): Grakn.DatabaseManager {
        return this._databases;
    }

    close(): void {
        closeClient(this._graknGrpc);
    }
}
