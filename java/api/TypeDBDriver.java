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

package com.vaticle.typedb.driver.api;

import com.vaticle.typedb.driver.api.database.DatabaseManager;

import javax.annotation.CheckReturnValue;

public interface TypeDBDriver extends AutoCloseable {

    /**
     * Checks whether this connection is presently open.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.isOpen();
     * </pre>
     */
    @CheckReturnValue
    boolean isOpen();

    /**
     * The <code>DatabaseManager</code> for this connection, providing access to database management methods.
     */
    @CheckReturnValue
    DatabaseManager databases();

    /**
     * Opens a communication tunnel (transaction) to the given database on the running TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.transaction(database, sessionType);
     * </pre>
     *
     * @param database The name of the database with which the session connects
     * @param type The type of session to be created (DATA or SCHEMA)
     */
    @CheckReturnValue
    TypeDBTransaction transaction(String database, TypeDBTransaction.Type type);

//    @CheckReturnValue
//    TypeDBTransaction transaction(String database, TypeDBTransaction.Type type, TypeDBOptions options);

    /**
     * Closes the driver. Before instantiating a new driver, the driver that’s currently open should first be closed.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.close()
     * </pre>
     */
    void close();

    /**
     * Returns the logged-in user for the connection. Only for TypeDB Cloud.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.user();
     * </pre>
     */
//    @CheckReturnValue
//    User user();

    /**
     * The <code>UserManager</code> instance for this connection, providing access to user management methods.
     * Only for TypeDB Cloud.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users();
     * </pre>
     */
//    @CheckReturnValue
//    UserManager users();
}
