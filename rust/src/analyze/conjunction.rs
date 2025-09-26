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

use typedb_protocol::analyze::res::query_structure;
use typedb_protocol::conjunction_structure;
use typedb_protocol::conjunction_structure::{StructureConstraint, StructureVertex};
use crate::analyze::{enum_from_proto, expect_try_into, vec_from_proto};
use crate::concept;

#[derive(Debug)]
pub struct ConjunctionID(pub usize);

#[derive(Debug)]
pub struct Conjunction {
    constraints: Vec<Constraint>,
}

impl TryFrom<typedb_protocol::ConjunctionStructure> for Conjunction {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: typedb_protocol::ConjunctionStructure) -> Result<Self, Self::Error> {
        Ok(Self { constraints: vec_from_proto(value.constraints)? })
    }
}

#[derive(Debug)]
pub enum ConstraintExactness {
    Exact,
    Subtypes,
}

impl TryFrom<conjunction_structure::structure_constraint::ConstraintExactness> for ConstraintExactness {
    type Error = crate::analyze::TryFromError;

    fn try_from(value: conjunction_structure::structure_constraint::ConstraintExactness) -> Result<Self, Self::Error> {
        use conjunction_structure::structure_constraint::ConstraintExactness as ExactnessProto;
        match value {
            ExactnessProto::Exact => Ok(ConstraintExactness::Exact),
            ExactnessProto::Subtypes => Ok(ConstraintExactness::Subtypes),
        }
    }
}

#[derive(Debug)]
pub enum Constraint {
    Isa {
        instance: ConstraintVertex,
        r#type: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    Has {
        owner: ConstraintVertex,
        attribute: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    Links {
        relation: ConstraintVertex,
        player: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },

    Sub {
        subtype: ConstraintVertex,
        supertype: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    Owns {
        owner: ConstraintVertex,
        attribute: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    Relates {
        relation: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    Plays {
        player: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },

    FunctionCall {
        name: String,
        assigned: Vec<ConstraintVertex>,
        arguments: Vec<ConstraintVertex>,
    },
    Expression {
        text: String,
        assigned: Vec<ConstraintVertex>,
        arguments: Vec<ConstraintVertex>,
    },
    Is {
        lhs: ConstraintVertex,
        rhs: ConstraintVertex,
    },
    Iid {
        concept: ConstraintVertex,
        iid: Vec<u8>,
    },
    Comparison {
        lhs: ConstraintVertex,
        rhs: ConstraintVertex,
        comparator: String,
    },
    Kind {
        kind: concept::Kind,
        r#type: ConstraintVertex,
    },
    Label {
        r#type: ConstraintVertex,
        label: String,
    },
    Value {
        attribute_type: ConstraintVertex,
        value_type: String,
    },

    // Nested patterns are now constraints too
    Or {
        branches: Vec<ConjunctionID>,
    },
    Not {
        conjunction: ConjunctionID,
    },
    Try {
        conjunction: ConjunctionID,
    },
}

impl TryFrom<conjunction_structure::StructureConstraint> for Constraint {
    type Error = crate::analyze::TryFromError;

    fn try_from(value: StructureConstraint) -> Result<Self, Self::Error> {
        use conjunction_structure::{structure_constraint as constraint_proto, structure_constraint::{Constraint as ConstraintProto}};
        let constraint = match value.constraint.ok_or("expected constraint")? {
            ConstraintProto::Or(constraint_proto::Or { branches }) => {
                let branches = branches
                    .iter()
                    .map(|branch| ConjunctionID(*branch as usize))
                    .collect();
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
                    instance: expect_try_into(instance)?,
                    r#type: expect_try_into(r#type)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?,
                }
            }
            ConstraintProto::Has(constraint_proto::Has { owner, attribute, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Has {
                    owner: expect_try_into(owner)?,
                    attribute: expect_try_into(attribute)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Links(constraint_proto::Links { relation, player, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Links {
                    relation: expect_try_into(relation)?,
                    player: expect_try_into(player)?,
                    role: expect_try_into(role)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Kind(constraint_proto::Kind { kind, r#type }) => {
                Constraint::Kind { kind, r#type: expect_try_into(r#type)? }
            }
            ConstraintProto::Sub(constraint_proto::Sub { subtype, supertype, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Sub {
                    subtype: expect_try_into(subtype)?,
                    supertype: expect_try_into(supertype)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Owns(constraint_proto::Owns { owner, attribute, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Owns {
                    owner: expect_try_into(owner)?,
                    attribute: expect_try_into(attribute)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Relates(constraint_proto::Relates { relation, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Relates {
                    relation: expect_try_into(relation)?,
                    role: expect_try_into(role)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Plays(constraint_proto::Plays { player, role, exactness }) => {
                let exactness_proto = enum_from_proto::<constraint_proto::ConstraintExactness>(exactness)?;
                Constraint::Plays {
                    player: expect_try_into(player)?,
                    role: expect_try_into(role)?,
                    exactness: ConstraintExactness::try_from(exactness_proto)?
                }
            }
            ConstraintProto::Value(constraint_proto::Value { attribute_type, value_type }) => {
                Constraint::Value {
                    attribute_type: expect_try_into(attribute_type)?,
                    value_type,
                }
            }
            ConstraintProto::Label(constraint_proto::Label { r#type, label }) => {
                Constraint::Label {
                    r#type: expect_try_into(r#type)?,
                    label,
                }
            }
            ConstraintProto::Comparison(constraint_proto::Comparison { lhs, rhs, comparator }) => {
                let comparator = enum_from_proto::<conjunction_structure::structure_constraint::comparison::Comparator>(comparator)?;
                Constraint::Comparison {
                    lhs: expect_try_into(lhs)?,
                    rhs: expect_try_into(rhs)?,
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
            ConstraintProto::Is(constraint_proto::Is { lhs, rhs }) => {
                Constraint::Is {
                    lhs: expect_try_into(lhs)?,
                    rhs: expect_try_into(rhs)?,
                }
            }
            ConstraintProto::Iid(constraint_proto::Iid { concept, iid }) => {
                Constraint::Iid {
                    concept: expect_try_into(concept)?,
                    iid,
                }
            }
        };
        Ok(constraint)
    }
}

#[derive(Debug)]
pub struct Variable(u32);

impl TryFrom<conjunction_structure::Variable> for Variable {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: conjunction_structure::Variable) -> Result<Self, Self::Error> {
        Ok(Self(value.id))
    }
}

#[derive(Debug)]
pub enum ConstraintVertex {
    Variable(Variable),
    Label(LabelVertex),
    Value(concept::Value),
}

impl TryFrom<conjunction_structure::StructureVertex> for ConstraintVertex {
    type Error = crate::analyze::TryFromError;

    fn try_from(value: conjunction_structure::StructureVertex) -> Result<Self, Self::Error> {
        use conjunction_structure::structure_vertex::Vertex;
        match value.vertex.ok_or("Expected vertex")? {
            Vertex::Variable(variable) => Ok(ConstraintVertex::Variable(variable.try_into()?)),
            Vertex::Label(label) => Ok(ConstraintVertex::Label(LabelVertex::try_from(label)?)),
            Vertex::Value(value) => Ok(ConstraintVertex::Value(value.try_into()?)),
        }
    }
}

#[derive(Debug)]
pub enum LabelVertex {
    Resolved(concept::Concept),
    Unresolved(String),
}

impl TryFrom<conjunction_structure::structure_vertex::Label> for LabelVertex {
    type Error = crate::analyze::TryFromError;

    fn try_from(value: conjunction_structure::structure_vertex::Label) -> Result<Self, Self::Error> {
        use conjunction_structure::structure_vertex::label::Label as LabelProto;
        match value.label.ok_or("Expected label")? {
            LabelProto::Resolved(concept) => Ok(LabelVertex::Resolved(concept.try_into()?)),
            LabelProto::FailedInference(label) => Ok(LabelVertex::Unresolved(label.try_into()?)),
        }
    }
}

#[derive(Debug)]
pub struct Reducer {
    variables: Vec<Variable>,
    reducer: String,
}

impl TryFrom<query_structure::Reducer> for Reducer {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: query_structure::Reducer) -> Result<Self, Self::Error> {
        Ok(Self {
            variables: vec_from_proto(value.variables)?,
            reducer: value.reducer,
        })
    }
}
