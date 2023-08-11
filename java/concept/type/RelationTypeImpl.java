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
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.thing.RelationImpl;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.relation_type_create;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_instances;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_relates;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_relates_for_role_label;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_relates_overridden;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_subtypes;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_get_supertypes;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_set_relates;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_set_supertype;
import static com.vaticle.typedb.client.jni.typedb_client.relation_type_unset_relates;

public class RelationTypeImpl extends ThingTypeImpl implements RelationType {
    public RelationTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    @Override
    public final RelationImpl create(TypeDBTransaction transaction) {
        try {
            return new RelationImpl(relation_type_create(nativeTransaction(transaction), nativeObject));
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void setSupertype(TypeDBTransaction transaction, RelationType relationType) {
        try {
            relation_type_set_supertype(nativeTransaction(transaction), nativeObject, ((RelationTypeImpl) relationType).nativeObject);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RoleTypeImpl> getRelates(TypeDBTransaction transaction) {
        return getRelates(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RoleTypeImpl> getRelates(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return relation_type_get_relates(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final RoleTypeImpl getRelates(TypeDBTransaction transaction, String roleLabel) {
        try {
            com.vaticle.typedb.client.jni.Concept res = relation_type_get_relates_for_role_label(nativeTransaction(transaction), nativeObject, roleLabel);
            if (res != null) return new RoleTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public RoleType getRelatesOverridden(TypeDBTransaction transaction, RoleType roleType) {
        return getRelatesOverridden(transaction, roleType.getLabel().name());
    }

    @Override
    public final RoleTypeImpl getRelatesOverridden(TypeDBTransaction transaction, String roleLabel) {
        try {
            com.vaticle.typedb.client.jni.Concept res = relation_type_get_relates_overridden(nativeTransaction(transaction), nativeObject, roleLabel);
            if (res != null) return new RoleTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
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
        try {
            relation_type_set_relates(nativeTransaction(transaction), nativeObject, roleLabel, overriddenLabel);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public void unsetRelates(TypeDBTransaction transaction, RoleType roleType) {
        try {
            relation_type_unset_relates(nativeTransaction(transaction), nativeObject, roleType.getLabel().name());
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final void unsetRelates(TypeDBTransaction transaction, String roleLabel) {
        try {
            relation_type_unset_relates(nativeTransaction(transaction), nativeObject, roleLabel);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Nullable
    @Override
    public RelationTypeImpl getSupertype(TypeDBTransaction transaction) {
        try {
            com.vaticle.typedb.client.jni.Concept res = relation_type_get_supertype(nativeTransaction(transaction), nativeObject);
            if (res != null) return new RelationTypeImpl(res);
            else return null;
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationTypeImpl> getSupertypes(TypeDBTransaction transaction) {
        try {
            return relation_type_get_supertypes(nativeTransaction(transaction), nativeObject).stream().map(RelationTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypes(TypeDBTransaction transaction) {
        return getSubtypes(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RelationTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return relation_type_get_subtypes(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(RelationTypeImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }

    @Override
    public final Stream<RelationImpl> getInstances(TypeDBTransaction transaction) {
        return getInstances(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RelationImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return relation_type_get_instances(nativeTransaction(transaction), nativeObject, transitivity.nativeObject).stream().map(RelationImpl::new);
        } catch (com.vaticle.typedb.client.jni.Error e) {
            throw new TypeDBClientException(e);
        }
    }
}
