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

use std::collections::HashSet;

use cucumber::{gherkin::Step, given, then, when};
use futures::{future::try_join_all, TryFutureExt};
use typedb_client::Database;

use crate::{
    behaviour::{util, Context},
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "connection create database: {word}")]
    async fn connection_create_database(context: &mut Context, name: String) {
        context.databases.create(name).await.unwrap();
    }

    #[step(expr = "connection create database(s):")]
    async fn connection_create_databases(context: &mut Context, step: &Step) {
        for name in util::iter_table(step) {
            context.databases.create(name).await.unwrap();
        }
    }

    #[step("connection create databases in parallel:")]
    async fn connection_create_databases_in_parallel(context: &mut Context, step: &Step) {
        try_join_all(util::iter_table(step).map(|name| context.databases.create(name))).await.unwrap();
    }

    #[step(expr = "connection delete database: {word}")]
    async fn connection_delete_database(context: &mut Context, name: String) {
        context.databases.get(name).and_then(Database::delete).await.unwrap();
    }

    #[step(expr = "connection delete database(s):")]
    async fn connection_delete_databases(context: &mut Context, step: &Step) {
        for name in util::iter_table(step) {
            context.databases.get(name).and_then(Database::delete).await.unwrap();
        }
    }

    #[step(expr = "connection delete databases in parallel:")]
    async fn connection_delete_databases_in_parallel(context: &mut Context, step: &Step) {
        try_join_all(util::iter_table(step).map(|name| context.databases.get(name).and_then(Database::delete)))
            .await
            .unwrap();
    }

    #[step(expr = "connection delete database; throws exception: {word}")]
    async fn connection_delete_database_throws_exception(context: &mut Context, name: String) {
        assert!(context.databases.get(name).and_then(Database::delete).await.is_err());
    }

    #[step(expr = "connection delete database(s); throws exception")]
    async fn connection_delete_databases_throws_exception(context: &mut Context, step: &Step) {
        for name in util::iter_table(step) {
            assert!(context.databases.get(name).and_then(Database::delete).await.is_err());
        }
    }

    #[step(expr = "connection has database: {word}")]
    async fn connection_has_database(context: &mut Context, name: String) {
        assert!(context.databases.contains(name).await.unwrap());
    }

    #[step(expr = "connection has database(s):")]
    async fn connection_has_databases(context: &mut Context, step: &Step) {
        let names: HashSet<String> = util::iter_table(step).map(|name| name.to_owned()).collect();
        let all_databases = context.databases.all().await.unwrap().into_iter().map(|db| db.name().to_owned()).collect();
        assert_eq!(names, all_databases);
    }

    #[step(expr = "connection does not have database: {word}")]
    async fn connection_does_not_have_database(context: &mut Context, name: String) {
        assert!(!context.databases.contains(name).await.unwrap());
    }

    #[step(expr = "connection does not have database(s):")]
    async fn connection_does_not_have_databases(context: &mut Context, step: &Step) {
        let all_databases: HashSet<String> =
            context.databases.all().await.unwrap().into_iter().map(|db| db.name().to_owned()).collect();
        for name in util::iter_table(step) {
            assert!(!all_databases.contains(name));
        }
    }
}
