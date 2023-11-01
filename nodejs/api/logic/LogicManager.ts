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

import {Stream} from "../../common/util/Stream";
import {Rule} from "./Rule";

/** Provides methods for manipulating rules in the database. */
export interface LogicManager {
    /**
     * Retrieves the Rule that has the given label.
     *
     * ### Examples
     *
     * ```ts
     * transaction.logic.getRule(label)
     * ```
     *
     * @param label - The label of the Rule to create or retrieve
     */
    getRule(label: string): Promise<Rule | undefined>;

    /**
     * Retrieves all rules.
     *
     * ### Examples
     *
     * ```ts
     * transaction.logic.getRules()
     * ```
     */
    getRules(): Stream<Rule>;

    /**
     * Creates a new Rule if none exists with the given label, or replaces the existing one.
     *
     * ### Examples
     *
     * ```ts
     * transaction.logic.putRule(label, when, then)
     * ```
     *
     * @param label - The label of the Rule to create or replace
     * @param when - The when body of the rule to create
     * @param then - The then body of the rule to create
     */
    putRule(label: string, when: string, then: string): Promise<Rule>;
}
