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
import com.vaticle.typedb.driver.api.answer.Numeric;
import com.vaticle.typedb.driver.api.answer.NumericGroup;
import com.vaticle.typedb.driver.api.logic.Explanation;
import com.vaticle.typedb.driver.api.query.QueryManager;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.answer.ConceptMapGroupImpl;
import com.vaticle.typedb.driver.concept.answer.ConceptMapImpl;
import com.vaticle.typedb.driver.concept.answer.NumericGroupImpl;
import com.vaticle.typedb.driver.concept.answer.NumericImpl;
import com.vaticle.typedb.driver.logic.ExplanationImpl;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import java.util.stream.Stream;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Driver.TRANSACTION_CLOSED;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Query.MISSING_QUERY;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_define;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_delete;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_explain;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_insert;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_match;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_match_aggregate;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_match_group;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_match_group_aggregate;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_undefine;
import static com.vaticle.typedb.driver.jni.typedb_driver.query_update;

public final class QueryManagerImpl implements QueryManager {
    private final com.vaticle.typedb.driver.jni.Transaction nativeTransaction;

    public QueryManagerImpl(com.vaticle.typedb.driver.jni.Transaction nativeTransaction) {
        this.nativeTransaction = nativeTransaction;
    }

    @Override
    public Stream<ConceptMap> match(TypeQLMatch query) {
        return match(query.toString(false));
    }

    @Override
    public Stream<ConceptMap> match(TypeQLMatch query, TypeDBOptions options) {
        return match(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMap> match(String query) {
        return match(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMap> match(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return query_match(nativeTransaction, query, options.nativeObject).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Numeric match(TypeQLMatch.Aggregate query) {
        return matchAggregate(query.toString(false));
    }

    @Override
    public Numeric match(TypeQLMatch.Aggregate query, TypeDBOptions options) {
        return matchAggregate(query.toString(false), options);
    }

    @Override
    public Numeric matchAggregate(String query) {
        return matchAggregate(query, new TypeDBOptions());
    }

    @Override
    public Numeric matchAggregate(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return new NumericImpl(query_match_aggregate(nativeTransaction, query, options.nativeObject));
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<ConceptMapGroup> match(TypeQLMatch.Group query) {
        return matchGroup(query.toString(false));
    }

    @Override
    public Stream<ConceptMapGroup> match(TypeQLMatch.Group query, TypeDBOptions options) {
        return matchGroup(query.toString(false), options);
    }

    @Override
    public Stream<ConceptMapGroup> matchGroup(String query) {
        return matchGroup(query, new TypeDBOptions());
    }

    @Override
    public Stream<ConceptMapGroup> matchGroup(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return query_match_group(nativeTransaction, query, options.nativeObject).stream().map(ConceptMapGroupImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query) {
        return matchGroupAggregate(query.toString(false));
    }

    @Override
    public Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query, TypeDBOptions options) {
        return matchGroupAggregate(query.toString(false), options);
    }

    @Override
    public Stream<NumericGroup> matchGroupAggregate(String query) {
        return matchGroupAggregate(query, new TypeDBOptions());
    }

    @Override
    public Stream<NumericGroup> matchGroupAggregate(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            return query_match_group_aggregate(nativeTransaction, query, options.nativeObject).stream().map(NumericGroupImpl::new);
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
            return query_insert(nativeTransaction, query, options.nativeObject).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void delete(TypeQLDelete query) {
        delete(query.toString(false));
    }

    @Override
    public void delete(TypeQLDelete query, TypeDBOptions options) {
        delete(query.toString(false), options);
    }

    @Override
    public void delete(String query) {
        delete(query, new TypeDBOptions());
    }

    @Override
    public void delete(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            query_delete(nativeTransaction, query, options.nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
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
            return query_update(nativeTransaction, query, options.nativeObject).stream().map(ConceptMapImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void define(TypeQLDefine query) {
        define(query.toString(false));
    }

    @Override
    public void define(TypeQLDefine query, TypeDBOptions options) {
        define(query.toString(false), options);
    }

    @Override
    public void define(String query) {
        define(query, new TypeDBOptions());
    }

    @Override
    public void define(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            query_define(nativeTransaction, query, options.nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public void undefine(TypeQLUndefine query) {
        undefine(query.toString(false));
    }

    @Override
    public void undefine(TypeQLUndefine query, TypeDBOptions options) {
        undefine(query.toString(false), options);
    }

    @Override
    public void undefine(String query) {
        undefine(query, new TypeDBOptions());
    }

    @Override
    public void undefine(String query, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        if (query == null || query.isEmpty()) throw new TypeDBDriverException(MISSING_QUERY);
        try {
            query_undefine(nativeTransaction, query, options.nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable) {
        return explain(explainable, new TypeDBOptions());
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable, TypeDBOptions options) {
        if (!nativeTransaction.isOwned()) throw new TypeDBDriverException(TRANSACTION_CLOSED);
        try {
            return query_explain(nativeTransaction, explainable.id(), options.nativeObject).stream().map(ExplanationImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
