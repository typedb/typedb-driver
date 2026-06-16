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

import assert from "assert";
import { TypeDBHttpDriver, isOkResponse,ApiResponse, isApiErrorResponse, Value, TransactionType, ConceptRowAnswer, GivenRows } from "../../dist/index.cjs";

const ADDRESS = process.env.TYPEDB_HTTP_ADDRESS || "http://127.0.0.1:8000";
const USERNAME = "admin";
const PASSWORD = "password";
const DATABASE_NAME = "test-given";

const SCHEMA = `
define
  attribute id, value integer;
  attribute some-attribute, value string;
  entity some-entity, owns id, owns some-attribute;
  relation some-relation, relates self, plays some-relation:self, owns id;
`;

// --- Given concept roundtrip ---

interface GivenExample {
    description: string;
    insertQuery: string;  // must project $x
    givenQuery: string;   // declares "given $x: <type>;" and projects $id
    expectedId: number;
}

const givenExamples: GivenExample[] = [
    {
        description: "entity",
        insertQuery: `insert $x isa some-entity, has id 12345;`,
        givenQuery:  `given $x: some-entity; match $x has id $id;`,
        expectedId: 12345,
    },
    {
        description: "relation",
        insertQuery: `insert $x isa some-relation; $x links (self: $x), has id 67890;`,
        givenQuery:  `given $x: some-relation; match $x has id $id;`,
        expectedId: 67890,
    },
    {
        description: "attribute",
        insertQuery: `insert $x isa some-attribute "hello"; $_ isa some-entity, has $x, has id 11111;`,
        givenQuery:  `given $x: some-attribute; match $_ isa some-entity, has some-attribute $x, has id $id;`,
        expectedId: 11111,
    },
];

function checkResponseOk<OK_RES>(res: ApiResponse<OK_RES>, description: string) {
    if (isApiErrorResponse(res)) {
        throw new Error(`${description}: ${JSON.stringify(res)}`);
    }
}

async function runOneShot(driver: TypeDBHttpDriver, txType: TransactionType, query: string, commit: boolean, givenRows?: GivenRows): Promise<ConceptRowAnswer[]> {
    const resp = await driver.oneShotQuery(query, commit, DATABASE_NAME, txType, undefined, undefined, givenRows);
    if (isApiErrorResponse(resp)) throw new Error(`Query failed: ${resp.err.message}`);
    if (resp.ok.answerType !== "conceptRows") throw new Error(`Expected conceptRows for query`);
    if (resp.ok.answers.length === 0) throw new Error(`Query returned no rows`);
    return resp.ok.answers;
}

async function runGivenTests(driver: TypeDBHttpDriver, failures: string[]): Promise<number> {
    let passed = 0;
    for (const ex of givenExamples) {
        try {
            const insertAnswers = await runOneShot(driver, "write", ex.insertQuery, true);
            if (insertAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${insertAnswers.length}`);
            const xConcept = insertAnswers[0].data["x"];

            const readAnswers = await runOneShot(driver, "read", ex.givenQuery, false, [{ x: xConcept }]);
            if (readAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${readAnswers.length}`);
            const idConcept = readAnswers[0].data["id"] as any;
            assert.equal(idConcept.value, ex.expectedId, `id mismatch: expected ${ex.expectedId}, got ${idConcept.value}`);
            console.log(`  PASS [given]: ${ex.description}`);
            passed++;
        } catch (e) {
            const msg = `FAIL [given]: ${ex.description}: ${e}`;
            console.error(`  ${msg}`);
            failures.push(msg);
        }
    }
    return passed;
}

async function runGivenRawIIDTests(driver: TypeDBHttpDriver, failures: string[]): Promise<number> {
    let passed = 0;
    for (const ex of givenExamples) {
        try {
            const insertAnswers = await runOneShot(driver, "write", ex.insertQuery, true);
            if (insertAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${insertAnswers.length}`);
            const xConcept = insertAnswers[0].data["x"];

            const readAnswers = await runOneShot(driver, "read", ex.givenQuery, false, [{ x: xConcept.iid }]);
            if (readAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${readAnswers.length}`);
            const idConcept = readAnswers[0].data["id"] as any;
            assert.equal(idConcept.value, ex.expectedId, `id mismatch: expected ${ex.expectedId}, got ${idConcept.value}`);
            console.log(`  PASS [givenRawIID]: ${ex.description}`);
            passed++;
        } catch (e) {
            const msg = `FAIL [givenRawIID]: ${ex.description}: ${e}`;
            console.error(`  ${msg}`);
            failures.push(msg);
        }
    }
    return passed;
}

// --- Value roundtrip ---

function mkValue(valueType: string, value: any): Value {
    return { kind: "value", valueType: valueType as any, value };
}

const valueExamples: [string, Value, string][] = [
    ["boolean", mkValue("boolean", true),  "true"],
    ["boolean", mkValue("boolean", false), "false"],
    ["integer", mkValue("integer", 25),    "25"],
    ["double",  mkValue("double",  54.321), "54.321"],
    ["decimal", mkValue("decimal", "1234567890.0001234567890"),  "1234567890.0001234567890dec"],
    ["decimal", mkValue("decimal", "-1234567890.0001234567890"), "-1234567890.0001234567890dec"],
    ["string",  mkValue("string",  "John"), '"John"'],
    ["date",        mkValue("date",        "2024-09-20"),                              "2024-09-20"],
    ["datetime",    mkValue("datetime",    "1999-02-26T12:15:05.000000000"),           "1999-02-26T12:15:05"],
    ["datetime-tz", mkValue("datetime-tz", "2024-09-20T16:40:05.000000000 Europe/Belfast"), "2024-09-20T16:40:05 Europe/Belfast"],
    ["datetime-tz", mkValue("datetime-tz", "2024-09-20T16:40:05.028129323+05:45"),         "2024-09-20T16:40:05.028129323+0545"],
    ["duration", mkValue("duration", "P1Y10M7DT15H44M5.00394892S"), "P1Y10M7DT15H44M5.00394892S"],
];

async function runValueTests(driver: TypeDBHttpDriver, failures: string[]): Promise<number> {
    let passed = 0;
    for (const [valueType, value, literal] of valueExamples) {
        try {
            const query = `given $native: ${valueType}; match let $parsed = ${literal};`;
            const readAnswers = await runOneShot(driver, "read", query, false, [{ native: value }]);
            if (readAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${readAnswers.length}`);

            const native = readAnswers[0].data["native"] as any;
            const parsed = readAnswers[0].data["parsed"] as any;

            assert.deepEqual(
                native.value, parsed.value,
                `native !== parsed: ${JSON.stringify(native.value)} !== ${JSON.stringify(parsed.value)}`
            );

            console.log(`  PASS [values]: ${valueType} (${literal})`);
            passed++;
        } catch (e) {
            const msg = `FAIL [values]: ${valueType} (${literal}): ${e}`;
            console.error(`  ${msg}`);
            failures.push(msg);
        }
    }
    return passed;
}

const rawValueExamples: [string, any, string][] = [
    ["boolean", true,  "true"],
    ["boolean", false, "false"],
    ["integer", 25,    "25"],
    ["double",  54.321, "54.321"],
    ["decimal", "1234567890.0001234567890",  "1234567890.0001234567890dec"],
    ["decimal", "-1234567890.0001234567890", "-1234567890.0001234567890dec"],
    ["string",  "John", '"John"'],
    ["date",        "2024-09-20",                              "2024-09-20"],
    ["datetime",    "1999-02-26T12:15:05.000000000",           "1999-02-26T12:15:05"],
    ["datetime-tz", "2024-09-20T16:40:05.000000000 Europe/Belfast", "2024-09-20T16:40:05 Europe/Belfast"],
    ["datetime-tz", "2024-09-20T16:40:05.028129323+05:45",         "2024-09-20T16:40:05.028129323+0545"],
    ["duration", "P1Y10M7DT15H44M5.00394892S", "P1Y10M7DT15H44M5.00394892S"],
];

async function runRawValueTests(driver: TypeDBHttpDriver, failures: string[]): Promise<number> {
    let passed = 0;
    for (const [valueType, value, literal] of rawValueExamples) {
        try {
            const query = `given $native: ${valueType}; match let $parsed = ${literal};`;
            const readAnswers = await runOneShot(driver, "read", query, false, [{ native: value }]);
            if (readAnswers.length !== 1) throw new Error(`Expected 1 row, got: ${readAnswers.length}`);

            const native = readAnswers[0].data["native"] as any;
            const parsed = readAnswers[0].data["parsed"] as any;

            assert.deepEqual(
                native.value, parsed.value,
                `native !== parsed: ${JSON.stringify(native.value)} !== ${JSON.stringify(parsed.value)}`
            );

            console.log(`  PASS [rawValues]: ${valueType} (${literal})`);
            passed++;
        } catch (e) {
            const msg = `FAIL [rawValues]: ${valueType} (${literal}): ${e}`;
            console.error(`  ${msg}`);
            failures.push(msg);
        }
    }
    return passed;
}

async function main(): Promise<void> {
    console.log("=== Integration Tests (HTTP-TS) ===\n");

    const driver = new TypeDBHttpDriver({ username: USERNAME, password: PASSWORD, addresses: [ADDRESS] });

    const dbsResp = await driver.getDatabases();
    checkResponseOk(dbsResp, "Failed to list databases");
    if (dbsResp.ok.databases.some(db => db.name === DATABASE_NAME)) {
        checkResponseOk(await driver.deleteDatabase(DATABASE_NAME), "Failed to delete existing database");
    }
    checkResponseOk(await driver.createDatabase(DATABASE_NAME), "Failed to create database");
    checkResponseOk(await driver.oneShotQuery(SCHEMA, true, DATABASE_NAME, "schema"), "Schema definition failed");

    const failures: string[] = [];
    let passed = 0;

    passed += await runGivenTests(driver, failures);
    passed += await runGivenRawIIDTests(driver, failures);
    passed += await runValueTests(driver, failures);
    passed += await runRawValueTests(driver, failures);

    await driver.deleteDatabase(DATABASE_NAME);

    console.log(`\n${passed} passed, ${failures.length} failed.`);
    if (failures.length > 0) {
        throw new Error(`${failures.length} test(s) failed:\n${failures.join("\n")}`);
    }
    console.log("All integration tests passed!");
}

main().then(
    () => process.exit(0),
    (err) => {
        console.error("\nTEST FAILED:", err);
        process.exit(1);
    }
);
