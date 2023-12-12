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

#include "common.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

cucumber_bdd::StepCollection<Context> connectionSteps = {
    BDD_STEP("typedb starts", {}),

    BDD_STEP("connection opens with default authentication", {
        context.driver = CoreOrCloudConnection::defaultConnection();
    }),

    BDD_STEP_AND_THROWS("connection opens with authentication: (\\S+), (\\S+)", {
        context.driver = CoreOrCloudConnection::connectWithAuthentication(matches[1].str(), matches[2].str());
    }),

    BDD_STEP("connection has been opened", {
        ASSERT_TRUE(context.driver->isOpen());
    }),

    BDD_STEP("connection does not have any database", {
        DatabaseIterable databases = context.driver->databases.all();
        ASSERT_TRUE(databases.begin() == databases.end());
    }),

    BDD_STEP("connection closes", {
        context.driver->close();
    }),

    BDD_NOOP("typedb stops"),
};

}  // namespace TypeDB::BDD
