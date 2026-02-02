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

import javax.annotation.CheckReturnValue;

/**
 * TypeDB user information
 */
public interface User {
    /**
     * Returns the name of this user.
     */
    @CheckReturnValue
    String name();

    /**
     * Updates the password for this user, using default strong consistency.
     * See {@link #updatePassword(String, ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * user.updatePassword("new-password");
     * </pre>
     *
     * @param password The new password
     */
    default void updatePassword(String password) {
        updatePassword(password, null);
    }

    /**
     * Updates the password for this user.
     *
     * <h3>Examples</h3>
     * <pre>
     * user.updatePassword("new-password", new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param password         The new password
     * @param consistencyLevel The consistency level to use for the operation
     */
    void updatePassword(String password, ConsistencyLevel consistencyLevel);

    /**
     * Deletes this user, using default strong consistency.
     * See {@link #delete(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * user.delete();
     * </pre>
     */
    default void delete() {
        delete(null);
    }

    /**
     * Deletes this user.
     *
     * <h3>Examples</h3>
     * <pre>
     * user.delete(new ConsistencyLevel.Strong());
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    void delete(ConsistencyLevel consistencyLevel);
}
