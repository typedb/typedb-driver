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
use macro_rules_attribute::apply;
use tokio::time::sleep;
use typedb_driver::{Credentials, TypeDBDriver};

use crate::{assert_with_timeout, generic_step, params, params::check_boolean, Context};

mod database;
mod transaction;
mod user;

#[apply(generic_step)]
#[step("typedb starts")]
async fn typedb_starts(_: &mut Context) {}

#[apply(generic_step)]
#[step("connection opens with default authentication")]
async fn connection_opens_with_default_authentication(context: &mut Context) {
    context.set_driver(context.create_default_driver().await.unwrap());
}

#[apply(generic_step)]
#[step(expr = "connection opens with username '{word}', password '{word}'{may_error}")]
async fn connection_opens_with_authentication(
    context: &mut Context,
    username: String,
    password: String,
    may_error: params::MayError,
) {
    if let Some(driver) = may_error.check(context.create_driver(Some(&username), Some(&password)).await) {
        context.set_driver(driver)
    }
}

fn change_host(address: &str, new_host: &str) -> String {
    let parts: Vec<&str> = address.split(':').collect();
    assert_eq!(parts.len(), 2);
    format!("{}:{}", new_host, parts[1])
}

fn change_port(address: &str, new_port: &str) -> String {
    let parts: Vec<&str> = address.split(':').collect();
    assert_eq!(parts.len(), 2);
    format!("{}:{}", parts[0], new_port)
}

#[apply(generic_step)]
#[step(expr = "connection opens with a wrong host{may_error}")]
async fn connection_opens_with_a_wrong_host(context: &mut Context, may_error: params::MayError) {
    may_error.check(match context.is_cluster {
        false => {
            context
                .create_driver_community(
                    &change_host(Context::DEFAULT_ADDRESS, "surely-not-localhost"),
                    Context::ADMIN_USERNAME,
                    Context::ADMIN_PASSWORD,
                )
                .await
        }
        true => {
            let updated_address =
                change_host(Context::DEFAULT_CLUSTER_ADDRESSES.get(0).unwrap(), "surely-not-localhost");
            context.create_driver_cluster(&[&updated_address], Context::ADMIN_USERNAME, Context::ADMIN_PASSWORD).await
        }
    });
}

#[apply(generic_step)]
#[step(expr = "connection opens with a wrong port{may_error}")]
async fn connection_opens_with_a_wrong_port(context: &mut Context, may_error: params::MayError) {
    may_error.check(match context.is_cluster {
        false => {
            context
                .create_driver_community(
                    &change_port(Context::DEFAULT_ADDRESS, "0"),
                    Context::ADMIN_USERNAME,
                    Context::ADMIN_PASSWORD,
                )
                .await
        }
        true => {
            let updated_address = change_port(Context::DEFAULT_CLUSTER_ADDRESSES.get(0).unwrap(), "0");
            context.create_driver_cluster(&[&updated_address], Context::ADMIN_USERNAME, Context::ADMIN_PASSWORD).await
        }
    });
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
#[step(expr = r"connection has {int} user(s)")]
async fn connection_has_count_users(context: &mut Context, count: usize) {
    assert_eq!(context.driver.as_ref().unwrap().users().all().await.unwrap().len(), count);
}

#[apply(generic_step)]
#[step(expr = r"connection closes{may_error}")]
async fn driver_closes(context: &mut Context, may_error: params::MayError) {
    may_error.check(context.driver.as_ref().unwrap().force_close());
    context.cleanup_transactions().await;
}
