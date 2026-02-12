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
using Xunit;

namespace TypeDB.Driver.Test.Behaviour
{
    /// <summary>
    /// Helper class for handling optional error expectations in BDD steps.
    /// Parses error mode strings like "; fails", "; parsing fails",
    /// or "; fails with a message containing: "..."" and provides
    /// assertion methods that check for expected errors.
    /// </summary>
    public class MayError
    {
        public bool ExpectsError { get; }
        public string? ExpectedMessage { get; }

        private MayError(bool expectsError, string? expectedMessage = null)
        {
            ExpectsError = expectsError;
            ExpectedMessage = expectedMessage;
        }

        /// <summary>
        /// Creates a MayError that expects success (no error).
        /// </summary>
        public static MayError No() => new MayError(false);

        /// <summary>
        /// Creates a MayError that expects any error.
        /// </summary>
        public static MayError Yes() => new MayError(true);

        /// <summary>
        /// Creates a MayError that expects an error containing the specified message.
        /// </summary>
        public static MayError WithMessage(string message) => new MayError(true, message);

        /// <summary>
        /// Parses the error mode string captured from a regex group.
        /// </summary>
        /// <param name="errorMode">The captured error mode (empty, "; fails", "; parsing fails",
        /// or "; fails with a message containing: "..."")</param>
        /// <param name="expectedMessage">Optional message captured from a separate regex group</param>
        public static MayError Parse(string errorMode, string? expectedMessage = null)
        {
            if (string.IsNullOrEmpty(errorMode))
                return No();

            if (errorMode == "; fails" || errorMode == "; parsing fails")
                return Yes();

            // Handle case where message is in a separate capture group
            if (!string.IsNullOrEmpty(expectedMessage))
                return WithMessage(expectedMessage);

            // Handle case where message is embedded in errorMode
            var prefix = "; fails with a message containing: \"";
            if (errorMode.StartsWith(prefix) && errorMode.EndsWith("\""))
                return WithMessage(errorMode.Substring(prefix.Length, errorMode.Length - prefix.Length - 1));

            throw new ArgumentException($"Invalid error mode: {errorMode}");
        }

        /// <summary>
        /// Executes the action and asserts based on error expectations.
        /// If no error expected: action runs normally.
        /// If error expected: asserts that action throws.
        /// If error with message expected: asserts that exception message contains expected text.
        /// </summary>
        public void Check(Action action)
        {
            if (!ExpectsError)
            {
                action();
            }
            else if (ExpectedMessage == null)
            {
                Assert.ThrowsAny<Exception>(action);
            }
            else
            {
                var exception = Assert.ThrowsAny<Exception>(action);
                Assert.Contains(ExpectedMessage, exception.Message);
            }
        }

        /// <summary>
        /// Executes the function and asserts based on error expectations.
        /// Returns the result if no error expected, default(T) otherwise.
        /// </summary>
        public T? Check<T>(Func<T> func)
        {
            if (!ExpectsError)
            {
                return func();
            }

            Check(() => { func(); });
            return default;
        }
    }
}
