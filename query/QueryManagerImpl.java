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

package grakn.client.query;

import grakn.client.api.GraknOptions;
import grakn.client.api.GraknTransaction;
import grakn.client.api.answer.ConceptMap;
import grakn.client.api.answer.ConceptMapGroup;
import grakn.client.api.logic.Explanation;
import grakn.client.api.answer.Numeric;
import grakn.client.api.answer.NumericGroup;
import grakn.client.api.query.QueryFuture;
import grakn.client.api.query.QueryManager;
import grakn.client.concept.answer.ConceptMapGroupImpl;
import grakn.client.concept.answer.ConceptMapImpl;
import grakn.client.logic.ExplanationImpl;
import grakn.client.concept.answer.NumericGroupImpl;
import grakn.client.concept.answer.NumericImpl;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUndefine;
import graql.lang.query.GraqlUpdate;

import java.util.stream.Stream;

import static grakn.client.common.rpc.RequestBuilder.QueryManager.defineReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.deleteReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.explainReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.insertReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.matchAggregateReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.matchGroupAggregateReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.matchGroupReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.matchReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.undefineReq;
import static grakn.client.common.rpc.RequestBuilder.QueryManager.updateReq;

public final class QueryManagerImpl implements QueryManager {

    private final GraknTransaction.Extended transactionExt;

    public QueryManagerImpl(GraknTransaction.Extended transactionExt) {
        this.transactionExt = transactionExt;
    }

    @Override
    public Stream<ConceptMap> match(GraqlMatch query) {
        return match(query, GraknOptions.core());
    }

    @Override
    public Stream<ConceptMap> match(GraqlMatch query, GraknOptions options) {
        return stream(matchReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public QueryFuture<Numeric> match(GraqlMatch.Aggregate query) {
        return match(query, GraknOptions.core());
    }

    @Override
    public QueryFuture<Numeric> match(GraqlMatch.Aggregate query, GraknOptions options) {
        return query(matchAggregateReq(query, options.proto()))
                .map(r -> r.getMatchAggregateRes().getAnswer())
                .map(NumericImpl::of);
    }

    @Override
    public Stream<ConceptMapGroup> match(GraqlMatch.Group query) {
        return match(query, GraknOptions.core());
    }

    @Override
    public Stream<ConceptMapGroup> match(GraqlMatch.Group query, GraknOptions options) {
        return stream(matchGroupReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchGroupResPart().getAnswersList().stream())
                .map(ConceptMapGroupImpl::of);
    }

    @Override
    public Stream<NumericGroup> match(GraqlMatch.Group.Aggregate query) {
        return match(query, GraknOptions.core());
    }

    @Override
    public Stream<NumericGroup> match(GraqlMatch.Group.Aggregate query, GraknOptions options) {
        return stream(matchGroupAggregateReq(query, options.proto()))
                .flatMap(rp -> rp.getMatchGroupAggregateResPart().getAnswersList().stream())
                .map(NumericGroupImpl::of);
    }

    @Override
    public Stream<ConceptMap> insert(GraqlInsert query) {
        return insert(query, GraknOptions.core());
    }

    @Override
    public Stream<ConceptMap> insert(GraqlInsert query, GraknOptions options) {
        return stream(insertReq(query, options.proto()))
                .flatMap(rp -> rp.getInsertResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public QueryFuture<Void> delete(GraqlDelete query) {
        return delete(query, GraknOptions.core());
    }

    @Override
    public QueryFuture<Void> delete(GraqlDelete query, GraknOptions options) {
        return queryVoid(deleteReq(query, options.proto()));
    }

    @Override
    public Stream<ConceptMap> update(GraqlUpdate query) {
        return update(query, GraknOptions.core());
    }

    @Override
    public Stream<ConceptMap> update(GraqlUpdate query, GraknOptions options) {
        return stream(updateReq(query.toString(), options.proto()))
                .flatMap(rp -> rp.getUpdateResPart().getAnswersList().stream())
                .map(ConceptMapImpl::of);
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainables.Explainable explainable) {
        return explain(explainable, GraknOptions.core());
    }

    @Override
    public Stream<Explanation> explain(ConceptMap.Explainables.Explainable explainable, GraknOptions options) {
        return stream(explainReq(explainable.id(), options.proto()))
                .flatMap(rp -> rp.getExplainResPart().getExplanationsList().stream())
                .map(ExplanationImpl::of);
    }

    @Override
    public QueryFuture<Void> define(GraqlDefine query) {
        return define(query, GraknOptions.core());
    }

    @Override
    public QueryFuture<Void> define(GraqlDefine query, GraknOptions options) {
        return queryVoid(defineReq(query, options.proto()));
    }

    @Override
    public QueryFuture<Void> undefine(GraqlUndefine query) {
        return undefine(query, GraknOptions.core());
    }

    @Override
    public QueryFuture<Void> undefine(GraqlUndefine query, GraknOptions options) {
        return queryVoid(undefineReq(query, options.proto()));
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
