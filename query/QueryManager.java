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

import grakn.client.GraknOptions;
import grakn.client.concept.answer.ConceptMap;
import grakn.client.concept.answer.ConceptMapGroup;
import grakn.client.concept.answer.Numeric;
import grakn.client.concept.answer.NumericGroup;
import grakn.client.rpc.QueryFuture;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUndefine;
import graql.lang.query.GraqlUpdate;

import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.GraknProtoBuilder.options;

public final class QueryManager {

    private final RPCTransaction rpcTransaction;

    public QueryManager(RPCTransaction rpcTransaction) {
        this.rpcTransaction = rpcTransaction;
    }

    public Stream<ConceptMap> match(GraqlMatch query) {
        return match(query, GraknOptions.core());
    }

    public Stream<ConceptMap> match(GraqlMatch query, GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setMatchReq(
                QueryProto.Query.Match.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> res.getQueryRes().getMatchRes().getAnswersList().stream().map(ConceptMap::of));
    }

    public QueryFuture<Numeric> match(GraqlMatch.Aggregate query) {
        return match(query, GraknOptions.core());
    }

    public QueryFuture<Numeric> match(GraqlMatch.Aggregate query, GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setMatchAggregateReq(
                QueryProto.Query.MatchAggregate.Req.newBuilder().setQuery(query.toString()));
        return runQuery(request, options, res -> Numeric.of(res.getQueryRes().getMatchAggregateRes().getAnswer()));
    }

    public Stream<ConceptMapGroup> match(GraqlMatch.Group query) {
        return match(query, GraknOptions.core());
    }

    public Stream<ConceptMapGroup> match(GraqlMatch.Group query, GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setMatchGroupReq(
                QueryProto.Query.MatchGroup.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(
                request, options,
                res -> res.getQueryRes().getMatchGroupRes().getAnswersList().stream().map(ConceptMapGroup::of)
        );
    }

    public Stream<NumericGroup> match(GraqlMatch.Group.Aggregate query) {
        return match(query, GraknOptions.core());
    }

    public Stream<NumericGroup> match(GraqlMatch.Group.Aggregate query, GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setMatchGroupAggregateReq(
                QueryProto.Query.MatchGroupAggregate.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(
                request, options,
                res -> res.getQueryRes().getMatchGroupAggregateRes().getAnswersList().stream().map(NumericGroup::of)
        );
    }

    public Stream<ConceptMap> insert(GraqlInsert query) {
        return insert(query, GraknOptions.core());
    }

    public Stream<ConceptMap> insert(GraqlInsert query, GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setInsertReq(
                QueryProto.Query.Insert.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> res.getQueryRes().getInsertRes().getAnswersList().stream().map(ConceptMap::of));
    }

    public QueryFuture<Void> delete(GraqlDelete query) {
        return delete(query, GraknOptions.core());
    }

    public QueryFuture<Void> delete(GraqlDelete query, GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setDeleteReq(QueryProto.Query.Delete.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<Void> update(GraqlUpdate query) {
        return update(query, GraknOptions.core());
    }

    public QueryFuture<Void> update(GraqlUpdate query, GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setUpdateReq(QueryProto.Query.Update.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<Void> define(GraqlDefine query) {
        return define(query, GraknOptions.core());
    }

    public QueryFuture<Void> define(GraqlDefine query, GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setDefineReq(QueryProto.Query.Define.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<Void> undefine(GraqlUndefine query) {
        return undefine(query, GraknOptions.core());
    }

    public QueryFuture<Void> undefine(GraqlUndefine query, GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setUndefineReq(QueryProto.Query.Undefine.Req.newBuilder().setQuery(query.toString())), options);
    }

    private <T> QueryFuture<T> runQuery(QueryProto.Query.Req.Builder request, GraknOptions options, Function<TransactionProto.Transaction.Res, T> mapper) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options)));
        return rpcTransaction.executeAsync(req, mapper);
    }

    private QueryFuture<Void> runQuery(QueryProto.Query.Req.Builder request, GraknOptions options) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options)));
        return rpcTransaction.executeAsync(req, res -> null);
    }

    private <T> Stream<T> iterateQuery(QueryProto.Query.Req.Builder request, GraknOptions options,
                                       Function<TransactionProto.Transaction.Res, Stream<T>> responseReader) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options)));
        return rpcTransaction.stream(req, responseReader);
    }
}
