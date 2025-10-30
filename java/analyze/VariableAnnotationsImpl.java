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

import com.typedb.driver.api.analyze.VariableAnnotations;
import com.typedb.driver.api.concept.type.Type;
import com.typedb.driver.common.NativeIterator;
import com.typedb.driver.common.NativeObject;
import com.typedb.driver.common.exception.TypeDBDriverException;
import com.typedb.driver.concept.ConceptImpl;
import com.typedb.driver.jni.VariableAnnotationsVariant;

import java.util.stream.Stream;

import static com.typedb.driver.common.exception.ErrorMessage.Analyze.INVALID_VARIABLE_ANNOTATIONS_CASTING;

public class VariableAnnotationsImpl extends NativeObject<com.typedb.driver.jni.VariableAnnotations> implements VariableAnnotations {
    protected VariableAnnotationsImpl(com.typedb.driver.jni.VariableAnnotations nativeObject) {
        super(nativeObject);
    }

    public com.typedb.driver.jni.VariableAnnotationsVariant variant() {
        return com.typedb.driver.jni.typedb_driver.variable_annotations_variant(nativeObject);
    }

    public boolean isInstance() {
        return variant() == VariableAnnotationsVariant.InstanceAnnotations;
    }

    public boolean isType() {
        return variant() == VariableAnnotationsVariant.TypeAnnotations;
    }

    public boolean isValue() {
        return variant() == VariableAnnotationsVariant.ValueAnnotations;
    }

    public Stream<Type> asInstance() {
        if (!isInstance()) {
            throw new TypeDBDriverException(INVALID_VARIABLE_ANNOTATIONS_CASTING, variant(), VariableAnnotationsVariant.InstanceAnnotations);
        }
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_instance(nativeObject)).stream().map(ConceptImpl::of).map(ConceptImpl::asType);
    }

    public Stream<Type> asType() {
        if (!isType()) {
            throw new TypeDBDriverException(INVALID_VARIABLE_ANNOTATIONS_CASTING, variant(), VariableAnnotationsVariant.TypeAnnotations);
        }
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_type(nativeObject)).stream().map(ConceptImpl::of).map(ConceptImpl::asType);
    }

    public Stream<String> asValue() {
        if (!isValue()) {
            throw new TypeDBDriverException(INVALID_VARIABLE_ANNOTATIONS_CASTING, variant(), VariableAnnotationsVariant.ValueAnnotations);
        }
        return new NativeIterator<>(com.typedb.driver.jni.typedb_driver.variable_annotations_value(nativeObject)).stream();
    }
}
