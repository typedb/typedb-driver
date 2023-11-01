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
import com.vaticle.typedb.driver.api.concept.type.RelationType;
import com.vaticle.typedb.driver.api.concept.type.RoleType;
import com.vaticle.typedb.driver.common.NativeIterator;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.thing.RelationImpl;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_create;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_instances;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_relates;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_relates_for_role_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_relates_overridden;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_subtypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_supertype;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_get_supertypes;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_set_relates;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_set_supertype;
import static com.vaticle.typedb.driver.jni.typedb_driver.relation_type_unset_relates;

public class RelationTypeImpl extends ThingTypeImpl implements RelationType {
    public RelationTypeImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    @Override
    public final Promise<RelationImpl> create(TypeDBTransaction transaction) {
        return Promise.map(relation_type_create(nativeTransaction(transaction), nativeObject), RelationImpl::new);
    }

    @Override
    @CheckReturnValue
    public final Promise<Void> setSupertype(TypeDBTransaction transaction, RelationType relationType) {
        return new Promise<>(relation_type_set_supertype(nativeTransaction(transaction), nativeObject, ((RelationTypeImpl) relationType).nativeObject));
    }

    @Override
    public final Stream<RoleTypeImpl> getRelates(TypeDBTransaction transaction) {
        return getRelates(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RoleTypeImpl> getRelates(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NativeIterator<>(relation_type_get_relates(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Promise<RoleTypeImpl> getRelates(TypeDBTransaction transaction, String roleLabel) {
        return Promise.map(relation_type_get_relates_for_role_label(nativeTransaction(transaction), nativeObject, roleLabel), RoleTypeImpl::new);
    }

    @Nullable
    @Override
    public Promise<RoleTypeImpl> getRelatesOverridden(TypeDBTransaction transaction, RoleType roleType) {
        return getRelatesOverridden(transaction, roleType.getLabel().name());
    }

    @Override
    public final Promise<RoleTypeImpl> getRelatesOverridden(TypeDBTransaction transaction, String roleLabel) {
        return Promise.map(relation_type_get_relates_overridden(nativeTransaction(transaction), nativeObject, roleLabel), RoleTypeImpl::new);
    }

    @Override
    @CheckReturnValue
    public final Promise<Void> setRelates(TypeDBTransaction transaction, String roleLabel) {
        return setRelates(transaction, roleLabel, (String) null);
    }

    @Override
    @CheckReturnValue
    public Promise<Void> setRelates(TypeDBTransaction transaction, String roleLabel, RoleType overriddenType) {
        return setRelates(transaction, roleLabel, overriddenType.getLabel().name());
    }

    @Override
    @CheckReturnValue
    public final Promise<Void> setRelates(TypeDBTransaction transaction, String roleLabel, String overriddenLabel) {
        return new Promise<>(relation_type_set_relates(nativeTransaction(transaction), nativeObject, roleLabel, overriddenLabel));
    }

    @Override
    @CheckReturnValue
    public Promise<Void> unsetRelates(TypeDBTransaction transaction, RoleType roleType) {
        return new Promise<>(relation_type_unset_relates(nativeTransaction(transaction), nativeObject, roleType.getLabel().name()));
    }

    @Override
    @CheckReturnValue
    public final Promise<Void> unsetRelates(TypeDBTransaction transaction, String roleLabel) {
        return new Promise<>(relation_type_unset_relates(nativeTransaction(transaction), nativeObject, roleLabel));
    }

    @Nullable
    @Override
    public Promise<RelationTypeImpl> getSupertype(TypeDBTransaction transaction) {
        return Promise.map(relation_type_get_supertype(nativeTransaction(transaction), nativeObject), RelationTypeImpl::new);
    }

    @Override
    public final Stream<RelationTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return new NativeIterator<>(relation_type_get_supertypes(nativeTransaction(transaction), nativeObject)).stream().map(RelationTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NativeIterator<>(relation_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(RelationTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final Stream<RelationImpl> getInstances(TypeDBTransaction transaction) {
        return getInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RelationImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NativeIterator<>(relation_type_get_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(RelationImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }
}
