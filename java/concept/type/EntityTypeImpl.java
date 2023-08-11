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
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.thing.EntityImpl;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.entity_type_create;
import static com.vaticle.typedb.client.jni.typedb_client.entity_type_get_instances;
import static com.vaticle.typedb.client.jni.typedb_client.entity_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client.entity_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.entity_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client.entity_type_set_supertype;

public class EntityTypeImpl extends ThingTypeImpl implements EntityType {
    public EntityTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public final EntityImpl create(TypeDBTransaction transaction) {
        try {
            return new EntityImpl(entity_type_create(nativeTransaction(transaction), nativeObject));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void setSupertype(TypeDBTransaction transaction, EntityType entityType) {
        try {
            entity_type_set_supertype(nativeTransaction(transaction), nativeObject, ((EntityTypeImpl) entityType).nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public EntityTypeImpl getSupertype(TypeDBTransaction transaction) {
        try {
            com.vaticle.typedb.client.jni.Concept res = entity_type_get_supertype(nativeTransaction(transaction), nativeObject);
            if (res != null) return new EntityTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<EntityTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return entity_type_get_supertypes(nativeTransaction(transaction), nativeObject).stream().map(EntityTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<EntityTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<EntityTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return entity_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(EntityTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<EntityImpl> getInstances(TypeDBTransaction transaction) {
        return getInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<EntityImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return entity_type_get_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(EntityImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
