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

package com.vaticle.typedb.driver.api.query;

import com.vaticle.typedb.driver.api.TypeDBOptions;
import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.api.answer.ConceptMapGroup;
import com.vaticle.typedb.driver.api.answer.JSON;
import com.vaticle.typedb.driver.api.answer.ValueGroup;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.api.logic.Explanation;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLFetch;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLGet;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Provides methods for executing TypeQL queries in the transaction.
 */
public interface QueryManager {
    /**
     * Performs a TypeQL Get (Get) with default options.
     *
     * @see QueryManager#get(TypeQLGet, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> get(TypeQLGet query);

    /**
     * Performs a TypeQL Get (Get) query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().get(query, options)
     * </pre>
     *
     * @param query The TypeQL Get (Get) query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<ConceptMap> get(TypeQLGet query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get (Get) with default options.
     * @see QueryManager#get(TypeQLGet, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> get(String query);

    /**
     * @see QueryManager#get(TypeQLGet, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> get(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Aggregate query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Value> get(TypeQLGet.Aggregate query);

    /**
     * Performs a TypeQL Get Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().getAggregate(query, options).resolve()
     * </pre>
     *
     * @param query The TypeQL Get Aggregate query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Promise<Value> get(TypeQLGet.Aggregate query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Aggregate query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Value> getAggregate(String query);

    /**
     * @see QueryManager#get(TypeQLGet.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Value> getAggregate(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Group query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> get(TypeQLGet.Group query);

    /**
     * Performs a TypeQL Get Group query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().getGroup(query, options)
     * </pre>
     *
     * @param query The TypeQL Get Group query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> get(TypeQLGet.Group query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Group query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> getGroup(String query);

    /**
     * @see QueryManager#get(TypeQLGet.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> getGroup(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Group Aggregate query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ValueGroup> get(TypeQLGet.Group.Aggregate query);

    /**
     * Performs a TypeQL Get Group Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().getGroupAggregate(query, options)
     * </pre>
     *
     * @param query The TypeQL Get Group Aggregate query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<ValueGroup> get(TypeQLGet.Group.Aggregate query, TypeDBOptions options);

    /**
     * Performs a TypeQL Get Group Aggregate query with default options.
     *
     * @see QueryManager#get(TypeQLGet.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ValueGroup> getGroupAggregate(String query);

    /**
     * @see QueryManager#get(TypeQLGet.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ValueGroup> getGroupAggregate(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Fetch (Fetch) with default options.
     *
     * @see QueryManager#fetch(TypeQLFetch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<JSON> fetch(TypeQLFetch query);

    /**
     * Performs a TypeQL Fetch (Fetch) query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().fetch(query, options)
     * </pre>
     *
     * @param query The TypeQL Fetch (Fetch) query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<JSON> fetch(TypeQLFetch query, TypeDBOptions options);

    /**
     * Performs a TypeQL Fetch (Fetch) with default options.
     * @see QueryManager#fetch(TypeQLFetch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<JSON> fetch(String query);

    /**
     * @see QueryManager#fetch(TypeQLFetch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<JSON> fetch(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Insert query with default options.
     *
     * @see QueryManager#insert(TypeQLInsert, TypeDBOptions)
     */
    Stream<ConceptMap> insert(TypeQLInsert query);

    /**
     * Performs a TypeQL Insert query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().insert(query, options)
     * </pre>
     *
     * @param query The TypeQL Insert query to be executed
     * @param options Specify query options
     */
    Stream<ConceptMap> insert(TypeQLInsert query, TypeDBOptions options);

    /**
     * Performs a TypeQL Insert query with default options.
     *
     * @see QueryManager#insert(TypeQLInsert, TypeDBOptions)
     */
    Stream<ConceptMap> insert(String query);

    /**
     * @see QueryManager#insert(TypeQLInsert, TypeDBOptions)
     */
    Stream<ConceptMap> insert(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Delete query with default options.
     *
     * @see QueryManager#delete(TypeQLDelete, TypeDBOptions) 
     */
    @CheckReturnValue
    Promise<Void> delete(TypeQLDelete query);

    /**
     * Performs a TypeQL Delete query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().delete(query, options).resolve()
     * </pre>
     *
     * @param query The TypeQL Delete query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Promise<Void> delete(TypeQLDelete query, TypeDBOptions options);

    /**
     * Performs a TypeQL Delete query with default options.
     *
     * @see QueryManager#delete(TypeQLDelete, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> delete(String query);

    /**
     * @see QueryManager#delete(TypeQLDelete, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> delete(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Update query with default options.
     *
     * @see QueryManager#update(TypeQLUpdate, TypeDBOptions)
     */
    Stream<ConceptMap> update(TypeQLUpdate query);

    /**
     * Performs a TypeQL Update query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().update(query, options)
     * </pre>
     *
     * @param query The TypeQL Update query to be executed
     * @param options Specify query options
     */
    Stream<ConceptMap> update(TypeQLUpdate query, TypeDBOptions options);

    /**
     * Performs a TypeQL Update query with default options.
     *
     * @see QueryManager#update(TypeQLUpdate, TypeDBOptions)
     */
    Stream<ConceptMap> update(String query);

    /**
     * @see QueryManager#update(TypeQLUpdate, TypeDBOptions)
     */
    Stream<ConceptMap> update(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Define query with default options.
     *
     * @see QueryManager#define(TypeQLDefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> define(TypeQLDefine query);

    /**
     * Performs a TypeQL Define query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().define(query, options).resolve()
     * </pre>
     *
     * @param query The TypeQL Define query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Promise<Void> define(TypeQLDefine query, TypeDBOptions options);


    /**
     * Performs a TypeQL Define query with default options.
     *
     * @see QueryManager#define(TypeQLDefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> define(String query);

    /**
     * @see QueryManager#define(TypeQLDefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> define(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Undefine query with default options.
     *
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> undefine(TypeQLUndefine query);

    /**
     * Performs a TypeQL Undefine query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().undefine(query, options).resolve()
     * </pre>
     *
     * @param query The TypeQL Undefine query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Promise<Void> undefine(TypeQLUndefine query, TypeDBOptions options);

    /**
     * Performs a TypeQL Undefine query with default options.
     *
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> undefine(String query);

    /**
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    @CheckReturnValue
    Promise<Void> undefine(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Explain query with default options.
     *
     * @see QueryManager#explain(ConceptMap.Explainable, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<Explanation> explain(ConceptMap.Explainable explainable);

    /**
     * Performs a TypeQL Explain query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().explain(explainable, options)
     * </pre>
     *
     * @param explainable The Explainable to be explained
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<Explanation> explain(ConceptMap.Explainable explainable, TypeDBOptions options);
}
