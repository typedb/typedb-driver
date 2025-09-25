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

use std::collections::HashMap;
use std::fmt::format;
use futures::StreamExt;
use itertools::Itertools;
use crate::analyze::pipeline::PipelineStructure;
use typedb_protocol::analyze::res as protocol;
use function::FunctionStructure;

pub mod pipeline;
pub mod conjunction;
pub mod function;

#[derive(Debug)]
pub struct AnalyzeResponse {
    structure: QueryStructure,
    annotations: QueryAnnotations,
}

#[derive(Debug)]
pub struct QueryStructure {
    query: PipelineStructure,
    preamble: FunctionStructure,
}

impl From<protocol::QueryStructure> for QueryStructure {
    fn from(value: protocol::QueryStructure) -> Self {
        let query = value.query.expect("Expected query structure").into();
        let preamble = value.preamble.into_iter().map(|f| f.into()).collect();
        Self { query, preamble }
    }
}

#[derive(Debug)]
pub struct QueryAnnotations { }

// Helpers
pub(crate) struct Context {
    variable_names: HashMap<typedb_protocol::conjunction_structure::Variable, String>,
}

impl Context {
    const UNNAMED: &'static str = "_";
    pub(crate) fn var_name(&self, variable: &typedb_protocol::conjunction_structure::Variable) -> Option<String> {
        self.variable_names.get(variable).cloned()
    }
}

impl Context {
    fn build(pipeline_structure: &protocol::query_structure::PipelineStructure) -> Self {
        let variable_names = pipeline_structure.variable_info.iter().cloned().map(|(id,name)| {
            (typedb_protocol::conjunction_structure::Variable { id }, name)
        }).collect();
        Self { variable_names }
    }
}

pub trait FromPipelineProto<T> {
    fn from_proto(context: &Context, value: T) -> Self;
}

fn vec_from_proto<From, To: FromPipelineProto<From>>(context: &Context, protocol_vec: Vec<From>) -> Vec<To> {
    protocol_vec.into_iter().map(|x| To::from_proto(context, x)).collect()
}
