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

 namespace com.vaticle.typedb.driver.Common.Exception
 {
    public abstract class ErrorMessage
    {
        public ErrorMessage(
            string codePrefix, int codeNumber, string messagePrefix, string messageBody)
        {
            _codePrefix = codePrefix;
            _codeNumber = codeNumber;
            _messagePrefix = messagePrefix;
            _messageBody = messageBody;
        }

        public override string ToString()
        {
            return $"[{_codePrefix}{_codeNumer}] {_messagePrefix}: {_messageBody}";
        }

        public static class Driver : ErrorMessage
        {
            public Driver(int codeNumber, string message)
                : base(_sCodePrefix, codeNumber, _sMessagePrefix, message)
            {
            }

            public static readonly Driver sDriverClosed = new Driver(1, "The driver has been closed and no further operation is allowed.");
            public static readonly Driver sSessionClosed = new Driver(2, "The session has been closed and no further operation is allowed.");
            public static readonly Driver sTransactionClosed = new Driver(3, "The transaction has been closed and no further operation is allowed.");
            public static readonly Driver sTransactionClosedWithErrors = new Driver(4, "The transaction has been closed with error(s): \n%s.");
            public static readonly Driver sDatabaseDeleted = new Driver(5, "The database has been deleted and no further operation is allowed.");
            public static readonly Driver sPositiveValueRequired = new Driver(6, "Value cannot be less than 1, was: '%d'.");
            public static readonly Driver sMissingDbName = new Driver(7, "Database name cannot be null.");

            private static readonly string _sCodePrefix = "CCL";
            private static readonly string _sMessagePrefix = "Driver Error";
        }

        public static class Concept : ErrorMessage
        {
            public Concept(int codeNumber, string message)
                : base(_sCodePrefix, codeNumber, _sMessagePrefix, message)
            {
            }

            public static readonly Concept sInvalidConceptCasting = new Concept(1, "Invalid concept conversion from '%s' to '%s'.");
            public static readonly Concept sMissingTransaction = new Concept(2, "Transaction cannot be null.");
            public static readonly Concept sMissingIID = new Concept(3, "IID cannot be null or empty.");
            public static readonly Concept sMissingLabel = new Concept(4, "Label cannot be null or empty.");
            public static readonly Concept sMissingVariable = new Concept(5, "Variable name cannot be null or empty.");
            public static readonly Concept sMissingValue = new Concept(6, "Value cannot be null.");
            public static readonly Concept sNonexistentExplainableConcept = new Concept(7, "The concept identified by '%s' is not explainable.");
            public static readonly Concept sNonexistentExplainableOwnership = new Concept(8, "The ownership by owner '%s' of attribute '%s' is not explainable.");
            public static readonly Concept sUnrecognisedAnnotation = new Concept(9, "The annotation '%s' is not recognised.");

            private static readonly string _sCodePrefix = "CCO";
            private static readonly string _sMessagePrefix = "Concept Error";
        }
        
        public static class Query : ErrorMessage
        {
            public Query(int codeNumber, string message)
                : base(_sCodePrefix, codeNumber, _sMessagePrefix, message)
            {
            }

            public static readonly Query sVariableDoesntExist = new Query(1, "The variable '%s' does not exist.");
            public static readonly Query sMissingQuery = new Query(2, "Query cannot be null or empty.");

            private static readonly string _sCodePrefix = "CQY";
            private static readonly string _sMessagePrefix = "Query Error";
        }

        public static class Internal : ErrorMessage
        {
            public Internal(int codeNumber, string message)
                : base(_sCodePrefix, codeNumber, _sMessagePrefix, message)
            {
            }

            public static readonly Internal sUnexpectedNativeValue = new Internal(1, "Unexpected native value encountered!");
            public static readonly Internal sIllegalState = new Internal(2, "Illegal state has been reached! (%s : %d).");
            public static readonly Internal sIllegalCast = new Internal(3, "Illegal casting operation to '%s'.");
            public static readonly Internal sNullNativeValue = new Internal(4, "Unhandled null pointer to a native object encountered!");
            public static readonly Internal sInvalidNativeHandle = new Internal(5, "The object does not have a valid native handle. It may have been:  uninitialised, moved or disposed.");
            public static readonly Internal sIteratorInvalidated = new Internal(6, "Dereferenced iterator which has reached end (or was invalidated by a move).");

            private static readonly string _sCodePrefix = "CIN";
            private static readonly string _sMessagePrefix = "C# Internal Error";
        }

        private readonly string _codePrefix;
        private readonly int _codeNumber;
        private readonly string _messagePrefix;
        private readonly string _messageBody;
    }

 }