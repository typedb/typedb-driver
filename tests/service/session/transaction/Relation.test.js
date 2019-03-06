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

const env = require('../../../support/GraknTestEnvironment');
let session;
let tx;

beforeAll(async () => {
    session = await env.session();
});

afterAll(async () => {
    await env.tearDown();
});

beforeEach(async () => {
    tx = await session.transaction().write();
})

afterEach(() => {
    tx.close();
});

describe("Relationsihp methods", () => {

    test("rolePlayersMap && rolePlayers with 2 roles with 1 player each", async () => {
        const relationType = await tx.putRelationType('parenthood');
        const relation = await relationType.create();
        const parentRole = await tx.putRole('parent');
        const childRole = await tx.putRole('child');
        const personType = await tx.putEntityType('person');
        const parent = await personType.create();
        const child = await personType.create();
        await relation.assign(parentRole, parent);
        await relation.assign(childRole, child);
        const map = await relation.rolePlayersMap();
        expect(map.size).toBe(2);
        Array.from(map.keys()).forEach(key => { expect(key.isRole()).toBeTruthy(); });
        Array.from(map.values()).forEach(set => { expect(Array.from(set).length).toBe(1); });
        const rolePlayers = await (await relation.rolePlayers()).collect();
        expect(rolePlayers.length).toBe(2);
    });

    test("rolePlayersMap && rolePlayers with 1 role with 2 players", async () => {
        const relationType = await tx.putRelationType('parenthood');
        const relation = await relationType.create();
        const parentRole = await tx.putRole('parent');
        const personType = await tx.putEntityType('person');
        const parent = await personType.create();
        const anotherParent = await personType.create();
        await relation.assign(parentRole, parent);
        await relation.assign(parentRole, anotherParent);
        const map = await relation.rolePlayersMap();
        expect(map.size).toBe(1);
        Array.from(map.keys()).forEach(key => { expect(key.isRole()).toBeTruthy(); });
        Array.from(map.values()).forEach(set => { expect(Array.from(set).length).toBe(2); });
        const rolePlayers = await (await relation.rolePlayers()).collect();
        expect(rolePlayers.length).toBe(2);
    });

    test("rolePlayersMap && rolePlayers with 2 roles with the same player", async () => {
        const relationType = await tx.putRelationType('parenthood');
        const relation = await relationType.create();
        const parentRole = await tx.putRole('parent');
        const childRole = await tx.putRole('child');
        const personType = await tx.putEntityType('person');
        const parentOfHimself = await personType.create();
        await relation.assign(parentRole, parentOfHimself);
        await relation.assign(childRole, parentOfHimself);
        const map = await relation.rolePlayersMap();
        expect(map.size).toBe(2);
        Array.from(map.keys()).forEach(key => { expect(key.isRole()).toBeTruthy(); });
        Array.from(map.values()).forEach(set => { expect(Array.from(set).length).toBe(1); });
        const rolePlayers = await (await relation.rolePlayers()).collect();
        expect(rolePlayers.length).toBe(1);
        expect(rolePlayers[0].isThing()).toBeTruthy();
    });

    test("assign && unassign && rolePlayers", async () => {
        const relationType = await tx.putRelationType('parenthood');
        const relation = await relationType.create();
        const parentRole = await tx.putRole('parent');
        const personType = await tx.putEntityType('person');
        const person = await personType.create();
        const emptyRolePlayers = await (await relation.rolePlayers()).collect();
        expect(emptyRolePlayers.length).toBe(0);
        await relation.assign(parentRole, person);
        const rolePlayers = await (await relation.rolePlayers()).collect();
        expect(rolePlayers.length).toBe(1);
        expect(rolePlayers[0].isThing()).toBeTruthy();
        expect(rolePlayers[0].id).toBe(person.id);
        await relation.unassign(parentRole, person);
        const rolePlayersRemoved = await (await relation.rolePlayers()).collect();
        expect(rolePlayersRemoved.length).toBe(0);

    });

    test("rolePlayers() filtered by Role", async () => {
        const relationType = await tx.putRelationType('parenthood');
        const relation = await relationType.create();
        const parentRole = await tx.putRole('parent');
        const childRole = await tx.putRole('child');
        const personType = await tx.putEntityType('person');
        const parent = await personType.create();
        const child = await personType.create();
        await relation.assign(parentRole, parent);
        await relation.assign(childRole, child);
        const rolePlayers = await (await relation.rolePlayers()).collect();
        expect(rolePlayers.length).toBe(2);
        const filteredRolePlayers = await (await relation.rolePlayers(childRole)).collect();
        expect(filteredRolePlayers.length).toBe(1);
        const player = filteredRolePlayers[0];
        expect(player.id).toBe(child.id);
        const doubleRolePlayers = await (await relation.rolePlayers(childRole, parentRole)).collect();
        expect(doubleRolePlayers.length).toBe(2);
    });
});