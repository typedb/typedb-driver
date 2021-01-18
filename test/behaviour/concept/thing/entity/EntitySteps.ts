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

When("{var} = entity\\({type_label}) create new instance", async (thingName: string, typeLabel: string) => {
    putThing(thingName, await (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).create());
});

When("{var} = entity\\({type_label}) create new instance; throws exception", async (thingName: string, typeLabel: string) => {
    await assertThrows(async () => await (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).create());
});

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as BooleanAttributeType).asRemote(tx()).put(value);
        const entity = await (await tx().concepts().getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        putThing(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as LongAttributeType).asRemote(tx()).put(value);
        const entity = await (await tx().concepts().getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        putThing(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as StringAttributeType).asRemote(tx()).put(value);
        const entity = await (await tx().concepts().getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        putThing(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as DateTimeAttributeType).asRemote(tx()).put(value);
        const entity = await (await tx().concepts().getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        putThing(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as BooleanAttributeType).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).getType()).equals(await tx().concepts().getEntityType(thingTypeLabel))) {
                putThing(thingName, owner);
                return
            }
        }
        putThing(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as LongAttributeType).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).getType()).equals(await tx().concepts().getEntityType(thingTypeLabel))) {
                putThing(thingName, owner);
                return
            }
        }
        putThing(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as StringAttributeType).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).getType()).equals(await tx().concepts().getEntityType(thingTypeLabel))) {
                putThing(thingName, owner);
                return
            }
        }
        putThing(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as DateTimeAttributeType).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).getType()).equals(await tx().concepts().getEntityType(thingTypeLabel))) {
                putThing(thingName, owner);
                return
            }
        }
        putThing(thingName, null);
    }
);

When("entity\\({type_label}) get instances contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).getInstances()) {
        if (instance.equals(getThing(variableName))) return;
    }
    assert.fail();
});

When("entity\\({type_label}) get instances is empty", async (typeLabel: string) => {
    assert.strictEqual((await (await tx().concepts().getEntityType(typeLabel)).asRemote(tx()).getInstances().collect()).length, 0);
});