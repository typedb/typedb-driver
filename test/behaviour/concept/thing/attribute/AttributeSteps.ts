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

import { When } from "@cucumber/cucumber";
import { getThing, putThing, things } from "../ThingSteps";
import { tx } from "../../../connection/ConnectionSteps";
import assert = require("assert");
import {
    AttributeType,
    BooleanAttributeType, DateTimeAttributeType,
    DoubleAttributeType,
    LongAttributeType, StringAttributeType
} from "../../../../../dist/concept/type/AttributeType";
import ValueType = AttributeType.ValueType;
import {
    Attribute,
    BooleanAttribute, DateTimeAttribute,
    DoubleAttribute,
    LongAttribute, StringAttribute
} from "../../../../../dist/concept/thing/Attribute";
import { assertThrows } from "../../../util/Util";

When("attribute\\({type_label}) get instances contain: {var}", async (attributeTypeLabel: string, attributeName: string) => {
    const type = await tx().concepts().getAttributeType(attributeTypeLabel)
    for await (const attribute of type.asRemote(tx()).getInstances()) {
        if (attribute.equals(getThing(attributeName))) return;
    }
    assert.fail();
});

When("attribute\\({type_label}) get instances do not contain: {var}", async (attributeTypeLabel: string, attributeName: string) => {
    const type = await tx().concepts().getAttributeType(attributeTypeLabel)
    for await (const attribute of type.asRemote(tx()).getInstances()) {
        if (attribute.equals(getThing(attributeName))) assert.fail();
    }
});

When("attribute\\({type_label}) get owners contain: {var}", async (attributeTypeLabel: string, ownerName: string) => {
    const type = await tx().concepts().getAttributeType(attributeTypeLabel)
    for await (const attribute of type.asRemote(tx()).getOwners()) {
        if (attribute.equals(getThing(ownerName))) return;
    }
    assert.fail();
});

When("attribute\\({type_label}) get owners contain: {var}", async (attributeTypeLabel: string, ownerName: string) => {
    const type = await tx().concepts().getAttributeType(attributeTypeLabel)
    for await (const attribute of type.asRemote(tx()).getOwners()) {
        if (attribute.equals(getThing(ownerName))) assert.fail();
    }
});

When("attribute {var} has value type: {value_type}", async (attribute: string, valueType: ValueType) => {
    assert.strictEqual(valueType, (await (getThing(attribute) as Attribute<ValueType>).asRemote(tx()).getType()).getValueType())
});

When("{var} = attribute\\({type_label}) as\\(boolean) put: {bool}", async (attributeName: string, typeLabel: string, value: boolean) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as BooleanAttributeType).asRemote(tx()).put(value))
});

When("attribute\\({type_label}) as\\(boolean) put: {bool}; throws exception", async (typeLabel: string, value: boolean) => {
    await assertThrows(async () => await ((await tx().concepts().getAttributeType(typeLabel)) as BooleanAttributeType).asRemote(tx()).put(value))
});

When("{var} = attribute\\({type_label}) as\\(long) put: {int}", async (attributeName: string, typeLabel: string, value: number) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as LongAttributeType).asRemote(tx()).put(value))
});

When("attribute\\({type_label}) as\\(long) put: {int}; throws exception", async (typeLabel: string, value: number) => {
    await assertThrows(async () => await ((await tx().concepts().getAttributeType(typeLabel)) as LongAttributeType).asRemote(tx()).put(value))
});

When("{var} = attribute\\({type_label}) as\\(double) put: {float}", async (attributeName: string, typeLabel: string, value: number) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as DoubleAttributeType).asRemote(tx()).put(value))
});

When("attribute\\({type_label}) as\\(double) put: {float}; throws exception", async (typeLabel: string, value: number) => {
    await assertThrows(async () => await ((await tx().concepts().getAttributeType(typeLabel)) as DoubleAttributeType).asRemote(tx()).put(value))
});

When("{var} = attribute\\({type_label}) as\\(string) put: {word}", async (attributeName: string, typeLabel: string, value: string) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as StringAttributeType).asRemote(tx()).put(value))
});

When("attribute\\({type_label}) as\\(string) put: {word}; throws exception", async (typeLabel: string, value: string) => {
    await assertThrows(async () => await ((await tx().concepts().getAttributeType(typeLabel)) as StringAttributeType).asRemote(tx()).put(value))
});

When("{var} = attribute\\({type_label}) as\\(datetime) put: {datetime}", async (attributeName: string, typeLabel: string, value: Date) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as DateTimeAttributeType).asRemote(tx()).put(value))
});

When("attribute\\({type_label}) as\\(datetime) put: {datetime}; throws exception", async (typeLabel: string, value: Date) => {
    await assertThrows(async () => await ((await tx().concepts().getAttributeType(typeLabel)) as DateTimeAttributeType).asRemote(tx()).put(value))
});

When("{var} = attribute\\({type_label}) as\\(boolean) get: {bool}", async (attributeName: string, typeLabel: string, value: boolean) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as BooleanAttributeType).asRemote(tx()).get(value))
});

When("{var} = attribute\\({type_label}) as\\(long) get: {int}", async (attributeName: string, typeLabel: string, value: number) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as LongAttributeType).asRemote(tx()).get(value))
});

When("{var} = attribute\\({type_label}) as\\(double) get: {float}", async (attributeName: string, typeLabel: string, value: number) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as DoubleAttributeType).asRemote(tx()).get(value))
});

When("{var} = attribute\\({type_label}) as\\(string) get: {word}", async (attributeName: string, typeLabel: string, value: string) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as StringAttributeType).asRemote(tx()).get(value))
});

When("{var} = attribute\\({type_label}) as\\(datetime) get: {datetime}", async (attributeName: string, typeLabel: string, value: Date) => {
    putThing(attributeName, await ((await tx().concepts().getAttributeType(typeLabel)) as DateTimeAttributeType).asRemote(tx()).get(value))
});

When("attribute {var} has boolean value: {bool}", async (attributeName: string, value: boolean) => {
    assert.strictEqual(value, (getThing(attributeName) as BooleanAttribute).getValue())
});

When("attribute {var} has long value: {int}", async (attributeName: string, value: number) => {
    assert.strictEqual(value, (getThing(attributeName) as LongAttribute).getValue())
});

When("attribute {var} has double value: {float}", async (attributeName: string, value: number) => {
    assert.strictEqual(value, (getThing(attributeName) as DoubleAttribute).getValue())
});

When("attribute {var} has string value: {string}", async (attributeName: string, value: string) => {
    assert.strictEqual(value, (getThing(attributeName) as StringAttribute).getValue())
});

When("attribute {var} has datetime value: {datetime}", async (attributeName: string, value: Date) => {
    assert.strictEqual(value, (getThing(attributeName) as DateTimeAttribute).getValue())
});
