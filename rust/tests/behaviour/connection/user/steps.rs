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

use cucumber::{given, then, when};
use tokio::time::sleep;
use typedb_client::Result as TypeDBResult;

use crate::{assert_err, assert_with_timeout, behaviour::Context, generic_step_impl};

generic_step_impl! {
    #[step(expr = "users get all")]
    async fn users_get_all(context: &mut Context) -> TypeDBResult {
        context.users.all().await?;
        Ok(())
    }

    #[step(expr = "users get all; throws exception")]
    async fn users_get_all_throws(context: &mut Context) {
        assert_err!(users_get_all(context).await);
    }

    #[step(expr = "users get user: {word}")]
    async fn users_get_user(context: &mut Context, username: String) -> TypeDBResult {
        context.users.get(username).await?;
        Ok(())
    }

    #[step(expr = "users get user: {word}; throws exception")]
    async fn users_get_user_throws(context: &mut Context, username: String) {
        assert_err!(users_get_user(context, username).await);
    }

    #[step(expr = "users contains: {word}")]
    async fn users_contains(context: &mut Context, username: String) -> TypeDBResult {
        assert_with_timeout!(context.users.contains(username.clone()).await?, "User {username} doesn't exist.");
        Ok(())
    }

    #[step(expr = "users contains: {word}; throws exception")]
    async fn users_contains_throws(context: &mut Context, username: String) {
        assert_err!(users_contains(context, username).await);
    }

    #[step(expr = "users not contains: {word}")]
    async fn users_not_contains(context: &mut Context, username: String) -> TypeDBResult {
        assert_with_timeout!(!context.users.contains(username.clone()).await?, "User {username} exists.");
        Ok(())
    }

    #[step(expr = "users not contains: {word}; throws exception")]
    async fn users_not_contains_throws(context: &mut Context, username: String) {
        assert_err!(users_not_contains(context, username).await);
    }

    #[step(expr = "users create: {word}, {word}")]
    async fn users_create(context: &mut Context, username: String, password: String) -> TypeDBResult {
        context.users.create(username, password).await
    }

    #[step(expr = "users create: {word}, {word}; throws exception")]
    async fn users_create_throws(context: &mut Context, username: String, password: String) {
        assert_err!(users_create(context, username, password).await);
    }

    #[step(expr = "users password set: {word}, {word}")]
    async fn users_password_set(context: &mut Context, username: String, password: String) -> TypeDBResult {
        context.users.set_password(username, password).await
    }

    #[step(expr = "users password set: {word}, {word}; throws exception")]
    async fn users_password_set_throws(context: &mut Context, username: String, password: String) {
        assert_err!(users_password_set(context, username, password).await);
    }

    #[step(expr = "user password update: {word}, {word}")]
    async fn user_password_update(context: &mut Context, password_old: String, password_new: String) -> TypeDBResult {
        let connected_user = context.users.current_user().await?;
        assert!(connected_user.is_some());
        connected_user.unwrap().password_update(&context.connection, password_old, password_new).await
    }

    #[step(expr = "user password update: {word}, {word}; throws exception")]
    async fn user_password_update_throws(context: &mut Context, password_old: String, password_new: String) {
        assert_err!(user_password_update(context, password_old, password_new).await);
    }

    #[step(expr = "users delete: {word}")]
    async fn user_delete(context: &mut Context, username: String) -> TypeDBResult {
        context.users.delete(username).await
    }

    #[step(expr = "users delete: {word}; throws exception")]
    async fn user_delete_throws(context: &mut Context, username: String) {
        assert_err!(user_delete(context, username).await);
    }

    #[step(expr = "user expiry-seconds")]
    async fn user_expiry_seconds(context: &mut Context) -> TypeDBResult {
        let connected_user = context.users.current_user().await?;
        assert!(connected_user.is_some());
        assert!(connected_user.unwrap().password_expiry_seconds.is_some());
        Ok(())
    }

    #[step(expr = "get connected user")]
    async fn get_connected_user(context: &mut Context) -> TypeDBResult {
        assert!(context.users.current_user().await?.is_some());
        Ok(())
    }
}
