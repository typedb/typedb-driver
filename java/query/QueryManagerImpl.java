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

package com.vaticle.typedb.driver.query;

import com.vaticle.typedb.driver.api.TypeDBOptions;
import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.api.answer.ConceptMapGroup;
import com.vaticle.typedb.driver.api.answer.ValueGroup;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.api.logic.Explanation;
import com.vaticle.typedb.driver.api.query.QueryManager;
import com.vaticle.typedb.driver.common.NativeIterator;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.answer.ConceptMapGroupImpl;
import com.vaticle.typedb.driver.concept.answer.ConceptMapImpl;
import com.vaticle.typedb.driver.concept.answer.ValueGroupImpl;
import com.vaticle.typedb.driver.concept.value.ValueImpl;
import com.vaticle.typedb.driver.logic.ExplanationImpl;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLFetch;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLGet;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Query.MISSING_QUERY;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_define;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_delete;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_explain;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_fetch;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_insert;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_get;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_get_aggregate;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_get_group;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_get_group_aggregate;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_undefine;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_update;

public final class QueryManagerImpl implements QueryManager {
    private final com.vaticle.typedb.driver.jni.Transaction nativeTransaction;

    public QueryManagerImpl(com.vaticle.typedb.driver.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    public Stream<ConceptMap> get(TypeQLGet query) {
        return get(query.toString(false));
    }

    @Override
    public Stream<ConceptMap> get(TypeQLGet query, TypeDBOptions options) {
        return get(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMap> get(String query) {
        return get(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMap> get(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_get(nativeTransaction, query, options.nativeObject)).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    @CheckReturnValue
    public Promise<Value> get(TypeQLGet.Aggregate query) {
        return getAggregate(query.toString(false));
    }

    @Override
    @CheckReturnValue
    public Promise<Value> get(TypeQLGet.Aggregate query, TypeDBOptions options) {
        return getAggregate(query.toString(false), options);
    }

    @Override
    @CheckReturnValue
    public Promise<Value> getAggregate(String query) {
        return getAggregate(query, new TypeDBOptions());
    }

    @Override
    @CheckReturnValue
    public Promise<Value> getAggregate(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        com.vaticle.typedb.driver.jni.ConceptPromise promise = query_get_aggregate(nativeTransaction, query, options.nativeObject);
        return Promise.map(promise, ValueImpl::new);
    }

    @Override
    public Stream<ConceptMapGroup> get(TypeQLGet.Group query) {
        return getGroup(query.toString(false));
    }

    @Override
    public Stream<ConceptMapGroup> get(TypeQLGet.Group query, TypeDBOptions options) {
        return getGroup(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMapGroup> getGroup(String query) {
        return getGroup(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMapGroup> getGroup(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_get_group(nativeTransaction, query, options.nativeObject)).stream().map(ConceptMapGroupImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<ValueGroup> get(TypeQLGet.Group.Aggregate query) {
        return getGroupAggregate(query.toString(false));
    }

    @Override
    public Stream<ValueGroup> get(TypeQLGet.Group.Aggregate query, TypeDBOptions options) {
        return getGroupAggregate(query.toString(false), options);
    }

    @Override
    public Stream<ValueGroup> getGroupAggregate(String query) {
        return getGroupAggregate(query, new TypeDBOptions());
    }

    @Override
    public Stream<ValueGroup> getGroupAggregate(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_get_group_aggregate(nativeTransaction, query, options.nativeObject)).stream().map(ValueGroupImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<String> fetch(TypeQLFetch query) {
        return fetch(query.toString(false));
    }

    @Override
    public Stream<String> fetch(TypeQLFetch query, TypeDBOptions options) {
        return fetch(query.toString(false), options);
    }

    @Override
    public Stream<String> fetch(String query) {
        return fetch(query, new TypeDBOptions());
    }

    @Override
    public Stream<String> fetch(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_fetch(nativeTransaction, query, options.nativeObject)).stream();
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<ConceptMap> insert(TypeQLInsert query) {
        return insert(query.toString(false));
    }

    @Override
    public Stream<ConceptMap> insert(TypeQLInsert query, TypeDBOptions options) {
        return insert(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMap> insert(String query) {
        return insert(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMap> insert(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_insert(nativeTransaction, query, options.nativeObject)).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(TypeQLDelete query) {
        return delete(query.toString(false));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(TypeQLDelete query, TypeDBOptions options) {
        return delete(query.toString(false), options);
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(String query) {
        return delete(query, new TypeDBOptions());
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        return new Promise<>(query_delete(nativeTransaction, query, options.nativeObject));
    }

    @Override
    public Stream<ConceptMap> update(TypeQLUpdate query) {
        return update(query.toString(false));
    }

    @Override
    public Stream<ConceptMap> update(TypeQLUpdate query, TypeDBOptions options) {
        return update(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMap> update(String query) {
        return update(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMap> update(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NativeIterator<>(query_update(nativeTransaction, query, options.nativeObject)).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    @CheckReturnValue
    public Promise<Void> define(TypeQLDefine query) {
        return define(query.toString(false));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> define(TypeQLDefine query, TypeDBOptions options) {
        return define(query.toString(false), options);
    }

    @Override
    @CheckReturnValue
    public Promise<Void> define(String query) {
        return define(query, new TypeDBOptions());
    }

    @Override
    @CheckReturnValue
    public Promise<Void> define(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        return new Promise<>(query_define(nativeTransaction, query, options.nativeObject));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> undefine(TypeQLUndefine query) {
        return undefine(query.toString(false));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> undefine(TypeQLUndefine query, TypeDBOptions options) {
        return undefine(query.toString(false), options);
    }

    @Override
    @CheckReturnValue
    public Promise<Void> undefine(String query) {
        return undefine(query, new TypeDBOptions());
    }

    @Override
    @CheckReturnValue
    public Promise<Void> undefine(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        return new Promise<>(query_undefine(nativeTransaction, query, options.nativeObject));
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable) {
        return explain(explainable, new TypeDBOptions());
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            return new NativeIterator<>(query_explain(nativeTransaction, ((ConceptMapImpl.ExplainableImpl) explainable).nativeObject, options.nativeObject)).stream()
                    .map(ExplanationImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
