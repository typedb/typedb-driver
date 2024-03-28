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

std::unique_ptr<RoleType> roleOrOverridden(Context& context, const std::unique_ptr<RelationType>& relType, const std::string& roleLabel, const std::string& isOverridden = "") {
    if (isOverridden.find("overridden") != std::string::npos) {
        return relType->getRelatesOverridden(context.transaction(), roleLabel).get();
    } else return relType->getRelates(context.transaction(), roleLabel).get();
}

std::unique_ptr<RoleType> roleOrOverridden(Context& context, const std::string& relType, const std::string& roleLabel, const std::string& isOverridden = "") {
    return roleOrOverridden(context, relationType(context, relType), roleLabel, isOverridden);
}

cucumber_bdd::StepCollection<Context> relationTypeSteps = {

    // Basic
    BDD_STEP("put relation type: (\\S+)", {
        context.transaction().concepts.putRelationType(matches[1].str()).wait();
    }),
    BDD_STEP_AND_THROWS("relation\\((\\S+)\\) set supertype: (\\S+)", {
        relationType(context, matches[1].str())->setSupertype(context.transaction(), relationType(context, matches[2].str()).get()).wait();
    }),

    BDD_STEP("relation\\((\\S+)\\) get subtypes contain:", {
        auto subtypeLabels = transform(relationType(context, matches[1].str())->getSubtypes(context.transaction()), &label<RelationType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), subtypeLabels));
    }),

    // relates
    BDD_STEP_AND_THROWS("relation\\((\\S+)\\) set relates role: (\\S+)", {
        relationType(context, matches[1].str())->setRelates(context.transaction(), matches[2].str()).wait();
    }),
    BDD_STEP_AND_THROWS("relation\\((\\S+)\\) set relates role: (\\S+) as (\\S+)", {
        relationType(context, matches[1].str())->setRelates(context.transaction(), matches[2].str(), matches[3].str()).wait();
    }),

    BDD_STEP_AND_THROWS("relation\\((\\S+)\\) unset related role: (\\S+)", {
        auto relType = relationType(context, matches[1].str());
        relType->unsetRelates(context.transaction(), roleOrOverridden(context, relType, matches[2].str()).get()).wait();
    }),

    BDD_STEP("relation\\((\\S+)\\) get( overridden)? role\\((\\S+)\\) is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[4].str()), nullptr == roleOrOverridden(context, matches[1].str(), matches[3].str(), matches[2].str()));
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) is abstract: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[3].str()), roleOrOverridden(context, matches[1].str(), matches[2].str())->isAbstract());
    }),
    BDD_STEP("relation\\((\\S+)\\) get( overridden)? role\\((\\S+)\\) get label: (\\S+)", {
        ASSERT_EQ(matches[4].str(), roleOrOverridden(context, matches[1].str(), matches[3].str(), matches[2].str())->getName());
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) set label: (\\S+)", {
        roleOrOverridden(context, matches[1].str(), matches[2].str())->setLabel(context.transaction(), matches[3].str()).wait();
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get supertype: (\\S+)", {
        ASSERT_EQ(matches[3].str(), roleOrOverridden(context, matches[1].str(), matches[2].str())->getSupertype(context.transaction()).get()->getLabel());
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get supertypes contain:", {
        auto supertypeLabels = transform(roleOrOverridden(context, matches[1].str(), matches[2].str())->getSupertypes(context.transaction()), &label<RoleType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), supertypeLabels));
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get subtypes contain:", {
        auto subtypeLabels = transform(roleOrOverridden(context, matches[1].str(), matches[2].str())->getSubtypes(context.transaction()), &label<RoleType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get subtypes do not contain:", {
        auto subtypeLabels = transform(roleOrOverridden(context, matches[1].str(), matches[2].str())->getSubtypes(context.transaction()), &label<RoleType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("relation\\((\\S+)\\) get related( explicit)? roles contain:", {
        Transitivity transitivity = parseTransitivity(matches[2].str());
        auto relatesLabels = transform(relationType(context, matches[1].str())->getRelates(context.transaction(), transitivity), &label<RoleType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), relatesLabels));
    }),
    BDD_STEP("relation\\((\\S+)\\) get related( explicit)? roles do not contain:", {
        Transitivity transitivity = parseTransitivity(matches[2].str());
        auto relatesLabels = transform(relationType(context, matches[1].str())->getRelates(context.transaction(), transitivity), &label<RoleType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), relatesLabels));
    }),

};

}  // namespace TypeDB::BDD
