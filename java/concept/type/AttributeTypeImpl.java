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

package com.vaticle.typedb.client.concept.type;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.value.ValueImpl;
import com.vaticle.typedb.client.concept.thing.AttributeImpl;
import com.vaticle.typedb.client.jni.Transitivity;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_instances;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_owners;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_regex;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_subtypes_with_value_type;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_get_value_type;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_put;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_set_regex;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_set_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.attribute_type_unset_regex;
import static java.util.Collections.emptySet;

public class AttributeTypeImpl extends ThingTypeImpl implements AttributeType {
    public AttributeTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public Value.Type getValueType() {
        return Value.Type.of(attribute_type_get_value_type(nativeObject));
    }

    @Override
    public final void setSupertype(TypeDBTransaction transaction, AttributeType attributeType) {
        try {
            attribute_type_set_supertype(nativeTransaction(transaction),
                nativeObject, ((AttributeTypeImpl) attributeType).nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public AttributeTypeImpl getSupertype(TypeDBTransaction transaction) {
        try {
            com.vaticle.typedb.client.jni.Concept res = attribute_type_get_supertype(nativeTransaction(transaction),
                    nativeObject);
            if (res != null) return new AttributeTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<AttributeTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return attribute_type_get_supertypes(nativeTransaction(transaction), nativeObject).stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
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
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<AttributeTypeImpl> getSubtypes(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity) {
        try {
            return attribute_type_get_subtypes_with_value_type(nativeTransaction(transaction), nativeObject, valueType.nativeObject, transitivity.nativeObject)
                    .stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
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
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
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
        com.vaticle.typedb.client.jni.Annotation[] annotationsArray = annotations.stream().map(anno -> anno.nativeObject).toArray(com.vaticle.typedb.client.jni.Annotation[]::new);
        try {
            return attribute_type_get_owners(nativeTransaction(transaction), nativeObject, transitivity.nativeObject, annotationsArray).stream().map(ThingTypeImpl::of);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public Attribute put(TypeDBTransaction transaction, String value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Attribute put(TypeDBTransaction transaction, long value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Attribute put(TypeDBTransaction transaction, double value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Attribute put(TypeDBTransaction transaction, boolean value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public Attribute put(TypeDBTransaction transaction, LocalDateTime value) {
        return put(transaction, ValueImpl.of(value));
    }

    @Override
    public final Attribute put(TypeDBTransaction transaction, Value value) {
        try {
            return new AttributeImpl(attribute_type_put(nativeTransaction(transaction), nativeObject, ((ValueImpl) value).nativeObject));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public Attribute get(TypeDBTransaction transaction, String value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Nullable
    @Override
    public Attribute get(TypeDBTransaction transaction, long value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Nullable
    @Override
    public Attribute get(TypeDBTransaction transaction, double value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Nullable
    @Override
    public Attribute get(TypeDBTransaction transaction, boolean value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Nullable
    @Override
    public Attribute get(TypeDBTransaction transaction, LocalDateTime value) {
        return get(transaction, ValueImpl.of(value));
    }

    @Nullable
    @Override
    public final Attribute get(TypeDBTransaction transaction, Value value) {
        try {
            com.vaticle.typedb.client.jni.Concept res = attribute_type_get(nativeTransaction(transaction), nativeObject, ((ValueImpl) value).nativeObject);
            if (res != null) return new AttributeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public String getRegex(TypeDBTransaction transaction) {
        try {
            return attribute_type_get_regex(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void setRegex(TypeDBTransaction transaction, String regex) {
        try {
            attribute_type_set_regex(nativeTransaction(transaction), nativeObject, regex);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void unsetRegex(TypeDBTransaction transaction) {
        try {
            attribute_type_unset_regex(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
