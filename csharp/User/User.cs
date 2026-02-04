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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.User
{
    /// <summary>
    /// TypeDB user information.
    /// </summary>
    public class User : NativeObjectWrapper<Pinvoke.User>, IUser
    {
        private readonly UserManager _users;

        internal User(Pinvoke.User nativeUser, UserManager users)
            : base(nativeUser)
        {
            _users = users;
        }

        /// <inheritdoc/>
        public string Username
        {
            get { return Pinvoke.typedb_driver.user_get_name(NativeObject); }
        }

        /// <inheritdoc/>
        public void UpdatePassword(string password)
        {
            try
            {
                Pinvoke.typedb_driver.user_update_password(NativeObject, password);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <summary>
        /// Deletes this user.
        /// </summary>
        public void Delete()
        {
            try
            {
                // Released() transfers ownership to user_delete(), preventing double-free
                // when GC later runs the destructor. This matches Java's pattern.
                Pinvoke.typedb_driver.user_delete(NativeObject.Released());
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
