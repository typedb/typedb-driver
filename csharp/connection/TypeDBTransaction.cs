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

using com.vaticle.typedb.driver;
using com.vaticle.typedb.driver.Api;
using com.vaticle.typedb.driver.Api.Database;
using com.vaticle.typedb.driver.Common;
using com.vaticle.typedb.driver.Common.Exception;
using DriverError = com.vaticle.typedb.driver.Common.Exception.Error.Driver;

namespace com.vaticle.typedb.driver.Connection
{
    public class TypeDBTransaction : NativeObjectWrapper<pinvoke.Transaction>, ITypeDBTransaction
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

        private static pinvoke.Transaction NewNative(
            TypeDBSession session, TransactionType type, TypeDBOptions options)
        {
            try
            {
                return pinvoke.typedb_driver.transaction_new(
                    session.NativeObject, TransactionTypeGetter.ToNative(type), options.NativeObject);
            }
            catch (pinvoke.Error e)
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

            return pinvoke.typedb_driver.transaction_is_open(NativeObject);
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
                throw new TypeDBDriverException(DriverError.s_TransactionClosed);
            }

            try
            {
                TransactionOnClose callback = new TransactionOnClose(function);
                _callbacks.Add(callback);
                pinvoke.typedb_driver.transaction_on_close(NativeObject, callback.Released());
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Commit()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.s_TransactionClosed);
            }

            try
            {
                pinvoke.typedb_driver.transaction_commit(NativeObject.Released());  // TODO: .Get() after implementing VoidPromises
            }
            catch (pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Rollback()
        {
            if (!NativeObject.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.s_TransactionClosed);
            }

            try
            {
                pinvoke.typedb_driver.transaction_rollback(NativeObject); // TODO: .Get() after implementing VoidPromises
            }
            catch (pinvoke.Error e)
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
                pinvoke.typedb_driver.transaction_force_close(NativeObject);
            }
            catch (pinvoke.Error e)
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

        private class TransactionOnClose : pinvoke.TransactionCallbackDirector
        {
            private readonly Action<Exception> _function;

            public TransactionOnClose(Action<Exception> function)
            {
                _function = function;
            }

            public override void callback(pinvoke.Error e)
            {
                _function(e);
            }
        }
    }
}
