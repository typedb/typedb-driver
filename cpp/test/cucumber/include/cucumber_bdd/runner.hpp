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

#include <cucumber/messages/envelope.hpp>

#include "cucumber_bdd/step.hpp"
#include "cucumber_bdd/testrun.hpp"

namespace cucumber_bdd {

using pickle = cucumber::messages::pickle;
using pickle_step = cucumber::messages::pickle_step;

class TestRunnerBase {
   public:
    void loadFeature(const std::string& path);
    int runAllTests();

    virtual void beforeAllTests() = 0;
    virtual void afterAllTests() = 0;
    virtual void registerTest(const std::string& featureName, const pickle& scenario) = 0;
    virtual bool skipScenario(const cucumber::messages::pickle& scenario) = 0;
};

template <typename CTX>
class TestRunner : public TestRunnerBase {
   private:
    const TestHooks<CTX>* hooks;
    std::vector<StepDefinition<CTX>> steps;

   public:
    TestRunner(std::initializer_list<StepCollection<CTX>> stepLists, const TestHooks<CTX>* hooks = nullptr)
        : hooks(hooks) {
        int totalSteps = 0;
        for (const StepCollection<CTX> stepVec : stepLists) {
            totalSteps += stepVec.size();
        }

        steps.reserve(totalSteps);
        for (const StepCollection<CTX> stepVec : stepLists) {
            for (const cucumber_bdd::StepDefinition<CTX> stepDef : stepVec) {
                steps.push_back(StepDefinition<CTX>{stepDef.regex, stepDef.impl});  // TODO: Avoid deep copy?
            }
        }
        assert(steps.size() == totalSteps);
        std::cerr << "Found a total of " << steps.size() << " steps\n";
    }

    void beforeAllTests() override {
        if (hooks != nullptr) hooks->beforeAll();
    }

    void afterAllTests() override {
        if (hooks != nullptr) hooks->afterAll();
    }
    bool skipScenario(const cucumber::messages::pickle& scenario) override {
        return (hooks != nullptr) ? hooks->skipScenario(scenario) : false;
    }

    void registerTest(const std::string& featureName, const pickle& scenario) override {
        std::vector<ResolvedStep<CTX>> resolvedSteps;
        resolvedSteps.reserve(scenario.steps.size());
        for (pickle_step step : scenario.steps) {
            resolvedSteps.push_back(ResolvedStep<CTX>{step, resolveStep(step)});
        }
        testing::RegisterTest(
            featureName.c_str(), scenario.name.c_str(),
            nullptr, nullptr,
            scenario.uri.c_str(), __LINE__,
            TestRunFactory<CTX>{resolvedSteps, hooks});
    }

    const StepDefinition<CTX>* resolveStep(pickle_step& toResolve) {
        for (size_t i = 0; i < steps.size(); i++) {
            if (std::regex_match(toResolve.text, steps[i].regex)) {
                return &steps[i];
            }
        }
        throw std::runtime_error("Unmatched step: " + toResolve.text);
    }
};

}  // namespace cucumber_bdd
