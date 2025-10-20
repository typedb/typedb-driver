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

use crate::analyze::conjunction::{Conjunction, ConjunctionID, Variable};

/// A representation of a query pipeline.
#[derive(Debug, Clone)]
pub struct Pipeline {
    pub conjunctions: Vec<Conjunction>,
    pub stages: Vec<PipelineStage>,
    pub variable_info: HashMap<Variable, VariableInfo>,
    pub outputs: Vec<Variable>,
}

impl Pipeline {
    pub fn variable_name(&self, variable: &Variable) -> Option<&str> {
        self.variable_info.get(variable).map(|v| v.name.as_str())
    }
}

/// Holds information about variables in a <code>Pipeline</code>.
#[derive(Debug, Clone)]
pub struct VariableInfo {
    pub name: String,
}

/// Representation of a stage in a <code>Pipeline</code>.
#[derive(Debug, Clone)]
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
    Reduce { reducers: Vec<ReduceAssignment>, groupby: Vec<Variable> },
}

/// Representation of an assignment from a reduction in a <code>PipelineStage::Reduce</code>,
/// such as <code>reduce $c = sum($x);</code>
#[derive(Debug, Clone)]
pub struct ReduceAssignment {
    pub assigned: Variable,
    pub reducer: Reducer,
}

/// Representation of a reducer used either in a <code>PipelineStage::Reduce</code>
/// or in a function's <code>ReturnOperation</code>.
#[derive(Debug, Clone)]
pub struct Reducer {
    pub arguments: Vec<Variable>,
    pub reducer: String,
}

/// The variable being sorted on and the ordering of the sort, as used in a <code>PipelineStage::Sort</code>,
///  e.g. <code>sort $v desc</code>
#[derive(Debug, Clone)]
pub struct SortVariable {
    pub variable: Variable,
    pub order: SortOrder,
}

/// The order of a variable being sorted on in a <code>PipelineStage::Sort</code>
#[repr(C)]
#[derive(Debug, Clone)]
pub enum SortOrder {
    Ascending,
    Descending,
}
