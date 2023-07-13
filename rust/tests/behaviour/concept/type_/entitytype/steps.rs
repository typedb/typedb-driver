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
use futures::TryStreamExt;
use typedb_client::{
    concept::{EntityType, ScopedLabel, Transitivity},
    transaction::concept::api::{EntityTypeAPI, RelationTypeAPI, ThingTypeAPI},
    Result as TypeDBResult,
};

use crate::{
    assert_err,
    behaviour::{
        parameter::{
            ContainmentParam, LabelParam, OptionalAnnotationsParam, OptionalExplicitParam, OptionalOverrideLabelParam,
            OptionalOverrideScopedLabelParam, ScopedLabelParam,
        },
        util::iter_table,
        Context,
    },
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "put entity type: {label}")]
    async fn put_entity_type(context: &mut Context, type_label: LabelParam) -> TypeDBResult<EntityType> {
        context.transaction().concept().put_entity_type(type_label.name).await
    }

    #[step(expr = "delete entity type: {label}")]
    async fn delete_entity_type(context: &mut Context, type_label: LabelParam) -> TypeDBResult {
        let tx = context.transaction();
        context.get_entity_type(type_label.name).await?.delete(tx).await
    }

    #[step(expr = "delete entity type: {label}; throws exception")]
    async fn delete_entity_type_throws(context: &mut Context, type_label: LabelParam) {
        assert_err!(delete_entity_type(context, type_label).await);
    }

    #[step(expr = r"entity\(( ){label}( )\) is null: {word}")]
    async fn entity_type_is_null(context: &mut Context, type_label: LabelParam, is_null: bool) -> TypeDBResult {
        let res = context.transaction().concept().get_entity_type(type_label.name).await?;
        assert_eq!(res.is_none(), is_null, "{res:?}");
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) set label: {label}")]
    async fn entity_type_set_label(
        context: &mut Context,
        type_label: LabelParam,
        new_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        context.get_entity_type(type_label.name).await?.set_label(tx, new_label.name).await
    }

    #[step(expr = r"entity\(( ){label}( )\) get label: {label}")]
    async fn entity_type_get_label(
        context: &mut Context,
        type_label: LabelParam,
        get_label: LabelParam,
    ) -> TypeDBResult {
        assert_eq!(context.get_entity_type(type_label.name).await?.label, get_label.name);
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) set abstract: {word}")]
    async fn entity_type_set_abstract(
        context: &mut Context,
        type_label: LabelParam,
        is_abstract: bool,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut entity_type = context.get_entity_type(type_label.name).await?;
        if is_abstract {
            entity_type.set_abstract(tx).await
        } else {
            entity_type.unset_abstract(tx).await
        }
    }

    #[step(expr = r"entity\(( ){label}( )\) set abstract: {word}; throws exception")]
    async fn entity_type_set_abstract_throws(context: &mut Context, type_label: LabelParam, is_abstract: bool) {
        assert_err!(entity_type_set_abstract(context, type_label, is_abstract).await);
    }

    #[step(expr = r"entity\(( ){label}( )\) is abstract: {word}")]
    async fn entity_type_is_abstract(context: &mut Context, type_label: LabelParam, is_abstract: bool) -> TypeDBResult {
        assert_eq!(context.get_entity_type(type_label.name).await?.is_abstract, is_abstract);
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) set supertype: {label}")]
    async fn entity_type_set_supertype(
        context: &mut Context,
        type_label: LabelParam,
        supertype_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let supertype = context.get_entity_type(supertype_label.name).await?;
        context.get_entity_type(type_label.name).await?.set_supertype(tx, supertype).await
    }

    #[step(expr = r"entity\(( ){label}( )\) set supertype: {label}; throws exception")]
    async fn entity_type_set_supertype_throws(
        context: &mut Context,
        type_label: LabelParam,
        supertype_label: LabelParam,
    ) {
        assert_err!(entity_type_set_supertype(context, type_label, supertype_label).await)
    }

    #[step(expr = r"entity\(( ){label}( )\) get supertype: {label}")]
    async fn entity_type_get_supertype(
        context: &mut Context,
        type_label: LabelParam,
        supertype: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        assert_eq!(context.get_entity_type(type_label.name).await?.get_supertype(tx).await?.label, supertype.name);
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get supertypes {containment}:")]
    async fn entity_type_get_supertypes(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let actuals = entity_type.get_supertypes(tx)?.map_ok(|et| et.label).try_collect::<Vec<_>>().await?;
        for supertype in iter_table(step) {
            containment.assert(&actuals, supertype);
        }
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get subtypes {containment}:")]
    async fn entity_type_get_subtypes(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let actuals = entity_type
            .get_subtypes(tx, Transitivity::Transitive)?
            .map_ok(|et| et.label)
            .try_collect::<Vec<_>>()
            .await?;
        for subtype in iter_table(step) {
            containment.assert(&actuals, subtype);
        }
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) set owns attribute type: {label}{optional_override_label}{annotations}")]
    async fn entity_type_set_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        overridden_attribute_type_label: OptionalOverrideLabelParam,
        annotations: OptionalAnnotationsParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut entity_type = context.get_entity_type(type_label.name).await?;
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        let overridden_attribute_type = if let Some(label) = overridden_attribute_type_label.name {
            Some(context.get_attribute_type(label).await?)
        } else {
            None
        };
        entity_type.set_owns(tx, attribute_type, overridden_attribute_type, annotations.annotations).await
    }

    #[step(
        expr = r"entity\(( ){label}( )\) set owns attribute type: {label}{optional_override_label}{annotations}; throws exception"
    )]
    async fn entity_type_set_owns_attribute_type_throws(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        overridden_attribute_type_label: OptionalOverrideLabelParam,
        annotations: OptionalAnnotationsParam,
    ) {
        assert_err!(
            entity_type_set_owns_attribute_type(
                context,
                type_label,
                attribute_type_label,
                overridden_attribute_type_label,
                annotations
            )
            .await
        );
    }

    #[step(expr = r"entity\(( ){label}( )\) unset owns attribute type: {label}")]
    async fn entity_type_unset_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut entity_type = context.get_entity_type(type_label.name).await?;
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        entity_type.unset_owns(tx, attribute_type).await
    }

    #[step(expr = r"entity\(( ){label}( )\) unset owns attribute type: {label}; throws exception")]
    async fn entity_type_unset_owns_attribute_type_throws(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
    ) {
        assert_err!(entity_type_unset_owns_attribute_type(context, type_label, attribute_type_label).await);
    }

    #[step(
        expr = r"entity\(( ){label}( )\) get owns{optional_explicit} attribute types{annotations}(;) {containment}:"
    )]
    async fn entity_type_get_owns_attribute_types_contain(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        optional_explicit: OptionalExplicitParam,
        annotations: OptionalAnnotationsParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let actuals: Vec<String> = entity_type
            .get_owns(tx, None, optional_explicit.transitivity, annotations.annotations)?
            .map_ok(|at| at.label)
            .try_collect()
            .await?;
        for attribute in iter_table(step) {
            containment.assert(&actuals, attribute);
        }
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get owns overridden attribute\(( ){label}( )\) is null: {word}")]
    async fn entity_type_get_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        is_null: bool,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        let res = entity_type.get_owns_overridden(tx, attribute_type).await?;
        assert_eq!(res.is_none(), is_null);
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) get owns overridden attribute\(( ){label}( )\) get label: {label}")]
    async fn entity_type_get_owns_attribute_type_label(
        context: &mut Context,
        type_label: LabelParam,
        attribute_type_label: LabelParam,
        overridden_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let attribute_type = context.get_attribute_type(attribute_type_label.name).await?;
        let res = entity_type.get_owns_overridden(tx, attribute_type).await?;
        assert_eq!(res.map(|at| at.label), Some(overridden_label.name));
        Ok(())
    }

    #[step(expr = r"entity\(( ){label}( )\) set plays role: {scoped_label}{optional_override_scoped_label}")]
    async fn entity_type_set_plays_role(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
        overridden_role_label: OptionalOverrideScopedLabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let role_label = role_label.label;
        let relation_type = context.get_relation_type(role_label.scope).await?;
        let role = relation_type.get_relates_for_role_label(tx, role_label.name).await?.unwrap();
        let overridden_role = if let Some(ScopedLabel { scope, name }) = overridden_role_label.label {
            Some(context.get_relation_type(scope).await?.get_relates_for_role_label(tx, name).await?.unwrap())
        } else {
            None
        };
        let mut entity_type = context.get_entity_type(type_label.name).await?;
        entity_type.set_plays(tx, role, overridden_role).await
    }

    #[step(
        expr = r"entity\(( ){label}( )\) set plays role: {scoped_label}{optional_override_scoped_label}; throws exception"
    )]
    async fn entity_type_set_plays_role_throws(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
        overridden_role_label: OptionalOverrideScopedLabelParam,
    ) {
        assert_err!(entity_type_set_plays_role(context, type_label, role_label, overridden_role_label).await);
    }

    #[step(expr = r"entity\(( ){label}( )\) unset plays role: {scoped_label}")]
    async fn entity_type_unset_plays_role(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let role_label = role_label.label;
        let mut entity_type = context.get_entity_type(type_label.name).await?;
        let role = context
            .get_relation_type(role_label.scope)
            .await?
            .get_relates_for_role_label(tx, role_label.name)
            .await?
            .unwrap();
        entity_type.unset_plays(tx, role).await
    }

    #[step(expr = r"entity\(( ){label}( )\) unset plays role: {scoped_label}; throws exception")]
    async fn entity_type_unset_plays_role_throws(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
    ) {
        assert_err!(entity_type_unset_plays_role(context, type_label, role_label).await);
    }

    #[step(expr = r"entity\(( ){label}( )\) get playing roles{optional_explicit} {containment}:")]
    async fn entity_type_get_playing_roles_contain(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        optional_explicit: OptionalExplicitParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let entity_type = context.get_entity_type(type_label.name).await?;
        let actuals: Vec<ScopedLabel> =
            entity_type.get_plays(tx, optional_explicit.transitivity)?.map_ok(|rt| rt.label).try_collect().await?;
        for role_label in iter_table(step) {
            let role_label = role_label.parse::<ScopedLabelParam>().unwrap().label;
            containment.assert(&actuals, &role_label);
        }
        Ok(())
    }
}
