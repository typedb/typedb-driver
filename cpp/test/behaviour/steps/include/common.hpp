/*
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
#include <map>
#include <memory>
#include <vector>

#include "typedb_driver.hpp"

#include "cucumber_bdd/runner.hpp"
#include "cucumber_bdd/step.hpp"
#include "cucumber_bdd/testrun.hpp"

namespace TypeDB::BDD {


struct Context {
    std::optional<TypeDB::Driver> driver;  // TODO: All these optional, so the null checks are meaningful?

    std::vector<TypeDB::Session> sessions;
    std::map<TypeDB::Session*, std::vector<TypeDB::Transaction>> sessionTransactions;  // Brittle: We strongly assume the session will always live in Context.sessions

    TypeDB::Options sessionOptions;
    TypeDB::Options transactionOptions;

    std::vector<TypeDB::ConceptMap> lastConceptMapResult;
    TypeDB::AggregateResult lastAggregateResult;
    std::vector<TypeDB::ConceptMapGroup> lastConceptMapGroupResult;
    std::vector<TypeDB::ValueGroup> lastValueGroupResult;
    std::vector<TypeDB::JSON> lastFetchResult;

    std::map<std::string, std::unique_ptr<Concept>> vars;

    Context() {
        transactionOptions.infer(true);
    }

    TypeDB::Session& session() {
        return sessions[0];
    }
    TypeDB::Transaction& transaction() {
        return sessionTransactions[&sessions[0]][0];
    }

    void setSession(TypeDB::Session&& session) {
        sessions.insert(sessions.begin(), std::move(session));
    }

    void setTransaction(TypeDB::Transaction&& transaction) {
        auto& transactions = sessionTransactions[&sessions[0]];
        transactions.insert(transactions.begin(), std::move(transaction));
    }

    void setResult(AggregateFuture results) {
        lastAggregateResult = results.get();
    }

    void setResult(TypeDB::ConceptMapIterable&& results) {
        lastConceptMapResult.clear();
        for (TypeDB::ConceptMap& result : results) {
            lastConceptMapResult.push_back(std::move(result));
        }
    }

    void setResult(TypeDB::ValueGroupIterable&& results) {
        lastValueGroupResult.clear();
        for (TypeDB::ValueGroup& result : results) {
            lastValueGroupResult.push_back(std::move(result));
        }
    }

    void setResult(TypeDB::ConceptMapGroupIterable&& results) {
        lastConceptMapGroupResult.clear();
        for (TypeDB::ConceptMapGroup& result : results) {
            lastConceptMapGroupResult.push_back(std::move(result));
        }
    }

    void setResult(TypeDB::JSONIterable&& results) {
        lastFetchResult.clear();
        for (TypeDB::JSON& result : results) {
            lastFetchResult.push_back(result);
        }
    }
};

class TestHooks : public cucumber_bdd::TestHooks<Context> {
    virtual bool skipScenario(const cucumber::messages::pickle& scenario) const override;
    virtual void beforeAll() const override;
    virtual void afterScenario(Context& context, const cucumber_bdd::Scenario<Context>* scenario) const override;
};
extern const TestHooks testHooks;

struct CoreOrClusterConnection {
    static TypeDB::Driver defaultConnection();
    static TypeDB::Driver connectWithAuthentication(const std::string& username, const std::string& password);
};

}  // namespace TypeDB::BDD
