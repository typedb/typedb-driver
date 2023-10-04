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

package com.vaticle.typedb.driver.api.concept.type;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.thing.Thing;
import com.vaticle.typedb.driver.api.concept.value.Value;
import com.vaticle.typedb.driver.common.NativeObject;

import javax.annotation.CheckReturnValue;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_equals;
import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_is_key;
import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_is_unique;
import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_new_key;
import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_new_unique;
import static com.vaticle.typedb.driver.jni.typedb_driver.annotation_to_string;

public interface ThingType extends Type {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isThingType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default ThingType asThingType() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    ThingType getSupertype(TypeDBTransaction transaction);

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSupertypes(TypeDBTransaction transaction);

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypes(TypeDBTransaction transaction);

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    Stream<? extends ThingType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves all <code>Thing</code> objects that are instances of this <code>ThingType</code> or its subtypes.
     * Equivalent to <code>getInstances(transaction, Transitivity.TRANSITIVE)</code>
     *
     * @see ThingType#getInstances(TypeDBTransaction, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends Thing> getInstances(TypeDBTransaction transaction);

    /**
     * Retrieves <code>Thing</code> objects that are instances of this exact <code>ThingType</code>, OR
     * this <code>ThingType</code> and any of its subtypes
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getInstances(transaction);
     * thingType.getInstances(transaction, Transitivity.EXPLICIT);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.EXPLICIT</code> for direct instances only, <code>Transitivity.TRANSITIVE</code> to include instances of subtypes
     */
    @CheckReturnValue
    Stream<? extends Thing> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Set a <code>ThingType</code> to be abstract, meaning it cannot have instances.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setAbstract(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    void setAbstract(TypeDBTransaction transaction);

    /**
     * Set a <code>ThingType</code> to be non-abstract, meaning it can have instances.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetAbstract(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    void unsetAbstract(TypeDBTransaction transaction);

    /**
     * Allows the instances of this <code>ThingType</code> to play the given role.
     *
     * @see ThingType#setPlays(TypeDBTransaction, RoleType, RoleType)
     */
    void setPlays(TypeDBTransaction transaction, RoleType roleType);

    /**
     * Allows the instances of this <code>ThingType</code> to play the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setPlays(transaction, roleType)
     * thingType.setPlays(transaction, roleType, overriddenType)
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to be played by the instances of this type
     * @param overriddenType The role type that this role overrides, if applicable
     */
    void setPlays(TypeDBTransaction transaction, RoleType roleType, RoleType overriddenType);

    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
     * Optionally, overriding a previously declared ownership.
     * Optionally, adds annotations to the ownership.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setOwns(transaction, attributeType);
     * thingType.setOwns(transaction, attributeType, overriddenType, Collections.singleton(Annotation.key()));
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to be owned by the instances of this type.
     * @param overriddenType The <code>AttributeType</code> that this attribute ownership overrides, if applicable.
     * @param annotations Adds annotations to the ownership.
     */
    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType, Set<Annotation> annotations);

    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>,
     *
     * @see ThingType#setOwns(TypeDBTransaction, AttributeType, AttributeType, Set)
     */
     void setOwns(TypeDBTransaction transaction, AttributeType attributeType, AttributeType overriddenType);

    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
     *
     * @see ThingType#setOwns(TypeDBTransaction, AttributeType, AttributeType, Set)
     */
    void setOwns(TypeDBTransaction transaction, AttributeType attributeType, Set<Annotation> annotations);

    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
     *
     * @see ThingType#setOwns(TypeDBTransaction, AttributeType, AttributeType, Set)
     */
    void setOwns(TypeDBTransaction transaction, AttributeType attributeType);

    /**
     * Retrieves all direct and inherited roles that are allowed
     * to be played by the instances of this <code>ThingType</code>.
     *
     * @see ThingType#getPlays(TypeDBTransaction, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends RoleType> getPlays(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and inherited (or direct only) roles that are allowed
     * to be played by the instances of this <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getPlays(transaction);
     * thingType.getPlays(transaction, Transitivity.EXPLICIT);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity transitivity: <code>Transitivity.TRANSITIVE</code> for direct and indirect playing, <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    @CheckReturnValue
    Stream<? extends RoleType> getPlays(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the given
     * <code>role_type</code> for this <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getPlaysOverridden(transaction, roleType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The <code>RoleType</code> that overrides an inherited role
     */
    @CheckReturnValue
    RoleType getPlaysOverridden(TypeDBTransaction transaction, RoleType roleType);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Transitivity transitivity);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * @see ThingType#getOwns(TypeDBTransaction, Value.Type, Set, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Set<Annotation> annotations, Transitivity transitivity);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getOwns(transaction);
     * thingType.getOwns(transaction, valueType, Transitivity.EXPLICIT, Collections.singleton(Annotation.key()));
     * </pre>
     *
     * @param transaction The current transaction
     * @param valueType If specified, only attribute types of this <code>ValueType</code> will be retrieved.
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited ownership, <code>Transitivity.EXPLICIT</code> for direct ownership only
     * @param annotations Only retrieve attribute types owned with annotations.
     */
    @CheckReturnValue
    Stream<? extends AttributeType> getOwns(TypeDBTransaction transaction, Value.Type valueType, Set<Annotation> annotations, Transitivity transitivity);

    /**
     * Retrieves an <code>AttributeType</code>, ownership of which is overridden
     * for this <code>ThingType</code> by a given <code>attribute_type</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getOwnsOverridden(transaction, attributeType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> that overrides requested <code>AttributeType</code>
     */
    @CheckReturnValue
    AttributeType getOwnsOverridden(TypeDBTransaction transaction, AttributeType attributeType);

    /**
     * Disallows the instances of this <code>ThingType</code> from playing the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetPlays(transaction, roleType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to not be played by the instances of this type.
     */
    void unsetPlays(TypeDBTransaction transaction, RoleType roleType);

    /**
     * Disallows the instances of this <code>ThingType</code> from owning the given <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetOwns(transaction, attributeType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to not be owned by the type.
     */
    void unsetOwns(TypeDBTransaction transaction, AttributeType attributeType);

    /**
     * Produces a pattern for creating this <code>ThingType</code> in a <code>define</code> query.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getSyntax(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    String getSyntax(TypeDBTransaction transaction);

    /**
     * Annotation
     */
    class Annotation extends NativeObject<com.vaticle.typedb.driver.jni.Annotation> {
        private final int hash;

        private Annotation(com.vaticle.typedb.driver.jni.Annotation annotation) {
            super(annotation);
            this.hash = Objects.hash(isKey(), isUnique());
        }

        /**
         * Produces a <code>@key</code> annotation.
         *
         * <h3>Examples</h3>
         * <pre>
         * ThingType.Annotation.key();
         * </pre>
         */
        public static Annotation key() {
            return new Annotation(annotation_new_key());
        }

        /**
         * Produces a <code>@unique</code> annotation.
         *
         * <h3>Examples</h3>
         * <pre>
         * Annotation.unique();
         * </pre>
         */
        public static Annotation unique() {
            return new Annotation(annotation_new_unique());
        }

        /**
         * Checks if this <code>Annotation</code> is a <code>@key</code> annotation.
         *
         * <h3>Examples</h3>
         * <pre>
         * annotation.isKey();
         * </pre>
         */
        public boolean isKey() {
            return annotation_is_key(nativeObject);
        }

        /**
         * Checks if this <code>Annotation</code> is a <code>@unique</code> annotation.
         *
         * <h3>Examples</h3>
         * <pre>
         * annotation.isUnique();
         * </pre>
         */
        public boolean isUnique() {
            return annotation_is_unique(nativeObject);
        }

        /**
         * Retrieves a string representation of this <code>Annotation</code>.
         *
         * <h3>Examples</h3>
         * <pre>
         * annotation.toString();
         * </pre>
         */
        @Override
        public String toString() {
            return annotation_to_string(nativeObject);
        }

        /**
         * Checks if this <code>Annotation</code> is equal to another object.
         *
         * <h3>Examples</h3>
         * <pre>
         * annotation.equals(obj);
         * </pre>
         *
         * @param obj Object to compare with
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Annotation that = (Annotation) obj;
            return annotation_equals(this.nativeObject, that.nativeObject);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
