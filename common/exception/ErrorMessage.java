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

package com.vaticle.typedb.client.common.exception;

public abstract class ErrorMessage extends com.vaticle.typedb.common.exception.ErrorMessage {

    private ErrorMessage(String codePrefix, int codeNumber, String messagePrefix, String messageBody) {
        super(codePrefix, codeNumber, messagePrefix, messageBody);
    }

    public static class Client extends ErrorMessage {
        public static final Client RPC_METHOD_UNAVAILABLE =
                new Client(1, "The server does not support this method. Please ensure that the TypeDB Client and TypeDB Server versions are compatible:\n'%s'.");
        public static final Client CLIENT_CLOSED =
                new Client(2, "The client has been closed and no further operation is allowed.");
        public static final Client CLIENT_CONNECTION_NOT_VALIDATED =
                new Client(3, "The client connection has not been validated and cannot be used.");
        public static final Client SESSION_CLOSED =
                new Client(4, "The session has been closed and no further operation is allowed.");
        public static final Client TRANSACTION_CLOSED =
                new Client(5, "The transaction has been closed and no further operation is allowed.");
        public static final Client TRANSACTION_CLOSED_WITH_ERRORS =
                new Client(6, "The transaction has been closed with error(s): \n%s.");
        public static final Client UNABLE_TO_CONNECT =
                new Client(7, "Unable to connect to TypeDB server.");
        public static final Client NEGATIVE_VALUE_NOT_ALLOWED =
                new Client(8, "Value cannot be less than 1, was: '%d'.");
        public static final Client MISSING_DB_NAME =
                new Client(9, "Database name cannot be null.");
        public static final Client DB_DOES_NOT_EXIST =
                new Client(10, "The database '%s' does not exist.");
        public static final Client MISSING_RESPONSE =
                new Client(11, "Unexpected empty response for request ID '%s'.");
        public static final Client UNKNOWN_REQUEST_ID =
                new Client(12, "Received a response with unknown request id '%s':\n%s");
        public static final Client CLUSTER_NO_PRIMARY_REPLICA_YET =
                new Client(13, "No replica has been marked as the primary replica for latest known term '%d'.");
        public static final Client CLUSTER_UNABLE_TO_CONNECT =
                new Client(14, "Unable to connect to TypeDB Cluster. Attempted connecting to the cluster members, but none are available: '%s'.");
        public static final Client CLUSTER_REPLICA_NOT_PRIMARY =
                new Client(15, "The replica is not the primary replica.");
        public static final Client CLUSTER_ALL_NODES_FAILED =
                new Client(16, "Attempted connecting to all cluster members, but the following errors occurred: \n%s.");
        public static final Client CLUSTER_USER_DOES_NOT_EXIST =
                new Client(17, "The user '%s' does not exist.");
        public static final ErrorMessage CLUSTER_TOKEN_CREDENTIAL_INVALID =
                new Client(18, "Invalid token credential.");
        public static final ErrorMessage CLUSTER_PASSWORD_CREDENTIAL_EXPIRED =
                new Client(19, "Expired password credential.");
        public static final ErrorMessage CLUSTER_ENDPOINT_NOT_ENCRYPTED =
                new Client(20, "Unable to connect to TypeDB Cluster: attempting an encrypted connection to an unencrypted endpoint.");
        public static final ErrorMessage CLUSTER_SSL_HANDSHAKE_FAILED =
                new Client(21, "SSL handshake with TypeDB Cluster failed: %s.");
        public static final ErrorMessage CLUSTER_SSL_CERTIFICATE_NOT_VALIDATED =
                new Client(22, "SSL handshake with TypeDB Cluster failed: the server's identity could not be verified.");
        public static final ErrorMessage CLUSTER_SSL_CERTIFICATE_INVALID_FOR_HOSTNAME =
                new Client(23, "SSL handshake with TypeDB Cluster failed: the server's certificate is not valid for the provided hostname.");
        public static final ErrorMessage CLUSTER_CONNECTION_CLOSED_UKNOWN =
                new Client(24, "Network closed for unknown reason. This may be caused by a client-server version mismatch or incompatible encryption settings.");

        private static final String codePrefix = "CLI";
        private static final String messagePrefix = "Client Error";

        Client(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Concept extends ErrorMessage {
        public static final Concept INVALID_CONCEPT_CASTING =
                new Concept(1, "Invalid concept conversion from '%s' to '%s'.");
        public static final Concept MISSING_TRANSACTION =
                new Concept(2, "Transaction cannot be null.");
        public static final Concept MISSING_IID =
                new Concept(3, "IID cannot be null or empty.");
        public static final Concept MISSING_LABEL =
                new Concept(4, "Label cannot be null or empty.");
        public static final Concept BAD_ENCODING =
                new Concept(5, "The encoding '%s' was not recognised.");
        public static final Concept BAD_VALUE_TYPE =
                new Concept(6, "The value type '%s' was not recognised.");
        public static final Concept BAD_ATTRIBUTE_VALUE =
                new Concept(7, "The attribute value '%s' was not recognised.");
        public static final Concept NONEXISTENT_EXPLAINABLE_CONCEPT =
                new Concept(8, "The concept identified by '%s' is not explainable.");
        public static final Concept NONEXISTENT_EXPLAINABLE_OWNERSHIP =
                new Concept(9, "The ownership by owner '%s' of attribute '%s' is not explainable.");
        public static final Concept UNRECOGNISED_ANNOTATION =
                new Concept(10, "The annotation '%s' is not recognised");
        public static final Concept VALUE_HAS_NO_REMOTE =
                new Concept(11, "A 'value' has no remote concept.");

        private static final String codePrefix = "CON";
        private static final String messagePrefix = "Concept Error";

        Concept(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Query extends ErrorMessage {
        public static final Query VARIABLE_DOES_NOT_EXIST =
                new Query(1, "The variable '%s' does not exist.");
        public static final Query NO_EXPLANATION =
                new Query(2, "No explanation was found.");
        public static final Query BAD_ANSWER_TYPE =
                new Query(3, "The answer type '%s' was not recognised.");
        public static final Query MISSING_ANSWER =
                new Query(4, "The required field 'answer' of type '%s' was not set.");

        private static final String codePrefix = "QRY";
        private static final String messagePrefix = "Query Error";

        Query(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Internal extends ErrorMessage {
        public static final Internal UNEXPECTED_INTERRUPTION =
                new Internal(1, "Unexpected thread interruption!");
        public static final Internal ILLEGAL_STATE =
                new Internal(2, "Illegal state has been reached!");
        public static final Internal ILLEGAL_ARGUMENT =
                new Internal(3, "Illegal argument provided: '%s'");
        public static final Internal ILLEGAL_CAST =
                new Internal(4, "Illegal casting operation to '%s'.");
        public static final Internal ILLEGAL_ARGUMENT_COMBINATION =
                new Internal(5, "Illegal argument combination provided: '%s'");

        private static final String codePrefix = "INT";
        private static final String messagePrefix = "Internal Error";

        Internal(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
