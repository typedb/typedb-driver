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

import { tx } from "../../../connection/ConnectionSteps";
import { When } from "@cucumber/cucumber";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import assert from "assert";
import { RootLabel } from "../../../config/Parameters";
import { assertThrows } from "../../../util/Util";

export function getThingType(rootLabel: RootLabel, typeLabel: string){
    switch(rootLabel) {
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

When("thing type root get supertypes contain:", async (supertypes: DataTable) => {
    const actualSupertypes = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSupertypes().collect();
    for (const type of supertypes.raw()) {
        let found = false;
        for (const actualType of actualSupertypes) {
            if (actualType.getLabel() === type[0]) {found = true; break;}
        }
        assert.ok(found);
    }
});

When("thing type root get supertypes do not contain:", async (supertypes: DataTable) => {
    const actualSupertypes = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSupertypes().collect();
    for (const type of supertypes.raw()) {
        for (const actualType of actualSupertypes) {
            if (actualType.getLabel() === type[0]) assert.fail();
        }
    }
});

When("thing type root get subtypes contain:", async (supertypes: DataTable) => {
    const actualSupertypes = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSubtypes().collect();
    for (const type of supertypes.raw()) {
        let found = false;
        for (const actualType of actualSupertypes) {
            if (actualType.getLabel() === type[0]) {found = true; break;}
        }
        assert.ok(found);
    }
});

When("thing type root get subtypes do not contain:", async (supertypes: DataTable) => {
    const actualSupertypes = await (await tx().concepts().getRootThingType()).asRemote(tx()).getSubtypes().collect();
    for (const type of supertypes.raw()) {
        for (const actualType of actualSupertypes) {
            if (actualType.getLabel() === type[0]) assert.fail();
        }
    }
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
    await assert.strictEqual(null === await getThingType(rootLabel, typeLabel), isNull);
});

When("{root_label}\\({type_label}) is null: {bool}", async (rootLabel: RootLabel, typeLabel: string, isNull: boolean) => {
    await assert.strictEqual(null === await getThingType(rootLabel, typeLabel), isNull);
});

When("{root_label}\\({type_label}) set label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setLabel(label);
});

When("{root_label}\\({type_label}) get label: {type_label}", async (rootLabel: RootLabel, typeLabel: string, label: string) => {
    await assert.strictEqual((await getThingType(rootLabel, typeLabel)).asRemote(tx()).getLabel(), label);
});

When("{root_label}\\({type_label}) set abstract: {bool}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    if (isAbstract) {
        await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).setAbstract();
    } else {
        await (await getThingType(rootLabel, typeLabel)).asRemote(tx()).unsetAbstract();
    }
});

When("{root_label}\\({type_label}) is abstract: {type_label}", async (rootLabel: RootLabel, typeLabel: string, isAbstract: boolean) => {
    await assert.strictEqual((await getThingType(rootLabel, typeLabel)).asRemote(tx()).isAbstract(), isAbstract);
});
