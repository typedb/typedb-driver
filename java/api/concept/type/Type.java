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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.concept.Concept;
import com.vaticle.typedb.driver.common.Label;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import java.util.stream.Stream;

public interface Type extends Concept {
    /**
     * Retrieves the unique label of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getLabel();
     * </pre>
     */
    @CheckReturnValue
    Label getLabel();

    /**
     * Checks if the type is a root type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.isRoot();
     * </pre>
     */
    @CheckReturnValue
    boolean isRoot();

    /**
     * Checks if the type is prevented from having data instances (i.e., <code>abstract</code>).
     *
     * <h3>Examples</h3>
     * <pre>
     * type.isAbstract();
     * </pre>
     */
    @CheckReturnValue
    boolean isAbstract();

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default boolean isType() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @CheckReturnValue
    default Type asType() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default JsonObject toJSON() {
        return Json.object().add("label", getLabel().scopedName());
    }

    /**
     * Renames the label of the type. The new label must remain unique.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.setLabel(transaction, newLabel)
     * </pre>
     *
     * @param transaction The current transaction
     * @param label The new <code>Label</code> to be given to the type.
     */
    void setLabel(TypeDBTransaction transaction, String label);

    /**
     * Retrieves the most immediate supertype of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getSupertype(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @Nullable
    @CheckReturnValue
    Type getSupertype(TypeDBTransaction transaction);

    /**
     * Retrieves all supertypes of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getSupertypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    Stream<? extends Type> getSupertypes(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect subtypes of the type.
     * Equivalent to <code>getSubtypes(transaction, Transitivity.TRANSITIVE)</code>
     *
     * @see Type#getSubtypes(TypeDBTransaction, Transitivity)
     */
    @CheckReturnValue
    Stream<? extends Type> getSubtypes(TypeDBTransaction transaction);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getSubtypes(transaction);
     * type.getSubtypes(transaction, Transitivity.EXPLICIT);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect subtypes, <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    @CheckReturnValue
    Stream<? extends Type> getSubtypes(TypeDBTransaction transaction, Transitivity transitivity);

    /**
     * Deletes this type from the database.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.delete(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    void delete(TypeDBTransaction transaction);

    /**
     * Check if the concept has been deleted
     *
     * @param transaction The current transaction
     */
    @CheckReturnValue
    boolean isDeleted(TypeDBTransaction transaction);
}
