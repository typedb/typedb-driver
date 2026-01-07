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

using System.Collections.Generic;

using TypeDB.Driver.Api;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// Provides access to all user management methods.
    /// </summary>
    public interface IUserManager
    {
        /// <summary>
        /// Checks if a user with the given name exists.
        /// </summary>
        /// <param name="username">The user name to be checked.</param>
        /// <returns><c>true</c> if the user exists, <c>false</c> otherwise.</returns>
        /// <example>
        /// <code>
        /// driver.Users.Contains(username);
        /// </code>
        /// </example>
        bool Contains(string username);

        /// <summary>
        /// Creates a user with the given name and password.
        /// </summary>
        /// <param name="username">The name of the user to be created.</param>
        /// <param name="password">The password of the user to be created.</param>
        /// <example>
        /// <code>
        /// driver.Users.Create(username, password);
        /// </code>
        /// </example>
        void Create(string username, string password);

        /// <summary>
        /// Retrieves a user with the given name.
        /// </summary>
        /// <param name="username">The name of the user to retrieve.</param>
        /// <returns>The user if it exists, or <c>null</c> otherwise.</returns>
        /// <example>
        /// <code>
        /// driver.Users.Get(username);
        /// </code>
        /// </example>
        IUser? Get(string username);

        /// <summary>
        /// Retrieves all users which exist on the TypeDB server.
        /// </summary>
        /// <returns>A set of all users.</returns>
        /// <example>
        /// <code>
        /// driver.Users.GetAll();
        /// </code>
        /// </example>
        ISet<IUser> GetAll();

        /// <summary>
        /// Retrieves the currently logged in user.
        /// </summary>
        /// <returns>The current user.</returns>
        /// <example>
        /// <code>
        /// driver.Users.GetCurrentUser();
        /// </code>
        /// </example>
        IUser GetCurrentUser();
    }
}
