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

package com.vaticle.typedb.driver.common;

import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.jni.StringPromise;
import com.vaticle.typedb.driver.jni.VoidPromise;

import java.util.function.Function;
import java.util.function.Supplier;

public class Promise<T> {
    private final Supplier<T> inner;

    public <F extends Supplier<T>> Promise(F inner) {
        this.inner = inner;
    }

    public T resolve() {
        return this.inner.get();
    }

    static public<T, U, P extends Supplier<T>, F extends Function<T, U>>
    Promise<U> map(P promise, F fn) {
        return new Promise<>(() -> {
            try {
                T res = promise.get();
                if (res != null) return fn.apply(res);
                else return null;
            } catch (com.vaticle.typedb.driver.jni.Error e) {
                throw new TypeDBDriverException(e);
            }
        });
    }

    static public Promise<Void> of(VoidPromise promise) {
        return new Promise<>(() -> {
            try {
                return promise.get();
            } catch (com.vaticle.typedb.driver.jni.Error e) {
                throw new TypeDBDriverException(e);
            }
        });
    }

    static public Promise<String> of(StringPromise promise) {
        return new Promise<>(() -> {
            try {
                return promise.get();
            } catch (com.vaticle.typedb.driver.jni.Error e) {
                throw new TypeDBDriverException(e);
            }
        });
    }
}
