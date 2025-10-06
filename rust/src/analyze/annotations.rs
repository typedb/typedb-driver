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

use std::{
    collections::{BTreeMap, HashMap},
    vec::Vec,
};

use crate::{
    analyze::conjunction::Variable,
    concept::{type_::Type, ValueType},
};

#[derive(Debug)]
pub struct PipelineAnnotations {
    pub conjunction_annotations: Vec<ConjunctionAnnotations>,
}

#[derive(Debug)]
pub struct FunctionAnnotations {
    pub arguments: Vec<VariableAnnotations>,
    pub returns: FunctionReturnAnnotations,
    pub body: PipelineAnnotations,
}

#[derive(Debug)]
pub enum FunctionReturnAnnotations {
    Stream(Vec<VariableAnnotations>),
    Single(Vec<VariableAnnotations>),
}

#[derive(Debug)]
pub enum FetchAnnotations {
    List(Box<FetchAnnotations>),
    Leaf(Vec<ValueType>),
    Object(HashMap<String, FetchAnnotations>),
}

#[derive(Debug)]
pub struct ConjunctionAnnotations {
    pub variable_annotations: HashMap<Variable, VariableAnnotations>,
}

#[derive(Debug)]
pub enum VariableAnnotations {
    Thing(Vec<Type>),
    Type(Vec<Type>),
    Value(ValueType),
}
