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

use cucumber::{given, then, when};
use tokio::time::sleep;
use typedb_driver::{Credential, TypeDBDriver};

use crate::{assert_with_timeout, Context, generic_step, params};

use macro_rules_attribute::apply;
use crate::params::check_boolean;

mod database;
mod transaction;
mod user;

async fn create_driver(context: &Context, login: Option<String>, password: Option<String>) -> TypeDBDriver {
    if context.is_cloud {
        TypeDBDriver::new_cloud(
            &["localhost:11729", "localhost:21729", "localhost:31729"],
            Credential::with_tls(
                &login.expect("Login is required for cloud connection"),
                &password.expect("Password is required for cloud connection"),
                Some(&context.tls_root_ca),
            ).unwrap()
        ).unwrap()
    } else {
        TypeDBDriver::new_core("127.0.0.1:1729").await.unwrap()
    }
}

#[apply(generic_step)]
#[step("typedb starts")]
async fn typedb_starts(_: &mut Context) {}


#[apply(generic_step)]
#[step("connection opens with default authentication")]
async fn connection_opens_with_default_authentication(context: &mut Context) {
    context.set_driver(Some(create_driver(context, None, None).await));
}

#[apply(generic_step)]
#[step(expr = "connection opens with authentication: {word}, {word}")]
async fn connection_opens_with_authentication(context: &mut Context, login: String, password: String) {
    context.set_driver(Some(create_driver(context, Some(login), Some(password)).await))
}

#[apply(generic_step)]
#[step(expr = "connection is open: {boolean}")]
async fn connection_has_been_opened(context: &mut Context, is_open: params::Boolean) {
    check_boolean!(is_open, context.driver.is_some() && context.driver.as_ref().unwrap().is_open());
}

#[apply(generic_step)]
#[step(expr = r"connection has {int} database(s)")]
async fn connection_has_count_databases(context: &mut Context, count: usize) {
    assert_eq!(context.driver.as_ref().unwrap().databases().all().await.unwrap().len(), count);
}

#[apply(generic_step)]
#[step("connection closes")]
async fn driver_closes(context: &mut Context) {
    assert!(context.driver.as_ref().unwrap().force_close().is_ok());
}