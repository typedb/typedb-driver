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

use crate::{behaviour::Context, generic_step_impl};

generic_step_impl! {
    #[step("typedb starts")]
    async fn typedb_starts(_: &mut Context) {}

    #[step("connection opens with default authentication")]
    async fn connection_opens_with_default_authentication(_: &mut Context) {}

    #[step("connection has been opened")]
    async fn connection_has_been_opened(_: &mut Context) {}

    #[step("connection does not have any database")]
    async fn connection_does_not_have_any_database(context: &mut Context) {
        assert!(context.databases.all().await.unwrap().is_empty());
    }
}
