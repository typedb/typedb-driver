/*
 * Copyright (C) 2021 Vaticle
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


import { Numeric as NumericProto } from "typedb-protocol/common/answer_pb";
import { Numeric } from "../../api/answer/Numeric";
import { ErrorMessage } from "../../common/errors/ErrorMessage";
import { TypeDBClientError } from "../../common/errors/TypeDBClientError";
import ILLEGAL_CAST = ErrorMessage.Internal.ILLEGAL_CAST;
import BAD_ANSWER_TYPE = ErrorMessage.Query.BAD_ANSWER_TYPE;

export class NumericImpl implements Numeric {

    private readonly _value?: number;

    constructor(value?: number) {
        this._value = value;
    }

    asNumber(): number {
        if (this.isNumber()) return this._value;
        else throw new TypeDBClientError(ILLEGAL_CAST.message("NaN", "number"));
    }

    isNaN(): boolean {
        return !this.isNumber();
    }

    isNumber(): boolean {
        return this._value != null;
    }

    public toString() {
        return this.isNumber() ? `${this.asNumber()}` : 'NaN';
    }
}

export namespace NumericImpl {

    export function of(numericProto: NumericProto) {
        switch (numericProto.getValueCase()) {
            case NumericProto.ValueCase.NAN:
                return new NumericImpl(null);
            case NumericProto.ValueCase.DOUBLE_VALUE:
                return new NumericImpl(numericProto.getDoubleValue());
            case NumericProto.ValueCase.LONG_VALUE:
                return new NumericImpl(numericProto.getLongValue());
            case NumericProto.ValueCase.VALUE_NOT_SET:
            default:
                throw new TypeDBClientError(BAD_ANSWER_TYPE.message(numericProto.getValueCase()));
        }
    }
}
