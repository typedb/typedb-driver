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
    behaviour::{parameter::TransactionTypeParam, util::iter_table, Context},
    generic_step_impl,
};

generic_step_impl! {
    // =============================================//
    // sequential sessions, sequential transactions //
    // =============================================//

    #[step(expr = "(for each )session(,) open(s) transaction(s) of type: {transaction_type}")]
    pub async fn session_opens_transaction_of_type(context: &mut Context, type_: TransactionTypeParam) {
        for session_tracker in &mut context.session_trackers {
            session_tracker.open_transaction(type_.transaction_type).await.unwrap();
        }
    }

    #[step(expr = "(for each )session(,) open transaction(s) of type:")]
    async fn for_each_session_open_transactions_of_type(context: &mut Context, step: &Step) {
        for type_ in iter_table(step) {
            let transaction_type = type_.parse::<TransactionTypeParam>().unwrap().transaction_type;
            for session_tracker in &mut context.session_trackers {
                session_tracker.open_transaction(transaction_type).await.unwrap();
            }
        }
    }

    #[step(expr = "(for each )session(,) open transaction(s) of type; throws exception: {transaction_type}")]
    async fn for_each_session_open_transactions_of_type_throws_exception(
        context: &mut Context,
        type_: TransactionTypeParam,
    ) {
        for session_tracker in &mut context.session_trackers {
            assert!(session_tracker.open_transaction(type_.transaction_type).await.is_err());
        }
    }

    #[step(expr = "(for each )session(,) open transaction(s) of type; throws exception")]
    async fn for_each_session_open_transactions_of_type_throws_exception_table(context: &mut Context, step: &Step) {
        for type_ in iter_table(step) {
            let transaction_type = type_.parse::<TransactionTypeParam>().unwrap().transaction_type;
            for session_tracker in &mut context.session_trackers {
                assert!(session_tracker.open_transaction(transaction_type).await.is_err());
            }
        }
    }

    #[step(expr = "(for each )session(,) transaction(s) is/are null: {word}")]
    #[step(expr = "for each session, transactions in parallel are null: {word}")]
    #[step(expr = "for each session in parallel, transactions in parallel are null: {word}")]
    async fn for_each_session_transactions_are_null(_context: &mut Context, is_null: bool) {
        assert!(!is_null); // Rust transactions are not nullable
    }

    #[step(expr = "(for each )session(,) transaction(s) is/are open: {word}")]
    #[step(expr = "for each session, transactions in parallel are open: {word}")]
    #[step(expr = "for each session in parallel, transactions in parallel are open: {word}")]
    async fn for_each_session_transactions_are_open(context: &mut Context, is_open: bool) {
        for session_tracker in &context.session_trackers {
            for transaction in session_tracker.transactions() {
                assert_eq!(transaction.is_open(), is_open);
            }
        }
    }

    #[step(expr = "transaction commits")]
    pub async fn transaction_commits(context: &mut Context) {
        context.take_transaction().commit().await.unwrap();
    }

    #[step(expr = "transaction commits; throws exception")]
    async fn transaction_commits_throws(context: &mut Context) {
        assert!(context.take_transaction().commit().await.is_err());
    }

    #[step(expr = "transaction commits; throws exception containing {string}")]
    async fn transaction_commits_throws_exception(context: &mut Context, exception: String) {
        let res = context.take_transaction().commit().await;
        assert!(res.is_err());
        assert!(res.unwrap_err().to_string().contains(&exception));
    }

    #[step(expr = "(for each )session(,) transaction(s) commit(s)")]
    async fn for_each_session_transactions_commit(context: &mut Context) {
        for session_tracker in &mut context.session_trackers {
            for transaction in session_tracker.transactions_mut().drain(..) {
                transaction.commit().await.unwrap();
            }
        }
    }

    #[step(expr = "(for each )session(,) transaction(s) commit(s); throws exception")]
    async fn for_each_session_transactions_commits_throws_exception(context: &mut Context) {
        for session_tracker in &mut context.session_trackers {
            for transaction in session_tracker.transactions_mut().drain(..) {
                assert!(transaction.commit().await.is_err());
            }
        }
    }

    #[step(expr = "(for each )session(,) transaction close(s)")]
    async fn for_each_session_transaction_closes(context: &mut Context) {
        for session_tracker in &mut context.session_trackers {
            session_tracker.transactions_mut().clear();
        }
    }

    #[step(expr = "(for each )session(,) transaction(s) has/have type: {transaction_type}")]
    async fn for_each_session_transactions_have_type(context: &mut Context, type_: TransactionTypeParam) {
        let transaction_type: TransactionType = type_.transaction_type;
        for session_tracker in &context.session_trackers {
            assert_eq!(session_tracker.transactions().len(), 1);
            assert_eq!(transaction_type, session_tracker.transaction().type_());
        }
    }

    #[step(expr = "(for each )session(,) transaction(s) has/have type:")]
    #[step(expr = "for each session, transactions in parallel have type:")]
    async fn for_each_session_transactions_have_types(context: &mut Context, step: &Step) {
        let types: Vec<TransactionType> =
            iter_table(step).map(|s| s.parse::<TransactionTypeParam>().unwrap().transaction_type).collect();
        for session_tracker in &context.session_trackers {
            assert_eq!(types.len(), session_tracker.transactions().len());
            for (type_, transaction) in types.iter().zip(session_tracker.transactions()) {
                assert_eq!(*type_, transaction.type_());
            }
        }
    }

    // ===========================================//
    // sequential sessions, parallel transactions //
    // ===========================================//

    #[step(expr = "for each session, open transaction(s) in parallel of type:")]
    async fn for_each_session_open_transactions_in_parallel_of_type(context: &mut Context, step: &Step) {
        for type_ in iter_table(step) {
            let transaction_type = type_.parse::<TransactionTypeParam>().unwrap().transaction_type;
            for session_tracker in &mut context.session_trackers {
                // FIXME parallel
                session_tracker.open_transaction(transaction_type).await.unwrap();
            }
        }
    }
}
