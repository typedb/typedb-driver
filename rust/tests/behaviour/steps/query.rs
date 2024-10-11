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

use cucumber::{gherkin::Step, given, then, when};
use futures::TryStreamExt;
use typedb_driver::{answer::JSON, concept::Value, Result as TypeDBResult, Transaction};

use macro_rules_attribute::apply;
use typedb_driver::answer::QueryAnswer;
use crate::{
    assert_err,
    util, Context,
    generic_step,
    params,
};

async fn run_query(transaction: &Transaction, query: impl AsRef<str>) -> TypeDBResult<QueryAnswer> {
    transaction.query(query).await
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
pub async fn answer_type_is(context: &mut Context, is_or_not: params::IsOrNot, query_answer_type: params::QueryAnswerType) {
    match query_answer_type {
        params::QueryAnswerType::Ok => assert!(context.answer.as_ref().unwrap().is_ok()),
        params::QueryAnswerType::ConceptRows => assert!(context.answer.as_ref().unwrap().is_rows_stream()),
        params::QueryAnswerType::ConceptTrees => assert!(context.answer.as_ref().unwrap().is_json_stream()),
    }
}



// #[apply(generic_step)]
// #[step(expr = "typeql define; throws exception")]
// async fn typeql_define_throws(context: &mut Context, step: &Step) {
//     assert_err!(typeql_define(context, step).await);
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql define; throws exception containing {string}")]
// async fn typeql_define_throws_containing(context: &mut Context, step: &Step, exception: String) {
//     assert!(typeql_define(context, step).await.unwrap_err().to_string().contains(&exception));
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql undefine")]
// async fn typeql_undefine(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     context.transaction().query().undefine(&parsed.to_string()).await
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql undefine; throws exception")]
// async fn typeql_undefine_throws(context: &mut Context, step: &Step) {
//     assert_err!(typeql_undefine(context, step).await);
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql insert")]
// pub async fn typeql_insert(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     context.transaction().query().insert(&parsed.to_string())?.try_collect::<Vec<_>>().await?;
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql insert; throws exception")]
// async fn typeql_insert_throws(context: &mut Context, step: &Step) {
//     assert_err!(typeql_insert(context, step).await);
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql insert; throws exception containing {string}")]
// async fn typeql_insert_throws_containing(context: &mut Context, step: &Step, exception: String) {
//     assert!(typeql_insert(context, step).await.unwrap_err().to_string().contains(&exception));
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql delete")]
// async fn typeql_delete(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     context.transaction().query().delete(&parsed.to_string()).await
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql delete; throws exception")]
// async fn typeql_delete_throws(context: &mut Context, step: &Step) {
//     assert_err!(typeql_delete(context, step).await);
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql delete; throws exception containing {string}")]
// async fn typeql_delete_throws_containing(context: &mut Context, step: &Step, exception: String) {
//     assert!(typeql_delete(context, step).await.unwrap_err().to_string().contains(&exception));
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql update")]
// async fn typeql_update(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     context.transaction().query().update(&parsed.to_string())?.try_collect::<Vec<_>>().await?;
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql update; throws exception")]
// async fn typeql_update_throws(context: &mut Context, step: &Step) {
//     assert_err!(typeql_update(context, step).await);
// }
//
// #[apply(generic_step)]
// #[step(expr = "get answers of typeql get")]
// pub async fn get_answers_typeql_get(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     let answer = context.transaction().query().get(&parsed.to_string())?.try_collect::<Vec<_>>().await?;
//     context.answer = answer;
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "get answers of typeql insert")]
// async fn get_answers_typeql_insert(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     let answer = context.transaction().query().insert(&parsed.to_string())?.try_collect::<Vec<_>>().await?;
//     context.answer = answer;
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "answer size is: {int}")]
// pub async fn answer_size(context: &mut Context, expected_answers: usize) {
//     let actual_answers = context.answer.len();
//     assert_eq!(
//         actual_answers, expected_answers,
//         "The number of identifier entries (rows) should match the number of answers, \
//         but found {expected_answers} identifier entries and {actual_answers} answers."
//     );
// }
//
// #[apply(generic_step)]
// #[step(expr = "uniquely identify answer concepts")]
// async fn uniquely_identify_answer_concepts(context: &mut Context, step: &Step) {
//     let step_table = iter_table_map(step).collect::<Vec<_>>();
//     let expected_answers = step_table.len();
//     let actual_answers = context.answer.len();
//     assert_eq!(
//         actual_answers, expected_answers,
//         "The number of identifier entries (rows) should match the number of answers, \
//         but found {expected_answers} identifier entries and {actual_answers} answers."
//     );
//     let mut matched_rows = 0;
//     for ans_row in &context.answer {
//         for table_row in &step_table {
//             if match_answer_concept_map(context, table_row, ans_row).await {
//                 matched_rows += 1;
//                 break;
//             }
//         }
//     }
//     assert_eq!(
//         matched_rows, actual_answers,
//         "An identifier entry (row) should match 1-to-1 to an answer, but there are only {matched_rows} \
//         matched entries of given {actual_answers}."
//     );
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql get; throws exception")]
// async fn typeql_get_throws(context: &mut Context, step: &Step) {
//     let parsed = parse_query(step.docstring().unwrap());
//     match parsed {
//         Ok(_) => {
//             let answers = context.transaction().query().get(&parsed.unwrap().to_string());
//             if answers.is_ok() {
//                 let res = answers.unwrap().try_collect::<Vec<_>>().await;
//                 assert!(res.is_err());
//             }
//         }
//         // NOTE: We manually close transaction here, because we want to align with all non-rust and non-java drivers,
//         // where parsing happens at server-side which closes transaction if they fail
//         Err(_) => {
//             for session_tracker in &mut context.session_trackers {
//                 session_tracker.transactions_mut().clear();
//             }
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql get; throws exception containing {string}")]
// async fn typeql_get_throws_containing(context: &mut Context, step: &Step, message: String) {
//     let parsed = parse_query(step.docstring().unwrap());
//     match parsed {
//         Ok(_) => {
//             let answers = context.transaction().query().get(&parsed.unwrap().to_string());
//             if answers.is_ok() {
//                 let res = answers.unwrap().try_collect::<Vec<_>>().await;
//                 assert!(res.is_err());
//                 assert!(res.unwrap_err().to_string().contains(&message));
//             }
//         }
//         // NOTE: We manually close transaction here, because we want to align with all non-rust and non-java drivers,
//         // where parsing happens at server-side which closes transaction if they fail
//         Err(_) => {
//             for session_tracker in &mut context.session_trackers {
//                 session_tracker.transactions_mut().clear();
//             }
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "each answer satisfies")]
// async fn each_answer_satisfies(context: &mut Context, step: &Step) -> TypeDBResult {
//     for answer in context.answer.clone() {
//         let answer = match_templated_answer(context, step, &answer).await?;
//         assert_eq!(answer.len(), 1);
//     }
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "get answers of templated typeql get")]
// async fn get_answers_of_templated_typeql_get(context: &mut Context, step: &Step) {
//     if context.answer.len() > 1 {
//         panic!("Can only retrieve answers of templated typeql get given 1 previous answer.")
//     }
//     let answer = context.answer.get(0).unwrap();
//     context.answer = match_templated_answer(context, step, &answer).await.unwrap();
// }
//
// #[apply(generic_step)]
// #[step(expr = "templated typeql get; throws exception")]
// async fn templated_typeql_get_throws(context: &mut Context, step: &Step) {
//     for answer in &context.answer {
//         assert_err!(match_templated_answer(context, step, answer).await);
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "order of answer concepts is")]
// async fn order_of_answer_concept(context: &mut Context, step: &Step) {
//     let step_table = iter_table_map(step).collect::<Vec<_>>();
//     let expected_answers = step_table.len();
//     let actual_answers = context.answer.len();
//     assert_eq!(
//         actual_answers, expected_answers,
//         "The number of identifier entries (rows) should match the number of answers, \
//         but found {expected_answers} identifier entries and {actual_answers} answers."
//     );
//     for i in 0..expected_answers {
//         let ans_row = &context.answer.get(i).unwrap();
//         let table_row = &step_table.get(i).unwrap();
//         assert!(
//             match_answer_concept_map(context, table_row, ans_row).await,
//             "The answer at index {i} does not match the identifier entry (row) at index {i}."
//         );
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "get answer of typeql get aggregate")]
// async fn get_answers_typeql_get_aggregate(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     context.value_answer = Some(context.transaction().query().get_aggregate(&parsed.to_string()).await?);
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql get aggregate; throws exception")]
// async fn typeql_get_aggregate_throws(context: &mut Context, step: &Step) {
//     assert_err!(get_answers_typeql_get_aggregate(context, step).await);
// }
//
// #[step(expr = "get answers of typeql fetch")]
// pub async fn get_answers_typeql_fetch(context: &mut Context, step: &Step) -> TypeDBResult {
//     let parsed = parse_query(step.docstring().unwrap())?;
//     let fetch_answer = Some(JSON::Array(context.transaction().query().fetch(&parsed.to_string())?.try_collect().await?));
//     context.fetch_answer = fetch_answer;
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "typeql fetch; throws exception")]
// async fn typeql_fetch_throws(context: &mut Context, step: &Step) {
//     let parsed = parse_query(step.docstring().unwrap());
//     match parsed {
//         Ok(_) => {
//             let answers = context.transaction().query().fetch(&parsed.unwrap().to_string());
//             if answers.is_ok() {
//                 let res = answers.unwrap().try_collect::<Vec<_>>().await;
//                 assert!(res.is_err());
//             }
//         }
//         // NOTE: We manually close transaction here, because we want to align with all non-rust and non-java drivers,
//         // where parsing happens at server-side which closes transaction if they fail
//         Err(_) => {
//             for session_tracker in &mut context.session_trackers {
//                 session_tracker.transactions_mut().clear();
//             }
//         }
//     }
// }
//
// #[apply(generic_step)]
// #[step(expr = "fetch answers are")]
// async fn fetch_answers_are(context: &mut Context, step: &Step) -> TypeDBResult {
//     let expected = step.docstring().unwrap();
//     let actual = context.fetch_answer.as_ref().expect("trying to assert on fetch answers without performing fetch first!");
//     assert!(json_matches_str(expected, actual)?, "expected: {}\nactual: {}", expected, actual);
//     Ok(())
// }
//
// #[apply(generic_step)]
// #[step(expr = "rules contain: {label}")]
// async fn rules_contain(context: &mut Context, rule_label: LabelParam) {
//     let res = context.transaction().logic().get_rule(rule_label.name).await;
//     assert!(res.is_ok(), "{res:?}");
//     assert!(res.as_ref().unwrap().is_some(), "{res:?}");
// }
//
// #[apply(generic_step)]
// #[step(expr = "rules do not contain: {label}")]
// async fn rules_do_not_contain(context: &mut Context, rule_label: LabelParam) {
//     let res = context.transaction().logic().get_rule(rule_label.name).await;
//     assert_eq!(res, Ok(None), "{res:?}");
// }
//
// #[apply(generic_step)]
// #[step(expr = "rules are")]
// async fn rules_are(context: &mut Context, step: &Step) -> TypeDBResult {
//     let stream = context.transaction().logic().get_rules();
//     assert!(stream.is_ok(), "{:?}", stream.err());
//     let res = stream.unwrap().try_collect::<Vec<_>>().await;
//     assert!(res.is_ok(), "{:?}", res.err());
//     let answers = res.unwrap();
//     let step_table = iter_table_map(step).collect::<Vec<_>>();
//     let expected_answers = step_table.len();
//     let actual_answers = answers.len();
//     assert_eq!(
//         actual_answers, expected_answers,
//         "The number of identifier entries (rows) should match the number of answers, \
//         but found {expected_answers} identifier entries and {actual_answers} answers."
//     );
//     let mut matched_rows = 0;
//     for ans_row in &answers {
//         for table_row in &step_table {
//             if match_answer_rule(table_row, ans_row).await? {
//                 matched_rows += 1;
//                 break;
//             }
//         }
//     }
//     assert_eq!(
//         matched_rows, actual_answers,
//         "An identifier entry (row) should match 1-to-1 to an answer, but there are only {matched_rows} \
//         matched entries of given {actual_answers}."
//     );
//     Ok(())
// }
