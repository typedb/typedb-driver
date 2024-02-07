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

using System;

namespace com.vaticle.typedb.driver.Common.Exception
{
    public abstract class ErrorMessage
    {
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
            return $"[{_codePrefix}{_codeNumber}]{_messagePrefix}: {formattedBody}";
        }

        private readonly string _codePrefix;
        private readonly string _codeNumber;
        private readonly string _messagePrefix;
        private readonly string _messageBody;
    }

    namespace Error
    {
        public class DriverErrorMessage : ErrorMessage
        {
            public DriverErrorMessage(int codeNumber, string message)
                : base(s_codePrefix, codeNumber, s_messagePrefix, message)
            {
            }

            private static readonly string s_codePrefix = "CCL";
            private static readonly string s_messagePrefix = "DriverErrorMessage Error";
        }

        public static class Driver
        {
            public static readonly DriverErrorMessage s_DriverClosed =
                new DriverErrorMessage(1, "The driver has been closed and no further operation is allowed.");
            public static readonly DriverErrorMessage s_SessionClosed =
                new DriverErrorMessage(2, "The session has been closed and no further operation is allowed.");
            public static readonly DriverErrorMessage s_TransactionClosed =
                new DriverErrorMessage(3, "The transaction has been closed and no further operation is allowed.");
            public static readonly DriverErrorMessage s_TransactionClosedWithErrors =
                new DriverErrorMessage(4, "The transaction has been closed with error(s): \n{}.");
            public static readonly DriverErrorMessage s_DatabaseDeleted =
                new DriverErrorMessage(5, "The database has been deleted and no further operation is allowed.");
            public static readonly DriverErrorMessage s_PositiveValueRequired =
                new DriverErrorMessage(6, "Value cannot be less than 1, was: {}.");
            public static readonly DriverErrorMessage s_MissingDbName =
                new DriverErrorMessage(7, "Database name cannot be null.");
        }

        public class ConceptErrorMessage : ErrorMessage
        {
            public ConceptErrorMessage(int codeNumber, string message)
                : base(s_codePrefix, codeNumber, s_messagePrefix, message)
            {
            }

            private static readonly string s_codePrefix = "CCO";
            private static readonly string s_messagePrefix = "ConceptErrorMessage Error";
        }

        public static class Concept
        {
            public static readonly ConceptErrorMessage s_InvalidConceptCasting =
                new ConceptErrorMessage(1, "Invalid concept conversion from {} to {}.");
            public static readonly ConceptErrorMessage s_MissingTransaction =
                new ConceptErrorMessage(2, "Transaction cannot be null.");
            public static readonly ConceptErrorMessage s_MissingIID =
                new ConceptErrorMessage(3, "IID cannot be null or empty.");
            public static readonly ConceptErrorMessage s_MissingLabel =
                new ConceptErrorMessage(4, "Label cannot be null or empty.");
            public static readonly ConceptErrorMessage s_MissingVariable =
                new ConceptErrorMessage(5, "Variable name cannot be null or empty.");
            public static readonly ConceptErrorMessage s_MissingValue =
                new ConceptErrorMessage(6, "Value cannot be null.");
            public static readonly ConceptErrorMessage s_NonexistentExplainableConcept =
                new ConceptErrorMessage(7, "The concept identified by {} is not explainable.");
            public static readonly ConceptErrorMessage s_NonexistentExplainableOwnership =
                new ConceptErrorMessage(8, "The ownership by owner {} of attribute {} is not explainable.");
            public static readonly ConceptErrorMessage s_UnrecognisedAnnotation =
                new ConceptErrorMessage(9, "The annotation {} is not recognised.");
        }

        public class QueryErrorMessage : ErrorMessage
        {
            public QueryErrorMessage(int codeNumber, string message)
                : base(s_codePrefix, codeNumber, s_messagePrefix, message)
            {
            }

            private static readonly string s_codePrefix = "CQY";
            private static readonly string s_messagePrefix = "Query Error";
        }

        public static class Query
        {
            public static readonly QueryErrorMessage s_VariableDoesntExist =
                new QueryErrorMessage(1, "The variable {} does not exist.");
            public static readonly QueryErrorMessage s_MissingQuery =
                new QueryErrorMessage(2, "Query cannot be null or empty.");
        }

        public class InternalErrorMessage : ErrorMessage
        {
            public InternalErrorMessage(int codeNumber, string message)
                : base(s_codePrefix, codeNumber, s_messagePrefix, message)
            {
            }

            private static readonly string s_codePrefix = "CIN";
            private static readonly string s_messagePrefix = "C# InternalErrorMessage Error";
        }

        public static class Internal
        {
            public static readonly InternalErrorMessage s_UnexpectedNativeValue =
                new InternalErrorMessage(1, "Unexpected native value encountered!");
            public static readonly InternalErrorMessage s_IllegalState =
                new InternalErrorMessage(2, "Illegal state has been reached! ({} : {}).");
            public static readonly InternalErrorMessage s_IllegalCast =
                new InternalErrorMessage(3, "Illegal casting operation to {}.");
            public static readonly InternalErrorMessage s_NullNativeValue =
                new InternalErrorMessage(4, "Unhandled null pointer to a native object encountered!");
            public static readonly InternalErrorMessage s_InvalidNativeHandle =
                new InternalErrorMessage(5, "The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed.");
            public static readonly InternalErrorMessage s_IteratorInvalidated =
                new InternalErrorMessage(6, "Dereferenced iterator which has reached end (or was invalidated by a move).");
            public static readonly InternalErrorMessage s_UnexpectedInternalValue =
                new InternalErrorMessage(7, "Unexpected internal value encountered!");
        }
    }
}
