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
    borrow::Cow,
    collections::HashMap,
    fmt::{self, Write},
};

use crate::concept::{Attribute, AttributeType, Concept, EntityType, RelationType, RoleType, RootThingType, Value};

#[derive(Clone, Debug)]
pub enum JSON {
    Object(HashMap<Cow<'static, str>, JSON>),
    List(Vec<JSON>),
    String(Cow<'static, str>),
    Number(f64),
    Boolean(bool),
}

impl JSON {
    fn type_(root: &'static str, label: Cow<'static, str>) -> Self {
        const ROOT: Cow<'static, str> = Cow::Borrowed("root");
        const LABEL: Cow<'static, str> = Cow::Borrowed("label");
        Self::Object([(ROOT, Self::String(Cow::Borrowed(root))), (LABEL, Self::String(label))].into())
    }

    fn value(value: Value) -> Self {
        const VALUE_TYPE: Cow<'static, str> = Cow::Borrowed("value_type");
        const VALUE: Cow<'static, str> = Cow::Borrowed("value");

        const BOOLEAN: Cow<'static, str> = Cow::Borrowed("boolean");
        const DATETIME: Cow<'static, str> = Cow::Borrowed("datetime");
        const DOUBLE: Cow<'static, str> = Cow::Borrowed("double");
        const LONG: Cow<'static, str> = Cow::Borrowed("long");
        const STRING: Cow<'static, str> = Cow::Borrowed("string");

        match value {
            Value::Boolean(bool) => {
                Self::Object([(VALUE, Self::Boolean(bool)), (VALUE_TYPE, Self::String(BOOLEAN))].into())
            }
            Value::Double(double) => {
                Self::Object([(VALUE, Self::Number(double)), (VALUE_TYPE, Self::String(DOUBLE))].into())
            }
            Value::Long(long) => {
                Self::Object([(VALUE, Self::Number(long as f64)), (VALUE_TYPE, Self::String(LONG))].into())
            }
            Value::String(string) => {
                Self::Object([(VALUE, Self::String(Cow::Owned(string))), (VALUE_TYPE, Self::String(STRING))].into())
            }
            Value::DateTime(datetime) => Self::Object(
                [(VALUE, Self::String(Cow::Owned(datetime.to_string()))), (VALUE_TYPE, Self::String(DATETIME))].into(),
            ),
        }
    }
}

impl fmt::Display for JSON {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            JSON::Object(object) => {
                f.write_char('{')?;
                for (i, (k, v)) in object.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, r#""{}": {}"#, k, v)?;
                }
                f.write_char('}')?;
            }
            JSON::List(list) => {
                f.write_char('[')?;
                for (i, v) in list.iter().enumerate() {
                    if i > 0 {
                        f.write_str(", ")?;
                    }
                    write!(f, "{}", v)?;
                }
                f.write_char(']')?;
            }
            JSON::String(string) => write!(f, r#""{string}""#)?,
            JSON::Number(number) => write!(f, "{number}")?,
            JSON::Boolean(boolean) => write!(f, "{boolean}")?,
        }
        Ok(())
    }
}

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
                JSON::type_(RootThingType::LABEL, Cow::Borrowed(RootThingType::LABEL))
            }
            Node::Leaf(Concept::EntityType(EntityType { label, .. })) => {
                JSON::type_(EntityType::ROOT_LABEL, Cow::Owned(label))
            }
            Node::Leaf(Concept::RelationType(RelationType { label, .. })) => {
                JSON::type_(RelationType::ROOT_LABEL, Cow::Owned(label))
            }
            Node::Leaf(Concept::AttributeType(AttributeType { label, .. })) => {
                JSON::type_(AttributeType::ROOT_LABEL, Cow::Owned(label))
            }

            Node::Leaf(Concept::RoleType(RoleType { label, .. })) => {
                JSON::type_("relation:role", Cow::Owned(label.to_string()))
            }

            Node::Leaf(Concept::Attribute(Attribute { type_: AttributeType { label, .. }, value, .. })) => {
                let JSON::Object(mut map) = JSON::value(value) else { unreachable!() };
                map.insert(Cow::Borrowed("type"), JSON::type_(AttributeType::ROOT_LABEL, Cow::Owned(label)));
                JSON::Object(map)
            }

            Node::Leaf(Concept::Value(value)) => JSON::value(value),

            Node::Leaf(concept @ (Concept::Entity(_) | Concept::Relation(_))) => {
                unreachable!("Unexpected concept encountered in fetch response: {concept:?}")
            }
        }
    }
}
