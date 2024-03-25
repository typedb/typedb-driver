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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Concept;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;
using TypeDB.Driver.Logic;
using TypeDB.Driver.Query;

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Connection
{
    public class TypeDBTransaction : NativeObjectWrapper<Pinvoke.Transaction>, ITypeDBTransaction
    {
        private readonly List<TransactionOnClose> _callbacks;

        internal TypeDBTransaction(TypeDBSession session, TransactionType type, TypeDBOptions options)
            : base(NewNative(session, type, options))
        {
            Type = type;
            Options = options;

            Concepts = new ConceptManager(NativeObject);
            Logic = new LogicManager(NativeObject);
            Query = new QueryManager(NativeObject);

            _callbacks = new List<TransactionOnClose>();
        }

        private static Pinvoke.Transaction NewNative(
            TypeDBSession session, TransactionType type, TypeDBOptions options)
        {
            try
            {
                return Pinvoke.typedb_driver.transaction_new(
                    session.NativeObject, (Pinvoke.TransactionType)type, options.NativeObject);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public TransactionType Type { get; }

        public TypeDBOptions Options { get; }

        public IConceptManager Concepts { get; }

        public ILogicManager Logic { get; }

        public IQueryManager Query { get; }

        public bool IsOpen()
        {
            return NativeObject.IsOwned()
                ? Pinvoke.typedb_driver.transaction_is_open(NativeObject)
                : false;
        }

        public void OnClose(Action<Exception> function)
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

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
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                Pinvoke.typedb_driver.transaction_commit(NativeObject.Released()).Resolve();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        public void Rollback()
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                Pinvoke.typedb_driver.transaction_rollback(NativeObject).Resolve();
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
            Close();
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
