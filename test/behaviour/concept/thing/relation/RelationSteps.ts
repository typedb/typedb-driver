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

import {When} from "@cucumber/cucumber";
import {get, put} from "../ThingSteps";
import {tx} from "../../../connection/ConnectionStepsBase";
import {assertThrows} from "../../../util/Util";
import {AttributeType} from "../../../../../dist/api/concept/type/AttributeType";
import {Relation} from "../../../../../dist/api/concept/thing/Relation";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {parseVar} from "../../../config/Parameters";
import assert = require("assert");

When("{var} = relation\\({type_label}) create new instance", async (var0: string, typeLabel: string) => {
    put(var0, await (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).create());
});

When("relation\\({type_label}) create new instance; throws exception", async (typeLabel: string) => {
    await assertThrows(async () => await (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).create());
});

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.Boolean).asRemote(tx()).put(value);
        const relation = await (await tx().concepts().getRelationType(thingTypeLabel)).asRemote(tx()).create();
        await relation.asRemote(tx()).setHas(key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.Long).asRemote(tx()).put(value);
        const relation = await (await tx().concepts().getRelationType(thingTypeLabel)).asRemote(tx()).create();
        await relation.asRemote(tx()).setHas(key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.String).asRemote(tx()).put(value);
        const relation = await (await tx().concepts().getRelationType(thingTypeLabel)).asRemote(tx()).create();
        await relation.asRemote(tx()).setHas(key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.DateTime).asRemote(tx()).put(value);
        const relation = await (await tx().concepts().getRelationType(thingTypeLabel)).asRemote(tx()).create();
        await relation.asRemote(tx()).setHas(key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.Boolean).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.getType().getLabel().scopedName() === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.Long).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.getType().getLabel().scopedName() === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.String).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if (owner.getType().getLabel().scopedName() === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts().getAttributeType(keyTypeLabel)) as AttributeType.DateTime).asRemote(tx()).get(value);
        for await (const owner of key.asRemote(tx()).getOwners()) {
            if ((await owner.asRemote(tx()).getType()).equals(await tx().concepts().getRelationType(thingTypeLabel))) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("relation\\({type_label}) get instances contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).getInstances()) {
        if (instance.equals(get(variableName))) return;
    }
    assert.fail();
});

When("relation\\({type_label}) get instances do not contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).getInstances()) {
        if (instance.equals(get(variableName))) assert.fail();
    }
});

When("relation\\({type_label}) get instances is empty", async (typeLabel: string) => {
    assert.strictEqual((await (await tx().concepts().getRelationType(typeLabel)).asRemote(tx()).getInstances().collect()).length, 0);
});

When("relation {var} add player for role\\({type_label}): {var}", async (relationName:string, typeLabel: string, playerName: string) => {
    const relation = get(relationName) as Relation;
    const roleType = await relation.getType().asRemote(tx()).getRelates(typeLabel);
    const player = get(playerName);
    await relation.asRemote(tx()).addPlayer(roleType, player);
});

When("relation {var} add player for role\\({type_label}): {var}; throws exception", async (relationName:string, typeLabel: string, playerName: string) => {
    await assertThrows(async () => {
        const relation = get(relationName) as Relation;
        const roleType = await relation.getType().asRemote(tx()).getRelates(typeLabel);
        const player = get(playerName);
        await relation.asRemote(tx()).addPlayer(roleType, player);
    });
});

When("relation {var} remove player for role\\({type_label}): {var}", async (relationName:string, typeLabel: string, playerName: string) => {
    const relation = get(relationName) as Relation;
    const roleType = await relation.getType().asRemote(tx()).getRelates(typeLabel);
    const player = get(playerName);
    await relation.asRemote(tx()).removePlayer(roleType, player);
});

When("relation {var} get players contain:", async (var1: string, players: DataTable) => {
    const relation = get(var1) as Relation;
    const playersByRoleType = await relation.asRemote(tx()).getPlayersByRoleType();
    for (const [roleLabel, var2Raw] of players.raw()) {
        const var2 = parseVar(var2Raw);
        const roleType = Array.from(playersByRoleType.keys()).find(x => x.getLabel().name() == roleLabel);
        assert(roleType);
        assert(playersByRoleType.get(roleType).some(x => x.equals(get(var2))));
    }
});

When("relation {var} get players do not contain:", async (var1: string, players: DataTable) => {
    const relation = get(var1) as Relation;
    const playersByRoleType = await relation.asRemote(tx()).getPlayersByRoleType();
    for (const [roleLabel, var2Raw] of players.raw()) {
        const var2 = parseVar(var2Raw);
        const roleType = Array.from(playersByRoleType.keys()).find(x => x.getLabel().scopedName() == roleLabel);
        assert(!playersByRoleType.get(roleType)?.some(x => x.equals(get(var2))));
    }
});

When("relation {var} get players contain: {var}", async (var1: string, var2: string) => {
    assert(await (get(var1) as Relation).asRemote(tx()).getPlayers().some(x => x.equals(get(var2))));
});

When("relation {var} get players do not contain: {var}", async (var1: string, var2: string) => {
    assert(!(await (get(var1) as Relation).asRemote(tx()).getPlayers().some(x => x.equals(get(var2)))));
});

When("relation {var} get players for role\\({type_label}) contain: {var}", async (var1: string, roleLabel: string, var2: string) => {
    const roleType = await (get(var1) as Relation).getType().asRemote(tx()).getRelates(roleLabel);
    assert(await (get(var1) as Relation).asRemote(tx()).getPlayers([roleType]).some(x => x.equals(get(var2))));
});

When("relation {var} get players for role\\({type_label}) do not contain: {var}", async (var1: string, roleLabel: string, var2: string) => {
    const roleType = await (get(var1) as Relation).getType().asRemote(tx()).getRelates(roleLabel);
    assert(!(await (get(var1) as Relation).asRemote(tx()).getPlayers([roleType]).some(x => x.equals(get(var2)))));
});
