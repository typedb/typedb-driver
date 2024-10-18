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

package com.typedb.driver.concept.answer;

import com.typedb.driver.api.answer.ConceptRow;
import com.typedb.driver.api.answer.ConceptRowIterator;
import com.typedb.driver.common.NativeIterator;

import javax.annotation.CheckReturnValue;
import java.util.stream.Stream;

public class ConceptRowIteratorImpl extends QueryAnswerImpl implements ConceptRowIterator {
    NativeIterator<com.typedb.driver.jni.ConceptRow> nativeIterator;

    protected ConceptRowIteratorImpl(com.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
        nativeIterator = new NativeIterator<>(answer.intoRows());
    }

    @Override
    @CheckReturnValue
    public ConceptRowIterator asConceptRows() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return nativeIterator.hasNext();
    }

    @Override
    public ConceptRow next() {
        return new ConceptRowImpl(nativeIterator.next());
    }

    @Override
    public Stream<ConceptRow> stream() {
        return nativeIterator.stream().map(ConceptRowImpl::new);
    }
}
