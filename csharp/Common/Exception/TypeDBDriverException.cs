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
    /**
     * Exceptions raised by the driver.
     */
    public class TypeDBDriverException : System.Exception
    {
        /**
         * @private
         */
        public TypeDBDriverException(ErrorMessage error, params object?[] errorParams)
            : base(error.ToString(errorParams))
        {
        }

        /**
         * @private
         */
        public TypeDBDriverException(string message)
            : base(message)
        {
        }

        /**
         * @private
         */
        public TypeDBDriverException(System.Exception error)
            : base(error.Message)
        {
        }

        /**
         * @private
         */
        public TypeDBDriverException(Pinvoke.Error nativeError)
            : base(nativeError.Message)
        {
        }

        /**
         * Checks whether a substring is a part of this exception's message.
         *
         * <h3>Examples</h3>
         * <pre>
         * try
         * {
         *     ...
         * }
         * catch (TypeDBDriverException e)
         * {
         *     if (e.Contains("CSCO01"))
         *     {
         *         ...
         *     }
         *     else
         *     {
         *         ...
         *     }
         * }
         * </pre>
         */
        public bool Contains(string subString)
        {
            return Message.Contains(subString);
        }
    }
}
