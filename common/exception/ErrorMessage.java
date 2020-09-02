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

package grakn.client.common.exception;

public abstract class ErrorMessage extends grakn.common.exception.ErrorMessage {

    private ErrorMessage(String codePrefix, int codeNumber, String messagePrefix, String messageBody) {
        super(codePrefix, codeNumber, messagePrefix, messageBody);
    }

    public static class ClientInternal extends ErrorMessage {
        public static final ClientInternal UNRECOGNISED_VALUE =
                new ClientInternal(1, "Unrecognised schema value!");

        private static final String codePrefix = "CIN";
        private static final String messagePrefix = "Invalid Internal State (Client)";

        ClientInternal(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Connection extends ErrorMessage {
        public static final Connection CONNECTION_CLOSED =
                new Connection(1, "The connection to the database is closed.");
        public static final Connection TRANSACTION_LISTENER_TERMINATED =
                new Connection(2, "Transaction listener was terminated");
        public static final Connection NEGATIVE_BATCH_SIZE =
                new Connection(3, "Batch size cannot be less than 1, was: '%d'.");

        private static final String codePrefix = "CNN";
        private static final String messagePrefix = "Database Connection Error";

        Connection(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class DatabaseManager extends ErrorMessage {
        public static final DatabaseManager NULL_OR_EMPTY_DB_NAME =
                new DatabaseManager(1, "Database name cannot be null or empty.");

        private static final String codePrefix = "CDB";
        private static final String messagePrefix = "Invalid Database Operations (Client)";

        DatabaseManager(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Concept extends ErrorMessage {
        public static final Concept UNRECOGNISED_CONCEPT =
                new Concept(1, "The %s '%s' was not recognised.");
        public static final Concept INVALID_CONCEPT_CASTING =
                new Concept(2, "Invalid concept conversion from '%s' to '%s'.");
        public static final Concept NULL_TRANSACTION =
                new Concept(3, "Transaction can not be null.");
        public static final Concept NULL_OR_EMPTY_IID =
                new Concept(4, "IID cannot be null or empty.");

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
        public static final Query UNRECOGNISED_QUERY_OBJECT =
                new Query(3, "The query object '%s' was not recognised.");

        private static final String codePrefix = "QRY";
        private static final String messagePrefix = "Query Error";

        Query(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Protocol extends ErrorMessage {
        public static final Protocol UNRECOGNISED_FIELD =
                new Protocol(1, "The %s '%s' was not recognised.");
        public static final Protocol REQUIRED_FIELD_NOT_SET =
                new Protocol(2, "The required field '%s' was not set.");
        public static final Protocol ILLEGAL_COMBINATION_OF_FIELDS =
                new Protocol(3, "'%s' cannot be [%s] while '%s' is [%s].");

        private static final String codePrefix = "PRO";
        private static final String messagePrefix = "Protocol Error";

        Protocol(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
