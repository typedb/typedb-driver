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
import { ErrorMessage } from "../../dependencies_internal"
import { ServiceError } from "@grpc/grpc-js";
import { Status } from "@grpc/grpc-js/build/src/constants";
import UNABLE_TO_CONNECT = ErrorMessage.Client.UNABLE_TO_CONNECT;
import CLUSTER_REPLICA_NOT_PRIMARY = ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY;

function isReplicaNotPrimaryError(e: ServiceError): boolean {
    return e instanceof GraknClientError && e.message.includes("[RPL01]");
}

export class GraknClientError extends Error {
    private readonly _errorMessage: ErrorMessage;

    constructor(error: string | Error | ServiceError | ErrorMessage) {
        if (typeof error === "string") super(error);
        else super(error.toString());

        this.name = "GraknClientError"; // Required to correctly report error type in default throw

        if (error instanceof ErrorMessage) {
            this._errorMessage = error;
        } else if (error instanceof Error && "code" in error) {
            if ([Status.UNAVAILABLE, Status.UNKNOWN, Status.CANCELLED].includes(error.code) || error.message.includes("Received RST_STREAM")) {
                this._errorMessage = UNABLE_TO_CONNECT;
            } else if (isReplicaNotPrimaryError(error)) {
                this._errorMessage = CLUSTER_REPLICA_NOT_PRIMARY;
            }
        }
    }

    errorMessage(): ErrorMessage {
        return this._errorMessage;
    }
}
