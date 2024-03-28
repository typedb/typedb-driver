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
    public class User : NativeObjectWrapper<Pinvoke.User>, IUser 
    {
        private long? _passwordExpirySeconds;
        private bool _passwordExpirySecondsFetched = false;

        private readonly UserManager _users;

        internal User(Pinvoke.User nativeUser, UserManager users)
            : base(nativeUser) 
        {
            _users = users;
        }

        public string Username 
        {
            get { return Pinvoke.typedb_driver.user_get_username(NativeObject); }
        }

        public long? PasswordExpirySeconds
        {
            get
            {
                if (_passwordExpirySecondsFetched)
                {
                    return _passwordExpirySeconds;
                }

                long res = Pinvoke.typedb_driver.user_get_password_expiry_seconds(NativeObject);
                _passwordExpirySecondsFetched = true;

                if (res >= 0)
                {
                    _passwordExpirySeconds = res;
                }

                return _passwordExpirySeconds;
            }
        }

        public void UpdatePassword(string passwordOld, string passwordNew)
        {
            try
            {
                Pinvoke.typedb_driver.user_password_update(
                    NativeObject, _users.NativeObject, passwordOld, passwordNew);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
