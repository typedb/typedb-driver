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

use std::fmt;

use typedb_driver::{Options, Transaction, TransactionType};

pub struct TransactionTracker {
    transactions: Vec<Transaction>,
}

impl fmt::Debug for TransactionTracker {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        todo!()
    }
}

impl Drop for TransactionTracker {
    fn drop(&mut self) {
        self.transactions.clear(); // ensure transactions are dropped before the sessions
    }
}

impl TransactionTracker {
    pub async fn open_transaction(
        &mut self,
        transaction_type: TransactionType,
        transaction_options: Options,
    ) -> typedb_driver::Result {
        unsafe {
            // // SAFETY: the transactions tracked by the SessionTracker instance borrow SessionTracker::session.
            // // As long as SessionTracker is alive, the transactions are valid.
            // self.transactions.push(std::mem::transmute(transaction));
            todo!()
        }
        Ok(())
    }

    pub fn transaction(&self) -> &Transaction {
        // SAFETY: the returned transaction borrows SessionTracker, which from the POV of lifetimes is equivalent to
        // borrowing SessionTracker::session.
        unsafe { std::mem::transmute(self.transactions.last().unwrap()) }
    }

    pub fn take_transaction(&mut self) -> Transaction {
        // SAFETY: the returned transaction borrows SessionTracker, which from the POV of lifetimes is equivalent to
        // borrowing SessionTracker::session.
        unsafe { std::mem::transmute(self.transactions.pop()) }
    }

    pub fn transactions(&self) -> &Vec<Transaction> {
        // SAFETY: the returned transactions borrow SessionTracker, which from the POV of lifetimes is equivalent to
        // borrowing SessionTracker::session.
        unsafe { std::mem::transmute(&self.transactions) }
    }

    pub fn transactions_mut(&mut self) -> &mut Vec<Transaction> {
        // SAFETY: the returned transactions borrow SessionTracker, which from the POV of lifetimes is equivalent to
        // borrowing SessionTracker::session.
        unsafe { std::mem::transmute(&mut self.transactions) }
    }
}
