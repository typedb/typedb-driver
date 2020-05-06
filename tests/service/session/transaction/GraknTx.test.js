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

describe("Transaction methods", () => {

  it("match get - Answer of ConceptMap", async () => {
    let localSession = await env.sessionForKeyspace('shortestpathks');
  
    localTx = await localSession.transaction().write();
  
    await localTx.query(`
      define
          object sub entity, plays owned, plays owner;
          ownership sub relation, relates owned, relates owner;
          transitive-ownership sub rule, when {
              (owned: $x, owner: $y) isa ownership;
              (owned: $y, owner: $z) isa ownership;
          }, then {
              (owned: $x, owner: $z) isa ownership;
          };
    `);

    await localTx.query(`
      insert
        $a isa object; $b isa object; $c isa object; $d isa object; $e isa object;
        (owned: $a, owner: $b) isa ownership;
        (owned: $b, owner: $c) isa ownership;
        (owned: $c, owner: $d) isa ownership;
        (owned: $d, owner: $e) isa ownership;
    `);

    await localTx.commit()

    localTx = await localSession.transaction().write();
    const answers = await (await localTx.query("match (owner: $x, owned: $y) isa ownership; get;")).collect();

    let hasExplanationCounter = 0;
    let noExplanationCounter = 0;

    for (conceptMap of answers) {
      const pattern = conceptMap.queryPattern();
      if (conceptMap.hasExplanation()) {
        const explanation = await conceptMap.explanation();
        expect(explanation).not.toBeNull();
        expect(pattern.length).toBeGreaterThan(0);
        Array.from(conceptMap.map().entries()).forEach(([variable]) => {
          expect(pattern.includes(`$${variable}`)).toBeTruthy();
        });
        hasExplanationCounter += 1;
      } else {
        noExplanationCounter += 1;
      }
    }

    expect(noExplanationCounter).toEqual(4);
    expect(hasExplanationCounter).toEqual(6);

    await localTx.close();
    await localSession.close();
    await env.graknClient.keyspaces().delete('shortestpathks');
  });

  it("shortest path - Answer of conceptList", async ()=>{
    let localSession = await env.sessionForKeyspace('shortestpathks');
    let localTx = await localSession.transaction().write();
    const parentshipMap = await env.buildParentship(localTx);
    await localSession.close();
    localSession = await env.sessionForKeyspace('shortestpathks');
    localTx = await localSession.transaction().read();
    const result = await localTx.query(`compute path from ${parentshipMap.parent}, to ${parentshipMap.child};`);
    const answer = await(result.next());
    expect(answer.list().length).toBe(3);
    expect(answer.list().includes(parentshipMap.child)).toBeTruthy();
    expect(answer.list().includes(parentshipMap.parent)).toBeTruthy();
    expect(answer.list().includes(parentshipMap.rel)).toBeTruthy();
    await localTx.close();
    await localSession.close();
    await env.graknClient.keyspaces().delete('shortestpathks');
  });

  it("cluster connected components - Answer of conceptSet", async ()=>{
    let localSession = await env.sessionForKeyspace('clusterkeyspace');
    let localTx = await localSession.transaction().write();
    const parentshipMap = await env.buildParentship(localTx);
    await localSession.close();
    localSession = await env.sessionForKeyspace('clusterkeyspace');
    localTx = await localSession.transaction().read();
    const result = await localTx.query("compute cluster in [person, parentship], using connected-component;");
    const answer = await(result.next());
    expect(answer.set().size).toBe(3);
    expect(answer.set().has(parentshipMap.child)).toBeTruthy();
    expect(answer.set().has(parentshipMap.parent)).toBeTruthy();
    expect(answer.set().has(parentshipMap.rel)).toBeTruthy();
    await localTx.close();
    await localSession.close();
    await env.graknClient.keyspaces().delete('clusterkeyspace');
  });

  it("compute centrality - Answer of conceptSetMeasure", async ()=>{
    const localSession = await env.sessionForKeyspace('computecentralityks');
    let localTx = await localSession.transaction().write();
    const parentshipMap = await env.buildParentship(localTx);
    localTx = await localSession.transaction().read();
    const result = await localTx.query("compute centrality in [person, parentship], using degree;");
    const answer = await(result.next());
    expect(answer.measurement()).toBe(1);
    expect(answer.set().has(parentshipMap.child)).toBeTruthy();
    expect(answer.set().has(parentshipMap.parent)).toBeTruthy();
    await localTx.close();
    await localSession.close();
    await env.graknClient.keyspaces().delete('computecentralityks');
  });

  it("transaction closes on error", async () => {
    const localSession = await env.sessionForKeyspace('computecentralityks');
    let localTx = await localSession.transaction().write();
    await localTx.query("invalid query").catch(() => {});
    expect(localTx.isOpen()).toBe(false);
    await localSession.close();
    await env.graknClient.keyspaces().delete('computecentralityks');
  });

  it("transaction isOpen", async () => {
    const localSession = await env.sessionForKeyspace('computecentralityks');
    const localTx = await localSession.transaction().write();
    expect(localTx.isOpen()).toEqual(true);
    await localTx.close();
    expect(localTx.isOpen()).toEqual(false);
    await localSession.close();
    await env.graknClient.keyspaces().delete('computecentralityks');
  });

  it("closes transaction twice", async () => {
    const localSession = await env.sessionForKeyspace('computecentralityks');
    const localTx = await localSession.transaction().read();
    await localTx.close();
    await localTx.close();
    expect(localTx.isOpen()).toEqual(false);
    await localSession.close();
    await env.graknClient.keyspaces().delete('computecentralityks');
  });
});
