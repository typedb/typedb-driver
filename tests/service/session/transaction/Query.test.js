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

describe("Query methods", () => {
  it("delete query response type should be Void", async () => {
    const localSession = await env.sessionForKeyspace('gene');
    let localTx = await localSession.transaction().write();
    await localTx.query('insert $x isa person, has identifier "a";');
    const iterator = await localTx.query('match $x isa person, has identifier "a"; delete $x;');
    const response = await iterator.next();
    expect(response.message()).toEqual("Delete successful.");
    await localTx.close();
    await localSession.close();
    await env.graknClient.keyspaces().delete('gene');
  });
});
