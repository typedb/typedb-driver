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

/// Common super-type of Entity, Relation, and Attribute
class Thing : public Concept {
public:
    /**
     * Retrieves the unique id of the <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getIID();
     * </pre>
     */
    std::string getIID();

    /**
     * Retrieves the type which this <code>Thing</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getType();
     * </pre>
     */
    std::unique_ptr<ThingType> getType();

    /**
     * Checks if this <code>Thing</code> is inferred by a [Reasoning Rule].
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.isInferred();
     * </pre>
     */
    bool isInferred();


    /**
     * Checks if this <code>Thing</code> is deleted.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.isDeleted(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    BoolFuture isDeleted(Transaction& transaction);

    /**
     * Deletes this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.deleteThing(transaction).get();
     * </pre>
     *
     * @param transaction The current transaction
     */
    VoidFuture deleteThing(Transaction& transaction);

    /**
     * Assigns an <code>Attribute</code> to be owned by this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.setHas(transaction, attribute).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attribute The <code>Attribute</code> to be owned by this <code>Thing</code>.
     */
    [[nodiscard]] VoidFuture setHas(Transaction& transaction, Attribute* attribute);

    /**
     * Unassigns an <code>Attribute</code> from this <code>Thing</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.unsetHas(transaction, attribute).get();
     * </pre>
     *
     * @param transaction The current transaction
     * @param attribute The <code>Attribute</code> to be disowned from this <code>Thing</code>.
     */
    [[nodiscard]] VoidFuture unsetHas(Transaction& transaction, Attribute* attribute);

    /**
     * Retrieves the <code>Attribute</code>s that this <code>Thing</code> owns,
     * filtered by <code>Annotation</code>s.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getHas(transaction);
     * thing.getHas(transaction, {Annotation.key()}));
     * </pre>
     *
     * @param transaction The current transaction
     * @param annotations Only retrieve attributes with all given <code>Annotation</code>s
     */
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::initializer_list<Annotation>& annotations = {});

    /**
     * See \ref getHas(Transaction&, const std::vector<std::unique_ptr<AttributeType>>&)
     */
    ConceptIterable<Attribute> getHas(Transaction& transaction, const AttributeType* attribute);

    /**
     * Retrieves the <code>Attribute</code>s of the specified <code>AttributeType</code>s
     * that this <code>Thing</code> owns.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getHas(transaction, attributeTypes);
     * </pre>
     *
     * @param transaction The current transaction
     * @param attributeTypes The <code>AttributeType</code>s to filter the attributes by
     */
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<std::unique_ptr<AttributeType>>& attributeTypes);

    /**
     * See \ref getHas(Transaction& transaction, const std::vector<std::unique_ptr<AttributeType>>& attributeTypes)
     */
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<const AttributeType*>& attributeTypes);

    /**
     * See \ref getHas(Transaction&, const std::initializer_list<Annotation>&)
     */
    ConceptIterable<Attribute> getHas(Transaction& transaction, const std::vector<Annotation>& annotations);


    /**
     * Retrieves all the <code>Relations</code> which this <code>Thing</code> plays a role in,
     * optionally filtered by one or more given roles.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getRelations(transaction);
     * thing.getRelations(transaction, roleTypes);
     * </pre>
     *
     * @param transaction The current transaction
     * @param roleTypes The array of roles to filter the relations by.
     */
    ConceptIterable<Relation> getRelations(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes = {});

    /**
     * See \ref getRelations(Transaction& transaction, const std::vector<std::unique_ptr<RoleType>>& roleTypes)
     */
    ConceptIterable<Relation> getRelations(Transaction& transaction, const std::vector<RoleType*>& roleTypes);

    /**
     * Retrieves the roles that this <code>Thing</code> is currently playing.
     *
     * <h3>Examples</h3>
     * <pre>
     * thing.getPlaying(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<RoleType> getPlaying(Transaction& transaction);

protected:
    Thing(ConceptType conceptType, _native::Concept* conceptNative);
    virtual _native::Concept* getTypeNative() = 0;

    friend class ConceptFactory;
};

}  // namespace TypeDB
