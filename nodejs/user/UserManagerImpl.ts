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

import {User} from "../api/connection/user/User";
import {UserManager} from "../api/connection/user/UserManager";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {UserImpl} from "../dependencies_internal";
import {ServerDriver, TypeDBDriverImpl} from "../connection/TypeDBDriverImpl";
import {TypeDBDatabaseImpl} from "../connection/TypeDBDatabaseImpl";
import {TypeDBDriverError} from "../common/errors/TypeDBDriverError";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import USER_MANAGEMENT_CLOUD_ONLY = ErrorMessage.Driver.USER_MANAGEMENT_CLOUD_ONLY;

export class UserManagerImpl implements UserManager {
    static _SYSTEM_DB = "_system";
    private readonly _driver: TypeDBDriverImpl;

    constructor(driver: TypeDBDriverImpl) {
        this._driver = driver;
    }

    async all(): Promise<User[]> {
        return this.runFailsafe(driver =>
            driver.stub.usersAll(RequestBuilder.UserManager.allReq())
                .then((res) => res.users.map(user => UserImpl.of(user, this._driver)))
        );
    }

    async contains(username: string): Promise<boolean> {
        return this.runFailsafe(driver =>
            driver.stub.usersContains(RequestBuilder.UserManager.containsReq(username))
        );
    }

    async create(username: string, password: string): Promise<void> {
        return this.runFailsafe(driver =>
            driver.stub.usersCreate(RequestBuilder.UserManager.createReq(username, password))
        );
    }

    async delete(username: string): Promise<void> {
        return this.runFailsafe(driver =>
            driver.stub.usersDelete(RequestBuilder.UserManager.deleteReq(username))
        );
    }

    async get(username: string): Promise<UserImpl> {
        const user = (await this.runFailsafe((driver) =>
            driver.stub.usersGet(RequestBuilder.UserManager.getReq(username))
        )).user;
        return UserImpl.of(user, this._driver);
    }

    async passwordSet(username: string, password: string): Promise<void> {
        return this.runFailsafe((driver) =>
            driver.stub.usersPasswordSet(RequestBuilder.UserManager.passwordSetReq(username, password))
        );
    }

    async runFailsafe<T>(task: (driver: ServerDriver) => Promise<T>): Promise<T> {
        if (!this._driver.isCloud()) throw new TypeDBDriverError(USER_MANAGEMENT_CLOUD_ONLY);
        return await (await TypeDBDatabaseImpl.get(UserManagerImpl._SYSTEM_DB, this._driver)).runOnPrimaryReplica(task);
    }
}
