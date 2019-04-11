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

describe("Relation type methods", () => {

    test("create", async () => {
        const relationType = await tx.putRelationType("parenthood");
        const relation = await relationType.create();
        expect(relation.isRelation()).toBeTruthy();
    });

    test('Get/set/delete relates', async () => {
        const relationType = await tx.putRelationType("parenthood");
        const parentRole = await tx.putRole('parent');
        const childRole = await tx.putRole('child');
        const relates = await (await relationType.roles()).collect();
        expect(relates.length).toBe(0);
        await relationType.relates(parentRole);
        await relationType.relates(childRole);
        const populateRelates = await (await relationType.roles()).collect();
        expect(populateRelates.length).toBe(2);
        await relationType.unrelate(parentRole);
        const oneRole = await (await relationType.roles()).collect();
        expect(oneRole.length).toBe(1);
        expect(oneRole[0].baseType).toBe('ROLE');
    });
});