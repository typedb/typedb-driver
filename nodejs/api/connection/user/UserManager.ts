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

import {User} from "./User";

/** Provides access to all user management methods. */
export interface UserManager {
    /**
     * Checks if a user with the given name exists.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.contains(username)
     * ```
     *
     * @param username - The user name to be checked
     */
    contains(name: string): Promise<boolean>;

    /**
     * Create a user with the given name &amp; password.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.create(username, password)
     * ```
     *
     * @param username - The name of the user to be created
     * @param password - The password of the user to be created
     */
    create(name: string, password: string): Promise<void>;

    /**
     * Deletes a user with the given name.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.delete(username)
     * ```
     *
     * @param username - The name of the user to be deleted
     */
    delete(name: string): Promise<void>;

    /**
     * Retrieves all users which exist on the TypeDB server.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.all()
     * ```
     */
    all(): Promise<User[]>;

    /**
     * Sets a new password for a user. This operation can only be performed by administrators.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.passwordSet(username, password)
     * ```
     *
     * @param username - The name of the user to set the password of
     * @param password - The new password
     */
    passwordSet(name: string, password: string): Promise<void>;

    /**
     * Retrieve a user with the given name.
     *
     * ### Examples
     *
     * ```ts
     * driver.users.get(username)
     * ```
     *
     * @param username - The name of the user to retrieve
     */
    get(name: string): Promise<User>;
}
