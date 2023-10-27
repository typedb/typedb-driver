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
import com.vaticle.typedb.driver.api.concept.type.EntityType;
import com.vaticle.typedb.driver.common.NetworkIterator;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.thing.EntityImpl;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_create;
import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_get_instances;
import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_get_subtypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_get_supertype;
import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_get_supertypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.entity_type_set_supertype;

public class EntityTypeImpl extends ThingTypeImpl implements EntityType {
    public EntityTypeImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public final Promise<EntityImpl> create(TypeDBTransaction transaction) {
        return Promise.map(entity_type_create(nativeTransaction(transaction), nativeObject), EntityImpl::new);
    }

    @Override
    public final Promise<Void> setSupertype(TypeDBTransaction transaction, EntityType entityType) {
        return new Promise<>(entity_type_set_supertype(nativeTransaction(transaction), nativeObject, ((EntityTypeImpl) entityType).nativeObject));
    }

    @Nullable
    @Override
    public Promise<EntityTypeImpl> getSupertype(TypeDBTransaction transaction) {
        return Promise.map(entity_type_get_supertype(nativeTransaction(transaction), nativeObject), EntityTypeImpl::new);
    }

    @Override
    public final Stream<EntityTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return new NetworkIterator<>(entity_type_get_supertypes(nativeTransaction(transaction), nativeObject)).stream().map(EntityTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<EntityTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<EntityTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NetworkIterator<>(entity_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(EntityTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<EntityImpl> getInstances(TypeDBTransaction transaction) {
        return getInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<EntityImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NetworkIterator<>(entity_type_get_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(EntityImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
