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

import { Then } from "@cucumber/cucumber";
import { sessionsToTransactions } from "../../../connection/ConnectionSteps";
import { assertThrows, assertThrowsWithMessage } from "../../../util/Util";

Then('graql define', async function (queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await transaction.query().define(queryString);
        }
    }
});

Then('graql define; throws exception containing {string}', async function (exceptionString: string, queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await assertThrowsWithMessage(async () => await transaction.query().define(queryString), exceptionString);
        }
    }
});

Then('graql define; throws exception', async function (queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await assertThrows(async () => await transaction.query().define(queryString));
        }
    }
});

Then('graql insert', async function (queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await (await transaction.query().insert(queryString)).collect();
        }
    }
});

Then('graql insert; throws exception containing {string}', async function (exceptionString: string, queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await assertThrowsWithMessage(async () => await (await transaction.query().insert(queryString)).collect(), exceptionString);
        }
    }
});

Then('graql insert; throws exception', async function (queryString: string) {
    for (const transactionList of sessionsToTransactions.values()) {
        for (const transaction of transactionList) {
            await assertThrows(async () => await (await transaction.query().insert(queryString)).collect());
        }
    }
});

