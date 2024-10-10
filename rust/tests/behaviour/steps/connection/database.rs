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

use std::collections::HashSet;

use cucumber::{gherkin::Step, given, then, when};
use futures::{
    future::{join_all, try_join_all},
    stream, StreamExt, TryFutureExt,
};
use tokio::time::sleep;
use typedb_driver::Database;
use macro_rules_attribute::apply;

use crate::{assert_with_timeout, util, util::{create_database_with_timeout, iter_table}, Context, generic_step, params};

#[apply(generic_step)]
#[step(expr = "connection create database: {word}")]
pub async fn connection_create_database(context: &mut Context, name: String) {
    create_database_with_timeout(context.driver.as_ref().unwrap().databases(), name).await;
}

#[apply(generic_step)]
#[step(expr = "connection create database(s):")]
async fn connection_create_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step) {
        connection_create_database(context, name.into()).await;
    }
}

#[apply(generic_step)]
#[step(expr = "connection create databases in parallel:")]
async fn connection_create_databases_in_parallel(context: &mut Context, step: &Step) {
    join_all(iter_table(step).map(|name| create_database_with_timeout(context.driver.as_ref().unwrap().databases(), name.to_string())))
        .await;
}

#[apply(generic_step)]
#[step(expr = "connection delete database: {word}{may_error}")]
pub async fn connection_delete_database(context: &mut Context, name: String, may_error: params::MayError) {
    may_error.check(context.driver.as_ref().unwrap().databases().get(name).and_then(Database::delete).await);
}

#[apply(generic_step)]
#[step(expr = "connection delete database(s):")]
async fn connection_delete_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step) {
        context.driver.as_ref().unwrap().databases().get(name).and_then(Database::delete).await.unwrap();
    }
}

#[apply(generic_step)]
#[step(expr = "connection delete databases in parallel:")]
async fn connection_delete_databases_in_parallel(context: &mut Context, step: &Step) {
    try_join_all(iter_table(step).map(|name| context.driver.as_ref().unwrap().databases().get(name).and_then(Database::delete)))
        .await
        .unwrap();
}

#[apply(generic_step)]
#[step(expr = "connection has database: {word}")]
async fn connection_has_database(context: &mut Context, name: String) {
    assert_with_timeout!(
        context.driver.as_ref().unwrap().databases().contains(name.clone()).await.unwrap(),
        "Connection doesn't contain database {name}.",
    );
}

#[apply(generic_step)]
#[step(expr = "connection has database(s):")]
async fn connection_has_databases(context: &mut Context, step: &Step) {
    let names: HashSet<String> = iter_table(step).map(|name| name.to_owned()).collect();
    assert_with_timeout!(
        context.all_databases().await == names,
        "Connection doesn't contain at least one of the databases.",
    );
}

#[apply(generic_step)]
#[step(expr = "connection does not have database: {word}")]
async fn connection_does_not_have_database(context: &mut Context, name: String) {
    assert_with_timeout!(
        !context.driver.as_ref().unwrap().databases().contains(name.clone()).await.unwrap(),
        "Connection contains database {name}.",
    );
}

#[apply(generic_step)]
#[step(expr = "connection does not have database(s):")]
async fn connection_does_not_have_databases(context: &mut Context, step: &Step) {
    assert_with_timeout!(
        stream::iter(iter_table(step))
            .all(|name| async {
                !context.all_databases().await.contains(name)
            })
            .await,
        "Connection contains at least one of the databases.",
    )
}
