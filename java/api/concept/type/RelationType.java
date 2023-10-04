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
import com.vaticle.typedb.driver.api.concept.thing.Relation;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Relation types (or subtypes of the relation root type) represent relationships between types. Relation types have roles.
 * Other types can play roles in relations if it’s mentioned in their definition.
 * A relation type must specify at least one role.
 */
public interface RelationType extends ThingType {
    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isRelationType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default RelationType asRelationType() {
        return this;
    }

    /**
     * Creates and returns an instance of this <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.create(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Relation create(TypeDBTransaction transaction);

    /**
     * Retrieves all <code>Relation</code> objects that are instances of this <code>RelationType</code> or its subtypes.
     * Equivalent to <code>getInstances(transaction, Transitivity.TRANSITIVE)</code>
     *
     * @see RelationType#getInstances(TypeDBTransaction, Transitivity)
     */
    @Override
    @CheckReturnValue
    Stream<? extends Relation> getInstances(TypeDBTransaction transaction);


    /**
     * Retrieves <code>Relation</code>s that are instances of this exact <code>RelationType</code>, OR
     * this <code>RelationType</code> and any of its subtypes.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getInstances(transaction, transitivity)
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect instances, <code>Transitivity.EXPLICIT</code> for direct relates only
     */
    @Override
    @CheckReturnValue
    Stream<? extends Relation> getInstances(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
     *
     * @see RelationType#getRelates(TypeDBTransaction, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends RoleType> getRelates(TypeDBTransaction transaction);

    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelates(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and inherited relates, <code>Transitivity.EXPLICIT</code> for direct relates only
     */
    @CheckReturnValue
    Stream<? extends RoleType> getRelates(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
     * If <code>role_label</code> is given, returns a corresponding <code>RoleType</code> or <code>null</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelates(transaction, roleLabel);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel Label of the role we wish to retrieve
     */
    @Nullable
    @CheckReturnValue
    RoleType getRelates(TypeDBTransaction transaction, String roleLabel);

    @Nullable
    @CheckReturnValue
    RoleType getRelatesOverridden(TypeDBTransaction transaction, RoleType roleType);

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelatesOverridden(transaction, roleLabel);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel Label of the role that overrides an inherited role
     */
    @Nullable
    @CheckReturnValue
    RoleType getRelatesOverridden(TypeDBTransaction transaction, String roleLabel);

    /**
     * Sets the new role that this <code>RelationType</code> relates to.
     *
     * @see RelationType#setRelates(TypeDBTransaction, String, String)
     */
    void setRelates(TypeDBTransaction transaction, String roleLabel);

    /**
     * Sets the new role that this <code>RelationType</code> relates to.
     *
     * @see RelationType#setRelates(TypeDBTransaction, String, String)
     */
    void setRelates(TypeDBTransaction transaction, String roleLabel, RoleType overriddenType);

    /**
     * Sets the new role that this <code>RelationType</code> relates to.
     * If we are setting an overriding type this way, we have to also pass the overridden type as a second argument.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.setRelates(transaction, roleLabel);
     * relationType.setRelates(transaction, roleLabel, overriddenLabel);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel The new role for the <code>RelationType</code> to relate to
     * @param overriddenLabel The label being overridden, if applicable
     */
    void setRelates(TypeDBTransaction transaction, String roleLabel, String overriddenLabel);

    /**
     * Disallows this <code>RelationType</code> from relating to the given role.
     *
     * @see RelationType#unsetRelates(TypeDBTransaction, String)
     */
    void unsetRelates(TypeDBTransaction transaction, RoleType roleType);

    /**
     * Disallows this <code>RelationType</code> from relating to the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.unsetRelates(transaction, roleLabel);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel The role to not relate to the relation type.
     */
    void unsetRelates(TypeDBTransaction transaction, String roleLabel);

    /**
     * Retrieves all direct and indirect subtypes of the <code>RelationType</code>.
     * Equivalent to <code>getSubtypes(transaction, Transitivity.TRANSITIVE)</code>
     * 
     * @see RelationType#getSubtypes(TypeDBTransaction, Transitivity)
     */
     @Override
    @CheckReturnValue
    Stream<? extends RelationType> getSubtypes(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @Override
    @CheckReturnValue
    Stream<? extends RelationType> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Sets the supplied <code>RelationType</code> as the supertype of the current <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.setSupertype(transaction, superRelationType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param superRelationType The <code>RelationType</code> to set as the supertype of this <code>RelationType</code>
     */
    void setSupertype(TypeDBTransaction transaction, RelationType superRelationType);
}
