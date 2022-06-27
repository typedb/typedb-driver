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

import { Transaction } from "typedb-protocol/common/transaction_pb";
import { Stream } from "../../common/util/Stream";
import { ConceptManager } from "../concept/ConceptManager";
import { LogicManager } from "../logic/LogicManager";
import { QueryManager } from "../query/QueryManager";
import { TypeDBOptions } from "./TypeDBOptions";

export interface TypeDBTransaction {

    isOpen(): boolean;

    readonly type: TransactionType;

    readonly options: TypeDBOptions;

    readonly concepts: ConceptManager;

    readonly logic: LogicManager;

    readonly query: QueryManager;

    commit(): Promise<void>;

    rollback(): Promise<void>;

    close(): Promise<void>;
}

export interface TransactionType {

    proto(): Transaction.Type;

    isRead(): boolean;

    isWrite(): boolean;
}

export namespace TransactionType {

    class TransactionTypeImpl implements TransactionType {

        private readonly _type: Transaction.Type;

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

export namespace TypeDBTransaction {

    export interface Extended extends TypeDBTransaction {

        rpcExecute(request: Transaction.Req, batch?: boolean): Promise<Transaction.Res>;

        rpcStream(request: Transaction.Req): Stream<Transaction.ResPart>;
    }
}
