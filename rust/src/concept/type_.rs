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

use std::fmt;

use super::ValueType;

#[derive(Clone, Copy, Debug, PartialEq, Eq, Hash)]
pub enum Annotation {
    Key,
    Unique,
}

#[derive(Clone, Debug, PartialEq)]
pub enum ThingType {
    RootThingType(RootThingType),
    EntityType(EntityType),
    RelationType(RelationType),
    AttributeType(AttributeType),
}

impl ThingType {
    pub fn label(&self) -> &str {
        match self {
            Self::RootThingType(_) => RootThingType::LABEL,
            Self::EntityType(entity_type) => &entity_type.label,
            Self::RelationType(relation_type) => &relation_type.label,
            Self::AttributeType(attribute_type) => &attribute_type.label,
        }
    }
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RootThingType;

impl RootThingType {
    pub(crate) const LABEL: &'static str = "thing";
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct EntityType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RelationType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
}

#[derive(Clone, Debug, PartialEq)]
pub struct AttributeType {
    pub label: String,
    pub is_root: bool,
    pub is_abstract: bool,
    pub value_type: ValueType,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct RoleType {
    pub label: ScopedLabel,
    pub is_root: bool,
    pub is_abstract: bool,
}

#[derive(Clone, Debug, PartialEq, Eq)]
pub struct ScopedLabel {
    pub scope: String,
    pub name: String,
}

impl fmt::Display for ScopedLabel {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}:{}", self.scope, self.name)
    }
}
