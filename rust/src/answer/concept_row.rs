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
    fmt,
    fmt::{Debug, Formatter},
    sync::Arc,
};

use itertools::Itertools;

use crate::{answer::QueryType, concept::Concept};

#[derive(Debug, PartialEq)]
pub struct ConceptRowHeader {
    pub column_names: Vec<String>,
    pub query_type: QueryType,
}

impl ConceptRowHeader {
    fn get_index(&self, name: &str) -> Option<usize> {
        self.column_names.iter().find_position(|column_name| **column_name == name).map(|(pos, _)| pos)
    }
}

/// A single row of concepts representing substitutions for variables in the query.
/// Contains a Header (column names and query type), and the row of optional concepts.
/// An empty concept in a column means the variable does not have a substitution in this answer.
#[derive(Clone, PartialEq)]
pub struct ConceptRow {
    header: Arc<ConceptRowHeader>,
    pub row: Vec<Option<Concept>>,
}

impl ConceptRow {
    pub fn new(header: Arc<ConceptRowHeader>, row: Vec<Option<Concept>>) -> Self {
        Self { header, row }
    }

    /// Retrieve the row column names (shared by all elements in this stream).
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get_column_names()
    /// ```
    pub fn get_column_names(&self) -> &[String] {
        &self.header.column_names
    }

    /// Retrieve the executed query's type (shared by all elements in this stream).
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get_query_type()
    /// ```
    pub fn get_query_type(&self) -> QueryType {
        self.header.query_type
    }

    /// Retrieves a concept for a given variable. Returns an empty optional if
    /// the variable name has an empty answer, or if the variable name is not present.
    ///
    /// # Arguments
    ///
    /// * `var_name` -- The variable name in the row to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get(var_name)
    /// ```
    pub fn get(&self, column_name: &str) -> Option<&Concept> {
        self.header.get_index(column_name).and_then(|index| self.get_index(index))
    }

    /// Retrieves a concept for a given column index. Returns an empty optional if
    /// the position has an empty answer, or if the index is not range for the row.
    ///
    /// # Arguments
    ///
    /// * `column_index` -- The position in the row to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get_position(column_index)
    /// ```
    pub fn get_index(&self, column_index: usize) -> Option<&Concept> {
        self.row.get(column_index).and_then(|inner| inner.as_ref())
    }

    /// Produces an iterator over all concepts in this `ConceptRow`, skipping empty results
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.concepts()
    /// ```
    pub fn get_concepts(&self) -> impl Iterator<Item = &Concept> {
        self.row.iter().filter_map(|concept| concept.as_ref())
    }
}

impl fmt::Display for ConceptRow {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        fmt::Debug::fmt(self, f)
    }
}

impl fmt::Debug for ConceptRow {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "|")?;
        for (concept, name) in self.row.iter().zip(self.header.column_names.iter()) {
            match concept {
                None => write!(f, "  ${}: empty  ", name)?,
                Some(concept) => write!(f, "  ${}: {}  |", name, concept)?,
            }
        }
        Ok(())
    }
}
