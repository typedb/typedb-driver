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

use crate::answer::concept_row::ConceptRowsHeader;
use crate::answer::concept_tree::{ConceptTreesHeader, Tree};
use crate::BoxStream;
pub use self::{
    concept_row::ConceptRow,
    json::JSON,
    value_group::ValueGroup,
};
use crate::Result;

pub mod concept_row;
mod json;
pub(crate) mod concept_tree;
mod value_group;


pub enum QueryAnswer {
    Ok(),
    ConceptRowsStream(BoxStream<'static, Result<ConceptRow>>),
    ConceptTreesStream(ConceptTreesHeader, BoxStream<'static, crate::Result<Tree>>),
}

impl QueryAnswer {
    pub fn into_rows(self) -> BoxStream<'static, Result<ConceptRow>> {
        if let Self::ConceptRowsStream(stream) = self {
            stream
        } else {
            panic!("Query answer is not a rows stream.")
        }
    }
}

