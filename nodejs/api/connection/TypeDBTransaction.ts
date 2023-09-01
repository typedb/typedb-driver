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

import {
    TransactionReq,
    TransactionRes,
    TransactionResPart,
    TransactionType as TransactionTypeProto
} from "typedb-protocol/proto/transaction";
import {Stream} from "../../common/util/Stream";
import {ConceptManager} from "../concept/ConceptManager";
import {LogicManager} from "../logic/LogicManager";
import {QueryManager} from "../query/QueryManager";
import {TypeDBOptions} from "./TypeDBOptions";

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
    proto(): TransactionTypeProto;

    isRead(): boolean;

    isWrite(): boolean;
}

export namespace TransactionType {
    class TransactionTypeImpl implements TransactionType {
        private readonly _type: TransactionTypeProto;

        constructor(type: TransactionTypeProto) {
            this._type = type;
        }

        proto(): TransactionTypeProto {
            return this._type;
        }

        isRead(): boolean {
            return this == READ;
        }

        isWrite(): boolean {
            return this == WRITE;
        }
    }

    export const READ = new TransactionTypeImpl(TransactionTypeProto.READ);
    export const WRITE = new TransactionTypeImpl(TransactionTypeProto.WRITE);
}

export namespace TypeDBTransaction {
    export interface Extended extends TypeDBTransaction {
        rpcExecute(request: TransactionReq, batch?: boolean): Promise<TransactionRes>;

        rpcStream(request: TransactionReq): Stream<TransactionResPart>;
    }
}
