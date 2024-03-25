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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Connection;
using TypeDB.Driver.User;

namespace TypeDB.Driver.Connection
{
    public class TypeDBDriver : NativeObjectWrapper<Pinvoke.Connection>, ITypeDBDriver
    {
        private readonly IDatabaseManager _databaseManager;
        private readonly UserManager _userManager;

        public TypeDBDriver(string address)
            : this(OpenCore(address))
        {}

        public TypeDBDriver(ICollection<string> initAddresses, TypeDBCredential credential)
            : this(OpenCloud(initAddresses, credential))
        {}

        private TypeDBDriver(Pinvoke.Connection connection)
            : base(connection)
        {
            _databaseManager = new TypeDBDatabaseManager(this.NativeObject);
            _userManager = new UserManager(this.NativeObject);
        }

        private static Pinvoke.Connection OpenCore(string address)
        {
            try
            {
                return Pinvoke.typedb_driver.connection_open_core(address);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private static Pinvoke.Connection OpenCloud(ICollection<string> initAddresses, TypeDBCredential credential)
        {
            try
            {
                return Pinvoke.typedb_driver.connection_open_cloud(initAddresses.ToArray(), credential.NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool IsOpen()
        {
            return Pinvoke.typedb_driver.connection_is_open(NativeObject);
        }

        public IDatabaseManager Databases
        {
            get { return _databaseManager; }
        }

        public IUser GetCurrentUser()
        {
            return _userManager.GetCurrentUser();
        }

        public IUserManager Users
        {
            get { return _userManager; }
        }

        public ITypeDBSession Session(string database, SessionType type)
        {
            return Session(database, type, new TypeDBOptions());
        }

        public ITypeDBSession Session(
            string database, SessionType type, TypeDBOptions options)
        {
            return new TypeDBSession(Databases, database, type, options);
        }

        public void Close()
        {
            if (!IsOpen())
            {
                return;
            }

            try
            {
                Pinvoke.typedb_driver.connection_force_close(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Dispose()
        {
            Close();
        }
    }
}
