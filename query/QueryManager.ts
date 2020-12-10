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

import {
    RPCTransaction,
    GraknOptions,
    ProtoBuilder,
    Stream,
    ConceptMap,
} from "../dependencies_internal";
import QueryProto from "grakn-protocol/protobuf/query_pb";
import Query = QueryProto.Query;
import Graql = QueryProto.Graql;
import TransactionProto from "grakn-protocol/protobuf/transaction_pb";
import Transaction = TransactionProto.Transaction;

export class QueryManager {
    private readonly _rpcTransaction: RPCTransaction;
    constructor (transaction: RPCTransaction) {
        this._rpcTransaction = transaction;
    }

    public match(query: string, options?: GraknOptions): Stream<ConceptMap> {
        const matchQuery = new Query.Req().setMatchReq(
            new Graql.Match.Req().setQuery(query));
        return this.iterateQuery(matchQuery, options ? options : new GraknOptions(), (res: Transaction.Res) => res.getQueryRes().getMatchRes().getAnswerList().map(ConceptMap.of));
    }

    public insert(query: string, options?: GraknOptions): Stream<ConceptMap> {
        const insertQuery = new Query.Req().setInsertReq(
            new Graql.Insert.Req().setQuery(query));
        return this.iterateQuery(insertQuery, options ? options : new GraknOptions(), (res: Transaction.Res) => res.getQueryRes().getInsertRes().getAnswerList().map(ConceptMap.of));
    }

    public delete(query: string, options?: GraknOptions): Promise<void> {
        const deleteQuery = new Query.Req().setDeleteReq(
            new Graql.Delete.Req().setQuery(query));
        return this.runQuery(deleteQuery, options ? options : new GraknOptions())
    }

    public define(query: string, options?: GraknOptions): Promise<void> {
        const defineQuery = new Query.Req().setDefineReq(
                    new Graql.Define.Req().setQuery(query));
        return this.runQuery(defineQuery, options ? options : new GraknOptions())
    }

    public undefine(query: string, options?: GraknOptions): Promise<void> {
        const undefineQuery = new Query.Req().setUndefineReq(
            new Graql.Undefine.Req().setQuery(query));
        return this.runQuery(undefineQuery, options ? options : new GraknOptions())
    }

    private async runQuery(request: Query.Req, options: GraknOptions): Promise<void> {
        const transactionRequest = new Transaction.Req()
            .setQueryReq(request.setOptions(ProtoBuilder.options(options)));
        await this._rpcTransaction.execute(transactionRequest);
    }

    private iterateQuery<T>(request: Query.Req, options: GraknOptions, responseReader: (res: Transaction.Res) => T[]): Stream<T> {
        const transactionRequest = new Transaction.Req()
            .setQueryReq(request.setOptions(ProtoBuilder.options(options)));
        return this._rpcTransaction.stream(transactionRequest, responseReader);
    }
}
