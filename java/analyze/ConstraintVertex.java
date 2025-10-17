package com.typedb.driver.analyze;

import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

public class ConstraintVertex extends NativeObject<com.typedb.driver.jni.ConstraintVertex> {
    public ConstraintVertex(com.typedb.driver.jni.ConstraintVertex nativeObject) {
        super(nativeObject);
    }

    public boolean isVariable() {
        return typedb_driver.constraint_vertex_is_variable(nativeObject);
    }

    public boolean isLabel() {
        return typedb_driver.constraint_vertex_is_label(nativeObject);
    }

    public boolean isValue() {
        return typedb_driver.constraint_vertex_is_value(nativeObject);
    }

    public boolean isNamedRole() {
        return typedb_driver.constraint_vertex_is_named_role(nativeObject);
    }

    public Variable asVariable() {
        if (!isVariable()) {
            throw new IllegalStateException("ConstraintVertex is not a Variable");
        }
        return new Variable(typedb_driver.constraint_vertex_as_variable(nativeObject));
    }

    public com.typedb.driver.api.concept.type.Type asLabel() {
        if (!isLabel()) {
            throw new IllegalStateException("ConstraintVertex is not a Label");
        }
        return com.typedb.driver.concept.ConceptImpl.of(typedb_driver.constraint_vertex_as_label(nativeObject)).asType();
    }

    public com.typedb.driver.api.concept.value.Value asValue() {
        if (!isValue()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return new com.typedb.driver.concept.value.ValueImpl(typedb_driver.constraint_vertex_as_value(nativeObject));
    }


    public Variable asNamedRoleGetVariable() {
        if (!isNamedRole()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return new Variable(typedb_driver.constraint_vertex_as_named_role_get_variable(nativeObject));
    }

    public String asNamedRoleGetName() {
        if (!isNamedRole()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return typedb_driver.constraint_vertex_as_named_role_get_name(nativeObject);
    }
}
