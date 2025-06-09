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

use std::{collections::HashSet, fs::File, io::Read};

use cucumber::{gherkin::Step, given, then, when};
use futures::{
    future::{join_all, try_join_all},
    stream, StreamExt, TryFutureExt,
};
use macro_rules_attribute::apply;
use tokio::time::sleep;
use typedb_driver::{Database, DatabaseManager, Result as TypeDBResult, TransactionType, TypeDBDriver};
use uuid::Uuid;

use crate::{
    assert_with_timeout,
    connection::transaction::open_transaction_for_database,
    generic_step, in_oneshot_background, params,
    query::run_query,
    util::{iter_table, read_file_to_string},
    Context,
};

async fn create_database(driver: &TypeDBDriver, name: String, may_error: params::MayError) {
    may_error.check(driver.databases().create(name).await);
}

async fn delete_database(driver: &TypeDBDriver, name: &str, may_error: params::MayError) {
    may_error.check(driver.databases().get(name).and_then(Database::delete).await);
}

async fn database_schema(driver: &TypeDBDriver, name: &str) -> String {
    driver.databases().get(name).await.expect("Expected database").schema().await.expect("Expected schema")
}

async fn database_type_schema(driver: &TypeDBDriver, name: &str) -> String {
    driver.databases().get(name).await.expect("Expected database").type_schema().await.expect("Expected type schema")
}

async fn has_database(driver: &TypeDBDriver, name: &str) -> bool {
    driver.databases().contains(name).await.unwrap()
}

async fn execute_and_retrieve_schema_for_comparison(driver: &TypeDBDriver, schema_query: String) -> String {
    let temp_database_name = create_temporary_database_with_schema(driver, schema_query).await;
    database_schema(driver, &temp_database_name).await
}

async fn create_temporary_database_with_schema(driver: &TypeDBDriver, schema_query: String) -> String {
    let name = format!("temp-{}", Uuid::new_v4());
    create_database(driver, name.clone(), params::MayError::False).await;
    let transaction = open_transaction_for_database(driver, &name, TransactionType::Schema, None)
        .await
        .expect("Expected transaction");
    run_query(&transaction, &schema_query, None).await.expect("Expected successful query");
    transaction.commit().await.expect("Expected successful commit");
    name
}

async fn import_database(
    context: &mut Context,
    name: String,
    schema: String,
    data_file_name: String,
    may_error: params::MayError,
) {
    let data_file_path = context.get_full_file_path(&data_file_name);
    let databases = context.driver.as_ref().unwrap().databases();
    may_error.check(databases.import_from_file(name, schema, data_file_path).await);
}

#[apply(generic_step)]
#[step(expr = "connection create database: {word}{may_error}")]
pub async fn connection_create_database(context: &mut Context, name: String, may_error: params::MayError) {
    create_database(context.driver.as_ref().unwrap(), name, may_error).await;
}

#[apply(generic_step)]
#[step(expr = "connection create database with empty name{may_error}")]
pub async fn connection_create_database_with_an_empty_name(context: &mut Context, may_error: params::MayError) {
    create_database(context.driver.as_ref().unwrap(), "".to_string(), may_error).await;
}

#[apply(generic_step)]
#[step(expr = "connection create database(s):")]
async fn connection_create_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step) {
        create_database(context.driver.as_ref().unwrap(), name.into(), params::MayError::False).await;
    }
}

#[apply(generic_step)]
#[step(expr = "connection create databases in parallel:")]
async fn connection_create_databases_in_parallel(context: &mut Context, step: &Step) {
    join_all(iter_table(step).map(|name| context.driver.as_ref().unwrap().databases().create(name))).await;
}

#[apply(generic_step)]
#[step(expr = "in background, connection create database: {word}{may_error}")]
pub async fn in_background_connection_create_database(
    context: &mut Context,
    name: String,
    may_error: params::MayError,
) {
    in_oneshot_background!(context, |background| {
        create_database(&background, name, may_error).await;
    });
}

#[apply(generic_step)]
#[step(expr = "connection delete database: {word}{may_error}")]
pub async fn connection_delete_database(context: &mut Context, name: String, may_error: params::MayError) {
    delete_database(context.driver.as_ref().unwrap(), &name, may_error).await;
}

#[apply(generic_step)]
#[step(expr = "connection delete database(s):")]
async fn connection_delete_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step) {
        delete_database(context.driver.as_ref().unwrap(), name, params::MayError::False).await;
    }
}

#[apply(generic_step)]
#[step(expr = "connection delete databases in parallel:")]
async fn connection_delete_databases_in_parallel(context: &mut Context, step: &Step) {
    try_join_all(
        iter_table(step).map(|name| context.driver.as_ref().unwrap().databases().get(name).and_then(Database::delete)),
    )
    .await
    .unwrap();
}

#[apply(generic_step)]
#[step(expr = "in background, connection delete database: {word}{may_error}")]
pub async fn in_background_connection_delete_database(
    context: &mut Context,
    name: String,
    may_error: params::MayError,
) {
    in_oneshot_background!(context, |background| {
        delete_database(&background, &name, may_error).await;
    });
}

#[apply(generic_step)]
#[step(expr = "connection has database: {word}")]
async fn connection_has_database(context: &mut Context, name: String) {
    assert_with_timeout!(
        has_database(context.driver.as_ref().unwrap(), &name).await,
        "Connection doesn't contain database {name}.",
    );
}

#[apply(generic_step)]
#[step(expr = "connection has database(s):")]
async fn connection_has_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step).map(|name| name.to_owned()) {
        assert_with_timeout!(
            has_database(context.driver.as_ref().unwrap(), &name).await,
            "Connection doesn't contain at least one of the databases.",
        );
    }
}

#[apply(generic_step)]
#[step(expr = "connection does not have database: {word}")]
async fn connection_does_not_have_database(context: &mut Context, name: String) {
    assert_with_timeout!(
        !has_database(context.driver.as_ref().unwrap(), &name).await,
        "Connection contains database {name}.",
    );
}

#[apply(generic_step)]
#[step(expr = "connection does not have database(s):")]
async fn connection_does_not_have_databases(context: &mut Context, step: &Step) {
    for name in iter_table(step).map(|name| name.to_owned()) {
        assert_with_timeout!(
            !has_database(context.driver.as_ref().unwrap(), &name).await,
            "Connection doesn't contain at least one of the databases.",
        );
    }
}

#[apply(generic_step)]
#[step(expr = r"connection get database\({word}\) has schema:")]
async fn connection_get_database_has_schema(context: &mut Context, name: String, step: &Step) {
    let expected_schema = step.docstring.as_ref().unwrap().trim().to_string();
    let driver = context.driver.as_ref().unwrap();
    let expected_schema_retrieved = if expected_schema.is_empty() {
        String::new()
    } else {
        execute_and_retrieve_schema_for_comparison(driver, expected_schema).await
    };
    let schema = database_schema(driver, &name).await;
    assert_eq!(expected_schema_retrieved, schema);
}

#[apply(generic_step)]
#[step(expr = r"connection get database\({word}\) has type schema:")]
async fn connection_get_database_has_type_schema(context: &mut Context, name: String, step: &Step) {
    let expected_type_schema = step.docstring.as_ref().unwrap().trim().to_string();
    let driver = context.driver.as_ref().unwrap();
    let expected_type_schema_retrieved = if expected_type_schema.is_empty() {
        String::new()
    } else {
        let temp_database_name = create_temporary_database_with_schema(driver, expected_type_schema).await;
        database_type_schema(driver, &temp_database_name).await
    };
    let type_schema = database_type_schema(driver, &name).await;
    assert_eq!(expected_type_schema_retrieved, type_schema);
}

#[apply(generic_step)]
#[step(expr = r"connection get database\({word}\) export to schema file\({word}\), data file\({word}\){may_error}")]
async fn connection_get_database_export_to_schema_file_data_file(
    context: &mut Context,
    name: String,
    schema_file_name: String,
    data_file_name: String,
    may_error: params::MayError,
) {
    let database = context.driver.as_ref().unwrap().databases().get(name).await.expect("Expected database");
    let schema_file_path = context.get_full_file_path(&schema_file_name);
    let data_file_path = context.get_full_file_path(&data_file_name);
    may_error.check(database.export_to_file(schema_file_path, data_file_path).await);
}

#[apply(generic_step)]
#[step(expr = r"connection import database\({word}\) from schema file\({word}\), data file\({word}\){may_error}")]
async fn connection_import_database_from_schema_file_data_file(
    context: &mut Context,
    name: String,
    schema_file_name: String,
    data_file_name: String,
    may_error: params::MayError,
) {
    let schema_file_path = context.get_full_file_path(&schema_file_name);
    let schema = read_file_to_string(schema_file_path);
    import_database(context, name, schema, data_file_name, may_error).await
}

#[apply(generic_step)]
#[step(expr = r"connection import database\({word}\) from data file\({word}\) and schema{may_error}")]
async fn connection_import_database_from_data_file_and_schema(
    context: &mut Context,
    name: String,
    data_file_name: String,
    may_error: params::MayError,
    step: &Step,
) {
    let schema = step.docstring.as_ref().unwrap().trim().to_string();
    import_database(context, name, schema, data_file_name, may_error).await
}

#[apply(generic_step)]
#[step(expr = r"file\({word}\) has schema:")]
async fn file_has_schema(context: &mut Context, file_name: String, step: &Step) {
    let expected_schema = step.docstring.as_ref().unwrap().trim().to_string();
    let file_schema = read_file_to_string(context.get_full_file_path(&file_name));
    let driver = context.driver.as_ref().unwrap();
    let expected_schema_retrieved = if expected_schema.is_empty() {
        String::new()
    } else {
        execute_and_retrieve_schema_for_comparison(driver, expected_schema).await
    };
    let file_schema_retrieved = execute_and_retrieve_schema_for_comparison(driver, file_schema).await;
    assert_eq!(expected_schema_retrieved, file_schema_retrieved);
}
