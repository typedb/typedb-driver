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

use cucumber::gherkin::Step;
use futures::{future::join_all, StreamExt, TryStreamExt};
use itertools::Itertools;
use macro_rules_attribute::apply;
use typedb_driver::{
    answer::{ConceptRow, QueryAnswer},
    concept::{AttributeType, Concept, ConceptCategory, EntityType, RelationType, Value, ValueType},
    resolve, Result as TypeDBResult, Transaction,
};

use crate::{
    generic_step, params,
    params::check_boolean,
    util::{iter_table, list_contains_json, parse_json},
    BehaviourTestOptionalError,
    BehaviourTestOptionalError::InvalidValueCasting,
    Context,
};

async fn run_query(transaction: &Transaction, query: impl AsRef<str>) -> TypeDBResult<QueryAnswer> {
    resolve!(transaction.query(query))
}

fn get_collected_column_names(concept_row: &ConceptRow) -> Vec<String> {
    concept_row.get_column_names().to_vec()
}

async fn get_answer_rows_var(
    context: &mut Context,
    index: usize,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
) -> Result<&Concept, BehaviourTestOptionalError> {
    let concept_row = context.get_collected_answer_row_index(index).await;
    match is_by_var_index {
        params::IsByVarIndex::Is => {
            let collected_column_names = get_collected_column_names(concept_row);
            let position = collected_column_names.iter().find_position(|name| name == &&var.name).map(|(pos, _)| pos);
            match position {
                None => None,
                Some(position) => concept_row.get_index(position),
            }
        }
        params::IsByVarIndex::IsNot => concept_row.get(&var.name),
    }
    .ok_or(BehaviourTestOptionalError::VariableDoesNotExist(var.name))
}

fn check_concept_is_type(concept: &Concept, is_type: params::Boolean) {
    let concept_category = concept.get_category();
    let actual_is_type = concept_category == ConceptCategory::EntityType
        || concept_category == ConceptCategory::RelationType
        || concept_category == ConceptCategory::AttributeType
        || concept_category == ConceptCategory::RoleType;
    check_boolean!(is_type, actual_is_type);
    check_boolean!(is_type, concept.is_type());
}

fn check_concept_is_instance(concept: &Concept, is_instance: params::Boolean) {
    let concept_category = concept.get_category();
    let actual_is_instance = concept_category == ConceptCategory::Entity
        || concept_category == ConceptCategory::Relation
        || concept_category == ConceptCategory::Attribute;
    check_boolean!(is_instance, actual_is_instance);
    check_boolean!(is_instance, concept.is_instance());
}

fn check_concept_is_entity_type(concept: &Concept, is_entity_type: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_entity_type, concept_category == ConceptCategory::EntityType);
    check_boolean!(is_entity_type, concept.is_entity_type());
}

fn check_concept_is_relation_type(concept: &Concept, is_relation_type: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_relation_type, concept_category == ConceptCategory::RelationType);
    check_boolean!(is_relation_type, concept.is_relation_type());
}

fn check_concept_is_role_type(concept: &Concept, is_role_type: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_role_type, concept_category == ConceptCategory::RoleType);
    check_boolean!(is_role_type, concept.is_role_type());
}

fn check_concept_is_attribute_type(concept: &Concept, is_attribute_type: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_attribute_type, concept_category == ConceptCategory::AttributeType);
    check_boolean!(is_attribute_type, concept.is_attribute_type());
}

fn check_concept_is_entity(concept: &Concept, is_entity: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_entity, concept_category == ConceptCategory::Entity);
    check_boolean!(is_entity, concept.is_entity());
}

fn check_concept_is_relation(concept: &Concept, is_relation: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_relation, concept_category == ConceptCategory::Relation);
    check_boolean!(is_relation, concept.is_relation());
}

fn check_concept_is_attribute(concept: &Concept, is_attribute: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_attribute, concept_category == ConceptCategory::Attribute);
    check_boolean!(is_attribute, concept.is_attribute());
}

fn check_concept_is_value(concept: &Concept, is_value: params::Boolean) {
    let concept_category = concept.get_category();
    check_boolean!(is_value, concept_category == ConceptCategory::Value);
    check_boolean!(is_value, concept.is_value());
}

fn check_concept_is_kind(concept: &Concept, concept_kind: params::ConceptKind, is_kind: params::Boolean) {
    match concept_kind {
        params::ConceptKind::Concept => (),
        params::ConceptKind::Type => check_concept_is_type(concept, is_kind),
        params::ConceptKind::Instance => check_concept_is_instance(concept, is_kind),
        params::ConceptKind::EntityType => check_concept_is_entity_type(concept, is_kind),
        params::ConceptKind::RelationType => check_concept_is_relation_type(concept, is_kind),
        params::ConceptKind::AttributeType => check_concept_is_attribute_type(concept, is_kind),
        params::ConceptKind::RoleType => check_concept_is_role_type(concept, is_kind),
        params::ConceptKind::Entity => check_concept_is_entity(concept, is_kind),
        params::ConceptKind::Relation => check_concept_is_relation(concept, is_kind),
        params::ConceptKind::Attribute => check_concept_is_attribute(concept, is_kind),
        params::ConceptKind::Value => check_concept_is_value(concept, is_kind),
    }
}

fn concept_get_type(concept: &Concept) -> Concept {
    match concept {
        Concept::Entity(entity) => {
            let type_: &EntityType = entity.type_().unwrap();
            Concept::EntityType(type_.clone())
        }
        Concept::Relation(relation) => {
            let type_: &RelationType = relation.type_().unwrap();
            Concept::RelationType(type_.clone())
        }
        Concept::Attribute(attribute) => {
            let type_: &AttributeType = attribute.type_().unwrap();
            Concept::AttributeType(type_.clone())
        }
        _ => panic!("Only instances can have types"),
    }
}

#[apply(generic_step)]
#[step(expr = "typeql schema query{may_error}")]
#[step(expr = "typeql write query{may_error}")]
#[step(expr = "typeql read query{may_error}")]
pub async fn typeql_query(context: &mut Context, may_error: params::MayError, step: &Step) {
    context.cleanup_answers().await;
    may_error.check(run_query(context.transaction(), step.docstring().unwrap()).await);
}

#[apply(generic_step)]
#[step(expr = "get answers of typeql schema query")]
#[step(expr = "get answers of typeql write query")]
#[step(expr = "get answers of typeql read query")]
pub async fn get_answers_of_typeql_query(context: &mut Context, step: &Step) {
    context.cleanup_answers().await;
    context.set_answer(run_query(context.transaction(), step.docstring().unwrap()).await).unwrap();
}

#[apply(generic_step)]
#[step(expr = r"concurrently get answers of typeql schema query {int} times")]
#[step(expr = r"concurrently get answers of typeql write query {int} times")]
#[step(expr = r"concurrently get answers of typeql read query {int} times")]
pub async fn concurrently_get_answers_of_typeql_query_times(context: &mut Context, count: usize, step: &Step) {
    context.cleanup_concurrent_answers().await;

    let queries = vec![step.docstring().unwrap(); count];
    let answers: Vec<QueryAnswer> = join_all(queries.into_iter().map(|query| run_query(context.transaction(), query)))
        .await
        .into_iter()
        .map(|result| result.unwrap())
        .collect();

    context.set_concurrent_answers(answers);
}

#[apply(generic_step)]
#[step(expr = "answer type {is_or_not}: {query_answer_type}")]
pub async fn answer_type_is(
    context: &mut Context,
    is_or_not: params::IsOrNot,
    query_answer_type: params::QueryAnswerType,
) {
    match query_answer_type {
        params::QueryAnswerType::Ok => is_or_not.check(context.answer.as_ref().unwrap().is_ok()),
        params::QueryAnswerType::ConceptRows => is_or_not.check(context.answer.as_ref().unwrap().is_row_stream()),
        params::QueryAnswerType::ConceptDocuments => {
            is_or_not.check(context.answer.as_ref().unwrap().is_document_stream())
        }
    }
}

#[apply(generic_step)]
#[step(expr = "answer unwraps as {query_answer_type}{may_error}")]
pub async fn answer_unwraps_as(
    context: &mut Context,
    query_answer_type: params::QueryAnswerType,
    may_error: params::MayError,
) {
    let expect = !may_error.expects_error();
    match context.answer.as_ref().unwrap() {
        QueryAnswer::Ok(_) => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::Ok),
                "Expected {expect} {query_answer_type}"
            )
        }
        QueryAnswer::ConceptRowStream(_, _) => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::ConceptRows),
                "Expected {expect} {query_answer_type}"
            )
        }
        QueryAnswer::ConceptDocumentStream(_, _) => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::ConceptDocuments),
                "Expected {expect} {query_answer_type}"
            )
        }
    }
}

#[apply(generic_step)]
#[step(expr = r"answer size is: {int}")]
pub async fn answer_size_is(context: &mut Context, size: usize) {
    let actual_size = match context.try_get_collected_rows().await {
        None => match context.try_get_collected_documents().await {
            None => panic!("No collected answer to check its size"),
            Some(documents) => documents.len(),
        },
        Some(rows) => rows.len(),
    };
    assert_eq!(actual_size, size, "Expected {size} answers, got {actual_size}");
}

#[apply(generic_step)]
#[step(expr = "concurrently process {int} row(s) from answers{may_error}")]
pub async fn concurrently_process_rows_from_answers(context: &mut Context, count: usize, may_error: params::MayError) {
    let expects_error = may_error.expects_error();
    let streams = context.get_concurrent_rows_streams().await;

    let mut jobs = Vec::new();

    for stream in streams.iter_mut() {
        let job = async {
            let mut failed = false;
            let mut rows = Vec::new();

            for _ in 0..count {
                if let Some(row) = stream.next() {
                    rows.push(row.unwrap());
                } else {
                    failed = true;
                    break;
                }
            }

            assert_eq!(expects_error, failed, "Expected to fail? {expects_error}, but did it? {failed}");
        };

        jobs.push(job);
    }

    join_all(jobs).await;
}

#[apply(generic_step)]
#[step(expr = r"answer column names are:")]
pub async fn answer_column_names_are(context: &mut Context, step: &Step) {
    let actual_column_names: Vec<String> =
        get_collected_column_names(context.get_collected_answer_row_index(0).await).into_iter().sorted().collect();
    let expected_column_names: Vec<String> = iter_table(step).sorted().map(|s| s.to_string()).collect();
    assert_eq!(actual_column_names, expected_column_names);
}

#[apply(generic_step)]
#[step(expr = r"answer query type {is_or_not}: {query_type}")]
pub async fn answer_query_type_is(context: &mut Context, is_or_not: params::IsOrNot, query_type: params::QueryType) {
    let real_query_type = context.get_answer_query_type().await.unwrap();
    is_or_not.compare(real_query_type, query_type.query_type);
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) query type {is_or_not}: {query_type}")]
pub async fn answer_get_row_query_type_is(
    context: &mut Context,
    index: usize,
    is_or_not: params::IsOrNot,
    query_type: params::QueryType,
) {
    let real_query_type = context.get_collected_answer_row_index(index).await.get_query_type();
    is_or_not.compare(real_query_type, query_type.query_type);
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get variable{is_by_var_index}\({var}\){may_error}")]
pub async fn answer_get_row_get_variable(
    context: &mut Context,
    index: usize,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    may_error: params::MayError,
) {
    may_error.check(get_answer_rows_var(context, index, is_by_var_index, var).await);
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get variable{is_by_var_index}\({var}\) as {concept_kind}{may_error}")]
pub async fn answer_get_row_get_variable_as(
    context: &mut Context,
    index: usize,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    kind: params::ConceptKind,
    may_error: params::MayError,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    may_error.check({
        kind.matches_concept(concept).then_some(()).ok_or(BehaviourTestOptionalError::InvalidConceptConversion)
    });
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is {concept_kind}: {boolean}")]
pub async fn answer_get_row_get_variable_is_kind(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    checked_kind: params::ConceptKind,
    is_kind: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    // Can be sometimes redundant (e.g. get type(p) is type: true), but is a way to emulate unwrapping
    // in other languages!
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_concept_is_kind(concept, checked_kind, is_kind);
}

#[apply(generic_step)]
#[step(
    expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get type is {concept_kind}: {boolean}"
)]
pub async fn answer_get_row_get_variable_get_type_is_kind(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    checked_kind: params::ConceptKind,
    is_kind: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    let type_ = concept_get_type(concept);
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_concept_is_kind(&type_, checked_kind, is_kind);
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get label: {word}")]
pub async fn answer_get_row_get_variable_get_label(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    label: String,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    assert_eq!(label.as_str(), concept.get_label());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get type get label: {word}")]
pub async fn answer_get_row_get_variable_get_type_get_label(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    label: String,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    let type_ = concept_get_type(concept);
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    assert_eq!(label.as_str(), type_.get_label());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) {contains_or_doesnt} iid")]
pub async fn answer_get_row_get_variable_get_iid_exists(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    contains_or_doesnt: params::ContainsOrDoesnt,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    contains_or_doesnt.check(&concept.get_iid(), &format!("iid for concept {}", concept.get_label()));
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get value type: {word}")]
pub async fn answer_get_row_get_variable_get_value_type(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    value_type: String,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    assert_eq!(value_type.as_str(), concept.get_value_label().unwrap_or("none"));
    assert_eq!(value_type, concept.get_value_type().map(|type_| type_.name().to_owned()).unwrap_or("none".to_owned()));
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get type get value type: {word}")]
pub async fn answer_get_row_get_variable_get_type_get_value_type(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    value_type: String,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    let type_ = concept_get_type(concept);
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    assert_eq!(value_type.as_str(), type_.get_value_label().unwrap_or("none"));
    assert_eq!(value_type, type_.get_value_type().map(|type_| type_.name().to_owned()).unwrap_or("none".to_owned()));
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get value {is_or_not}: {value}")]
pub async fn answer_get_row_get_variable_get_value(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_or_not: params::IsOrNot,
    value: params::Value,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    let actual_value = concept.get_value().expect("Value is expected");
    let value_type = actual_value.get_type();
    let expected_value = value.into_typedb(value_type.clone());
    match value_type {
        ValueType::Struct(_) => {
            // Compare string representations
            match expected_value {
                Value::String(expected_struct) => is_or_not.compare(expected_struct, actual_value.to_string()),
                _ => panic!("Structs are expected to be parsed to Strings!"),
            }
        }
        _ => is_or_not.compare(&expected_value, actual_value),
    }
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) as {value_type}{may_error}")]
pub async fn answer_get_row_get_variable_as_value_type(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    value_type: params::ValueType,
    may_error: params::MayError,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    let type_name = value_type.value_type.name().to_string();
    may_error.check(match value_type.value_type {
        ValueType::Boolean => concept.get_boolean().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Long => concept.get_long().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Double => concept.get_double().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Decimal => concept.get_decimal().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::String => concept.get_string().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Date => concept.get_date().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Datetime => concept.get_datetime().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::DatetimeTZ => concept.get_datetime_tz().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Duration => concept.get_duration().map(|_| ()).ok_or(InvalidValueCasting(type_name)),
        ValueType::Struct(_) => concept.get_struct().map(|_| ()).ok_or(InvalidValueCasting("struct".to_owned())),
    });
}

#[apply(generic_step)]
#[step(
    expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) as {value_type} {is_or_not}: {value}"
)]
pub async fn answer_get_row_get_variable_get_specific_value(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    value_type: params::ValueType,
    is_or_not: params::IsOrNot,
    value: params::Value,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    let _actual_value = concept.get_value().expect("Value is expected");
    let expected_value = value.into_typedb(value_type.value_type.clone());
    match value_type.value_type {
        ValueType::Boolean => {
            let actual_boolean = concept.get_boolean().unwrap();
            match expected_value {
                Value::Boolean(expected_boolean) => is_or_not.compare(actual_boolean, expected_boolean),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Long => {
            let actual_long = concept.get_long().unwrap();
            match expected_value {
                Value::Long(expected_long) => is_or_not.compare(actual_long, expected_long),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Double => {
            let actual_double = concept.get_double().unwrap();
            match expected_value {
                Value::Double(expected_double) => is_or_not.compare(actual_double, expected_double),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Decimal => {
            let actual_decimal = concept.get_decimal().unwrap();
            match expected_value {
                Value::Decimal(expected_decimal) => is_or_not.compare(actual_decimal, expected_decimal),
                _ => is_or_not.check(false),
            }
        }
        ValueType::String => {
            let actual_string = concept.get_string().unwrap();
            match expected_value {
                Value::String(expected_string) => is_or_not.compare(actual_string, expected_string.as_str()),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Date => {
            let actual_date = concept.get_date().unwrap();
            match expected_value {
                Value::Date(expected_date) => is_or_not.compare(actual_date, expected_date),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Datetime => {
            let actual_datetime = concept.get_datetime().unwrap();
            match expected_value {
                Value::Datetime(expected_datetime) => is_or_not.compare(actual_datetime, expected_datetime),
                _ => is_or_not.check(false),
            }
        }
        ValueType::DatetimeTZ => {
            let actual_datetime_tz = concept.get_datetime_tz().unwrap();
            match expected_value {
                Value::DatetimeTZ(expected_datetime_tz) => is_or_not.compare(actual_datetime_tz, expected_datetime_tz),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Duration => {
            let actual_duration = concept.get_duration().unwrap();
            match expected_value {
                Value::Duration(expected_duration) => is_or_not.compare(actual_duration, expected_duration),
                _ => is_or_not.check(false),
            }
        }
        ValueType::Struct(_) => {
            let actual_struct = concept.get_struct().unwrap();
            // Compare string representations
            match expected_value {
                Value::String(expected_struct) => is_or_not.compare(expected_struct, actual_struct.to_string()),
                _ => is_or_not.check(false),
            }
        }
    }
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is untyped: {boolean}")]
pub async fn answer_get_row_get_variable_is_untyped(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_untyped: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_untyped, concept.get_value_type().is_none());
    check_boolean!(is_untyped, concept.get_value_label().is_none());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is boolean: {boolean}")]
pub async fn answer_get_row_get_variable_is_boolean(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_boolean: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_boolean, concept.is_boolean());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is long: {boolean}")]
pub async fn answer_get_row_get_variable_is_long(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_long: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_long, concept.is_long());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is decimal: {boolean}")]
pub async fn answer_get_row_get_variable_is_decimal(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_decimal: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_decimal, concept.is_decimal());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is double: {boolean}")]
pub async fn answer_get_row_get_variable_is_double(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_double: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_double, concept.is_double());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is string: {boolean}")]
pub async fn answer_get_row_get_variable_is_string(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_string: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_string, concept.is_string());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is date: {boolean}")]
pub async fn answer_get_row_get_variable_is_date(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_date: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_date, concept.is_date());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is datetime: {boolean}")]
pub async fn answer_get_row_get_variable_is_datetime(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_datetime: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_datetime, concept.is_datetime());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is datetime-tz: {boolean}")]
pub async fn answer_get_row_get_variable_is_datetime_tz(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_datetime_tz: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_datetime_tz, concept.is_datetime_tz());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is duration: {boolean}")]
pub async fn answer_get_row_get_variable_is_duration(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_duration: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_duration, concept.is_duration());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is struct: {boolean}")]
pub async fn answer_get_row_get_variable_is_struct(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    is_struct: params::Boolean,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    check_boolean!(is_struct, concept.is_struct());
}

#[apply(generic_step)]
#[step(expr = r"answer get row\({int}\) get concepts size is: {int}")]
pub async fn answer_get_row_get_concepts_size_is(context: &mut Context, index: usize, size: usize) {
    let concept_row = context.get_collected_answer_row_index(index).await;
    assert_eq!(size, concept_row.get_concepts().collect_vec().len());
}

#[apply(generic_step)]
#[step(expr = r"answer {contains_or_doesnt} document:")]
pub async fn answer_contains_document(
    context: &mut Context,
    contains_or_doesnt: params::ContainsOrDoesnt,
    step: &Step,
) {
    let expected_document = parse_json(step.docstring().unwrap()).expect("Given docstring is not a JSON document!");
    let concept_documents =
        context.get_collected_documents().await.clone().into_iter().map(|document| document.into_json()).collect_vec();
    contains_or_doesnt.check_bool(
        list_contains_json(&concept_documents, &expected_document),
        &format!("Concept documents: {:?}", concept_documents),
    );
}
