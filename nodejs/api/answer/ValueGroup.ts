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

import {Concept} from "../concept/Concept";
import {Value} from "../concept/value/Value";

/**
 * Contains an element of the group aggregate query result.
 */
export interface ValueGroup {
    /**
     * Retrieves the concept that is the group owner.
     *
     * ### Examples
     *
     * ```ts
     * valueGroup.owner
     * ```
     */
    readonly owner: Concept;

    /**
     * Retrieves the <code>Value</code> answer of the group, if there is one.
     *
     * ### Examples
     *
     * ```ts
     * valueGroup.value
     * ```
     */
    readonly value: Value | undefined;
}
