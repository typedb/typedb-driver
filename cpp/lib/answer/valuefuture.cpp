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

#include "typedb/answer/valuefuture.hpp"

#include "inc/conceptfactory.hpp"
#include "inc/macros.hpp"

namespace TypeDB {

template<>
AggregateResult FutureHelper<AggregateResult, _native::ConceptPromise>::resolve(_native::ConceptPromise* conceptPromiseNative) {
    _native::Concept* conceptNative = _native::concept_promise_resolve(conceptPromiseNative);
    if (conceptNative != nullptr) {
        return AggregateResult(ConceptFactory::value(conceptNative));
    } else {
        return AggregateResult();
    }
}

}  // namespace TypeDB
