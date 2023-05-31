/*
 * Copyright (C) 2022 Vaticle
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
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import CLIENT_NOT_OPEN = ErrorMessage.Client.CLIENT_NOT_OPEN;

export class ClusterServerClient extends TypeDBClientImpl {

    private readonly _address: string;
    private readonly _credential: TypeDBCredential;
    private _stub: ClusterServerStub;
    private _databases: TypeDBDatabaseManagerImpl;
    private _isOpen: boolean;

    constructor(address: string, credential: TypeDBCredential) {
        super();
        this._address = address;
        this._credential = credential;
        this._isOpen = false
    }

    async open(): Promise<ClusterServerClient> {
        await super.open();
        this._stub = new ClusterServerStub(this._address, this._credential);
        await this._stub.open();
        this._databases = new TypeDBDatabaseManagerImpl(this._stub);
        this._isOpen = true;
        return this;
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    stub(): ClusterServerStub {
        return this._stub;
    }

    get databases(): TypeDBDatabaseManagerImpl {
        if (!this.isOpen()) throw new TypeDBClientError(CLIENT_NOT_OPEN);
        return this._databases;
    }

    async close(): Promise<void> {
        if (this.isOpen()) {
            this._isOpen = false;
            await super.close();
            this._stub.close();
        }
    }
}
