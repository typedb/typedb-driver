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

            public static readonly Driver TRANSACTION_CLOSED =
                new Driver(1, "The transaction has been closed and no further operation is allowed.");
            public static readonly Driver DATABASE_DELETED =
                new Driver(2, "The database has been deleted and no further operation is allowed.");
            public static readonly Driver UNEXPECTED_NATIVE_VALUE =
                new Driver(3, "Unexpected native value encountered!");
            public static readonly Driver NON_NULL_VALUE_REQUIRED =
                new Driver(4, "Value of '{0}' should not be null.");
            public static readonly Driver POSITIVE_VALUE_REQUIRED =
                new Driver(5, "Value of '{0}' should be positive, was: '{1}'.");
            public static readonly Driver NON_NEGATIVE_VALUE_REQUIRED =
                new Driver(6, "Value of '{0}' should be non-negative, was: '{1}'.");
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
            public static readonly Concept INVALID_QUERY_ANSWER_CASTING =
                new Concept(2, "Invalid query answer conversion from {0} to {1}.");
            public static readonly Concept INVALID_VALUE_RETRIEVAL =
                new Concept(3, "Could not retrieve a '{0}' value.");
        }

        public class Analyze : ErrorMessage
        {
            private const string CODE_PREFIX = "CSAN";
            private const string MESSAGE_PREFIX = "Analyze Error";

            public Analyze(int codeNumber, string message)
                : base(CODE_PREFIX, codeNumber, MESSAGE_PREFIX, message)
            {
            }

            public static readonly Analyze INVALID_CONSTRAINT_CASTING =
                new Analyze(1, "Invalid constraint conversion from {0} to {1}.");
            public static readonly Analyze INVALID_CONSTRAINT_VERTEX_CASTING =
                new Analyze(2, "Invalid constraint vertex conversion from {0} to {1}.");
            public static readonly Analyze INVALID_STAGE_CASTING =
                new Analyze(3, "Invalid stage conversion from {0} to {1}.");
            public static readonly Analyze INVALID_RETURN_OPERATION_CASTING =
                new Analyze(4, "Invalid return operation conversion from {0} to {1}.");
            public static readonly Analyze INVALID_FETCH_CASTING =
                new Analyze(5, "Invalid fetch conversion from {0} to {1}.");
            public static readonly Analyze INVALID_VARIABLE_ANNOTATIONS_CASTING =
                new Analyze(6, "Invalid VariableAnnotations conversion from {0} to {1}.");
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
            public static readonly Internal NULL_NATIVE_VALUE =
                new Internal(2, "Unhandled null pointer to a native object encountered!");
            public static readonly Internal ENUMERATOR_EXCESSIVE_ACCESS =
                new Internal(3, "Such Enumerables support Enumerator's getting only once.");
            public static readonly Internal UNEXPECTED_INTERNAL_VALUE =
                new Internal(4, "Unexpected internal value {0} encountered!");
        }
    }
}
