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

use futures::StreamExt;
use crate::analyze::pipeline::PipelineStructure;
use typedb_protocol::analyze::res as protocol;
use function::FunctionStructure;

pub mod pipeline;
pub mod conjunction;
pub mod function;

pub type TryFromError = &'static str;

#[derive(Debug)]
pub struct AnalyzeResponse {
    structure: QueryStructure,
    annotations: QueryAnnotations,
}

#[derive(Debug)]
pub struct QueryStructure {
    query: PipelineStructure,
    preamble: Vec<FunctionStructure>,
}

impl TryFrom<protocol::QueryStructure> for QueryStructure {
    type Error = TryFromError;

	fn try_from(value: protocol::QueryStructure) -> Result<Self, Self::Error> {
        let query = value.query.ok_or("Expected query structure")?.try_into()?;
        let preamble = vec_from_proto(value.preamble)?;
        Ok(Self { query, preamble })
    }
}

#[derive(Debug)]
pub struct QueryAnnotations { }

// helpers
fn vec_from_proto<Src, To>(protocol_vec: Vec<Src>) -> Result<Vec<To>, TryFromError>
where To: TryFrom<Src, Error = TryFromError> {
    protocol_vec.into_iter().map(|x| To::try_from(x)).collect()
}
