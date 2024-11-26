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

use std::{borrow::Cow, collections::HashMap, sync::Arc};

use super::{QueryType, JSON};
use crate::concept::{
    value::Struct, Attribute, AttributeType, Concept, EntityType, Kind, RelationType, RoleType, Value, ValueType,
};

#[derive(Debug, PartialEq)]
pub struct ConceptDocumentHeader {
    pub query_type: QueryType,
}

/// A single document of concepts representing substitutions for variables in the query.
/// Contains a Header (query type), and the document of concepts.
#[derive(Debug, Clone, PartialEq)]
pub struct ConceptDocument {
    header: Arc<ConceptDocumentHeader>,
    pub root: Option<Node>,
}

impl ConceptDocument {
    pub fn new(header: Arc<ConceptDocumentHeader>, root: Option<Node>) -> Self {
        Self { header, root }
    }

    pub fn into_json(self) -> JSON {
        match self.root {
            None => JSON::Null,
            Some(root_node) => root_node.into_json(),
        }
    }

    /// Retrieve the executed query's type (shared by all elements in this stream).
    ///
    /// # Examples
    ///
    /// ```rust
    /// concept_document.get_query_type()
    /// ```
    pub fn get_query_type(&self) -> QueryType {
        self.header.query_type
    }
}

#[derive(Clone, Debug, PartialEq)]
pub enum Node {
    Map(HashMap<String, Node>),
    List(Vec<Node>),
    Leaf(Option<Leaf>),
}

impl Node {
    pub(crate) fn into_json(self) -> JSON {
        match self {
            Node::Map(map) => {
                JSON::Object(map.into_iter().map(|(var, node)| (Cow::Owned(var), node.into_json())).collect())
            }
            Node::List(list) => JSON::Array(list.into_iter().map(Node::into_json).collect()),
            Node::Leaf(Some(leaf)) => leaf.into_json(),
            Node::Leaf(None) => JSON::Null,
        }
    }
}

#[derive(Clone, Debug, PartialEq)]
pub enum Leaf {
    Empty,
    Concept(Concept),
    ValueType(ValueType),
    Kind(Kind),
}

impl Leaf {
    fn into_json(self) -> JSON {
        match self {
            Self::Empty => JSON::Null,
            Self::Concept(Concept::EntityType(EntityType { label, .. })) => json_type(Kind::Entity, Cow::Owned(label)),
            Self::Concept(Concept::RelationType(RelationType { label, .. })) => {
                json_type(Kind::Relation, Cow::Owned(label))
            }
            Self::Concept(Concept::AttributeType(AttributeType { label, value_type, .. })) => {
                json_attribute_type(Cow::Owned(label), value_type)
            }
            Self::Concept(Concept::RoleType(RoleType { label, .. })) => {
                json_type(Kind::Role, Cow::Owned(label.to_string()))
            }
            Self::Concept(Concept::Attribute(Attribute { value, .. })) => json_value(value),
            Self::Concept(Concept::Value(value)) => json_value(value),
            Self::Concept(concept @ (Concept::Entity(_) | Concept::Relation(_))) => {
                unreachable!("Unexpected concept encountered in fetch response: {:?}", concept)
            }
            Self::ValueType(value_type) => json_value_type(Some(value_type)),
            Self::Kind(kind) => json_kind(kind),
        }
    }
}

const KIND: Cow<'static, str> = Cow::Borrowed("kind");
const LABEL: Cow<'static, str> = Cow::Borrowed("label");
const VALUE_TYPE: Cow<'static, str> = Cow::Borrowed("value_type");

fn json_type(kind: Kind, label: Cow<'static, str>) -> JSON {
    JSON::Object([(KIND, json_kind(kind)), (LABEL, JSON::String(label))].into())
}

fn json_attribute_type(label: Cow<'static, str>, value_type: Option<ValueType>) -> JSON {
    JSON::Object(
        [
            (KIND, JSON::String(Cow::Borrowed(Kind::Attribute.name()))),
            (LABEL, JSON::String(label)),
            (VALUE_TYPE, json_value_type(value_type)),
        ]
        .into(),
    )
}

fn json_value_type(value_type: Option<ValueType>) -> JSON {
    const NONE: Cow<'static, str> = Cow::Borrowed(ValueType::NONE_STR);
    const BOOLEAN: Cow<'static, str> = Cow::Borrowed(ValueType::BOOLEAN_STR);
    const LONG: Cow<'static, str> = Cow::Borrowed(ValueType::LONG_STR);
    const DOUBLE: Cow<'static, str> = Cow::Borrowed(ValueType::DOUBLE_STR);
    const DECIMAL: Cow<'static, str> = Cow::Borrowed(ValueType::DECIMAL_STR);
    const STRING: Cow<'static, str> = Cow::Borrowed(ValueType::STRING_STR);
    const DATE: Cow<'static, str> = Cow::Borrowed(ValueType::DATE_STR);
    const DATETIME: Cow<'static, str> = Cow::Borrowed(ValueType::DATETIME_STR);
    const DATETIME_TZ: Cow<'static, str> = Cow::Borrowed(ValueType::DATETIME_TZ_STR);
    const DURATION: Cow<'static, str> = Cow::Borrowed(ValueType::DURATION_STR);

    JSON::String(match value_type {
        None => NONE,
        Some(ValueType::Boolean) => BOOLEAN,
        Some(ValueType::Long) => LONG,
        Some(ValueType::Double) => DOUBLE,
        Some(ValueType::Decimal) => DECIMAL,
        Some(ValueType::String) => STRING,
        Some(ValueType::Date) => DATE,
        Some(ValueType::Datetime) => DATETIME,
        Some(ValueType::DatetimeTZ) => DATETIME_TZ,
        Some(ValueType::Duration) => DURATION,
        Some(ValueType::Struct(name)) => Cow::Owned(name),
    })
}

fn json_value(value: Value) -> JSON {
    match value {
        Value::Boolean(bool) => JSON::Boolean(bool),
        Value::Long(long) => JSON::Number(long as f64),
        Value::Double(double) => JSON::Number(double),
        Value::String(string) => JSON::String(Cow::Owned(string)),

        Value::Decimal(_) | Value::Date(_) | Value::Datetime(_) | Value::DatetimeTZ(_) | Value::Duration(_) => {
            JSON::String(Cow::Owned(value.to_string()))
        }

        Value::Struct(struct_, struct_name) => {
            JSON::Object(HashMap::from([(Cow::Owned(struct_name), json_struct(struct_))]))
        }
    }
}

fn json_struct(struct_: Struct) -> JSON {
    let mut json_object = HashMap::new();

    for (key, value_option) in struct_.fields {
        let json_value = match value_option {
            Some(value) => json_value(value),
            None => JSON::Null,
        };
        json_object.insert(Cow::Owned(key), json_value);
    }

    JSON::Object(json_object)
}

fn json_kind(kind: Kind) -> JSON {
    JSON::String(Cow::Borrowed(kind.name()))
}
