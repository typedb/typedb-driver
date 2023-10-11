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

import {ServiceError} from "@grpc/grpc-js";
import {Status} from "@grpc/grpc-js/build/src/constants";
import {ErrorMessage} from "./ErrorMessage";
import ENTERPRISE_REPLICA_NOT_PRIMARY = ErrorMessage.Driver.ENTPERPRISE_REPLICA_NOT_PRIMARY;
import ENTERPRISE_TOKEN_CREDENTIAL_INVALID = ErrorMessage.Driver.ENTPERPRISE_TOKEN_CREDENTIAL_INVALID;
import UNABLE_TO_CONNECT = ErrorMessage.Driver.UNABLE_TO_CONNECT;
import RPC_METHOD_UNAVAILABLE = ErrorMessage.Driver.RPC_METHOD_UNAVAILABLE;

function isReplicaNotPrimaryError(e: ServiceError): boolean {
    return e.message.includes("[RPL01]");
}

function isTokenCredentialInvalidError(e: ServiceError): boolean {
    return e.message.includes("[CLS08]");
}

function isServiceError(e: Error | ServiceError): e is ServiceError {
    return "code" in e;
}

/** Errors encountered when interacting with TypeDB */
export class TypeDBDriverError extends Error {
    private readonly _messageTemplate: ErrorMessage;

    constructor(error: string | Error | ServiceError | ErrorMessage) {
        if (typeof error === "string") super(error);
        else if (error instanceof ErrorMessage) {
            super(error.toString());
            this._messageTemplate = error;
        }
        // TODO: clean this up once we have our own error protocol
        else if (isServiceError(error)) {
            if (error.code == Status.UNIMPLEMENTED) {
                super(RPC_METHOD_UNAVAILABLE.message(error.details))
                this._messageTemplate = RPC_METHOD_UNAVAILABLE;
            } else if ([Status.UNAVAILABLE, Status.UNKNOWN, Status.CANCELLED].includes(error.code) || error.message.includes("Received RST_STREAM")) {
                super(UNABLE_TO_CONNECT.message());
                this._messageTemplate = UNABLE_TO_CONNECT;
            } else if (isReplicaNotPrimaryError(error)) {
                super(ENTERPRISE_REPLICA_NOT_PRIMARY.message());
                this._messageTemplate = ENTERPRISE_REPLICA_NOT_PRIMARY;
            } else if (isTokenCredentialInvalidError(error)) {
                super(ENTERPRISE_TOKEN_CREDENTIAL_INVALID.message());
                this._messageTemplate = ENTERPRISE_TOKEN_CREDENTIAL_INVALID;
            } else if (error.code === Status.INTERNAL) super(error.details)
            else super(error.toString());
        } else super(error.toString());

        this.name = "TypeDBDriverError"; // Required to correctly report error type in default throw
    }

    /** Returns the message template for this error.*/
    get messageTemplate(): ErrorMessage {
        return this._messageTemplate;
    }
}
