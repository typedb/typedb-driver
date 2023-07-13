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
use typedb_client::{transaction::logic::api::RuleAPI, Result as TypeDBResult};

use crate::{
    assert_err,
    behaviour::{parameter::LabelParam, Context},
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "delete rule: {label}")]
    async fn delete_rule(context: &mut Context, label: LabelParam) -> TypeDBResult {
        let tx = context.transaction();
        context.get_rule(label.name).await?.unwrap().delete(tx).await
    }

    #[step(expr = "delete rule: {label}; throws exception")]
    async fn delete_rule_throws(context: &mut Context, type_label: LabelParam) {
        assert_err!(delete_rule(context, type_label).await);
    }

    #[step(expr = r"rule\(( ){label}( )\) set label: {label}")]
    async fn rule_set_label(context: &mut Context, current_label: LabelParam, new_label: LabelParam) -> TypeDBResult {
        let tx = context.transaction();
        context.get_rule(current_label.name).await?.unwrap().set_label(tx, new_label.name).await
    }
}
