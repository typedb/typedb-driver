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

import {QueryManagerRes, QueryManagerResPart} from "typedb-protocol/proto/query";
import {TransactionReq} from "typedb-protocol/proto/transaction";
import {ConceptMap} from "../api/answer/ConceptMap";
import {ConceptMapGroup} from "../api/answer/ConceptMapGroup";
import {Numeric} from "../api/answer/Numeric";
import {NumericGroup} from "../api/answer/NumericGroup";
import {TypeDBOptions} from "../api/connection/TypeDBOptions";
import {TypeDBTransaction} from "../api/connection/TypeDBTransaction";
import {Explanation} from "../api/logic/Explanation";
import {QueryManager} from "../api/query/QueryManager";
import {RequestBuilder} from "../common/rpc/RequestBuilder";
import {Stream} from "../common/util/Stream";
import {ConceptMapImpl} from "../concept/answer/ConceptMapImpl";
import {NumericGroupImpl} from "../concept/answer/NumericGroupImpl";
import {ConceptMapGroupImpl} from "../concept/answer/ConceptMapGroupImpl";
import {NumericImpl} from "../concept/answer/NumericImpl";
import {ExplanationImpl} from "../logic/ExplanationImpl";

export class QueryManagerImpl implements QueryManager {
    private _transaction: TypeDBTransaction.Extended;

    constructor(transaction: TypeDBTransaction.Extended) {
        this._transaction = transaction;
    }

    match(query: string, options?: TypeDBOptions): Stream<ConceptMap> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.matchReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.match_res_part.answers)
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    matchAggregate(query: string, options?: TypeDBOptions): Promise<Numeric> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.matchAggregateReq(query, options.proto());
        return this.query(request).then((res) => NumericImpl.of(res.match_aggregate_res.answer));
    }

    matchGroup(query: string, options?: TypeDBOptions): Stream<ConceptMapGroup> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.matchGroupReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.match_group_res_part.answers)
                .map((conceptMapGroupProto) => ConceptMapGroupImpl.of(conceptMapGroupProto))
        );
    }

    matchGroupAggregate(query: string, options?: TypeDBOptions): Stream<NumericGroup> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.matchGroupAggregateReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.match_group_aggregate_res_part.answers)
                .map((numericGroupArray) => NumericGroupImpl.of(numericGroupArray))
        );
    }

    insert(query: string, options?: TypeDBOptions): Stream<ConceptMap> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.insertReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.insert_res_part.answers)
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    delete(query: string, options?: TypeDBOptions): Promise<void> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.deleteReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    update(query: string, options?: TypeDBOptions): Stream<ConceptMap> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.updateReq(query, options.proto());
        return this.stream(request).flatMap((queryResPart) =>
            Stream.array(queryResPart.update_res_part.answers)
                .map((conceptMapProto) => ConceptMapImpl.of(conceptMapProto))
        );
    }

    define(query: string, options?: TypeDBOptions): Promise<void> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.defineReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    undefine(query: string, options?: TypeDBOptions): Promise<void> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.undefineReq(query, options.proto());
        return this.query(request).then(() => null);
    }

    explain(explainable: ConceptMap.Explainable, options?: TypeDBOptions): Stream<Explanation> {
        if (!options) options = new TypeDBOptions();
        const request = RequestBuilder.QueryManager.explainReq(explainable.id, options.proto());
        return this.stream(request)
            .flatMap((resPart) => Stream.array(resPart.explain_res_part.explanations))
            .map((explanationProto) => ExplanationImpl.of(explanationProto));
    }

    private query(req: TransactionReq): Promise<QueryManagerRes> {
        return this._transaction.rpcExecute(req).then((res) => res.query_manager_res);
    }

    private stream(req: TransactionReq): Stream<QueryManagerResPart> {
        return this._transaction.rpcStream(req).map((res) => res.query_manager_res_part);
    }
}
