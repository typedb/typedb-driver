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

import {Value} from "../../api/concept/value/Value";
import {ConceptImpl} from "../ConceptImpl";
import {Concept} from "../../api/concept/Concept";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {Value as ValueProto} from "typedb-protocol/proto/concept";
import ValueType = Concept.ValueType;
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export class ValueImpl extends ConceptImpl implements Value {
    private readonly _valueType: ValueType;
    private readonly _value: boolean | string | number | Date;

    constructor(type: ValueType, value: boolean | string | number | Date) {
        super();
        this._valueType = type;
        this._value = value;
    }

    protected get className(): string {
        return "Value";
    }

    get valueType(): ValueType {
        return this._valueType;
    }

    get value(): boolean | string | number | Date {
        return this._value;
    }

    isValue(): boolean {
        return true
    }

    asValue(): Value {
        return this;
    }

    equals(concept: Concept): boolean {
        if (!concept.isValue()) return false;
        else {
            return this.valueType == concept.asValue().valueType && this.value == concept.asValue().value;
        }
    }

    toJSONRecord(): Record<string, boolean | string | number> {
        let value;
        if (this.value instanceof Date) value = this.value.toISOString().slice(0, -1);
        else value = this.value;
        return {
            value_type: this.valueType.name(),
            value: value
        };
    }

    isBoolean(): boolean {
        return this.valueType == ValueType.BOOLEAN;
    }

    isLong(): boolean {
        return this.valueType == ValueType.LONG;
    }

    isDouble(): boolean {
        return this.valueType == ValueType.DOUBLE;
    }

    isString(): boolean {
        return this.valueType == ValueType.STRING;
    }

    isDateTime(): boolean {
        return this.valueType == ValueType.DATETIME;
    }

    asBoolean(): boolean {
        if (!this.isBoolean()) throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Boolean"));
        return this.value as boolean;
    }

    asLong(): number {
        if (!this.isLong()) throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Long"));
        return this.value as number;
    }

    asDouble(): number {
        if (!this.isDouble()) throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Double"));
        return this.value as number;
    }

    asString(): string {
        if (!this.isString()) throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.String"));
        return this.value as string;
    }

    asDateTime(): Date {
        if (!this.isDateTime()) throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.DateTime"));
        return this.value as Date;
    }
}

export namespace ValueImpl {
    export function ofValueProto(valueProto: ValueProto): Value {
        if (!valueProto) return null;
        if (valueProto.has_boolean) return new ValueImpl(ValueType.BOOLEAN, valueProto.boolean);
        else if (valueProto.has_long) return new ValueImpl(ValueType.LONG, valueProto.long);
        else if (valueProto.has_double) return new ValueImpl(ValueType.DOUBLE, valueProto.double);
        else if (valueProto.has_string) return new ValueImpl(ValueType.STRING, valueProto.string);
        else if (valueProto.has_date_time) return new ValueImpl(ValueType.DATETIME, new Date(valueProto.date_time));
        else throw new TypeDBClientError(BAD_VALUE_TYPE.message(valueProto));
    }
}
