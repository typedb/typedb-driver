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

use typeql::pattern::{Conjunction, ThingStatement};

/// Rules are a part of schema and define embedded logic.
/// The reasoning engine uses rules as a set of logic to infer new data.
/// A rule consists of a condition and a conclusion, and is uniquely identified by a label.
#[derive(Clone, Debug, PartialEq)]
pub struct Rule {
    /// The unique label of the rule.
    pub label: String,
    /// The statements that constitute the ‘when’ of the rule.
    pub when: Conjunction,
    /// The single statement that constitutes the ‘then’ of the rule.
    pub then: ThingStatement,
}

impl Rule {
    pub(crate) fn new(label: String, when: Conjunction, then: ThingStatement) -> Self {
        Self { label, when, then }
    }
}
