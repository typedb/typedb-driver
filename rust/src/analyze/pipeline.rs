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
use typedb_protocol::analyze::res::query_structure::pipeline_structure::pipeline_stage::sort::sort_variable::SortDirection;
use crate::analyze::conjunction::{Conjunction, ConjunctionID, Reducer, Variable};
use crate::analyze::{vec_from_proto};


#[derive(Debug)]
pub struct PipelineStructure {
    conjunctions: Vec<Conjunction>,
    stages: Vec<PipelineStage>,
}

impl TryFrom<protocol::query_structure::PipelineStructure> for PipelineStructure {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: protocol::query_structure::PipelineStructure) -> Result<Self, Self::Error> {
        let conjunctions = vec_from_proto(value.conjunctions)?;
        let stages = vec_from_proto(value.stages)?;
        Ok(Self { conjunctions, stages })
    }
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
    Reduce { reducers: Vec<ReduceAssignment>, groupby: Vec<Variable> },
}

#[derive(Debug)]
pub struct ReduceAssignment {
    assigned: Variable,
    reducer: Reducer,
}

impl TryFrom<pipeline_proto::pipeline_stage::reduce::ReduceAssign> for ReduceAssignment {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: pipeline_proto::pipeline_stage::reduce::ReduceAssign) -> Result<Self, Self::Error> {
        Ok(Self {
            assigned: value.assigned.ok_or("expected assigned")?.try_into()?,
            reducer: value.reducer.ok_or("expected reducer")?.try_into()?,
        })
    }
}

impl TryFrom<pipeline_proto::PipelineStage> for PipelineStage {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: pipeline_proto::PipelineStage) -> Result<Self, Self::Error> {
        use pipeline_proto::{pipeline_stage as stage_proto, pipeline_stage::Stage as StageProto};
        let stage = match value.stage.ok_or("expected stage")? {
            StageProto::Match(stage_proto::Match { block }) => {
                Self::Match { block: ConjunctionID(block as usize) }
            },
            StageProto::Insert(stage_proto::Insert { block }) => {
                Self::Insert { block: ConjunctionID(block as usize) }
            }
            StageProto::Put(stage_proto::Put { block }) => {
                Self::Put { block: ConjunctionID(block as usize) }
            }
            StageProto::Update(stage_proto::Update { block }) => {
                Self::Update { block: ConjunctionID(block as usize) }
            }
            StageProto::Delete(stage_proto::Delete { block, deleted_variables }) => {
                let deleted_variables = vec_from_proto(deleted_variables)?;
                Self::Delete { block: ConjunctionID(block as usize), deleted_variables }
            }
            StageProto::Select(stage_proto::Select { variables }) => {
                Self::Select { variables: vec_from_proto(variables)? }
            }
            StageProto::Sort(stage_proto::Sort { sort_variables }) => {
                Self::Sort { variables: vec_from_proto(sort_variables)? }
            }
            StageProto::Require(stage_proto::Require { variables }) => {
                Self::Require { variables: vec_from_proto(variables)? }
            }
            StageProto::Offset(stage_proto::Offset { offset }) => {
                Self::Offset { offset }
            }
            StageProto::Limit(stage_proto::Limit { limit }) => {
                Self::Limit { limit }
            }
            StageProto::Distinct(stage_proto::Distinct {}) => {
                Self::Distinct
            }
            StageProto::Reduce(stage_proto::Reduce { reducers, groupby }) => {
                let reducers = vec_from_proto(reducers)?;
                let groupby = vec_from_proto(groupby)?;
                Self::Reduce { reducers, groupby }
            }
        };
        Ok(stage)
    }
}

#[derive(Debug)]
pub struct SortVariable {
    pub variable: Variable,
    pub order: SortOrder,
}

impl TryFrom<pipeline_proto::pipeline_stage::sort::SortVariable> for SortVariable {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: pipeline_proto::pipeline_stage::sort::SortVariable) -> Result<Self, Self::Error> {
        use pipeline_proto::pipeline_stage::sort::sort_variable::SortDirection;
        Ok(Self {
            variable: value.variable.ok_or("expected variable")?.try_into()?,
            order: match SortDirection::try_from(value.direction).map_err(|_| "expected sort direction")? {
                SortDirection::Asc => SortOrder::Ascending,
                SortDirection::Desc => SortOrder::Descending,
            }
        })
    }
}

#[derive(Debug)]
pub enum SortOrder {
    Ascending,
    Descending,
}