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

/// Common super-type of EntityType, RelationType, and AttributeType
class ThingType : public Type {
public:
    /// \copydoc Type::getLabel()
    virtual std::string getLabel() override;

    /// \copydoc Type::setLabel()
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

    /// \copydoc Type::isDeleted(Transaction&)
    virtual BoolFuture isDeleted(Transaction& transaction) override;

    /// \copydoc Type::deleteType(Transaction&)
    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) override;

    /**
     * Set a <code>ThingType</code> to be abstract, meaning it cannot have instances.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setAbstract(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @return
     */
    [[nodiscard]] VoidFuture setAbstract(Transaction& transaction);

    /**
     * Set a <code>ThingType</code> to be non-abstract, meaning it can have instances.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetAbstract(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    [[nodiscard]] VoidFuture unsetAbstract(Transaction& transaction);

    /// \copydoc setOwns(Transaction&, AttributeType*, const std::vector<Annotation>&)
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, const std::initializer_list<Annotation>& annotations = {});

    /**
     * Variant of \ref setOwns(Transaction&, AttributeType*, AttributeType*, const std::vector<Annotation>&)
     * with no overridden attribute type
     * */
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, const std::vector<Annotation>& annotations);

    /** See \ref setOwns(Transaction&, AttributeType*, AttributeType*, const std::vector<Annotation>&) */
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::initializer_list<Annotation>& annotations = {});

    /**
     * Allows the instances of this <code>ThingType</code> to own the given <code>AttributeType</code>.
     * Optionally, overriding a previously declared ownership.
     * Optionally, adds annotations to the ownership.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setOwns(transaction, attributeType).get();
     * thingType.setOwns(transaction, attributeType, overriddenType, Collections.singleton(Annotation.key())).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to be owned by the instances of this type.
     * @param overriddenType The <code>AttributeType</code> that this attribute ownership overrides, if applicable.
     * @param annotations Adds annotations to the ownership.
     */
    [[nodiscard]] VoidFuture setOwns(Transaction& transaction, AttributeType* attributeType, AttributeType* overriddenType, const std::vector<Annotation>& annotations);

    /**
     * Disallows the instances of this <code>ThingType</code> from owning the given <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetOwns(transaction, attributeType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> to not be owned by the type.
     */
    [[nodiscard]] VoidFuture unsetOwns(Transaction& transaction, AttributeType* attributeType);

    /**
     * Variant of \ref setPlays(Transaction&, RoleType*, RoleType*) with no overridden role type.
     */
    [[nodiscard]] VoidFuture setPlays(Transaction& transaction, RoleType* roleType);

    /**
     * Allows the instances of this <code>ThingType</code> to play the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.setPlays(transaction, roleType).get();
     * thingType.setPlays(transaction, roleType, overriddenType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to be played by the instances of this type
     * @param overriddenType The role type that this role overrides, if applicable
     */
    [[nodiscard]] VoidFuture setPlays(Transaction& transaction, RoleType* roleType, RoleType* overriddenRoleType);

    /**
     * Disallows the instances of this <code>ThingType</code> from playing the given role.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.unsetPlays(transaction, roleType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The role to not be played by the instances of this type.
     */
    [[nodiscard]] VoidFuture unsetPlays(Transaction& transaction, RoleType* roleType);


    /**
     * Retrieves all direct and inherited (or direct only) roles that are allowed
     * to be played by the instances of this <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getPlays(transaction).get();
     * thingType.getPlays(transaction, Transitivity::EXPLICIT).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param transitivity transitivity: <code>Transitivity::TRANSITIVE</code> for direct and indirect playing,
     *                                   <code>Transitivity::EXPLICIT</code> for direct playing only
     */
    ConceptIterable<RoleType> getPlays(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves a <code>RoleType</code> that is overridden by the given
     * <code>role_type</code> for this <code>ThingType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getPlaysOverridden(transaction, roleType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleType The <code>RoleType</code> that overrides an inherited role
     */
    ConceptPtrFuture<RoleType> getPlaysOverridden(Transaction& transaction, RoleType* roleType);

    /**
     * Variant of \ref getOwns(Transaction&, ValueType, const std::vector<Annotation>&, Transitivity)
     * without filtering on <code>ValueType</code> or <code>Annotation</code>s
     */
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    /// \copydoc getOwns(Transaction&, const std::vector<Annotation>&, Transitivity)
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Variant of \ref getOwns(Transaction&, ValueType, const std::vector<Annotation>&, Transitivity)
     * without filtering on <code>ValueType</code>
     */
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Variant of \ref getOwns(Transaction&, ValueType, const std::vector<Annotation>&, Transitivity)
     * without filtering on <code>Annotation</code>s
     */
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, Transitivity transitivity = Transitivity::TRANSITIVE);

    /** See \ref getOwns(Transaction&, ValueType, const std::vector<Annotation>&, Transitivity) */
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, const std::initializer_list<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves <code>AttributeType</code> that the instances of this
     * <code>ThingType</code> are allowed to own directly or via inheritance.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getOwns(transaction);
     * thingType.getOwns(transaction, valueType, Transitivity::EXPLICIT, Collections.singleton(Annotation.key()));
     * </pre>
     *
     * @param transaction The current transaction
     * @param valueType If specified, only attribute types of this <code>ValueType</code> will be retrieved.
     * @param transitivity <code>Transitivity::TRANSITIVE</code> for direct and inherited ownership,
     *                     <code>Transitivity::EXPLICIT</code> for direct ownership only
     * @param annotations Only retrieve attribute types owned with annotations.
     */
    ConceptIterable<AttributeType> getOwns(Transaction& transaction, ValueType valueType, const std::vector<Annotation>& annotations, Transitivity transitivity = Transitivity::TRANSITIVE);

    /**
     * Retrieves an <code>AttributeType</code>, ownership of which is overridden
     * for this <code>ThingType</code> by a given <code>AttributeType</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getOwnsOverridden(transaction, attributeType).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeType The <code>AttributeType</code> that overrides requested <code>AttributeType</code>
     */
    ConceptPtrFuture<AttributeType> getOwnsOverridden(Transaction& transaction, AttributeType* attributeType);

    /**
     * Produces a pattern for creating this <code>ThingType</code> in a <code>define</code> query.
     *
     * <h3>Examples</h3>
     * <pre>
     * thingType.getSyntax(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    StringFuture getSyntax(Transaction& transaction);

    // non-virtual to avoid the complexity of making Iterable covariant
    /// \copydoc Type::getSupertype(Transaction&)
    ConceptPtrFuture<ThingType> getSupertype(Transaction& transaction);

    /// \copydoc Type::getSupertypes(Transaction&)
    ConceptIterable<ThingType> getSupertypes(Transaction& transaction);

    /// \copydoc Type::getSubtypes(Transaction&, Transitivity)
    ConceptIterable<ThingType> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

protected:
    ThingType(ConceptType conceptType, _native::Concept* conceptNative);

    friend class ConceptFactory;
};

/// \private
class RootThingType : public ThingType {
protected:
    RootThingType(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
