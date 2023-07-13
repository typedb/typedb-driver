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
use futures::TryStreamExt;
use typedb_client::{
    concept::{Attribute, Thing, Transitivity},
    transaction::concept::api::{AttributeAPI, AttributeTypeAPI, ThingAPI},
    Result as TypeDBResult,
};

use crate::{
    assert_err,
    behaviour::{
        parameter::{ContainmentParam, LabelParam, ValueParam, ValueTypeParam, VarParam},
        Context,
    },
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "attribute {var} is deleted: {word}")]
    async fn attribute_is_deleted(context: &mut Context, var: VarParam, is_deleted: bool) -> TypeDBResult {
        assert_eq!(context.get_attribute(var.name).is_deleted(context.transaction()).await?, is_deleted);
        Ok(())
    }

    #[step(expr = "attribute {var} has type: {label}")]
    async fn attribute_has_type(context: &mut Context, var: VarParam, type_label: LabelParam) {
        assert_eq!(context.get_attribute(var.name).type_.label, type_label.name);
    }

    #[step(expr = "delete attribute: {var}")]
    async fn delete_attribute(context: &mut Context, var: VarParam) -> TypeDBResult {
        context.get_attribute(var.name).delete(context.transaction()).await
    }

    #[step(expr = r"attribute\(( ){label}( )\) get instances {containment}: {var}")]
    async fn attribute_get_instances_contain(
        context: &mut Context,
        type_label: LabelParam,
        containment: ContainmentParam,
        var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<Attribute> = attribute_type.get_instances(tx, Transitivity::Transitive)?.try_collect().await?;
        let attribute = context.get_attribute(var.name);
        containment.assert(&actuals, attribute);
        Ok(())
    }

    #[step(expr = "attribute {var} get owners {containment}: {var}")]
    async fn attribute_get_owners_contain(
        context: &mut Context,
        var: VarParam,
        containment: ContainmentParam,
        owner_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute = context.get_attribute(var.name);
        let actuals: Vec<Thing> = attribute.get_owners(tx, None)?.try_collect().await?;
        let expected = context.get_thing(owner_var.name);
        containment.assert(&actuals, expected);
        Ok(())
    }

    #[step(expr = "attribute {var} has value type: {value_type}")]
    async fn attribute_has_value_type(context: &mut Context, var: VarParam, value_type: ValueTypeParam) {
        assert_eq!(context.get_attribute(var.name).type_.value_type, value_type.value_type);
    }

    #[step(expr = r"{var} = attribute\(( ){label}( )\) as\(( ){value_type}( )\) put: {value}")]
    async fn attribute_put_value(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        value: ValueParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        let attribute = attribute_type.put(tx, value.into_value(value_type.value_type)).await?;
        context.insert_attribute(var.name, Some(attribute));
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) as\(( ){value_type}( )\) put: {value}; throws exception")]
    async fn attribute_put_value_throws(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        value: ValueParam,
    ) {
        assert_err!(attribute_put_value(context, VarParam::default(), type_label, value_type, value).await);
    }

    #[step(expr = r"{var} = attribute\(( ){label}( )\) as\(( ){value_type}( )\) get: {value}")]
    async fn attribute_get_value(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        value: ValueParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        let attribute = attribute_type.get(tx, value.into_value(value_type.value_type)).await?;
        context.insert_attribute(var.name, attribute);
        Ok(())
    }

    #[step(expr = "attribute {var} has {value_type} value: {value}")]
    async fn attribute_has_value(context: &mut Context, var: VarParam, value_type: ValueTypeParam, value: ValueParam) {
        assert_eq!(context.get_attribute(var.name).value, value.into_value(value_type.value_type));
    }
}
