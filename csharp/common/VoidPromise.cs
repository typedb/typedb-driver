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

using Vaticle.Typedb.Driver.Common.Exception;

namespace Vaticle.Typedb.Driver.Common
{
    /**
     * A <code>VoidPromise</code> represents an asynchronous network operation without a return type.
     * <p>The request it represents is performed immediately. The response is only retrieved
     * once the <code>Promise</code> is <code>Resolve</code>d, but it is always null.</p> // TODO Change this description!
     */
    public class VoidPromise
    {
        private readonly Action _resolver;
        /**
         * Promise constructor
         *
         * <h3>Examples</h3>
         * <pre>
         * new Promise(supplier)
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
         * promise.Resolve()
         * </pre>
         */
        private void Resolve()
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

        /**
         * Helper function to map promises.
         *
         * <h3>Examples</h3>
         * <pre>
         * Promise.map(supplier, mapper);
         * </pre>
         *
         * @param promise The function to wrap into the promise
         * @param fn The mapping function
         */
        static public VoidPromise Map<T, U>(Action action)
        {
            return new VoidPromise(action);
        }
    }
}
