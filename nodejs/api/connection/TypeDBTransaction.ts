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
    /**
     * Checks whether this transaction is open.
     *
     * ### Examples
     *
     * ```ts
     * transaction.isOpen()
     * ```
     */
    isOpen(): boolean;

    /** The transaction’s type (READ or WRITE) */
    readonly type: TransactionType;

    /** The options for the transaction. */
    readonly options: TypeDBOptions;

    /** The <code>ConceptManager</code> for this transaction, providing access to all Concept API methods. */
    readonly concepts: ConceptManager;

    /** The <code>LogicManager</code> for this Transaction, providing access to all Concept API - Logic methods. */
    readonly logic: LogicManager;

    /** The<code></code>QueryManager<code></code> for this Transaction, from which any TypeQL query can be executed. */
    readonly query: QueryManager;

    /**
     * Commits the changes made via this transaction to the TypeDB database. Whether or not the transaction is commited successfully, it gets closed after the commit call.
     *
     * ### Examples
     *
     * ```ts
     * transaction.commit()
     * ```
     */
    commit(): Promise<void>;

    /**
     * Rolls back the uncommitted changes made via this transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.rollback()
     * ```
     */
    rollback(): Promise<void>;

    /**
     * Closes the transaction.
     *
     * ### Examples
     *
     * ```ts
     * transaction.close()
     * ```
     */
    close(): Promise<void>;
}

/**
 * This class is used to specify the type of transaction.
 *
 * ### Examples
 *
 * ```ts
 * session.transaction(TransactionType.READ)
 * ```
 */
export interface TransactionType {
    proto(): TransactionTypeProto;

    /** Checks whether this is the READ TransactionType  */
    isRead(): boolean;

    /** Checks whether this is the WRITE TransactionType  */
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

    /** Constant used to specify a READ transaction must be created  */
    export const READ = new TransactionTypeImpl(TransactionTypeProto.READ);
    /** Constant used to specify a WRITE transaction must be created  */
    export const WRITE = new TransactionTypeImpl(TransactionTypeProto.WRITE);
}

/** @ignore */
export namespace TypeDBTransaction {
    export interface Extended extends TypeDBTransaction {
        rpcExecute(request: TransactionReq, batch?: boolean): Promise<TransactionRes>;

        rpcStream(request: TransactionReq): Stream<TransactionResPart>;
    }
}
