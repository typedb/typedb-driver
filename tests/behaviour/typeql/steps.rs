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

use cucumber::{gherkin::Step, given, then, when};
use futures::{TryFutureExt, TryStreamExt};
use typeql_lang::parse_query;

use crate::{behaviour::Context, generic_step_impl};

generic_step_impl! {
    #[step(expr = "typeql define")]
    async fn typeql_define(context: &mut Context, step: &Step) {
        let parsed = parse_query(step.docstring().unwrap());
        assert!(parsed.is_ok());
        let res = context.transaction().query().define(&parsed.unwrap().to_string()).await;
        assert!(res.is_ok());
    }

    #[step(expr = "typeql define; throws exception")]
    async fn typeql_define_throws(context: &mut Context, step: &Step) {
        let parsed = parse_query(step.docstring().unwrap());
        if parsed.is_ok() {
            let res = context.transaction().query().define(&parsed.unwrap().to_string()).await;
            assert!(res.is_err());
        }
    }

    #[step(expr = "typeql define; throws exception containing {string}")]
    async fn typeql_define_throws_exception(context: &mut Context, step: &Step, exception: String) {
        let result = async { parse_query(step.docstring().unwrap()).map_err(|error| error.to_string()) }
            .and_then(|parsed| async move {
                context.transaction().query().define(&parsed.to_string()).await.map_err(|error| error.to_string())
            })
            .await;
        assert!(result.is_err());
        assert!(result.unwrap_err().contains(&exception));
    }

    #[step(expr = "typeql insert")]
    async fn typeql_insert(context: &mut Context, step: &Step) {
        let parsed = parse_query(step.docstring().unwrap());
        assert!(parsed.is_ok());
        let inserted = context.transaction().query().insert(&parsed.unwrap().to_string());
        assert!(inserted.is_ok());
        let res = inserted.unwrap().try_collect::<Vec<_>>().await;
        assert!(res.is_ok());
    }

    #[step(expr = "typeql insert; throws exception")]
    async fn typeql_insert_throws(context: &mut Context, step: &Step) {
        let parsed = parse_query(step.docstring().unwrap());
        if parsed.is_ok() {
            let inserted = context.transaction().query().insert(&parsed.unwrap().to_string());
            if inserted.is_ok() {
                let res = inserted.unwrap().try_collect::<Vec<_>>().await;
                assert!(res.is_err());
            }
        }
    }

    #[step(expr = "typeql insert; throws exception containing {string}")]
    async fn typeql_insert_throws_exception(context: &mut Context, step: &Step, exception: String) {
        let result = async {
            parse_query(step.docstring().unwrap()).map_err(|error| error.to_string()).and_then(|parsed| {
                context.transaction().query().insert(&parsed.to_string()).map_err(|error| error.to_string())
            })
        }
        .and_then(|inserted| async { inserted.try_collect::<Vec<_>>().await.map_err(|error| error.to_string()) })
        .await;
        assert!(result.is_err());
        assert!(result.unwrap_err().contains(&exception));
    }
}
