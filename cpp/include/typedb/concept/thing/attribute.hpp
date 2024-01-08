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

#include "typedb/concept/thing/thing.hpp"

namespace TypeDB {

/**
 * \brief Attribute is an instance of the attribute type and has a value.
 *
 * This value is fixed and unique for every given instance of the attribute type.
 * Attributes can be uniquely addressed by their type and value.
 */
class Attribute : public Thing {
public:
    /**
     * Retrieves the value which the <code>Attribute</code> instance holds.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getValue();
     * </pre>
     */
    std::unique_ptr<Value> getValue();

    /**
     * Retrieves the type which this <code>Attribute</code> belongs to.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getType();
     * </pre>
     */
    std::unique_ptr<AttributeType> getType();

    /**
     * Retrieves the instances that own this <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getOwners(transaction);
     * </pre>
     *
     * @param transaction The current transaction
     */
    ConceptIterable<Thing> getOwners(Transaction& transaction);

    /**
     * Retrieves the instances that own this <code>Attribute</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * attribute.getOwners(transaction, ownerType);
     * </pre>
     *
     * @param transaction The current transaction
     * @param ownerType Filter results for only owners of the given type
     */
    ConceptIterable<Thing> getOwners(Transaction& transaction, const ThingType* ownerType);

protected:
    virtual _native::Concept* getTypeNative() override;

private:
    Attribute(_native::Concept* conceptNative);

    friend class ConceptFactory;
};

}  // namespace TypeDB
