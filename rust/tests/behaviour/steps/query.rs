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

use std::ops::Index;

use cucumber::{gherkin::Step, given, then, when};
use futures::TryStreamExt;
use itertools::Itertools;
use macro_rules_attribute::apply;
use typedb_driver::{
    answer::{ConceptRow, QueryAnswer, JSON},
    concept::{AttributeType, Concept, ConceptCategory, EntityType, RelationType, Value, ValueType},
    Result as TypeDBResult, Transaction,
};

use crate::{
    assert_err, generic_step, params,
    params::{check_boolean, ConceptKind, IsOrNot},
    util,
    util::iter_table,
    BehaviourTestOptionalError, Context,
};

async fn run_query(transaction: &Transaction, query: impl AsRef<str>) -> TypeDBResult<QueryAnswer> {
    transaction.query(query).await
}

fn get_collected_column_names(concept_row: &ConceptRow) -> Vec<String> {
    concept_row.get_column_names().into_iter().cloned().collect()
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
#[step(expr = "answer type {is_or_not}: {query_answer_type}")]
pub async fn answer_type_is(
    context: &mut Context,
    is_or_not: params::IsOrNot,
    query_answer_type: params::QueryAnswerType,
) {
    match query_answer_type {
        params::QueryAnswerType::Ok => is_or_not.check(context.answer.as_ref().unwrap().is_ok()),
        params::QueryAnswerType::ConceptRows => is_or_not.check(context.answer.as_ref().unwrap().is_rows_stream()),
        params::QueryAnswerType::ConceptTrees => is_or_not.check(context.answer.as_ref().unwrap().is_json_stream()),
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
        QueryAnswer::Ok() => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::Ok),
                "Expected {expect} {query_answer_type}"
            )
        }
        QueryAnswer::ConceptRowsStream(_) => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::ConceptRows),
                "Expected {expect} {query_answer_type}"
            )
        }
        QueryAnswer::ConceptTreesStream(_, _) => {
            assert_eq!(
                expect,
                matches!(query_answer_type, params::QueryAnswerType::ConceptTrees),
                "Expected {expect} {query_answer_type}"
            )
        }
    }
}

#[apply(generic_step)]
#[step(expr = r"answer size is: {int}")]
pub async fn answer_size_is(context: &mut Context, size: usize) {
    let actual_size = match context.try_get_collected_rows().await {
        // When trees are implemented: match context.try_get_collected_trees() -> ...
        None => panic!("No collected answer to check its size"),
        Some(rows) => rows.len(),
    };
    assert_eq!(actual_size, size, "Expected {size} answers, got {actual_size}");
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
    let real_query_type = context.get_collected_rows().await.get(0).unwrap().get_query_type();
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
    may_error.check((|| {
        kind.matches_concept(concept).then(|| ()).ok_or(BehaviourTestOptionalError::InvalidConceptConversion)
    })());
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
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get iid {exists_or_doesnt}")]
pub async fn answer_get_row_get_variable_get_iid_exists(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
    exists_or_doesnt: params::ExistsOrDoesnt,
) {
    let concept = get_answer_rows_var(context, index, is_by_var_index, var).await.unwrap();
    check_concept_is_kind(concept, var_kind, params::Boolean::True);
    exists_or_doesnt.check(&concept.get_iid(), &format!("iid for concept {}", concept.get_label()));
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
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) get value: {value}")]
pub async fn answer_get_row_get_variable_get_value(
    context: &mut Context,
    index: usize,
    var_kind: params::ConceptKind,
    is_by_var_index: params::IsByVarIndex,
    var: params::Var,
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
                Value::String(expected_struct) => assert_eq!(expected_struct, actual_value.to_string()),
                _ => panic!("Structs are expected to be parsed to Strings!"),
            }
        }
        _ => assert_eq!(&expected_value, actual_value),
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
    check_boolean!(is_untyped, matches!(concept.get_value_type(), None));
    check_boolean!(is_untyped, matches!(concept.get_value_label(), None));
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
#[step(expr = r"answer get row\({int}\) get {concept_kind}{is_by_var_index}\({var}\) is timezoned-datetime: {boolean}")]
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
