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

describe("Attribute type methods", () => {

    it("create", async () => {
        const attributeType = await tx.putAttributeType("firstname", env.valueType().STRING);
        const attribute = await attributeType.create('Marco');
        expect(attribute.isAttribute()).toBeTruthy();
        expect(await attribute.value()).toBe('Marco');

        const boolAttributeType = await tx.putAttributeType("employed", env.valueType().BOOLEAN);
        const boolAttribute = await boolAttributeType.create(false);
        expect(await boolAttribute.value()).toBe(false);

        const doubleAttributeType = await tx.putAttributeType("length", env.valueType().DOUBLE);
        const doubleAttribute = await doubleAttributeType.create(11.58);
        expect(await doubleAttribute.value()).toBe(11.58);
    });

    it('valueType', async () => {
        const attributeType = await tx.putAttributeType("firstname", env.valueType().STRING);
        expect(await attributeType.valueType()).toBe('String');

        const boolAttributeType = await tx.putAttributeType("employed", env.valueType().BOOLEAN);
        expect(await boolAttributeType.valueType()).toBe('Boolean');

        const doubleAttributeType = await tx.putAttributeType("length", env.valueType().DOUBLE);
        expect(await doubleAttributeType.valueType()).toBe('Double');
    });

    it('attribute', async () => {
        const attributeType = await tx.putAttributeType("firstname", env.valueType().STRING);
        await attributeType.create('Marco');
        const attribute = await attributeType.attribute('Marco');
        expect(attribute.isAttribute()).toBeTruthy();
        const nullAttribute = await attributeType.attribute('Giangiovannino');
        expect(nullAttribute).toBeNull();
    });

    it('set/get regex', async () => {
        const attributeType = await tx.putAttributeType("id", env.valueType().STRING);
        const emptyRegex = await attributeType.regex();
        expect(emptyRegex.length).toBe(0);

        await attributeType.regex("(good|bad)-dog");
        const regex = await attributeType.regex();

        expect(regex).toBe("(good|bad)-dog");
    });
});