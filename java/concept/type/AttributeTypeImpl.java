/*
 * Copyright (C) 2022 Vaticle
 *
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

package com.vaticle.typedb.driver.concept.type;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.api.concept.thing.Attribute;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.value.ValueImpl;
import com.vaticle.typedb.driver.concept.thing.AttributeImpl;
import com.vaticle.typedb.driver.jni.ConceptPromise;
import com.vaticle.typedb.driver.jni.Transitivity;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_instances;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_owners;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_regex;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_subtypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_subtypes_with_value_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_supertype;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_supertypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_get_value_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_put;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_set_regex;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_set_supertype;
import static com.vaticle.typedb.driver.jni.typedb_driver.attribute_type_unset_regex;
import static java.util.Collections.emptySet;

public class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {
    public AttributeTypeImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @CheckReturnValue
    public static Promise<AttributeTypeImpl> promise(ConceptPromise promise) {
        return new Promise<>(() -> {
            try {
                com.vaticle.typedb.driver.jni.Concept res = promise.get();
                if (res != null) return new AttributeTypeImpl(res);
                else return null;
            } catch (com.vaticle.typedb.driver.jni.Error e) {
                throw new TypeDBDriverException(e);
            }
        });
    }

    @Override
    public Value.Type getValueType() {
        return Value.Type.of(attribute_type_get_value_type(nativeObject));
    }

    @Override
    public final Promise<Void> setSupertype(TypeDBTransaction transaction, AttributeType attributeType) {
        return Promise.ofVoid(attribute_type_set_supertype(nativeTransaction(transaction),
                nativeObject, ((AttributeTypeImpl) attributeType).nativeObject));
    }

    @Nullable
    @Override
    public Promise<AttributeTypeImpl> getSupertype(TypeDBTransaction transaction) {
        return AttributeTypeImpl.promise(attribute_type_get_supertype(nativeTransaction(transaction), nativeObject));
    }

    @Override
    public final Stream<AttributeTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return attribute_type_get_supertypes(nativeTransaction(transaction), nativeObject).stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<AttributeTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<AttributeTypeImpl> getSubtypes(TypeDBTransaction transaction, Value.Type valueType) {
        return getSubtypes(transaction, valueType, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<AttributeTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return attribute_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<AttributeTypeImpl> getSubtypes(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity) {
        try {
            return attribute_type_get_subtypes_with_value_type(nativeTransaction(transaction), nativeObject, valueType.nativeObject, transitivity.nativeObject)
                    .stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<AttributeImpl> getInstances(TypeDBTransaction transaction) {
        return getInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<AttributeImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return attribute_type_get_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(AttributeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Stream<ThingTypeImpl> getOwners(TypeDBTransaction transaction) {
        return getOwners(transaction, emptySet());
    }

    @Override
    public Stream<ThingTypeImpl> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations) {
        return getOwners(transaction, annotations, Transitivity.TRANSITIVE);
    }

    @Override
    public Stream<ThingTypeImpl> getOwners(TypeDBTransaction transaction, Transitivity transitivity) {
        return getOwners(transaction, emptySet(), transitivity);
    }

    @Override
    public Stream<ThingTypeImpl> getOwners(TypeDBTransaction transaction, Set<Annotation> annotations, Transitivity transitivity) {
        com.vaticle.typedb.driver.jni.Annotation[] annotationsArray = annotations.stream().map(anno -> anno.nativeObject).toArray(com.vaticle.typedb.driver.jni.Annotation[]::new);
        try {
            return attribute_type_get_owners(nativeTransaction(transaction), nativeObject, transitivity.nativeObject, annotationsArray).stream().map(ThingTypeImpl::of);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Promise<AttributeImpl> put(TypeDBTransaction transaction, String value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> put(TypeDBTransaction transaction, long value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> put(TypeDBTransaction transaction, double value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> put(TypeDBTransaction transaction, boolean value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> put(TypeDBTransaction transaction, LocalDateTime value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public final Promise<AttributeImpl> put(TypeDBTransaction transaction, Value value) {
        return AttributeImpl.promise(attribute_type_put(nativeTransaction(transaction), nativeObject, ((ValueImpl) value).nativeObject));
    }

    @Override
    public Promise<AttributeImpl> get(TypeDBTransaction transaction, String value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> get(TypeDBTransaction transaction, long value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> get(TypeDBTransaction transaction, double value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> get(TypeDBTransaction transaction, boolean value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Override
    public Promise<AttributeImpl> get(TypeDBTransaction transaction, LocalDateTime value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Override
    public final Promise<AttributeImpl> get(TypeDBTransaction transaction, Value value) {
        return AttributeImpl.promise(attribute_type_get(nativeTransaction(transaction), nativeObject, ((ValueImpl) value).nativeObject));
    }

    @Override
    public Promise<String> getRegex(TypeDBTransaction transaction) {
        return Promise.ofString(attribute_type_get_regex(nativeTransaction(transaction), nativeObject));
    }

    @Override
    public Promise<Void> setRegex(TypeDBTransaction transaction, String regex) {
        return Promise.ofVoid(attribute_type_set_regex(nativeTransaction(transaction), nativeObject, regex));
    }

    @Override
    public Promise<Void> unsetRegex(TypeDBTransaction transaction) {
        return Promise.ofVoid(attribute_type_unset_regex(nativeTransaction(transaction), nativeObject));
    }
}
