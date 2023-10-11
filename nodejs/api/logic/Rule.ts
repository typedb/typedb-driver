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

import {TypeDBTransaction} from "../connection/TypeDBTransaction";

/**
 * Rules are a part of schema and define embedded logic.
 * The reasoning engine uses rules as a set of logic to infer new data.
 * A rule consists of a condition and a conclusion, and is uniquely identified by a label.
 */
export interface Rule {
    /** The unique label of the rule. */
    readonly label: string;

    /** The statements that constitute the ‘when’ of the rule. */
    readonly when: string;

    /** The single statement that constitutes the ‘then’ of the rule. */
    readonly then: string;

    /**
     * Renames the label of the rule. The new label must remain unique.
     *
     * ### Examples
     *
     * ```ts
     * rule.setLabel(transaction, newLabel)
     * ```
     *
     * @param transaction - The current <code>Transaction</code>
     * @param newLabel - The new label to be given to the rule
     */
    setLabel(transaction: TypeDBTransaction, label: string): Promise<void>;

    /**
     * Deletes this rule.
     *
     * ### Examples
     *
     * ```ts
     * rule.delete(transaction)
     * ```
     *
     * @param transaction - The current <code>Transaction</code>
     */
    delete(transaction: TypeDBTransaction): Promise<void>;

    /**
     * Check if this rule has been deleted.
     *
     * ### Examples
     *
     * ```ts
     * rule.isDeleted(transaction)
     * ```
     *
     * @param transaction - The current <code>Transaction</code>
     */
    isDeleted(transaction: TypeDBTransaction): Promise<boolean>;
}
