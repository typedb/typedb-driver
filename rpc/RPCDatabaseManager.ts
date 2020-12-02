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

import { GraknClient as GraknGrpc } from "graknlabs-grpc-protocol/protobuf/grakn_grpc_pb"
import {
    Grakn
} from "../dependencies_internal";
//import { Database } from "graknlabs-grpc-protocol/protobuf/database_pb";
import database_pb from "graknlabs-grpc-protocol/protobuf/database_pb";
const { Database } = database_pb;

export class RPCDatabaseManager implements Grakn.DatabaseManager {
    private _grpcClient: GraknGrpc;

    constructor(client: GraknGrpc) {
        this._grpcClient = client;
    }

    contains(name: string): Promise<boolean> {
        if (!name) throw "Database name cannot be null or empty.";
        const req = new Database.Contains.Req().setName(name);
        return new Promise((resolve, reject) => {
            this._grpcClient.database_contains(req, (err, res) => {
                if (err) reject(err);
                else resolve(res.getContains());
            });
        });
    }

    create(name: string): Promise<void> {
        if (!name) throw "Database name cannot be null or empty.";
        const req = new Database.Create.Req().setName(name);
        return new Promise((resolve, reject) => {
            this._grpcClient.database_create(req, (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    }

    delete(name: string): Promise<void> {
        if (!name) throw "Database name cannot be null or empty.";
        const req = new Database.Delete.Req().setName(name);
        return new Promise((resolve, reject) => {
            this._grpcClient.database_delete(req, (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    }

    all(): Promise<string[]> {
        const allRequest = new Database.All.Req();
        return new Promise((resolve, reject) => {
            this._grpcClient.database_all(allRequest, (err, res) => {
                if (err) reject(err);
                else resolve(res.getNamesList());
            });
        });
    }
}
