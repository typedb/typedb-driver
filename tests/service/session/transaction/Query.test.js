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
const reporters = require('jasmine-reporters')
const tapReporter = new reporters.TapReporter();
jasmine.getEnv().addReporter(tapReporter)

const env = require('../../../support/GraknTestEnvironment');
let session;
let tx;

beforeAll(async () => {
  await env.startGraknServer();
  session = await env.sessionForKeyspace('gene');
}, env.beforeAllTimeout);

afterAll(async () => {
  await session.close();
  await env.tearDown();
});

beforeEach(async () => {
  tx = await session.transaction().write();
})

afterEach(async () => {
  await tx.query('match $x isa thing; delete $x isa thing;');
  tx.close();
});

describe("Query methods", () => {
  it("delete query response type should be Void", async () => {
    await tx.query('insert $x isa person, has identifier "a";');
    const iterator = await tx.query('match $x isa person, has identifier "a"; delete $x isa person;');
    const response = await iterator.next();
    expect(response.message().includes("Deleted facts")).toEqual(true);
  });

  it("returned concepts should have local values", async () => {
    await tx.query('insert $x isa person, has identifier "a";');
    const iterator = await tx.query('match $y isa person, has identifier $x; get $x;');
    const response = await iterator.next();
    const id = response.get('x');
    expect(id.value()).toEqual('a');
    expect(id.valueType()).toEqual('String');
    expect(id.type().label()).toEqual('identifier');
    expect(id.isInferred()).toEqual(false);
  })

  it("works with a query greater than the batch size", async () => {
    inserts = ['insert'];
    for (let i = 1; i <= 128; ++i) {
      inserts.push(`\$p${i} isa person, has identifier "${i}";`);
    }
    await tx.query(inserts.join(' '));

    const iterator = await tx.query('match $x isa person; get $x;');
    const results = await iterator.collect();
    expect(results.length).toEqual(128);
  })
});
