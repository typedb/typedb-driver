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

#include "typedb/concept/type/thing_type.hpp"

namespace TypeDB {

/**
 * \brief Relation types (or subtypes of the relation root type) represent relationships between types.
 *
 * Relation types have roles.
 * Other types can play roles in relations if it’s mentioned in their definition.
 * A relation type must specify at least one role.
 */
class RelationType : public ThingType {
public:
    /**
     * Creates and returns an instance of this <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.create(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    [[nodiscard]] ConceptPtrFuture<Relation> create(Transaction& transaction);

    /**
     * Sets the supplied <code>RelationType</code> as the supertype of the current <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.setSupertype(transaction, superRelationType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param superRelationType The <code>RelationType</code> to set as the supertype of this <code>RelationType</code>
     */
    [[nodiscard]] VoidFuture setSupertype(Transaction& transaction, RelationType* superRelationType);

    /**
     * Variant of \ref setRelates(Transaction& transaction, const std::string& roleLabel, const std::string& overriddenLabel)
     * where the RoleType does not override an existing role.
     */
    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel);

    /**
     * Variant of \ref setRelates(Transaction& transaction, const std::string& roleLabel, const std::string& overriddenLabel)
     * where the RoleType is specified directly rather than the label.
     */
    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel, RoleType* overriddenType);

    /**
     * Sets the supplied <code>RelationType</code> as the supertype of the current <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.setSupertype(transaction, superRelationType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param superRelationType The <code>RelationType</code> to set as the supertype of this <code>RelationType</code>
     */
    [[nodiscard]] VoidFuture setRelates(Transaction& transaction, const std::string& roleLabel, const std::string& overriddenLabel);

    /**
     * Variant of \ref unsetRelates(Transaction& transaction, const std::string& roleLabel)
     * where the RoleType is specified directly rather than the label.
     */
    [[nodiscard]] VoidFuture unsetRelates(Transaction& transaction, RoleType* roleType);

    /**
     * Disallows this <code>RelationType</code> from relating to the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.unsetRelates(transaction, roleLabel).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel The role to not relate to the relation type.
     */
    [[nodiscard]] VoidFuture unsetRelates(Transaction& transaction, const std::string& roleLabel);

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
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect instances,
     *                     <code>Transitivity::EXPLICIT</code> for direct relates only
     */
    ConceptIterable<Relation> getInstances(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);


    /**
     * Retrieves all direct and indirect (or direct only) subtypes of the <code>RelationType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getSubtypes(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and indirect subtypes,
     *                     <code>Transitivity::EXPLICIT</code> for direct subtypes only
     */
    ConceptIterable<RelationType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelates(transaction, transitivity);
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and inherited relates,
     *                     <code>Transitivity::EXPLICIT</code> for direct relates only
     */
    ConceptIterable<RoleType> getRelates(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves roles that this <code>RelationType</code> relates to directly or via inheritance.
     * If <code>role_label</code> is given, returns a corresponding <code>RoleType</code> or <code>nullptr</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelates(transaction, roleLabel).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel Label of the role we wish to retrieve
     */
    ConceptPtrFuture<RoleType> getRelates(Transaction& transaction, const std::string& roleLabel);

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelatesOverridden(transaction, roleLabel).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel Label of the role that overrides an inherited role
     */
    ConceptPtrFuture<RoleType> getRelatesOverridden(Transaction& transaction, RoleType* roleType);

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the role with the <code>role_label</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * relationType.getRelatesOverridden(transaction, roleLabel).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleLabel Label of the role that overrides an inherited role
     */
    ConceptPtrFuture<RoleType> getRelatesOverridden(Transaction& transaction, const std::string& roleLabel);

private:
    RelationType(_native::Concept* conceptNative);

    friend class ConceptFactory;
    VoidFuture setRelatesNative(Transaction& transaction, const char* roleLabel, const char* overriddenRoleLabel);
};

}  // namespace TypeDB
