/*
 * Copyright (C) 2021 Vaticle
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

use std::sync::{Arc, Mutex};
use futures::{Stream, StreamExt};
use QueryManager_ResPart_oneof_res::{explain_res_part, insert_res_part, match_group_aggregate_res_part, match_group_res_part, match_res_part, update_res_part};
use Transaction_ResPart_oneof_res::query_manager_res_part;
use typedb_protocol::query::{QueryManager_Match_ResPart, QueryManager_Res_oneof_res, QueryManager_ResPart, QueryManager_ResPart_oneof_res};
use typedb_protocol::transaction::{Transaction_Req, Transaction_Res, Transaction_ResPart, Transaction_ResPart_oneof_res};
use typedb_protocol::transaction::Transaction_Res_oneof_res::query_manager_res;

use crate::common::error::MESSAGES;
use crate::common::Result;
use crate::concept::Concept;
use crate::rpc::builder::query_manager::{define_req, insert_req, match_req};
use crate::rpc::transaction::TransactionRpc;

#[derive(Clone, Debug)]
pub struct QueryManager {
    tx: Arc<Mutex<TransactionRpc>>
}

impl QueryManager {
    pub(crate) fn new(tx: Arc<Mutex<TransactionRpc>>) -> QueryManager {
        QueryManager { tx }
    }

    pub async fn define(&self, query: &str) -> Result {
        self.single_rpc(define_req(query)).await.map(|_| ())
    }

    pub fn insert(&self, query: &str) -> impl Stream<Item = Result<Vec<Concept>>> {
        self.streaming_rpc(insert_req(query)).map(|result: Result<QueryManager_ResPart_oneof_res>| {
            match result {
                Ok(res_part) => {
                    match res_part {
                        insert_res_part(x) => {
                            let mut concepts: Vec<Concept> = vec![];
                            for concept in x.answers.iter().flat_map(|answer| answer.map.values()) {
                                concepts.push(Concept::from_proto(concept.clone())?);
                            }
                            Ok(concepts)
                        }
                        _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["query_manager_res_part.insert_res_part"])) }
                    }
                }
                Err(err) => { Err(err) }
            }
        })
    }

    pub fn match_(&self, query: &str) -> impl Stream<Item = Result<Vec<Concept>>> {
        self.streaming_rpc(match_req(query)).map(|result: Result<QueryManager_ResPart_oneof_res>| {
            match result {
                Ok(res_part) => {
                    match res_part {
                        match_res_part(x) => {
                            let mut concepts: Vec<Concept> = vec![];
                            for concept in x.answers.iter().flat_map(|answer| answer.map.values()) {
                                concepts.push(Concept::from_proto(concept.clone())?);
                            }
                            Ok(concepts)
                        }
                        _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["query_manager_res_part.match_res_part"])) }
                    }
                }
                Err(err) => { Err(err) }
            }
        })
    }

    async fn single_rpc(&self, req: Transaction_Req) -> Result<QueryManager_Res_oneof_res> {
        match self.tx.lock().unwrap().single(req).await?.res {
            Some(query_manager_res(res)) => {
                res.res.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["res.query_manager_res"]))
            }
            _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["res.query_manager_res"])) }
        }
    }

    // TODO: Returns wrong answers - each tx_res_part contains multiple answers but we're flattening them!
    fn streaming_rpc(&self, req: Transaction_Req) -> impl Stream<Item = Result<QueryManager_ResPart_oneof_res>> {
        self.tx.lock().unwrap().stream(req).map(|result: Result<Transaction_ResPart>| {
            match result {
                Ok(tx_res_part) => {
                    match tx_res_part.res {
                        Some(query_manager_res_part(res_part)) => {
                            res_part.res.ok_or_else(|| MESSAGES.client.missing_response_field.to_err(vec!["res_part.query_manager_res_part"]))
                        }
                        _ => { Err(MESSAGES.client.missing_response_field.to_err(vec!["res_part.query_manager_res_part"])) }
                    }
                }
                Err(err) => { Err(err) }
            }
        })
    }
}
