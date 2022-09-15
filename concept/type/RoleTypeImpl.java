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
import com.vaticle.typedb.client.common.rpc.RequestBuilder;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.protocol.ConceptProto;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.RoleType.getPlayerInstancesExplicitReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.RoleType.getPlayerInstancesReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.RoleType.getPlayerTypesReq;
import static com.vaticle.typedb.client.common.rpc.RequestBuilder.Type.RoleType.getRelationTypesReq;

public class RoleTypeImpl extends TypeImpl implements RoleType {

    RoleTypeImpl(Label label, boolean root) {
        super(label, root);
    }

    public static RoleTypeImpl of(ConceptProto.Type typeProto) {
        return new RoleTypeImpl(Label.of(typeProto.getScope(), typeProto.getLabel()), typeProto.getRoot());
    }

    public static ConceptProto.Type protoRoleType(RoleType roleType) {
        return RequestBuilder.Type.RoleType.protoRoleType(roleType.getLabel(), TypeImpl.encoding(roleType));
    }

    @Override
    public RoleTypeImpl.Remote asRemote(TypeDBTransaction transaction) {
        return new RoleTypeImpl.Remote(transaction, getLabel(), isRoot());
    }

    @Override
    public RoleTypeImpl asRoleType() {
        return this;
    }

    public static class Remote extends TypeImpl.Remote implements RoleType.Remote {

        public Remote(TypeDBTransaction transaction, Label label, boolean isRoot) {
            super(transaction, label, isRoot);
        }

        @Nullable
        @Override
        public RoleTypeImpl getSupertype() {
            TypeImpl supertype = super.getSupertype();
            return supertype != null ? supertype.asRoleType() : null;
        }

        @Override
        public final Stream<RoleTypeImpl> getSupertypes() {
            return super.getSupertypes().map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RoleTypeImpl> getSubtypes() {
            return super.getSubtypes().map(TypeImpl::asRoleType);
        }

        @Override
        public final Stream<RoleTypeImpl> getSubtypesExplicit() {
            return super.getSubtypesExplicit().map(TypeImpl::asRoleType);
        }

        @Override
        public RoleType.Remote asRemote(TypeDBTransaction transaction) {
            return new RoleTypeImpl.Remote(transaction, getLabel(), isRoot());
        }

        @Nullable
        @Override
        public final RelationType getRelationType() {
            assert getLabel().scope().isPresent();
            return transactionExt.concepts().getRelationType(getLabel().scope().get());
        }

        @Override
        public final Stream<RelationTypeImpl> getRelationTypes() {
            return stream(getRelationTypesReq(getLabel()))
                    .flatMap(rp -> rp.getRoleTypeGetRelationTypesResPart().getRelationTypesList().stream())
                    .map(RelationTypeImpl::of);
        }

        @Override
        public final Stream<ThingTypeImpl> getPlayerTypes() {
            return stream(getPlayerTypesReq(getLabel()))
                    .flatMap(rp -> rp.getRoleTypeGetPlayerTypesResPart().getThingTypesList().stream())
                    .map(ThingTypeImpl::of);
        }

        @Override
        public final Stream<ThingImpl> getPlayerInstances() {
            return stream(getPlayerInstancesReq(getLabel()))
                    .flatMap(rp -> rp.getRoleTypeGetPlayerInstancesResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public final Stream<ThingImpl> getPlayerInstancesExplicit() {
            return stream(getPlayerInstancesExplicitReq(getLabel()))
                    .flatMap(rp -> rp.getRoleTypeGetPlayerInstancesExplicitResPart().getThingsList().stream())
                    .map(ThingImpl::of);
        }

        @Override
        public final boolean isDeleted() {
            return getRelationType() == null ||
                    getRelationType().asRemote(transactionExt).getRelates(getLabel().name()) == null;
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            return this;
        }
    }
}
