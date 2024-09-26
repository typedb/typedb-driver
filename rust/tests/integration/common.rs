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

use std::path::PathBuf;

use futures::TryFutureExt;
use typedb_driver::{
    answer::QueryAnswer, driver::TypeDBDriver, Credential, Database, DatabaseManager, Options, TransactionType,
};

pub const TEST_DATABASE: &str = "test";

pub async fn new_core_driver() -> typedb_driver::Result<TypeDBDriver> {
    TypeDBDriver::new_core("127.0.0.1:1729").await
}

pub async fn new_cloud_driver() -> typedb_driver::Result<TypeDBDriver> {
    TypeDBDriver::new_cloud(
        &["localhost:11729", "localhost:21729", "localhost:31729"],
        Credential::with_tls(
            "admin",
            "password",
            Some(&PathBuf::from(
                std::env::var("ROOT_CA").expect("ROOT_CA environment variable needs to be set for cloud tests to run"),
            )),
        )?,
    )
}

pub async fn create_test_database_with_schema(driver: &TypeDBDriver, schema: &str) -> typedb_driver::Result {
    let databases = driver.databases();
    if databases.contains(TEST_DATABASE).await? {
        databases.get(TEST_DATABASE).and_then(Database::delete).await?;
    }
    databases.create(TEST_DATABASE).await?;

    let transaction = driver.transaction(TEST_DATABASE, TransactionType::Schema).await?;

    let answer = transaction.query(schema).await?;
    assert!(matches!(answer, QueryAnswer::Ok()));

    let result = transaction.commit().await;
    assert!(matches!(result, Ok(_)));
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
