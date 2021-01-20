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

import { After, When } from "@cucumber/cucumber";
import { Thing } from "../../../../dist/concept/thing/Thing";
import assert = require("assert");
import { tx } from "../../connection/ConnectionSteps";
import { getThingType } from "../type/thingtype/ThingTypeSteps";
import { assertThrows } from "../../util/Util";
import { Attribute } from "../../../../dist/concept/thing/Attribute";
import { AttributeType } from "../../../../dist/concept/type/AttributeType";
import ValueClass = AttributeType.ValueClass;
import { RootLabel, ScopedLabel } from "../../config/Parameters";

export const things: Map<string, Thing> = new Map<string, Thing>();
export const get: (name: string) => Thing = (name: string) => things.get(name);
export const put: (name: string, thing: Thing) => void = (name: string, thing: Thing) => things.set(name, thing);
export const clearThings: () => void = () => things.clear();

After(clearThings);

When("entity/attribute/relation {var} is null: {bool}", async (thingName: string, isNull: boolean) => {
    if (things.has(thingName)) assert.ok((things.get(thingName) === null) === isNull)
    else assert.ok(isNull);
});

When("entity/attribute/relation {var} is deleted: {bool}", async (thingName: string, isDeleted: boolean) => {
    assert.ok(isDeleted === await get(thingName).asRemote(tx()).isDeleted());
});

When("{root_label} {var} has type: {type_label}", async (rootLabel: RootLabel, thingName: string, label: string) => {
    const desiredType = await getThingType(rootLabel, label);
    assert((await get(thingName).asRemote(tx()).getType()).equals(desiredType));
});

When("delete entity:/attribute:/relation: {var}", async (thingName: string) => {
    await get(thingName).asRemote(tx()).delete();
});

When("entity/attribute/relation {var} set has: {var}", async (thingName: string, attributeName: string) => {
    await get(thingName).asRemote(tx()).setHas(get(attributeName) as Attribute<ValueClass>);
});

When("entity/attribute/relation {var} set has: {var}; throws exception", async (thingName: string, attributeName: string) => {
    await assertThrows(async () => get(thingName).asRemote(tx()).setHas(get(attributeName) as Attribute<ValueClass>));
});

When("entity/attribute/relation {var} unset has: {var}", async (thingName: string, attributeName: string) => {
    await get(thingName).asRemote(tx()).unsetHas(get(attributeName) as Attribute<ValueClass>);
});

When("entity/attribute/relation {var} get keys contain: {var}", async (thingName: string, attributeName: string) => {
    for await (const keyAttribute of get(thingName).asRemote(tx()).getHas(true)) {
        if (keyAttribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get keys do not contain: {var}", async (thingName: string, attributeName: string) => {
    for await (const keyAttribute of get(thingName).asRemote(tx()).getHas(true)) {
        if (keyAttribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes contain: {var}", async (thingName: string, attributeName: string) => {
    for await (const attribute of get(thingName).asRemote(tx()).getHas()) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes do not contain: {var}", async (thingName: string, attributeName: string) => {
    for await (const attribute of get(thingName).asRemote(tx()).getHas()) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = await tx().concepts().getAttributeType(typeLabel)
    for await (const attribute of get(thingName).asRemote(tx()).getHas([type])) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(boolean) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asBoolean();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(long) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asLong();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(double) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asDouble();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(string) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asString();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(datetime) contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asDateTime();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get attributes\\({type_label}) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = await tx().concepts().getAttributeType(typeLabel)
    for await (const attribute of get(thingName).asRemote(tx()).getHas([type])) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(boolean) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asBoolean();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(long) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asLong();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(double) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asDouble();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(string) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asString();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get attributes\\({type_label}) as\\(datetime) do not contain: {var}", async (thingName: string, typeLabel: string, attributeName: string) => {
    const type = (await tx().concepts().getAttributeType(typeLabel)).asDateTime();
    for await (const attribute of get(thingName).asRemote(tx()).getHas(type)) {
        if (attribute.equals(get(attributeName))) assert.fail();
    }
});

When("entity/attribute/relation {var} get relations\\({scoped_label}) contain: {var}", async (thingName: string, scopedLabel: ScopedLabel, relationName: string) => {
    for await (const relation of get(thingName).asRemote(tx()).getRelations([await (await tx().concepts().getRelationType(scopedLabel.scope())).asRemote(tx()).getRelates(scopedLabel.role())])) {
        if (relation.equals(get(relationName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get relations contain: {var}", async (thingName: string, relationName: string) => {
    for await (const relation of get(thingName).asRemote(tx()).getRelations()) {
        if (relation.equals(get(relationName))) return;
    }
    assert.fail();
});

When("entity/attribute/relation {var} get relations\\({scoped_label}) do not contain: {var}", async (thingName: string, scopedLabel: ScopedLabel, relationName: string) => {
    for await (const relation of get(thingName).asRemote(tx()).getRelations([await (await tx().concepts().getRelationType(scopedLabel.scope())).asRemote(tx()).getRelates(scopedLabel.role())])) {
        if (relation.equals(get(relationName))) assert.fail();
    }

});

When("entity/attribute/relation {var} get relations do not contain: {var}", async (thingName: string, relationName: string) => {
    for await (const relation of get(thingName).asRemote(tx()).getRelations()) {
        if (relation.equals(get(relationName))) assert.fail();
    }
});

When("root\\(thing) get instances count: {int}", async (count: number) => {
    assert.strictEqual((await (await tx().concepts().getRootThingType()).asRemote(tx()).getInstances().collect()).length, count);
});

When("root\\(thing) get instances contain: {var}", async (variableName: string) => {
    for await (const instance of (await tx().concepts().getRootThingType()).asRemote(tx()).getInstances()) {
        if (instance.equals(get(variableName))) return;
    }
    assert.fail();
});

When("root\\(thing) get instances is empty", async () => {
    assert.strictEqual((await (await tx().concepts().getRootThingType()).asRemote(tx()).getInstances().collect()).length, 0);
});
