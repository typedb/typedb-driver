import {
    assertNotError,
    checkContainsOrDoesnt,
    checkMayError,
    ContainsOrDoesnt,
    EXPECT_ERROR_CONTAINING,
    MayError
} from "./params";
import { driver } from "./context";
import { DataTable, Then, When } from "@cucumber/cucumber";
import assert from "assert";
import { isApiErrorResponse, isOkResponse, User } from "../../../dist";

async function createUser(username: string, password: string, mayError: MayError) {
    await driver.createUser(username, password).then(checkMayError(mayError));
}
When("create user with username '{word}', password {string}{may_error}", createUser);
Then(`create user with username '{word}', password {string}${EXPECT_ERROR_CONTAINING}`, createUser);

Then('get current username: {word}', async (username: string) => {
    const res = await driver.getCurrentUser().then(assertNotError);
    assert.equal(res.ok.username, username);
});

Then('get all users:', async (names: DataTable) => {
    const res = await driver.getUsers().then(assertNotError);
    const expectedUsers = names.raw().map(x => x[0]);
    const actualUsers = res.ok.users.map((x: User) => x.username);
    assert.equal(expectedUsers.length, actualUsers.length);
    expectedUsers.forEach(expectedUser => {
        assert.ok(actualUsers.includes(expectedUser), `Did not find ${expectedUser} in ${actualUsers}`);
    })
});
async function getAllUsersMayError(mayError: MayError) {
    await driver.getUsers().then(checkMayError(mayError));
}
Then('get all users{may_error}', getAllUsersMayError);
Then(`get all users${EXPECT_ERROR_CONTAINING}`, getAllUsersMayError);

async function getUserUpdatePassword(username: string, password: string, mayError: MayError) {
    const getUserRes = await driver.getUser(username).then(checkMayError(mayError));
    if (isOkResponse(getUserRes)) await driver.updateUser(getUserRes.ok.username, password).then(checkMayError(mayError));
}
Then('get user\\({word}\\) update password to {string}{may_error}', getUserUpdatePassword);
Then(`get user\\({word}\\) update password to {string}${EXPECT_ERROR_CONTAINING}`, getUserUpdatePassword);

async function deleteUser(username: string, mayError: MayError) {
    await driver.deleteUser(username).then(checkMayError(mayError));
}
Then('delete user: {word}{may_error}', deleteUser);
Then(`delete user: {word}${EXPECT_ERROR_CONTAINING}`, deleteUser);

async function getUser(username: string, mayError: MayError) {
    await driver.getUser(username).then(checkMayError(mayError));
}
Then('get user: {word}{may_error}', getUser);
Then(`get user: {word}${EXPECT_ERROR_CONTAINING}`, getUser);

Then('get user\\({word}\\) get name: {word}', async (user: string, name: string) => {
    const res = await driver.getUser(user).then(assertNotError);
    assert.equal(res.ok.username, name);
});

Then('get all users {contains_or_doesnt}: {word}', async (containsOrDoesnt: ContainsOrDoesnt, user: string) => {
    const res = await driver.getUsers().then(assertNotError);
    checkContainsOrDoesnt(containsOrDoesnt, user, res.ok.users.map(x => x.username));
})
