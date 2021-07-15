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

import { When } from "@cucumber/cucumber";
import { AttributeType } from "../../../../../dist";
import { tx } from "../../../connection/ConnectionStepsBase";


import { assertThrows } from "../../../util/Util";
import { get, put } from "../ThingSteps";
import assert = require("assert");

When("{var} = entity\\({type_label}) create new instance", async (var0: string, typeLabel: string) => {
    put(var0, await (await tx().concepts.getEntityType(typeLabel)).asRemote(tx()).create());
});

When("entity\\({type_label}) create new instance; throws exception", async (typeLabel: string) => {
    await assertThrows(async () => await (await tx().concepts.getEntityType(typeLabel)).asRemote(tx()).create());
});

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.Boolean).asRemote(tx()).put(value);
        const entity = await (await tx().concepts.getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        put(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.Long).asRemote(tx()).put(value);
        const entity = await (await tx().concepts.getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        put(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.String).asRemote(tx()).put(value);
        const entity = await (await tx().concepts.getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        put(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) create new instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.DateTime).asRemote(tx()).put(value);
        const entity = await (await tx().concepts.getEntityType(thingTypeLabel)).asRemote(tx()).create();
        await entity.asRemote(tx()).setHas(key)
        put(thingName, entity);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.Boolean).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return;
            }
        }
        put(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.Long).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return;
            }
        }
        put(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.String).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return;
            }
        }
        put(thingName, null);
    }
);

When("{var} = entity\\({type_label}) get instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel)) as AttributeType.DateTime).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).type).equals(await tx().concepts.getEntityType(thingTypeLabel))) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("entity\\({type_label}) get instances contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts.getEntityType(typeLabel)).asRemote(tx()).getInstances()) {
        if (instance.equals(get(variableName))) return;
    }
    assert.fail();
});

When("entity\\({type_label}) get instances is empty", async (typeLabel: string) => {
    assert.strictEqual((await (await tx().concepts.getEntityType(typeLabel)).asRemote(tx()).getInstances().collect()).length, 0);
});