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
use futures::{StreamExt, TryStreamExt};
use typedb_client::{
    concept::{Annotation, Attribute, Entity, Relation, Thing, ThingType, Transitivity},
    transaction::concept::api::{AttributeAPI, AttributeTypeAPI, EntityTypeAPI, RelationTypeAPI, ThingAPI},
    Result as TypeDBResult,
};

use crate::{
    assert_err,
    behaviour::{
        parameter::{ContainmentParam, LabelParam, OptionalAsValueTypeParam, ScopedLabelParam, ValueParam, VarParam},
        Context,
    },
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "entity {var} is deleted: {word}")]
    async fn entity_is_deleted(context: &mut Context, var: VarParam, is_deleted: bool) -> TypeDBResult {
        assert_eq!(context.get_entity(var.name).is_deleted(context.transaction()).await?, is_deleted);
        Ok(())
    }

    #[step(expr = "entity {var} has type: {label}")]
    async fn entity_has_type(context: &mut Context, var: VarParam, type_label: LabelParam) {
        assert_eq!(context.get_entity(var.name).type_.label, type_label.name);
    }

    #[step(expr = "delete entity: {var}")]
    async fn delete_entity(context: &mut Context, var: VarParam) -> TypeDBResult {
        context.get_entity(var.name).delete(context.transaction()).await
    }

    #[step(expr = "entity {var} set has: {var}")]
    async fn entity_set_has(context: &mut Context, var: VarParam, attribute_var: VarParam) -> TypeDBResult {
        context
            .get_entity(var.name)
            .set_has(context.transaction(), context.get_attribute(attribute_var.name).clone())
            .await
    }

    #[step(expr = "entity {var} set has: {var}; throws exception")]
    async fn entity_set_has_throws(context: &mut Context, var: VarParam, attribute_var: VarParam) {
        assert_err!(entity_set_has(context, var, attribute_var).await)
    }

    #[step(expr = "entity {var} unset has: {var}")]
    async fn entity_unset_has(context: &mut Context, var: VarParam, attribute_var: VarParam) -> TypeDBResult {
        context
            .get_entity(var.name)
            .unset_has(context.transaction(), context.get_attribute(attribute_var.name).clone())
            .await
    }

    #[step(expr = "entity {var} get keys {containment}: {var}")]
    async fn entity_get_keys_contain(
        context: &mut Context,
        var: VarParam,
        containment: ContainmentParam,
        attribute_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity(var.name);
        let actuals: Vec<Attribute> = entity.get_has(tx, vec![], vec![Annotation::Key])?.try_collect().await?;
        let attribute = context.get_attribute(attribute_var.name);
        containment.assert(&actuals, attribute);
        Ok(())
    }

    #[step(expr = "entity {var} get attributes {containment}: {var}")]
    async fn entity_get_attributes_contain(
        context: &mut Context,
        var: VarParam,
        containment: ContainmentParam,
        attribute_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity(var.name);
        let actuals: Vec<Attribute> = entity.get_has(tx, vec![], vec![])?.try_collect().await?;
        let attribute = context.get_attribute(attribute_var.name);
        containment.assert(&actuals, attribute);
        Ok(())
    }

    #[step(expr = r"entity {var} get attributes\(( ){label}( )\){optional_value_type} {containment}: {var}")]
    async fn entity_get_attributes_of_type_contain(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
        value_type: OptionalAsValueTypeParam,
        containment: ContainmentParam,
        attribute_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity(var.name);
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        if let Some(value_type) = value_type.value_type {
            assert_eq!(attribute_type.value_type, value_type);
        }
        let actuals: Vec<Attribute> = entity.get_has(tx, vec![attribute_type], vec![])?.try_collect().await?;
        let attribute = context.get_attribute(attribute_var.name);
        containment.assert(&actuals, attribute);
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get instances is empty")]
    async fn entity_type_get_instances_is_empty(context: &mut Context, type_label: LabelParam) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        assert!(entity_type.get_instances(tx, Transitivity::Transitive)?.next().await.is_none());
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get instances {containment}: {var}")]
    async fn entity_type_get_instances_contain(
        context: &mut Context,
        type_label: LabelParam,
        containment: ContainmentParam,
        var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let actuals: Vec<Entity> = entity_type.get_instances(tx, Transitivity::Transitive)?.try_collect().await?;
        let entity = context.get_entity(var.name);
        containment.assert(&actuals, entity);
        Ok(())
    }

    #[step(expr = r"{var} = entity\(( ){label}( )\) create new instance")]
    async fn entity_type_create_new_instance(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity_type(type_label.name).await?.create(tx).await?;
        context.insert_entity(var.name, Some(entity));
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) create new instance; throws exception")]
    async fn entity_type_create_new_instance_throws(context: &mut Context, type_label: LabelParam) {
        assert_err!(entity_type_create_new_instance(context, VarParam::default(), type_label).await);
    }

    #[step(expr = r"{var} = entity\(( ){label}( )\) create new instance with key\({label}\): {value}")]
    async fn entity_type_create_new_instance_with_key(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        value: ValueParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity_type(type_label.name).await?.create(tx).await?;
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        let attribute = attribute_type.put(tx, value.into_value(attribute_type.value_type)).await?;
        entity.set_has(tx, attribute).await?;
        context.insert_entity(var.name, Some(entity));
        Ok(())
    }

    #[step(expr = r"{var} = entity\(( ){label}( )\) get instance with key\({label}\): {value}")]
    async fn entity_type_get_instance_with_key(
        context: &mut Context,
        var: VarParam,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        value: ValueParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        let attribute = attribute_type.get(tx, value.into_value(attribute_type.value_type)).await?.unwrap();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let entities: Vec<Thing> =
            attribute.get_owners(tx, Some(ThingType::EntityType(entity_type)))?.try_collect().await?;
        context.insert_thing(var.name, entities.into_iter().next());
        Ok(())
    }

    #[step(expr = r"entity {var} get relations {containment}: {var}")]
    async fn entity_type_get_relations_contain(
        context: &mut Context,
        var: VarParam,
        containment: ContainmentParam,
        relation_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity = context.get_entity(var.name);
        let actuals: Vec<Relation> = entity.get_relations(tx, vec![])?.try_collect().await?;
        let expected = context.get_relation(relation_var.name);
        containment.assert(&actuals, expected);
        Ok(())
    }

    #[step(expr = r"entity {var} get relations\({scoped_label}\) {containment}: {var}")]
    async fn entity_type_get_relations_by_role(
        context: &mut Context,
        var: VarParam,
        role_label: ScopedLabelParam,
        containment: ContainmentParam,
        relation_var: VarParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let role_label = role_label.label;
        let relation_type = context.get_relation_type(role_label.scope).await?;
        let role_type = relation_type.get_relates_for_role_label(tx, role_label.name).await?.unwrap();
        let entity = context.get_entity(var.name);
        let actuals: Vec<Relation> = entity.get_relations(tx, vec![role_type])?.try_collect().await?;
        let expected = context.get_relation(relation_var.name);
        containment.assert(&actuals, expected);
        Ok(())
    }
}
