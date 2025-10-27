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

import com.typedb.driver.api.analyze.Fetch;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.stream.Stream;

public abstract class FetchImpl extends NativeObject<com.typedb.driver.jni.Fetch> implements Fetch {
    protected FetchImpl(com.typedb.driver.jni.Fetch fetch) {
        super(fetch);
    }

    public static FetchImpl of(com.typedb.driver.jni.Fetch nativeObject) {
        switch (com.typedb.driver.jni.typedb_driver.fetch_variant(nativeObject)) {
            case Leaf:
                new FetchImpl.FetchLeafImpl(nativeObject);
            case List:
                new FetchImpl.FetchListImpl(nativeObject);
            case Object:
                new FetchImpl.FetchObjectImpl(nativeObject);
            default:
                throw new TypeDBDriverException("Unrecognised Fetch variant", null);
        }
    }

    public com.typedb.driver.jni.FetchVariant variant() {
        return com.typedb.driver.jni.typedb_driver.fetch_variant(nativeObject);
    }

    public static class FetchLeafImpl extends NativeObject<com.typedb.driver.jni.Fetch> implements Fetch.FetchLeaf {
        protected FetchLeafImpl(com.typedb.driver.jni.Fetch nativeObject) {
            super(nativeObject);
        }

        @Override
        public Stream<String> annotations() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.fetch_leaf_annotations(nativeObject)).stream();
        }
    }

    public static class FetchListImpl extends NativeObject<com.typedb.driver.jni.Fetch> implements Fetch.FetchList {
        protected FetchListImpl(com.typedb.driver.jni.Fetch nativeObject) {
            super(nativeObject);
        }

        @Override
        public Fetch element() {
            return FetchImpl.of(com.typedb.driver.jni.typedb_driver.fetch_list_element(nativeObject));
        }
    }

    public static class FetchObjectImpl extends NativeObject<com.typedb.driver.jni.Fetch> implements Fetch.FetchObject {

        protected FetchObjectImpl(com.typedb.driver.jni.Fetch nativeObject) {
            super(nativeObject);
        }

        @Override
        public Stream<String> keys() {
            return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.fetch_object_fields(nativeObject)).stream();
        }

        @Override
        public Fetch get(String key) {
            return FetchImpl.of(com.typedb.driver.jni.typedb_driver.fetch_object_get_field(nativeObject, key));
        }
    }
}
