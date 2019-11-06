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

let graknClient;
let session;

beforeAll(async () => {
    await env.startGraknServer();
    graknClient = env.graknClient;
    session = await graknClient.session("testcommit");
}, env.beforeAllTimeout);

afterAll(async () => {
    await session.close();
    await graknClient.keyspaces().delete("testcommit");
    await env.tearDown();
});

describe('Integration test', () => {

    it("Tx open in READ mode should throw when trying to define", async () => {
        const tx = await session.transaction().read();
        await expectAsync(tx.query("define person sub entity;")).toBeRejected();
        await tx.close();
    });

    it("If tx does not commit, different Tx won't see changes", async () => {
        const tx = await session.transaction().write();
        await tx.query("define catwoman sub entity;");
        tx.close()
        const newTx = await session.transaction().write();
        await expectAsync(newTx.query("match $x sub catwoman; get;")).toBeRejected(); // catwoman label does not exist in the graph
        newTx.close();
    });

    it("When tx commit, different tx will see changes", async () => {
        const tx = await session.transaction().write();
        await tx.query("define superman sub entity;");
        await tx.commit();
        const newTx = await session.transaction().write();
        const superman = await newTx.getSchemaConcept('superman');
        expect(superman.isSchemaConcept()).toBeTruthy();
        await newTx.close();
    });

    it("explanation and default of infer is true", async () => {
        const localSession = await graknClient.session("gene");
        let tx = await localSession.transaction().write();
        await tx.query(`
            insert 
                $p1 isa person; $c1 isa person; (parent: $p1, child: $c1) isa parentship;
                $p2 isa person; $c2 isa person; (parent: $p2, child: $c2) isa parentship;
                (sibling: $p1, sibling: $p2) isa siblings;
        `);
        await tx.commit();

        tx = await localSession.transaction().write();
        const iterator = await tx.query("match $x isa cousins; get;");
        const answer = await iterator.next();
        expect(answer.map().size).toBe(1);
        expect((await answer.explanation()).getAnswers().length).toEqual(3);
        expect(answer.queryPattern().includes("$x isa cousins;")).toBeTruthy();
        await tx.close();
        await localSession.close();
    });

    it("explanation with join explanation", async () => {
        const localSession = await graknClient.session("gene");
        const tx = await localSession.transaction().write();
        await tx.query(`
            insert 
                $x isa person; $y isa person; $z isa person; 
                (spouse: $x, spouse: $y) isa marriage; 
                (spouse: $y, spouse: $z) isa marriage;
        `)
        const iterator = await tx.query(`match ($x, $y) isa marriage; ($y, $z) isa marriage; $x != $z; get;`);
        const answers = await iterator.collect();
        expect(answers.length).toEqual(2);
        answers.forEach(a => expect(a.explanation).toBeDefined());
        await tx.close()
        await localSession.close();
    });

    it("no results with infer false", async () => {
        const localSession = await graknClient.session("gene");
        
        let tx = await localSession.transaction().write();
        await tx.query(`
            insert 
                $p1 isa person; $c1 isa person; (parent: $p1, child: $c1) isa parentship;
                $p2 isa person; $c2 isa person; (parent: $p2, child: $c2) isa parentship;
                (sibling: $p1, sibling: $p2) isa siblings;
        `);
        await tx.commit();

        tx = await localSession.transaction().write();
        const iterator = await tx.query("match $x isa cousins; get;", { infer: false });
        const answer = await iterator.next();
        expect(answer).toBeNull();
        await tx.close();
        await localSession.close();
    });

});




