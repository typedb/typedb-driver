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
import com.vaticle.typedb.client.api.concept.type.AttributeType;
import com.vaticle.typedb.client.api.concept.type.RoleType;
import com.vaticle.typedb.client.api.concept.type.ThingType;
import com.vaticle.typedb.client.common.Label;
import com.vaticle.typedb.client.concept.ConceptManagerImpl;
import com.vaticle.typedb.client.concept.thing.ThingImpl;
import com.vaticle.typedb.client.jni.Transitivity;
import com.vaticle.typedb.client.jni.ValueType;
import com.vaticle.typeql.lang.common.TypeQLToken;

import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.client.jni.typedb_client.concept_is_attribute_type;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_entity_type;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_relation_type;
import static com.vaticle.typedb.client.jni.typedb_client.concept_is_root_thing_type;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_delete;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_label;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_owns;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_owns_overridden;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_plays;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_plays_overridden;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_get_syntax;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_is_abstract;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_is_deleted;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_set_abstract;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_set_label;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_set_owns;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_set_plays;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_unset_abstract;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_unset_owns;
import static com.vaticle.typedb.client.jni.typedb_client.thing_type_unset_plays;
import static com.vaticle.typedb.common.collection.Collections.set;
import static java.util.Collections.emptySet;

public abstract class ThingTypeImpl extends TypeImpl implements ThingType {

    ThingTypeImpl(com.vaticle.typedb.client.jni.Concept concept) {
        super(concept);
    }

    public static ThingTypeImpl of(com.vaticle.typedb.client.jni.Concept concept) {
        if (concept_is_entity_type(concept)) return new EntityTypeImpl(concept);
        else if (concept_is_relation_type(concept)) return new RelationTypeImpl(concept);
        else if (concept_is_attribute_type(concept)) return new AttributeTypeImpl(concept);
        else if (concept_is_root_thing_type(concept)) return new Root(concept);
        return null; // FIXME throw
    }

    @Override
    public final boolean isRoot() {
        return false; // FIXME
//        return thing_type_is_root(concept);
    }

    @Override
    public final boolean isAbstract() {
        return thing_type_is_abstract(concept);
    }

    @Override
    public Label getLabel() {
        return Label.of(thing_type_get_label(concept));
    }

    @Override
    public void delete(TypeDBTransaction transaction) {
        thing_type_delete(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public boolean isDeleted(TypeDBTransaction transaction) {
        return thing_type_is_deleted(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public final void setLabel(TypeDBTransaction transaction, String newLabel) {
        thing_type_set_label(((ConceptManagerImpl) transaction.concepts()).transaction, concept, newLabel);
    }

    @Override
    public abstract ThingTypeImpl getSupertype(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSupertypes(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSubtypesExplicit(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingImpl> getInstancesExplicit(TypeDBTransaction transaction);

    @Override
    public final void setAbstract(TypeDBTransaction transaction) {
        thing_type_set_abstract(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public final void unsetAbstract(TypeDBTransaction transaction) {
        thing_type_unset_abstract(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    @Override
    public final void setPlays(TypeDBTransaction transaction, RoleType roleType) {
        thing_type_set_plays(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept, null);
    }

    @Override
    public final void setPlays(TypeDBTransaction transaction, RoleType roleType, RoleType overriddenRoleType) {
        thing_type_set_plays(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept, ((RoleTypeImpl) overriddenRoleType).concept);
    }

    @Override
    public void setOwns(TypeDBTransaction transaction, AttributeType attributeType) {
        setOwns(transaction, attributeType, null, emptySet());
    }

    @Override
    public void setOwns(TypeDBTransaction transaction, AttributeType attributeType, Set<TypeQLToken.Annotation> annotations) {
        setOwns(transaction, attributeType, null, annotations);
    }

    @Override
    public void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType) {
        setOwns(transaction, attributeType, overriddenType, emptySet());
    }

    @Override
    public final void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType, Set<TypeQLToken.Annotation> annotations) {
        thing_type_set_owns(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((AttributeTypeImpl) attributeType).concept, overriddenType != null ? ((AttributeTypeImpl) overriddenType).concept : null, annotations.stream().map(TypeQLToken.Annotation::toString).toArray(String[]::new));
    }

    @Override
    public final Stream<RoleTypeImpl> getPlays(TypeDBTransaction transaction) {
        return getPlays(transaction, Transitivity.Transitive);
    }

    @Override
    public final Stream<RoleTypeImpl> getPlaysExplicit(TypeDBTransaction transaction) {
        return getPlays(transaction, Transitivity.Explicit);
    }

    private Stream<RoleTypeImpl> getPlays(TypeDBTransaction transaction, Transitivity transitivity) {
        return thing_type_get_plays(((ConceptManagerImpl) transaction.concepts()).transaction, concept, transitivity).stream().map(RoleTypeImpl::new);
    }

    @Override
    public RoleTypeImpl getPlaysOverridden(TypeDBTransaction transaction, RoleType roleType) {
        com.vaticle.typedb.client.jni.Concept res = thing_type_get_plays_overridden(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept);
        if (res != null) return new RoleTypeImpl(res);
        else return null;
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction) {
        return getOwns(transaction, Transitivity.Transitive, set());
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, ValueType valueType) {
        return getOwns(transaction, valueType, Transitivity.Transitive, set());
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Set<TypeQLToken.Annotation> annotations) {
        return getOwns(transaction, Transitivity.Transitive, annotations);
    }

    @Override
    public final Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, ValueType valueType, Set<TypeQLToken.Annotation> annotations) {
        return getOwns(transaction, valueType, Transitivity.Transitive, annotations);
    }

    @Override
    public Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction) {
        return getOwns(transaction, Transitivity.Explicit, set());
    }

    @Override
    public Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, ValueType valueType) {
        return getOwns(transaction, valueType, Transitivity.Explicit, set());
    }

    @Override
    public Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, Set<TypeQLToken.Annotation> annotations) {
        return getOwns(transaction, Transitivity.Explicit, annotations);
    }

    @Override
    public Stream<? extends AttributeType> getOwnsExplicit(TypeDBTransaction transaction, ValueType valueType, Set<TypeQLToken.Annotation> annotations) {
        return getOwns(transaction, valueType, Transitivity.Explicit, annotations);
    }

    private Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Transitivity transitivity, Set<TypeQLToken.Annotation> annotations) {
        return getOwns(transaction, null, transitivity, annotations);
    }

    private Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, ValueType valueType, Transitivity transitivity, Set<TypeQLToken.Annotation> annotations) {
        return thing_type_get_owns(((ConceptManagerImpl) transaction.concepts()).transaction, concept, valueType, transitivity, annotations.stream().map(TypeQLToken.Annotation::toString).toArray(String[]::new)).stream().map(AttributeTypeImpl::new);
    }

    @Override
    public AttributeTypeImpl getOwnsOverridden(TypeDBTransaction transaction, AttributeType attributeType) {
        com.vaticle.typedb.client.jni.Concept res = thing_type_get_owns_overridden(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((AttributeTypeImpl) attributeType).concept);
        if (res != null) return new AttributeTypeImpl(res);
        else return null;
    }

    @Override
    public final void unsetOwns(TypeDBTransaction transaction, AttributeType attributeType) {
        thing_type_unset_owns(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((AttributeTypeImpl) attributeType).concept);
    }

    @Override
    public final void unsetPlays(TypeDBTransaction transaction, RoleType roleType) {
        thing_type_unset_plays(((ConceptManagerImpl) transaction.concepts()).transaction, concept, ((RoleTypeImpl) roleType).concept);
    }

    @Override
    public final String getSyntax(TypeDBTransaction transaction) {
        return thing_type_get_syntax(((ConceptManagerImpl) transaction.concepts()).transaction, concept);
    }

    public static class Root extends ThingTypeImpl {
        public Root(com.vaticle.typedb.client.jni.Concept concept) {
            super(concept);
        }

        @Override
        public final Label getLabel() {
            return Label.of(TypeQLToken.Type.THING.toString());
        }

        @Override
        public boolean isDeleted(TypeDBTransaction transaction) {
            return false;
        }

        @Override
        public ThingTypeImpl getSupertype(TypeDBTransaction transaction) {
            return null;
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSupertypes(TypeDBTransaction transaction) {
            return null;
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction) {
            return null;
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypesExplicit(TypeDBTransaction transaction) {
            return null;
        }

        @Override
        public Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction) {
            return null;
        }

        @Override
        public Stream<? extends ThingImpl> getInstancesExplicit(TypeDBTransaction transaction) {
            return null;
        }
    }
}
