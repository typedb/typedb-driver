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

use typedb_protocol::{
    analyze::{
        res as analyze_proto,
        res::{
            query_structure,
            query_structure::{
                pipeline_structure, pipeline_structure::pipeline_stage::sort::sort_variable::SortDirection,
            },
        },
    },
    conjunction_structure,
    conjunction_structure::StructureConstraint,
    ConjunctionStructure,
};

use crate::{
    analyze::{
        conjunction::{
            Conjunction, ConjunctionID, Constraint, ConstraintExactness, ConstraintVertex, LabelVertex, Reducer,
            Variable,
        },
        pipeline::{PipelineStage, PipelineStructure, ReduceAssign, SortOrder, SortVariable},
        AnalyzedQuery, FunctionStructure, QueryAnnotations, QueryStructure, ReturnOperation,
    },
    common::Result,
    concept,
    concept::{type_, Kind, Value},
    connection::network::proto::TryFromProto,
    error::AnalyzeError,
};

pub(super) fn expect_try_into<Src, Dst: TryFromProto<Src>>(x: Option<Src>, field: &'static str) -> Result<Dst> {
    Dst::try_from_proto(x.ok_or_else(|| crate::Error::Analyze(AnalyzeError::MissingResponseField { field }))?)
}

fn vec_from_proto<Src, Dst>(protocol_vec: Vec<Src>) -> Result<Vec<Dst>>
where
    Dst: TryFromProto<Src>,
{
    protocol_vec.into_iter().map(|x| Dst::try_from_proto(x)).collect()
}

fn enum_from_proto<T: TryFrom<i32, Error = prost::UnknownEnumValue>>(as_i32: i32) -> Result<T> {
    T::try_from(as_i32)
        .map_err(|_| AnalyzeError::UnknownEnumValue { enum_name: std::any::type_name::<T>(), value: as_i32 }.into())
}

impl TryFromProto<typedb_protocol::analyze::Res> for AnalyzedQuery {
    fn try_from_proto(value: typedb_protocol::analyze::Res) -> Result<Self> {
        let typedb_protocol::analyze::Res { structure, annotations } = value;
        let structure = expect_try_into(structure, "analyze.Res.structure")?;
        let annotations = expect_try_into(annotations, "analyze.Res.annotations")?;
        Ok(Self { structure, annotations })
    }
}

// Structure
impl TryFromProto<analyze_proto::QueryStructure> for QueryStructure {
    fn try_from_proto(value: analyze_proto::QueryStructure) -> Result<Self> {
        let analyze_proto::QueryStructure { query, preamble } = value;
        let query = expect_try_into(query, "QueryStructure.query")?;
        let preamble = vec_from_proto(preamble)?;
        Ok(Self { query, preamble })
    }
}

impl TryFromProto<query_structure::FunctionStructure> for FunctionStructure {
    fn try_from_proto(value: query_structure::FunctionStructure) -> Result<Self> {
        let query_structure::FunctionStructure { body, returns, arguments } = value;
        let body = expect_try_into(body, "FunctionStructure.body")?;
        let returns = expect_try_into(returns, "FunctionStructure.returns")?;
        let arguments = vec_from_proto(arguments)?;
        Ok(Self { arguments, returns, body })
    }
}

impl TryFromProto<query_structure::function_structure::Returns> for ReturnOperation {
    fn try_from_proto(value: query_structure::function_structure::Returns) -> Result<Self> {
        use typedb_protocol::analyze::res::query_structure::function_structure::Returns;
        let returns = match value {
            Returns::Stream(stream) => Self::Stream { variables: vec_from_proto(stream.variables)? },
            Returns::Single(single) => {
                Self::Single { selector: single.selector, variables: vec_from_proto(single.variables)? }
            }
            Returns::Check(_check) => Self::Check {},
            Returns::Reduce(reduce) => Self::Reduce { reducers: vec_from_proto(reduce.reducers)? },
        };
        Ok(returns)
    }
}

impl TryFromProto<query_structure::PipelineStructure> for PipelineStructure {
    fn try_from_proto(value: query_structure::PipelineStructure) -> Result<Self> {
        let query_structure::PipelineStructure { conjunctions, stages, variable_info, outputs } = value;
        let conjunctions = vec_from_proto(conjunctions)?;
        let stages = vec_from_proto(stages)?;
        let variable_names = variable_info.into_iter().map(|(k, v)| (Variable(k), v.name)).collect();
        let outputs = vec_from_proto(outputs)?;
        Ok(Self { conjunctions, stages, variable_names, outputs })
    }
}

impl TryFromProto<pipeline_structure::PipelineStage> for PipelineStage {
    fn try_from_proto(value: pipeline_structure::PipelineStage) -> Result<Self> {
        use pipeline_structure::{pipeline_stage as stage_proto, pipeline_stage::Stage as StageProto};
        let unwrapped = value.stage.ok_or_else(|| {
            crate::Error::Analyze(AnalyzeError::MissingResponseField { field: "PipelineStage.stage" })
        })?;
        let stage = match unwrapped {
            StageProto::Match(stage_proto::Match { block }) => Self::Match { block: ConjunctionID(block as usize) },
            StageProto::Insert(stage_proto::Insert { block }) => Self::Insert { block: ConjunctionID(block as usize) },
            StageProto::Put(stage_proto::Put { block }) => Self::Put { block: ConjunctionID(block as usize) },
            StageProto::Update(stage_proto::Update { block }) => Self::Update { block: ConjunctionID(block as usize) },
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
            StageProto::Offset(stage_proto::Offset { offset }) => Self::Offset { offset },
            StageProto::Limit(stage_proto::Limit { limit }) => Self::Limit { limit },
            StageProto::Distinct(stage_proto::Distinct {}) => Self::Distinct,
            StageProto::Reduce(stage_proto::Reduce { reducers, groupby }) => {
                let reducers = vec_from_proto(reducers)?;
                let groupby = vec_from_proto(groupby)?;
                Self::Reduce { reducers, groupby }
            }
        };
        Ok(stage)
    }
}

impl TryFromProto<pipeline_structure::pipeline_stage::reduce::ReduceAssign> for ReduceAssign {
    fn try_from_proto(value: pipeline_structure::pipeline_stage::reduce::ReduceAssign) -> Result<Self> {
        Ok(Self {
            assigned: expect_try_into(value.assigned, "ReduceAssign.assigned")?,
            reducer: expect_try_into(value.reducer, "ReduceAssign.reducer")?,
        })
    }
}

impl TryFromProto<query_structure::Reducer> for Reducer {
    fn try_from_proto(value: query_structure::Reducer) -> Result<Self> {
        Ok(Self { arguments: vec_from_proto(value.variables)?, reducer: value.reducer })
    }
}

impl TryFromProto<conjunction_structure::StructureConstraint> for Constraint {
    fn try_from_proto(value: StructureConstraint) -> Result<Self> {
        use conjunction_structure::{
            structure_constraint as constraint_proto, structure_constraint::Constraint as ConstraintProto,
        };
        let unwrapped = value.constraint.ok_or_else(|| {
            crate::Error::Analyze(AnalyzeError::MissingResponseField { field: "StructureConstraint.constraint" })
        })?;
        let constraint = match unwrapped {
            ConstraintProto::Or(constraint_proto::Or { branches }) => {
                let branches = branches.iter().map(|branch| ConjunctionID(*branch as usize)).collect();
                Constraint::Or { branches }
            }
            ConstraintProto::Not(constraint_proto::Not { conjunction }) => {
                Constraint::Not { conjunction: ConjunctionID(conjunction as usize) }
            }
            ConstraintProto::Try(constraint_proto::Try { conjunction }) => {
                Constraint::Try { conjunction: ConjunctionID(conjunction as usize) }
            }
            ConstraintProto::Isa(constraint_proto::Isa { thing: instance, r#type, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Isa {
                    instance: expect_try_into(instance, "structure_constraint::Isa.instance")?,
                    r#type: expect_try_into(r#type, "structure_constraint::Isa.type")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Has(constraint_proto::Has { owner, attribute, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Has {
                    owner: expect_try_into(owner, "structure_constraint::Has.owner")?,
                    attribute: expect_try_into(attribute, "structure_constraint::Has.attribute")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Links(constraint_proto::Links { relation, player, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Links {
                    relation: expect_try_into(relation, "structure_constraint::Links.relation")?,
                    player: expect_try_into(player, "structure_constraint::Links.player")?,
                    role: expect_try_into(role, "structure_constraint::Links.role")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Kind(constraint_proto::Kind { kind, r#type }) => Constraint::Kind {
                kind: Kind::try_from_proto(kind)?,
                r#type: expect_try_into(r#type, "structure_constraint::Kind.kind")?,
            },
            ConstraintProto::Sub(constraint_proto::Sub { subtype, supertype, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Sub {
                    subtype: expect_try_into(subtype, "structure_constraint::Sub.subtype")?,
                    supertype: expect_try_into(supertype, "structure_constraint::Sub.supertype")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Owns(constraint_proto::Owns { owner, attribute, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Owns {
                    owner: expect_try_into(owner, "structure_constraint::Owns.owner")?,
                    attribute: expect_try_into(attribute, "structure_constraint::Owns.attribute")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Relates(constraint_proto::Relates { relation, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Relates {
                    relation: expect_try_into(relation, "structure_constraint::Relates.relation")?,
                    role: expect_try_into(role, "structure_constraint::Relates.role")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Plays(constraint_proto::Plays { player, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Plays {
                    player: expect_try_into(player, "structure_constraint::Plays.player")?,
                    role: expect_try_into(role, "structure_constraint::Plays.role")?,
                    exactness: ConstraintExactness::try_from_proto(exactness_proto)?,
                }
            }
            ConstraintProto::Value(constraint_proto::Value { attribute_type, value_type }) => Constraint::Value {
                attribute_type: expect_try_into(attribute_type, "structure_constraint::Value.attribute_type")?,
                value_type: expect_try_into(value_type, "structure_constraint::Value.value_type")?,
            },
            ConstraintProto::Label(constraint_proto::Label { r#type, label }) => {
                Constraint::Label { r#type: expect_try_into(r#type, "structure_constraint::Label.type")?, label }
            }
            ConstraintProto::Comparison(constraint_proto::Comparison { lhs, rhs, comparator }) => {
                let comparator =
                    enum_from_proto::<conjunction_structure::structure_constraint::comparison::Comparator>(comparator)?;
                Constraint::Comparison {
                    lhs: expect_try_into(lhs, "structure_constraint::Comparison.lhs")?,
                    rhs: expect_try_into(rhs, "structure_constraint::Comparison.rhs")?,
                    comparator: comparator.as_str_name().to_owned(),
                }
            }
            ConstraintProto::Expression(constraint_proto::Expression { assigned, arguments, text }) => {
                Constraint::Expression {
                    assigned: vec_from_proto(assigned)?,
                    arguments: vec_from_proto(arguments)?,
                    text,
                }
            }
            ConstraintProto::FunctionCall(constraint_proto::FunctionCall { name, assigned, arguments }) => {
                Constraint::FunctionCall {
                    name,
                    assigned: vec_from_proto(assigned)?,
                    arguments: vec_from_proto(arguments)?,
                }
            }
            ConstraintProto::Is(constraint_proto::Is { lhs, rhs }) => Constraint::Is {
                lhs: expect_try_into(lhs, "structure_constraint::Is.lhs")?,
                rhs: expect_try_into(rhs, "structure_constraint::Is.rhs")?,
            },
            ConstraintProto::Iid(constraint_proto::Iid { concept, iid }) => {
                Constraint::Iid { concept: expect_try_into(concept, "structure_constraint::Iid.concept")?, iid }
            }
        };
        Ok(constraint)
    }
}

impl TryFromProto<typedb_protocol::ConjunctionStructure> for Conjunction {
    fn try_from_proto(proto: ConjunctionStructure) -> Result<Self> {
        Ok(Self { constraints: vec_from_proto(proto.constraints)? })
    }
}

impl TryFromProto<pipeline_structure::pipeline_stage::sort::SortVariable> for SortVariable {
    fn try_from_proto(value: pipeline_structure::pipeline_stage::sort::SortVariable) -> Result<Self> {
        use pipeline_structure::pipeline_stage::sort::sort_variable::SortDirection;
        Ok(Self {
            variable: expect_try_into(value.variable, "SortVariable.variable")?,
            order: match enum_from_proto(value.direction)? {
                SortDirection::Asc => SortOrder::Ascending,
                SortDirection::Desc => SortOrder::Descending,
            },
        })
    }
}

impl TryFromProto<conjunction_structure::structure_vertex::Label> for LabelVertex {
    fn try_from_proto(value: conjunction_structure::structure_vertex::Label) -> Result<Self> {
        use conjunction_structure::structure_vertex::label::Label as LabelProto;
        match value.label {
            Some(LabelProto::FailedInference(label)) => Ok(LabelVertex::Unresolved(label)),
            Some(LabelProto::Resolved(type_proto)) => {
                let type_ = type_::Type::try_from_proto(type_proto)?;
                Ok(LabelVertex::Resolved(type_))
            }
            None => Err(AnalyzeError::MissingResponseField { field: "LabelVertex.label" }.into()),
        }
    }
}

impl TryFromProto<conjunction_structure::StructureVertex> for ConstraintVertex {
    fn try_from_proto(value: conjunction_structure::StructureVertex) -> Result<Self> {
        use conjunction_structure::structure_vertex::Vertex;
        match value.vertex {
            Some(Vertex::Variable(variable)) => Ok(ConstraintVertex::Variable(Variable::try_from_proto(variable)?)),
            Some(Vertex::Label(label)) => Ok(ConstraintVertex::Label(LabelVertex::try_from_proto(label)?)),
            Some(Vertex::Value(value)) => Ok(ConstraintVertex::Value(Value::try_from_proto(value)?)),
            None => Err(AnalyzeError::MissingResponseField { field: "StructureVertex.vertex" }.into()),
        }
    }
}

impl TryFromProto<conjunction_structure::structure_constraint::ConstraintExactness> for ConstraintExactness {
    fn try_from_proto(value: conjunction_structure::structure_constraint::ConstraintExactness) -> Result<Self> {
        use conjunction_structure::structure_constraint::ConstraintExactness as ConstraintExactnessProto;
        Ok(match value {
            ConstraintExactnessProto::Exact => ConstraintExactness::Exact,
            ConstraintExactnessProto::Subtypes => ConstraintExactness::Subtypes,
        })
    }
}

impl TryFromProto<conjunction_structure::Variable> for Variable {
    fn try_from_proto(value: conjunction_structure::Variable) -> Result<Self> {
        Ok(Self(value.id))
    }
}

// Annotations

impl TryFromProto<typedb_protocol::analyze::res::QueryAnnotations> for QueryAnnotations {
    fn try_from_proto(_value: typedb_protocol::analyze::res::QueryAnnotations) -> Result<Self> {
        Ok(Self {}) // TODO
    }
}
