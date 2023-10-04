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
import com.vaticle.typedb.driver.api.answer.Numeric;
import com.vaticle.typedb.driver.api.answer.NumericGroup;
import com.vaticle.typedb.driver.api.logic.Explanation;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

/**
 * Provides methods for executing TypeQL queries in the transaction.
 */
public interface QueryManager {
    /**
     * Performs a TypeQL Match (Get) with default options.
     *
     * @see QueryManager#match(TypeQLMatch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> match(TypeQLMatch query);

    /**
     * Performs a TypeQL Match (Get) query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().match(query, options)
     * </pre>
     *
     * @param query The TypeQL Match (Get) query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<ConceptMap> match(TypeQLMatch query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match (Get) with default options.
     * @see QueryManager#match(TypeQLMatch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> match(String query);

    /**
     * @see QueryManager#match(TypeQLMatch, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMap> match(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Aggregate query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Numeric match(TypeQLMatch.Aggregate query);

    /**
     * Performs a TypeQL Match Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().matchAggregate(query, options)
     * </pre>
     *
     * @param query The TypeQL Match Aggregate query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Numeric match(TypeQLMatch.Aggregate query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Aggregate query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Numeric matchAggregate(String query);

    /**
     * @see QueryManager#match(TypeQLMatch.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Numeric matchAggregate(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Group query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> match(TypeQLMatch.Group query);

    /**
     * Performs a TypeQL Match Group query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().matchGroup(query, options)
     * </pre>
     *
     * @param query The TypeQL Match Group query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> match(TypeQLMatch.Group query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Group query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> matchGroup(String query);

    /**
     * @see QueryManager#match(TypeQLMatch.Group, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<ConceptMapGroup> matchGroup(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Group Aggregate query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query);

    /**
     * Performs a TypeQL Match Group Aggregate query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().matchGroupAggregate(query, options)
     * </pre>
     *
     * @param query The TypeQL Match Group Aggregate query to be executed
     * @param options Specify query options
     */
    @CheckReturnValue
    Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query, TypeDBOptions options);

    /**
     * Performs a TypeQL Match Group Aggregate query with default options.
     *
     * @see QueryManager#match(TypeQLMatch.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<NumericGroup> matchGroupAggregate(String query);

    /**
     * @see QueryManager#match(TypeQLMatch.Group.Aggregate, TypeDBOptions)
     */
    @CheckReturnValue
    Stream<NumericGroup> matchGroupAggregate(String query, TypeDBOptions options);

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
    void delete(TypeQLDelete query);

    /**
     * Performs a TypeQL Delete query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().delete(query, options)
     * </pre>
     *
     * @param query The TypeQL Delete query to be executed
     * @param options Specify query options
     */
    void delete(TypeQLDelete query, TypeDBOptions options);

    /**
     * Performs a TypeQL Delete query with default options.
     *
     * @see QueryManager#delete(TypeQLDelete, TypeDBOptions)
     */
    void delete(String query);

    /**
     * @see QueryManager#delete(TypeQLDelete, TypeDBOptions)
     */
    void delete(String query, TypeDBOptions options);

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
    void define(TypeQLDefine query);

    /**
     * Performs a TypeQL Define query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().define(query, options)
     * </pre>
     *
     * @param query The TypeQL Define query to be executed
     * @param options Specify query options
     */
    void define(TypeQLDefine query, TypeDBOptions options);


    /**
     * Performs a TypeQL Define query with default options.
     *
     * @see QueryManager#define(TypeQLDefine, TypeDBOptions)
     */
    void define(String query);

    /**
     * @see QueryManager#define(TypeQLDefine, TypeDBOptions)
     */
    void define(String query, TypeDBOptions options);

    /**
     * Performs a TypeQL Undefine query with default options.
     *
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    void undefine(TypeQLUndefine query);

    /**
     * Performs a TypeQL Undefine query in the transaction.
     *
     * <h3>Examples</h3>
     * <pre>
     * transaction.query().undefine(query, options)
     * </pre>
     *
     * @param query The TypeQL Undefine query to be executed
     * @param options Specify query options
     */
    void undefine(TypeQLUndefine query, TypeDBOptions options);

    /**
     * Performs a TypeQL Undefine query with default options.
     *
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    void undefine(String query);

    /**
     * @see QueryManager#undefine(TypeQLUndefine, TypeDBOptions)
     */
    void undefine(String query, TypeDBOptions options);

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
