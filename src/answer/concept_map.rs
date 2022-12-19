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

use crate::{common::Result, concept::Concept};

#[derive(Debug)]
pub struct ConceptMap {
    pub map: HashMap<String, Concept>,
}

impl ConceptMap {
    pub(crate) fn from_proto(proto: typedb_protocol::ConceptMap) -> Result<Self> {
        let mut map = HashMap::with_capacity(proto.map.len());
        for (k, v) in proto.map {
            map.insert(k, Concept::from_proto(v)?);
        }
        Ok(Self { map })
    }

    pub fn get(&self, var_name: &str) -> Option<&Concept> {
        self.map.get(var_name)
    }

    pub fn concepts(&self) -> impl Iterator<Item = &Concept> {
        self.map.values()
    }

    pub fn concepts_to_vec(&self) -> Vec<&Concept> {
        self.concepts().collect::<Vec<&Concept>>()
    }
}

impl Clone for ConceptMap {
    fn clone(&self) -> Self {
        let mut map = HashMap::with_capacity(self.map.len());
        for (k, v) in &self.map {
            map.insert(k.clone(), v.clone());
        }
        Self { map }
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
