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

#include "typedb/concept/type/type.hpp"

namespace TypeDB {

/**
 * \brief Defines a role an instance can play in a Relation.
 *
 * Roles are special internal types used by relations. We can not create an instance of a role in a database.
 * But we can set an instance of another type (role player) to play a role in a particular instance of a relation type.
 * Roles allow a schema to enforce logical constraints on types of role players.
 */
class RoleType : public Type {
public:
    /**
     * Returns the name of this role type's label.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.getName();
     * </pre>
     */
    std::string getName();

    /**
     * Returns the scope part of this role type's label.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.getScope();
     * </pre>
     */
    std::string getScope();

    // Inherited

    /// \copydoc Type::getLabel()
    virtual std::string getLabel() override;

    /// \copydoc Type::setLabel(Transaction&, const std::string&)
    [[nodiscard]] virtual VoidFuture setLabel(Transaction& transaction, const std::string& newLabel) override;

    /**
     * Checks if the type is a root type.
     *
     * <h3>Examples</h3>
     * <pre>
     * type.isRoot();
     * </pre>
     */
    bool isRoot();

    /// \copydoc Type::isAbstract()
    virtual bool isAbstract() override;

    /// \copydoc Type::deleteType(Transaction&)
    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) override;

    /// \copydoc Type::isDeleted(Transaction&)
    [[nodiscard]] virtual BoolFuture isDeleted(Transaction& transaction) override;

    // Mimic overriding from Type
    /**
     * Retrieves the most immediate supertype of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSupertype(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptPtrFuture<RoleType> getSupertype(Transaction& transaction);

    /**
     * Retrieves all supertypes of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSupertypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<RoleType> getSupertypes(Transaction& transaction);

    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>RoleType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<RoleType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves the <code>RelationType</code> that this role is directly related to.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationType(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptPtrFuture<RelationType> getRelationType(Transaction& transaction);

    /**
     * Retrieves <code>RelationType</code>s that this role is related to (directly or indirectly).
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationTypes(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<RelationType> getRelationTypes(Transaction& transaction);

    /**
     * Retrieves the <code>ThingType</code>s whose instances play this role.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getPlayerTypes(transaction, transitivity)
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity.TRANSITIVE</code> for direct and indirect playing,
     *                     <code>Transitivity.EXPLICIT</code> for direct playing only
     */
    ConceptIterable<ThingType> getPlayerTypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves the <code>Relation</code> instances that this role is related to.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getRelationInstances(transaction, transitivity)
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect relation,
     *                     <code>Transitivity::EXPLICIT</code> for direct relation only
     */
    ConceptIterable<Relation> getRelationInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves the <code>Thing</code> instances that play this role.
     *
     * <h3>Examples</h3>
     * <pre>
     * roleType.getPlayerInstances(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect playing,
     *                     <code>Transitivity::EXPLICIT</code> for direct playing only
     */
    ConceptIterable<Thing> getPlayerInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

private:
    RoleType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
