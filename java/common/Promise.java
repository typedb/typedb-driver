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

package com.typedb.driver.common;

import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A <code>Promise</code> represents an asynchronous network operation.
 * <p>The request it represents is performed immediately. The response is only retrieved
 * once the <code>Promise</code> is <code>resolve</code>d.</p>
 */
public class Promise<T> {
    private final Supplier<T> inner;

    /**
     * Promise constructor
     *
     * <h3>Examples</h3>
     * <pre>
     * new Promise(supplier)
     * </pre>
     *
     * @param inner The supplier to function to wrap into the promise
     */
    public Promise(Supplier<T> inner) {
        this.inner = inner;
    }

    /**
     * Retrieves the result of the Promise.
     *
     * <h3>Examples</h3>
     * <pre>
     * promise.resolve()
     * </pre>
     */
    public T resolve() { // TODO: Can have a checked exception in some cases!
        try {
            return this.inner.get();
        } catch (com.typedb.driver.jni.Error.Unchecked e) {
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
     * @param promise The supplier function to wrap into the promise
     * @param fn      The mapping function
     */
    static public <T, U> Promise<U> map(Supplier<T> promise, Function<T, U> fn) {
        return new Promise<>(() -> {
            T res = promise.get();
            if (res != null) return fn.apply(res);
            else return null;
        });
    }
}
