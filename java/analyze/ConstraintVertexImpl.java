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

import com.typedb.driver.api.analyze.ConstraintVertex;
import com.typedb.driver.api.concept.value.Value;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.jni.typedb_driver;

public class ConstraintVertexImpl extends NativeObject<com.typedb.driver.jni.ConstraintVertex> implements ConstraintVertex {
    public ConstraintVertexImpl(com.typedb.driver.jni.ConstraintVertex nativeObject) {
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

    public com.typedb.driver.jni.Variable asVariable() {
        if (!isVariable()) {
            throw new IllegalStateException("ConstraintVertex is not a Variable");
        }
        return typedb_driver.constraint_vertex_as_variable(nativeObject);
    }

    public com.typedb.driver.api.concept.type.Type asLabel() {
        if (!isLabel()) {
            throw new IllegalStateException("ConstraintVertex is not a Label");
        }
        return com.typedb.driver.concept.ConceptImpl.of(typedb_driver.constraint_vertex_as_label(nativeObject)).asType();
    }

    public Value asValue() {
        if (!isValue()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return new com.typedb.driver.concept.value.ValueImpl(typedb_driver.constraint_vertex_as_value(nativeObject));
    }

    public com.typedb.driver.jni.Variable asNamedRoleGetVariable() {
        if (!isNamedRole()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return typedb_driver.constraint_vertex_as_named_role_get_variable(nativeObject);
    }

    public String asNamedRoleGetName() {
        if (!isNamedRole()) {
            throw new IllegalStateException("ConstraintVertex is not a Value");
        }
        return typedb_driver.constraint_vertex_as_named_role_get_name(nativeObject);
    }
}
