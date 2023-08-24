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

package com.vaticle.typedb.client.api.concept;

import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.client.api.TypeDBSession;
import com.vaticle.typedb.client.api.TypeDBTransaction;
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
import com.vaticle.typedb.client.api.concept.value.Value;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.client.jni.Transitivity;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.common.util.Objects.className;

public interface Concept {
    @CheckReturnValue
    default boolean isType() {
        return false;
    }

    @CheckReturnValue
    default boolean isThingType() {
        return false;
    }

    @CheckReturnValue
    default boolean isEntityType() {
        return false;
    }

    @CheckReturnValue
    default boolean isAttributeType() {
        return false;
    }

    @CheckReturnValue
    default boolean isRelationType() {
        return false;
    }

    @CheckReturnValue
    default boolean isRoleType() {
        return false;
    }

    @CheckReturnValue
    default boolean isThing() {
        return false;
    }

    @CheckReturnValue
    default boolean isEntity() {
        return false;
    }

    @CheckReturnValue
    default boolean isRelation() {
        return false;
    }

    @CheckReturnValue
    default boolean isAttribute() {
        return false;
    }

    @CheckReturnValue
    default boolean isValue() {
        return false;
    }

    default Type asType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Type.class));
    }

    default ThingType asThingType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
    }

    default EntityType asEntityType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
    }

    default RelationType asRelationType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
    }

    default AttributeType asAttributeType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
    }

    default RoleType asRoleType() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
    }

    default Thing asThing() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
    }

    default Entity asEntity() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
    }

    default Relation asRelation() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
    }

    default Attribute asAttribute() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
    }

    default Value asValue() {
        throw new TypeDBClientException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.class));
    }

    @CheckReturnValue
    JsonObject toJSON();

    enum Transitivity {
        TRANSITIVE(com.vaticle.typedb.client.jni.Transitivity.Transitive),
        EXPLICIT(com.vaticle.typedb.client.jni.Transitivity.Explicit);

        public final com.vaticle.typedb.client.jni.Transitivity nativeObject;

        Transitivity(com.vaticle.typedb.client.jni.Transitivity nativeObject) {
            this.nativeObject = nativeObject;
        }

        public static Transitivity of(com.vaticle.typedb.client.jni.Transitivity transitivity) {
            for (Transitivity value : Transitivity.values()) {
                if (value.nativeObject == transitivity) {
                    return value;
                }
            }
            throw new TypeDBClientException(UNEXPECTED_NATIVE_VALUE);
        }
    }
}
