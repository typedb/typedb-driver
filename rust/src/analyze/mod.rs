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

#[derive(Debug, Clone)]
pub struct AnalyzedQuery {
    pub query: Pipeline,
    pub preamble: Vec<Function>,
    pub fetch: Option<Fetch>,
}

#[derive(Debug, Clone)]
pub struct Function {
    pub argument_variables: Vec<Variable>,
    pub return_operation: ReturnOperation,
    pub body: Pipeline,
    pub argument_annotations: Vec<VariableAnnotations>,
    pub return_annotations: Vec<VariableAnnotations>,
}

#[derive(Debug, Clone)]
pub enum ReturnOperation {
    Stream { variables: Vec<Variable> },
    Single { selector: String, variables: Vec<Variable> },
    Check {},
    Reduce { reducers: Vec<Reducer> },
}

#[derive(Debug, Clone)]
pub enum Fetch {
    List(Box<Fetch>),
    Leaf(FetchLeaf),
    Object(HashMap<String, Fetch>),
}

#[derive(Debug, Clone)]
pub struct FetchLeaf {
    pub annotations: Vec<ValueType>,
}

#[derive(Debug, Clone)]
pub enum VariableAnnotations {
    Thing(Vec<Type>),
    Type(Vec<Type>),
    Value(ValueType),
}
