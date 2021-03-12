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

import { ErrorMessage, GraknClient, GraknClientError } from "../dependencies_internal";
import { GraknClient as GraknGrpc } from "grakn-protocol/protobuf/grakn_grpc_pb"
import DatabaseProto from "grakn-protocol/protobuf/database_pb";
import { DatabaseRPC } from "./DatabaseRPC";

export class DatabaseManagerRPC implements GraknClient.DatabaseManager {
    private readonly _grpcClient: GraknGrpc;

    constructor(client: GraknGrpc) {
        this._grpcClient = client;
    }

    contains(name: string): Promise<boolean> {
        if (!name) throw new GraknClientError(ErrorMessage.Client.MISSING_DB_NAME);
        const req = new DatabaseProto.Database.Contains.Req().setName(name);
        return new Promise((resolve, reject) => {
            this._grpcClient.database_contains(req, (err, res) => {
                if (err) reject(new GraknClientError(err));
                else resolve(res.getContains());
            });
        });
    }

    create(name: string): Promise<void> {
        if (!name) throw new GraknClientError(ErrorMessage.Client.MISSING_DB_NAME);
        const req = new DatabaseProto.Database.Create.Req().setName(name);
        return new Promise((resolve, reject) => {
            this._grpcClient.database_create(req, (err) => {
                if (err) reject(new GraknClientError(err));
                else resolve();
            });
        });
    }

    async get(name: string): Promise<DatabaseRPC> {
        if (await this.contains(name)) return new DatabaseRPC(this._grpcClient, name);
        else throw new GraknClientError(ErrorMessage.Client.DB_DOES_NOT_EXIST);
    }

    all(): Promise<DatabaseRPC[]> {
        const allRequest = new DatabaseProto.Database.All.Req();
        return new Promise((resolve, reject) => {
            this._grpcClient.database_all(allRequest, (err, res) => {
                if (err) reject(new GraknClientError(err));
                else resolve(res.getNamesList().map(name => new DatabaseRPC(this._grpcClient, name)));
            });
        });
    }

    grpcClient(): GraknGrpc {
        return this._grpcClient;
    }
}
