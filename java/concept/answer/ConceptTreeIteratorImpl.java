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

import com.typedb.driver.api.answer.ConceptTree;
import com.typedb.driver.api.answer.ConceptTreeIterator;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Driver.UNIMPLEMENTED;

public class ConceptTreeIteratorImpl extends QueryAnswerImpl implements ConceptTreeIterator {
    public ConceptTreeIteratorImpl(com.typedb.driver.jni.QueryAnswer answer) {
        super(answer);
        throw new TypeDBDriverException(UNIMPLEMENTED);
    }

    @Override
    public boolean hasNext() {
        throw new TypeDBDriverException(UNIMPLEMENTED);
//        try {
//            return nativeIterator.hasNext();
//        } catch (com.typedb.driver.jni.Error.Unchecked e) {
//            throw new TypeDBDriverException(e);
//        }
    }

    @Override
    public ConceptTree next() {
        throw new TypeDBDriverException(UNIMPLEMENTED);
//        try {
//            return new ConceptRowImpl(nativeIterator.next());
//        } catch (com.typedb.driver.jni.Error.Unchecked e) {
//            throw new TypeDBDriverException(e);
//        }
    }

    @Override
    public Stream<ConceptTree> stream() {
        throw new TypeDBDriverException(UNIMPLEMENTED);
//        return nativeIterator.stream().map(ConceptRowImpl::new);
    }
}
