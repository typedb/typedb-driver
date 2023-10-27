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
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.type.RoleType;
import com.vaticle.typedb.driver.api.concept.type.ThingType;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.Label;
import com.vaticle.typedb.driver.common.NativeIterator;
import com.vaticle.typedb.driver.common.Promise;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.concept.thing.ThingImpl;
import com.vaticle.typeql.lang.common.TypeQLToken;

import javax.annotation.CheckReturnValue;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_attribute_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_entity_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_relation_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.concept_is_root_thing_type;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_delete;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_owns;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_owns_overridden;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_plays;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_plays_overridden;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_get_syntax;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_is_abstract;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_is_root;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_is_deleted;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_set_abstract;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_set_label;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_set_owns;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_set_plays;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_unset_abstract;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_unset_owns;
import static com.vaticle.typedb.driver.jni.typedb_driver.thing_type_unset_plays;
import static java.util.Collections.emptySet;

public abstract class ThingTypeImpl extends TypeImpl implements ThingType {
    ThingTypeImpl(com.vaticle.typedb.driver.jni.Concept concept) {
        super(concept);
    }

    public static ThingTypeImpl of(com.vaticle.typedb.driver.jni.Concept concept) {
        if (concept_is_entity_type(concept)) return new EntityTypeImpl(concept);
        else if (concept_is_relation_type(concept)) return new RelationTypeImpl(concept);
        else if (concept_is_attribute_type(concept)) return new AttributeTypeImpl(concept);
        else if (concept_is_root_thing_type(concept)) return new Root(concept);
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    @Override
    public final boolean isRoot() {
        try {
            return thing_type_is_root(nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public final boolean isAbstract() {
        try {
            return thing_type_is_abstract(nativeObject);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Label getLabel() {
        try {
            return Label.of(thing_type_get_label(nativeObject));
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    @CheckReturnValue
    public Promise<Void> delete(TypeDBTransaction transaction) {
        return new Promise<>(thing_type_delete(nativeTransaction(transaction), nativeObject));
    }

    @Override
    @CheckReturnValue
    public Promise<Boolean> isDeleted(TypeDBTransaction transaction) {
        return new Promise<>(thing_type_is_deleted(nativeTransaction(transaction), nativeObject));
    }

    @Override
    @CheckReturnValue
    public final Promise<Void> setLabel(TypeDBTransaction transaction, String newLabel) {
        return new Promise<>(thing_type_set_label(nativeTransaction(transaction), nativeObject, newLabel));
    }

    @Override
    public abstract Promise<? extends ThingTypeImpl> getSupertype(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSupertypes(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    @Override
    public abstract Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction);

    @Override
    public abstract Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    @Override
    public final Promise<Void> setAbstract(TypeDBTransaction transaction) {
        return new Promise<>(thing_type_set_abstract(nativeTransaction(transaction), nativeObject));
    }

    @Override
    public final Promise<Void> unsetAbstract(TypeDBTransaction transaction) {
        return new Promise<>(thing_type_unset_abstract(nativeTransaction(transaction), nativeObject));
    }

    @Override
    public final Promise<Void> setPlays(TypeDBTransaction transaction, RoleType roleType) {
        return new Promise<>(thing_type_set_plays(nativeTransaction(transaction), nativeObject, ((RoleTypeImpl) roleType).nativeObject, null));
    }

    @Override
    public final Promise<Void> setPlays(TypeDBTransaction transaction, RoleType roleType, RoleType overriddenRoleType) {
        return new Promise<>(thing_type_set_plays(nativeTransaction(transaction),
                nativeObject, ((RoleTypeImpl) roleType).nativeObject, ((RoleTypeImpl) overriddenRoleType).nativeObject));
    }

    @Override
    public Promise<Void> setOwns(TypeDBTransaction transaction, AttributeType attributeType) {
        return setOwns(transaction, attributeType, null, emptySet());
    }

    @Override
    public Promise<Void> setOwns(TypeDBTransaction transaction, AttributeType attributeType, Set<Annotation> annotations) {
        return setOwns(transaction, attributeType, null, annotations);
    }

    @Override
    public Promise<Void> setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType) {
        return setOwns(transaction, attributeType, overriddenType, emptySet());
    }

    @Override
    public final Promise<Void> setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType, Set<Annotation> annotations) {
        com.vaticle.typedb.driver.jni.Concept overriddenTypeNative = overriddenType != null ? ((AttributeTypeImpl) overriddenType).nativeObject : null;
        com.vaticle.typedb.driver.jni.Annotation[] annotationsArray = annotations.stream().map(anno -> anno.nativeObject).toArray(com.vaticle.typedb.driver.jni.Annotation[]::new);
        return new Promise<>(thing_type_set_owns(nativeTransaction(transaction), nativeObject, ((AttributeTypeImpl) attributeType).nativeObject, overriddenTypeNative, annotationsArray));
    }

    @Override
    public final Stream<RoleTypeImpl> getPlays(TypeDBTransaction transaction) {
        return getPlays(transaction, Transitivity.TRANSITIVE);
    }

    @Override
    public final Stream<RoleTypeImpl> getPlays(TypeDBTransaction transaction, Transitivity transitivity) {
        try {
            return new NativeIterator<>(thing_type_get_plays(nativeTransaction(transaction), nativeObject, transitivity.nativeObject)).stream().map(RoleTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Promise<RoleTypeImpl> getPlaysOverridden(TypeDBTransaction transaction, RoleType roleType) {
        return Promise.map(
                thing_type_get_plays_overridden(nativeTransaction(transaction), nativeObject, ((RoleTypeImpl) roleType).nativeObject),
                RoleTypeImpl::new
        );
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction) {
        return getOwns(transaction, Transitivity.TRANSITIVE, emptySet());
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Value.Type valueType) {
        return getOwns(transaction, valueType, Transitivity.TRANSITIVE, emptySet());
    }

    @Override
    public Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations) {
        return getOwns(transaction, Transitivity.TRANSITIVE, annotations);
    }

    @Override
    public final Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations) {
        return getOwns(transaction, valueType, Transitivity.TRANSITIVE, annotations);
    }

    @Override
    public Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Transitivity transitivity) {
        return getOwns(transaction, transitivity, emptySet());
    }

    @Override
    public Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity) {
        return getOwns(transaction, valueType, transitivity, emptySet());
    }

    @Override
    public Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations, Transitivity transitivity) {
        return getOwns(transaction, transitivity, annotations);
    }

    @Override
    public Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations, Transitivity transitivity) {
        return getOwns(transaction, valueType, transitivity, annotations);
    }

    private Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Transitivity transitivity, Set<Annotation> annotations) {
        return getOwns(transaction, null, transitivity, annotations);
    }

    private Stream<AttributeTypeImpl> getOwns(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity, Set<Annotation> annotations) {
        try {
            return new NativeIterator<>(thing_type_get_owns(nativeTransaction(transaction), nativeObject, valueType == null ? null : valueType.nativeObject, transitivity.nativeObject,
                    annotations.stream().map(anno -> anno.nativeObject).toArray(com.vaticle.typedb.driver.jni.Annotation[]::new)
            )).stream().map(AttributeTypeImpl::new);
        } catch (com.vaticle.typedb.driver.jni.Error e) {
            throw new TypeDBDriverException(e);
        }
    }

    @Override
    public Promise<AttributeTypeImpl> getOwnsOverridden(TypeDBTransaction transaction, AttributeType attributeType) {
        return Promise.map(thing_type_get_owns_overridden(nativeTransaction(transaction),
                nativeObject, ((AttributeTypeImpl) attributeType).nativeObject), AttributeTypeImpl::new);
    }

    @Override
    public final Promise<Void> unsetOwns(TypeDBTransaction transaction, AttributeType attributeType) {
        return new Promise<>(thing_type_unset_owns(nativeTransaction(transaction),
                nativeObject, ((AttributeTypeImpl) attributeType).nativeObject));
    }

    @Override
    public final Promise<Void> unsetPlays(TypeDBTransaction transaction, RoleType roleType) {
        return new Promise<>(thing_type_unset_plays(nativeTransaction(transaction), nativeObject, ((RoleTypeImpl) roleType).nativeObject));
    }

    @Override
    public final Promise<String> getSyntax(TypeDBTransaction transaction) {
        return new Promise<>(thing_type_get_syntax(nativeTransaction(transaction), nativeObject));
    }

    public static class Root extends ThingTypeImpl {
        public Root(com.vaticle.typedb.driver.jni.Concept concept) {
            super(concept);
        }

        @Override
        public final Label getLabel() {
            return Label.of(TypeQLToken.Type.THING.toString());
        }

        @Override
        public Promise<ThingTypeImpl> getSupertype(TypeDBTransaction transaction) {
            return new Promise<>(() -> null);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSupertypes(TypeDBTransaction transaction) {
            return Stream.of(this);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction) {
            return Stream.concat(
                    Stream.of(this),
                    Stream.concat(
                            transaction.concepts().getRootEntityType().getSubtypes(transaction),
                            Stream.concat(
                                    transaction.concepts().getRootRelationType().getSubtypes(transaction),
                                    transaction.concepts().getRootAttributeType().getSubtypes(transaction)
                            ))).map(tt -> (ThingTypeImpl) tt);
        }

        @Override
        public Stream<? extends ThingTypeImpl> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity) {
            return Stream.of(
                    transaction.concepts().getRootEntityType(),
                    transaction.concepts().getRootRelationType(),
                    transaction.concepts().getRootAttributeType()
                ).map(tt -> (ThingTypeImpl) tt);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction) {
            return Stream.concat(
                    transaction.concepts().getRootEntityType().getInstances(transaction),
                    Stream.concat(
                            transaction.concepts().getRootRelationType().getInstances(transaction),
                            transaction.concepts().getRootAttributeType().getInstances(transaction)
                    )).map(t -> (ThingImpl) t);
        }

        @Override
        public Stream<? extends ThingImpl> getInstances(TypeDBTransaction transaction, Transitivity transitivity) {
            return Stream.empty();
        }
    }
}
