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

using TypeDB.Driver;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Common
{
    /// <summary>
    /// Exceptions raised by the driver.
    /// </summary>
    public class TypeDBDriverException : System.Exception
    {
        /// <summary>
        /// Creates a new exception from a structured error message.
        /// </summary>
        /// <param name="error">The error message template.</param>
        /// <param name="errorParams">The parameters for the error message.</param>
        public TypeDBDriverException(ErrorMessage error, params object?[] errorParams)
            : base(error.ToString(errorParams))
        {
        }

        /// <summary>
        /// Creates a new exception with the given message.
        /// </summary>
        /// <param name="message">The exception message.</param>
        public TypeDBDriverException(string message)
            : base(message)
        {
        }

        /// <summary>
        /// Creates a new exception wrapping an existing exception.
        /// </summary>
        /// <param name="error">The exception to wrap.</param>
        public TypeDBDriverException(System.Exception error)
            : base(error.Message)
        {
        }

        /// <summary>
        /// Creates a new exception from a native error.
        /// </summary>
        /// <param name="nativeError">The native error.</param>
        public TypeDBDriverException(Pinvoke.Error nativeError)
            : base(nativeError.Message)
        {
        }

        /// <summary>
        /// Checks whether a substring is a part of this exception's message.
        /// </summary>
        /// <param name="subString">The substring to search for.</param>
        /// <example>
        /// <code>
        /// try
        /// {
        ///     // ...
        /// }
        /// catch (TypeDBDriverException e)
        /// {
        ///     if (e.Contains("CSCO01"))
        ///     {
        ///         // ...
        ///     }
        /// }
        /// </code>
        /// </example>
        public bool Contains(string subString)
        {
            return Message.Contains(subString);
        }
    }
}
