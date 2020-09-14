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

    public static class Client extends ErrorMessage {
        public static final Client CONNECTION_CLOSED =
                new Client(1, "The connection to the database is closed.");
        public static final Client TRANSACTION_LISTENER_TERMINATED =
                new Client(2, "Transaction listener was terminated");
        public static final Client NEGATIVE_BATCH_SIZE =
                new Client(3, "Batch size cannot be less than 1, was: '%d'.");
        public static final Client MISSING_DB_NAME =
                new Client(4, "Database name cannot be null or empty.");
        public static final Client MISSING_RESPONSE =
                new Client(5, "The required field 'res' of type '%s' was not set.");

        private static final String codePrefix = "CLI";
        private static final String messagePrefix = "Illegal Client State";

        Client(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Concept extends ErrorMessage {
        public static final Concept INVALID_CONCEPT_CASTING =
                new Concept(1, "Invalid concept conversion from '%s' to '%s'.");
        public static final Concept MISSING_TRANSACTION =
                new Concept(2, "Transaction can not be null.");
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
        public static final Query BAD_QUERY_OBJECT =
                new Query(3, "The query object '%s' was not recognised.");
        public static final Query BAD_ANSWER_TYPE =
                new Query(4, "The answer type '%s' was not recognised.");
        public static final Query MISSING_ANSWER =
                new Query(5, "The required field 'answer' of type '%s' was not set.");

        private static final String codePrefix = "QRY";
        private static final String messagePrefix = "Query Error";

        Query(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
