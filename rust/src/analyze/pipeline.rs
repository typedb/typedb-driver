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

use typedb_protocol::analyze::{
    res as protocol,
    res::query_structure::pipeline_structure as pipeline_proto
};
use crate::analyze::conjunction::{Conjunction, ConjunctionId, Reducer, Variable};
use crate::analyze::{Context, FromPipelineProto, vec_from_proto};


#[derive(Debug)]
pub struct PipelineStructure {
    conjunctions: Conjunction,
    stages: Vec<PipelineStage>,
}

impl FromPipelineProto<protocol::query_structure::PipelineStructure> for PipelineStructure {
    fn from_proto(context: &Context, value: protocol::query_structure::PipelineStructure) -> Self {
        let conjunctions = Conjunction::from_proto(context, value.conjunctions);
        let stages = vec_from_proto(context, value.stages);
        Self {
            conjunctions,
            stages
        }
    }
}

#[derive(Debug)]
pub enum PipelineStage {
    Match { block: ConjunctionId },
    Insert { block: ConjunctionId },
    Put { block: ConjunctionId },
    Update { block: ConjunctionId },
    Delete { block: ConjunctionId, deleted_variables: Vec<Variable> },
    Select { variables: Vec<Variable> },
    Sort { variables: Vec<SortVariable> },
    Require { variables: Vec<SortVariable> },
    Offset { offset: u64 },
    Limit { limit: u64 },
    Distinct,
    Reduce { reducers: Vec<Reducer> },
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