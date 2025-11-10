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

package com.typedb.driver.api.analyze;

import java.util.Optional;
import java.util.stream.Stream;

/// An <code>AnalyzedQuery</code> contains the server's representation of the query and preamble functions;
/// as well as the result of types inferred for each variable by type-inference.
public interface AnalyzedQuery {
    /**
     * A representation of the query as a <code>Pipeline</code>
     *
     * @return the <code>Pipeline</code> representing the query.
     */
    Pipeline pipeline();

    /**
     * A representation of the <code>Function</code>s in the preamble of the query
     *
     * @return stream of the analyzed functions in the preamble.
     */
    Stream<? extends Function> preamble();

    /**
     * A representation of the <code>Fetch</code> stage of the query, if it has one
     *
     * @return an Optional containing the fetch stage if present, empty otherwise
     */
    Optional<? extends Fetch> fetch();
}
