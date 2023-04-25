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
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.client.jni.Transitivity;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_delete;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_name;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_player_instances;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_player_types;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_relation_instances;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_relation_type;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_relation_types;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_scope;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_is_abstract;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_is_deleted;
import static com.vaticle.typedb.client.jni.typedb_client_jni.role_type_set_label;

public class RoleTypeImpl extends TypeImpl implements RoleType {
    public RoleTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public RoleTypeImpl asRoleType() {
        return this;
    }

    @Override
    public final boolean isAbstract() {
        return role_type_is_abstract(concept);
    }

    @Override
    public Label getLabel() {
        return Label.of(role_type_get_scope(concept), role_type_get_name(concept));
    }

    @Override
    public final void delete(TypeDBTransaction transaction) {
        role_type_delete(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public final boolean isDeleted(TypeDBTransaction transaction) {
        return role_type_is_deleted(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public final void setLabel(TypeDBTransaction transaction, String newLabel) {
        role_type_set_label(((ConceptManagerImpl) transaction.concepts()).transaction, concept, newLabel);
    }

    @Nullable
    @Override
    public RoleTypeImpl getSupertype(TypeDBTransaction transaction) {
        com.vaticle.typedb.client.jni.Concept res = role_type_get_supertype(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
        if (res != null) return new RoleTypeImpl(res);
        else return null;
    }

    @Override
    public final Stream<RoleTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        return role_type_get_supertypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept).stream().map(RoleTypeImpl::new);
    }

    @Override
    public final Stream<RoleTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return role_type_get_subtypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(RoleTypeImpl::new);
    }

    @Override
    public final Stream<RoleTypeImpl> getSubtypesExplicit(TypeDBTransaction transaction) {
        return role_type_get_subtypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(RoleTypeImpl::new);
    }

    @Nullable
    @Override
    public final RelationType getRelationType(TypeDBTransaction transaction) {
        return new RelationTypeImpl(role_type_get_relation_type(((ConceptManagerImpl) transaction.concepts()).transaction, concept));
    }

    @Override
    public final Stream<RelationTypeImpl> getRelationTypes(TypeDBTransaction transaction) {
        return role_type_get_relation_types(((ConceptManagerImpl) transaction.concepts()).transaction, concept).stream().map(RelationTypeImpl::new);
    }

    @Override
    public final Stream<ThingTypeImpl> getPlayerTypes(TypeDBTransaction transaction) {
        return role_type_get_player_types(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(ThingTypeImpl::of);
    }

    @Override
    public final Stream<ThingTypeImpl> getPlayerTypesExplicit(TypeDBTransaction transaction) {
        return role_type_get_player_types(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(ThingTypeImpl::of);
    }

    @Override
    public final Stream<RelationImpl> getRelationInstances(TypeDBTransaction transaction) {
        return role_type_get_relation_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(RelationImpl::new);
    }

    @Override
    public final Stream<RelationImpl> getRelationInstancesExplicit(TypeDBTransaction transaction) {
        return role_type_get_relation_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(RelationImpl::new);
    }

    @Override
    public final Stream<ThingImpl> getPlayerInstances(TypeDBTransaction transaction) {
        return role_type_get_player_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(ThingImpl::of);
    }

    @Override
    public final Stream<ThingImpl> getPlayerInstancesExplicit(TypeDBTransaction transaction) {
        return role_type_get_player_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(ThingImpl::of);
    }
}
