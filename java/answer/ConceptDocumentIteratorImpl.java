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

package com.typedb.driver.answer;

import com.typedb.driver.api.answer.ConceptDocumentIterator;
import com.typedb.driver.api.answer.JSON;
import com.typedb.driver.common.NativeIterator;

import java.util.stream.Stream;

public class ConceptDocumentIteratorImpl extends QueryAnswerImpl implements ConceptDocumentIterator {
    NativeIterator<String> nativeIterator;

    public ConceptDocumentIteratorImpl(com.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
        nativeIterator = new NativeIterator<>(answer.intoDocuments());
    }

    @Override
    public boolean hasNext() {
        return nativeIterator.hasNext();
    }

    @Override
    public JSON next() {
        return JSON.parse(nativeIterator.next());
    }

    @Override
    public Stream<JSON> stream() {
        return nativeIterator.stream().map(JSON::parse);
    }
}
