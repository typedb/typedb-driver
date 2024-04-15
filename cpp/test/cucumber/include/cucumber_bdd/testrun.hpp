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
#include <cassert>

#include <cucumber/messages/envelope.hpp>
#include "gtest/gtest.h"

#include "cucumber_bdd/step.hpp"

namespace cucumber_bdd {

using pickle = cucumber::messages::pickle;
using pickle_step = cucumber::messages::pickle_step;

template <typename CTX>
using Scenario = std::vector<ResolvedStep<CTX>>;

#ifdef NDEBUG
#define DEBUGONLY(CMD) \
    {}
#else
#define DEBUGONLY(CMD) \
    { (CMD); }
#endif

template <typename CTX>
class TestHooks {
   public:
    virtual void beforeAll() const {}
    virtual void afterAll() const {}

    virtual bool skipScenario(const pickle& scenario) const {
        return std::any_of(scenario.tags.begin(), scenario.tags.end(), [&](const cucumber::messages::pickle_tag& tag) {
            return tag.name == "@ignore";
        });
    }
    virtual void beforeScenario(CTX& context, const Scenario<CTX>*) const {}
    virtual void afterScenario(CTX& context, const Scenario<CTX>*) const {}
};

template <typename CTX>
class TestRun : public testing::Test {
   private:
    CTX ctx;
    const Scenario<CTX>* scenario;
    const TestHooks<CTX>* hooks;

   public:
    TestRun(const Scenario<CTX>* scenario, const TestHooks<CTX>* hooks = nullptr)
        : scenario(scenario),
          hooks(hooks) {}

    void SetUp() override {
        if (hooks != nullptr) hooks->beforeScenario(ctx, scenario);
    }

    void TearDown() override {
        if (hooks != nullptr) hooks->afterScenario(ctx, scenario);
    }

    void TestBody() override {
        DEBUGONLY(std::cout << "Running scenario: " << testing::UnitTest::GetInstance()->current_test_info()->name() << std::endl);
        for (ResolvedStep<CTX> step : *scenario) {
            DEBUGONLY(std::cout << "\t-" << step.step.text << std::endl);
            std::smatch regex_matches;
            bool success = std::regex_match(step.step.text, regex_matches, step.definition->regex);
            assert(success);
            step.definition->impl(ctx, step.step, regex_matches);
        }
        DEBUGONLY(std::cout << "Completed scenario: " << testing::UnitTest::GetInstance()->current_test_info()->name() << std::endl);
    }
};

template <typename CTX>
struct TestRunFactory {
    const Scenario<CTX> resolvedSteps;
    const TestHooks<CTX>* hooks;

    TestRun<CTX>* operator()() {
        return new TestRun<CTX>(&resolvedSteps, hooks);
    }
};

}  // namespace cucumber_bdd

#undef DEBUG_ONLY
