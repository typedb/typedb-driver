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

package com.typedb.driver.api;

import com.typedb.driver.common.NativeObject;

import javax.annotation.CheckReturnValue;
import java.util.Optional;

import static com.typedb.driver.jni.typedb_driver.query_options_get_include_instance_types;
import static com.typedb.driver.jni.typedb_driver.query_options_has_include_instance_types;
import static com.typedb.driver.jni.typedb_driver.query_options_new;
import static com.typedb.driver.jni.typedb_driver.query_options_set_include_instance_types;

/**
 * TypeDB transaction options. <code>QueryOptions</code> object can be used to override
 * the default server behaviour for executed queries.
 */
public class QueryOptions extends NativeObject<com.typedb.driver.jni.QueryOptions> {
    /**
     * Produces a new <code>QueryOptions</code> object.
     *
     * <h3>Examples</h3>
     * <pre>
     * QueryOptions options = QueryOptions();
     * </pre>
     */
    public QueryOptions() {
        super(query_options_new());
    }

    /**
     * Returns the value set for the "include instance types" flag in this <code>QueryOptions</code> object.
     * If set, specifies if types should be included in instance structs returned in ConceptRow answers.
     * This option allows reducing the amount of unnecessary data transmitted.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.includeInstanceTypes();
     * </pre>
     */
    @CheckReturnValue
    public Optional<Boolean> includeInstanceTypes() {
        if (query_options_has_include_instance_types(nativeObject))
            return Optional.of(query_options_get_include_instance_types(nativeObject));
        return Optional.empty();
    }

    /**
     * Explicitly set the "include instance types" flag.
     * If set, specifies if types should be included in instance structs returned in ConceptRow answers.
     * This option allows reducing the amount of unnecessary data transmitted.
     *
     * <h3>Examples</h3>
     * <pre>
     * options.includeInstanceTypes(includeInstanceTypes);
     * </pre>
     *
     * @param includeInstanceTypes Whether to include instance types in ConceptRow answers.
     */
    public QueryOptions includeInstanceTypes(boolean includeInstanceTypes) {
        query_options_set_include_instance_types(nativeObject, includeInstanceTypes);
        return this;
    }
}
