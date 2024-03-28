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

use chrono::NaiveDateTime;

#[derive(Clone, Debug, PartialEq)]
pub enum Value {
    Boolean(bool),
    Long(i64),
    Double(f64),
    String(String),
    DateTime(NaiveDateTime),
}

impl Value {
    /// Retrieves the `ValueType` of this value concept.
    ///
    /// # Examples
    ///
    /// ```rust
    /// value.get_value_type();
    /// ```
    pub fn get_type(&self) -> ValueType {
        match self {
            Self::Boolean(_) => ValueType::Boolean,
            Self::Long(_) => ValueType::Long,
            Self::Double(_) => ValueType::Double,
            Self::String(_) => ValueType::String,
            Self::DateTime(_) => ValueType::DateTime,
        }
    }
}

/// Represents the type of primitive value is held by a Value or Attribute.
#[repr(C)]
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum ValueType {
    Object,
    Boolean,
    Long,
    Double,
    String,
    DateTime,
}
