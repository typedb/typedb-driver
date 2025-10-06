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

use crate::analyze::{
    annotations::QueryAnnotations,
    conjunction::{Reducer, Variable},
    pipeline::PipelineStructure,
};

pub mod annotations;
pub mod conjunction;
pub mod pipeline;

pub type TryFromError = &'static str;

#[derive(Debug)]
pub struct AnalyzedQuery {
    pub structure: QueryStructure,
    pub annotations: QueryAnnotations,
}

#[derive(Debug)]
pub struct QueryStructure {
    pub query: PipelineStructure,
    pub preamble: Vec<FunctionStructure>,
}

#[derive(Debug)]
pub struct FunctionStructure {
    pub arguments: Vec<Variable>,
    pub returns: ReturnOperation,
    pub body: PipelineStructure,
}

#[derive(Debug)]
pub enum ReturnOperation {
    Stream { variables: Vec<Variable> },
    Single { selector: String, variables: Vec<Variable> },
    Check {},
    Reduce { reducers: Vec<Reducer> },
}
