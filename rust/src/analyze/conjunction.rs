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
use typedb_protocol::conjunction_structure::StructureConstraint;
use crate::analyze::vec_from_proto;
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
                Constraint::Or { branches: vec_from_proto(branches)? }
            }
            ConstraintProto::Not(constraint_proto::Not { conjunction }) => {
                constraint_proto::Not { conjunction: conjunction.try_into()? }
            }
            ConstraintProto::Try(constraint_proto::Try { conjunction }) => {
                constraint_proto::Try { conjunction: conjunction.try_into()? }
            }
            ConstraintProto::Isa(constraint_proto::Isa { thing: instance, r#type, exactness }) => {
                Constraint::Isa {
                    instance: instance.try_into()?,
                    r#type: r#type.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Has(constraint_proto::Has { owner, attribute, exactness }) => {
                Constraint::Has {
                    owner: owner.try_into()?,
                    attribute: attribute.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Links(constraint_proto::Links { relation, player, role, exactness }) => {
                Constraint::Links {
                    relation: relation.try_into()?,
                    player: player.try_into()?,
                    role: role.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Kind(constraint_proto::Kind { kind, r#type }) => {
                Constraint::Kind { kind, r#type: r#type.try_into()? }
            }
            ConstraintProto::Sub(constraint_proto::Sub { subtype, supertype, exactness }) => {
                Constraint::Sub {
                    subtype: subtype.try_into()?,
                    supertype: supertype.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Owns(constraint_proto::Owns { owner, attribute, exactness }) => {
                Constraint::Owns {
                    owner: owner.try_into()?,
                    attribute: attribute.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Relates(constraint_proto::Relates { relation, role, exactness }) => {
                Constraint::Relates {
                    relation: relation.try_into()?,
                    role: role.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Plays(constraint_proto::Plays { player, role, exactness }) => {
                Constraint::Plays {
                    player: player.try_into()?,
                    role: role.try_into()?,
                    exactness: ConstraintExactness::try_from(exactness)?
                }
            }
            ConstraintProto::Value(constraint_proto::Value { attribute_type, value_type }) => {
                Constraint::Value {
                    attribute_type: attribute_type.try_into()?,
                    value_type,
                }
            }
            ConstraintProto::Label(constraint_proto::Label { r#type, label }) => {
                Constraint::Label {
                    r#type: r#type.try_into()?,
                    label,
                }
            }
            ConstraintProto::Comparison(constraint_proto::Comparison { lhs, rhs, comparator }) => {
                let comparator = conjunction_structure::structure_constraint::comparison::Comparator::try_from(comparator)?;
                Constraint::Comparison {
                    lhs: lhs.try_into()?,
                    rhs: rhs.try_into()?,
                    comparator: comparator.as_str_name().to_owned(),
                }
            }
            ConstraintProto::Expression(constraint_proto::Expression { assigned, arguments, text }) => {
                Constraint::Expression {
                    assigned: assigned.try_into()?,
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
                    lhs: lhs.try_into()?,
                    rhs: rhs.try_into()?,
                }
            }
            ConstraintProto::Iid(constraint_proto::Iid { concept, iid }) => {
                Constraint::Iid {
                    concept: concept.try_into()?,
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
    Iid(String),
    Value(concept::Value),
}

#[derive(Debug)]
pub enum LabelVertex {
    Resolve(concept::Concept),
    Unresolved(String),
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
