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

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Database;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;

namespace Vaticle.Typedb.Driver.Connection
{
    public class TypeDBSession : NativeObjectWrapper<Pinvoke.Session>, ITypeDBSession
    {
        private readonly SessionType _type;
        private readonly TypeDBOptions _options;

        private readonly List<SessionCallback> _callbacks;

        public TypeDBSession(IDatabaseManager databaseManager, string database, SessionType type, TypeDBOptions options)
            : base(NewNative(databaseManager, database, type, options))
        {
            _type = type;
            _options = options;
            _callbacks = new List<SessionCallback>();
        }

        private static Pinvoke.Session NewNative(
            IDatabaseManager databaseManager, string database, SessionType type, TypeDBOptions options)
        {
            try
            {
                return Pinvoke.typedb_driver.session_new(
                    ((TypeDBDatabaseManager)databaseManager).NativeObject,
                    database,
                    type.NativeObject,
                    options.NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public bool IsOpen()
        {
            return Pinvoke.typedb_driver.session_is_open(NativeObject);
        }

        public SessionType Type()
        {
            return _type;
        }

        public string DatabaseName()
        {
            return Pinvoke.typedb_driver.session_get_database_name(NativeObject);
        }

        public TypeDBOptions Options()
        {
            return _options;
        }

        public ITypeDBTransaction Transaction(TransactionType type)
        {
            return Transaction(type, new TypeDBOptions());
        }

        public ITypeDBTransaction Transaction(TransactionType type, TypeDBOptions options)
        {
            return new TypeDBTransaction(this, type, options);
        }

        public void OnClose(Action function)
        {
            try
            {
                SessionCallback callback = new SessionCallback(function);
                _callbacks.Add(callback);
                Pinvoke.typedb_driver.session_on_close(NativeObject, callback.Released());
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void OnReopen(Action function)
        {
            try
            {
                SessionCallback callback = new SessionCallback(function);
                _callbacks.Add(callback);
                Pinvoke.typedb_driver.session_on_reopen(NativeObject, callback.Released());
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Close()
        {
            try
            {
                Pinvoke.typedb_driver.session_force_close(NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
            finally
            {
                _callbacks.Clear();
            }
        }

        public void Dispose()
        {
        }

        private class SessionCallback : Pinvoke.SessionCallbackDirector
        {
            private readonly Action _function;

            public SessionCallback(Action function)
            {
                _function = function;
            }

            public override void callback()
            {
                _function();
            }
        }
    }
}
