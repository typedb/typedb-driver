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


import {Label} from "../../../common/Label";
import {Stream} from "../../../common/util/Stream";
import {TypeDBTransaction} from "../../connection/TypeDBTransaction";
import {Concept} from "../Concept";
import Transitivity = Concept.Transitivity;

export interface Type extends Concept {
     /** The unique label of the type. */
    readonly label: Label;

    /** Whether the type is a root type. */
    readonly root: boolean;

    /** Whether the type is prevented from having data instances (i.e., <code>abstract</code>). */
    readonly abstract: boolean;

    /**
     * Deletes this type from the database.
     *
     * ### Examples
     *
     * ```ts
     * type.delete(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    delete(transaction: TypeDBTransaction): Promise<void>;

    /**
     * Check if the concept has been deleted
     *
     * @param transaction The current transaction
     */
    isDeleted(transaction: TypeDBTransaction): Promise<boolean>;

    /**
     * Renames the label of the type. The new label must remain unique.
     *
     * ### Examples
     *
     * ```ts
     * type.setLabel(transaction, label)
     * ```
     *
     * @param transaction - The current transaction
     * @param label - The new <code>Label</code> to be given to the type.
     */
    setLabel(transaction: TypeDBTransaction, label: string): Promise<void>;

    /**
     * Retrieves the most immediate supertype of the type.
     *
     * ### Examples
     *
     * ```ts
     * type.getSupertype(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSupertype(transaction: TypeDBTransaction): Promise<Type>;

    /**
     * Retrieves all supertypes of the type.
     *
     * ### Examples
     *
     * ```ts
     * type.getSupertypes(transaction)
     * ```
     *
     * @param transaction - The current transaction
     */
    getSupertypes(transaction: TypeDBTransaction): Stream<Type>;

    /** {@inheritDoc Type#getSubtypes:(1)} */
    getSubtypes(transaction: TypeDBTransaction): Stream<Type>;
    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the type.
     *
     * ### Examples
     *
     * ```ts
     * type.getSubtypes(transaction)
     * type.getSubtypes(transaction, Transitivity.EXPLICIT)
     * ```
     *
     * @param transaction - The current transaction
     * @param transitivity - <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    getSubtypes(transaction: TypeDBTransaction, transitivity: Transitivity): Stream<Type>;
}
