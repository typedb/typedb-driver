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

using com.vaticle.typedb.driver.pinvoke;
using com.vaticle.typedb.driver.Api.Database;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Common.Exception;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBDatabaseManager: NativeObject<pinvoke.DatabaseManager>, IDatabaseManager
    {
        public TypeDBDatabaseManager(pinvoke.Connection nativeConnection)
            : base(newNative(nativeConnection))
        {}

        private static pinvoke.DatabaseManager NewNative(pinvoke.Connection nativeConnection)
        {
            try
            {
                return pinvoke.database_manager_new(nativeConnection);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override Database Get(string name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(MISSING_DB_NAME);
            }

            try
            {
                return new TypeDBDatabase(pinvoke.databases_get(nativeObject, name));
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override bool Contains(string name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(MISSING_DB_NAME);
            }

            try
            {
                return pinvoke.databases_contains(nativeObject, name);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override void create(String name)
        {
            if (String.IsNullOrEmpty(name))
            {
                throw new TypeDBDriverException(MISSING_DB_NAME);
            }

            try
            {
                pinvoke.databases_create(nativeObject, name);
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public override List<IDatabase> All()
        {
            try
            {
            // TODO:
                return List<IDatabase>();
//                return new NativeIterator<>(databases_all(nativeObject)).stream().map(TypeDBDatabaseImpl::new).collect(toList());
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}

