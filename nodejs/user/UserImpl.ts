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

import {User as UserProto} from "typedb-protocol/proto/user";
import {User} from "../api/connection/user/User";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {TypeDBClientImpl} from "../connection/TypeDBClientImpl";

export class UserImpl implements User {
    private readonly _client: TypeDBClientImpl;
    private readonly _username: string;
    private readonly _passwordExpirySeconds: number;

    constructor(client: TypeDBClientImpl, username: string, passwordExpirySeconds: number) {
        this._client = client;
        this._username = username;
        this._passwordExpirySeconds = passwordExpirySeconds;
    }

    static of(user: UserProto, client: TypeDBClientImpl): UserImpl {
        if (user.has_password_expiry_seconds) return new UserImpl(client, user.username, user.password_expiry_seconds);
        else return new UserImpl(client, user.username, null);
    }

    get passwordExpirySeconds(): number {
        return this._passwordExpirySeconds;
    }

    async passwordUpdate(oldPassword: string, newPassword: string): Promise<void> {
        return this._client.users.runFailsafe((client) =>
            client.stub.userPasswordUpdate(RequestBuilder.User.passwordUpdateReq(this.username, oldPassword, newPassword))
        );
    }

    get username(): string {
        return this._username;
    }
}
