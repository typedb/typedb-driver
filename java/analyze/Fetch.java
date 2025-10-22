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
