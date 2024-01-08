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

#pragma once

#include "typedb/concept/concept.hpp"
namespace TypeDB {

/**
 * \brief Common super-type of RoleType & ThingType.
 */
class Type : public Concept {
public:
    // We don't make these virtual so we can emulate returning iterators to the subtypes.

    /**
     * Retrieves the most immediate supertype of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getSupertype(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptPtrFuture<Type> getSupertype(Transaction& transaction);

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
    ConceptIterable<Type> getSupertypes(Transaction& transaction);

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
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity.EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<Type> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Checks if the type is prevented from having data instances (i.e., <code>abstract</code>).
     *
     * <h3>Examples</h3>
     * <pre>
     * type.isAbstract();
     * </pre>
     */
    virtual bool isAbstract() = 0;

    /**
     * Retrieves the unique label of the type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.getLabel();
     * </pre>
     */
    virtual std::string getLabel() = 0;

    /**
     * Renames the label of the type. The new label must remain unique.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.setLabel(transaction, newLabel).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param label The new <code>Label</code> to be given to the type.
     */
    [[nodiscard]] virtual VoidFuture setLabel(Transaction& transaction, const std::string& newLabel) = 0;

    /**
     * Check if the type has been deleted
     *
     * <h3>Examples</h3>
     * <pre>
     * type.isDeleted(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    virtual BoolFuture isDeleted(Transaction& transaction) = 0;

    /**
     * Deletes this type from the database.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.deleteType(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) = 0;

protected:
    Type(ConceptType conceptType, _native::Concept* conceptNative);
    /// \private
    ConceptFutureWrapper* getSuperTypeFutureNative(Transaction& transaction);
    /// \private
    ConceptIteratorWrapper* getSubtypesIteratorNative(Transaction& transaction, Transitivity transitivity);
    /// \private
    ConceptIteratorWrapper* getSupertypesIteratorNative(Transaction& transaction);
};

}  // namespace TypeDB
