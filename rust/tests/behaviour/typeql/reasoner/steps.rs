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

use cucumber::{gherkin::Step, given, then, when};
use typedb_client::TransactionType;

use crate::{
    behaviour::{
        connection::{
            database::steps::connection_create_database,
            session::steps::{
                connection_close_all_sessions, connection_open_data_session_for_database,
                connection_open_schema_session_for_database,
            },
            transaction::steps::{session_opens_transaction_of_type, transaction_commits},
        },
        parameter::TransactionTypeParam,
        typeql::language::steps::{answer_size, get_answers_typeql_match, typeql_define, typeql_insert},
        Context,
    },
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "reasoning schema")]
    async fn reasoning_schema(context: &mut Context, step: &Step) {
        if !context.databases.contains(Context::DEFAULT_DATABASE).await.unwrap() {
            connection_create_database(context, Context::DEFAULT_DATABASE.to_string()).await;
        }
        connection_open_schema_session_for_database(context, Context::DEFAULT_DATABASE.to_string()).await;
        session_opens_transaction_of_type(context, TransactionTypeParam { transaction_type: TransactionType::Write })
            .await;
        assert!(typeql_define(context, step).await.is_ok());
        transaction_commits(context).await;
        connection_close_all_sessions(context).await;
    }

    #[step(expr = "reasoning data")]
    async fn reasoning_data(context: &mut Context, step: &Step) {
        connection_open_data_session_for_database(context, Context::DEFAULT_DATABASE.to_string()).await;
        session_opens_transaction_of_type(context, TransactionTypeParam { transaction_type: TransactionType::Write })
            .await;
        assert!(typeql_insert(context, step).await.is_ok());
        transaction_commits(context).await;
        connection_close_all_sessions(context).await;
    }

    #[step(expr = "reasoning query")]
    async fn reasoning_query(context: &mut Context, step: &Step) {
        connection_open_data_session_for_database(context, Context::DEFAULT_DATABASE.to_string()).await;
        session_opens_transaction_of_type(context, TransactionTypeParam { transaction_type: TransactionType::Read })
            .await;
        assert!(get_answers_typeql_match(context, step).await.is_ok());
        connection_close_all_sessions(context).await;
    }

    #[step(expr = "verify answer size is: {int}")]
    async fn verify_answer_size(context: &mut Context, expected_answers: usize) {
        answer_size(context, expected_answers).await;
    }

    #[step(expr = "verify answer set is equivalent for query")]
    async fn verify_answer_set_is_equivalent_for_query(context: &mut Context, step: &Step) {
        let prev_answer = context.answer.clone();
        reasoning_query(context, step).await;
        let total_rows = context.answer.len();
        let mut matched_rows = 0;
        for row_curr in &context.answer {
            for row_prev in &prev_answer {
                if row_curr == row_prev {
                    matched_rows += 1;
                    break;
                }
            }
        }
        assert_eq!(matched_rows, total_rows, "There are only {matched_rows} matched entries of given {total_rows}.");
    }

    #[step(expr = "verifier is initialised")]
    #[step(expr = "verify answers are sound")]
    #[step(expr = "verify answers are complete")]
    async fn do_nothing(_context: &mut Context) {
        //     We don't have a verifier
    }

    #[step(expr = "verify answers are consistent across {int} executions")]
    async fn verify_answers_are_consistent_across_executions(_context: &mut Context, _executions: usize) {
        //     We can't execute previous query again because don't remember the query
    }
}
