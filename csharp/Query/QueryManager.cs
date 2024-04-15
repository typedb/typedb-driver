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
using System.Linq;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Concept;
using TypeDB.Driver.Logic;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;
using QueryError = TypeDB.Driver.Common.Error.Query;

namespace TypeDB.Driver.Query
{
    public class QueryManager : IQueryManager
    {
        private readonly Pinvoke.Transaction _nativeTransaction;

        public QueryManager(Pinvoke.Transaction nativeTransaction) 
        {
            _nativeTransaction = nativeTransaction;
        }
    
        public IEnumerable<IConceptMap> Get(string query)
        {
            return Get(query, new TypeDBOptions());
        }
    
        public IEnumerable<IConceptMap> Get(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);
            
            try 
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_get(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj));
            }
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public Promise<IValue> GetAggregate(string query)
        {
            return GetAggregate(query, new TypeDBOptions());
        }
    
        public Promise<IValue> GetAggregate(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            Pinvoke.ConceptPromise promise = Pinvoke.typedb_driver.query_get_aggregate(
                _nativeTransaction, query, options.NativeObject);

            return new Promise<IValue>(() =>
                {
                    var res = promise.Resolve();
                    if (res == null)
                    {
                        return null;
                    }

                    return new Value(res);
                });
        }
    
        public IEnumerable<IConceptMapGroup> GetGroup(string query)
        {
            return GetGroup(query, new TypeDBOptions());
        }
    
        public IEnumerable<IConceptMapGroup> GetGroup(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMapGroup>(
                    Pinvoke.typedb_driver.query_get_group(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMapGroup(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public IEnumerable<IValueGroup> GetGroupAggregate(string query)
        {
            return GetGroupAggregate(query, new TypeDBOptions());
        }
    
        public IEnumerable<IValueGroup> GetGroupAggregate(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ValueGroup>(
                    Pinvoke.typedb_driver.query_get_group_aggregate(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ValueGroup(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public IEnumerable<JObject> Fetch(string query)
        {
            return Fetch(query, new TypeDBOptions());
        }

        public IEnumerable<JObject> Fetch(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<string>(
                    Pinvoke.typedb_driver.query_fetch(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => JObject.Parse(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public IEnumerable<IConceptMap> Insert(string query)
        {
            return Insert(query, new TypeDBOptions());
        }
    
        public IEnumerable<IConceptMap> Insert(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_insert(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public VoidPromise Delete(string query)
        {
            return Delete(query, new TypeDBOptions());
        }
    
        public VoidPromise Delete(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_delete(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public IEnumerable<IConceptMap> Update(string query)
        {
            return Update(query, new TypeDBOptions());
        }
    
        public IEnumerable<IConceptMap> Update(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_update(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public VoidPromise Define(string query)
        {
            return Define(query, new TypeDBOptions());
        }
    
        public VoidPromise Define(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_define(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public VoidPromise Undefine(string query)
        {
            return Undefine(query, new TypeDBOptions());
        }
    
        public VoidPromise Undefine(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_undefine(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public IEnumerable<IExplanation> Explain(IConceptMap.IExplainable explainable)
        {
            return Explain(explainable, new TypeDBOptions());
        }
    
        public IEnumerable<IExplanation> Explain(
            IConceptMap.IExplainable explainable, TypeDBOptions options)
        {
            CheckTransaction();

            try
            {
                return new NativeEnumerable<Pinvoke.Explanation>(
                    Pinvoke.typedb_driver.query_explain(
                        _nativeTransaction, 
                        ((ConceptMap.Explainable)explainable).NativeObject,
                        options.NativeObject))
                    .Select(obj => new Explanation(obj));
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }

        private void CheckTransaction()
        {
            if (!_nativeTransaction.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }
        }

        private void CheckQueryAndTransaction(string query)
        {
            CheckTransaction();
            Validator.NonEmptyString(query, QueryError.MISSING_QUERY);
        }
    }
}
