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
using System.Linq;
using TypeDB.Driver;
using TypeDB.Driver.Analyze;
using TypeDB.Driver.Answer;
using TypeDB.Driver.Api;
using TypeDB.Driver.Api.Answer;
using TypeDB.Driver.Api.Analyze;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;
using TypeDB.Driver.Concept;

namespace TypeDB.Driver.Connection
{
    /// <summary>
    /// A transaction with a TypeDB database.
    /// </summary>
    public class Transaction : NativeObjectWrapper<Pinvoke.Transaction>, ITransaction
    {
        private readonly List<TransactionOnClose> _callbacks;

        internal Transaction(IDriver driver, Pinvoke.Transaction transaction, TransactionType type, TransactionOptions options)
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
                Pinvoke.typedb_driver.transaction_close(NativeObject).Resolve();
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

        /// <inheritdoc/>
        public Promise<IQueryAnswer> Query(string query)
        {
            return Query(query, new QueryOptions());
        }

        /// <inheritdoc/>
        public Promise<IQueryAnswer> Query(string query, QueryOptions options)
        {
            Validator.NonNull(query, DriverError.NON_NULL_VALUE_REQUIRED, "query");
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                var promise = Pinvoke.typedb_driver.transaction_query(NativeObject, query, options.NativeObject);
                return Promise<IQueryAnswer>.Map<Pinvoke.QueryAnswer, IQueryAnswer>(
                    () => promise.Resolve(),
                    QueryAnswer.Of);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public Promise<IQueryAnswer> Query(string query, QueryOptions options, List<Dictionary<string, IConcept?>> givenRows)
        {
            Validator.NonNull(query, DriverError.NON_NULL_VALUE_REQUIRED, "query");
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                var promise = Pinvoke.typedb_driver.transaction_query_given_rows(
                    NativeObject, query, options.NativeObject, BuildNativeGivenRowsFromMap(givenRows).Released());
                return Promise<IQueryAnswer>.Map<Pinvoke.QueryAnswer, IQueryAnswer>(
                    () => promise.Resolve(),
                    QueryAnswer.Of);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        /// <inheritdoc/>
        public Promise<IQueryAnswer> Query(string query, QueryOptions options, List<string> givenVariables, List<List<IConcept?>> givenRows)
        {
            Validator.NonNull(query, DriverError.NON_NULL_VALUE_REQUIRED, "query");
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                var promise = Pinvoke.typedb_driver.transaction_query_given_rows(
                    NativeObject, query, options.NativeObject, BuildNativeGivenRows(givenVariables, givenRows).Released());
                return Promise<IQueryAnswer>.Map<Pinvoke.QueryAnswer, IQueryAnswer>(
                    () => promise.Resolve(),
                    QueryAnswer.Of);
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private static Pinvoke.GivenRows BuildNativeGivenRowsFromMap(List<Dictionary<string, IConcept?>> rows)
        {
            var variables = rows.SelectMany(r => r.Keys).Distinct().ToList();
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)variables.Count);
            foreach (var variable in variables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)rows.Count);
            foreach (var row in rows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                foreach (var (variable, concept) in row)
                {
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_empty(rowsBuilder, variable);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_variable_to_concept(rowsBuilder, variable, ((Concept.Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released());
        }

        private static Pinvoke.GivenRows BuildNativeGivenRows(List<string> variables, List<List<IConcept?>> rows)
        {
            var headerBuilder = Pinvoke.typedb_driver.given_rows_header_builder_new((uint)variables.Count);
            foreach (var variable in variables)
                Pinvoke.typedb_driver.given_rows_header_builder_push(headerBuilder, variable);
            var header = Pinvoke.typedb_driver.given_rows_header_builder_finish(headerBuilder.Released());
            var rowsBuilder = Pinvoke.typedb_driver.given_rows_builder_new(header, (uint)rows.Count);
            foreach (var row in rows)
            {
                Pinvoke.typedb_driver.given_rows_builder_start_new_row(rowsBuilder);
                for (int i = 0; i < row.Count; i++)
                {
                    var concept = row[i];
                    if (concept == null)
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_empty(rowsBuilder, (uint)i);
                    else
                        Pinvoke.typedb_driver.given_rows_builder_set_index_to_concept(rowsBuilder, (uint)i, ((Concept.Concept)concept).NativeObject);
                }
                Pinvoke.typedb_driver.given_rows_builder_commit_row(rowsBuilder);
            }
            return Pinvoke.typedb_driver.given_rows_builder_finish(rowsBuilder.Released());
        }

        /// <inheritdoc/>
        public Promise<IAnalyzedQuery> Analyze(string query)
        {
            Validator.NonNull(query, DriverError.NON_NULL_VALUE_REQUIRED, "query");
            Validator.ThrowIfFalse(NativeObject.IsOwned, DriverError.TRANSACTION_CLOSED);

            try
            {
                var promise = Pinvoke.typedb_driver.transaction_analyze(NativeObject, query);
                return Promise<IAnalyzedQuery>.Map<Pinvoke.AnalyzedQuery, IAnalyzedQuery>(
                    () => promise.Resolve(),
                    nativeAnalyzed => new AnalyzedQuery(nativeAnalyzed));
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
