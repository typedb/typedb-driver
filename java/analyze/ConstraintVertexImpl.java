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
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.jni.ConstraintVertexVariant;
import com.typedb.driver.jni.typedb_driver;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_CONSTRAINT_VERTEX_CASTING;
import static com.typedb.driver.common.util.Objects.className;

public class ConstraintVertexImpl extends NativeObject<com.typedb.driver.jni.ConstraintVertex> implements ConstraintVertex {
    public ConstraintVertexImpl(com.typedb.driver.jni.ConstraintVertex nativeObject) {
        super(nativeObject);
    }

    public com.typedb.driver.jni.ConstraintVertexVariant variant() {
        return typedb_driver.constraint_vertex_variant(nativeObject);
    }

    public boolean isVariable() {
        return variant() == ConstraintVertexVariant.VariableVertex;
    }

    public boolean isLabel() {
        return variant() == ConstraintVertexVariant.LabelVertex;
    }

    public boolean isValue() {
        return variant() == ConstraintVertexVariant.ValueVertex;
    }

    public boolean isNamedRole() {
        return variant() == ConstraintVertexVariant.NamedRoleVertex;
    }

    public com.typedb.driver.jni.Variable asVariable() {
        if (!isVariable()) {
            throw new TypeDBDriverException(INVALID_CONSTRAINT_VERTEX_CASTING, this.variant(), ConstraintVertexVariant.VariableVertex);
        }
        return typedb_driver.constraint_vertex_as_variable(nativeObject);
    }

    public com.typedb.driver.api.concept.type.Type asLabel() {
        if (!isLabel()) {
            throw new TypeDBDriverException(INVALID_CONSTRAINT_VERTEX_CASTING, this.variant(), ConstraintVertexVariant.LabelVertex);
        }
        return com.typedb.driver.concept.ConceptImpl.of(typedb_driver.constraint_vertex_as_label(nativeObject)).asType();
    }

    public Value asValue() {
        if (!isValue()) {
            throw new TypeDBDriverException(INVALID_CONSTRAINT_VERTEX_CASTING, this.variant(), ConstraintVertexVariant.ValueVertex);
        }
        return new com.typedb.driver.concept.value.ValueImpl(typedb_driver.constraint_vertex_as_value(nativeObject));
    }

    public com.typedb.driver.jni.Variable asNamedRoleGetVariable() {
        if (!isNamedRole()) {
            throw new TypeDBDriverException(INVALID_CONSTRAINT_VERTEX_CASTING, this.variant(), ConstraintVertexVariant.NamedRoleVertex);
        }
        return typedb_driver.constraint_vertex_as_named_role_get_variable(nativeObject);
    }

    public String asNamedRoleGetName() {
        if (!isNamedRole()) {
            throw new TypeDBDriverException(INVALID_CONSTRAINT_VERTEX_CASTING, this.variant(), ConstraintVertexVariant.NamedRoleVertex);
        }
        return typedb_driver.constraint_vertex_as_named_role_get_name(nativeObject);
    }
}
