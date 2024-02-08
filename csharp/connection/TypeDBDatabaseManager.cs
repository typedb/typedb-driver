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

using System;
using System.Collections.Generic;
using System.Linq;

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api.Database;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Common.Exception;
using DriverError = com.vaticle.typedb.driver.Common.Exception.Error.Driver;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBDatabaseManager : NativeObjectWrapper<pinvoke.DatabaseManager>, IDatabaseManager
    {
        public TypeDBDatabaseManager(pinvoke.Connection nativeConnection)
            : base(NewNative(nativeConnection))
        {}

        private static pinvoke.DatabaseManager NewNative(pinvoke.Connection nativeConnection)
        {
            try
            {
                return pinvoke.typedb_driver.database_manager_new(nativeConnection);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IDatabase Get(string name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(DriverError.MISSING_DB_NAME);
            }

            try
            {
                return new TypeDBDatabase(pinvoke.typedb_driver.databases_get(NativeObject, name));
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool Contains(string name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(DriverError.MISSING_DB_NAME);
            }

            try
            {
                return pinvoke.typedb_driver.databases_contains(NativeObject, name);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Create(string name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(DriverError.MISSING_DB_NAME);
            }

            try
            {
                pinvoke.typedb_driver.databases_create(NativeObject, name);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ICollection<IDatabase> GetAll()
        {
            try
            {
                return new NativeEnumerable<pinvoke.Database>(
                    pinvoke.typedb_driver.databases_all(NativeObject))
                    .Select(obj => new TypeDBDatabase(obj))
                    .ToList<IDatabase>();
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
