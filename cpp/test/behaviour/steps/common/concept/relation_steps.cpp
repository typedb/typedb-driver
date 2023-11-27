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

std::string stripDollar(const std::string& str) {
    return str.substr(1);
}

bool checkPlayersContain(Context& context, const cucumber::messages::pickle_table& table, const std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>>& toCheckList) {
    return std::all_of(table.rows.begin(), table.rows.end(), [&](const cucumber::messages::pickle_table_row& row) {
        return std::any_of(toCheckList.begin(), toCheckList.end(), [&](const std::pair<const std::unique_ptr<RoleType>, std::unique_ptr<Thing>>& toCheck) {
            return row.cells[0].value == toCheck.first->getName() && Concept::equals(context.vars[stripDollar(row.cells[1].value)].get(), toCheck.second.get());
        });
    });
}

bool checkPlayersNotContain(Context& context, const cucumber::messages::pickle_table& table, const std::map<std::unique_ptr<RoleType>, std::unique_ptr<Thing>>& toCheckList) {
    return std::all_of(table.rows.begin(), table.rows.end(), [&](const cucumber::messages::pickle_table_row& row) {
        return std::none_of(toCheckList.begin(), toCheckList.end(), [&](const std::pair<const std::unique_ptr<RoleType>, std::unique_ptr<Thing>>& toCheck) {
            return row.cells[0].value == toCheck.first->getLabel() && Concept::equals(context.vars[stripDollar(row.cells[1].value)].get(), toCheck.second.get());
        });
    });
}

std::unique_ptr<RoleType> getRole(Context& context, Relation* rel, const std::string& roleLabel) {
    return rel->getType()->getRelates(context.transaction(), roleLabel).get();
}


cucumber_bdd::StepCollection<Context> relationSteps = {

    // Write
    BDD_STEP("\\$(\\S+) = relation\\((\\S+)\\) create new instance", {
        context.vars[matches[1].str()] = relationType(context, matches[2].str())->create(context.transaction()).get();
    }),
    BDD_STEP("\\$(\\S+) = relation\\((\\S+)\\) create new instance with key\\((\\S+)\\): (\\S+)", {
        auto keyType = attrType(context, matches[3].str());
        auto key = keyType->put(context.transaction(), parseValueFromString(keyType->getValueType(), matches[4].str()).get()).get();
        auto relation = relationType(context, matches[2].str())->create(context.transaction()).get();
        relation->setHas(context.transaction(), key.get()).wait();
        context.vars[matches[1].str()] = std::move(relation);
    }),
    BDD_STEP_AND_THROWS("relation \\$(\\S+) add player for role\\((\\S+)\\): \\$(\\S+)", {
        auto relation = context.vars[matches[1].str()]->asRelation();
        relation->asRelation()->addPlayer(context.transaction(), relation->getType()->getRelates(context.transaction(), matches[2].str()).get()->asRoleType(), context.vars[matches[3].str()]->asThing()).wait();
    }),

    BDD_STEP("relation \\$(\\S+) remove player for role\\((\\S+)\\): \\$(\\S+)", {
        auto relation = context.vars[matches[1].str()]->asRelation();
        relation->asRelation()->removePlayer(context.transaction(), relation->getType()->getRelates(context.transaction(), matches[2].str()).get()->asRoleType(), context.vars[matches[3].str()]->asThing()).wait();
    }),

    // Read
    BDD_STEP("relation\\((\\S+)\\) get instances is empty", {
        ASSERT_EQ(0, collect(relationType(context, matches[1].str())->getInstances(context.transaction())).size());
    }),
    BDD_STEP("relation\\((\\S+)\\) get instances contain: \\$(\\S+)", {
        ASSERT_TRUE(containsInstance(collect(relationType(context, matches[1].str())->getInstances(context.transaction())), context.vars[matches[2].str()].get()));
    }),
    BDD_STEP("relation\\((\\S+)\\) get instances do not contain: \\$(\\S+)", {
        ASSERT_FALSE(containsInstance(collect(relationType(context, matches[1].str())->getInstances(context.transaction())), context.vars[matches[2].str()].get()));
    }),

    BDD_STEP("\\$(\\S+) = relation\\((\\S+)\\) get instance with key\\((\\S+)\\): (\\S+)", {
        auto keyType = attrType(context, matches[3].str());
        auto key = keyType->get(context.transaction(), parseValueFromString(keyType->getValueType(), matches[4].str()).get()).get();
        auto owners = collect(key->getOwners(context.transaction(), relationType(context, matches[2].str()).get()));
        context.vars[matches[1].str()] = owners.empty() ? nullptr : std::move(owners[0]);
    }),

    // Read players
    BDD_STEP("relation \\$(\\S+) get players for role\\((\\S+)\\) contain: \\$(\\S+)", {
        assert(nullptr != context.vars[matches[1].str()]);
        Relation* rel = context.vars[matches[1].str()]->asRelation();
        auto players = collect(rel->getPlayersByRoleType(context.transaction(), {getRole(context, rel, matches[2].str()).get()}));
        ASSERT_TRUE(containsInstance(players, context.vars[matches[3].str()].get()));
    }),
    BDD_STEP("relation \\$(\\S+) get players for role\\((\\S+)\\) do not contain: \\$(\\S+)", {
        Relation* rel = context.vars[matches[1].str()]->asRelation();
        auto players = collect(rel->getPlayersByRoleType(context.transaction(), {getRole(context, rel, matches[2].str()).get()}));
        ASSERT_FALSE(containsInstance(players, context.vars[matches[3].str()].get()));
    }),
    BDD_STEP("relation \\$(\\S+) get players contain: \\$(\\S+)", {
        auto playerMap = context.vars[matches[1].str()]->asRelation()->getPlayers(context.transaction());
        std::vector<std::unique_ptr<Thing>> players;
        for (auto& kv : playerMap)
            players.push_back(std::move(kv.second));
        ASSERT_TRUE(containsInstance(players, context.vars[matches[2].str()].get()));
    }),
    BDD_STEP("relation \\$(\\S+) get players contain:", {
        auto playerMap = context.vars[matches[1].str()]->asRelation()->getPlayers(context.transaction());
        ASSERT_TRUE(checkPlayersContain(context, step.argument->data_table.value(), playerMap));
    }),
    BDD_STEP("relation \\$(\\S+) get players do not contain:", {
        auto playerMap = context.vars[matches[1].str()]->asRelation()->getPlayers(context.transaction());
        ASSERT_TRUE(checkPlayersNotContain(context, step.argument->data_table.value(), playerMap));
    }),

    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get players contain:", {
        std::vector<std::string> playerTypeLabels = transform(
            relationType(context, matches[1].str())->getRelates(context.transaction(), matches[2].str()).get()->getPlayerTypes(context.transaction()),
            &label<ThingType>);
        ASSERT_TRUE(checkContains(step.argument->data_table.value(), playerTypeLabels));
    }),
    BDD_STEP("relation\\((\\S+)\\) get role\\((\\S+)\\) get players do not contain:", {
        std::vector<std::string> playerTypeLabels = transform(
            relationType(context, matches[1].str())->getRelates(context.transaction(), matches[2].str()).get()->getPlayerTypes(context.transaction()),
            &label<ThingType>);
        ASSERT_TRUE(checkNotContains(step.argument->data_table.value(), playerTypeLabels));
    }),
};

}  // namespace TypeDB::BDD
