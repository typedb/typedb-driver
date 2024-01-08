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

#include <cstdlib>

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> userSteps = {
    BDD_STEP_AND_THROWS("users get all", {
        for (auto& it : context.driver->users.all())
            ;
    }),
    BDD_STEP_AND_THROWS("users get user: (\\S+)", {
        ASSERT_FALSE(nullptr == context.driver->users.get(matches[1].str()));
    }),
    BDD_STEP_AND_THROWS("users contains: (\\S+)", {
        ASSERT_TRUE(context.driver->users.contains(matches[1].str()));
    }),
    BDD_STEP("users not contains: (\\S+)", {
        ASSERT_FALSE(context.driver->users.contains(matches[1].str()));
    }),
    BDD_STEP_AND_THROWS("users create: (\\S+), (\\S+)", {
        context.driver->users.create(matches[1].str(), matches[2].str());
    }),
    BDD_STEP_AND_THROWS("users password set: (\\S+), (\\S+)", {
        context.driver->users.passwordSet(matches[1].str(), matches[2].str());
    }),
    BDD_STEP_AND_THROWS("users delete: (\\S+)", {
        context.driver->users.deleteUser(matches[1].str());
    }),
    BDD_STEP("\\s*get connected user\\s*", {
        auto ignored = context.driver->user();
    }),
    BDD_STEP_AND_THROWS("user password update: (\\S+), (\\S+)", {
        context.driver->user().passwordUpdate(context.driver->users, matches[1].str(), matches[2].str());
    }),
    BDD_STEP_AND_THROWS("user expiry-seconds", {
        ASSERT_TRUE(context.driver->user().passwordExpirySeconds().has_value());
    }),
};

}  // namespace TypeDB::BDD
