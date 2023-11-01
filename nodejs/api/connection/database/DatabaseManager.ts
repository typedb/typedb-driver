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

import {Database} from "./Database";

/** Provides access to all database management methods. */
export interface DatabaseManager {
    /**
     * Retrieve the database with the given name.
     *
     * ### Examples
     *
     * ```ts
     * driver.databases().get(name)
     * ```
     *
     * @param name - The name of the database to retrieve
     */
    get(name: string): Promise<Database>;

    /**
     * Retrieves all databases present on the TypeDB server
     *
     * ### Examples
     *
     * ```ts
     * driver.databases().all()
     * ```
     */
    all(): Promise<Database[]>;

    /**
     * Checks if a database with the given name exists
     *
     * ### Examples
     *
     * ```ts
     * driver.databases().contains(name)
     * ```
     *
     * @param name - The database name to be checked
     */
    contains(name: string): Promise<boolean>;

    /**
     * Create a database with the given name
     *
     * ### Examples
     *
     * ```ts
     * driver.databases().create(name)
     * ```
     *
     * @param name - The name of the database to be created
     */
    create(name: string): Promise<void>;
}
