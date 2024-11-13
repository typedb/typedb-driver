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

use std::{fmt, sync::Arc};

pub use self::{concept_document::ConceptDocument, concept_row::ConceptRow, json::JSON, value_group::ValueGroup};
use crate::{
    answer::{concept_document::ConceptDocumentHeader, concept_row::ConceptRowHeader},
    BoxStream, Result,
};

pub mod concept_document;
pub mod concept_row;
mod json;
mod value_group;

pub enum QueryAnswer {
    Ok(QueryType),
    ConceptRowStream(Arc<ConceptRowHeader>, BoxStream<'static, Result<ConceptRow>>),
    ConceptDocumentStream(Arc<ConceptDocumentHeader>, BoxStream<'static, Result<ConceptDocument>>),
}

impl QueryAnswer {
    /// Retrieve the executed query's type (shared by all elements in this stream).
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.get_query_type()
    /// ```
    pub fn get_query_type(&self) -> QueryType {
        match self {
            &QueryAnswer::Ok(query_type) => query_type,
            QueryAnswer::ConceptRowStream(header, _) => header.query_type,
            QueryAnswer::ConceptDocumentStream(header, _) => header.query_type,
        }
    }

    /// Check if the <code>QueryAnswer</code> is an <code>Ok</code> response.
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.is_ok()
    /// ```
    pub fn is_ok(&self) -> bool {
        matches!(self, Self::Ok(_))
    }

    /// Check if the <code>QueryAnswer</code> is a <code>ConceptRowStream</code>.
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.is_row_stream()
    /// ```
    pub fn is_row_stream(&self) -> bool {
        matches!(self, Self::ConceptRowStream(_, _))
    }

    /// Check if the <code>QueryAnswer</code> is a <code>ConceptDocumentStream</code>.
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.is_document_stream()
    /// ```
    pub fn is_document_stream(&self) -> bool {
        matches!(self, Self::ConceptDocumentStream(_, _))
    }

    /// Unwraps the <code>QueryAnswer</code> into a <code>ConceptRowStream</code>.
    /// Panics if it is not a <code>ConceptRowStream</code>.
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.into_rows()
    /// ```
    pub fn into_rows(self) -> BoxStream<'static, Result<ConceptRow>> {
        if let Self::ConceptRowStream(_, stream) = self {
            stream
        } else {
            panic!("Query answer is not a rows stream.")
        }
    }

    /// Unwraps the <code>QueryAnswer</code> into a <code>ConceptDocumentStream</code>.
    /// Panics if it is not a <code>ConceptDocumentStream</code>.
    ///
    /// # Examples
    ///
    /// ```rust
    /// query_answer.into_documents()
    /// ```
    pub fn into_documents(self) -> BoxStream<'static, Result<ConceptDocument>> {
        if let Self::ConceptDocumentStream(_, stream) = self {
            stream
        } else {
            panic!("Query answer is not a documents stream.")
        }
    }
}

impl fmt::Debug for QueryAnswer {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            QueryAnswer::Ok(_) => write!(f, "QueryAnswer::Ok"),
            QueryAnswer::ConceptRowStream(_, _) => write!(f, "QueryAnswer::ConceptRowStream(<stream>)"),
            QueryAnswer::ConceptDocumentStream(_, _) => write!(f, "QueryAnswer::ConceptDocumentStream(<stream>)"),
        }
    }
}

/// This enum is used to specify the type of the query resulted in this answer.
///
/// # Examples
///
/// ```rust
/// concept_row.get_query_type()
/// ```
#[repr(C)]
#[derive(Clone, Copy, Hash, PartialEq, Eq, Debug)]
pub enum QueryType {
    ReadQuery,
    WriteQuery,
    SchemaQuery,
}
