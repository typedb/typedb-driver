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

use std::{collections::HashMap, fmt};

use crate::analyze::conjunction::{Conjunction, ConjunctionID, Variable};

/// A representation of a query pipeline.
#[derive(Debug, Clone)]
pub struct Pipeline {
    /// A flattened list of conjunctions in the pipeline.
    /// The actual logical structure can be reconstructed from the <code>Constraint</code>s within the conjunction.
    pub conjunctions: Vec<Conjunction>,
    /// The stages making up the pipeline
    pub stages: Vec<PipelineStage>,
    /// General information about a variable, such as its name.
    pub variable_info: HashMap<Variable, VariableInfo>,
    /// The variables available at the end of a pipeline
    pub outputs: Vec<Variable>,
}

impl Pipeline {
    /// Retrieves the name of a variable, if it has one.
    pub fn variable_name(&self, variable: &Variable) -> Option<&str> {
        self.variable_info.get(variable).map(|v| v.name.as_str())
    }
}

/// Holds information about variables in a <code>Pipeline</code>.
#[derive(Debug, Clone)]
pub struct VariableInfo {
    /// The name of the variable, if any.
    pub name: String,
}

/// Representation of a stage in a <code>Pipeline</code>.
#[derive(Debug, Clone)]
pub enum PipelineStage {
    /// match <block>
    /// e.g. `match $f isa friendship, links (friend: $x, friend: $y)`
    Match {
        /// The index into <code>Pipeline.conjunctions</code>
        block: ConjunctionID,
    },
    /// insert <block>
    /// e.g. `insert $f isa friendship, links (friend: $x, friend: $y)`
    Insert {
        /// The index into <code>Pipeline.conjunctions</code>
        block: ConjunctionID,
    },
    /// put <block>
    /// e.g. `put $f isa friendship, links (friend: $x, friend: $y)`
    Put {
        /// The index into <code>Pipeline.conjunctions</code>
        block: ConjunctionID,
    },
    /// update <block>
    /// e.g. `update $owner has name "John"`
    Update {
        /// The index into <code>Pipeline.conjunctions</code>
        block: ConjunctionID,
    },
    /// delete
    ///     <block>;
    ///     <deleted_variables>;
    /// e.g.
    /// ```typeql
    ///  delete
    ///     has $attribute of $owner; links ($player) of $relation;
    ///     $deleted-instance;
    /// ```
    Delete {
        /// The index into <code>Pipeline.conjunctions</code>
        block: ConjunctionID,
        /// The variables for which the unified concepts are to be deleted.
        deleted_variables: Vec<Variable>,
    },
    /// select <variables>
    /// e.g. `select $x, $y`
    Select { variables: Vec<Variable> },
    /// sort <variables-and-order>
    /// e.g. `sort $x asc, $y desc`
    Sort { variables: Vec<SortVariable> },
    /// require <variables>
    /// e.g. `require $x, $y`
    Require { variables: Vec<Variable> },
    /// offset <offset>
    /// e.g. `offset 3`
    Offset { offset: u64 },
    /// limit <limit>
    /// e.g. `limit 5`
    Limit { limit: u64 },
    /// distinct
    Distinct,
    /// reduce <reducers> groupby <groupby>
    /// e.g.
    /// ```typeql
    /// reduce $sum = sum($v), $count = count groupby $x, $y;
    /// ```
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
    /// The arguments to the reducer.
    pub arguments: Vec<Variable>,
    /// The reduce operation applied
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
