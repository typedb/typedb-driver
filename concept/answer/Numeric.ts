/*
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

import * as answer_pb from "grakn-protocol/protobuf/answer_pb";
import {
    ErrorMessage,
    GraknClientError,
} from "../../dependencies_internal";

export class Numeric {
    private readonly _numberValue?: number;

    private constructor(numberValue?: number) {
        this._numberValue = numberValue;
    }

    public static of(answer: answer_pb.Numeric): Numeric {
        switch (answer.getValueCase()) {
            case answer_pb.Numeric.ValueCase.LONG_VALUE:
                return Numeric.ofNumber(answer.getLongValue());
            case answer_pb.Numeric.ValueCase.DOUBLE_VALUE:
                return Numeric.ofNumber(answer.getDoubleValue());
            case answer_pb.Numeric.ValueCase.NAN:
                return Numeric.ofNaN();
            default:
                throw new GraknClientError(ErrorMessage.Query.BAD_ANSWER_TYPE.message(answer.getValueCase()));
        }
    }

    public isNumber(): boolean {
        return this._numberValue != null;
    }

    public isNaN(): boolean {
        return !this.isNumber();
    }

    public asNumber(): number {
        if (this.isNumber()) return this._numberValue;
        else throw new GraknClientError(ErrorMessage.Internal.ILLEGAL_CAST.message("NaN", "number"));
    }

    private static ofNumber(value: number): Numeric {
        return new Numeric(value);
    }

    private static ofNaN(): Numeric {
        return new Numeric(null);
    }

    public toString = () : string => {
        return this.isNumber() ? `${this.asNumber()}` : 'NaN';
    }
}
