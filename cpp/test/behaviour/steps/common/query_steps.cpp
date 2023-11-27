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

#include <algorithm>
#include <chrono>
#include <cstdlib>
#include <ctime>
#include <iomanip>
#include <map>

#include "common.hpp"
#include "concept_tables.hpp"
#include "steps.hpp"
#include "utils.hpp"

#include "typedb/query/querymanager.hpp"


using namespace cucumber::messages;

namespace TypeDB::BDD {

bool rulesContain(Transaction& tx, const std::string label);
std::vector<std::string> extractVarsFromQueryTemplate(std::string& queryTemplate);
std::string applyTemplate(std::string& queryTemplate, std::vector<std::string>& varNames, ConceptMap& cm);

cucumber_bdd::StepCollection<Context> querySteps = {

    BDD_STEP("typeql define", {
        context.transaction().query.define(step.argument->doc_string->content, TypeDB::Options()).wait();
    }),
    BDD_STEP("typeql define; throws exception", {
        DRIVER_THROWS("", { context.transaction().query.define(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql define; throws exception containing \"(.+)\"", {
        DRIVER_THROWS(matches[1].str(), { context.transaction().query.define(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql undefine", {
        context.transaction().query.undefine(step.argument->doc_string->content, TypeDB::Options()).wait();
    }),
    BDD_STEP("typeql undefine; throws exception", {
        DRIVER_THROWS("", { context.transaction().query.undefine(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql undefine; throws exception containing \"(.*)\"", {
        DRIVER_THROWS(matches[1].str(), { context.transaction().query.undefine(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql insert", {
        context.setResult(context.transaction().query.insert(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql insert; throws exception", {
        DRIVER_THROWS("", {
            context.setResult(context.transaction().query.insert(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("typeql insert; throws exception containing \"(.*)\"", {
        DRIVER_THROWS(matches[1].str(), {
            context.setResult(context.transaction().query.insert(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("typeql delete", {
        context.transaction().query.matchDelete(step.argument->doc_string->content, TypeDB::Options()).wait();
    }),
    BDD_STEP("typeql delete; throws exception", {
        DRIVER_THROWS("", { context.transaction().query.matchDelete(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql delete; throws exception containing \"(.*)\"", {
        DRIVER_THROWS(matches[1].str(), { context.transaction().query.matchDelete(step.argument->doc_string->content, TypeDB::Options()).wait(); });
    }),
    BDD_STEP("typeql update", {
        context.setResult(context.transaction().query.update(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql update; throws exception", {DRIVER_THROWS("", {
                 context.setResult(context.transaction().query.update(step.argument->doc_string->content, TypeDB::Options()));
             })}),
    BDD_STEP("typeql update; throws exception containing \"(.*)\"", {DRIVER_THROWS(matches[1].str(), {
                 context.setResult(context.transaction().query.update(step.argument->doc_string->content, TypeDB::Options()));
             })}),
    BDD_STEP("get answers of typeql insert", {
        context.setResult(context.transaction().query.insert(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("get answers of typeql get", {
        context.setResult(context.transaction().query.get(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql get; throws exception", {
        DRIVER_THROWS("", {
            context.setResult(context.transaction().query.get(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("typeql get; throws exception containing \"(.*)\"", {
        DRIVER_THROWS(matches[1].str(), {
            context.setResult(context.transaction().query.get(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("get answer of typeql get aggregate", {
        context.setResult(context.transaction().query.getAggregate(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql get aggregate; throws exception", {
        DRIVER_THROWS("", {
            context.setResult(context.transaction().query.getAggregate(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("get answers of typeql get group", {
        context.setResult(context.transaction().query.getGroup(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql get group; throws exception", {
        DRIVER_THROWS("", {
            context.setResult(context.transaction().query.getGroup(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),

    BDD_STEP("answer size is: (\\d+)", {
        ASSERT_EQ(atoi(matches[1].str().c_str()), context.lastConceptMapResult.size());
    }),

    BDD_STEP("uniquely identify answer concepts", {
        auto expected = dataTabletoMap(step.argument->data_table.value());
        auto varNames = getVarsFromDataTable(expected);
        auto actual = conceptMapResultToMap(context.transaction(), context.lastConceptMapResult, varNames);
        ASSERT_TRUE(compareResults(expected, actual));
    }),
    BDD_STEP("aggregate value is: (\\d+(\\.\\d+)?)", {
        ASSERT_TRUE(context.lastAggregateResult.has_value());
        std::string expected = matches[1].str();
        TypedValue tv = typedValueFromValue(context.lastAggregateResult.value()->asValue());
        ASSERT_TRUE(compareStringToTypedValue(expected, tv));
    }),
    BDD_STEP("aggregate answer is empty", {
        ASSERT_FALSE(context.lastAggregateResult.has_value());
    }),
    BDD_STEP("answer groups are", {
        auto expected = dataTabletoMap(step.argument->data_table.value());
        auto varNames = getVarsFromDataTable(expected);
        auto ownerIt = std::find(varNames.begin(), varNames.end(), "owner");
        assert(ownerIt != varNames.end());
        varNames.erase(ownerIt);
        auto actual = conceptMapGroupResultToMap(context.transaction(), context.lastConceptMapGroupResult, varNames);
        ASSERT_TRUE(compareResults(expected, actual));
    }),

    BDD_STEP("get answers of typeql get group aggregate", {
        context.setResult(context.transaction().query.getGroupAggregate(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("group aggregate values are", {
        auto expected = dataTabletoMap(step.argument->data_table.value());
        auto actual = valueGroupResultToMap(context.transaction(), context.lastValueGroupResult);
        ASSERT_TRUE(compareResultsValueGroup(expected, actual));
    }),
    BDD_STEP("each answer satisfies", {
        std::string queryTemplate = step.argument->doc_string->content;
        std::vector<std::string> varNames = extractVarsFromQueryTemplate(queryTemplate);
        for (ConceptMap& cm : context.lastConceptMapResult) {
            std::string substitutedQuery = applyTemplate(queryTemplate, varNames, cm);
            auto res = context.transaction().query.get(substitutedQuery, TypeDB::Options());
            int resultCount = 0;
            for (auto& it : res)
                resultCount++;
            ASSERT_EQ(1, resultCount);
        }
    }),
    BDD_STEP("group aggregate answer value is empty", {
        assert(context.lastValueGroupResult.size() == 1);  // STEP REQUIRES ONE VALUE
        ASSERT_FALSE(context.lastValueGroupResult[0].value().has_value());
    }),
    BDD_STEP("templated typeql get; throws exception", {
        std::string queryTemplate = step.argument->doc_string->content;
        std::vector<std::string> varNames = extractVarsFromQueryTemplate(queryTemplate);
        for (ConceptMap& cm : context.lastConceptMapResult) {
            std::string substitutedQuery = applyTemplate(queryTemplate, varNames, cm);
            DRIVER_THROWS("", {
                auto res = context.transaction().query.get(substitutedQuery, TypeDB::Options());
                for (auto& it : res)
                    ;
            });
        }
    }),
    BDD_STEP("order of answer concepts is", {
        auto expected = dataTabletoMap(step.argument->data_table.value());
        auto varNames = getVarsFromDataTable(expected);
        auto actual = conceptMapResultToMap(context.transaction(), context.lastConceptMapResult, varNames);
        ASSERT_EQ(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            compareRow(expected[i], actual[i]);
        }
    }),
    BDD_STEP("rules contain: ([A-Za-z_\\-]+)", {
        ASSERT_EQ(matches[1].str(), context.transaction().logic.getRule(matches[1].str()).get()->label());
    }),
    BDD_STEP("rules do not contain: ([A-Za-z_\\-]+)", {
        ASSERT_EQ(nullptr, context.transaction().logic.getRule(matches[1].str()).get());
    }),

    BDD_STEP("get answers of typeql fetch", {
        context.setResult(context.transaction().query.fetch(step.argument->doc_string->content, TypeDB::Options()));
    }),
    BDD_STEP("typeql fetch; throws exception", {
        DRIVER_THROWS("", {
            context.setResult(context.transaction().query.fetch(step.argument->doc_string->content, TypeDB::Options()));
        });
    }),
    BDD_STEP("fetch answers are", {
        ASSERT_TRUE(compareResults(step.argument->doc_string->content, context.lastFetchResult));
    }),
};


std::vector<std::string> extractVarsFromQueryTemplate(std::string& queryTemplate) {
    std::smatch matches;
    std::regex_search(queryTemplate, matches, std::regex("<answer\\.[A-Za-z_\\-]+\\.iid>"));
    std::vector<std::string> varNames;
    for (auto& m : matches) {
        varNames.push_back(split(split(m.str(), ".").second, ".").first);
    }
    return varNames;
}

std::string applyTemplate(std::string& queryTemplate, std::vector<std::string>& varNames, ConceptMap& cm) {
    std::string s = queryTemplate;  // Copy
    for (auto& varName : varNames) {
        std::string lookFor = "<answer." + varName + ".iid>";
        size_t start = s.find(lookFor);
        std::string iidStr = cm.get(varName)->asThing()->getIID();
        s.replace(start, lookFor.size(), iidStr);
    }
    return s;
}


}  // namespace TypeDB::BDD
