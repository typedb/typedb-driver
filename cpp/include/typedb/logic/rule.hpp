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

#include <string>

#include "typedb/common/future.hpp"
#include "typedb/common/iterator.hpp"
#include "typedb/common/native.hpp"

namespace TypeDB {

class Explanation;  // Forward declaration for friendship
class Rule;
class Transaction;

using RuleFuture = Future<Rule, _native::RulePromise>;
using OptionalRuleFuture = Future<std::optional<Rule>, _native::RulePromise>;

using RuleIterable = Iterable<_native::RuleIterator, _native::Rule, Rule>;
using RuleIterator = Iterator<_native::RuleIterator, _native::Rule, Rule>;

/**
 * \brief Rules are a part of schema and define embedded logic.
 *
 * The reasoning engine uses rules as a set of logic to infer new data.
 * A rule consists of a condition and a conclusion, and is uniquely identified by a label.
 */
class Rule {
public:
    Rule(Rule&&) = default;
    Rule& operator=(Rule&&) = default;
    ~Rule() = default;

    /**
     * Retrieves the unique label of the rule.
     */
    std::string label();

    /**
     * The statements that constitute the ‘when’ of the rule.
     */
    std::string when();

    /**
     * The single statement that constitutes the ‘then’ of the rule.
     */
    std::string then();

    /**
     * Renames the label of the rule. The new label must remain unique.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.setLabel(transaction, newLabel).get();
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     * @param label The new label to be given to the rule
     */
    VoidFuture setLabel(Transaction& transaction, const std::string& label);

    /**
     * Deletes this rule.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.deleteRule(transaction).get();
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     */
    VoidFuture deleteRule(Transaction& transaction);

    /**
     * Check if this rule has been deleted.
     *
     * <h3>Examples</h3>
     * <pre>
     * rule.isDeleted(transaction).get();
     * </pre>
     *
     * @param transaction The current <code>Transaction</code>
     */
    BoolFuture isDeleted(Transaction& transaction);

    /**
     * A string representation of this Rule.
     */
    std::string toString();


private:
    Rule(_native::Rule*);
    Rule(const Rule&) = delete;
    Rule& operator=(const Rule&) = delete;

    NativePointer<_native::Rule> ruleNative;

    friend class Explanation;
    friend class IteratorHelper<_native::RuleIterator, _native::Rule, Rule>;
    friend class FutureHelper<Rule, _native::RulePromise>;
    friend class FutureHelper<std::optional<Rule>, _native::RulePromise>;
};

}  // namespace TypeDB
