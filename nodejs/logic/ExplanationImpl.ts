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

import {Explanation as ExplanationProto, ExplanationVarList} from "typedb-protocol/proto/logic";
import {ConceptMap} from "../api/answer/ConceptMap";
import {Explanation} from "../api/logic/Explanation";
import {Rule} from "../api/logic/Rule";
import {ConceptMapImpl} from "../concept/answer/ConceptMapImpl";
import {RuleImpl} from "./RuleImpl";

export class ExplanationImpl implements Explanation {
    private readonly _rule: Rule;
    private readonly _condition: ConceptMap;
    private readonly _conclusion: ConceptMap;
    private readonly _variableMapping: Map<string, Set<string>>;

    constructor(rule: Rule, condition: ConceptMap, conclusion: ConceptMap, variableMapping: Map<string, Set<string>>) {
        this._rule = rule;
        this._condition = condition;
        this._conclusion = conclusion;
        this._variableMapping = variableMapping;
    }

    get rule(): Rule {
        return this._rule;
    }

    get conclusion(): ConceptMap {
        return this._conclusion;
    }

    get condition(): ConceptMap {
        return this._condition;
    }

    get variableMapping(): Map<string, Set<string>> {
        return this._variableMapping;
    }
}

export namespace ExplanationImpl {
    export function of(proto: ExplanationProto) {
        const varMapping = new Map<string, Set<string>>();
        proto.var_mapping.forEach((vars: ExplanationVarList, key: string) => varMapping.set(key, new Set(vars.vars)));
        return new ExplanationImpl(
            RuleImpl.of(proto.rule),
            ConceptMapImpl.of(proto.condition),
            ConceptMapImpl.of(proto.conclusion),
            varMapping
        );
    }
}
