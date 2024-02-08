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
using DriverError = Vaticle.Typedb.Driver.Common.Exception.Error.Driver;

namespace Vaticle.Typedb.Driver.Connection
{
    public class TypeDBTransaction : NativeObjectWrapper<Pinvoke.Transaction>, ITypeDBTransaction
    {
        private readonly TransactionType _type;
        private readonly TypeDBOptions _options;

// TODO:
//        private readonly IConceptManager _conceptManager;
//        private readonly ILogicManager _logicManager;
//        private readonly IQueryManager _queryManager;

        private readonly List<TransactionOnClose> _callbacks;

        public TypeDBTransaction(TypeDBSession session, TransactionType type, TypeDBOptions options)
            : base(NewNative(session, type, options))
        {
            _type = type;
            _options = options;

//            _conceptManager = new ConceptManager(NativeObject);
//            _logicManager = new LogicManager(NativeObject);
//            _queryManager = new QueryManager(NativeObject);

            _callbacks = new List<TransactionOnClose>();
        }

        private static Pinvoke.Transaction NewNative(
            TypeDBSession session, TransactionType type, TypeDBOptions options)
        {
            try
            {
                return Pinvoke.typedb_driver.transaction_new(
                    session.NativeObject, type.NativeObject, options.NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public TransactionType Type()
        {
            return _type;
        }

        public TypeDBOptions Options()
        {
            return _options;
        }

        public bool IsOpen()
        {
            if (!NativeObject.IsOwned())
            {
                return false;
            }

            return Pinvoke.typedb_driver.transaction_is_open(NativeObject);
        }
// TODO:
//        public IConceptManager Concepts()
//        {
//            return _conceptManager;
//        }
//
//        public ILogicManager Logic()
//        {
//            return _logicManager;
//        }
//
//        public IQueryManager Query()
//        {
//            return _queryManager;
//        }

        public void OnClose(Action<Exception> function)
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }

            try
            {
                TransactionOnClose callback = new TransactionOnClose(function);
                _callbacks.Add(callback);
                Pinvoke.typedb_driver.transaction_on_close(NativeObject, callback.Released());
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Commit()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }

            try
            {
                Pinvoke.typedb_driver.transaction_commit(NativeObject.Released());  // TODO: .Get() after implementing VoidPromises
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Rollback()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }

            try
            {
                Pinvoke.typedb_driver.transaction_rollback(NativeObject); // TODO: .Get() after implementing VoidPromises
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Close()
        {
            if (!NativeObject.IsOwned())
            {
                return;
            }

            try
            {
                Pinvoke.typedb_driver.transaction_force_close(NativeObject);
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

        private class TransactionOnClose : Pinvoke.TransactionCallbackDirector
        {
            private readonly Action<Exception> _function;

            public TransactionOnClose(Action<Exception> function)
            {
                _function = function;
            }

            public override void callback(Pinvoke.Error e)
            {
                _function(e);
            }
        }
    }
}
