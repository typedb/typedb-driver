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

import {ConceptMap} from "../answer/ConceptMap";
import {Rule} from "./Rule";

/**
 * An explanation of which rule was used for inferring the explained concept,
 * the condition of the rule, the conclusion of the rule,
 * and the mapping of variables between the query and the rule’s conclusion.
 */
export interface Explanation {
    /** Retrieves the Rule for this Explanation. */
    readonly rule: Rule;

    /** The Condition for this Explanation. */
    readonly condition: ConceptMap;

    /** The Conclusion for this Explanation. */
    readonly conclusion: ConceptMap;

    /** Retrieves the query variables for this <code>Explanation</code>. */
    readonly variableMapping: Map<string, Set<string>>;
}
