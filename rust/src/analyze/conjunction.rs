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

use crate::{analyze::VariableAnnotations, concept, IID};

/// Holds the index of the conjunction in a <code>Pipeline</code>'s <code>conjunctions</code> field.
/// Used as indirection in the representation of a pipeline.
#[repr(C)]
#[derive(Debug, Clone, Copy)]
pub struct ConjunctionID(pub usize);

/// A representation of the constraints involved in the query, and types inferred for each variable.
#[derive(Debug, Clone)]
pub struct Conjunction {
    /// The <code>Constraint</code>s in the conjunction.
    pub constraints: Vec<ConstraintWithSpan>,
    /// The annotations of each variable in the conjunction.
    pub variable_annotations: HashMap<Variable, VariableAnnotations>,
}

/// Tells apart exact variants of constraints from the ones allowing subtype-polymorphism.
/// e.g. <code>isa!</code> would be represented as an <code>Constraint::Isa</code>
/// with its exactness field <code>ConstraintExactness::Exact</code>.
#[repr(C)]
#[derive(Debug, Clone)]
pub enum ConstraintExactness {
    /// Indicates the constraint matches exactly the specified type - e.g. `isa!` or `sub!`
    Exact,
    /// Indicates the constraint matches the specified type and its subtypes - e.g. `isa!` or `sub!`
    Subtypes,
}

/// The span of a constraint in the <code>source</code> of the <code>AnalyzedQuery</code>.
#[derive(Debug, Clone)]
pub struct ConstraintSpan {
    /// The offset of the first character in the span
    pub begin: usize,
    // TODO: Is this the offset of the last character, or after the last character?
    /// The offset of the last character in the span
    pub end: usize,
}

#[derive(Debug, Clone)]
pub struct ConstraintWithSpan {
    pub constraint: Constraint,
    pub span: Option<ConstraintSpan>,
}

/// A representation of a TypeQL constraint.
#[derive(Debug, Clone)]
pub enum Constraint {
    /// <instance> isa(!) <type>
    Isa {
        instance: ConstraintVertex,
        r#type: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <owner> has <attribute>
    Has {
        owner: ConstraintVertex,
        attribute: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <relation> links (<role>: <player>)
    Links {
        relation: ConstraintVertex,
        player: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <subtype> sub(!) <supertype>
    Sub {
        subtype: ConstraintVertex,
        supertype: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <owner> owns <attribute>
    Owns {
        owner: ConstraintVertex,
        attribute: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <relation> relates <role>
    Relates {
        relation: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// <player> plays <role>
    Plays {
        player: ConstraintVertex,
        role: ConstraintVertex,
        exactness: ConstraintExactness,
    },
    /// let <assigned> = name(<arguments>)
    /// e.g. let $x, $y = my_function($a, $b);
    FunctionCall {
        name: String,
        assigned: Vec<ConstraintVertex>,
        arguments: Vec<ConstraintVertex>,
    },
    /// let <assigned> = <expression>
    /// e.g. let $x = $y + 5;
    /// Here, arguments will be `[$y]`
    Expression {
        text: String,
        assigned: ConstraintVertex,
        arguments: Vec<ConstraintVertex>,
    },
    /// <lhs> is <rhs>
    /// $x is $y
    Is {
        lhs: ConstraintVertex,
        rhs: ConstraintVertex,
    },
    /// <concept> iid <iid>
    /// e.g. `$y iid 0x1f0005000000000000012f`
    Iid {
        concept: ConstraintVertex,
        iid: IID,
    },
    /// <lhs> <comparator> <rhs>
    /// e.g. `$x < 5`
    Comparison {
        lhs: ConstraintVertex,
        rhs: ConstraintVertex,
        comparator: Comparator,
    },
    /// <kind> <type>
    /// e.g. `entity person`
    Kind {
        kind: concept::Kind,
        r#type: ConstraintVertex,
    },
    /// <type> label <label>
    /// e.g. `$t label person`
    Label {
        r#type: ConstraintVertex,
        label: String,
    },
    /// <attribute_type> value <value_type>
    /// e.g. $t value string
    Value {
        attribute_type: ConstraintVertex,
        value_type: concept::ValueType,
    },
    /// { <branches[0]> } or { <branches[1]> } [or ...]
    Or {
        /// Index into <code>Pipeline.conjunctions</code>
        branches: Vec<ConjunctionID>,
    },
    /// not { <conjunction> }
    Not {
        /// Index into <code>Pipeline.conjunctions</code>
        conjunction: ConjunctionID,
    },
    /// try { <conjunction> }
    Try {
        /// Index into <code>Pipeline.conjunctions</code>
        conjunction: ConjunctionID,
    },
}

/// Uniquely identifies a variable in a <code>Pipeline</code>pipeline.
/// Its name (if any) can be retrieved from the <code>variable_names</code> field in <code>Pipeline</code>
#[derive(Debug, Hash, Clone, Eq, PartialEq)]
pub struct Variable(pub u32);

/// The answer to a TypeDB query is a set of concepts which satisfy the <code>Constraints</code> in the query.
/// A <code>ConstraintVertex</code> is either a variable, or some identifier of the concept.
///
/// * A <code>Variable</code> is a vertex the query must match and return.
/// * A <code>Label</code> uniquely identifies a type
/// * A <code>Value</code> represents a primitive value literal in TypeDB.
/// * A <code>NamedRole</code> vertex is used in links & relates constraints, as multiple relations may have roles with the same name.
/// The types inferred for <code>Variable</code>, <code>Label</code> and <code>NamedRole</code> vertices
/// can be read from the <code>variable_annotations</code> field of the <code>Conjunction</code> it is in.
#[derive(Debug, Clone)]
pub enum ConstraintVertex {
    Variable(Variable),
    Label(concept::type_::Type),
    Value(concept::Value),
    NamedRole(NamedRole),
}

/// A <code>NamedRole</code> vertex is used in links & relates constraints, as multiple relations may have roles with the same name.
#[derive(Debug, Clone)]
pub struct NamedRole {
    pub variable: Variable,
    pub name: String,
}

/// A representation of the comparator used in a comparison constraint.
#[repr(C)]
#[derive(Debug, Clone)]
pub enum Comparator {
    Equal,
    NotEqual,
    LessThan,
    LessOrEqual,
    Greater,
    GreaterOrEqual,
    Like,
    Contains,
}

impl Comparator {
    /// The symbol representing the comparator in TypeQL
    pub fn symbol(&self) -> &'static str {
        match self {
            Comparator::Equal => "==",
            Comparator::NotEqual => "!=",
            Comparator::LessThan => "<",
            Comparator::LessOrEqual => "<=",
            Comparator::Greater => ">",
            Comparator::GreaterOrEqual => ">=",
            Comparator::Like => "Like",
            Comparator::Contains => "Contains",
        }
    }
}
