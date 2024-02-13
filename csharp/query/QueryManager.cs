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

using System.Collections.Generic;
using System.Linq;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Api.Answer;
using Vaticle.Typedb.Driver.Api.Concept.Value;
using Vaticle.Typedb.Driver.Api.Logic;
using Vaticle.Typedb.Driver.Api.Query;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Common.Exception;
using Vaticle.Typedb.Driver.Concept.Answer;
using Vaticle.Typedb.Driver.Concept.Value;
using Vaticle.Typedb.Driver.Logic;
using Vaticle.Typedb.Driver.Util;

using DriverError = Vaticle.Typedb.Driver.Common.Exception.Error.Driver;
using QueryError = Vaticle.Typedb.Driver.Common.Exception.Error.Query;

namespace Vaticle.Typedb.Driver.Query
{
    public sealed class QueryManager : IQueryManager
    {
        private Pinvoke.Transaction _nativeTransaction { get; }

        public QueryManager(Pinvoke.Transaction nativeTransaction) 
        {
            _nativeTransaction = nativeTransaction;
        }
    
        public override ICollection<IConceptMap> Get(string query)
        {
            return Get(query, new TypeDBOptions());
        }
    
        public override ICollection<IConceptMap> Get(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);
            
            try 
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_get(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj))
                    .ToList<IConceptMap>();
            }
            catch (Pinvoke.Error e) 
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override Promise<IValue> GetAggregate(string query)
        {
            return GetAggregate(query, new TypeDBOptions());
        }
    
        public override Promise<IValue> GetAggregate(string query, TypeDBOptions options)
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
    
        public override ICollection<IConceptMapGroup> GetGroup(string query)
        {
            return GetGroup(query, new TypeDBOptions());
        }
    
        public override ICollection<IConceptMapGroup> GetGroup(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMapGroup>(
                    Pinvoke.typedb_driver.query_get_group(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMapGroup(obj))
                    .ToList<IConceptMapGroup>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override ICollection<ValueGroup> GetGroupAggregate(string query) 
        {
            return GetGroupAggregate(query, new TypeDBOptions());
        }
    
        public override ICollection<ValueGroup> GetGroupAggregate(string query, TypeDBOptions options) 
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ValueGroup>(
                    Pinvoke.typedb_driver.query_get_group_aggregate(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ValueGroup(obj))
                    .ToList<IValueGroup>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override ICollection<JSON> Fetch(string query) 
        {
            return Fetch(query, new TypeDBOptions());
        }
    
        public override ICollection<JSON> Fetch(string query, TypeDBOptions options) 
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ValueGroup>(
                    Pinvoke.typedb_driver.query_fetch(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => JSON.Parse) // TODO: Implement
                    .ToList<IValueGroup>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override ICollection<IConceptMap> Insert(string query)
        {
            return Insert(query, new TypeDBOptions());
        }
    
        public override ICollection<IConceptMap> Insert(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_insert(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj))
                    .ToList<IConceptMap>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override VoidPromise Delete(string query) 
        {
            return Delete(query, new TypeDBOptions());
        }
    
        public override VoidPromise Delete(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_delete(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public override ICollection<IConceptMap> Update(string query)
        {
            return Update(query, new TypeDBOptions());
        }
    
        public override ICollection<IConceptMap> Update(string query, TypeDBOptions options)
        {
            CheckQueryAndTransaction(query);

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_update(_nativeTransaction, query, options.NativeObject))
                    .Select(obj => new ConceptMap(obj))
                    .ToList<IConceptMap>();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    
        public override VoidPromise Define(string query) 
        {
            return Define(query, new TypeDBOptions());
        }
    
        public override VoidPromise Define(string query, TypeDBOptions options) 
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_define(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public override VoidPromise Undefine(string query) 
        {
            return Undefine(query, new TypeDBOptions());
        }
    
        public override VoidPromise Undefine(string query, TypeDBOptions options) 
        {
            CheckQueryAndTransaction(query);

            return new VoidPromise(Pinvoke.typedb_driver.query_undefine(
                _nativeTransaction, query, options.NativeObject).Resolve);
        }
    
        public override ICollection<Explanation> Explain(IConceptMap.IExplainable explainable)
        {
            return Explain(explainable, new TypeDBOptions());
        }
    
        public override ICollection<IExplanation> Explain(
            IConceptMap.IExplainable explainable, TypeDBOptions options)
        {
            CheckTransaction();

            try
            {
                return new NativeEnumerable<Pinvoke.ConceptMap>(
                    Pinvoke.typedb_driver.query_explain(
                        _nativeTransaction, 
                        ((IConceptMapImpl.IExplainableImpl)explainable).NativeObject, 
                        options.NativeObject))
                    .Select(obj => new Explanation(obj))
                    .ToList<IExplanation>();
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
            InputChecker.NonEmptyString(name, QueryError.MISSING_QUERY);
        }
    }
}
