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

package com.vaticle.typedb.client.concept.thing;

import com.vaticle.typedb.client.api.TypeDBTransaction;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType.Annotation;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.ConceptImpl;
import com.vaticle.typedb.client.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.client.concept.type.RoleTypeImpl;
import com.vaticle.typedb.client.concept.type.ThingTypeImpl;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_attribute;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_entity;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_relation;
import static com.vaticle.typedb.client.jni.typedb_client.thing_delete;
import static com.vaticle.typedb.client.jni.typedb_client.thing_get_has;
import static com.vaticle.typedb.client.jni.typedb_client.thing_get_iid;
import static com.vaticle.typedb.client.jni.typedb_client.thing_get_is_inferred;
import static com.vaticle.typedb.client.jni.typedb_client.thing_get_playing;
import static com.vaticle.typedb.client.jni.typedb_client.thing_get_relations;
import static com.vaticle.typedb.client.jni.typedb_client.thing_is_deleted;
import static com.vaticle.typedb.client.jni.typedb_client.thing_set_has;
import static com.vaticle.typedb.client.jni.typedb_client.thing_unset_has;

public abstract class ThingImpl extends ConceptImpl implements Thing {
    private int hash = 0;

    ThingImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    public static ThingImpl of(com.vaticle.typedb.client.jni.Concept concept) {
        if (concept_is_entity(concept)) return new EntityImpl(concept);
        else if (concept_is_relation(concept)) return new RelationImpl(concept);
        else if (concept_is_attribute(concept)) return new AttributeImpl(concept);
        throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public final String getIID() {
        return thing_get_iid(nativeObject);
    }

    @Override
    public abstract ThingTypeImpl getType();

    @Override
    public boolean isInferred() {
        return thing_get_is_inferred(nativeObject);
    }

    @Override
    public ThingImpl asThing() {
        return this;
    }

    @Override
    public final Stream<AttributeImpl> getHas(TypeDBTransaction transaction, AttributeType... attributeTypes) {
        com.vaticle.typedb.client.jni.Concept[] attributeTypesArray = Arrays.stream(attributeTypes).map(at -> ((AttributeTypeImpl) at).nativeObject).toArray(com.vaticle.typedb.client.jni.Concept[]::new);
        try {
            return thing_get_has(nativeTransaction(transaction), nativeObject, attributeTypesArray, new com.vaticle.typedb.client.jni.Annotation[0]).stream().map(AttributeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<AttributeImpl> getHas(TypeDBTransaction transaction, Set<Annotation> annotations) {
        com.vaticle.typedb.client.jni.Annotation[] annotationsArray = annotations.stream().map(anno -> anno.nativeObject).toArray(com.vaticle.typedb.client.jni.Annotation[]::new);
        try {
            return thing_get_has(nativeTransaction(transaction), nativeObject, new com.vaticle.typedb.client.jni.Concept[0], annotationsArray).stream().map(AttributeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationImpl> getRelations(TypeDBTransaction transaction, RoleType... roleTypes) {
        com.vaticle.typedb.client.jni.Concept[] roleTypesArray = Arrays.stream(roleTypes).map(rt -> ((RoleTypeImpl) rt).nativeObject).toArray(com.vaticle.typedb.client.jni.Concept[]::new);
        try {
            return thing_get_relations(nativeTransaction(transaction), nativeObject, roleTypesArray).stream().map(RelationImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RoleTypeImpl> getPlaying(TypeDBTransaction transaction) {
        try {
            return thing_get_playing(nativeTransaction(transaction), nativeObject).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void setHas(TypeDBTransaction transaction, Attribute attribute) {
        try {
            thing_set_has(nativeTransaction(transaction), nativeObject, ((AttributeImpl) attribute).nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void unsetHas(TypeDBTransaction transaction, Attribute attribute) {
        try {
            thing_unset_has(nativeTransaction(transaction), nativeObject, ((AttributeImpl) attribute).nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void delete(TypeDBTransaction transaction) {
        try {
            thing_delete(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final boolean isDeleted(TypeDBTransaction transaction) {
        try {
            return thing_is_deleted(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) hash = getIID().hashCode();
        return hash;
    }
}
