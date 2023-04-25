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
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.jni.Transitivity;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_create;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_instances;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_relates;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_relates_for_role_label;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_relates_overridden;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_set_relates;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_set_supertype;
import static com.vaticle.typedb.client.jni.typedb_client_jni.relation_type_unset_relates;

public class RelationTypeImpl extends ThingTypeImpl implements RelationType {
    public RelationTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public RelationTypeImpl asRelationType() {
        return this;
    }

    @Override
    public final RelationImpl create(TypeDBTransaction transaction) {
        return new RelationImpl(relation_type_create(((ConceptManagerImpl) transaction.concepts()).transaction, concept));
    }

    @Override
    public final void setSupertype(TypeDBTransaction transaction, RelationType relationType) {
        relation_type_set_supertype(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RelationTypeImpl) relationType).concept);
    }

    @Override
    public final Stream<RoleTypeImpl> getRelates(TypeDBTransaction transaction) {
        return relation_type_get_relates(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(RoleTypeImpl::new);
    }

    @Override
    public final Stream<RoleTypeImpl> getRelatesExplicit(TypeDBTransaction transaction) {
        return relation_type_get_relates(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(RoleTypeImpl::new);
    }

    @Override
    public final RoleTypeImpl getRelates(TypeDBTransaction transaction, String roleLabel) {
        com.vaticle.typedb.client.jni.Concept res = relation_type_get_relates_for_role_label(((ConceptManagerImpl) transaction.concepts()).transaction, concept, roleLabel);
        if (res != null) return new RoleTypeImpl(res);
        else return null;
    }

    @Nullable
    @Override
    public RoleType getRelatesOverridden(TypeDBTransaction transaction, RoleType roleType) {
        return getRelatesOverridden(transaction, roleType.getLabel().name());
    }

    @Override
    public final RoleTypeImpl getRelatesOverridden(TypeDBTransaction transaction, String roleLabel) {
        com.vaticle.typedb.client.jni.Concept res = relation_type_get_relates_overridden(((ConceptManagerImpl) transaction.concepts()).transaction, concept, roleLabel);
        if (res != null) return new RoleTypeImpl(res);
        else return null;
    }

    @Override
    public final void setRelates(TypeDBTransaction transaction, String roleLabel) {
        setRelates(transaction, roleLabel, (String) null);
    }

    @Override
    public void setRelates(TypeDBTransaction transaction, String roleLabel, RoleType overriddenType) {
        setRelates(transaction, roleLabel, overriddenType.getLabel().name());
    }

    @Override
    public final void setRelates(TypeDBTransaction transaction, String roleLabel, String overriddenLabel) {
        relation_type_set_relates(((ConceptManagerImpl) transaction.concepts()).transaction, concept, roleLabel, overriddenLabel);
    }

    @Override
    public void unsetRelates(TypeDBTransaction transaction, RoleType roleType) {
        relation_type_unset_relates(((ConceptManagerImpl) transaction.concepts()).transaction, concept, roleType.getLabel().name());
    }

    @Override
    public final void unsetRelates(TypeDBTransaction transaction, String roleLabel) {
        relation_type_unset_relates(((ConceptManagerImpl) transaction.concepts()).transaction, concept, roleLabel);
    }

    @Nullable
    @Override
    public RelationTypeImpl getSupertype(TypeDBTransaction transaction) {
        com.vaticle.typedb.client.jni.Concept res = relation_type_get_supertype(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
        if (res != null) return new RelationTypeImpl(res);
        else return null;
    }

    @Override
    public final Stream<RelationTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        return relation_type_get_supertypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept).stream().map(RelationTypeImpl::new);
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return relation_type_get_subtypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(RelationTypeImpl::new);
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypesExplicit(TypeDBTransaction transaction) {
        return relation_type_get_subtypes(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(RelationTypeImpl::new);
    }

    @Override
    public final Stream<RelationImpl> getInstances(TypeDBTransaction transaction) {
        return relation_type_get_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Transitive).stream().map(RelationImpl::new);
    }

    @Override
    public final Stream<RelationImpl> getInstancesExplicit(TypeDBTransaction transaction) {
        return relation_type_get_instances(((ConceptManagerImpl) transaction.concepts()).transaction, concept, Transitivity.Explicit).stream().map(RelationImpl::new);
    }
}
