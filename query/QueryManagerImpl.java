/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.client.query;

import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.answer.ConceptMap;
import com.vaticle.typedb.client.api.answer.ConceptMapGroup;
import com.vaticle.typedb.client.api.answer.Numeric;
import com.vaticle.typedb.client.api.answer.NumericGroup;
import com.vaticle.typedb.client.api.logic.Explanation;
import com.vaticle.typedb.client.api.query.QueryFuture;
import com.vaticle.typedb.client.api.query.QueryManager;
import com.vaticle.typedb.client.concept.answer.ConceptMapGroupImpl;
import com.vaticle.typedb.client.concept.answer.ConceptMapImpl;
import com.vaticle.typedb.client.concept.answer.NumericGroupImpl;
import com.vaticle.typedb.client.concept.answer.NumericImpl;
import com.vaticle.typedb.client.logic.ExplanationImpl;
import com.vaticle.typedb.protocol.QueryProto;
import com.vaticle.typedb.protocol.TransactionProto;
import com.vaticle.typeql.lang.query.TypeQLDefine;
import com.vaticle.typeql.lang.query.TypeQLDelete;
import com.vaticle.typeql.lang.query.TypeQLInsert;
import com.vaticle.typeql.lang.query.TypeQLMatch;
import com.vaticle.typeql.lang.query.TypeQLUndefine;
import com.vaticle.typeql.lang.query.TypeQLUpdate;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.defineReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.deleteReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.explainReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.insertReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.matchAggregateReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.matchGroupAggregateReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.matchGroupReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.matchReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.undefineReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.QueryManager.updateReq;

public final class QueryManagerImpl implements QueryManager {

    private final TypeDBTransaction.Extended transactionExt;

    public QueryManagerImpl(TypeDBTransaction.Extended transactionExt) {
        this.transactionExt = transactionExt;
    }

    @Override
    public Stream<ConceptMap> match(TypeQLMatch query) {
        return match(query.toString());
    }

    @Override
    public Stream<ConceptMap> match(TypeQLMatch query, TypeDBOptions options) {
        return match(query.toString(), options);
    }

    @Override
    public Stream<ConceptMap> match(String query) {
        return match(query, TypeDBOptions.core());
    }

    @Override
    public Stream<ConceptMap> match(String query, TypeDBOptions options) {
        return stream(matchReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public QueryFuture<Numeric> match(TypeQLMatch.Aggregate query) {
        return matchAggregate(query.toString());
    }

    @Override
    public QueryFuture<Numeric> match(TypeQLMatch.Aggregate query, TypeDBOptions options) {
        return matchAggregate(query.toString(), options);
    }

    @Override
    public QueryFuture<Numeric> matchAggregate(String query) {
        return matchAggregate(query, TypeDBOptions.core());
    }

    @Override
    public QueryFuture<Numeric> matchAggregate(String query, TypeDBOptions options) {
        return query(matchAggregateReq(query, options.proto()))
                .map(r -> r.getMatchAggregateRes().getAnswer())
                .map(NumericImpl::of);
    }

    @Override
    public Stream<ConceptMapGroup> match(TypeQLMatch.Group query) {
        return matchGroup(query.toString());
    }

    @Override
    public Stream<ConceptMapGroup> match(TypeQLMatch.Group query, TypeDBOptions options) {
        return matchGroup(query.toString(), options);
    }

    @Override
    public Stream<ConceptMapGroup> matchGroup(String query) {
        return matchGroup(query, TypeDBOptions.core());
    }

    @Override
    public Stream<ConceptMapGroup> matchGroup(String query, TypeDBOptions options) {
        return stream(matchGroupReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchGroupResPart().getAnswersList().stream())
                .map(ConceptMapGroupImpl::of);
    }

    @Override
    public Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query) {
        return matchGroupAggregate(query.toString());
    }

    @Override
    public Stream<NumericGroup> match(TypeQLMatch.Group.Aggregate query, TypeDBOptions options) {
        return matchGroupAggregate(query.toString(), options);
    }

    @Override
    public Stream<NumericGroup> matchGroupAggregate(String query) {
        return matchGroupAggregate(query, TypeDBOptions.core());
    }

    @Override
    public Stream<NumericGroup> matchGroupAggregate(String query, TypeDBOptions options) {
        return stream(matchGroupAggregateReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchGroupAggregateResPart().getAnswersList().stream())
                .map(NumericGroupImpl::of);
    }

    @Override
    public Stream<ConceptMap> insert(TypeQLInsert query) {
        return insert(query.toString());
    }

    @Override
    public Stream<ConceptMap> insert(TypeQLInsert query, TypeDBOptions options) {
        return insert(query.toString(), options);
    }

    @Override
    public Stream<ConceptMap> insert(String query) {
        return insert(query, TypeDBOptions.core());
    }

    @Override
    public Stream<ConceptMap> insert(String query, TypeDBOptions options) {
        return stream(insertReq(query, options.proto()))
                .flatMap(rp -> rp.getInsertResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public QueryFuture<Void> delete(TypeQLDelete query) {
        return delete(query.toString());
    }

    @Override
    public QueryFuture<Void> delete(TypeQLDelete query, TypeDBOptions options) {
        return delete(query.toString(), options);
    }

    @Override
    public QueryFuture<Void> delete(String query) {
        return delete(query, TypeDBOptions.core());
    }

    @Override
    public QueryFuture<Void> delete(String query, TypeDBOptions options) {
        return queryVoid(deleteReq(query, options.proto()));
    }

    @Override
    public Stream<ConceptMap> update(TypeQLUpdate query) {
        return update(query.toString());
    }

    @Override
    public Stream<ConceptMap> update(TypeQLUpdate query, TypeDBOptions options) {
        return update(query.toString(), options);
    }

    @Override
    public Stream<ConceptMap> update(String query) {
        return update(query, TypeDBOptions.core());
    }

    @Override
    public Stream<ConceptMap> update(String query, TypeDBOptions options) {
        return stream(updateReq(query, options.proto()))
                .flatMap(rp -> rp.getUpdateResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public QueryFuture<Void> define(TypeQLDefine query) {
        return define(query.toString());
    }

    @Override
    public QueryFuture<Void> define(TypeQLDefine query, TypeDBOptions options) {
        return define(query.toString(), options);
    }

    @Override
    public QueryFuture<Void> define(String query) {
        return define(query, TypeDBOptions.core());
    }

    @Override
    public QueryFuture<Void> define(String query, TypeDBOptions options) {
        return queryVoid(defineReq(query, options.proto()));
    }

    @Override
    public QueryFuture<Void> undefine(TypeQLUndefine query) {
        return undefine(query.toString());
    }

    @Override
    public QueryFuture<Void> undefine(TypeQLUndefine query, TypeDBOptions options) {
        return define(query.toString(), options);
    }

    @Override
    public QueryFuture<Void> undefine(String query) {
        return undefine(query, TypeDBOptions.core());
    }

    @Override
    public QueryFuture<Void> undefine(String query, TypeDBOptions options) {
        return queryVoid(undefineReq(query, options.proto()));
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable) {
        return explain(explainable, TypeDBOptions.core());
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainable explainable, TypeDBOptions options) {
        return stream(explainReq(explainable.id(), options.proto()))
                .flatMap(rp -> rp.getExplainResPart().getExplanationsList().stream())
                .map(ExplanationImpl::of);
    }

    private QueryFuture<Void> queryVoid(TransactionProto.Transaction.Req.Builder req) {
        return transactionExt.query(req).map(res -> null);
    }

    private QueryFuture<QueryProto.QueryManager.Res> query(TransactionProto.Transaction.Req.Builder req) {
        return transactionExt.query(req).map(TransactionProto.Transaction.Res::getQueryManagerRes);
    }

    private Stream<QueryProto.QueryManager.ResPart> stream(TransactionProto.Transaction.Req.Builder req) {
        return transactionExt.stream(req).map(TransactionProto.Transaction.ResPart::getQueryManagerResPart);
    }
}
