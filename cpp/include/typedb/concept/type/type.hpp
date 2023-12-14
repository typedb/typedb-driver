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

class Type : public Concept {
public:
    // We don't make these virtual so we can emulate returning iterators to the subtypes.
    ConceptPtrFuture<Type> getSupertype(Transaction& transaction);
    ConceptIterable<Type> getSupertypes(Transaction& transaction);
    ConceptIterable<Type> getSubtypes(Transaction& transaction, Transitivity transitivity = Transitivity::TRANSITIVE);

    virtual bool isAbstract() = 0;
    virtual std::string getLabel() = 0;
    [[nodiscard]] virtual VoidFuture setLabel(Transaction& transaction, const std::string& newLabel) = 0;

    virtual BoolFuture isDeleted(Transaction& transaction) = 0;
    [[nodiscard]] virtual VoidFuture deleteType(Transaction& transaction) = 0;

protected:
    Type(ConceptType conceptType, _native::Concept* conceptNative);

    ConceptFutureWrapper* getSuperTypeFutureNative(Transaction& transaction);
    ConceptIteratorWrapper* getSubtypesIteratorNative(Transaction& transaction, Transitivity transitivity);
    ConceptIteratorWrapper* getSupertypesIteratorNative(Transaction& transaction);
};

}  // namespace TypeDB
