/*
 * Copyright (C) 2021 Vaticle
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

package com.vaticle.typedb.client.concept;

import com.vaticle.typedb.client.api.concept.Concept;
import com.vaticle.typedb.client.api.concept.thing.Attribute;
import com.vaticle.typedb.client.api.concept.thing.Entity;
import com.vaticle.typedb.client.api.concept.thing.Relation;
import com.vaticle.typedb.client.api.concept.thing.Thing;
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.EntityType;
import com.vaticle.typedb.client.api.concept.type.RelationType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.api.concept.type.Type;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.concept.thing.AttributeImpl;
import com.vaticle.typedb.client.concept.thing.EntityImpl;
import com.vaticle.typedb.client.concept.thing.RelationImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.client.concept.type.AttributeTypeImpl;
import com.vaticle.typedb.client.concept.type.EntityTypeImpl;
import com.vaticle.typedb.client.concept.type.RelationTypeImpl;
import com.vaticle.typedb.client.concept.type.RoleTypeImpl;
import com.vaticle.typedb.client.concept.type.ThingTypeImpl;
import com.vaticle.typedb.client.concept.type.TypeImpl;
import com.vaticle.typedb.protocol.ConceptProto;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.vaticle.typedb.common.util.Objects.className;

public abstract class ConceptImpl implements Concept {

    public static Concept of(ConceptProto.Concept protoConcept) {
        if (protoConcept.hasThing()) return ThingImpl.of(protoConcept.getThing());
        else return TypeImpl.of(protoConcept.getType());
    }

    @Override
    public final boolean isRemote() {
        return false;
    }

    @Override
    public TypeImpl asType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Type.class));
    }

    @Override
    public ThingTypeImpl asThingType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
    }

    @Override
    public EntityTypeImpl asEntityType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
    }

    @Override
    public AttributeTypeImpl asAttributeType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
    }

    @Override
    public RelationTypeImpl asRelationType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
    }

    @Override
    public RoleTypeImpl asRoleType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
    }

    @Override
    public ThingImpl asThing() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
    }

    @Override
    public EntityImpl asEntity() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
    }

    @Override
    public AttributeImpl<?> asAttribute() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
    }

    @Override
    public RelationImpl asRelation() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
    }

    public abstract static class Remote implements Concept.Remote {

        @Override
        public final boolean isRemote() {
            return true;
        }

        @Override
        public TypeImpl.Remote asType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Type.class));
        }

        @Override
        public ThingTypeImpl.Remote asThingType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
        }

        @Override
        public EntityTypeImpl.Remote asEntityType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
        }

        @Override
        public RelationTypeImpl.Remote asRelationType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
        }

        @Override
        public AttributeTypeImpl.Remote asAttributeType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
        }

        @Override
        public RoleTypeImpl.Remote asRoleType() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
        }

        @Override
        public ThingImpl.Remote asThing() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
        }

        @Override
        public EntityImpl.Remote asEntity() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
        }

        @Override
        public RelationImpl.Remote asRelation() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
        }

        @Override
        public AttributeImpl.Remote<?> asAttribute() {
            throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
        }
    }
}
