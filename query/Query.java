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
import grakn.client.concept.type.ThingType;
import grakn.client.concept.type.impl.ThingTypeImpl;
import grakn.client.rpc.QueryFuture;
import grakn.client.rpc.RPCExecutor;
import grakn.client.rpc.RPCIterator;
import grakn.client.rpc.RPCTransaction;
import grakn.protocol.QueryProto;
import grakn.protocol.TransactionProto;
import graql.lang.query.GraqlDefine;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import graql.lang.query.GraqlUndefine;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grakn.client.common.ProtoBuilder.options;

public final class Query {

    private final RPCTransaction rpcTransaction;

    public Query(final RPCTransaction rpcTransaction) {
        this.rpcTransaction = rpcTransaction;
    }

    public QueryFuture<Stream<ConceptMap>> match(final GraqlMatch query) {
        return match(query, new GraknOptions());
    }

    public QueryFuture<Stream<ConceptMap>> match(final GraqlMatch query, final GraknOptions options) {
        final QueryProto.Query.Iter.Req.Builder request = newIterRequest().setMatchIterReq(
                QueryProto.Graql.Match.Iter.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> ConceptMap.of(rpcTransaction, res.getMatchIterRes().getAnswer()));
    }

    public QueryFuture<Stream<ConceptMap>> insert(final GraqlInsert query) {
        return insert(query, new GraknOptions());
    }

    public QueryFuture<Stream<ConceptMap>> insert(final GraqlInsert query, final GraknOptions options) {
        final QueryProto.Query.Iter.Req.Builder request = newIterRequest().setInsertIterReq(
                QueryProto.Graql.Insert.Iter.Req.newBuilder().setQuery(query.toString()));
        return iterateQuery(request, options, res -> ConceptMap.of(rpcTransaction, res.getInsertIterRes().getAnswer()));
    }

    public QueryFuture<Void> delete(final GraqlDelete query) {
        return delete(query, new GraknOptions());
    }

    public QueryFuture<Void> delete(final GraqlDelete query, final GraknOptions options) {
        return runQuery(newRequest().setDeleteReq(QueryProto.Graql.Delete.Req.newBuilder().setQuery(query.toString())), options);
    }

    public QueryFuture<List<ThingType>> define(final GraqlDefine query) {
        return define(query, new GraknOptions());
    }

    public QueryFuture<List<ThingType>> define(final GraqlDefine query, final GraknOptions options) {
        final QueryProto.Query.Req.Builder request = newRequest().setDefineReq(
                QueryProto.Graql.Define.Req.newBuilder().setQuery(query.toString()));
        return runQuery(request, options, res -> res.getDefineRes().getThingTypeList().stream()
                .map(ThingTypeImpl::of).collect(Collectors.toList()));
    }

    public QueryFuture<Void> undefine(final GraqlUndefine query) {
        return undefine(query, new GraknOptions());
    }

    public QueryFuture<Void> undefine(final GraqlUndefine query, final GraknOptions options) {
        return runQuery(newRequest().setUndefineReq(QueryProto.Graql.Undefine.Req.newBuilder().setQuery(query.toString())), options);
    }

    private static QueryProto.Query.Req.Builder newRequest() {
        return QueryProto.Query.Req.newBuilder();
    }

    private static QueryProto.Query.Iter.Req.Builder newIterRequest() {
        return QueryProto.Query.Iter.Req.newBuilder();
    }

    private QueryFuture<Void> runQuery(final QueryProto.Query.Req.Builder request, final GraknOptions options) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options))).build();
        final RPCExecutor rpcExecutor = new RPCExecutor(rpcTransaction, req);
        return rpcExecutor.await();
    }

    private <T> QueryFuture<T> runQuery(final QueryProto.Query.Req.Builder request, final GraknOptions options,
                                        final Function<QueryProto.Query.Res, T> responseReader) {
        final TransactionProto.Transaction.Req req = TransactionProto.Transaction.Req.newBuilder()
                .setQueryReq(request.setOptions(options(options))).build();
        final RPCExecutor rpcExecutor = new RPCExecutor(rpcTransaction, req);
        return rpcExecutor.await(responseReader);
    }

    private <T> QueryFuture<Stream<T>> iterateQuery(final QueryProto.Query.Iter.Req.Builder request, final GraknOptions options,
                                                    final Function<QueryProto.Query.Iter.Res, T> responseReader) {
        final TransactionProto.Transaction.Iter.Req req = TransactionProto.Transaction.Iter.Req.newBuilder()
                .setQueryIterReq(request.setOptions(options(options))).build();
        final RPCIterator rpcIterator = new RPCIterator(rpcTransaction, req);
        return rpcIterator.await(responseReader);
    }
}
