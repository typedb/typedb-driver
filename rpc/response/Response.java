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

package grakn.client.rpc.response;

import grakn.client.common.exception.GraknClientException;
import grakn.protocol.TransactionProto;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;
import java.util.Objects;

import static grakn.common.util.Objects.className;

public class Response {

    private final TransactionProto.Transaction.Res response;
    private final StatusRuntimeException error;

    private Response(@Nullable TransactionProto.Transaction.Res response, @Nullable StatusRuntimeException error) {
        if (!(response == null || error == null)) {
            throw new GraknClientException(new IllegalArgumentException("One of Transaction.Res or StatusRuntimeException must be null"));
        }
        this.response = response;
        this.error = error;
    }

    static Response completed() {
        return new Response(null, null);
    }

    static Response error(StatusRuntimeException error) {
        return new Response(null, error);
    }

    static Response ok(TransactionProto.Transaction.Res response) {
        return new Response(response, null);
    }

    @Nullable
    public TransactionProto.Transaction.Res response() {
        return response;
    }

    public final Type type() {
        if (response != null) {
            return Type.OK;
        } else if (error != null) {
            return Type.ERROR;
        } else {
            return Type.COMPLETED;
        }
    }

    public final TransactionProto.Transaction.Res ok() {
        if (response != null) {
            return response;
        } else if (error != null) {
            // TODO: parse different GRPC errors into specific GraknClientException
            throw new GraknClientException(error);
        } else {
            throw new GraknClientException("Transaction interrupted, all running queries have been stopped.");
        }
    }

    @Override
    public String toString() {
        return className(getClass()) + "{response=" + response + ", error=" + error + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Response) {
            final Response that = (Response) o;
            return Objects.equals(this.response, that.response)
                    && Objects.equals(this.error, that.error);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, error);
    }

    public enum Type {
        OK, ERROR, COMPLETED
    }
}
