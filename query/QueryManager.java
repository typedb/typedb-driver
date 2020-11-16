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
import grakn.client.rpc.QueryFuture;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUndefine;

import java.util.function.Function;
import java.util.stream.Stream;

import static grakn.client.common.ProtoBuilder.options;

public final class QueryManager {

    private final RPCTransaction rpcTransaction;

    public QueryManager(final RPCTransaction rpcTransaction) {
        this.rpcTransaction = rpcTransaction;
    }

    public Stream<ConceptMap> match(final GraqlMatch query) {
        return match(query, new GraknOptions());
    }

    public Stream<ConceptMap> match(final GraqlMatch query, final GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setMatchReq(
                QueryProto.Graql.Match.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> ConceptMap.of(res.getQueryRes().getMatchRes().getAnswer()));
    }

    public Stream<ConceptMap> insert(final GraqlInsert query) {
        return insert(query, new GraknOptions());
    }

    public Stream<ConceptMap> insert(final GraqlInsert query, final GraknOptions options) {
        final QueryProto.Query.Req.Builder request = QueryProto.Query.Req.newBuilder().setInsertReq(
                QueryProto.Graql.Insert.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> ConceptMap.of(res.getQueryRes().getInsertRes().getAnswer()));
    }

    public QueryFuture<Void> delete(final GraqlDelete query) {
        return delete(query, new GraknOptions());
    }

    public QueryFuture<Void> delete(final GraqlDelete query, final GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setDeleteReq(QueryProto.Graql.Delete.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<Void> define(final GraqlDefine query) {
        return define(query, new GraknOptions());
    }

    public QueryFuture<Void> define(final GraqlDefine query, final GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setDefineReq(QueryProto.Graql.Define.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<Void> undefine(final GraqlUndefine query) {
        return undefine(query, new GraknOptions());
    }

    public QueryFuture<Void> undefine(final GraqlUndefine query, final GraknOptions options) {
        return runQuery(QueryProto.Query.Req.newBuilder().setUndefineReq(QueryProto.Graql.Undefine.Req.newBuilder().setQuery(query.toString())), options);
    }

    private QueryFuture<Void> runQuery(final QueryProto.Query.Req.Builder request, final GraknOptions options) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options)));
        return rpcTransaction.executeAsync(req, res -> null);
    }

    private <T> Stream<T> iterateQuery(final QueryProto.Query.Req.Builder request, final GraknOptions options,
                                                    final Function<TransactionProto.Transaction.Res, T> responseReader) {
        final TransactionProto.Transaction.Req.Builder req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options)));
        return rpcTransaction.stream(req, responseReader);
    }
}
