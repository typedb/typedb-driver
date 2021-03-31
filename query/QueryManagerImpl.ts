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

import {QueryManager} from "../api/query/QueryManager";
import {GraknOptions} from "../api/GraknOptions";
import {ConceptMap} from "../api/answer/ConceptMap";
import {Numeric} from "../api/answer/Numeric";
import {ConceptMapGroup} from "../api/answer/ConceptMapGroup";
import {NumericGroup} from "../api/answer/NumericGroup";
import {GraknTransaction} from "../api/GraknTransaction";
import {Explanation} from "../api/logic/Explanation";
import {ExplanationImpl} from "../logic/ExplanationImpl";
import {ConceptMapImpl} from "../concept/answer/ConceptMapImpl";
import {ConceptMapGroupImpl} from "../concept/answer/ConceptMapGroupImpl";
import {NumericImpl} from "../concept/answer/NumericImpl";
import {NumericGroupImpl} from "../concept/answer/NumericGroupImpl";
import {Stream} from "../common/util/Stream";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {Transaction as TransactionProto} from "grakn-protocol/common/transaction_pb";
import {QueryManager as QueryProto} from "grakn-protocol/common/query_pb";


export class QueryManagerImpl implements QueryManager {

    private _transaction: GraknTransaction.Extended;

    constructor(transaction: GraknTransaction.Extended) {
        this._transaction = transaction;
    }

    match(query: string, options?: GraknOptions): Stream<ConceptMap> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.matchReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.getMatchResPart().getAnswersList())
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    matchAggregate(query: string, options?: GraknOptions): Promise<Numeric> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.matchAggregateReq(query, options.proto());
        return this.query(request).then((res) => NumericImpl.of(res.getMatchAggregateRes().getAnswer()));
    }

    matchGroup(query: string, options?: GraknOptions): Stream<ConceptMapGroup> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.matchGroupReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.getMatchGroupResPart().getAnswersList())
                .map((conceptMapGroupProto) => ConceptMapGroupImpl.of(conceptMapGroupProto))
        );
    }

    matchGroupAggregate(query: string, options?: GraknOptions): Stream<NumericGroup> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.matchGroupAggregateReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.getMatchGroupAggregateResPart().getAnswersList())
                .map((numericGroupArray) => NumericGroupImpl.of(numericGroupArray))
        );
    }

    insert(query: string, options?: GraknOptions): Stream<ConceptMap> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.insertReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.getInsertResPart().getAnswersList())
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    delete(query: string, options?: GraknOptions): Promise<void> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.deleteReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    update(query: string, options?: GraknOptions): Stream<ConceptMap> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.updateReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.getUpdateResPart().getAnswersList())
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    define(query: string, options?: GraknOptions): Promise<void> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.defineReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    undefine(query: string, options?: GraknOptions): Promise<void> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.undefineReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    explain(explainable: ConceptMap.Explainable, options?: GraknOptions): Stream<Explanation> {
        if (!options) options = GraknOptions.core();
        const request = RequestBuilder.QueryManager.explainReq(explainable.id(), options.proto());
        return this.stream(request)
            .flatMap((resPart) => Stream.array(resPart.getExplainResPart().getExplanationsList()))
            .map((explanationProto) => ExplanationImpl.of(explanationProto));
    }

    private query(req: TransactionProto.Req): Promise<QueryProto.Res> {
        return this._transaction.rpcExecute(req).then((res) => res.getQueryManagerRes());
    }

    private stream(req: TransactionProto.Req): Stream<QueryProto.ResPart> {
        return this._transaction.rpcStream(req).map((res) => res.getQueryManagerResPart());
    }
}