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


import {Numeric as NumericProto} from "typedb-protocol/proto/answer";
import {Numeric} from "../../api/answer/Numeric";
import {ErrorMessage} from "../../common/errors/ErrorMessage";
import {TypeDBDriverError} from "../../common/errors/TypeDBDriverError";
import ILLEGAL_CAST = ErrorMessage.Internal.ILLEGAL_CAST;
import BAD_ANSWER_TYPE = ErrorMessage.Query.BAD_ANSWER_TYPE;

export class NumericImpl implements Numeric {

    private readonly _value?: number;

    constructor(value?: number) {
        this._value = value;
    }

    asNumber(): number {
        if (this.isNumber()) return this._value;
        else throw new TypeDBDriverError(ILLEGAL_CAST.message("NaN", "number"));
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
        if (numericProto.has_nan) return new NumericImpl(null);
        else if (numericProto.has_double_value) return new NumericImpl(numericProto.double_value);
        else if (numericProto.has_long_value) return new NumericImpl(numericProto.long_value);
        else throw new TypeDBDriverError(BAD_ANSWER_TYPE.message(numericProto));
    }
}
