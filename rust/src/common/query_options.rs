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

/// TypeDB query options.
/// `QueryOptions` object can be used to override the default server behaviour for executed queries.
///
/// # Examples
///
/// ```rust
/// let options = QueryOptions::new().transaction_timeout(Duration::from_secs(60));
/// ```
#[derive(Clone, Copy, Debug, Default)]
pub struct QueryOptions {
    /// If set, specifies if types should be included in instance structs returned in ConceptRow answers.
    /// This option allows reducing the amount of unnecessary data transmitted.
    pub include_instance_types: Option<bool>,
}

impl QueryOptions {
    pub fn new() -> Self {
        Self::default()
    }

    /// If set, specifies if types should be included in instance structs returned in ConceptRow answers.
    /// This option allows reducing the amount of unnecessary data transmitted.
    pub fn include_instance_types(self, include_instance_types: bool) -> Self {
        Self { include_instance_types: Some(include_instance_types), ..self }
    }
}
