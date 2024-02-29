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

using System.Collections.Generic;

using Vaticle.Typedb.Driver.Api;

namespace Vaticle.Typedb.Driver.Api
{
    /**
     * Provides access to all user management methods.
     */
    public interface IUserManager
    {
        /**
         * Checks if a user with the given name exists.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.Contains(username);
         * </pre>
         *
         * @param username The user name to be checked
         */
        bool Contains(string username);

        /**
         * Creates a user with the given name &amp; password.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.Create(username, password);
         * </pre>
         *
         * @param username The name of the user to be created
         * @param password The password of the user to be created
         */
        void Create(string username, string password);

        /**
         * Deletes a user with the given name.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.Delete(username);
         * </pre>
         *
         * @param username The name of the user to be deleted
         */
        void Delete(string username);

        /**
         * Retrieves a user with the given name.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.Get(username);
         * </pre>
         *
         * @param username The name of the user to retrieve
         */
        IUser? Get(string username);

        /**
         * Retrieves all users which exist on the TypeDB server.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.All;
         * </pre>
         */
        ISet<IUser> All { get; }

        /**
         * Sets a new password for a user. This operation can only be performed by administrators.
         *
         * <h3>Examples</h3>
         * <pre>
         * driver.Users.SetPassword(username, password);
         * </pre>
         *
         * @param username The name of the user to set the password of
         * @param password The new password
         */
        void SetPassword(string username, string password);
    }
}
