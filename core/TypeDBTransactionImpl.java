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

package com.vaticle.typedb.client.core;

import com.google.protobuf.ByteString;
import com.vaticle.typedb.client.api.TypeDBOptions;
import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.ConceptManager;
import com.vaticle.typedb.client.api.logic.LogicManager;
import com.vaticle.typedb.client.api.query.QueryFuture;
import com.vaticle.typedb.client.api.query.QueryManager;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.logic.LogicManagerImpl;
import com.vaticle.typedb.client.query.QueryManagerImpl;
import com.vaticle.typedb.client.stream.BidirectionalStream;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.Req;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.Res;
import com.vaticle.typedb.protocol.TransactionProto.Transaction.ResPart;

import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.TRANSACTION_CLOSED;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Transaction.commitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Transaction.openReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Transaction.rollbackReq;

public class TypeDBTransactionImpl implements TypeDBTransaction.Extended {

    private final TypeDBTransaction.Type type;
    private final TypeDBOptions options;
    private final ConceptManager conceptMgr;
    private final LogicManager logicMgr;
    private final QueryManager queryMgr;

    private final BidirectionalStream bidirectionalStream;

    TypeDBTransactionImpl(TypeDBSessionImpl session, ByteString sessionId, Type type, TypeDBOptions options) {
        this.type = type;
        this.options = options;
        conceptMgr = new ConceptManagerImpl(this);
        logicMgr = new LogicManagerImpl(this);
        queryMgr = new QueryManagerImpl(this);
        bidirectionalStream = new BidirectionalStream(session.stub(), session.transmitter());
        execute(openReq(sessionId, type.proto(), options.proto(), session.networkLatencyMillis()), false);
    }

    @Override
    public Type type() { return type; }

    @Override
    public TypeDBOptions options() { return options; }

    @Override
    public boolean isOpen() { return bidirectionalStream.isOpen(); }

    @Override
    public ConceptManager concepts() { return conceptMgr; }

    @Override
    public LogicManager logic() { return logicMgr; }

    @Override
    public QueryManager query() { return queryMgr; }

    @Override
    public Res execute(Req.Builder request) {
        return execute(request, true);
    }

    private Res execute(Req.Builder request, boolean batch) {
        return query(request, batch).get();
    }

    @Override
    public QueryFuture<Res> query(Req.Builder request) {
        return query(request, true);
    }

    private QueryFuture<Res> query(Req.Builder request, boolean batch) {
        if (!isOpen()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        BidirectionalStream.Single<Res> single = bidirectionalStream.single(request, batch);
        return single::get;
    }

    @Override
    public Stream<ResPart> stream(Req.Builder request) {
        if (!isOpen()) throw new TypeDBClientException(TRANSACTION_CLOSED);
        return bidirectionalStream.stream(request);
    }

    @Override
    public void commit() {
        try {
            execute(commitReq());
        } finally {
            close();
        }
    }

    @Override
    public void rollback() {
        execute(rollbackReq());
    }

    @Override
    public void close() {
        bidirectionalStream.close();
    }
}
