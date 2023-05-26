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
import {TypeDBTransaction} from "../../api/connection/TypeDBTransaction";
import {TypeDBClientError} from "../../common/errors/TypeDBClientError";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {Value as ValueProto, ValueType as ValueTypeProto} from "typedb-protocol/common/concept_pb";
import ValueType = Concept.ValueType;
import VALUE_HAS_NO_REMOTE = ErrorMessage.Concept.VALUE_HAS_NO_REMOTE;
import INVALID_CONCEPT_CASTING = ErrorMessage.Concept.INVALID_CONCEPT_CASTING;

import BAD_VALUE_TYPE = ErrorMessage.Concept.BAD_VALUE_TYPE;

export abstract class ValueImpl extends ConceptImpl implements Value {

    private readonly _valueType: ValueType;

    protected constructor(type: ValueType) {
        super();
        this._valueType = type;
    }

    get valueType(): ValueType {
        return this._valueType;
    }

    abstract get value(): boolean | string | number | Date;

    asRemote(transaction: TypeDBTransaction): Concept.Remote {
        throw new TypeDBClientError(VALUE_HAS_NO_REMOTE);
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
        return false;
    }

    isDateTime(): boolean {
        return false;
    }

    isDouble(): boolean {
        return false;
    }

    isLong(): boolean {
        return false;
    }

    isString(): boolean {
        return false;
    }

    asBoolean(): Value.Boolean {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Boolean"));
    }

    asLong(): Value.Long {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Long"));
    }

    asDouble(): Value.Double {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.Double"));
    }

    asString(): Value.String {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.String"));
    }

    asDateTime(): Value.DateTime {
        throw new TypeDBClientError(INVALID_CONCEPT_CASTING.message(this.className, "Value.DateTime"));
    }
}

export namespace ValueImpl {

    export function of(valueProto: ValueProto): Value {
        if (!valueProto) return null;
        switch (valueProto.getValueType()) {
            case ValueTypeProto.BOOLEAN:
                return new ValueImpl.Boolean(valueProto.getValue().getBoolean());
            case ValueTypeProto.LONG:
                return new ValueImpl.Long(valueProto.getValue().getLong());
            case ValueTypeProto.DOUBLE:
                return new ValueImpl.Double(valueProto.getValue().getDouble());
            case ValueTypeProto.STRING:
                return new ValueImpl.String(valueProto.getValue().getString());
            case ValueTypeProto.DATETIME:
                return new ValueImpl.DateTime(new Date(valueProto.getValue().getDateTime()));
            default:
                throw new TypeDBClientError(BAD_VALUE_TYPE.message(valueProto.getValueType()));
        }
    }

    export class Boolean extends ValueImpl implements Value.Boolean {
        readonly _value: boolean;

        constructor(value: boolean) {
            super(ValueType.BOOLEAN);
            this._value = value;
        }

        get value(): boolean {
            return this._value;
        }

        isBoolean(): boolean {
            return true;
        }

        asBoolean(): Value.Boolean {
            return this;
        }

        protected get className(): string {
            return "Value.Boolean";
        }
    }

    export class Long extends ValueImpl implements Value.Long {
        readonly _value: number;

        constructor(value: number) {
            super(ValueType.LONG);
            this._value = value;
        }

        get value(): number {
            return this._value;
        }

        isLong(): boolean {
            return true;
        }

        asLong(): Value.Long {
            return this;
        }

        protected get className(): string {
            return "Value.Long";
        }
    }

    export class Double extends ValueImpl implements Value.Double {
        readonly _value: number;

        constructor(value: number) {
            super(ValueType.DOUBLE);
            this._value = value;
        }

        get value(): number {
            return this._value;
        }

        isDouble(): boolean {
            return true;
        }

        asDouble(): Value.Double {
            return this;
        }

        protected get className(): string {
            return "Value.Double";
        }
    }

    export class String extends ValueImpl implements Value.String {
        readonly _value: string;

        constructor(value: string) {
            super(ValueType.STRING);
            this._value = value;
        }

        get value(): string {
            return this._value;
        }

        isString(): boolean {
            return true;
        }

        asString(): Value.String {
            return this;
        }

        protected get className(): string {
            return "Value.String";
        }
    }

    export class DateTime extends ValueImpl implements Value.DateTime {
        readonly _value: Date;

        constructor(value: Date) {
            super(ValueType.DATETIME);
            this._value = value;
        }

        get value(): Date {
            return this._value;
        }

        isDateTime(): boolean {
            return true;
        }

        asDateTime(): Value.DateTime {
            return this;
        }

        protected get className(): string {
            return "Value.DateTime";
        }
    }

}