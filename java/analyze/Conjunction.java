package com.typedb.driver.analyze;

import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.concept.ConceptImpl;

import java.util.stream.Stream;

public class Conjunction extends NativeObject<com.typedb.driver.jni.Conjunction> {
    protected Conjunction(com.typedb.driver.jni.Conjunction nativeObject) {
        super(nativeObject);
    }

    public Stream<Constraint> constraints() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.conjunction_get_constraints(nativeObject)).stream().map(Constraint::of);
    }

    public Stream<Variable> annotated_variables() {
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.conjunction_get_annotated_variables(nativeObject)).stream().map(Variable::new);
    }

    public VariableAnnotations variable_annotations(Variable variable) {
        return new VariableAnnotations(com.typedb.driver.jni.typedb_driver.conjunction_get_variable_annotations(nativeObject, variable.nativeObject));
    }
}
