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

#include "typedb/concept/all.hpp"

namespace TypeDB::BDD {

using namespace cucumber::messages;

cucumber_bdd::StepCollection<Context> attributeTypeSteps = {
    // Basic
    BDD_STEP("put attribute type: (.+), with value type: (\\S+)", {
        context.transaction().concepts.putAttributeType(matches[1].str(), parseValueType(matches[2].str())).wait();
    }),

    BDD_STEP_AND_THROWS("attribute\\((\\S+)\\) set supertype: (\\S+)", {
        attrType(context, matches[1].str())->setSupertype(context.transaction(), attrType(context, matches[2].str()).get()).wait();
    }),

    // read
    BDD_STEP("attribute\\((\\S+)\\) is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[2].str()), nullptr == attrType(context, matches[1].str()));
    }),
    BDD_STEP("attribute\\((\\S+)\\) is abstract: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[2].str()), attrType(context, matches[1].str())->isAbstract());
    }),
    BDD_STEP("attribute\\((\\S+)\\) get label: (\\S+)", {
        ASSERT_EQ(matches[2].str(), attrType(context, matches[1].str())->getLabel());
    }),
    BDD_STEP("attribute\\((\\S+)\\) get value type: (\\w+)", {
        ASSERT_EQ(parseValueType(matches[2].str()), attrType(context, matches[1].str())->getValueType());
    }),
    BDD_STEP("attribute\\((\\S+)\\) get subtypes contain:", {
        auto subtypeLabels = transform(attrType(context, matches[1].str())->getSubtypes(context.transaction()), &label<AttributeType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\((\\w+)\\) get subtypes contain:", {
        auto subtypeLabels = transform(attrType(context, matches[1].str())->getSubtypes(context.transaction(), parseValueType(matches[2])), &label<AttributeType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) get subtypes do not contain:", {
        auto subtypeLabels = transform(attrType(context, matches[1].str())->getSubtypes(context.transaction()), &label<AttributeType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), subtypeLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\((\\w+)\\) get subtypes do not contain:", {
        auto subtypeLabels = transform(attrType(context, matches[1].str())->getSubtypes(context.transaction(), parseValueType(matches[2])), &label<AttributeType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), subtypeLabels));
    }),

    // Value constraints
    BDD_STEP("attribute\\((\\S+)\\) as\\(string\\) set regex: (.+)", {
        attrType(context, matches[1].str())->setRegex(context.transaction(), matches[2].str()).wait();
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(string\\) unset regex", {
        attrType(context, matches[1].str())->unsetRegex(context.transaction()).wait();
    }),
    BDD_STEP("attribute\\((\\S+)\\) as\\(string\\) does not have any regex", {
        ASSERT_FALSE(attrType(context, matches[1].str())->getRegex(context.transaction()).get().has_value());
    }),

    BDD_STEP("attribute\\((\\S+)\\) as\\(string\\) get regex: (.+)", {
        ASSERT_EQ(matches[2].str(), attrType(context, matches[1].str())->getRegex(context.transaction()).get().value());
    }),

    // Owns stuff
    BDD_STEP("attribute\\((\\S+)\\) get owners((?: explicit)?) contain:", {
        auto ownerLabels = transform(attrType(context, matches[1].str())->getOwners(context.transaction(), parseTransitivity(matches[2].str())), &label<ThingType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), ownerLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) get owners((?: explicit)?) do not contain:", {
        auto ownerLabels = transform(attrType(context, matches[1].str())->getOwners(context.transaction(), parseTransitivity(matches[2].str())), &label<ThingType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), ownerLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) get owners((?: explicit)?), with annotations: (key|unique); contain:", {
        auto ownerLabels = transform(attrType(context, matches[1].str())->getOwners(context.transaction(), parseAnnotation(matches[3].str()), parseTransitivity(matches[2].str())), &label<ThingType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), ownerLabels));
    }),
    BDD_STEP("attribute\\((\\S+)\\) get owners((?: explicit)?), with annotations: (key|unique); do not contain:", {
        auto ownerLabels = transform(attrType(context, matches[1].str())->getOwners(context.transaction(), parseAnnotation(matches[3].str()), parseTransitivity(matches[2].str())), &label<ThingType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), ownerLabels));
    }),

};

}  // namespace TypeDB::BDD
