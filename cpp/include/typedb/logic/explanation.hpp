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

#include "typedb/answer/concept_map.hpp"
#include "typedb/common/native.hpp"
#include "typedb/logic/rule.hpp"

namespace TypeDB {

class Explanation {
public:
    Explanation(Explanation&&) = default;
    Explanation& operator=(Explanation&&) = default;
    ~Explanation() = default;

    Rule rule();
    ConceptMap conclusion();
    ConceptMap condition();
    std::string toString();

private:
    Explanation(_native::Explanation*);
    Explanation(const Explanation&) = delete;
    Explanation& operator=(const Explanation&) = delete;

    NativePointer<_native::Explanation> explanationNative;


    friend class IteratorHelper<_native::ExplanationIterator, _native::Explanation, Explanation>;
};

using ExplanationIterator = Iterator<_native::ExplanationIterator, _native::Explanation, Explanation>;
using ExplanationIterable = Iterable<_native::ExplanationIterator, _native::Explanation, Explanation>;

}  // namespace TypeDB
