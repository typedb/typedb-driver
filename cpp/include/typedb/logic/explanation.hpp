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

/**
 * \brief An explanation of which rule was used to infer the concept and the satisfying ConceptMaps.
 *
 * An explanation of which rule was used for inferring the explained concept, the condition of the rule,
 * the conclusion of the rule, and the mapping of variables between the query and the rule’s conclusion.
 */
class Explanation {
public:
    Explanation(Explanation&&) = default;
    Explanation& operator=(Explanation&&) = default;
    ~Explanation() = default;

    /**
     * Retrieves the Rule for this Explanation.
     *
     * <h3>Examples</h3>
     * <pre>
     * explanation.rule()
     * </pre>
     */
    Rule rule();

    /**
     * Retrieves the Conclusion for this Explanation.
     *
     * <h3>Examples</h3>
     * <pre>
     * explanation.conclusion()
     * </pre>
     */
    ConceptMap conclusion();

    /**
     * Retrieves the Condition for this Explanation.
     *
     * <h3>Examples</h3>
     * <pre>
     * explanation.condition()
     * </pre>
     */
    ConceptMap condition();

    /**
     * Retrieves the query variables for this <code>Explanation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * explanation.queryVariables()
     * </pre>
     */
    std::vector<std::string> queryVariables();

    /**
     * Retrieves the rule variables corresponding to the query variable var for this <code>Explanation</code>.
     *
     * <h3>Examples</h3>
     * <pre>
     * explanation.variableMapping(var)
     * </pre>
     *
     * @param var The query variable to map to rule variables.
     */
    std::vector<std::string> queryVariableMapping(const std::string& var);

    /**
     * A string representation of this Explanation.
     */
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
