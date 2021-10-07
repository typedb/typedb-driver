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

import {TypeDBCredential} from "../../api/connection/TypeDBCredential";
import {TypeDBClientImpl} from "../TypeDBClientImpl";
import {ClusterServerStub} from "./ClusterServerStub";
import {TypeDBDatabaseManagerImpl} from "../TypeDBDatabaseManagerImpl";

export class ClusterServerClient extends TypeDBClientImpl {

    private readonly _stub: ClusterServerStub;
    private readonly _databases: TypeDBDatabaseManagerImpl;

    constructor(address: string, credential: TypeDBCredential) {
        super();
        this._stub = new ClusterServerStub(address, credential);
        this._databases = new TypeDBDatabaseManagerImpl(this._stub);
    }

    async open(): Promise<void> {
        await this._stub.open();
    }

    stub(): ClusterServerStub {
        return this._stub;
    }

    get databases(): TypeDBDatabaseManagerImpl {
        return this._databases;
    }

    close() {
        super.close();
        this._stub.close();
    }
}
