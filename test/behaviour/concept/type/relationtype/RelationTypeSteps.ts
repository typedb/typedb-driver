/*
 * Copyright (C) 2021 Vaticle
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

import {tx} from "../../../connection/ConnectionStepsBase";
import {Then, When} from "@cucumber/cucumber";
import {parseList, ScopedLabel} from "../../../config/Parameters";
import {assertThrows} from "../../../util/Util";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import assert = require("assert");


When("relation\\({type_label}) set relates role: {type_label}", async (relationLabel: string, roleLabel: string) => {
    await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).setRelates(roleLabel);
});

When("relation\\({type_label}) set relates role: {type_label}; throws exception", async (relationLabel: string, roleLabel: string) => {
    await assertThrows(async () => await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).setRelates(roleLabel));
});

When("relation\\({type_label}) unset related role: {type_label}", async (relationLabel: string, roleLabel: string) => {
    await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).unsetRelates(roleLabel);
});

When("relation\\({type_label}) unset related role: {type_label}; throws exception", async (relationLabel: string, roleLabel: string) => {
    await assertThrows(async () => await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).unsetRelates(roleLabel));
});

When("relation\\({type_label}) set relates role: {type_label} as {type_label}", async (relationLabel: string, roleLabel: string, superRole: string) => {
    await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).setRelates(roleLabel, superRole);
});

When("relation\\({type_label}) set relates role: {type_label} as {type_label}; throws exception", async (relationLabel: string, roleLabel: string, superRole: string) => {
    await assertThrows(async () => await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).setRelates(roleLabel, superRole));
});

When("relation\\({type_label}) remove related role: {type_label}", async (relationLabel: string, roleLabel: string) => {
    const relatedRole = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    await relatedRole.asRemote(tx()).delete();
});

Then("relation\\({type_label}) get role\\({type_label}) is null: {bool}", async (relationLabel: string, roleLabel: string, isNull: boolean) => {
    assert.strictEqual(!(await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel)), isNull);
});

When("relation\\({type_label}) get role\\({type_label}) set label: {type_label}", async (relationLabel: string, roleLabel: string, newLabel: string) => {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    await roleType.asRemote(tx()).setLabel(newLabel);
});

When("relation\\({type_label}) get role\\({type_label}) get label: {type_label}", async (relationLabel: string, roleLabel: string, getLabel: string) => {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    assert.strictEqual(roleType.getLabel().name(), getLabel);
});

When("relation\\({type_label}) get role\\({type_label}) is abstract: {bool}", async (relationLabel: string, roleLabel: string, isAbstract: boolean) => {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    assert.strictEqual(await roleType.asRemote(tx()).isAbstract(), isAbstract);
});

async function getActualRelatedRoles(relationLabel: string) {
    return ((await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates()).map(role => role.getLabel().scopedName()).collect();
}

Then("relation\\({type_label}) get related roles contain:", async (relationLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await getActualRelatedRoles(relationLabel);
    await actuals.every(rl => assert(roleLabels.includes(rl)));
});

Then("relation\\({type_label}) get related roles do not contain:", async (relationLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await getActualRelatedRoles(relationLabel);
    await actuals.every(rl => assert(!roleLabels.includes(rl)));
});

Then("relation\\({type_label}) get role\\({type_label}) get supertype: {scoped_label}", async (relationLabel: string, roleLabel: string, superLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    const superType = await (await tx().concepts().getRelationType(superLabel.scope())).asRemote(tx()).getRelates(superLabel.role());
    assert((await roleType.asRemote(tx()).getSupertype()).equals(superType));
});

async function getActualSupertypesForRelatedRole(relationLabel: string, roleLabel: string) {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    return roleType.asRemote(tx()).getSupertypes().map(role => role.getLabel().scopedName()).collect();
}

Then("relation\\({type_label}) get role\\({type_label}) get supertypes contain:", async (relationLabel: string, roleLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const actuals = await getActualSupertypesForRelatedRole(relationLabel, roleLabel);
    await actuals.every(rl => assert(superLabels.includes(rl)));
});

Then("relation\\({type_label}) get role\\({type_label}) get supertypes do not contain:", async (relationLabel: string, roleLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const actuals = await getActualSupertypesForRelatedRole(relationLabel, roleLabel);
    await actuals.every(rl => assert(!superLabels.includes(rl)));
});

async function getActualPlayersForRelatedRole(relationLabel: string, roleLabel: string) {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    return await roleType.asRemote(tx()).getPlayers().map(tt => tt.getLabel().scopedName()).collect();
}

Then("relation\\({type_label}) get role\\({type_label}) get players contain:", async (relationLabel: string, roleLabel: string, playerLabelsTable: DataTable) => {
    const playerLabels = parseList(playerLabelsTable);
    const actuals = await getActualPlayersForRelatedRole(relationLabel, roleLabel);
    await actuals.every(pl => assert(playerLabels.includes(pl)));
});

Then("relation\\({type_label}) get role\\({type_label}) get players do not contain:", async (relationLabel: string, roleLabel: string, playerLabelsTable: DataTable) => {
    const playerLabels = parseList(playerLabelsTable);
    const actuals = await getActualPlayersForRelatedRole(relationLabel, roleLabel);
    await actuals.every(pl => assert(!playerLabels.includes(pl)));
});

async function getActualSubtypesForRelatedRole(relationLabel: string, roleLabel: string) {
    const roleType = await (await tx().concepts().getRelationType(relationLabel)).asRemote(tx()).getRelates(roleLabel);
    return roleType.asRemote(tx()).getSubtypes().map(role => role.getLabel().scopedName()).collect();
}

Then("relation\\({type_label}) get role\\({type_label}) get subtypes contain:", async (relationLabel: string, roleLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const actuals = await getActualSubtypesForRelatedRole(relationLabel, roleLabel);
    await actuals.every(rl => assert(subLabels.includes(rl)));
});

Then("relation\\({type_label}) get role\\({type_label}) get subtypes do not contain:", async (relationLabel: string, roleLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const actuals = await getActualSubtypesForRelatedRole(relationLabel, roleLabel);
    await actuals.every(rl => assert(!subLabels.includes(rl)));
});
