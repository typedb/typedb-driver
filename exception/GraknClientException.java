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

package grakn.client.exception;

import grakn.core.common.exception.ErrorMessage;
import grakn.core.common.exception.GraknException;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;

public class GraknClientException extends GraknException {

    private String statusCode;

    protected GraknClientException(String error) {
        super(error);
    }

    protected GraknClientException(String error, RuntimeException e) {
        super(error, e);
    }

    private GraknClientException(String error, StatusRuntimeException e) {
        super(error, e);
        this.statusCode = e.getStatus().getCode().name();
    }

    public static GraknClientException create(String error) {
        return new GraknClientException(error);
    }

    public static GraknClientException create(String error, StatusRuntimeException e) {
        return new GraknClientException(error, e);
    }

    public static GraknClientException invalidKeyspaceName(String keyspace) {
        return create(ErrorMessage.INVALID_KEYSPACE_NAME.getMessage(keyspace));
    }

    public static GraknClientException connectionClosed() {
        return create(ErrorMessage.CONNECTION_CLOSED.getMessage());
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Nullable
    public String getStatusCode() { return this.statusCode; }
}
