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

use crate::{
    analyze::conjunction::ConjunctionID, answer::QueryType, common::Result, concept::Concept, error::ConceptError,
};

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
    involved_conjunctions: Option<Vec<u8>>,
}

impl ConceptRow {
    pub fn new(
        header: Arc<ConceptRowHeader>,
        row: Vec<Option<Concept>>,
        involved_conjunctions: Option<Vec<u8>>,
    ) -> Self {
        Self { header, involved_conjunctions, row }
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

    /// Retrieve the <code>ConjunctionID</code>s of <code>Conjunction</code>s that answered this row.
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get_involved_conjunctions()
    /// ```
    pub fn get_involved_conjunctions(&self) -> Option<impl Iterator<Item = ConjunctionID> + '_> {
        self.involved_conjunctions.as_ref().map(|involved| {
            (0..involved.len())
                .filter(|index| {
                    let index = index / 8;
                    let mask = 1 << (index % 8);
                    involved[index] & mask != 0
                })
                .map(ConjunctionID)
        })
    }

    /// Like <code>ConceptRow::get_involved_conjunctions</code> but clones the underlying data.
    /// Meant for simpler lifetimes over FFI.
    pub fn get_involved_conjunctions_cloned(&self) -> Option<impl Iterator<Item = ConjunctionID> + 'static> {
        let cloned = self.involved_conjunctions.clone();
        cloned.map(|involved| {
            (0..involved.len())
                .filter(move |index| {
                    let index = index / 8;
                    let mask = 1 << (index % 8);
                    involved[index] & mask != 0
                })
                .map(ConjunctionID)
        })
    }

    /// Retrieves a concept for a given variable. Returns an empty optional if
    /// the variable name has an empty answer. Returns an error if the variable name is not present.
    ///
    /// # Arguments
    ///
    /// * `var_name` — The variable name in the row to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get(var_name)
    /// ```
    pub fn get(&self, column_name: &str) -> Result<Option<&Concept>> {
        let index = self
            .header
            .get_index(column_name)
            .ok_or(ConceptError::UnavailableRowVariable { variable: column_name.to_string() })?;
        self.get_index(index)
    }

    /// Retrieves a concept for a given column index. Returns an empty optional if the index
    /// points to an empty answer. Returns an error if the index is not in the row's range.
    ///
    /// # Arguments
    ///
    /// * `column_index` — The position in the row to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_row.get_position(column_index)
    /// ```
    pub fn get_index(&self, column_index: usize) -> Result<Option<&Concept>> {
        let concept = self.row.get(column_index).ok_or(ConceptError::UnavailableRowIndex { index: column_index })?;
        Ok(concept.as_ref())
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
