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
    concept::{ScopedLabel, Transitivity},
    transaction::concept::api::{AttributeTypeAPI, RelationTypeAPI, ThingTypeAPI},
    Result as TypeDBResult,
};

use crate::{
    assert_err,
    behaviour::{
        parameter::{
            ContainmentParam, LabelParam, OptionalAnnotationsParam, OptionalExplicitParam, OptionalOverrideLabelParam,
            OptionalOverrideScopedLabelParam, ScopedLabelParam, ValueTypeParam,
        },
        util::iter_table,
        Context,
    },
    generic_step_impl,
};

generic_step_impl! {
    #[step(expr = "put attribute type: {label}, with value type: {value_type}")]
    async fn put_attribute_type(context: &mut Context, type_label: LabelParam, value_type: ValueTypeParam) {
        context.transaction().concept().put_attribute_type(type_label.name, value_type.value_type).await.unwrap();
    }

    #[step(expr = "delete attribute type: {label}")]
    async fn delete_attribute_type(context: &mut Context, type_label: LabelParam) -> TypeDBResult {
        let tx = context.transaction();
        context.get_attribute_type(type_label.name).await?.delete(tx).await
    }

    #[step(expr = "delete attribute type: {label}; throws exception")]
    async fn delete_attribute_type_throws(context: &mut Context, type_label: LabelParam) {
        assert_err!(delete_attribute_type(context, type_label).await);
    }

    #[step(expr = r"attribute\(( ){label}( )\) is null: {word}")]
    async fn attribute_type_is_null(context: &mut Context, type_label: LabelParam, is_null: bool) -> TypeDBResult {
        let res = context.transaction().concept().get_attribute_type(type_label.name).await?;
        assert_eq!(res.is_none(), is_null, "{res:?}");
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) set label: {label}")]
    async fn attribute_type_set_label(
        context: &mut Context,
        type_label: LabelParam,
        new_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        context.get_attribute_type(type_label.name).await?.set_label(tx, new_label.name).await
    }

    #[step(expr = r"attribute\(( ){label}( )\) get label: {label}")]
    async fn attribute_type_get_label(
        context: &mut Context,
        type_label: LabelParam,
        get_label: LabelParam,
    ) -> TypeDBResult {
        assert_eq!(context.get_attribute_type(type_label.name).await?.label, get_label.name);
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) set abstract: {word}")]
    async fn attribute_type_set_abstract(
        context: &mut Context,
        type_label: LabelParam,
        is_abstract: bool,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut attribute_type = context.get_attribute_type(type_label.name).await?;
        if is_abstract {
            attribute_type.set_abstract(tx).await
        } else {
            attribute_type.unset_abstract(tx).await
        }
    }

    #[step(expr = r"attribute\(( ){label}( )\) set abstract: {word}; throws exception")]
    async fn attribute_type_set_abstract_throws(context: &mut Context, type_label: LabelParam, is_abstract: bool) {
        assert_err!(attribute_type_set_abstract(context, type_label, is_abstract).await);
    }

    #[step(expr = r"attribute\(( ){label}( )\) is abstract: {word}")]
    async fn attribute_type_is_abstract(
        context: &mut Context,
        type_label: LabelParam,
        is_abstract: bool,
    ) -> TypeDBResult {
        assert_eq!(context.get_attribute_type(type_label.name).await?.is_abstract, is_abstract);
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) set supertype: {label}")]
    async fn attribute_type_set_supertype(
        context: &mut Context,
        type_label: LabelParam,
        supertype_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let supertype = context.get_attribute_type(supertype_label.name).await?;
        context.get_attribute_type(type_label.name).await?.set_supertype(tx, supertype).await
    }

    #[step(expr = r"attribute\(( ){label}( )\) set supertype: {label}; throws exception")]
    async fn attribute_type_set_supertype_throws(
        context: &mut Context,
        type_label: LabelParam,
        supertype_label: LabelParam,
    ) {
        assert_err!(attribute_type_set_supertype(context, type_label, supertype_label).await)
    }

    #[step(expr = r"attribute\(( ){label}( )\) get supertype: {label}")]
    async fn attribute_type_get_supertype(
        context: &mut Context,
        type_label: LabelParam,
        supertype: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        assert_eq!(context.get_attribute_type(type_label.name).await?.get_supertype(tx).await?.label, supertype.name);
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) get supertypes {containment}:")]
    async fn attribute_type_get_supertypes(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<String> = attribute_type.get_supertypes(tx)?.map_ok(|at| at.label).try_collect().await?;
        for supertype in iter_table(step) {
            containment.assert(&actuals, supertype);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) get subtypes {containment}:")]
    async fn attribute_type_get_subtypes(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals = attribute_type
            .get_subtypes(tx, Transitivity::Transitive)?
            .map_ok(|at| at.label)
            .try_collect::<Vec<_>>()
            .await?;
        for subtype in iter_table(step) {
            containment.assert(&actuals, subtype);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) set owns attribute type: {label}{optional_override_label}{annotations}")]
    async fn attribute_type_set_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
        overridden_attribute_type_label: OptionalOverrideLabelParam,
        annotations: OptionalAnnotationsParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut attribute_type = context.get_attribute_type(type_label.name).await?;
        let owned_attribute_type = context.get_attribute_type(owned_attribute_type_label.name).await?;
        // FIXME cleanup
        let overridden_attribute_type = if let Some(label) = overridden_attribute_type_label.name {
            Some(context.get_attribute_type(label).await?)
        } else {
            None
        };
        attribute_type.set_owns(tx, owned_attribute_type, overridden_attribute_type, annotations.annotations).await
    }

    #[step(
        expr = r"attribute\(( ){label}( )\) set owns attribute type: {label}{optional_override_label}{annotations}; throws exception"
    )]
    async fn attribute_type_set_owns_attribute_type_throws(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
        overridden_attribute_type_label: OptionalOverrideLabelParam,
        annotations: OptionalAnnotationsParam,
    ) {
        assert_err!(
            attribute_type_set_owns_attribute_type(
                context,
                type_label,
                owned_attribute_type_label,
                overridden_attribute_type_label,
                annotations
            )
            .await
        );
    }

    #[step(expr = r"attribute\(( ){label}( )\) unset owns attribute type: {label}")]
    async fn attribute_type_unset_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let mut attribute_type = context.get_attribute_type(type_label.name).await?;
        let owned_attribute_type = context.get_attribute_type(owned_attribute_type_label.name).await?;
        attribute_type.unset_owns(tx, owned_attribute_type).await
    }

    #[step(expr = r"attribute\(( ){label}( )\) unset owns attribute type: {label}; throws exception")]
    async fn attribute_type_unset_owns_attribute_type_throws(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
    ) {
        assert_err!(attribute_type_unset_owns_attribute_type(context, type_label, owned_attribute_type_label).await);
    }

    #[step(
        expr = r"attribute\(( ){label}( )\) get owns{optional_explicit} attribute types{annotations}(;) {containment}:"
    )]
    async fn attribute_type_get_owns_attribute_types_contain(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        optional_explicit: OptionalExplicitParam,
        annotations: OptionalAnnotationsParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<String> = attribute_type
            .get_owns(tx, None, optional_explicit.transitivity, annotations.annotations)?
            .map_ok(|at| at.label)
            .try_collect()
            .await?;
        for attribute in iter_table(step) {
            containment.assert(&actuals, attribute);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) get owns overridden attribute\(( ){label}( )\) is null: {word}")]
    async fn attribute_type_get_owns_attribute_type(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
        is_null: bool,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let owned_attribute_type = context.get_attribute_type(owned_attribute_type_label.name).await?;
        let res = attribute_type.get_owns_overridden(tx, owned_attribute_type).await?;
        assert_eq!(res.is_none(), is_null);
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) get owns overridden attribute\(( ){label}( )\) get label: {label}")]
    async fn attribute_type_get_owns_attribute_type_label(
        context: &mut Context,
        type_label: LabelParam,
        owned_attribute_type_label: LabelParam,
        overridden_label: LabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let owned_attribute_type = context.get_attribute_type(owned_attribute_type_label.name).await?;
        let res = attribute_type.get_owns_overridden(tx, owned_attribute_type).await?;
        assert_eq!(res.map(|at| at.label), Some(overridden_label.name));
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )\) set plays role: {scoped_label}{optional_override_scoped_label}")]
    async fn attribute_type_set_plays_role(
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
        let mut attribute_type = context.get_attribute_type(type_label.name).await?;
        attribute_type.set_plays(tx, role, overridden_role).await
    }

    #[step(
        expr = r"attribute\(( ){label}( )\) set plays role: {scoped_label}{optional_override_scoped_label}; throws exception"
    )]
    async fn attribute_type_set_plays_role_throws(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
        overridden_role_label: OptionalOverrideScopedLabelParam,
    ) {
        assert_err!(attribute_type_set_plays_role(context, type_label, role_label, overridden_role_label).await);
    }

    #[step(expr = r"attribute\(( ){label}( )\) unset plays role: {scoped_label}")]
    async fn attribute_type_unset_plays_role(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let role_label = role_label.label;
        let relation_type = context.get_relation_type(role_label.scope).await?;
        let role = relation_type.get_relates_for_role_label(tx, role_label.name).await?.unwrap();
        let mut attribute_type = context.get_attribute_type(type_label.name).await?;
        attribute_type.unset_plays(tx, role).await
    }

    #[step(expr = r"attribute\(( ){label}( )\) unset plays role: {scoped_label}; throws exception")]
    async fn attribute_type_unset_plays_role_throws(
        context: &mut Context,
        type_label: LabelParam,
        role_label: ScopedLabelParam,
    ) {
        assert_err!(attribute_type_unset_plays_role(context, type_label, role_label).await);
    }

    #[step(expr = r"attribute\(( ){label}( )\) get playing roles{optional_explicit} {containment}:")]
    async fn attribute_type_get_playing_roles_contain(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        optional_explicit: OptionalExplicitParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<ScopedLabel> =
            attribute_type.get_plays(tx, optional_explicit.transitivity)?.map_ok(|rt| rt.label).try_collect().await?;
        for role_label in iter_table(step) {
            let role_label = role_label.parse::<ScopedLabelParam>().unwrap().label;
            containment.assert(&actuals, &role_label);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) get subtypes {containment}:")]
    async fn attribute_type_get_subtypes_as_value_type(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<String> = attribute_type
            .get_subtypes_with_value_type(tx, value_type.value_type, Transitivity::Transitive)?
            .map_ok(|at| at.label)
            .try_collect()
            .await?;
        for subtype in iter_table(step) {
            containment.assert(&actuals, subtype);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) set regex: {}")]
    async fn attribute_type_set_regex(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        regex: String,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        attribute_type.set_regex(tx, regex).await
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) set regex: {}; throws exception")]
    async fn attribute_type_set_regex_throws(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        regex: String,
    ) {
        assert_err!(attribute_type_set_regex(context, type_label, value_type, regex).await)
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) unset regex")]
    async fn attribute_type_unset_regex(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        attribute_type.unset_regex(tx).await
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) get regex: {}")]
    async fn attribute_type_get_regex(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
        regex: String,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        assert_eq!(attribute_type.get_regex(tx).await?, Some(regex));
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) as\(( ){value_type}( )) does not have any regex")]
    async fn attribute_type_no_regex(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        assert!(attribute_type.get_regex(tx).await?.is_none());
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) get owners{optional_explicit}{annotations}(;) {containment}:")]
    async fn attribute_type_get_owners(
        context: &mut Context,
        step: &Step,
        type_label: LabelParam,
        optional_explicit: OptionalExplicitParam,
        annotations: OptionalAnnotationsParam,
        containment: ContainmentParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        let actuals: Vec<String> = attribute_type
            .get_owners(tx, optional_explicit.transitivity, annotations.annotations)?
            .map_ok(|t| t.label().to_owned())
            .try_collect()
            .await?;
        for owner in iter_table(step) {
            containment.assert(&actuals, owner);
        }
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) get supertype value type: {value_type}")]
    async fn attribute_type_get_supertype_value_type(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
    ) -> TypeDBResult {
        let tx = context.transaction();
        let supertype = context.get_attribute_type(type_label.name).await?.get_supertype(tx).await?;
        assert_eq!(supertype.value_type, value_type.value_type);
        Ok(())
    }

    #[step(expr = r"attribute\(( ){label}( )) get value type: {value_type}")]
    async fn attribute_type_get_value_type(
        context: &mut Context,
        type_label: LabelParam,
        value_type: ValueTypeParam,
    ) -> TypeDBResult {
        let attribute_type = context.get_attribute_type(type_label.name).await?;
        assert_eq!(attribute_type.value_type, value_type.value_type);
        Ok(())
    }
}
