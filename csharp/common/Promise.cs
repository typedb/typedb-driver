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

using com.vaticle.typedb.driver.Common.Exception;

namespace com.vaticle.typedb.driver.Common
{
    /**
     * A <code>Promise</code> represents an asynchronous network operation.
     * <p>The request it represents is performed immediately. The response is only retrieved
     * once the <code>Promise</code> is <code>Resolve</code>d.</p>
     */
    public class Promise<T>
    {
        private readonly Func<T> _inner;

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
        public Promise(Func<T> inner)
        {
            _inner = inner;
        }

        /**
         * Retrieves the result of the Promise.
         *
         * <h3>Examples</h3>
         * <pre>
         * promise.Resolve()
         * </pre>
         */
        public T Resolve()
        {
            try
            {
                return _inner();
            }
            catch (pinvoke.Error e) // TODO: .Unchecked
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
         */ // TODO:
//        static public<T, U> Promise<U> Map(Func<T> promise, Func<T, U> fn)
//        {
//            return new Promise<>(() ->
//                {
//                    T res = promise();
//                    if (res != null)
//                    {
//                        return fn(res);
//                    }
//
//                    return null;
//                });
//        }
    }
}
