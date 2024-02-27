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
using System.Linq;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.User
{
    public class UserManager : NativeObjectWrapper<Pinvoke.UserManager>, IUserManager 
    {
        public UserManager(Pinvoke.Connection nativeConnection) 
            : base(NewNative(nativeConnection))
        {
        }

        private static Pinvoke.UserManager NewNative(Pinvoke.Connection nativeConnection) 
        {
            try 
            {
                return Pinvoke.typedb_driver.user_manager_new(nativeConnection);
            } 
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool Contains(string username) 
        {
            try 
            {
                return Pinvoke.typedb_driver.users_contains(NativeObject, username);
            }
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Create(string username, string password)
        {
            try
            {
                Pinvoke.typedb_driver.users_create(NativeObject, username, password);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Delete(string username)
        {
            try
            {
                Pinvoke.typedb_driver.users_delete(NativeObject, username);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IUser Get(string username)
        {
            try
            {
                Pinvoke.User user = Pinvoke.typedb_driver.users_get(NativeObject, username);
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

        public ICollection<IUser> All
        {
            get
            {
                try
                {
                    return new NativeEnumerable<Pinvoke.User>(
                        Pinvoke.typedb_driver.users_all(NativeObject))
                        .Select(obj => new User(obj, this))
                        .ToHashSet<IUser>();
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        public void SetPassword(string username, string password)
        {
            try
            {
                Pinvoke.typedb_driver.users_set_password(NativeObject, username, password);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IUser CurrentUser
        {
            get
            {
                try
                {
                    return new User(Pinvoke.typedb_driver.users_current_user(NativeObject), this);
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }
    }
}
