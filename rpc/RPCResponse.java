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

package grakn.client.rpc;

import grakn.client.common.exception.GraknClientException;
import grakn.protocol.TransactionProto;
import io.grpc.StatusRuntimeException;

import static grakn.common.util.Objects.className;

abstract class RPCResponse {

    abstract TransactionProto.Transaction.Res read();

    static class Ok extends RPCResponse {
        private final TransactionProto.Transaction.Res response;

        Ok(final TransactionProto.Transaction.Res response) {
            this.response = response;
        }

        @Override
        TransactionProto.Transaction.Res read() {
            return response;
        }

        @Override
        public String toString() {
            return className(getClass()) + "{" + response + "}";
        }
    }

    static class Error extends RPCResponse {
        private final StatusRuntimeException error;

        Error(final StatusRuntimeException error) {
            this.error = error;
        }

        @Override
        TransactionProto.Transaction.Res read() {
            // TODO: parse different GRPC errors into specific GraknClientException
            throw new GraknClientException(error);
        }

        @Override
        public String toString() {
            return className(getClass()) + "{" + error + "}";
        }
    }

    static class Completed extends RPCResponse {

        @Override
        TransactionProto.Transaction.Res read() {
            // TODO: I'm not sure this is right.
            return null;
        }
    }
}
