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

using TypeDB.Driver.Common;

namespace TypeDB.Driver.Common
{
    /**
     * A <code>VoidPromise</code> represents a <code>Promise</code> without an operation's result.
     * @see Promise
     */
    public class VoidPromise
    {
        private readonly Action _resolver;
        /**
         * Promise constructor
         *
         * <h3>Examples</h3>
         * <pre>
         * new Promise(action);
         * </pre>
         *
         * @param promise The function to wrap into the promise
         */
        public VoidPromise(Action resolver)
        {
            _resolver = resolver;
        }

        /**
         * Retrieves the result of the Promise.
         *
         * <h3>Examples</h3>
         * <pre>
         * promise.Resolve();
         * </pre>
         */
        public void Resolve()
        {
            try
            {
                _resolver();
            }
            catch (Pinvoke.Error e)
            {
                throw new TypeDBDriverException(e);
            }
        }
    }
}
