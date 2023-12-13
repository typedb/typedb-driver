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
#include "concept_utils.hpp"
#include "steps.hpp"
#include "utils.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> thingAPISteps = {

    // steps

    // With vars
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[3].str()), nullptr == context.vars[matches[2].str()]);
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) has type: (\\S+)", {
        ASSERT_EQ(matches[3].str(), context.vars[matches[2].str()]->asThing()->getType()->getLabel());
    }),

    BDD_STEP("delete (attribute|entity|relation): \\$(\\S+)", {
        context.vars[matches[2].str()]->asThing()->deleteThing(context.transaction()).wait();
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) is deleted: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[3].str()), context.vars[matches[2].str()]->asThing()->isDeleted(context.transaction()).get());
    }),

    BDD_STEP_AND_THROWS("(attribute|entity|relation) \\$(\\S+) set has: \\$(\\S+)", {
        context.vars[matches[2].str()]->asThing()->setHas(context.transaction(), context.vars[matches[3].str()]->asAttribute()).wait();
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) unset has: \\$(\\S+)", {
        context.vars[matches[2].str()]->asThing()->unsetHas(context.transaction(), context.vars[matches[3].str()]->asAttribute()).wait();
    }),

    // has:
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get attributes contain: \\$(\\S+)", {
        auto owned = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction()));
        ASSERT_TRUE(containsInstance(owned, context.vars[matches[3].str()].get()));
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get attributes do not contain: \\$(\\S+)", {
        auto owned = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction()));
        ASSERT_FALSE(containsInstance(owned, context.vars[matches[3].str()].get()));
    }),

    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get attributes\\((\\S+)\\) as\\((?:\\w+)\\) contain: \\$(\\S+)", {
        auto attributeType = attrType(context, matches[3].str());
        auto owned = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction(), attributeType.get()));
        ASSERT_TRUE(containsInstance(owned, context.vars[matches[4].str()].get()));
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get attributes\\((\\S+)\\) as\\((?:\\w+)\\) do not contain: \\$(\\S+)", {
        auto attributeType = attrType(context, matches[3].str());
        auto owned = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction(), attributeType.get()));
        ASSERT_FALSE(containsInstance(owned, context.vars[matches[4].str()].get()));
    }),

    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get keys contain: \\$(\\S+)", {
        std::vector<Annotation> annotations;
        annotations.push_back(Annotation::key());
        auto keys = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction(), annotations));
        ASSERT_TRUE(containsInstance(keys, context.vars[matches[3].str()].get()));
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get keys do not contain: \\$(\\S+)", {
        std::vector<Annotation> annotations;
        annotations.push_back(Annotation::key());
        auto keys = collect(context.vars[matches[2].str()]->asThing()->getHas(context.transaction(), annotations));
        ASSERT_FALSE(containsInstance(keys, context.vars[matches[3].str()].get()));
    }),

    // relations

    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get relations do not contain: \\$(\\S+)", {
        auto relations = collect(context.vars[matches[2].str()]->asThing()->getRelations(context.transaction()));
        ASSERT_FALSE(containsInstance(relations, context.vars[matches[3].str()].get()));
    }),

    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get relations\\((\\S+)\\) contain: \\$(\\S+)", {
        auto roleTypeOwner = roleType(context, matches[3].str());
        std::vector<RoleType*> roles = {roleTypeOwner.get()};
        assert(roles[0] != nullptr);
        auto relations = collect(context.vars[matches[2].str()]->asThing()->getRelations(context.transaction(), roles));
        ASSERT_TRUE(containsInstance(relations, context.vars[matches[4].str()].get()));
        roleTypeOwner.reset();
    }),
    BDD_STEP("(attribute|entity|relation) \\$(\\S+) get relations\\((\\S+)\\) do not contain: \\$(\\S+)", {
        auto roleTypeOwner = roleType(context, matches[3].str());
        std::vector<RoleType*> roles = {roleTypeOwner.get()};
        auto relations = collect(context.vars[matches[2].str()]->asThing()->getRelations(context.transaction(), roles));
        ASSERT_FALSE(containsInstance(relations, context.vars[matches[4].str()].get()));
        roleTypeOwner.get();
    }),

};

}  // namespace TypeDB::BDD
