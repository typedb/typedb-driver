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

package com.vaticle.typedb.client.common.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;

public class TypeDBClientException extends RuntimeException {

    // TODO: propagate exception from the server side in a less-brittle way
    private static final String CLUSTER_REPLICA_NOT_PRIMARY_ERROR_CODE = "[RPL01]";
    private static final String CLUSTER_TOKEN_CREDENTIAL_INVALID_ERROR_CODE = "[CLS08]";

    @Nullable
    private final ErrorMessage errorMessage;

    public TypeDBClientException(ErrorMessage error, Object... parameters) {
        super(error.message(parameters));
        assert !getMessage().contains("%s");
        this.errorMessage = error;
    }

    public TypeDBClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = null;
    }

    public static TypeDBClientException of(StatusRuntimeException statusRuntimeException) {
        if (isRstStream(statusRuntimeException)) {
            return new TypeDBClientException(ErrorMessage.Client.UNABLE_TO_CONNECT);
        } else if (isReplicaNotPrimary(statusRuntimeException)) {
            return new TypeDBClientException(ErrorMessage.Client.CLUSTER_REPLICA_NOT_PRIMARY);
        } else if (isTokenCredentialInvalid(statusRuntimeException)) {
            return new TypeDBClientException(ErrorMessage.Client.CLUSTER_TOKEN_CREDENTIAL_INVALID);
        }

        return new TypeDBClientException(statusRuntimeException.getStatus().getDescription(), statusRuntimeException);
    }

    private static boolean isRstStream(StatusRuntimeException statusRuntimeException) {
        // "Received Rst Stream" occurs if the server is in the process of shutting down.
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNAVAILABLE ||
                statusRuntimeException.getStatus().getCode() == Status.Code.UNKNOWN ||
                statusRuntimeException.getMessage().contains("Received Rst Stream");
    }

    private static boolean isReplicaNotPrimary(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.INTERNAL &&
                statusRuntimeException.getStatus().getDescription() != null &&
                statusRuntimeException.getStatus().getDescription().contains(CLUSTER_REPLICA_NOT_PRIMARY_ERROR_CODE);
    }

    private static boolean isTokenCredentialInvalid(StatusRuntimeException statusRuntimeException) {
        return statusRuntimeException.getStatus().getCode() == Status.Code.UNAUTHENTICATED &&
                statusRuntimeException.getStatus().getDescription() != null &&
                statusRuntimeException.getStatus().getDescription().contains(CLUSTER_TOKEN_CREDENTIAL_INVALID_ERROR_CODE);
    }

    public String getName() {
        return this.getClass().getName();
    }

    @Nullable
    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
