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

import { Given, After, Before, setDefaultTimeout } from "@cucumber/cucumber";
import { GraknClient } from "../../../dist/rpc/GraknClient";
import { Grakn } from "../../../dist/Grakn";
import Session = Grakn.Session;
import Transaction = Grakn.Transaction;

setDefaultTimeout(20 * 1000);
export const THREAD_POOL_SIZE = 32;

export let client: GraknClient;
export const sessions: Session[] = [];
export const transactions: Map<Session, Transaction[]> = new Map<Session, Transaction[]>();

Given("connection has been opened", () => {
    if (client) return;
    client = new GraknClient();
});

Before(clearAll)
After(clearAll)

async function clearAll() {
    if (client) {
        for (const session of sessions) {
            if (transactions.has(session)){
                for (const transaction of transactions.get(session)) {
                    try {
                        await transaction.close();
                    } catch {
                        //We're okay with this.
                    }
                }
            }
            try {
                if (session.isOpen()) await session.close()
            } catch (err){
                //We're also okay with this.
            }
        }
        const databases = await client.databases().all();
        for (const name of databases) {
            await client.databases().delete(name);
        }
    }
    sessions.length = 0;
    transactions.clear();
}
