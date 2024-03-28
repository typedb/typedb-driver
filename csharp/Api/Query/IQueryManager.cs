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

using Newtonsoft.Json.Linq;
using System.Collections.Generic;

using TypeDB.Driver.Api;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /**
     * Provides methods for executing TypeQL queries in the transaction.
     */
    public interface IQueryManager 
    {
        /**
         * Performs a TypeQL Get (Get) with default options.
         *
         * @see IQueryManager#Get(string, TypeDBOptions)
         */
        IEnumerable<IConceptMap> Get(string query);

        /**
         * Performs a TypeQL Get (Get) query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Get(query, options);
         * </pre>
         *
         * @param query The TypeQL Get (Get) query to be executed
         * @param options Specify query options
         */
        IEnumerable<IConceptMap> Get(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Get Aggregate query with default options.
         *
         * @see IQueryManager#GetAggregate(string, TypeDBOptions)
         */
        Promise<IValue> GetAggregate(string query);

        /**
         * Performs a TypeQL Get Aggregate query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.GetAggregate(query, options).Resolve()
         * </pre>
         *
         * @param query The TypeQL Get Aggregate query to be executed
         * @param options Specify query options
         */
        Promise<IValue> GetAggregate(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Get Group query with default options.
         *
         * @see IQueryManager#GetGroup(string, TypeDBOptions)
         */
        IEnumerable<IConceptMapGroup> GetGroup(string query);

        /**
         * Performs a TypeQL Get Group query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.GetGroup(query, options)
         * </pre>
         *
         * @param query The TypeQL Get Group query to be executed
         * @param options Specify query options
         */
        IEnumerable<IConceptMapGroup> GetGroup(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Get Group Aggregate query with default options.
         *
         * @see IQueryManager#GetGroupAggregate(string, TypeDBOptions)
         */
        IEnumerable<IValueGroup> GetGroupAggregate(string query);

        /**
         * Performs a TypeQL Get Group Aggregate query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.GetGroupAggregate(query, options)
         * </pre>
         *
         * @param query The TypeQL Get Group Aggregate query to be executed
         * @param options Specify query options
         */
        IEnumerable<IValueGroup> GetGroupAggregate(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Fetch (Fetch) with default options.
         *
         * @see IQueryManager#Fetch(string, TypeDBOptions)
         */
        IEnumerable<JObject> Fetch(string query);

        /**
         * Performs a TypeQL Fetch (Fetch) query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Fetch(query, options)
         * </pre>
         *
         * @param query The TypeQL Fetch (Fetch) query to be executed
         * @param options Specify query options
         */
        IEnumerable<JObject> Fetch(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Insert query with default options.
         *
         * @see IQueryManager#Insert(string, TypeDBOptions)
         */
        IEnumerable<IConceptMap> Insert(string query);

        /**
         * Performs a TypeQL Insert query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Insert(query, options)
         * </pre>
         *
         * @param query The TypeQL Insert query to be executed
         * @param options Specify query options
         */
        IEnumerable<IConceptMap> Insert(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Delete query with default options.
         *
         * @see IQueryManager#Delete(string, TypeDBOptions)
         */
        VoidPromise Delete(string query);

        /**
         * Performs a TypeQL Delete query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Delete(query, options).Resolve()
         * </pre>
         *
         * @param query The TypeQL Delete query to be executed
         * @param options Specify query options
         */
        VoidPromise Delete(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Update query with default options.
         *
         * @see IQueryManager#Update(string, TypeDBOptions)
         */
        IEnumerable<IConceptMap> Update(string query);

        /**
         * Performs a TypeQL Update query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Update(query, options)
         * </pre>
         *
         * @param query The TypeQL Update query to be executed
         * @param options Specify query options
         */
        IEnumerable<IConceptMap> Update(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Define query with default options.
         *
         * @see IQueryManager#Define(string, TypeDBOptions)
         */
        VoidPromise Define(string query);

        /**
         * Performs a TypeQL Define query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Define(query, options).Resolve()
         * </pre>
         *
         * @param query The TypeQL Define query to be executed
         * @param options Specify query options
         */
        VoidPromise Define(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Undefine query with default options.
         *
         * @see IQueryManager#Undefine(string, TypeDBOptions)
         */
        VoidPromise Undefine(string query);

        /**
         * Performs a TypeQL Undefine query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Undefine(query, options).Resolve()
         * </pre>
         *
         * @param query The TypeQL Undefine query to be executed
         * @param options Specify query options
         */
        VoidPromise Undefine(string query, TypeDBOptions options);

        /**
         * Performs a TypeQL Explain query with default options.
         *
         * @see IQueryManager#Explain(IConceptMap.IExplainable, TypeDBOptions)
         */
        IEnumerable<IExplanation> Explain(IConceptMap.IExplainable explainable);

        /**
         * Performs a TypeQL Explain query in the transaction.
         *
         * <h3>Examples</h3>
         * <pre>
         * transaction.Query.Explain(explainable, options)
         * </pre>
         *
         * @param explainable The IExplainable to be explained
         * @param options Specify query options
         */
        IEnumerable<IExplanation> Explain(IConceptMap.IExplainable explainable, TypeDBOptions options);
    }
}