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
        public static final Client CLIENT_CLOSED =
                new Client(1, "The client has been closed and no further operation is allowed.");
        public static final Client SESSION_CLOSED =
                new Client(2, "The session has been closed and no further operation is allowed.");
        public static final Client TRANSACTION_CLOSED =
                new Client(3, "The transaction has been closed and no further operation is allowed.");
        public static final Client TRANSACTION_CLOSED_WITH_ERRORS =
                new Client(4, "The transaction has been closed with error(s): \n%s.");
        public static final Client DATABASE_DELETED =
                new Client(5, "The database has been deleted and no further operation is allowed.");
        public static final Client POSITIVE_VALUE_REQUIRED =
                new Client(6, "Value cannot be less than 1, was: '%d'.");
        public static final Client MISSING_DB_NAME =
                new Client(7, "Database name cannot be null.");

        private static final String codePrefix = "JCL";
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
        public static final Concept MISSING_VARIABLE =
                new Concept(5, "Variable name cannot be null or empty.");
        public static final Concept MISSING_VALUE =
                new Concept(6, "Value cannot be null.");
        public static final Concept NONEXISTENT_EXPLAINABLE_CONCEPT =
                new Concept(7, "The concept identified by '%s' is not explainable.");
        public static final Concept NONEXISTENT_EXPLAINABLE_OWNERSHIP =
                new Concept(8, "The ownership by owner '%s' of attribute '%s' is not explainable.");
        public static final Concept UNRECOGNISED_ANNOTATION =
                new Concept(9, "The annotation '%s' is not recognised");

        private static final String codePrefix = "JCO";
        private static final String messagePrefix = "Concept Error";

        Concept(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Query extends ErrorMessage {
        public static final Query VARIABLE_DOES_NOT_EXIST =
                new Query(1, "The variable '%s' does not exist.");
        public static final Query MISSING_QUERY =
                new Query(2, "Query cannot be null or empty");

        private static final String codePrefix = "JQY";
        private static final String messagePrefix = "Query Error";

        Query(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Internal extends ErrorMessage {
        public static final Internal UNEXPECTED_NATIVE_VALUE =
                new Internal(1, "Unexpected native value encountered!");
        public static final Internal ILLEGAL_STATE =
                new Internal(2, "Illegal state has been reached!");
        public static final Internal ILLEGAL_CAST =
                new Internal(3, "Illegal casting operation to '%s'.");
        public static final Internal NULL_NATIVE_VALUE =
                new Internal(4, "Unhandled null pointer to a native object encountered!");

        private static final String codePrefix = "JIN";
        private static final String messagePrefix = "Java Internal Error";

        Internal(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
