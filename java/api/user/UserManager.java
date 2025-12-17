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

package com.typedb.driver.api.user;

import com.typedb.driver.api.ConsistencyLevel;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;
import java.util.Set;

/**
 * Provides access to all user management methods.
 */
public interface UserManager {
    /**
     * Retrieves all users which exist on the TypeDB server, using default strong consistency.
     * See {@link #all(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().all();
     * </pre>
     */
    default Set<User> all() throws TypeDBDriverException {
        return all(null);
    }

    /**
     * Retrieves all users which exist on the TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().all(new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    Set<User> all(ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Checks if a user with the given name exists., using default strong consistency.
     * See {@link #contains(String, ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().contains(username);
     * </pre>
     *
     * @param username The username to be checked
     */
    @CheckReturnValue
    default boolean contains(String username) throws TypeDBDriverException {
        return contains(username, null);
    }

    /**
     * Checks if a user with the given name exists.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().contains(username, new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param username         The username to be checked
     * @param consistencyLevel The consistency level to use for the operation
     */
    @CheckReturnValue
    boolean contains(String username, ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Retrieves a user with the given name, using default strong consistency.
     * See {@link #get(String, ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().get(username);
     * </pre>
     *
     * @param username The name of the user to retrieve
     */
    @CheckReturnValue
    default User get(String username) throws TypeDBDriverException {
        return get(username, null);
    }

    /**
     * Retrieves a user with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().get(username, new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param username         The name of the user to retrieve
     * @param consistencyLevel The consistency level to use for the operation
     */
    @CheckReturnValue
    User get(String username, ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Retrieves the name of the user who opened the current connection, using default strong consistency.
     * See {@link #getCurrent(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().getCurrent();
     * </pre>
     */
    @CheckReturnValue
    default User getCurrent() throws TypeDBDriverException {
        return getCurrent(null);
    }

    /**
     * Retrieves the name of the user who opened the current connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().getCurrent(new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    @CheckReturnValue
    User getCurrent(ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Creates a user with the given name &amp; password, using default strong consistency.
     * See {@link #create(String, String, ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().create(username, password);
     * </pre>
     *
     * @param username The name of the user to be created
     * @param password The password of the user to be created
     */
    default void create(String username, String password) throws TypeDBDriverException {
        create(username, password, null);
    }

    /**
     * Creates a user with the given name &amp; password.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().create(username, password, new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param username         The name of the user to be created
     * @param password         The password of the user to be created
     * @param consistencyLevel The consistency level to use for the operation
     */
    void create(String username, String password, ConsistencyLevel consistencyLevel) throws TypeDBDriverException;
}
