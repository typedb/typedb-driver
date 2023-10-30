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
    collections::{HashMap, HashSet},
};

mod steps;

use chrono::{NaiveDate, NaiveDateTime, NaiveTime};
use cucumber::gherkin::Step;
use futures::{
    stream::{self, StreamExt},
    TryFutureExt, TryStreamExt,
};
use regex::{Captures, Regex};
use tokio::time::sleep;
use typedb_driver::{
    answer::{ConceptMap, JSON},
    concept::{
        Annotation, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType, Value,
    },
    logic::Rule,
    transaction::concept::api::ThingAPI,
    DatabaseManager, Result as TypeDBResult,
};
use typeql::{parse_pattern, parse_query, parse_statement, pattern::Statement};

use crate::{assert_with_timeout, behaviour::Context};

pub fn iter_table(step: &Step) -> impl Iterator<Item = &str> {
    step.table().unwrap().rows.iter().flatten().map(String::as_str)
}

pub fn iter_table_map(step: &Step) -> impl Iterator<Item = HashMap<&str, &str>> {
    let (keys, rows) = step.table().unwrap().rows.split_first().unwrap();
    rows.iter().map(|row| keys.iter().zip(row).map(|(k, v)| (k.as_str(), v.as_str())).collect())
}

pub async fn match_answer_concept_map(
    context: &Context,
    answer_identifiers: &HashMap<&str, &str>,
    answer: &ConceptMap,
) -> bool {
    stream::iter(answer_identifiers.keys())
        .all(|key| async {
            answer.map.contains_key(*key)
                && match_answer_concept(context, answer_identifiers.get(key).unwrap(), answer.get(key).unwrap()).await
        })
        .await
}

pub async fn match_answer_concept(context: &Context, answer_identifier: &str, answer: &Concept) -> bool {
    let identifiers: Vec<&str> = answer_identifier.splitn(2, ':').collect();
    match identifiers[0] {
        "key" => key_values_equal(context, identifiers[1], answer).await,
        "label" => labels_equal(identifiers[1], answer),
        "value" => values_equal(identifiers[1], answer),
        "attr" => attribute_values_equal(identifiers[1], answer),
        _ => unreachable!(),
    }
}

async fn key_values_equal(context: &Context, expected_label_and_value: &str, answer: &Concept) -> bool {
    let identifiers: Vec<&str> = expected_label_and_value.splitn(2, ':').collect();
    assert_eq!(identifiers.len(), 2, "Unexpected table cell format: {expected_label_and_value}.");

    let res = match answer {
        Concept::Entity(entity) => {
            async { entity.get_has(context.transaction(), vec![], vec![Annotation::Key]) }
                .and_then(|stream| async { stream.try_collect::<Vec<_>>().await })
                .await
        }
        Concept::Relation(rel) => {
            async { rel.get_has(context.transaction(), vec![], vec![Annotation::Key]) }
                .and_then(|stream| async { stream.try_collect::<Vec<_>>().await })
                .await
        }
        Concept::Attribute(attr) => {
            async { attr.get_has(context.transaction(), vec![], vec![Annotation::Key]) }
                .and_then(|stream| async { stream.try_collect::<Vec<_>>().await })
                .await
        }
        _ => unreachable!("Unexpected Concept type: {answer:?}"),
    };
    match res {
        Ok(keys) => keys
            .into_iter()
            .find(|key| key.type_.label == identifiers[0])
            .map(|attr| value_equals_str(&attr.value, identifiers[1]))
            .unwrap_or(false),
        Err(_) => false,
    }
}

fn labels_equal(expected_label: &str, answer: &Concept) -> bool {
    match answer {
        Concept::RootThingType(_) => expected_label == "thing",
        Concept::EntityType(EntityType { label, .. }) => expected_label == label,
        Concept::RelationType(RelationType { label, .. }) => expected_label == label,
        Concept::RoleType(RoleType { label, .. }) => expected_label == label.to_string(),
        Concept::AttributeType(AttributeType { label, .. }) => expected_label == label,
        _ => unreachable!(),
    }
}

fn attribute_values_equal(expected_label_and_value: &str, answer: &Concept) -> bool {
    let identifiers: Vec<&str> = expected_label_and_value.splitn(2, ':').collect();
    assert_eq!(identifiers.len(), 2, "Unexpected table cell format: {expected_label_and_value}.");
    let Concept::Attribute(Attribute { value, .. }) = answer else { unreachable!() };
    value_equals_str(value, identifiers[1])
}

fn values_equal(expected_label_and_value: &str, answer: &Concept) -> bool {
    let identifiers: Vec<&str> = expected_label_and_value.splitn(2, ':').collect();
    assert_eq!(identifiers.len(), 2, "Unexpected table cell format: {expected_label_and_value}.");
    let Concept::Value(value) = answer else { unreachable!() };
    value_equals_str(value, identifiers[1].trim())
}

fn value_equals_str(value: &Value, expected: &str) -> bool {
    match value {
        Value::String(val) => val == expected,
        Value::Long(val) => expected.parse::<i64>().map(|expected| expected.eq(val)).unwrap_or(false),
        Value::Double(val) => {
            expected.parse::<f64>().map(|expected| equals_approximate(expected, *val)).unwrap_or(false)
        }
        Value::Boolean(val) => expected.parse::<bool>().map(|expected| expected.eq(val)).unwrap_or(false),
        Value::DateTime(val) => {
            if expected.contains(':') {
                val == &NaiveDateTime::parse_from_str(expected, "%Y-%m-%dT%H:%M:%S").unwrap()
            } else {
                let my_date = NaiveDate::parse_from_str(expected, "%Y-%m-%d").unwrap();
                let my_time = NaiveTime::from_hms_opt(0, 0, 0).unwrap();
                val == &NaiveDateTime::new(my_date, my_time)
            }
        }
    }
}

pub fn json_matches_str(string: &str, json: &JSON) -> TypeDBResult<bool> {
    parse_json(string).map(|rhs| jsons_equal_up_to_reorder(json, &rhs))
}

fn parse_json(json: &str) -> TypeDBResult<JSON> {
    fn serde_json_into_fetch_answer(json: serde_json::Value) -> JSON {
        match json {
            serde_json::Value::Null => unreachable!("nulls are not allowed in fetch results"),
            serde_json::Value::Bool(bool) => JSON::Boolean(bool),
            serde_json::Value::Number(number) => JSON::Number(number.as_f64().unwrap()),
            serde_json::Value::String(string) => JSON::String(Cow::Owned(string)),
            serde_json::Value::Array(array) => {
                JSON::List(array.into_iter().map(serde_json_into_fetch_answer).collect())
            }
            serde_json::Value::Object(object) => JSON::Object(
                object.into_iter().map(|(k, v)| (Cow::Owned(k), serde_json_into_fetch_answer(v))).collect(),
            ),
        }
    }

    serde_json::from_str(json)
        .map(serde_json_into_fetch_answer)
        .map_err(|e| format!("Could not parse expected fetch answer: {e:?}").into())
}

fn jsons_equal_up_to_reorder(lhs: &JSON, rhs: &JSON) -> bool {
    match (lhs, rhs) {
        (JSON::Object(lhs), JSON::Object(rhs)) => {
            if lhs.len() != rhs.len() {
                return false;
            }
            lhs.iter().all(|(key, lhs_value)| match rhs.get(key) {
                Some(rhs_value) => jsons_equal_up_to_reorder(lhs_value, rhs_value),
                None => false,
            })
        }
        (JSON::List(lhs), JSON::List(rhs)) => {
            if lhs.len() != rhs.len() {
                return false;
            }
            let mut rhs_matches = HashSet::new();
            for item in lhs {
                match rhs
                    .iter()
                    .enumerate()
                    .filter(|(i, _)| !rhs_matches.contains(i))
                    .find_map(|(i, rhs_item)| jsons_equal_up_to_reorder(item, rhs_item).then_some(i))
                {
                    Some(idx) => {
                        rhs_matches.insert(idx);
                    }
                    None => return false,
                }
            }
            true
        }
        (JSON::String(lhs), JSON::String(rhs)) => lhs == rhs,
        (&JSON::Number(lhs), &JSON::Number(rhs)) => equals_approximate(lhs, rhs),
        (JSON::Boolean(lhs), JSON::Boolean(rhs)) => lhs == rhs,
        _ => false,
    }
}

pub fn equals_approximate(first: f64, second: f64) -> bool {
    const EPS: f64 = 1e-4;
    (first - second).abs() < EPS
}

pub async fn match_templated_answer(
    context: &Context,
    step: &Step,
    answer: &ConceptMap,
) -> TypeDBResult<Vec<ConceptMap>> {
    let query = apply_query_template(step.docstring().unwrap(), answer);
    let parsed = parse_query(&query)?;
    context.transaction().query().get(&parsed.to_string())?.try_collect::<Vec<_>>().await
}

fn apply_query_template(query_template: &str, answer: &ConceptMap) -> String {
    let re = Regex::new(r"<answer\.(.+?)\.iid>").unwrap();
    re.replace_all(query_template, |caps: &Captures| get_iid(answer.map.get(&caps[1]).unwrap())).to_string()
}

fn get_iid(concept: &Concept) -> String {
    let iid = match concept {
        Concept::Entity(Entity { iid, .. }) => iid,
        Concept::Relation(Relation { iid, .. }) => iid,
        Concept::Attribute(Attribute { iid, .. }) => iid,
        _ => unreachable!("Unexpected Concept type: {concept:?}"),
    };
    iid.to_string()
}

pub async fn match_answer_rule(answer_identifiers: &HashMap<&str, &str>, answer: &Rule) -> TypeDBResult<bool> {
    let when = parse_pattern(answer_identifiers.get("when").unwrap())?.into_conjunction();
    let then = parse_statement(answer_identifiers.get("then").unwrap())?;
    Ok(*answer_identifiers.get("label").unwrap() == answer.label
        && when == answer.when
        && then == Statement::Thing(answer.then.clone()))
}

pub async fn create_database_with_timeout(databases: &DatabaseManager, name: String) {
    assert_with_timeout!(databases.create(name.clone()).await.is_ok(), "Database {name} couldn't be created.");
}

#[macro_export]
macro_rules! assert_with_timeout {
    ($expr:expr, $message:expr $(, $arg:expr)* $(,)?) => {{
        't: {
            for _ in 0..Context::STEP_REATTEMPT_LIMIT {
                if $expr {
                    break 't;
                }
                sleep(Context::STEP_REATTEMPT_SLEEP).await;
            }
            panic!($message $(, $arg)*);
        }
    }};
}
