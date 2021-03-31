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


import {GraknOptions} from "./GraknOptions";
import {Transaction} from "grakn-protocol/common/transaction_pb";
import {ConceptManager} from "./concept/ConceptManager";
import {LogicManager} from "./logic/LogicManager";
import {QueryManager} from "./query/QueryManager";
import {Stream} from "../common/util/Stream";

export interface GraknTransaction {

    isOpen(): boolean;

    type(): TransactionType;

    options(): GraknOptions;

    concepts(): ConceptManager;

    logic(): LogicManager;

    query(): QueryManager;

    commit(): void;

    rollback(): void;

    close(): void;

}


export interface TransactionType {
    proto(): Transaction.Type;

    isRead(): boolean;

    isWrite(): boolean;
}

export namespace TransactionType {

    class TransactionTypeImpl implements TransactionType {

        private _type: Transaction.Type;

        constructor(type: Transaction.Type) {
            this._type = type;
        }

        proto(): Transaction.Type {
            return this._type;
        }

        isRead(): boolean {
            return this == READ;
        }

        isWrite(): boolean {
            return this == WRITE;
        }

    }

    export const READ = new TransactionTypeImpl(Transaction.Type.READ);
    export const WRITE = new TransactionTypeImpl(Transaction.Type.WRITE);
}

export namespace GraknTransaction {

    export interface Extended extends GraknTransaction {

        rpcExecute(request: Transaction.Req): Promise<Transaction.Res>;

        rpcStream(request: Transaction.Req): Stream<Transaction.ResPart>;

    }

}
