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

type Stringable = { toString: () => string; };

export abstract class ErrorMessage {
    private static knownErrors = new Map<string, Map<number, ErrorMessage>>();
    private static maxCodeNumber = 0;
    private static maxCodeDigits: number;
    private readonly _codePrefix: string;
    private readonly _codeNumber: number;
    private readonly _messagePrefix: string;
    private readonly _messageBody: (args?: Stringable[]) => string;
    private _code: string;

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
        constructor(code: number, message: (args?: Stringable[]) => string) {
            super("CLI", code, "Client Error", message)
        }
    }

    export namespace Client {
        export const RPC_METHOD_UNAVAILABLE = new Client(1, (args: Stringable[]) => `The server does not support this method. Please ensure that the TypeDB Client and TypeDB Server versions are compatible:\n'${args[0]}'.`);
        export const CLIENT_NOT_OPEN = new Client(2, (args: Stringable[]) => `The client is not open.`);
        export const SESSION_ID_EXISTS = new Client(3, (args: Stringable[]) => `The newly opened session id '${args[0]}' already exists`);
        export const SESSION_CLOSED = new Client(4, () => `Session is closed.`);
        export const TRANSACTION_CLOSED = new Client(5, () => `The transaction has been closed and no further operation is allowed.`);
        export const TRANSACTION_CLOSED_WITH_ERRORS = new Client(6, (args: Stringable[]) => `The transaction has been closed with error(s): \n${args[0]}.`)
        export const UNABLE_TO_CONNECT = new Client(7, () => `Unable to connect to TypeDB server.`);
        export const NEGATIVE_VALUE_NOT_ALLOWED = new Client(8, (args: Stringable[]) => `Value cannot be less than 1, was: '${args[0]}'.`);
        export const MISSING_DB_NAME = new Client(9, () => `Database name cannot be null.`);
        export const DATABASE_DOES_NOT_EXIST = new Client(10, (args: Stringable[]) => `The database '${args[0]}' does not exist.`);
        export const UNKNOWN_STREAM_STATE = new Client(11, (args: Stringable[]) => `RPC transaction stream response '${args[0]}' is unknown.`);
        export const MISSING_RESPONSE = new Client(12, (args: Stringable[]) => `The required field 'res' of type '${args[0]}' was not set.`);
        export const UNKNOWN_REQUEST_ID = new Client(13, (args: Stringable[]) => `Received a response with unknown request id '${args[0]}'.`);
        export const CLUSTER_NO_PRIMARY_REPLICA_YET = new Client(14, (args: Stringable[]) => `No replica has been marked as the primary replica for latest known term '${args[0]}'.`);
        export const CLUSTER_UNABLE_TO_CONNECT = new Client(15, (args: Stringable[]) => `Unable to connect to TypeDB Cluster. Attempted connecting to the cluster members, but none are available: '${args[1]}'.`);
        export const CLUSTER_REPLICA_NOT_PRIMARY = new Client(16, () => `The replica is not the primary replica.`);
        export const CLUSTER_ALL_NODES_FAILED = new Client(17, (args: Stringable[]) => `Attempted connecting to all cluster members, but the following errors occurred: \n'${args[0]}'`);
        export const CLUSTER_USER_DOES_NOT_EXIST = new Client(18, (args: Stringable[]) => `The user '${args[0]}' does not exist.`);
        export const CLUSTER_TOKEN_CREDENTIAL_INVALID = new Client(19, (args: Stringable[]) => `Invalid token credential.`);
        export const CLUSTER_INVALID_ROOT_CA_PATH = new Client(20, (args: Stringable[]) => `The provided Root CA path '${args[0]}' does not exist`);
        export const UNRECOGNISED_SESSION_TYPE = new Client(21, (args: Stringable[]) => `Session type '${args[1]}' was not recognised.`);
    }

    export class Concept extends ErrorMessage {
        constructor(code: number, message: (args: Stringable[]) => string) {
            super("CON", code, "Concept Error", message)
        }
    }

    export namespace Concept {
        export const INVALID_CONCEPT_CASTING = new Concept(1, (args: Stringable[]) => `Invalid concept conversion from '${args[0]}' to '${args[1]}'.`);
        export const MISSING_TRANSACTION = new Concept(2, () => `Transaction cannot be null.`);
        export const MISSING_IID = new Concept(3, () => `IID cannot be null or empty.`);
        export const MISSING_LABEL = new Concept(4, () => `Label cannot be null or empty.`);
        export const BAD_ENCODING = new Concept(5, (args: Stringable[]) => `The encoding '${args[0]}' was not recognised.`);
        export const BAD_VALUE_TYPE = new Concept(6, (args: Stringable[]) => `The value type '${args[0]}' was not recognised.`);
        export const BAD_ANNOTATION = new Concept(7, (args: Stringable[]) => `The annotation '${args[0]}' was not recognised.`);
        export const BAD_ATTRIBUTE_VALUE = new Concept(8, (args: Stringable[]) => `The attribute value '${args[0]}' was not recognised.`);
        export const VALUE_HAS_NO_REMOTE = new Concept(9, (args: Stringable[]) => `A 'value' has no remote concept.`);
    }

    export class Query extends ErrorMessage {
        constructor(code: number, message: (args: Stringable[]) => string) {
            super("QRY", code, "Query Error", message)
        }
    }

    export namespace Query {
        export const VARIABLE_DOES_NOT_EXIST = new Query(1, (args: Stringable[]) => `The variable '${args[0]}' does not exist.`);
        export const NONEXISTENT_EXPLAINABLE_CONCEPT = new Query(2, (args: Stringable[]) => `The concept identified by '${args[0]}' is not explainable.`);
        export const NONEXISTENT_EXPLAINABLE_OWNERSHIP = new Query(3, (args: Stringable[]) => `The ownership by owner '${args[0]}'of attribute '${args[1]}' is not explainable.`);
        export const BAD_ANSWER_TYPE = new Query(4, (args: Stringable[]) => `The answer type '${args[0]}' was not recognised.`);
        export const MISSING_ANSWER = new Query(5, (args: Stringable[]) => `The required field 'answer' of type '${args[0]}' was not set.`);
    }

    export class Internal extends ErrorMessage {
        constructor(code: number, message: (args: Stringable[]) => string) {
            super("INT", code, "Internal Error", message)
        }
    }

    export namespace Internal {
        export const ILLEGAL_CAST = new Internal(1, (args: Stringable[]) => `Illegal casting operation from '${args[0]}' to '${args[1]}'.`);
        export const ILLEGAL_ARGUMENT = new Internal(2, (args: Stringable[]) => `Illegal argument provided: '${args[0]}'`);
        export const ILLEGAL_STATE = new Internal(3, () => `Illegal state.`);
    }
}
