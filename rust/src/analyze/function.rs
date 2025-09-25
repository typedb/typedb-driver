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
use crate::analyze::{Context, FromPipelineProto, vec_from_proto};
use crate::analyze::conjunction::{Reducer, Variable};
use crate::analyze::pipeline::PipelineStructure;

#[derive(Debug)]
pub struct FunctionStructure {
    arguments: Vec<Variable>,
    returns: ReturnOperation,
    body: PipelineStructure,
}

impl From<query_structure::FunctionStructure> for FunctionStructure {
    fn from(value: query_structure::FunctionStructure) -> Self {
        let protocol_pipeline = value.body.expect("expected function body");
        let context = Context::build(&protocol_pipeline);
        let body = PipelineStructure::from_proto(&context, protocol_pipeline);
        let arguments = vec_from_proto(&context, value.arguments);
        let returns = ReturnOperation::from_proto(&context, value.returns.expect("expected function return"));
        Self { arguments, returns, body, }
    }
}

#[derive(Debug)]
pub enum ReturnOperation {
    Stream { variables: Vec<Variable> },
    Single { selector: String, variables: Vec<Variable> },
    Check {},
    Reduce { reducers: Vec<Reducer> },
}

impl FromPipelineProto<query_structure::function_structure::Returns> for ReturnOperation {
    fn from_proto(context: &Context, value: query_structure::function_structure::Returns) -> Self {
        use typedb_protocol::analyze::res::query_structure::function_structure::Returns;
        use crate::analyze;
        match value {
            Returns::Stream(stream) => {
                Self::Stream {variables: analyze::vec_from_proto(context, stream.variables) }
            }
            Returns::Single(single) => {
                Self::Single {
                    selector: single.selector,
                    variables: analyze::vec_from_proto(context, single.variables)
                }
            }
            Returns::Check(_check) => {
                Self::Check {}
            },
            Returns::Reduce(reduce) => {
                Self::Reduce {
                    reducers: analyze::vec_from_proto(context, reduce.reducers)
                }
            }
        }
    }
}
