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

import {Database} from "../api/connection/database/Database";
import {DatabaseManager} from "../api/connection/database/DatabaseManager";
import {ErrorMessage} from "../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../common/errors/TypeDBDriverError";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {ServerDriver, TypeDBDriverImpl} from "./TypeDBDriverImpl";
import {TypeDBDatabaseImpl} from "./TypeDBDatabaseImpl";
import ENTERPRISE_ALL_NODES_FAILED = ErrorMessage.Driver.ENTERPRISE_ALL_NODES_FAILED;
import ENTERPRISE_REPLICA_NOT_PRIMARY = ErrorMessage.Driver.ENTERPRISE_REPLICA_NOT_PRIMARY;
import DB_DOES_NOT_EXIST = ErrorMessage.Driver.DATABASE_DOES_NOT_EXIST;

export class TypeDBDatabaseManagerImpl implements DatabaseManager {
    private readonly _driver: TypeDBDriverImpl;

    constructor(driver: TypeDBDriverImpl) {
        this._driver = driver;
    }

    async get(name: string): Promise<Database> {
        if (!await this.contains(name)) {
            throw new TypeDBDriverError(DB_DOES_NOT_EXIST.message(name));
        }
        if (name in this._driver._database_cache) {
            return this._driver._database_cache[name];
        } else {
            return await TypeDBDatabaseImpl.get(name, this._driver);
        }
    }

    async contains(name: string): Promise<boolean> {
        return await this.runFailsafe(name, driver =>
            driver.stub.databasesContains(RequestBuilder.DatabaseManager.containsReq(name))
        );
    }

    async create(name: string): Promise<void> {
        return await this.runFailsafe(name, driver =>
            driver.stub.databasesCreate(RequestBuilder.DatabaseManager.createReq(name))
        );
    }

    async all(): Promise<Database[]> {
        let errors = "";
        for (const serverDriver of this._driver.serverDrivers.values()) {
            try {
                const dbs = await serverDriver.stub.databasesAll(RequestBuilder.DatabaseManager.allReq());
                return dbs.databases.map(db => TypeDBDatabaseImpl.of(db, this._driver));
            } catch (e) {
                errors += `- ${serverDriver.address}: ${e}\n`;
            }
        }
        throw new TypeDBDriverError(ENTERPRISE_ALL_NODES_FAILED.message(errors));
    }

    private async runFailsafe<T>(name: string, task: (driver: ServerDriver) => Promise<T>): Promise<T> {
        let errors = "";
        for (const serverDriver of this._driver.serverDrivers.values()) {
            try {
                return await task(serverDriver);
            } catch (e) {
                if (e instanceof TypeDBDriverError && ENTERPRISE_REPLICA_NOT_PRIMARY === e.messageTemplate) {
                    return await (await TypeDBDatabaseImpl.get(name, this._driver)).runOnPrimaryReplica(task);
                } else errors += `- ${serverDriver.address}: ${e}\n`;
            }
        }
        throw new TypeDBDriverError(ENTERPRISE_ALL_NODES_FAILED.message(errors));
    }
}
