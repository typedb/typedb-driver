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

import java.util.stream.Stream;

/**
 * A representation of the 'fetch' stage of a query
 */
public interface Fetch {
    /**
     * @return The variant. One of `List, Leaf, Object`
     */
    com.typedb.driver.jni.FetchVariant variant();

    /**
     * Down-casts this <code>Fetch</code> as a <code>FetchLeaf</code> variant
     */
    FetchLeaf asLeaf();

    /**
     * Down-casts this <code>Fetch</code> as a <code>FetchList</code> variant
     */
    FetchList asList();

    /**
     * Down-casts this <code>Fetch</code> as a <code>FetchObject</code> variant
     */
    FetchObject asObject();

    /**
     * A mapping of string keys to <code>Fetch</code> documents.
     */
    interface FetchObject {
        /**
         * @return The available keys of this <code>Fetch</code> document.
         */
        Stream<String> keys();

        /**
         * @return The <code>Fetch</code> object for the given key.
         */
        Fetch get(String key);
    }

    /**
     * A <code>List</code> of <code>Fetch</code> documents.
     */
    interface FetchList {

        /**
         * @return The element of the list
         */
        Fetch element();
    }

    /**
     * The leaf of a Fetch object. Holds information on the value it can hold.
     */
    interface FetchLeaf {
        /**
         * @return The possible <code>ValueType</code>s.
         */
        Stream<String> annotations();
    }
}
