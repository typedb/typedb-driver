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

using System;
using System.Collections.Generic;

using TypeDB.Driver;
using TypeDB.Driver.Answer;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;

namespace TypeDB.Driver.Connection
{
    /// <summary>
    /// A transaction with a TypeDB database.
    /// </summary>
    public class TypeDBTransaction : NativeObjectWrapper<Pinvoke.Transaction>, ITypeDBTransaction
    {
        private readonly List<TransactionOnClose> _callbacks;

        internal TypeDBTransaction(IDriver driver, Pinvoke.Transaction transaction, TransactionType type, TransactionOptions options)
            : base(transaction)
        {
            Type = type;
            _callbacks = new List<TransactionOnClose>();
        }

        /// <inheritdoc/>
        public TransactionType Type { get; }

        /// <inheritdoc/>
        public bool IsOpen()
        {
            return NativeObject.IsOwned()
                ? Pinvoke.typedb_driver.transaction_is_open(NativeObject)
                : false;
        }

        /// <inheritdoc/>
        public void OnClose(Action<Exception?> function)
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                TransactionOnClose callback = new TransactionOnClose(function);
                _callbacks.Add(callback);
                Pinvoke.typedb_driver.transaction_on_close(NativeObject, callback.Released()).Resolve();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
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

        /// <inheritdoc/>
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

        /// <inheritdoc/>
        public void Close()
        {
            if (!NativeObject.IsOwned())
            {
                return;
            }

            try
            {
                // Call transaction_close and wait for it to complete.
                // IMPORTANT: We must wait for close to complete BEFORE clearing callbacks.
                // The native close process invokes OnClose callbacks, which go through
                // the SWIG director mechanism. If we clear _callbacks first, the GC can
                // finalize TransactionOnClose objects and release their SWIG directors
                // while native code is still trying to invoke them, causing use-after-free.
                Pinvoke.typedb_driver.transaction_close(NativeObject).Resolve();

                // Now safe to clear callbacks - all native callbacks have been invoked
                _callbacks.Clear();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public IQueryAnswer Query(string query)
        {
            return Query(query, new QueryOptions());
        }

        /// <inheritdoc/>
        public IQueryAnswer Query(string query, QueryOptions options)
        {
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                // Execute query and wait for completion
                var promise = Pinvoke.typedb_driver.transaction_query(NativeObject, query, options.NativeObject);

//                // Prevent GC from collecting options during the native call
//                GC.KeepAlive(options);

                // query_answer_promise_resolve CONSUMES the native promise via take_ownership in Rust.
                // We must release ownership on the C# wrapper BEFORE calling resolve to prevent
                // the wrapper's finalizer from trying to free an already-freed pointer.
                var releasedPromise = promise.Released();
                var nativeAnswer = Pinvoke.typedb_driver.query_answer_promise_resolve(releasedPromise);
                return QueryAnswer.Of(nativeAnswer);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public void Dispose()
        {
            Close();
        }

        private class TransactionOnClose : Pinvoke.TransactionCallbackDirector
        {
            private readonly Action<Exception?> _function;

            public TransactionOnClose(Action<Exception?> function)
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
