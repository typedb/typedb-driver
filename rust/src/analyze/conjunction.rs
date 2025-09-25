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

use typedb_protocol::analyze::{res as protocol, res::query_structure as query_structure_proto};
use typedb_protocol::conjunction_structure;
use crate::analyze::{Context, FromPipelineProto, vec_from_proto};

#[derive(Debug)]
pub struct ConjunctionId(pub usize);

#[derive(Debug)]
pub struct Conjunction {
    constraints: Vec<Constraint>,
}

impl FromPipelineProto<typedb_protocol::ConjunctionStructure> for Conjunction {
    fn from_proto(context: &Context, value: typedb_protocol::ConjunctionStructure) -> Self {
        Self { constraints: vec_from_proto(context, value.constraints) }
    }
}

#[derive(Debug)]
pub enum Constraint {

}

#[derive(Debug)]
pub struct Variable {
    id: u32,
    name: Option<String>,
}

impl FromPipelineProto<conjunction_structure::Variable> for Variable {
    fn from_proto(context: &Context, value: conjunction_structure::Variable) -> Self {
        Self { id: value.id, name: context.var_name(&value) }
    }
}
    #[derive(Debug)]
pub struct Reducer {
    variables: Vec<Variable>,
    reducer: String,
}

impl FromPipelineProto<protocol::query_structure::Reducer> for Reducer {
    fn from_proto(context: &Context, value: protocol::query_structure::Reducer) -> Self {
        Self {
            variables: vec_from_proto(context, value.variables),
            reducer: value.reducer,
        }
    }
}
