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
use typedb_protocol::query::{QueryManager_Match_ResPart, QueryManager_Res, QueryManager_ResPart};
use typedb_protocol::query::QueryManager_ResPart_oneof_res::match_res_part;
use typedb_protocol::transaction::{Transaction_Req, Transaction_Res, Transaction_Res_oneof_res, Transaction_ResPart, Transaction_ResPart_oneof_res};
use typedb_protocol::transaction::Transaction_Res_oneof_res::query_manager_res;
use typedb_protocol::transaction::Transaction_ResPart_oneof_res::query_manager_res_part;

use crate::common::error::ERRORS;
use crate::common::Result;
use crate::rpc::builder::query_manager::{match_req, query_manager_req};
use crate::rpc::transaction::bidi_stream::BidiStream;

pub struct QueryManager {
    bidi_stream: Arc<Mutex<BidiStream>>
}

impl QueryManager {
    pub(crate) fn new(bidi_stream: Arc<Mutex<BidiStream>>) -> QueryManager {
        QueryManager { bidi_stream }
    }

    pub async fn match_query(&mut self, query: &str) -> Result<Vec<QueryManager_Match_ResPart>> {
        let res_parts: Vec<QueryManager_ResPart> = self.streaming_rpc(match_req(query)).await?;
        let mut match_res_parts: Vec<QueryManager_Match_ResPart> = vec![];
        for res_part in res_parts {
            let res = res_part.res
                .ok_or_else(|| ERRORS.client.missing_response_field.to_err(vec!["query_manager_res_part.res"]))?;
            match res {
                match_res_part(x) => { match_res_parts.push(x) }
                _ => { return Err(ERRORS.client.missing_response_field.to_err(vec!["query_manager_res_part.match_res_part"])) }
            }
        };
        Ok(match_res_parts)
    }

    async fn streaming_rpc(&mut self, req: Transaction_Req) -> Result<Vec<QueryManager_ResPart>> {
        let tx_res_parts = self.bidi_stream.lock().unwrap().streaming_rpc(req).await?;
        let mut query_mgr_res_parts: Vec<QueryManager_ResPart> = vec![];
        for tx_res_part in tx_res_parts {
            let res_part = tx_res_part.res
                .ok_or_else(|| ERRORS.client.missing_response_field.to_err(vec!["res_part.res"]))?;
            match res_part {
                query_manager_res_part(x) => { query_mgr_res_parts.push(x) },
                _ => { return Err(ERRORS.client.missing_response_field.to_err(vec!["res_part.query_manager_res_part"])) }
            }
        }
        Ok(query_mgr_res_parts)
    }
}
