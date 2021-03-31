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

import {Database} from "../api/database/Database";
import {GraknClientError} from "../common/errors/GraknClientError";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {GraknCoreClient} from "grakn-protocol/core/core_service_grpc_pb";

export class CoreDatabase implements Database {

    private readonly _name: string;
    private readonly _rpcClient: GraknCoreClient;

    constructor(name: string, rpcClient: GraknCoreClient) {
        this._name = name;
        this._rpcClient = rpcClient;
    }

    delete(): Promise<void> {
        if (!this._name) throw new GraknClientError(ErrorMessage.Client.MISSING_DB_NAME.message());
        const req = RequestBuilder.Core.Database.deleteReq(this._name);
        return new Promise((resolve, reject) => {
            this._rpcClient.database_delete(req, (err) => {
                if (err) reject(err);
                else resolve();
            });
        });
    }

    name(): string {
        return this._name;
    }

    async schema(): Promise<string> {
        const schema: Promise<string> = new Promise((resolve, reject) => {
            return this._rpcClient.database_schema(RequestBuilder.Core.Database.schemaReq(this.name()), (err, res) => {
                if (err) reject(err);
                else resolve(res.getSchema());
            });
        });
        return schema;
    }

    toString(): string {
        return "Database[" + this._name + "]";
    }

}
