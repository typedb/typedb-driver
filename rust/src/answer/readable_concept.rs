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

use std::{borrow::Cow, collections::HashMap};

use super::JSON;
use crate::concept::{Attribute, AttributeType, Concept, EntityType, RelationType, RoleType, RootThingType, Value};

#[derive(Clone, Debug, PartialEq)]
pub(crate) struct Tree {
    pub(crate) root: HashMap<String, Node>,
}

impl Tree {
    pub(crate) fn into_json(self) -> JSON {
        JSON::Object(self.root.into_iter().map(|(var, node)| (Cow::Owned(var), node.into_json())).collect())
    }
}

#[derive(Clone, Debug, PartialEq)]
pub(crate) enum Node {
    Map(HashMap<String, Node>),
    List(Vec<Node>),
    Leaf(Concept),
}

impl Node {
    pub(crate) fn into_json(self) -> JSON {
        match self {
            Node::Map(map) => {
                JSON::Object(map.into_iter().map(|(var, node)| (Cow::Owned(var), node.into_json())).collect())
            }
            Node::List(list) => JSON::List(list.into_iter().map(Node::into_json).collect()),
            Node::Leaf(Concept::RootThingType(_)) => {
                json_type(RootThingType::LABEL, Cow::Borrowed(RootThingType::LABEL))
            }
            Node::Leaf(Concept::EntityType(EntityType { label, .. })) => {
                json_type(EntityType::ROOT_LABEL, Cow::Owned(label))
            }
            Node::Leaf(Concept::RelationType(RelationType { label, .. })) => {
                json_type(RelationType::ROOT_LABEL, Cow::Owned(label))
            }
            Node::Leaf(Concept::AttributeType(AttributeType { label, .. })) => {
                json_type(AttributeType::ROOT_LABEL, Cow::Owned(label))
            }

            Node::Leaf(Concept::RoleType(RoleType { label, .. })) => {
                json_type("relation:role", Cow::Owned(label.to_string()))
            }

            Node::Leaf(Concept::Attribute(Attribute { type_: AttributeType { label, .. }, value, .. })) => {
                let JSON::Object(mut map) = json_value(value) else { unreachable!() };
                map.insert(Cow::Borrowed("type"), json_type(AttributeType::ROOT_LABEL, Cow::Owned(label)));
                JSON::Object(map)
            }

            Node::Leaf(Concept::Value(value)) => json_value(value),

            Node::Leaf(concept @ (Concept::Entity(_) | Concept::Relation(_))) => {
                unreachable!("Unexpected concept encountered in fetch response: {concept:?}")
            }
        }
    }
}

fn json_type(root: &'static str, label: Cow<'static, str>) -> JSON {
    const ROOT: Cow<'static, str> = Cow::Borrowed("root");
    const LABEL: Cow<'static, str> = Cow::Borrowed("label");
    JSON::Object([(ROOT, JSON::String(Cow::Borrowed(root))), (LABEL, JSON::String(label))].into())
}

fn json_value(value: Value) -> JSON {
    const VALUE_TYPE: Cow<'static, str> = Cow::Borrowed("value_type");
    const VALUE: Cow<'static, str> = Cow::Borrowed("value");

    const BOOLEAN: Cow<'static, str> = Cow::Borrowed("boolean");
    const DATETIME: Cow<'static, str> = Cow::Borrowed("datetime");
    const DOUBLE: Cow<'static, str> = Cow::Borrowed("double");
    const LONG: Cow<'static, str> = Cow::Borrowed("long");
    const STRING: Cow<'static, str> = Cow::Borrowed("string");

    match value {
        Value::Boolean(bool) => {
            JSON::Object([(VALUE, JSON::Boolean(bool)), (VALUE_TYPE, JSON::String(BOOLEAN))].into())
        }
        Value::Double(double) => {
            JSON::Object([(VALUE, JSON::Number(double)), (VALUE_TYPE, JSON::String(DOUBLE))].into())
        }
        Value::Long(long) => {
            JSON::Object([(VALUE, JSON::Number(long as f64)), (VALUE_TYPE, JSON::String(LONG))].into())
        }
        Value::String(string) => {
            JSON::Object([(VALUE, JSON::String(Cow::Owned(string))), (VALUE_TYPE, JSON::String(STRING))].into())
        }
        Value::DateTime(datetime) => JSON::Object(
            [(VALUE, JSON::String(Cow::Owned(datetime.to_string()))), (VALUE_TYPE, JSON::String(DATETIME))].into(),
        ),
    }
}
