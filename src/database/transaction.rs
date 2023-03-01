/*
 * Copyright (C) 2022 Vaticle
 *
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

use std::{fmt, marker::PhantomData, sync::Arc};

use super::query::QueryManager;
use crate::{
    common::{Result, TransactionType},
    connection::TransactionStream,
    Options,
};

pub struct Transaction<'a> {
    type_: TransactionType,
    options: Options,

    query: QueryManager,
    transaction_stream: Arc<TransactionStream>,

    _lifetime_guard: PhantomData<&'a ()>,
}

impl Transaction<'_> {
    pub(super) fn new(transaction_stream: TransactionStream) -> Result<Self> {
        let transaction_stream = Arc::new(transaction_stream);
        Ok(Transaction {
            type_: transaction_stream.type_(),
            options: transaction_stream.options().clone(),
            query: QueryManager::new(transaction_stream.clone()),
            transaction_stream,
            _lifetime_guard: PhantomData::default(),
        })
    }

    pub fn query(&self) -> &QueryManager {
        &self.query
    }

    pub async fn commit(self) -> Result {
        self.transaction_stream.commit().await
    }

    pub async fn rollback(&self) -> Result {
        self.transaction_stream.rollback().await
    }
}

impl fmt::Debug for Transaction<'_> {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("Transaction").field("type_", &self.type_).field("options", &self.options).finish()
    }
}
