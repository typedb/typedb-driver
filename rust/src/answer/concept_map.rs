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
    collections::{hash_map, HashMap},
    ops::Index,
};
use itertools::Itertools;

use crate::concept::Concept;

/// Contains a mapping of variables to concepts.
#[derive(Clone, Debug, PartialEq)]
pub struct AnswerRow {
    pub concepts: Vec<Option<Concept>>,
}

impl AnswerRow {
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
    /// answer_row.get_position(column_index)
    /// ```
    pub fn get_index(&self, column_index: usize) -> Option<&Concept> {
        self.concepts.get(column_index).map(|inner| inner.as_ref()).flatten()
    }

    /// Retrieves a concept for a given variable name and the column names. Returns an empty optional if
    /// the variable name has an empty answer, or if the variable name is present.
    ///
    /// # Arguments
    ///
    /// * `var_name` -- The variable name in the row to retrieve
    /// * `column_names` -- The column names (header)
    ///
    /// # Examples
    ///
    /// ```rust
    /// answer_row.get(var_name, column_names)
    /// ```
    pub fn get(&self, var_name: &str, column_names: &[&str]) -> Option<&Concept> {
        column_names.iter().find_position(|name| **name == var_name)
            .map(|(index, _)| self.get_index(index))
            .flatten()
    }

    /// Produces an iterator over all concepts in this `AnswerRow`, skipping empty results
    ///
    /// # Examples
    ///
    /// ```rust
    /// answer_row.concepts()
    /// ```
    pub fn concepts(&self) -> impl Iterator<Item = &Concept> {
        self.concepts.iter().filter_map(|concept| concept.as_ref())
    }
}

//
// impl Index<String> for AnswerRow {
//     type Output = Concept;
//
//     fn index(&self, index: String) -> &Self::Output {
//         &self.map[&index]
//     }
// }
//
// impl IntoIterator for AnswerRow {
//     type Item = (String, Concept);
//     type IntoIter = hash_map::IntoIter<String, Concept>;
//
//     fn into_iter(self) -> Self::IntoIter {
//         self.map.into_iter()
//     }
// }
