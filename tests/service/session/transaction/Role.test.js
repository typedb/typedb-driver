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
    await env.startGraknServer();
    session = await env.session();
}, env.beforeAllTimeout);

afterAll(async () => {
    await env.tearDown();
});

beforeEach(async () => {
    tx = await session.transaction().write();
})

afterEach(() => {
    tx.close();
});

describe("Role methods", () => {

    it("relations", async () => {
        await tx.query('define parentship sub relation, relates parent, relates child;');
        const result = await tx.query('match $x type parent; get;');
        const concepts = (await result.collectConcepts());
        const role = concepts[0];
        expect(role.baseType).toBe('ROLE');
        const rels = await (await role.relations()).collect();
        expect(rels[0].baseType).toBe('RELATION_TYPE');
        expect(await rels[0].label()).toBe('parentship');
    });

    it("players", async () => {
        await tx.query('define parentship sub relation, relates parent, relates child;');
        await tx.query('define person sub entity, plays parent;');
        const result = await tx.query('match $x type parent; get;');
        const concepts = (await result.collectConcepts());
        const role = concepts[0];
        expect(role.baseType).toBe('ROLE');
        const types = await (await role.players()).collect();
        expect(types[0].baseType).toBe('ENTITY_TYPE');
        expect(await types[0].label()).toBe('person');
    });
});
