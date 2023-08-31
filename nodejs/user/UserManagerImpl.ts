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

import {User} from "../api/connection/user/User";
import {UserManager} from "../api/connection/user/UserManager";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {UserImpl} from "../dependencies_internal";
import {ServerClient, TypeDBClientImpl} from "../connection/TypeDBClientImpl";
import {TypeDBDatabaseImpl} from "../connection/TypeDBDatabaseImpl";

export class UserManagerImpl implements UserManager {
    static _SYSTEM_DB = "_system";
    private readonly _client: TypeDBClientImpl;

    constructor(client: TypeDBClientImpl) {
        this._client = client;
    }

    async all(): Promise<User[]> {
        return this.runFailsafe(client =>
            client.stub.usersAll(RequestBuilder.UserManager.allReq())
                .then((res) => res.users.map(user => UserImpl.of(user, this._client)))
        );
    }

    async contains(username: string): Promise<boolean> {
        return this.runFailsafe(client =>
            client.stub.usersContains(RequestBuilder.UserManager.containsReq(username))
        );
    }

    async create(username: string, password: string): Promise<void> {
        return this.runFailsafe(client =>
            client.stub.usersCreate(RequestBuilder.UserManager.createReq(username, password))
        );
    }

    async delete(username: string): Promise<void> {
        return this.runFailsafe(client =>
            client.stub.usersDelete(RequestBuilder.UserManager.deleteReq(username))
        );
    }

    async get(username: string): Promise<UserImpl> {
        const user = (await this.runFailsafe((client) =>
            client.stub.usersGet(RequestBuilder.UserManager.getReq(username))
        )).user;
        return UserImpl.of(user, this._client);
    }

    async passwordSet(username: string, password: string): Promise<void> {
        return this.runFailsafe((client) =>
            client.stub.usersPasswordSet(RequestBuilder.UserManager.passwordSetReq(username, password))
        );
    }

    async runFailsafe<T>(task: (client: ServerClient) => Promise<T>): Promise<T> {
        return await (await TypeDBDatabaseImpl.get(UserManagerImpl._SYSTEM_DB, this._client)).runOnPrimaryReplica(task);
    }
}
