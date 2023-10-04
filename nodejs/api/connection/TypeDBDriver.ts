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

import {DatabaseManager} from "./database/DatabaseManager";
import {TypeDBOptions} from "./TypeDBOptions";
import {SessionType, TypeDBSession} from "./TypeDBSession";
import {UserManager} from "./user/UserManager";
import {User} from "./user/User";

export interface TypeDBDriver {
    /**
     * Checks whether this connection is presently open.
     *
     * ### Examples
     *
     * ```ts
     * driver.isOpen()
     * ```
     */
    isOpen(): boolean;

    /** The <code>DatabaseManager</code> for this connection, providing access to database management methods. */
    readonly databases: DatabaseManager;

    session(database: string, type: SessionType, options?: TypeDBOptions): Promise<TypeDBSession>;

    /**
     * Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
     *
     * ### Examples
     *
     * ```ts
     * driver.close()
     * ```
     */
    close(): Promise<void>;

    /** The <code>UserManager</code> instance for this connection, providing access to user management methods. */
    readonly users: UserManager;

    /**
     * Returns the logged-in user for the connection.
     *
     * ### Examples
     *
     * ```ts
     * driver.user()
     * ```
     */
    user(): Promise<User>;
}
