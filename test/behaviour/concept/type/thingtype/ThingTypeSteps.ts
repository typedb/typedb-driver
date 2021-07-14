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

import { Then, When } from "@cucumber/cucumber";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import assert from "assert";
import { ThingType } from "../../../../../dist";
import { parseList, RootLabel, ScopedLabel } from "../../../config/Parameters";
import { tx } from "../../../connection/ConnectionStepsBase";
import { assertThrows } from "../../../util/Util";

export function getThingType(rootLabel: RootLabel, typeLabel: string): Promise<ThingType> {
    switch (rootLabel) {
        case RootLabel.ENTITY:
            return tx().concepts().getEntityType(typeLabel);
        case RootLabel.ATTRIBUTE:
            return tx().concepts().getAttributeType(typeLabel);
        case RootLabel.RELATION:
            return tx().concepts().getRelationType(typeLabel);
        default:
            throw "Unsupported type"
    }
}

When("thing type root get supertypes contain:", async (supertypesTable: DataTable) => {
    const supertypes = parseList(supertypesTable);
    const actuals = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSupertypes().map(tt => tt.getLabel().scopedName()).collect();
    supertypes.every(st => assert(actuals.includes(st)));
});

When("thing type root get supertypes do not contain:", async (supertypesTable: DataTable) => {
    const supertypes = parseList(supertypesTable);
    const actuals = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSupertypes().map(tt => tt.getLabel().scopedName()).collect();
    supertypes.every(st => assert(!actuals.includes(st)));
});

When("thing type root get subtypes contain:", async (subtypesTable: DataTable) => {
    const subtypes = parseList(subtypesTable);
    const actuals = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    subtypes.every(st => assert(actuals.includes(st)));
});

When("thing type root get subtypes do not contain:", async (subtypesTable: DataTable) => {
    const subtypes = parseList(subtypesTable);
    const actuals = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    subtypes.every(st => assert(!actuals.includes(st)));
});

When("put {root_label} type: {type_label}", async (rootLabel: RootLabel, typeLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY:
            await tx().concepts().putEntityType(typeLabel);
            break;
        case RootLabel.RELATION:
            await tx().concepts().putRelationType(typeLabel);
            break;
        default:
            throw `Could not put ${typeLabel} of type Attribute`;
    }
});

When("delete {root_label} type: {type_label}", async (rootLabel: RootLabel, typeLabel: string) => {
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).delete();
});

When("delete {root_label} type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string) => {
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).delete());
});

When("{root_label}\\({type_label}) is null: {bool}", async (rootLabel: RootLabel, typeLabel: string, isNull: boolean) => {
    assert.strictEqual(null === await getThingType(rootLabel, typeLabel), isNull);
});

When("{root_label}\\({type_label}) set label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setLabel(label);
});

When("{root_label}\\({type_label}) get label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await assert.strictEqual((await getThingType(rootLabel, typeLabel)).asRemote(tx()).getLabel().scopedName(), label);
});

When("{root_label}\\({type_label}) set abstract: {bool}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    if (isAbstract) {
        await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setAbstract();
    } else {
        await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetAbstract();
    }
});

When("{root_label}\\({type_label}) is abstract: {bool}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    await assert.strictEqual(await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).isAbstract(), isAbstract);
});

When("{root_label}\\({type_label}) set supertype: {type_label}", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY: {
            const entitySuperType = await tx().concepts().getEntityType(superLabel);
            return await (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).setSupertype(entitySuperType);
        }
        case RootLabel.ATTRIBUTE: {
            const attributeSuperType = await tx().concepts().getAttributeType(superLabel);
            return await (await tx().concepts().getAttributeType(typeLabel)).asRemote(tx()).setSupertype(attributeSuperType);
        }
        case RootLabel.RELATION: {
            const relationSuperType = await tx().concepts().getRelationType(superLabel);
            return await (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).setSupertype(relationSuperType);
        }
    }
});

When("{root_label}\\({type_label}) set supertype: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    switch (rootLabel) {
        case RootLabel.ENTITY: {
            const entitySuperType = await tx().concepts().getEntityType(superLabel);
            return await assertThrows(async () => await (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).setSupertype(entitySuperType));
        }
        case RootLabel.ATTRIBUTE: {
            const attributeSuperType = await tx().concepts().getAttributeType(superLabel);
            return await assertThrows(async () => await (await tx().concepts().getAttributeType(typeLabel)).asRemote(tx()).setSupertype(attributeSuperType));
        }
        case RootLabel.RELATION: {
            const relationSuperType = await tx().concepts().getRelationType(superLabel);
            return await assertThrows(async () => await (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).setSupertype(relationSuperType));
        }
    }
});

Then("{root_label}\\({type_label}) get supertype: {type_label}", async (rootLabel: RootLabel, typeLabel: string, superLabel: string) => {
    const supertype = await getThingType(rootLabel, superLabel);
    assert((await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getSupertype()).equals(supertype));
});

Then("{root_label}\\({type_label}) get supertypes contain:", async (rootLabel: RootLabel, typeLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.asRemote(tx()).getSupertypes().map(tt => tt.getLabel().scopedName()).collect();
    superLabels.every(sl => assert(actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get supertypes do not contain:", async (rootLabel: RootLabel, typeLabel: string, superLabelsTable: DataTable) => {
    const superLabels = parseList(superLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.asRemote(tx()).getSupertypes().map(tt => tt.getLabel().scopedName()).collect();
    superLabels.every(sl => assert(!actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get subtypes contain:", async (rootLabel: RootLabel, typeLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    subLabels.every(sl => assert(actuals.includes(sl)));
});

Then("{root_label}\\({type_label}) get subtypes do not contain:", async (rootLabel: RootLabel, typeLabel: string, subLabelsTable: DataTable) => {
    const subLabels = parseList(subLabelsTable);
    const thingType = await getThingType(rootLabel, typeLabel);
    const actuals = await thingType.asRemote(tx()).getSubtypes().map(tt => tt.getLabel().scopedName()).collect();
    subLabels.every(sl => assert(!actuals.includes(sl)));
});

When("{root_label}\\({type_label}) set owns key type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attTypeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attTypeLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, true);
});

When("{root_label}\\({type_label}) set owns key type: {type_label} as {type_label}", async (rootLabel: RootLabel, typeLabel: string, attTypeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attTypeLabel);
    const overriddenType = await tx().concepts().getAttributeType(overriddenLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, overriddenType, true);
});

Then("{root_label}\\({type_label}) set owns key type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, true));
});

Then("{root_label}\\({type_label}) set owns key type: {type_label} as {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    const overriddenType = await tx().concepts().getAttributeType(overriddenLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, overriddenType, true));
});

When("{root_label}\\({type_label}) unset owns key type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetOwns(attributeType);
});

Then("{root_label}\\({type_label}) get owns key types contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getOwns(true).map(tt => tt.getLabel().scopedName()).collect();
    attributeLabels.every(al => assert(actuals.includes(al)));
});

Then("{root_label}\\({type_label}) get owns key types do not contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getOwns(true).map(tt => tt.getLabel().scopedName()).collect();
    attributeLabels.every(al => assert(!actuals.includes(al)));
});

When("{root_label}\\({type_label}) set owns attribute type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType);
});

Then("{root_label}\\({type_label}) set owns attribute type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType));
});

When("{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    const overriddenType = await tx().concepts().getAttributeType(overriddenLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, overriddenType);
});

Then("{root_label}\\({type_label}) set owns attribute type: {type_label} as {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string, overriddenLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    const overriddenType = await tx().concepts().getAttributeType(overriddenLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setOwns(attributeType, overriddenType));
});

When("{root_label}\\({type_label}) unset owns attribute type: {type_label}", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetOwns(attributeType);
});

When("{root_label}\\({type_label}) unset owns attribute type: {type_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, attributeLabel: string) => {
    const attributeType = await tx().concepts().getAttributeType(attributeLabel);
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetOwns(attributeType));
});

Then("{root_label}\\({type_label}) get owns attribute types contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getOwns().map(tt => tt.getLabel().scopedName()).collect();
    attributeLabels.every(al => assert(actuals.includes(al)));
});

Then("{root_label}\\({type_label}) get owns attribute types do not contain:", async (rootLabel: RootLabel, typeLabel: string, attributeLabelsTable: DataTable) => {
    const attributeLabels = parseList(attributeLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getOwns().map(tt => tt.getLabel().scopedName()).collect();
    attributeLabels.every(al => assert(!actuals.includes(al)));
});

When("{root_label}\\({type_label}) set plays role: {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setPlays(roleType);
});

When("{root_label}\\({type_label}) set plays role: {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setPlays(roleType));
});

When("{root_label}\\({type_label}) set plays role: {scoped_label} as {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel, overriddenLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    const overriddenType = await (await tx().concepts().getRelationType(overriddenLabel.scope())).asRemote(tx()).getRelates(overriddenLabel.role());
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setPlays(roleType, overriddenType);
});

When("{root_label}\\({type_label}) set plays role: {scoped_label} as {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel, overriddenLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    const overriddenType = await (await tx().concepts().getRelationType(overriddenLabel.scope())).asRemote(tx()).getRelates(overriddenLabel.role());
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setPlays(roleType, overriddenType));
});

When("{root_label}\\({type_label}) unset plays role: {scoped_label}", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetPlays(roleType);
});

When("{root_label}\\({type_label}) unset plays role: {scoped_label}; throws exception", async (rootLabel: RootLabel, typeLabel: string, roleLabel: ScopedLabel) => {
    const roleType = await (await tx().concepts().getRelationType(roleLabel.scope())).asRemote(tx()).getRelates(roleLabel.role());
    await assertThrows(async () => await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetPlays(roleType));
});

Then("{root_label}\\({type_label}) get playing roles contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getPlays().map(r => r.getLabel().scopedName()).collect();
    roleLabels.every(rl => assert(actuals.includes(rl)));
});

Then("{root_label}\\({type_label}) get playing roles do not contain:", async (rootLabel: RootLabel, typeLabel: string, roleLabelsTable: DataTable) => {
    const roleLabels = parseList(roleLabelsTable);
    const actuals = await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).getPlays().map(r => r.getLabel().scopedName()).collect();
    roleLabels.every(rl => assert(!actuals.includes(rl)));
});
