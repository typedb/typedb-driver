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
use crate::analyze::vec_from_proto;
use crate::analyze::conjunction::{Reducer, Variable};
use crate::analyze::pipeline::PipelineStructure;

#[derive(Debug)]
pub struct FunctionStructure {
    arguments: Vec<Variable>,
    returns: ReturnOperation,
    body: PipelineStructure,
}

impl TryFrom<query_structure::FunctionStructure> for FunctionStructure {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: query_structure::FunctionStructure) -> Result<Self, Self::Error> {
        let body = PipelineStructure::try_from(value.body.ok_or("expected function body")?)?;
        let returns = ReturnOperation::try_from(value.returns.ok_or("expected function return")?)?;
        let arguments = vec_from_proto(value.arguments)?;
        Ok(Self { arguments, returns, body })
    }
}

#[derive(Debug)]
pub enum ReturnOperation {
    Stream { variables: Vec<Variable> },
    Single { selector: String, variables: Vec<Variable> },
    Check {},
    Reduce { reducers: Vec<Reducer> },
}

impl TryFrom<query_structure::function_structure::Returns> for ReturnOperation {
    type Error = crate::analyze::TryFromError;

	fn try_from(value: query_structure::function_structure::Returns) -> Result<Self, Self::Error> {
        use typedb_protocol::analyze::res::query_structure::function_structure::Returns;
        let returns = match value {
            Returns::Stream(stream) => {
                Self::Stream {variables: vec_from_proto(stream.variables)? }
            }
            Returns::Single(single) => {
                Self::Single {
                    selector: single.selector,
                    variables: vec_from_proto(single.variables)?
                }
            }
            Returns::Check(_check) => {
                Self::Check {}
            },
            Returns::Reduce(reduce) => {
                Self::Reduce {
                    reducers: vec_from_proto(reduce.reducers)?
                }
            }
        };
        Ok(returns)
    }
}
