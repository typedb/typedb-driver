/*
 * Copyright (C) 2022 Vaticle
 *
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

use crate::concept::Concept;

/// Contains a mapping of variables to concepts.
#[derive(Clone, Debug, PartialEq)]
pub struct ConceptMap {
    /// The `HashMap` where keys are query variables, and values are concepts.
    pub map: HashMap<String, Concept>,
    /// The `Explainables` object for this `ConceptMap`, that exposes which of the concepts
    /// in this `ConceptMap` are explainable.
    pub explainables: Explainables,
}

impl ConceptMap {
    /// Retrieves a concept for a given variable name.
    ///
    /// # Arguments
    ///
    /// * `var_name` -- The string representation of a variable
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_map.get(var_name)
    /// ```
    pub fn get(&self, var_name: &str) -> Option<&Concept> {
        self.map.get(var_name)
    }

    /// Produces an iterator over all concepts in this `ConceptMap`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_map.concepts()
    /// ```
    pub fn concepts(&self) -> impl Iterator<Item = &Concept> {
        self.map.values()
    }
}

impl From<ConceptMap> for HashMap<String, Concept> {
    fn from(cm: ConceptMap) -> Self {
        cm.map
    }
}

impl Index<String> for ConceptMap {
    type Output = Concept;

    fn index(&self, index: String) -> &Self::Output {
        &self.map[&index]
    }
}

impl IntoIterator for ConceptMap {
    type Item = (String, Concept);
    type IntoIter = hash_map::IntoIter<String, Concept>;

    fn into_iter(self) -> Self::IntoIter {
        self.map.into_iter()
    }
}

/// Contains explainable objects.
#[derive(Clone, Debug, Default, PartialEq)]
pub struct Explainables {
    /// Explainable relations
    pub relations: HashMap<String, Explainable>,
    /// Explainable attributes
    pub attributes: HashMap<String, Explainable>,
    /// Explainable pairs of (owner, attribute)
    pub ownerships: HashMap<(String, String), Explainable>,
}

impl Explainables {
    /// Checks if this `Explainables` does not contain any `Explainable` object.
    ///
    /// # Examples
    ///
    /// ```rust
    /// explainables.is_empty()
    /// ```
    pub fn is_empty(&self) -> bool {
        self.relations.is_empty() && self.attributes.is_empty() && self.ownerships.is_empty()
    }
}

/// Contains an explainable object.
#[derive(Clone, Debug, PartialEq)]
pub struct Explainable {
    /// The subquery of the original query that is actually being explained.
    pub conjunction: String,
    /// The unique ID that identifies this `Explainable`.
    pub id: i64,
}

impl Explainable {
    pub(crate) fn new(conjunction: String, id: i64) -> Self {
        Self { conjunction, id }
    }
}
