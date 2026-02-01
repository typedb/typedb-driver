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
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.User
{
    /// <summary>
    /// Provides access to user management operations.
    /// </summary>
    public class UserManager : IUserManager
    {
        private readonly Pinvoke.TypeDBDriver _nativeDriver;

        /// <summary>
        /// Creates a new user manager for the given driver.
        /// </summary>
        /// <param name="nativeDriver">The native driver object.</param>
        public UserManager(Pinvoke.TypeDBDriver nativeDriver)
        {
            _nativeDriver = nativeDriver;
        }

        /// <inheritdoc/>
        public bool Contains(string username)
        {
            try
            {
                return Pinvoke.typedb_driver.users_contains(_nativeDriver, username);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public void Create(string username, string password)
        {
            try
            {
                Pinvoke.typedb_driver.users_create(_nativeDriver, username, password);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public IUser? Get(string username)
        {
            try
            {
                Pinvoke.User user = Pinvoke.typedb_driver.users_get(_nativeDriver, username);
                if (user != null)
                {
                    return new User(user, this);
                }

                return null;
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public ISet<IUser> GetAll()
        {
            try
            {
                return new NativeEnumerable<Pinvoke.User>(
                    Pinvoke.typedb_driver.users_all(_nativeDriver))
                    .Select(obj => new User(obj, this))
                    .ToHashSet<IUser>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public IUser GetCurrentUser()
        {
            try
            {
                return new User(Pinvoke.typedb_driver.users_get_current_user(_nativeDriver), this);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
