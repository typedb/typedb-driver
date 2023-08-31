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

import {When} from "@cucumber/cucumber";
import DataTable from "@cucumber/cucumber/lib/models/data_table";
import {parseVar} from "../../../config/Parameters";
import {tx} from "../../../connection/ConnectionStepsBase";
import {assertThrows} from "../../../util/Util";
import {get, put} from "../ThingSteps";
import assert = require("assert");

When("{var} = relation\\({type_label}) create new instance", async (var0: string, typeLabel: string) => {
    put(var0, await (await tx().concepts.getRelationType(typeLabel)).create(tx()));
});

When("relation\\({type_label}) create new instance; throws exception", async (typeLabel: string) => {
    await assertThrows(async () => await (await tx().concepts.getRelationType(typeLabel)).create(tx()));
});

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).putBoolean(tx(), value);
        const relation = await (await tx().concepts.getRelationType(thingTypeLabel)).create(tx());
        await relation.setHas(tx(), key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).putLong(tx(), value);
        const relation = await (await tx().concepts.getRelationType(thingTypeLabel)).create(tx());
        await relation.setHas(tx(), key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).putString(tx(), value);
        const relation = await (await tx().concepts.getRelationType(thingTypeLabel)).create(tx());
        await relation.setHas(tx(), key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) create new instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).putDateTime(tx(), value);
        const relation = await (await tx().concepts.getRelationType(thingTypeLabel)).create(tx());
        await relation.setHas(tx(), key)
        put(thingName, relation);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {bool}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: boolean) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).getBoolean(tx(), value);
        for await (const owner of key.getOwners(tx())) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {int}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: number) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).getLong(tx(), value);
        for await (const owner of key.getOwners(tx())) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {word}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: string) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).getString(tx(), value);
        for await (const owner of key.getOwners(tx())) {
            if (owner.type.label.scopedName === thingTypeLabel) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("{var} = relation\\({type_label}) get instance with key\\({type_label}): {datetime}",
    async (thingName: string, thingTypeLabel: string, keyTypeLabel: string, value: Date) => {
        const key = await ((await tx().concepts.getAttributeType(keyTypeLabel))).getDateTime(tx(), value);
        for await (const owner of key.getOwners(tx())) {
            if (owner.type.equals(await tx().concepts.getRelationType(thingTypeLabel))) {
                put(thingName, owner);
                return
            }
        }
        put(thingName, null);
    }
);

When("relation\\({type_label}) get instances contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts.getRelationType(typeLabel)).getInstances(tx())) {
        if (instance.equals(get(variableName))) return;
    }
    assert.fail();
});

When("relation\\({type_label}) get instances do not contain: {var}", async (typeLabel: string, variableName: string) => {
    for await (const instance of (await tx().concepts.getRelationType(typeLabel)).getInstances(tx())) {
        if (instance.equals(get(variableName))) assert.fail();
    }
});

When("relation\\({type_label}) get instances is empty", async (typeLabel: string) => {
    assert.strictEqual((await (await tx().concepts.getRelationType(typeLabel)).getInstances(tx()).collect()).length, 0);
});

When("relation {var} add player for role\\({type_label}): {var}", async (relationName: string, typeLabel: string, playerName: string) => {
    const relation = get(relationName).asRelation();
    const roleType = await relation.type.getRelatesForRoleLabel(tx(), typeLabel);
    const player = get(playerName);
    await relation.addRolePlayer(tx(), roleType, player);
});

When("relation {var} add player for role\\({type_label}): {var}; throws exception", async (relationName: string, typeLabel: string, playerName: string) => {
    await assertThrows(async () => {
        const relation = get(relationName).asRelation();
        const roleType = await relation.type.getRelatesForRoleLabel(tx(), typeLabel);
        const player = get(playerName);
        await relation.addRolePlayer(tx(), roleType, player);
    });
});

When("relation {var} remove player for role\\({type_label}): {var}", async (relationName: string, typeLabel: string, playerName: string) => {
    const relation = get(relationName).asRelation();
    const roleType = await relation.type.getRelatesForRoleLabel(tx(), typeLabel);
    const player = get(playerName);
    await relation.removeRolePlayer(tx(), roleType, player);
});

When("relation {var} get players contain:", async (var1: string, players: DataTable) => {
    const relation = get(var1).asRelation();
    const rolePlayers = await relation.getRolePlayers(tx());
    for (const [roleLabel, var2Raw] of players.raw()) {
        const var2 = parseVar(var2Raw);
        const roleType = Array.from(rolePlayers.keys()).find(x => x.label.name == roleLabel);
        assert(roleType);
        assert(rolePlayers.get(roleType).some(x => x.equals(get(var2))));
    }
});

When("relation {var} get players do not contain:", async (var1: string, players: DataTable) => {
    const relation = get(var1).asRelation();
    const rolePlayers = await relation.getRolePlayers(tx());
    for (const [roleLabel, var2Raw] of players.raw()) {
        const var2 = parseVar(var2Raw);
        const roleType = Array.from(rolePlayers.keys()).find(x => x.label.scopedName == roleLabel);
        assert(!rolePlayers.get(roleType)?.some(x => x.equals(get(var2))));
    }
});

When("relation {var} get players contain: {var}", async (var1: string, var2: string) => {
    assert(await get(var1).asRelation().getPlayersByRoleType(tx()).some(x => x.equals(get(var2))));
});

When("relation {var} get players do not contain: {var}", async (var1: string, var2: string) => {
    assert(!(await get(var1).asRelation().getPlayersByRoleType(tx()).some(x => x.equals(get(var2)))));
});

When("relation {var} get players for role\\({type_label}) contain: {var}", async (var1: string, roleLabel: string, var2: string) => {
    const roleType = await get(var1).asRelation().type.getRelatesForRoleLabel(tx(), roleLabel);
    assert(await get(var1).asRelation().getPlayersByRoleType(tx(), [roleType]).some(x => x.equals(get(var2))));
});

When("relation {var} get players for role\\({type_label}) do not contain: {var}", async (var1: string, roleLabel: string, var2: string) => {
    const roleType = await get(var1).asRelation().type.getRelatesForRoleLabel(tx(), roleLabel);
    assert(!(await get(var1).asRelation().getPlayersByRoleType(tx(), [roleType]).some(x => x.equals(get(var2)))));
});
