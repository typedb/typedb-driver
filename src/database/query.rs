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

use std::sync::Arc;

use futures::Stream;

use crate::{
    answer::{ConceptMap, Numeric},
    common::Result,
    connection::TransactionStream,
    Options,
};

#[derive(Debug)]
pub struct QueryManager {
    transaction_stream: Arc<TransactionStream>,
}

impl QueryManager {
    pub(super) fn new(transaction_stream: Arc<TransactionStream>) -> QueryManager {
        QueryManager { transaction_stream }
    }

    pub async fn define(&self, query: &str) -> Result {
        self.define_with_options(query, Options::new()).await
    }

    pub async fn define_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.define(query.to_string(), options).await
    }

    pub async fn undefine(&self, query: &str) -> Result {
        self.undefine_with_options(query, Options::new()).await
    }

    pub async fn undefine_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.undefine(query.to_string(), options).await
    }

    pub async fn delete(&self, query: &str) -> Result {
        self.delete_with_options(query, Options::new()).await
    }

    pub async fn delete_with_options(&self, query: &str, options: Options) -> Result {
        self.transaction_stream.delete(query.to_string(), options).await
    }

    pub fn match_(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.match_with_options(query, Options::new())
    }

    pub fn match_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.match_(query.to_string(), options)
    }

    pub fn insert(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.insert_with_options(query, Options::new())
    }

    pub fn insert_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.insert(query.to_string(), options)
    }

    pub fn update(&self, query: &str) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.update_with_options(query, Options::new())
    }

    pub fn update_with_options(&self, query: &str, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        self.transaction_stream.update(query.to_string(), options)
    }

    pub async fn match_aggregate(&self, query: &str) -> Result<Numeric> {
        self.match_aggregate_with_options(query, Options::new()).await
    }

    pub async fn match_aggregate_with_options(&self, query: &str, options: Options) -> Result<Numeric> {
        self.transaction_stream.match_aggregate(query.to_string(), options).await
    }
}
