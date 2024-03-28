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

import {User as UserProto} from "typedb-protocol/proto/user";
import {User} from "../api/connection/user/User";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {TypeDBDriverImpl} from "../connection/TypeDBDriverImpl";

export class UserImpl implements User {
    private readonly _driver: TypeDBDriverImpl;
    private readonly _username: string;
    private readonly _passwordExpirySeconds: number;

    constructor(driver: TypeDBDriverImpl, username: string, passwordExpirySeconds: number) {
        this._driver = driver;
        this._username = username;
        this._passwordExpirySeconds = passwordExpirySeconds;
    }

    static of(user: UserProto, driver: TypeDBDriverImpl): UserImpl {
        if (user.has_password_expiry_seconds) return new UserImpl(driver, user.username, user.password_expiry_seconds);
        else return new UserImpl(driver, user.username, null);
    }

    get passwordExpirySeconds(): number {
        return this._passwordExpirySeconds;
    }

    async passwordUpdate(oldPassword: string, newPassword: string): Promise<void> {
        return this._driver.users.runFailsafe((driver) =>
            driver.stub.userPasswordUpdate(RequestBuilder.User.passwordUpdateReq(this.username, oldPassword, newPassword))
        );
    }

    get username(): string {
        return this._username;
    }
}
