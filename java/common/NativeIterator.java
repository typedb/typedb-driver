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

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;

public class NativeIterator<T> implements Iterator<T> {
    private final Iterator<T> inner;

    public NativeIterator(Iterator<T> inner) {
        this.inner = inner;
    }

    @Override
    public boolean hasNext() {
        try {
            return inner.hasNext();
        } catch (com.vaticle.typedb.driver.jni.Error.Unchecked e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public T next() {
        try {
            return inner.next();
        } catch (com.vaticle.typedb.driver.jni.Error.Unchecked e) {
            throw new TypeDBDriverException(e);
        }
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliteratorUnknownSize(this, NONNULL | IMMUTABLE | ORDERED), false);
    }
}
