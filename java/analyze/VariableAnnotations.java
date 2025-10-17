package com.typedb.driver.analyze;

import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.concept.ConceptImpl;
import com.typedb.driver.jni.VariableAnnotationsVariant;

import java.util.stream.Stream;

public class VariableAnnotations extends NativeObject<com.typedb.driver.jni.VariableAnnotations> {
    protected VariableAnnotations(com.typedb.driver.jni.VariableAnnotations nativeObject) {
        super(nativeObject);
    }

    public com.typedb.driver.jni.VariableAnnotationsVariant variant() {
        return com.typedb.driver.jni.typedb_driver.variable_annotations_variant(nativeObject);
    }

    public boolean isThing() {
        return variant() == VariableAnnotationsVariant.ThingAnnotations;
    }

    public boolean isType() {
        return variant() == VariableAnnotationsVariant.TypeAnnotations;
    }

    public boolean isValue() {
        return variant() == VariableAnnotationsVariant.ValueAnnotations;
    }

    public Stream<Type> asThing() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_thing(nativeObject)).stream().map(ConceptImpl::of).map(ConceptImpl::asType);
    }

    public Stream<Type> asType() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_type(nativeObject)).stream().map(ConceptImpl::of).map(ConceptImpl::asType);
    }

    public Stream<String> asValue() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_value(nativeObject)).stream();
    }
}
