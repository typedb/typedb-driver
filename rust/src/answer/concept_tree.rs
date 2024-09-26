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

use std::{borrow::Cow, collections::HashMap};

use super::JSON;
use crate::concept::{Attribute, AttributeType, Concept, EntityType, RelationType, RoleType, Value, ValueType};

#[derive(Debug)]
pub struct ConceptTreesHeader {}

#[derive(Clone, Debug, PartialEq)]
pub struct ConceptTree {
    pub(crate) root: HashMap<String, Node>,
}

impl ConceptTree {
    pub(crate) fn into_json(self) -> JSON {
        JSON::Object(self.root.into_iter().map(|(var, node)| (Cow::Owned(var), node.into_json())).collect())
    }
}

#[derive(Clone, Debug, PartialEq)]
pub(crate) enum Node {
    Map(HashMap<String, Node>),
    List(Vec<Node>),
    Leaf(Option<Concept>),
}

impl Node {
    pub(crate) fn into_json(self) -> JSON {
        match self {
            Node::Map(map) => {
                JSON::Object(map.into_iter().map(|(var, node)| (Cow::Owned(var), node.into_json())).collect())
            }
            Node::List(list) => JSON::Array(list.into_iter().map(Node::into_json).collect()),
            Node::Leaf(Some(Concept::EntityType(EntityType { label, .. }))) => {
                todo!()
                // json_type(, Cow::Owned(label))
            }
            Node::Leaf(Some(Concept::RelationType(RelationType { label, .. }))) => {
                todo!()
                // json_type(RelationType::ROOT_LABEL, Cow::Owned(label))
            }
            Node::Leaf(Some(Concept::AttributeType(AttributeType { label, value_type, .. }))) => {
                json_attribute_type(Cow::Owned(label), value_type)
            }
            Node::Leaf(Some(Concept::RoleType(RoleType { label, .. }))) => {
                json_type("relation:role", Cow::Owned(label.to_string()))
            }
            Node::Leaf(Some(Concept::Attribute(Attribute { value, type_, .. }))) => JSON::Object(
                todo!(), // [(TYPE, json_attribute_type(Cow::Owned(label), value_type)), (VALUE, json_value(value))].into(),
            ),
            Node::Leaf(Some(Concept::Value(value))) => {
                JSON::Object([(VALUE_TYPE, json_value_type(value.get_type())), (VALUE, json_value(value))].into())
            }
            Node::Leaf(None) => JSON::Null,
            Node::Leaf(Some(concept @ (Concept::Entity(_) | Concept::Relation(_)))) => {
                unreachable!("Unexpected concept encountered in fetch response: {concept:?}")
            }
        }
    }
}

const TYPE: Cow<'static, str> = Cow::Borrowed("type");
const ROOT: Cow<'static, str> = Cow::Borrowed("root");
const LABEL: Cow<'static, str> = Cow::Borrowed("label");

const VALUE_TYPE: Cow<'static, str> = Cow::Borrowed("value_type");
const VALUE: Cow<'static, str> = Cow::Borrowed("value");

fn json_type(root: &'static str, label: Cow<'static, str>) -> JSON {
    todo!()
    // JSON::Object([(ROOT, JSON::String(Cow::Borrowed(root))), (LABEL, JSON::String(label))].into())
}

fn json_attribute_type(label: Cow<'static, str>, value_type: Option<ValueType>) -> JSON {
    todo!()
    // JSON::Object(
    //     [
    //         (ROOT, JSON::String(Cow::Borrowed(AttributeType::ROOT_LABEL))),
    //         (LABEL, JSON::String(label)),
    //         (VALUE_TYPE, json_value_type(value_type)),
    //     ]
    //     .into(),
    // )
}

fn json_value_type(value_type: ValueType) -> JSON {
    // const BOOLEAN: Cow<'static, str> = Cow::Borrowed("boolean");
    // const DATETIME: Cow<'static, str> = Cow::Borrowed("datetime");
    // const DOUBLE: Cow<'static, str> = Cow::Borrowed("double");
    // const LONG: Cow<'static, str> = Cow::Borrowed("long");
    // const STRING: Cow<'static, str> = Cow::Borrowed("string");
    //
    // match value_type {
    //     ValueType::Boolean => JSON::String(BOOLEAN),
    //     ValueType::Double => JSON::String(DOUBLE),
    //     ValueType::Long => JSON::String(LONG),
    //     ValueType::String => JSON::String(STRING),
    //     ValueType::DateTime => JSON::String(DATETIME),
    // }
    todo!()
}

fn json_value(value: Value) -> JSON {
    // match value {
    //     Value::Boolean(bool) => JSON::Boolean(bool),
    //     Value::Double(double) => JSON::Number(double),
    //     Value::Long(long) => JSON::Number(long as f64),
    //     Value::String(string) => JSON::String(Cow::Owned(string)),
    //     Value::DateTime(datetime) => JSON::String(Cow::Owned(datetime.format("%FT%T%.3f").to_string())),
    // }
    todo!()
}
