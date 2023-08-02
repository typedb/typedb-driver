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

use std::path::PathBuf;

use futures::TryFutureExt;
use typedb_client::{
    Connection, Credential, Database, DatabaseManager, Session, SessionType::Schema, TransactionType::Write,
};

pub const TEST_DATABASE: &str = "test";

pub fn new_core_connection() -> typedb_client::Result<Connection> {
    Connection::new_plaintext("0.0.0.0:1729")
}

pub fn new_cluster_connection() -> typedb_client::Result<Connection> {
    Connection::new_encrypted(
        &["localhost:11729", "localhost:21729", "localhost:31729"],
        Credential::with_tls(
            "admin",
            "password",
            Some(&PathBuf::from(
                std::env::var("ROOT_CA")
                    .expect("ROOT_CA environment variable needs to be set for cluster tests to run"),
            )),
        )?,
    )
}

pub async fn create_test_database_with_schema(connection: Connection, schema: &str) -> typedb_client::Result {
    let databases = DatabaseManager::new(connection);
    if databases.contains(TEST_DATABASE).await? {
        databases.get(TEST_DATABASE).and_then(Database::delete).await?;
    }
    databases.create(TEST_DATABASE).await?;

    let database = databases.get(TEST_DATABASE).await?;
    let session = Session::new(database, Schema).await?;
    let transaction = session.transaction(Write).await?;
    transaction.query().define(schema).await?;
    transaction.commit().await?;
    Ok(())
}

#[macro_export]
macro_rules! test_for_each_arg {
    {
        $perm_args:tt
        $( $( #[ $extra_anno:meta ] )* $async:ident fn $test:ident $args:tt -> $ret:ty $test_impl:block )+
    } => {
        test_for_each_arg!{ @impl $( $async fn $test $args $ret $test_impl )+ }
        test_for_each_arg!{ @impl_per $perm_args  { $( $( #[ $extra_anno ] )* $async fn $test )+ } }
    };

    { @impl $( $async:ident fn $test:ident $args:tt $ret:ty $test_impl:block )+ } => {
        mod _impl {
            use super::*;
            $( pub $async fn $test $args -> $ret $test_impl )+
        }
    };

    { @impl_per { $($mod:ident => $arg:expr),+ $(,)? } $fns:tt } => {
        $(test_for_each_arg!{ @impl_mod { $mod => $arg } $fns })+
    };

    { @impl_mod { $mod:ident => $arg:expr } { $( $( #[ $extra_anno:meta ] )* async fn $test:ident )+ } } => {
        mod $mod {
            use super::*;
        $(
            #[tokio::test]
            #[serial]
            $( #[ $extra_anno ] )*
            pub async fn $test() {
                _impl::$test($arg).await.unwrap();
            }
        )+
        }
    };
}
