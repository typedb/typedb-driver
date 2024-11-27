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
use macro_rules_attribute::apply;
use tokio::time::sleep;
use typedb_driver::Result as TypeDBResult;

use crate::{assert_err, assert_with_timeout, generic_step, params, util::iter_table, Context};

async fn all_user_names(context: &Context) -> HashSet<String> {
    context.driver.as_ref().unwrap().users().all().await.unwrap().into_iter().map(|user| user.name).collect()
}

#[apply(generic_step)]
#[step(expr = "get all users:")]
async fn get_all_users(context: &mut Context, step: &Step) {
    let expected_users: HashSet<String> = iter_table(step).map(|name| name.to_owned()).collect();
    assert_with_timeout!(all_user_names(context).await == expected_users, "Connection's databases are not as expected",);
}

#[apply(generic_step)]
#[step(expr = "get all users; fails")]
async fn get_all_users_fails(context: &mut Context) {
    params::MayError::True(None).check(context.driver.as_ref().unwrap().users().all().await);
}

#[apply(generic_step)]
#[step(expr = "get all users {contains_or_doesnt}: {word}")]
async fn get_all_users_contains(context: &mut Context, contains_or_doesnt: params::ContainsOrDoesnt, username: String) {
    contains_or_doesnt.check(&all_user_names(context).await.iter().find(|&name| name == &username), "user");
}

#[apply(generic_step)]
#[step(expr = "users get user: {word}; fails")]
async fn users_get_user_fails(context: &mut Context, username: String) {
    params::MayError::True(None).check(context.driver.as_ref().unwrap().users().get(username).await);
}

#[apply(generic_step)]
#[step(expr = r"get user\({word}\) get name: {word}")]
async fn get_user_get_name(context: &mut Context, user: String, name: String) {
    let user = context.driver.as_ref().unwrap().users().get(user).await.unwrap().unwrap();
    assert_eq!(user.name, name);
}

#[apply(generic_step)]
#[step(expr = "create user with username '{word}', password '{word}'{may_error}")]
async fn create_user(context: &mut Context, username: String, password: String, may_error: params::MayError) {
    may_error.check(context.driver.as_ref().unwrap().users().create(username, password).await);
}

#[apply(generic_step)]
#[step(expr = r"get user\({word}\) set password: {word}{may_error}")]
async fn get_user_set_password(context: &mut Context, username: String, password: String, may_error: params::MayError) {
    todo!("Password setting not implemented")
    // may_error.check(context.driver.as_ref().unwrap().users().set_password(username, password).await);
}

#[apply(generic_step)]
#[step(expr = r"get user\({word}\) update password from '{word}' to '{word}' {may_error}")]
async fn get_user_update_password(
    context: &mut Context,
    username: String,
    password_old: String,
    password_new: String,
    may_error: params::MayError,
) {
    todo!("Password updating not implemented")
    // may_error.check(context.driver.as_ref().unwrap().users().get(username).unwrap().unwrap().password_update(password_old, password_new).await);
}

#[apply(generic_step)]
#[step(expr = "delete user: {word}{may_error}")]
async fn delete_user(context: &mut Context, username: String, may_error: params::MayError) {
    may_error.check(context.driver.as_ref().unwrap().users().delete(username).await);
}

#[apply(generic_step)]
#[step(expr = "get current username: {word}")]
async fn get_current_username(context: &mut Context, username: String) {
    assert_eq!(context.driver.as_ref().unwrap().users().current_username(), &username);
}
