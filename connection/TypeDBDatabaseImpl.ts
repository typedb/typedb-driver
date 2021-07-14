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

import { Database } from "../api/connection/database/Database";
import { ErrorMessage } from "../common/errors/ErrorMessage";
import { TypeDBClientError } from "../common/errors/TypeDBClientError";
import { RequestBuilder } from "../common/rpc/RequestBuilder";
import { TypeDBStub } from "../common/rpc/TypeDBStub";

export class TypeDBDatabaseImpl implements Database {

    private readonly _name: string;
    private readonly _stub: TypeDBStub;

    constructor(name: string, typeDBStub: TypeDBStub) {
        this._name = name;
        this._stub = typeDBStub;
    }

    name(): string {
        return this._name;
    }

    delete(): Promise<void> {
        if (!this._name) throw new TypeDBClientError(ErrorMessage.Client.MISSING_DB_NAME.message());
        const req = RequestBuilder.Core.Database.deleteReq(this._name);
        return this._stub.databaseDelete(req);
    }

    schema(): Promise<string> {
        const req = RequestBuilder.Core.Database.schemaReq(this.name());
        return this._stub.databaseSchema(req);
    }

    toString(): string {
        return "Database[" + this._name + "]";
    }

}
