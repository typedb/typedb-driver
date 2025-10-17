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

package com.typedb.driver.analyze;

import com.typedb.driver.api.analyze.AnalyzedQuery;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

import java.util.Optional;
import java.util.stream.Stream;

public class AnalyzedQueryImpl extends NativeObject<com.typedb.driver.jni.AnalyzedQuery> implements AnalyzedQuery { // TODO: implements AnalyzedQuery
    public AnalyzedQueryImpl(com.typedb.driver.jni.AnalyzedQuery nativeObject) {
        super(nativeObject);
    }

    public Pipeline pipeline() {
        return new Pipeline(typedb_driver.analyzed_query_pipeline(nativeObject));
    }

    public Stream<Function> preamble() {
        return new NativeIterator<>(typedb_driver.analyzed_preamble(nativeObject)).stream().map(Function::new);
    }

    public Optional<Fetch> fetch() {
        return Optional.ofNullable(typedb_driver.analyzed_fetch(nativeObject)).map(Fetch::new);
    }
}
