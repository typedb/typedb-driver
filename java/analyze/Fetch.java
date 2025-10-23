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

import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;

import java.util.stream.Stream;

public class Fetch extends NativeObject<com.typedb.driver.jni.Fetch> {
    public Fetch(com.typedb.driver.jni.Fetch fetch) {
        super(fetch);
    }

    public com.typedb.driver.jni.FetchVariant variant() {
        return com.typedb.driver.jni.typedb_driver.fetch_variant(nativeObject);
    }
    public Fetch asListGetElement() {
        return new Fetch(com.typedb.driver.jni.typedb_driver.fetch_list_element(nativeObject));
    }

    public Stream<String> asLeafGetAnnotations() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.fetch_leaf_annotations(nativeObject)).stream();
    }

    public Stream<String> asObjectGetAvailableFields() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.fetch_object_fields(nativeObject)).stream();
    }

    public Fetch asObjectGetField(String field) {
        return new Fetch(com.typedb.driver.jni.typedb_driver.fetch_object_get_field(nativeObject, field));
    }
}
