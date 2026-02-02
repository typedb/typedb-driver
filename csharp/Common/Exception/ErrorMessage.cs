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

using System;

namespace TypeDB.Driver.Common
{
    public abstract class ErrorMessage
    {
        private readonly string _codePrefix;
        private readonly string _codeNumber;
        private readonly string _messagePrefix;
        private readonly string _messageBody;

        public ErrorMessage(
            string codePrefix, int codeNumber, string messagePrefix, string messageBody)
        {
            _codePrefix = codePrefix;
            _codeNumber = codeNumber.ToString("D2");
            _messagePrefix = messagePrefix;
            _messageBody = messageBody;
        }

        public string ToString(params object?[] errorParams)
        {
            var formattedBody = String.Format(_messageBody, errorParams);
            return $"[{_codePrefix}{_codeNumber}] {_messagePrefix}: {formattedBody}";
        }
    }

    namespace Error
    {
        public class Driver : ErrorMessage
        {
            private const string CODE_PREFIX = "CSDR";
            private const string MESSAGE_PREFIX = "Driver Error";

            public Driver(int codeNumber, string message)
                : base(CODE_PREFIX, codeNumber, MESSAGE_PREFIX, message)
            {
            }

            public static readonly Driver DRIVER_CLOSED =
                new Driver(1, "The driver has been closed and no further operation is allowed.");
            public static readonly Driver SESSION_CLOSED =
                new Driver(2, "The session has been closed and no further operation is allowed.");
            public static readonly Driver TRANSACTION_CLOSED =
                new Driver(3, "The transaction has been closed and no further operation is allowed.");
            public static readonly Driver TRANSACTION_CLOSED_WITH_ERRORS =
                new Driver(4, "The transaction has been closed with error(s): \n{0}.");
            public static readonly Driver DATABASE_DELETED =
                new Driver(5, "The database has been deleted and no further operation is allowed.");
            public static readonly Driver POSITIVE_VALUE_REQUIRED =
                new Driver(6, "Value cannot be less than 1, was: {0}.");
            public static readonly Driver UNEXPECTED_NATIVE_VALUE =
                new Driver(7, "Unexpected native value encountered!");
        }

        public class Concept : ErrorMessage
        {
            private const string CODE_PREFIX = "CSCO";
            private const string MESSAGE_PREFIX = "Concept Error";

            public Concept(int codeNumber, string message)
                : base(CODE_PREFIX, codeNumber, MESSAGE_PREFIX, message)
            {
            }

            public static readonly Concept INVALID_CONCEPT_CASTING =
                new Concept(1, "Invalid concept conversion from {0} to {1}.");
            public static readonly Concept MISSING_TRANSACTION =
                new Concept(2, "Transaction cannot be null.");
            public static readonly Concept MISSING_IID =
                new Concept(3, "IID cannot be null or empty.");
            public static readonly Concept MISSING_LABEL =
                new Concept(4, "Label cannot be null or empty.");
            public static readonly Concept MISSING_VARIABLE =
                new Concept(5, "Variable name cannot be null or empty.");
            public static readonly Concept MISSING_VALUE =
                new Concept(6, "Value cannot be null.");
            public static readonly Concept NONEXISTENT_EXPLAINABLE_CONCEPT =
                new Concept(7, "The concept identified by {0} is not explainable.");
            public static readonly Concept NONEXISTENT_EXPLAINABLE_OWNERSHIP =
                new Concept(8, "The ownership by owner {0} of attribute {1} is not explainable.");
            public static readonly Concept UNRECOGNISED_ANNOTATION =
                new Concept(9, "The annotation {0} is not recognised.");
            public static readonly Concept INVALID_QUERY_ANSWER_CASTING =
                new Concept(10, "Invalid query answer conversion from {0} to {1}.");
            public static readonly Concept INVALID_VALUE_RETRIEVAL =
                new Concept(11, "Could not retrieve a '{0}' value.");
        }

        public class Query : ErrorMessage
        {
            private const string CODE_PREFIX = "CSQR";
            private const string MESSAGE_PREFIX = "Query Error";

            public Query(int codeNumber, string message)
                : base(CODE_PREFIX, codeNumber, MESSAGE_PREFIX, message)
            {
            }

            public static readonly Query VARIABLE_DOES_NOT_EXIST =
                new Query(1, "The variable {0} does not exist.");
            public static readonly Query MISSING_QUERY =
                new Query(2, "Query cannot be null or empty.");
        }

        public class Internal : ErrorMessage
        {
            private const string CODE_PREFIX = "CSIN";
            private const string MESSAGE_PREFIX = "C# Internal Error";

            public Internal(int codeNumber, string message)
                : base(CODE_PREFIX, codeNumber, MESSAGE_PREFIX, message)
            {
            }

            public static readonly Internal UNEXPECTED_NATIVE_VALUE =
                new Internal(1, "Unexpected native value encountered!");
            public static readonly Internal ILLEGAL_STATE =
                new Internal(2, "Illegal state has been reached! ({0} : {1}).");
            public static readonly Internal ILLEGAL_CAST =
                new Internal(3, "Illegal casting operation to {0}.");
            public static readonly Internal NULL_NATIVE_VALUE =
                new Internal(4, "Unhandled null pointer to a native object encountered!");
            public static readonly Internal ENUMERATOR_EXCESSIVE_ACCESS =
                new Internal(5, "Such Enumerables support Enumerator's getting only once.");
            public static readonly Internal UNEXPECTED_INTERNAL_VALUE =
                new Internal(6, "Unexpected internal value {0} encountered!");
        }
    }
}
