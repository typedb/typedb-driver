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

std::unique_ptr<ThingType> asThingType(Context& context, const std::string& thingClass, const std::string& thingLabel) {
    if (thingClass == "attribute") {
        return attrType(context, thingLabel);
    } else if (thingClass == "entity") {
        return entityType(context, thingLabel);
    } else if (thingClass == "relation") {
        return relationType(context, thingLabel);
    } else THROW_ILLEGAL_STATE();
}

std::vector<std::string> ownsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel) {
    return transform(asThingType(context, thingClass, thingLabel)->getOwns(context.transaction()), &label<AttributeType>);
}

std::vector<std::string> ownsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel, std::vector<Annotation> annotations) {
    return transform(asThingType(context, thingClass, thingLabel)->getOwns(context.transaction(), annotations), &label<AttributeType>);
}

std::vector<std::string> ownsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel, Transitivity transitivity) {
    return transform(asThingType(context, thingClass, thingLabel)->getOwns(context.transaction(), transitivity), &label<AttributeType>);
}


std::vector<std::string> ownsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel, std::vector<Annotation> annotations, Transitivity transitivity) {
    return transform(asThingType(context, thingClass, thingLabel)->getOwns(context.transaction(), annotations, transitivity), &label<AttributeType>);
}

std::vector<std::string> playsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel) {
    return transform(asThingType(context, thingClass, thingLabel)->getPlays(context.transaction()), &label<RoleType>);
}

std::vector<std::string> playsLabels(Context& context, const std::string& thingClass, const std::string& thingLabel, Transitivity transitivity) {
    return transform(asThingType(context, thingClass, thingLabel)->getPlays(context.transaction(), transitivity), &label<RoleType>);
}


cucumber_bdd::StepCollection<Context> thingTypeSteps = {
    // Basic
    BDD_STEP_AND_THROWS("delete (attribute|entity|relation) type: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())->drop(context.transaction()).wait();
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) set label: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())->setLabel(context.transaction(), matches[3].str()).wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set abstract: (true|false)", {
        if (parseBoolean(matches[3].str())) {
            asThingType(context, matches[1].str(), matches[2].str())->setAbstract(context.transaction()).wait();
        } else {
            asThingType(context, matches[1].str(), matches[2].str())->unsetAbstract(context.transaction()).wait();
        }
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) is null: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[3].str()), nullptr == asThingType(context, matches[1].str(), matches[2].str()));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get label: (\\S+)", {
        ASSERT_EQ(matches[3].str(), asThingType(context, matches[1].str(), matches[2].str())->getLabel());
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) is abstract: (true|false)", {
        ASSERT_EQ(parseBoolean(matches[3].str()), asThingType(context, matches[1].str(), matches[2].str())->isAbstract());
    }),
    // Supertypes (Subtypes are in individual files)
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get supertype: (\\S+)", {
        ASSERT_EQ(matches[3].str(), asThingType(context, matches[1].str(), matches[2].str())->getSupertype(context.transaction()).get()->getLabel());
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get supertypes contain:", {
        auto supertypeLabels = transform(asThingType(context, matches[1].str(), matches[2].str())->getSupertypes(context.transaction()), &label<ThingType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), supertypeLabels));
    }),

    // Owns
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns attribute types contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), ownsLabels(context, matches[1].str(), matches[2].str())));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns attribute types do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), ownsLabels(context, matches[1].str(), matches[2].str())));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns attribute types, with annotations: (key|unique); contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), ownsLabels(context, matches[1].str(), matches[2].str(), parseAnnotation(matches[3].str()))));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns attribute types, with annotations: (key|unique); do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), ownsLabels(context, matches[1].str(), matches[2].str(), parseAnnotation(matches[3].str()))));
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns explicit attribute types contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(),
                                  ownsLabels(context, matches[1].str(), matches[2].str(), Transitivity::EXPLICIT)));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns explicit attribute types do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(),
                                     ownsLabels(context, matches[1].str(), matches[2].str(), Transitivity::EXPLICIT)));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns explicit attribute types, with annotations: (key|unique); contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(),
                                  ownsLabels(context, matches[1].str(), matches[2].str(), parseAnnotation(matches[3].str()), Transitivity::EXPLICIT)));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns explicit attribute types, with annotations: (key|unique); do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(),
                                     ownsLabels(context, matches[1].str(), matches[2].str(), parseAnnotation(matches[3].str()), Transitivity::EXPLICIT)));
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns overridden attribute\\((\\S+)\\) get label: (\\S+)", {
        std::unique_ptr<ThingType> ownerType = asThingType(context, matches[1].str(), matches[2].str());
        ASSERT_EQ(matches[4].str(),
                  ownerType->getOwnsOverridden(context.transaction(), attrType(context, matches[3].str()).get()).get()->getLabel());
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get owns overridden attribute\\((\\S+)\\) is null: (true|false)", {
        std::unique_ptr<ThingType> ownerType = asThingType(context, matches[1].str(), matches[2].str());
        ASSERT_EQ(parseBoolean(matches[4].str()),
                  nullptr == ownerType->getOwnsOverridden(context.transaction(), attrType(context, matches[3].str()).get()).get());
    }),


    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set owns attribute type: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setOwns(context.transaction(), attrType(context, matches[3].str())->asAttributeType())
            .wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set owns attribute type: (\\S+) as (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setOwns(context.transaction(), attrType(context, matches[3].str())->asAttributeType(), attrType(context, matches[4].str())->asAttributeType())
            .wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set owns attribute type: (\\S+), with annotations: (key|unique)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setOwns(context.transaction(), attrType(context, matches[3].str())->asAttributeType(), parseAnnotation(matches[4].str()))
            .wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set owns attribute type: (\\S+) as (\\S+), with annotations: (key|unique)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setOwns(context.transaction(), attrType(context, matches[3].str())->asAttributeType(),
                      attrType(context, matches[4].str())->asAttributeType(), parseAnnotation(matches[5].str()))
            .wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) unset owns attribute type: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->unsetOwns(context.transaction(), attrType(context, matches[3].str())->asAttributeType())
            .wait();
    }),

    // Plays
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set plays role: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setPlays(context.transaction(), roleType(context, matches[3].str()).get())
            .wait();
    }),
    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) set plays role: (\\S+) as (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->setPlays(context.transaction(), roleType(context, matches[3].str()).get(), roleType(context, matches[4].str()).get())
            .wait();
    }),

    BDD_STEP_AND_THROWS("(attribute|entity|relation)\\((\\S+)\\) unset plays role: (\\S+)", {
        asThingType(context, matches[1].str(), matches[2].str())
            ->unsetPlays(context.transaction(), roleType(context, matches[3].str()).get())
            .wait();
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get playing roles contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(),
                                  playsLabels(context, matches[1].str(), matches[2].str())));
        // asThingType(context, thingClass, thingLabel)->getPlaysOverridden(context.transaction(), roleType(context, overridenRoleLabel.get()))
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get playing roles do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(),
                                     playsLabels(context, matches[1].str(), matches[2].str())));
    }),

    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get playing roles explicit contain:", {
        ASSERT_TRUE(checkContains(step.argument->data_table.value(),
                                  playsLabels(context, matches[1].str(), matches[2].str(), Transitivity::EXPLICIT)));
    }),
    BDD_STEP("(attribute|entity|relation)\\((\\S+)\\) get playing roles explicit do not contain:", {
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(),
                                     playsLabels(context, matches[1].str(), matches[2].str(), Transitivity::EXPLICIT)));
    }),

};

}  // namespace TypeDB::BDD
