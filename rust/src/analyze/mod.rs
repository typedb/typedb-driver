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

use crate::{
    analyze::{
        conjunction::Variable,
        pipeline::{Pipeline, Reducer},
    },
    concept::{type_::Type, ValueType},
};

pub mod conjunction;
pub mod pipeline;

/// An <code>AnalyzedQuery</code> contains the server's representation of the query and preamble functions;
/// as well as the result of types inferred for each variable by type-inference.
#[derive(Debug, Clone)]
pub struct AnalyzedQuery {
    /// The original TypeQL query string
    pub source: String,
    /// A representation of the query as a <code>Pipeline</code>
    pub query: Pipeline,
    /// A representation of the <code>Function</code>s in the preamble of the query
    pub preamble: Vec<Function>,
    /// A representation of the <code>Fetch</code> stage of the query, if it has one
    pub fetch: Option<Fetch>,
}

/// Holds a representation of the function, and the result of type-inference for each variable.
#[derive(Debug, Clone)]
pub struct Function {
    /// The <code>Variable</code>s which are the arguments of the function.
    pub argument_variables: Vec<Variable>,
    /// A representation of the <code>ReturnOperation</code> of the function.
    pub return_operation: ReturnOperation,
    /// A representation of the <code>Pipeline</code> which forms the body of the function.
    pub body: Pipeline,
    /// The inferred type for each argument of the function.
    pub argument_annotations: Vec<VariableAnnotations>,
    /// The inferred type for each concept returned by the function.
    pub return_annotations: Vec<VariableAnnotations>,
}

/// A representation of the return operation of the function
#[derive(Debug, Clone)]
pub enum ReturnOperation {
    /// Indicates the function returns a stream of concepts.
    /// e.g. <code>return { $x, $y };</code>
    Stream {
        /// The <code>Variables</code> in the returned row.
        variables: Vec<Variable>,
    },
    /// Indicates the function returns a single row of the specified <code>Variables</code>.
    /// e.g. <code>return first $x, $y;</code>
    Single {
        /// Determines how the operation used to select the row.
        selector: String,
        /// The <code>Variables</code> in the returned row.
        variables: Vec<Variable>,
    },
    /// Indicates the function returns a boolean - true if the body had answers, false otherwise.
    /// e.g. <code>return check;</code>
    Check {},
    /// Indicates the function returns an aggregation over the rows in the body.
    Reduce {
        /// The <code>Reducers</code>s used to compute the aggregations.
        /// e.g. <code>return count($x), sum($y);</code>
        reducers: Vec<Reducer>,
    },
}

/// A representation of the 'fetch' stage of a query
#[derive(Debug, Clone)]
pub enum Fetch {
    /// Indicates the value is a list of <code>Fetch</code> documents.
    List(Box<Fetch>),
    /// Indicates the value is a raw value.
    Leaf(FetchLeaf),
    /// Indicates the value is a mapping of string keys to <code>Fetch</code> documents.
    Object(HashMap<String, Fetch>),
}

/// Holds typing information about a leaf value in a <code>Fetch</code> document.
#[derive(Debug, Clone)]
pub struct FetchLeaf {
    /// The <code>ValueType</code> this value can be.
    pub annotations: Vec<ValueType>,
}

#[derive(Debug, Clone)]
pub struct VariableAnnotations {
    pub(crate) is_optional: bool, // TODO: Make pub when we know this is correct
    /// The <code>TypeAnnotations</code> of this variable.
    pub types: TypeAnnotations,
}

/// The category of a variable, and the possible types determined by type-inference.
#[derive(Debug, Clone)]
pub enum TypeAnnotations {
    /// Indicates the variable holds instances of any of the specified <code>Type</code>s.
    Instance(Vec<Type>),
    /// Indicates the variable holds types of any of the specified <code>Type</code>s.
    Type(Vec<Type>),
    /// Indicates the variable holds values of any of the specified <code>ValueType</code>s.
    Value(ValueType),
}
