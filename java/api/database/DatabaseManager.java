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

package com.vaticle.typedb.driver.api.database;

import javax.annotation.CheckReturnValue;
import java.util.List;

/**
 * Provides access to all database management methods.
 */
public interface DatabaseManager {

    /**
     * Retrieve the database with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases().get(name)
     * </pre>
     *
     * @param name The name of the database to retrieve
     */
    @CheckReturnValue
    Database get(String name);

    /**
     * Checks if a database with the given name exists
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases().contains(name)
     * </pre>
     *
     * @param name The database name to be checked
     */
    @CheckReturnValue
    boolean contains(String name);

    /**
     * Create a database with the given name
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases().create(name)
     * </pre>
     *
     * @param name The name of the database to be created
     */
    // TODO: Return type should be 'Database' but right now that would require 2 server calls in Enterprise
    void create(String name);

    /**
     * Retrieves all databases present on the TypeDB server
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.databases().all()
     * </pre>
     */
    @CheckReturnValue
    List<Database> all();
}
