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
use futures::{future::try_join_all, TryFutureExt};
use typedb_client::{Session, SessionType};

use crate::{
    behaviour::{util, Context},
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "connection open schema session for database: {word}")]
    pub async fn connection_open_schema_session_for_database(context: &mut Context, name: String) {
        context
            .session_trackers
            .push(Session::new(context.databases.get(name).await.unwrap(), SessionType::Schema).await.unwrap().into());
    }

    #[step(expr = "connection open (data )session for database: {word}")]
    pub async fn connection_open_data_session_for_database(context: &mut Context, name: String) {
        context
            .session_trackers
            .push(Session::new(context.databases.get(name).await.unwrap(), SessionType::Data).await.unwrap().into());
    }

    #[step(expr = "connection open schema session(s) for database(s):")]
    async fn connection_open_schema_sessions_for_databases(context: &mut Context, step: &Step) {
        for name in util::iter_table(step) {
            context.session_trackers.push(
                Session::new(context.databases.get(name).await.unwrap(), SessionType::Schema).await.unwrap().into(),
            );
        }
    }

    #[step(expr = "connection open (data )session(s) for database(s):")]
    async fn connection_open_data_sessions_for_databases(context: &mut Context, step: &Step) {
        for name in util::iter_table(step) {
            context.session_trackers.push(
                Session::new(context.databases.get(name).await.unwrap(), SessionType::Data).await.unwrap().into(),
            );
        }
    }

    #[step(expr = "connection open (data )sessions in parallel for databases:")]
    async fn connection_open_data_sessions_in_parallel_for_databases(context: &mut Context, step: &Step) {
        let new_sessions = try_join_all(
            util::iter_table(step)
                .map(|name| context.databases.get(name).and_then(|db| Session::new(db, SessionType::Data))),
        )
        .await
        .unwrap();
        context.session_trackers.extend(new_sessions.into_iter().map(|session| session.into()));
    }

    #[step("connection close all sessions")]
    pub async fn connection_close_all_sessions(context: &mut Context) {
        context.session_trackers.clear();
    }

    #[step(expr = "session(s) is/are null: {word}")]
    #[step(expr = "sessions in parallel are null: {word}")]
    async fn sessions_are_null(_context: &mut Context, is_null: bool) {
        assert!(!is_null); // Rust sessions are not nullable
    }

    #[step(expr = "session(s) is/are open: {word}")]
    #[step(expr = "sessions in parallel are open: {word}")]
    async fn sessions_are_open(context: &mut Context, is_open: bool) {
        for session_tracker in &context.session_trackers {
            assert_eq!(session_tracker.session().is_open(), is_open);
        }
    }

    #[step(expr = "session(s) has/have database: {word}")]
    async fn sessions_have_database(context: &mut Context, name: String) {
        assert_eq!(context.session_trackers.get(0).unwrap().session().database_name(), name)
    }

    #[step(expr = "session(s) has/have database(s):")]
    #[step(expr = "sessions in parallel have databases:")]
    async fn sessions_have_databases(context: &mut Context, step: &Step) {
        assert_eq!(step.table.as_ref().unwrap().rows.len(), context.session_trackers.len());
        for (name, session_tracker) in util::iter_table(step).zip(&context.session_trackers) {
            assert_eq!(name, session_tracker.session().database_name());
        }
    }

    #[step(expr = "set session option {word} to: {word}")]
    async fn set_session_option_to(_context: &mut Context, _option: String, _value: String) {
        todo!()
        // if (!optionSetters.containsKey(option)) {
        //     throw new RuntimeException("Unrecognised option: " + option);
        // }
        // optionSetters.get(option).accept(sessionOptions, value);
    }
}
