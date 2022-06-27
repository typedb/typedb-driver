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

import { NumericGroup as NumericGroupProto } from "typedb-protocol/common/answer_pb";
import { Numeric } from "../../api/answer/Numeric";
import { NumericGroup } from "../../api/answer/NumericGroup";
import { Concept } from "../../api/concept/Concept";
import { ThingImpl, TypeImpl } from "../../dependencies_internal";
import { NumericImpl } from "./NumericImpl";

export class NumericGroupImpl implements NumericGroup {

    private readonly _owner: Concept;
    private readonly _numeric: Numeric;

    constructor(owner: Concept, numeric: Numeric) {
        this._owner = owner;
        this._numeric = numeric;
    }

    get owner(): Concept {
        return this._owner;
    }

    get numeric(): Numeric {
        return this._numeric;
    }
}

export namespace NumericGroupImpl {

    export function of(numericGroupProto: NumericGroupProto) {
        let concept: Concept;
        if (numericGroupProto.getOwner().hasThing()) concept = ThingImpl.of(numericGroupProto.getOwner().getThing());
        else concept = TypeImpl.of(numericGroupProto.getOwner().getType());
        return new NumericGroupImpl(concept, NumericImpl.of(numericGroupProto.getNumber()))
    }
}
