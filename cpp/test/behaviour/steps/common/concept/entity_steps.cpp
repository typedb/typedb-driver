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

#include <cstdlib>

#include "common.hpp"
#include "concept_utils.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> entitySteps = {
    // Basic
    BDD_STEP("entity\\((\\S+)\\) get instances contain: \\$(\\S+)", {
        ASSERT_TRUE(containsInstance(collect(entityType(context, matches[1].str())->getInstances(context.transaction())), context.vars[matches[2].str()].get()));
    }),
    BDD_STEP("entity\\((\\S+)\\) get instances is empty", {
        ASSERT_EQ(0, collect(entityType(context, matches[1].str())->getInstances(context.transaction())).size());
    }),
};

}  // namespace TypeDB::BDD
