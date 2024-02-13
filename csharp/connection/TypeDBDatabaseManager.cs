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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api.Database;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Util;

using DriverError = Vaticle.Typedb.Driver.Common.Exception.Error.Driver;

namespace Vaticle.Typedb.Driver.Connection
{
    public class TypeDBDatabaseManager : NativeObjectWrapper<Pinvoke.DatabaseManager>, IDatabaseManager
    {
        public TypeDBDatabaseManager(Pinvoke.Connection nativeConnection)
            : base(NewNative(nativeConnection))
        {}

        private static Pinvoke.DatabaseManager NewNative(Pinvoke.Connection nativeConnection)
        {
            try
            {
                return Pinvoke.typedb_driver.database_manager_new(nativeConnection);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public IDatabase Get(string name)
        {
            InputChecker.NonEmptyString(name, DriverError.MISSING_DB_NAME);

            try
            {
                return new TypeDBDatabase(Pinvoke.typedb_driver.databases_get(NativeObject, name));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool Contains(string name)
        {
            InputChecker.NonEmptyString(name, DriverError.MISSING_DB_NAME);

            try
            {
                return Pinvoke.typedb_driver.databases_contains(NativeObject, name);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Create(string name)
        {
            InputChecker.NonEmptyString(name, DriverError.MISSING_DB_NAME);

            try
            {
                Pinvoke.typedb_driver.databases_create(NativeObject, name);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public ICollection<IDatabase> All
        {
            get
            {
                try
                {
                    return new NativeEnumerable<Pinvoke.Database>(
                        Pinvoke.typedb_driver.databases_all(NativeObject))
                        .Select(obj => new TypeDBDatabase(obj))
                        .ToList<IDatabase>();
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }
    }
}
