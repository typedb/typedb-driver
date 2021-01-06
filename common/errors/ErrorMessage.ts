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

import { Stringable } from "../../dependencies_internal";

export abstract class ErrorMessage {
    private readonly _codePrefix: string;
    private readonly _codeNumber: number;
    private readonly _messagePrefix: string;
    private readonly _messageBody: (args?: Stringable[]) => string;
    private _code: string;

    private static knownErrors = new Map<string, Map<number, ErrorMessage>>();
    private static maxCodeNumber = 0;
    private static maxCodeDigits: number;

    protected constructor(codePrefix: string, codeNumber: number, messagePrefix: string, messageBody: (args: Stringable[]) => string) {
        this._codePrefix = codePrefix;
        this._codeNumber = codeNumber;
        this._messagePrefix = messagePrefix;
        this._messageBody = messageBody;

        if (!ErrorMessage.knownErrors.has(codePrefix)) {
            ErrorMessage.knownErrors.set(codePrefix, new Map<number, ErrorMessage>());
        }
        ErrorMessage.knownErrors.get(codePrefix).set(codeNumber, this);
        ErrorMessage.maxCodeNumber = Math.max(ErrorMessage.maxCodeNumber, codeNumber);
        ErrorMessage.maxCodeDigits = String(ErrorMessage.maxCodeNumber).length;
    }

    public code(): string {
        if (this._code == null) {
            let zeros = "";
            for (let length = String(this._code).length; length < ErrorMessage.maxCodeDigits; length++) {
                zeros += "0";
            }
            this._code = `${this._codePrefix}${zeros}${this._codeNumber}`;
        }
        return this._code;
    }

    public message(...args: Stringable[]): string {
        return `[${this.code()}] ${this._messagePrefix}: ${this._messageBody(args)}`
    }

    toString(): string {
        return `[${this.code()}] ${this._messagePrefix}: ${this._messageBody([])}`;
    }
}

export namespace ErrorMessage {
    export class Client extends ErrorMessage {
        constructor(code: number, message: (args?: Stringable[]) => string) {super("CLI", code, "Illegal Client State", message)}
    }
    export namespace Client {
        export const TRANSACTION_CLOSED = new Client(1, () => `The transaction has been closed and no further operation is allowed.`);
        export const NONPOSITIVE_BATCH_SIZE = new Client(2, (args: Stringable[]) => `Batch size cannot be less than 1, was: '${args[0]}'.`);
        export const MISSING_DB_NAME = new Client(3, () => `Database name cannot be null.`);
        export const MISSING_RESPONSE = new Client(4, (args: Stringable[]) => `The required field 'res' of type '${args[0]}' was not set.`);
        export const UNKNOWN_REQUEST_ID = new Client(5, (args: Stringable[]) => `Received a response with unknown request id '${args[0]}'.`);
        export const UNRECOGNISED_SESSION_TYPE = new Client(6, (args: Stringable[]) => `Session type '${args[0]}' was not recognised.`);
    }

    export class Concept extends ErrorMessage {
        constructor(code: number, message: (args: Stringable[]) => string) {super("CON", code, "Concept Error", message)}
    }
    export namespace Concept {
        export const INVALID_CONCEPT_CASTING = new Concept(1, (args: Stringable[]) => `Invalid concept conversion from '${args[0]}' to '${args[1]}'.`);
        export const MISSING_TRANSACTION = new Concept(2, () => `Transaction cannot be null.`);
        export const MISSING_IID = new Concept(3, () => `IID cannot be null or empty.`);
        export const MISSING_LABEL = new Concept(4, () => `Label cannot be null or empty.`);
        export const BAD_ENCODING = new Concept(5, (args: Stringable[]) => `The encoding '${args[0]}' was not recognised.`);
        export const BAD_VALUE_TYPE = new Concept(6, (args: Stringable[]) => `The value type '${args[0]}' was not recognised.`);
        export const BAD_ATTRIBUTE_VALUE = new Concept(7, (args: Stringable[]) => `The attribute value '${args[0]}' was not recognised›.`);
    }

    export class Query extends ErrorMessage {
        constructor(code: number, message: (args: Stringable[]) => string) {super("QRY", code, "Query Error", message)}
    }
    export namespace Query {
        export const VARIABLE_DOES_NOT_EXIST = new Query(1, (args: Stringable[]) => `The variable '${args[0]}' does not exist.`);
        export const NO_EXPLANATION = new Query(2, () => `No explanation was found.`);
        export const BAD_ANSWER_TYPE = new Query(3, (args: Stringable[]) => `The answer '${args[0]}' was not recognised.`);
        export const MISSING_ANSWER = new Query(4, (args: Stringable[]) => `The required field 'answer' of type '${args[0]}' was not set.`);
        export const ILLEGAL_CAST = new Query(5, (args: Stringable[]) => `Illegal casting operation from '${args[0]}' to '${args[1]}'.`)
    }
}
