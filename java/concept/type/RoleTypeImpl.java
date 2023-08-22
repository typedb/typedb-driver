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
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.role_type_delete;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_name;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_player_instances;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_player_types;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_relation_instances;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_relation_type;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_relation_types;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_scope;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_is_abstract;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_is_root;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_is_deleted;
import static com.vaticle.typedb.client.jni.typedb_client.role_type_set_label;

public class RoleTypeImpl extends TypeImpl implements RoleType {
    public RoleTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public final boolean isRoot() {
        return role_type_is_root(nativeObject);
    }

    @Override
    public final boolean isAbstract() {
        return role_type_is_abstract(nativeObject);
    }

    @Override
    public Label getLabel() {
        return Label.of(role_type_get_scope(nativeObject), role_type_get_name(nativeObject));
    }

    @Override
    public final void delete(TypeDBTransaction transaction) {
        try {
            role_type_delete(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final boolean isDeleted(TypeDBTransaction transaction) {
        try {
            return role_type_is_deleted(nativeTransaction(transaction), nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void setLabel(TypeDBTransaction transaction, String newLabel) {
        try {
            role_type_set_label(nativeTransaction(transaction), nativeObject, newLabel);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public RoleTypeImpl getSupertype(TypeDBTransaction transaction) {
        try {
            com.vaticle.typedb.client.jni.Concept res = role_type_get_supertype(nativeTransaction(transaction), nativeObject);
            if (res != null) return new RoleTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RoleTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return role_type_get_supertypes(nativeTransaction(transaction), nativeObject).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RoleTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RoleTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return role_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final RelationType getRelationType(TypeDBTransaction transaction) {
        try {
            return new RelationTypeImpl(role_type_get_relation_type(nativeTransaction(transaction), nativeObject));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationTypeImpl> getRelationTypes(TypeDBTransaction transaction) {
        try {
            return role_type_get_relation_types(nativeTransaction(transaction), nativeObject).stream().map(RelationTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<ThingTypeImpl> getPlayerTypes(TypeDBTransaction transaction) {
        return getPlayerTypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<ThingTypeImpl> getPlayerTypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return role_type_get_player_types(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(ThingTypeImpl::of);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationImpl> getRelationInstances(TypeDBTransaction transaction) {
        return getRelationInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RelationImpl> getRelationInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return role_type_get_relation_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(RelationImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<ThingImpl> getPlayerInstances(TypeDBTransaction transaction) {
        return getPlayerInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<ThingImpl> getPlayerInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return role_type_get_player_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(ThingImpl::of);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
