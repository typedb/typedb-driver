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

import {ValueGroup as ValueGroupProto} from "typedb-protocol/proto/answer";
import {ValueGroup} from "../../api/answer/ValueGroup";
import {Concept} from "../../api/concept/Concept";
import {ResponseReader} from "../../common/rpc/ResponseReader";
import {Value} from "../../api/concept/value/Value";
import {ValueImpl} from "../value/ValueImpl";

export class ValueGroupImpl implements ValueGroup {
    private readonly _owner: Concept;
    private readonly _value: Value | null;

    constructor(owner: Concept, value: Value) {
        this._owner = owner;
        this._value = value;
    }

    get owner(): Concept {
        return this._owner;
    }

    get value(): Value | null {
        return this._value;
    }
}

export namespace ValueGroupImpl {
    export function of(valueGroupProto: ValueGroupProto) {
        if (valueGroupProto.value) {
            return new ValueGroupImpl(ResponseReader.Concept.of(valueGroupProto.owner), ValueImpl.ofValueProto(valueGroupProto.value));
        } else {
            return new ValueGroupImpl(ResponseReader.Concept.of(valueGroupProto.owner), null);
        }
    }
}
