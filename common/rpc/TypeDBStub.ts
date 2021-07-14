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


import { ServiceError } from "@grpc/grpc-js";
import { Session } from "typedb-protocol/common/session_pb";
import { CoreDatabase as CoreDatabaseProto, CoreDatabaseManager as CoreDatabaseMgrProto } from "typedb-protocol/core/core_database_pb";
import { TypeDBClient } from "typedb-protocol/core/core_service_grpc_pb";
import { TypeDBDatabaseImpl } from "../../connection/TypeDBDatabaseImpl";
import { TypeDBClientError } from "../errors/TypeDBClientError";

/*
TODO implement ResilientCall
 */
export abstract class TypeDBStub {

    private _stub: TypeDBClient;

    constructor(stub: TypeDBClient) {
        this._stub = stub;
    }

    databasesCreate(req: CoreDatabaseMgrProto.Create.Req): Promise<void> {
        return new Promise((resolve, reject) => {
            this._stub.databases_create(req, (err) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve();
            })
        });
    }

    databasesContains(req: CoreDatabaseMgrProto.Contains.Req): Promise<boolean> {
        return new Promise((resolve, reject) => {
            this._stub.databases_contains(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res.getContains());
            });
        });
    }

    databasesAll(req: CoreDatabaseMgrProto.All.Req): Promise<TypeDBDatabaseImpl[]> {
        return new Promise((resolve, reject) => {
            this._stub.databases_all(req, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res.getNamesList().map(name => new TypeDBDatabaseImpl(name, this)));
            })
        })
    }

    databaseDelete(req: CoreDatabaseProto.Delete.Req): Promise<void> {
        return new Promise((resolve, reject) => {
            this._stub.database_delete(req, (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    }

    databaseSchema(req: CoreDatabaseProto.Schema.Req): Promise<string> {
        return new Promise((resolve, reject) => {
            return this._stub.database_schema(req, (err, res) => {
                if (err) reject(err);
                else resolve(res.getSchema());
            });
        });
    }

    sessionOpen(openReq: Session.Open.Req): Promise<Session.Open.Res> {
        return new Promise<Session.Open.Res>((resolve, reject) => {
            this._stub.session_open(openReq, (err, res) => {
                if (err) reject(new TypeDBClientError(err));
                else resolve(res);
            });
        });
    }

    sessionClose(req: Session.Close.Req): Promise<void> {
        return new Promise<void>(resolve => {
            this._stub.session_close(req, () => {
                resolve();
            });
        });
    }

    sessionPulse(pulse: Session.Pulse.Req, callback: (err: ServiceError, res: Session.Pulse.Res) => void) {
        this._stub.session_pulse(pulse, callback);
    }

    transaction() {
        return this._stub.transaction()
    }

    closeClient() {
        this._stub.close();
    }
}
