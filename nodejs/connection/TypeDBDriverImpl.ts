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

import {TypeDBDriver} from "../api/connection/TypeDBDriver";
import {TypeDBCredential} from "../api/connection/TypeDBCredential";
import {TypeDBOptions} from "../api/connection/TypeDBOptions";
import {SessionType} from "../api/connection/TypeDBSession";
import {Database} from "../api/connection/database/Database";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../common/errors/TypeDBDriverError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {TypeDBStub} from "../common/rpc/TypeDBStub";
import {RequestTransmitter} from "../stream/RequestTransmitter";
import {UserImpl} from "../user/UserImpl";
import {UserManagerImpl} from "../user/UserManagerImpl";
import {TypeDBDatabaseManagerImpl} from "./TypeDBDatabaseManagerImpl";
import {TypeDBSessionImpl} from "./TypeDBSessionImpl";
import {TypeDBStubImpl} from "./TypeDBStubImpl";
import DRIVER_NOT_OPEN = ErrorMessage.Driver.DRIVER_NOT_OPEN;
import CLOUD_UNABLE_TO_CONNECT = ErrorMessage.Driver.CLOUD_UNABLE_TO_CONNECT;
import SESSION_ID_EXISTS = ErrorMessage.Driver.SESSION_ID_EXISTS;
import UNABLE_TO_CONNECT = ErrorMessage.Driver.UNABLE_TO_CONNECT;

export class TypeDBDriverImpl implements TypeDBDriver {
    private _isOpen: boolean;
    private readonly _isCloud: boolean;

    private readonly _initAddresses: string[];
    private readonly _credential: TypeDBCredential;
    private _userManager: UserManagerImpl;

    private readonly _serverDrivers: Map<string, ServerDriver>;

    private readonly _databases: TypeDBDatabaseManagerImpl;
    _database_cache: { [db: string]: Database };

    private readonly _sessions: { [id: string]: TypeDBSessionImpl };

    constructor(addresses: string | string[], credential?: TypeDBCredential) {
        if (typeof addresses === 'string') addresses = [addresses];
        this._initAddresses = addresses;
        this._credential = credential;

        this._isOpen = false;
        this._isCloud = credential != null;
        this._serverDrivers = new Map([]);
        this._databases = new TypeDBDatabaseManagerImpl(this);
        this._database_cache = {};
        this._sessions = {};
    }

    async open(): Promise<TypeDBDriver> {
        if (this._isCloud) return this.openCloud()
        else return this.openCore()
    }

    private async openCore(): Promise<TypeDBDriver> {
        const serverAddress = this._initAddresses[0];
        const serverStub = new TypeDBStubImpl(serverAddress, this._credential);
        await serverStub.open();
        const advertisedAddress = (await serverStub.serversAll(RequestBuilder.ServerManager.allReq())).servers[0].address;
        this.serverDrivers.set(advertisedAddress, new ServerDriver(serverAddress, serverStub));
        this._isOpen = true;
        return this;
    }

    private async openCloud(): Promise<TypeDBDriver> {
        const serverAddresses = await this.fetchCloudServerAddresses();
        const openReqs: Promise<void>[] = []
        for (const addr of serverAddresses) {
            const serverStub = new TypeDBStubImpl(addr, this._credential);
            openReqs.push(serverStub.open());
            this.serverDrivers.set(addr, new ServerDriver(addr, serverStub));
        }
        try {
            await Promise.any(openReqs);
        } catch (e) {
            throw new TypeDBDriverError(CLOUD_UNABLE_TO_CONNECT.message(e));
        }
        this._userManager = new UserManagerImpl(this);
        this._isOpen = true;
        return this;
    }

    private async fetchCloudServerAddresses(): Promise<string[]> {
        for (const address of this._initAddresses) {
            try {
                const stub = new TypeDBStubImpl(address, this._credential);
                await stub.open();
                const res = await stub.serversAll(RequestBuilder.ServerManager.allReq());
                const members = res.servers.map(x => x.address);
                return members;
            } catch (e) {
                console.error(`Fetching cloud servers from ${address} failed.`, e);
            }
        }
        throw new TypeDBDriverError(CLOUD_UNABLE_TO_CONNECT.message(this._initAddresses.join(",")));
    }

    isOpen(): boolean {
        return this._isOpen;
    }

    isCloud(): boolean {
        return this._isCloud;
    }

    async session(databaseName: string, type: SessionType, options?: TypeDBOptions): Promise<TypeDBSessionImpl> {
        if (!this.isOpen()) throw new TypeDBDriverError(DRIVER_NOT_OPEN);
        if (!options) options = new TypeDBOptions();
        const session = new TypeDBSessionImpl(databaseName, type, options, this);
        await session.open();
        if (this._sessions[session.id]) throw new TypeDBDriverError(SESSION_ID_EXISTS.message(session.id));
        this._sessions[session.id] = session;
        return session;
    }

    get databases(): TypeDBDatabaseManagerImpl {
        if (!this.isOpen()) throw new TypeDBDriverError(DRIVER_NOT_OPEN);
        return this._databases;
    }

    async user(): Promise<UserImpl> {
        if (!this.isOpen()) throw new TypeDBDriverError(DRIVER_NOT_OPEN);
        return await this.users.get(this._credential.username)
    }

    get users(): UserManagerImpl {
        if (!this.isOpen()) throw new TypeDBDriverError(DRIVER_NOT_OPEN);
        return this._userManager;
    }

    get serverDrivers(): Map<string, ServerDriver> {
        return this._serverDrivers;
    }

    async close(): Promise<void> {
        if (this.isOpen()) {
            this._isOpen = false;
            for (const serverDriver of Object.values(this._serverDrivers)) {
                await serverDriver.close();
            }
        }
    }

    closeSession(session: TypeDBSessionImpl): void {
        delete this._sessions[session.id];
    }
}

export class ServerDriver {
    private readonly _address: string;
    private readonly _stub: TypeDBStub;
    private readonly _requestTransmitter: RequestTransmitter;

    constructor(address: string, stub: TypeDBStub) {
        this._address = address;
        this._stub = stub;
        this._requestTransmitter = new RequestTransmitter();
    }

    get address(): string {
        return this._address;
    }

    get stub():TypeDBStub {
        return this._stub;
    }

    get transmitter(): RequestTransmitter {
        return this._requestTransmitter;
    }

    close(): void {
        this.stub.close();
    }
}
