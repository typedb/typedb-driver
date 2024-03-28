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

cucumber_bdd::StepCollection<Context> entityTypeSteps = {
    // Basic
    BDD_STEP("put entity type: (\\S+)", {
        context.transaction().concepts.putEntityType(matches[1].str()).wait();
    }),
    BDD_STEP_AND_THROWS("entity\\((\\S+)\\) set supertype: (\\S+)", {
        entityType(context, matches[1].str())->setSupertype(context.transaction(), entityType(context, matches[2].str())->asEntityType()).wait();
    }),

    BDD_STEP("entity\\((\\S+)\\) get subtypes contain:", {
        auto subtypeLabels = transform(entityType(context, matches[1].str())->getSubtypes(context.transaction()), &label<EntityType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("entity\\((\\S+)\\) get subtypes do not contain:", {
        auto subtypeLabels = transform(entityType(context, matches[1].str())->getSubtypes(context.transaction()), &label<EntityType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), subtypeLabels));
    }),

    // Data write
    BDD_STEP("\\$(\\S+) = entity\\((\\S+)\\) create new instance", {
        context.vars[matches[1].str()] = entityType(context, matches[2].str())->create(context.transaction()).get();
    }),
    BDD_STEP("\\$(\\S+) = entity\\((\\S+)\\) create new instance with key\\((\\S+)\\): (\\S+)", {
        auto keyType = attrType(context, matches[3].str());
        auto key = keyType->put(context.transaction(), parseValueFromString(keyType->getValueType(), matches[4].str()).get()).get();
        auto entity = entityType(context, matches[2].str())->create(context.transaction()).get();
        entity->setHas(context.transaction(), key.get()).wait();
        context.vars[matches[1].str()] = std::move(entity);
    }),
    BDD_STEP("entity\\((\\S+)\\) create new instance; throws exception", {
        DRIVER_THROWS("", { entityType(context, matches[1].str())->create(context.transaction()).get(); });
    }),

    // Data read
    BDD_STEP("\\$(\\S+) = entity\\((\\S+)\\) get instance with key\\((\\S+)\\): (\\S+)", {
        auto keyType = attrType(context, matches[3].str());
        auto key = keyType->get(context.transaction(), parseValueFromString(keyType->getValueType(), matches[4].str()).get()).get();
        auto entity = std::move(*(key->getOwners(context.transaction(), entityType(context, matches[2].str()).get()).begin()));
        context.vars[matches[1].str()] = std::move(entity);
    }),
};

}  // namespace TypeDB::BDD
