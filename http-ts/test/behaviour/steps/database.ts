import { DataTable, Given, Then, When } from "@cucumber/cucumber";
import { driver } from "./context";
import { assertNotError, checkMayError, EXPECT_ERROR_CONTAINING, MayError } from "./params";
import assert from "assert";
import { Database, isApiErrorResponse } from "../../../dist";

async function createDatabase(database: string, mayError: MayError) {
    await driver.createDatabase(database).then(checkMayError(mayError));
}
Given('(in background, )connection create database: {word}{may_error}', createDatabase);
Given(`(in background, )connection create database: {word}${EXPECT_ERROR_CONTAINING}`, createDatabase);

async function createDatabaseWithEmptyName(mayError: MayError) {
    await createDatabase(" ", mayError);
}
Then('connection create database with empty name{may_error}', createDatabaseWithEmptyName);
Then(`connection create database with empty name${EXPECT_ERROR_CONTAINING}`, createDatabaseWithEmptyName);

async function deleteDatabase(database: string, mayError: MayError) {
    await driver.deleteDatabase(database)
        .then((res) => {
            if (isApiErrorResponse(res) && res.err.code === "DBD1") {
                return {
                    ...res,
                    err: {
                        code: res.err.code,
                        message: `Database '${database}' not found`
                    }
                };
            } else return res
        })
        .then(checkMayError(mayError));
}
Given('(in background, )connection delete database: {word}{may_error}', deleteDatabase);
Given(`(in background, )connection delete database: {word}${EXPECT_ERROR_CONTAINING}`, deleteDatabase);

Given('connection has {int} database(s)', async (count: number) => {
    const databases = await driver.getDatabases().then(assertNotError);
    assert.equal(databases.ok.databases.length, count);
});

Given('connection has database: {word}', async (database: string) => {
    await driver.getDatabase(database).then(assertNotError);
});
Given('connection has databases:', async (databases: DataTable) => {
    const expectedDBNames = databases.raw().map(x => x[0]);
    expectedDBNames.sort();
    const dbNames = (await driver.getDatabases().then(assertNotError)).ok.databases.map((x: Database) => x.name);
    dbNames.sort();
    assert.equal(expectedDBNames.length, dbNames.length);
    for (let i = 0; i < dbNames.length; i++) {
        assert.equal(dbNames[i], expectedDBNames[i]);
    }
});

Given('connection does not have database: {word}', async (database: string) => {
    const res = await driver.getDatabases().then(assertNotError);
    const dbNames = res.ok.databases.map(x => x.name);
    assert.ok(!dbNames.includes(database), `${database} unexpectedly found in ${dbNames.toString()}`)
});
Given('connection does not have databases:', async (databases: DataTable) => {
    const expectedDBNames = databases.raw().map(x => x[0]);
    const dbNames = (await driver.getDatabases().then(assertNotError)).ok.databases.map((x: Database) => x.name);
    for (const db of expectedDBNames) {
        assert.ok(!dbNames.includes(db), `${db} unexpectedly found in ${dbNames}`)
    }
});

async function createTemporaryDatabaseWithSchema(schema: string) {
    if (!schema) return "";
    const randomSuffix = (Math.random() + 1).toString(36).substring(7);
    const name = `temp-${randomSuffix}`;

    await driver.createDatabase(name).then(assertNotError);
    await driver.oneShotQuery(schema, true, name, "schema").then(assertNotError);
    return (await driver.getDatabaseSchema(name).then(assertNotError)).ok;
}
Given('connection get database\\({word}\\) has schema:', async (db: string, schema: string) => {
    const expectedSchema = await createTemporaryDatabaseWithSchema(schema);
    const res = await driver.getDatabaseSchema(db).then(assertNotError);
    assert.equal(res.ok, expectedSchema);
});
Given('connection get database\\({word}\\) has type schema:', async (db: string, schema: string) => {
    const expectedSchema = await createTemporaryDatabaseWithSchema(schema);
    const res = await driver.getDatabaseTypeSchema(db).then(assertNotError);
    assert.equal(res.ok, expectedSchema);
});
