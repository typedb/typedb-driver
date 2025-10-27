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

package com.typedb.driver.common.exception;

import java.util.HashMap;
import java.util.Map;

public abstract class ErrorMessage {
    private static final Map<String, Map<Integer, ErrorMessage>> errors = new HashMap<>();
    private static int maxCodeNumber = 0;
    private static int maxCodeDigits = 0;

    private final String codePrefix;
    private final int codeNumber;
    private final String message;
    private String code = null;

    protected ErrorMessage(String codePrefix, int codeNumber, String messagePrefix, String messageBody) {
        this.codePrefix = codePrefix;
        this.codeNumber = codeNumber;
        this.message = messagePrefix + ": " + messageBody;

        assert errors.get(codePrefix) == null || errors.get(codePrefix).get(codeNumber) == null;
        errors.computeIfAbsent(codePrefix, s -> new HashMap<>()).put(codeNumber, this);
        maxCodeNumber = Math.max(codeNumber, maxCodeNumber);
        maxCodeDigits = (int) Math.ceil(Math.log10(maxCodeNumber));
    }

    public String code() {
        if (code != null) return code;

        StringBuilder zeros = new StringBuilder();
        for (int digits = (int) Math.floor(Math.log10(codeNumber)) + 1; digits < maxCodeDigits; digits++) {
            zeros.append("0");
        }

        code = codePrefix + zeros + codeNumber;
        return code;
    }

    public String message(Object... parameters) {
        return String.format(toString(), parameters);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", code(), message);
    }

    public static class Driver extends ErrorMessage {
        public static final Driver UNRECOGNISED_OS =
                new Driver(1, "The operating system '%s' is not recognised.");
        public static final Driver UNRECOGNISED_ARCH =
                new Driver(2, "The architecture '%s' is not recognised.");
        public static final Driver UNRECOGNISED_OS_ARCH =
                new Driver(3, "The platform os '%s' and architecture '%s' are not supported by this driver.");
        public static final Driver JNI_LIBRARY_NOT_FOUND =
                new Driver(4, "Native JNI library not found (searching for '%s').");
        public static final Driver JNI_PLATFORM_LIBRARY_NOT_FOUND =
                new Driver(5, "Found multiple JNI libraries in classpath, could not select one for target platform (searching for '%s' for platform '%s').");
        public static final Driver TRANSACTION_CLOSED =
                new Driver(6, "The transaction has been closed and no further operation is allowed.");
        public static final Driver DATABASE_DELETED =
                new Driver(7, "The database has been deleted and no further operation is allowed.");
        public static final Driver POSITIVE_VALUE_REQUIRED =
                new Driver(8, "Value of '%s' should be positive, was: '%d'.");
        public static final Driver NON_NEGATIVE_VALUE_REQUIRED =
                new Driver(9, "Value of '%s' should be non-negative, was: '%d'.");
        public static final Driver NON_NULL_VALUE_REQUIRED =
                new Driver(10, "Value of '%s' should not be null.");
        public static final Driver UNIMPLEMENTED =
                new Driver(11, "This operation is not implemented yet.");

        private static final String codePrefix = "JDR";
        private static final String messagePrefix = "Driver Error";

        Driver(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Concept extends ErrorMessage {
        public static final Concept INVALID_CONCEPT_CASTING =
                new Concept(1, "Invalid concept conversion from '%s' to '%s'.");
        public static final Concept INVALID_QUERY_ANSWER_CASTING =
                new Concept(2, "Invalid query answer conversion from '%s' to '%s'.");
        public static final Concept INVALID_VALUE_RETRIEVAL =
                new Concept(3, "Could not retrieve a '%s' value.");

        private static final String codePrefix = "JCO";
        private static final String messagePrefix = "Concept Error";

        Concept(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Analyze extends ErrorMessage {
        public static final Analyze INVALID_CONSTRAINT_CASTING =
                new Analyze(1, "Invalid constraint conversion from '%s' to '%s'.");
        public static final Analyze INVALID_STAGE_CASTING =
                new Analyze(2, "Invalid stage conversion from '%s' to '%s'.");

        public static final Analyze INVALID_RETURN_OPERATION_CASTING =
                new Analyze(3, "Invalid return operation conversion from '%s' to '%s'.");

        public static final Analyze INVALID_FETCH_CASTING =
                new Analyze(4, "Invalid fetch conversion from '%s' to '%s'.");

        private static final String codePrefix = "JAN";
        private static final String messagePrefix = "Analyze Error";

        Analyze(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }

    public static class Internal extends ErrorMessage {
        public static final Internal UNEXPECTED_NATIVE_VALUE =
                new Internal(1, "Unexpected native value encountered!");
        public static final Internal ILLEGAL_STATE =
                new Internal(2, "Illegal state has been reached!");
        public static final Internal NULL_NATIVE_VALUE =
                new Internal(3, "Unhandled null pointer to a native object encountered!");
        public static final Internal NULL_CONCEPT_PROPERTY =
                new Internal(4, "Unexpected null for a concept (%s) property is found!");

        private static final String codePrefix = "JIN";
        private static final String messagePrefix = "Java Internal Error";

        Internal(int number, String message) {
            super(codePrefix, number, messagePrefix, message);
        }
    }
}
