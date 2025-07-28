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

import { Then, When } from "@cucumber/cucumber";
import { TransactionType } from "../../../dist/index.mjs";
import { checkMayError, EXPECT_ERROR_CONTAINING, MayError } from "./params";
import {
    driver, openBackgroundTransaction,
    openTransaction,
    setTransactionSchemaLockAcquireTimeout,
    setTransactionTimeoutMillis,
    tx
} from "./context";

async function connectionOpenTransactionForDatabase(type: TransactionType, databaseName: string, mayError: MayError) {
    await openTransaction(databaseName, type).then(checkMayError(mayError));
}
Then('connection open {transaction_type} transaction for database: {word}{may_error}', { timeout: 15_000 }, connectionOpenTransactionForDatabase);
Then(`connection open {transaction_type} transaction for database: {word}${EXPECT_ERROR_CONTAINING}`, { timeout: 15_000 }, connectionOpenTransactionForDatabase);

async function transactionCommits(mayError: MayError) {
    await driver.commitTransaction(tx()).then(checkMayError(mayError));
}
Then('transaction commits{may_error}', transactionCommits);
Then(`transaction commits${EXPECT_ERROR_CONTAINING}`, transactionCommits);

async function transactionCloses(mayError: MayError) {
    await driver.closeTransaction(tx()).then(checkMayError(mayError));
}
Then('transaction closes{may_error}', transactionCloses);
Then(`transaction closes${EXPECT_ERROR_CONTAINING}`, transactionCloses);

async function transactionRollbacks(mayError: MayError) {
    await driver.rollbackTransaction(tx()).then(checkMayError(mayError));
}
Then('transaction rollbacks{may_error}', transactionRollbacks);
Then(`transaction rollbacks${EXPECT_ERROR_CONTAINING}`, transactionRollbacks);


Then('transaction is open: {boolean}', (_) => { /* no op: cannot check in http */ });
Then('transaction has type: {word}', (_) => { /* no op: cannot check in http */ });
Then('transactions( in parallel) have type:', () => { /* no op: cannot check in http */ });

When('set transaction option transaction_timeout_millis to: {int}', setTransactionTimeoutMillis);
When('set transaction option schema_lock_acquire_timeout_millis to: {int}', setTransactionSchemaLockAcquireTimeout);

async function inBackgroundConnectionOpenTransactionForDatabase(type: TransactionType, database: string, mayError: MayError) {
    await openBackgroundTransaction(type, database).then(checkMayError(mayError));
}
When('in background, connection open {transaction_type} transaction for database: {word}{may_error}', { timeout: 15_000 }, inBackgroundConnectionOpenTransactionForDatabase);
When(`in background, connection open {transaction_type} transaction for database: {word}${EXPECT_ERROR_CONTAINING}`, { timeout: 15_000 }, inBackgroundConnectionOpenTransactionForDatabase);
