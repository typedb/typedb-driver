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

import javax.annotation.CheckReturnValue;
import java.util.Set;

/**
 * Provides access to all user management methods.
 */
public interface UserManager {
    /**
     * Checks if a user with the given name exists.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().contains(username);
     * </pre>
     *
     * @param username The user name to be checked
     */
    @CheckReturnValue
    boolean contains(String username);

    /**
     * Retrieves a user with the given name.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().get(username);
     * </pre>
     *
     * @param username The name of the user to retrieve
     */
    @CheckReturnValue
    User get(String username);

    // TODO: I don't like this, leaving this way for now. Use driver.users().get(username)

    /**
     * Retrieves the name of the user who opened the current connection.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().getCurrentUsername();
     * </pre>
     */
    @CheckReturnValue
    User getCurrentUser();

    /**
     * Retrieves all users which exist on the TypeDB server.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().all();
     * </pre>
     */
    Set<User> all();

    /**
     * Creates a user with the given name &amp; password.
     *
     * <h3>Examples</h3>
     * <pre>
     * driver.users().create(username, password);
     * </pre>
     *
     * @param username The name of the user to be created
     * @param password The password of the user to be created
     */
    void create(String username, String password);
}
