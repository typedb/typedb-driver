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

package com.vaticle.typedb.driver.api.concept;

import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.driver.api.TypeDBSession;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.thing.Attribute;
import com.vaticle.typedb.driver.api.concept.thing.Entity;
import com.vaticle.typedb.driver.api.concept.thing.Relation;
import com.vaticle.typedb.driver.api.concept.thing.Thing;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import com.vaticle.typedb.driver.api.concept.type.EntityType;
import com.vaticle.typedb.driver.api.concept.type.RelationType;
import com.vaticle.typedb.driver.api.concept.type.RoleType;
import com.vaticle.typedb.driver.api.concept.type.ThingType;
import com.vaticle.typedb.driver.api.concept.type.Type;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.exception.TypeDBDriverException;
import com.vaticle.typedb.driver.jni.Transitivity;

import javax.annotation.CheckReturnValue;

import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Concept.INVALID_CONCEPT_CASTING;
import static com.vaticle.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;
import static com.vaticle.typedb.common.util.Objects.className;

public interface Concept {
    /**
     * Checks if the concept is a <code>Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isThingType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isThingType() {
        return false;
    }

    /**
     * Checks if the concept is an <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntityType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isEntityType() {
        return false;
    }

    /**
     * Checks if the concept is an <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isAttributeType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isAttributeType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelationType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRelationType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRoleType();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRoleType() {
        return false;
    }

    /**
     * Checks if the concept is a <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isThing();
     * </pre>
     */
    @CheckReturnValue
    default boolean isThing() {
        return false;
    }

    /**
     * Checks if the concept is an <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isEntity();
     * </pre>
     */
    @CheckReturnValue
    default boolean isEntity() {
        return false;
    }

    /**
     * Checks if the concept is a <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isRelation();
     * </pre>
     */
    @CheckReturnValue
    default boolean isRelation() {
        return false;
    }

    /**
     * Checks if the concept is an <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isAttribute();
     * </pre>
     */
    @CheckReturnValue
    default boolean isAttribute() {
        return false;
    }

    /**
     * Checks if the concept is a <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.isValue();
     * </pre>
     */
    @CheckReturnValue
    default boolean isValue() {
        return false;
    }

    /**
     * Casts the concept to <code>Type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asType();
     * </pre>
     */
    default Type asType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Type.class));
    }

    /**
     * Casts the concept to <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asThingType();
     * </pre>
     */
    default ThingType asThingType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(ThingType.class));
    }

    /**
     * Casts the concept to <code>EntityType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntityType();
     * </pre>
     */
    default EntityType asEntityType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(EntityType.class));
    }

    /**
     * Casts the concept to <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelationType();
     * </pre>
     */
    default RelationType asRelationType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RelationType.class));
    }

    /**
     * Casts the concept to <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asAttributeType();
     * </pre>
     */
    default AttributeType asAttributeType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(AttributeType.class));
    }

    /**
     * Casts the concept to <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRoleType();
     * </pre>
     */
    default RoleType asRoleType() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(RoleType.class));
    }

    /**
     * Casts the concept to <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asThing();
     * </pre>
     */
    default Thing asThing() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Thing.class));
    }

    /**
     * Casts the concept to <code>Entity</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asEntity();
     * </pre>
     */
    default Entity asEntity() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Entity.class));
    }

    /**
     * Casts the concept to <code>Relation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asRelation();
     * </pre>
     */
    default Relation asRelation() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Relation.class));
    }

    /**
     * Casts the concept to <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asAttribute();
     * </pre>
     */
    default Attribute asAttribute() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Attribute.class));
    }

    /**
     * Casts the concept to <code>Value</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.asValue();
     * </pre>
     */
    default Value asValue() {
        throw new TypeDBDriverException(INVALID_CONCEPT_CASTING, className(this.getClass()), className(Value.class));
    }

    /**
     * Retrieves the concept as JSON.
     *
     * <h3>Examples</h3>
     * <pre>
     * concept.toJSON();
     * </pre>
     */
    @CheckReturnValue
    JsonObject toJSON();

    /**
     * This class is used for specifying whether we need explicit or transitive subtyping, instances, etc.
     *
     * <h3>Examples</h3>
     * <pre>
     * attributeType.getOwners(transaction, annotation, Concept.Transitivity.EXPLICIT);
     * </pre>
     */
    enum Transitivity {
        TRANSITIVE(com.vaticle.typedb.driver.jni.Transitivity.Transitive),
        EXPLICIT(com.vaticle.typedb.driver.jni.Transitivity.Explicit);

        public final com.vaticle.typedb.driver.jni.Transitivity nativeObject;

        Transitivity(com.vaticle.typedb.driver.jni.Transitivity nativeObject) {
            this.nativeObject = nativeObject;
        }

        public static Transitivity of(com.vaticle.typedb.driver.jni.Transitivity transitivity) {
            for (Transitivity value : Transitivity.values()) {
                if (value.nativeObject == transitivity) {
                    return value;
                }
            }
            throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
        }
    }
}
