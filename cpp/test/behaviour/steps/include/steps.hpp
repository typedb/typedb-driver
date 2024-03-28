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

#include "cucumber_bdd/runner.hpp"
#include "cucumber_bdd/step.hpp"
#include "cucumber_bdd/testrun.hpp"

#include <iostream>

#define BDD_STEP(REGEX, IMPL) (cucumber_bdd::StepDefinition<TypeDB::BDD::Context>{std::regex("\\s*" REGEX "\\s*"), [](Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches) { IMPL }})
#define BDD_UNIMPLEMENTED(REGEX) (cucumber_bdd::StepDefinition<TypeDB::BDD::Context>{std::regex((REGEX)), &unimplemented})
#define BDD_NOOP(REGEX) (cucumber_bdd::StepDefinition<TypeDB::BDD::Context>{std::regex((REGEX)), &noop})
#define BDD_STEP_AND_THROWS(REGEX, IMPL) \
    BDD_STEP(REGEX, IMPL), BDD_STEP(REGEX "; throws exception", { DRIVER_THROWS("", IMPL); })

#define DRIVER_THROWS(MSG, IMPL)                                     \
    {                                                                \
        try {                                                        \
            IMPL;                                                    \
            FAIL();                                                  \
        } catch (TypeDB::DriverException & e) {                      \
            ASSERT_TRUE(e.message().find(MSG) != std::string::npos); \
        }                                                            \
    }


namespace TypeDB::BDD {

void noop(Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches);
void unimplemented(Context& context, cucumber::messages::pickle_step& step, const std::smatch& matches);

extern cucumber_bdd::StepCollection<Context> commonSteps;
extern cucumber_bdd::StepCollection<Context> connectionSteps;
extern cucumber_bdd::StepCollection<Context> userSteps;
extern cucumber_bdd::StepCollection<Context> databaseSteps;
extern cucumber_bdd::StepCollection<Context> sessionSteps;
extern cucumber_bdd::StepCollection<Context> transactionSteps;
extern cucumber_bdd::StepCollection<Context> querySteps;

extern cucumber_bdd::StepCollection<Context> thingTypeSteps;
extern cucumber_bdd::StepCollection<Context> attributeTypeSteps;
extern cucumber_bdd::StepCollection<Context> entityTypeSteps;
extern cucumber_bdd::StepCollection<Context> relationTypeSteps;

extern cucumber_bdd::StepCollection<Context> thingAPISteps;
extern cucumber_bdd::StepCollection<Context> attributeSteps;
extern cucumber_bdd::StepCollection<Context> relationSteps;
extern cucumber_bdd::StepCollection<Context> entitySteps;

}  // namespace TypeDB::BDD
