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

use crate::analyze::conjunction::{Conjunction, ConjunctionID, Reducer, Variable};

#[derive(Debug)]
pub struct PipelineStructure {
    pub conjunctions: Vec<Conjunction>,
    pub stages: Vec<PipelineStage>,
    pub variable_names: HashMap<Variable, String>,
    pub outputs: Vec<Variable>,
}

#[derive(Debug)]
pub enum PipelineStage {
    Match { block: ConjunctionID },
    Insert { block: ConjunctionID },
    Put { block: ConjunctionID },
    Update { block: ConjunctionID },
    Delete { block: ConjunctionID, deleted_variables: Vec<Variable> },
    Select { variables: Vec<Variable> },
    Sort { variables: Vec<SortVariable> },
    Require { variables: Vec<Variable> },
    Offset { offset: u64 },
    Limit { limit: u64 },
    Distinct,
    Reduce { reducers: Vec<ReduceAssign>, groupby: Vec<Variable> },
}

#[derive(Debug)]
pub struct ReduceAssign {
    pub assigned: Variable,
    pub reducer: Reducer,
}

#[derive(Debug)]
pub struct SortVariable {
    pub variable: Variable,
    pub order: SortOrder,
}

#[derive(Debug)]
pub enum SortOrder {
    Ascending,
    Descending,
}
