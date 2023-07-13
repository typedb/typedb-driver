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

use crate::{
    common::{box_stream, stream::BoxStream},
    concept::{
        Annotation, Attribute, AttributeType, Entity, EntityType, Relation, RelationType, RoleType, RootThingType,
        Thing, ThingType, Transitivity, Value, ValueType,
    },
    Result, Transaction,
};

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait ThingTypeAPI: Sync + Send {
    fn label(&self) -> &str;

    fn is_abstract(&self) -> bool;

    fn is_root(&self) -> bool;

    fn to_thing_type_cloned(&self) -> ThingType;

    #[cfg(not(feature = "sync"))]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg(feature = "sync")]
    fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_delete(self.to_thing_type_cloned()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_label(&mut self, transaction: &Transaction<'_>, new_label: String) -> Result {
        transaction.concept().transaction_stream.thing_type_set_label(self.to_thing_type_cloned(), new_label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_abstract(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_set_abstract(self.to_thing_type_cloned()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_abstract(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_unset_abstract(self.to_thing_type_cloned()).await
    }

    fn get_owns(
        &self,
        transaction: &Transaction<'_>,
        value_type: Option<ValueType>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<Result<AttributeType>>> {
        transaction
            .concept()
            .transaction_stream
            .thing_type_get_owns(self.to_thing_type_cloned(), value_type, transitivity, annotations)
            .map(box_stream)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_owns_overridden(
        &self,
        transaction: &Transaction<'_>,
        overridden_attribute_type: AttributeType,
    ) -> Result<Option<AttributeType>> {
        transaction
            .concept()
            .transaction_stream
            .thing_type_get_owns_overridden(self.to_thing_type_cloned(), overridden_attribute_type)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_owns(
        &mut self,
        transaction: &Transaction<'_>,
        attribute_type: AttributeType,
        overridden_attribute_type: Option<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> Result {
        transaction
            .concept()
            .transaction_stream
            .thing_type_set_owns(self.to_thing_type_cloned(), attribute_type, overridden_attribute_type, annotations)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_owns(&mut self, transaction: &Transaction<'_>, attribute_type: AttributeType) -> Result {
        transaction
            .concept()
            .transaction_stream
            .thing_type_unset_owns(self.to_thing_type_cloned(), attribute_type)
            .await
    }

    fn get_plays(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<RoleType>>> {
        transaction
            .concept()
            .transaction_stream
            .thing_type_get_plays(self.to_thing_type_cloned(), transitivity)
            .map(box_stream)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_plays_overridden(
        &self,
        transaction: &Transaction<'_>,
        overridden_role_type: RoleType,
    ) -> Result<Option<RoleType>> {
        transaction
            .concept()
            .transaction_stream
            .thing_type_get_plays_overridden(self.to_thing_type_cloned(), overridden_role_type)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_plays(
        &mut self,
        transaction: &Transaction<'_>,
        role_type: RoleType,
        overridden_role_type: Option<RoleType>,
    ) -> Result {
        transaction
            .concept()
            .transaction_stream
            .thing_type_set_plays(self.to_thing_type_cloned(), role_type, overridden_role_type)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_plays(&mut self, transaction: &Transaction<'_>, role_type: RoleType) -> Result {
        transaction.concept().transaction_stream.thing_type_unset_plays(self.to_thing_type_cloned(), role_type).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_syntax(&self, transaction: &Transaction<'_>) -> Result<String> {
        transaction.concept().transaction_stream.thing_type_get_syntax(self.to_thing_type_cloned()).await
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl ThingTypeAPI for RootThingType {
    fn label(&self) -> &str {
        Self::LABEL
    }

    fn is_abstract(&self) -> bool {
        true
    }

    fn is_root(&self) -> bool {
        true
    }

    fn to_thing_type_cloned(&self) -> ThingType {
        ThingType::RootThingType(self.clone())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, _transaction: &Transaction<'_>) -> Result<bool> {
        Ok(false)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl ThingTypeAPI for EntityType {
    fn label(&self) -> &str {
        &self.label
    }

    fn is_abstract(&self) -> bool {
        self.is_abstract
    }

    fn is_root(&self) -> bool {
        self.is_root
    }

    fn to_thing_type_cloned(&self) -> ThingType {
        ThingType::EntityType(self.clone())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction.concept().transaction_stream.get_entity_type(self.label().to_owned()).await.map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait EntityTypeAPI: ThingTypeAPI + Clone + Into<EntityType> {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn create(&self, transaction: &Transaction<'_>) -> Result<Entity> {
        transaction.concept().transaction_stream.entity_type_create(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<EntityType> {
        transaction.concept().transaction_stream.entity_type_get_supertype(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: EntityType) -> Result {
        transaction.concept().transaction_stream.entity_type_set_supertype(self.clone().into(), supertype).await
    }

    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<EntityType>>> {
        transaction.concept().transaction_stream.entity_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    fn get_subtypes(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<EntityType>>> {
        transaction
            .concept()
            .transaction_stream
            .entity_type_get_subtypes(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_instances(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<Entity>>> {
        transaction
            .concept()
            .transaction_stream
            .entity_type_get_instances(self.clone().into(), transitivity)
            .map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl EntityTypeAPI for EntityType {}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl ThingTypeAPI for RelationType {
    fn label(&self) -> &str {
        &self.label
    }

    fn is_abstract(&self) -> bool {
        self.is_abstract
    }

    fn is_root(&self) -> bool {
        self.is_root
    }

    fn to_thing_type_cloned(&self) -> ThingType {
        ThingType::RelationType(self.clone())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction
            .concept()
            .transaction_stream
            .get_relation_type(self.label().to_owned())
            .await
            .map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait RelationTypeAPI: ThingTypeAPI + Clone + Into<RelationType> {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn create(&self, transaction: &Transaction<'_>) -> Result<Relation> {
        transaction.concept().transaction_stream.relation_type_create(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<RelationType> {
        transaction.concept().transaction_stream.relation_type_get_supertype(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: RelationType) -> Result {
        transaction.concept().transaction_stream.relation_type_set_supertype(self.clone().into(), supertype).await
    }

    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RelationType>>> {
        transaction.concept().transaction_stream.relation_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    fn get_subtypes(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<RelationType>>> {
        transaction
            .concept()
            .transaction_stream
            .relation_type_get_subtypes(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_instances(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<Relation>>> {
        transaction
            .concept()
            .transaction_stream
            .relation_type_get_instances(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_relates(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<RoleType>>> {
        transaction
            .concept()
            .transaction_stream
            .relation_type_get_relates(self.clone().into(), transitivity)
            .map(box_stream)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_relates_for_role_label(
        &self,
        transaction: &Transaction<'_>,
        role_label: String,
    ) -> Result<Option<RoleType>> {
        transaction
            .concept()
            .transaction_stream
            .relation_type_get_relates_for_role_label(self.clone().into(), role_label)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_relates_overridden(
        &self,
        transaction: &Transaction<'_>,
        overridden_role_label: String,
    ) -> Result<Option<RoleType>> {
        transaction
            .concept()
            .transaction_stream
            .relation_type_get_relates_overridden(self.clone().into(), overridden_role_label)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_relates(
        &mut self,
        transaction: &Transaction<'_>,
        role_label: String,
        overridden_role_label: Option<String>,
    ) -> Result {
        transaction
            .concept()
            .transaction_stream
            .relation_type_set_relates(self.clone().into(), role_label, overridden_role_label)
            .await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_relates(&mut self, transaction: &Transaction<'_>, role_label: String) -> Result {
        transaction.concept().transaction_stream.relation_type_unset_relates(self.clone().into(), role_label).await
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl RelationTypeAPI for RelationType {}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl ThingTypeAPI for AttributeType {
    fn label(&self) -> &str {
        &self.label
    }

    fn is_abstract(&self) -> bool {
        self.is_abstract
    }

    fn is_root(&self) -> bool {
        self.is_root
    }

    fn to_thing_type_cloned(&self) -> ThingType {
        ThingType::AttributeType(self.clone())
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction
            .concept()
            .transaction_stream
            .get_attribute_type(self.label().to_owned())
            .await
            .map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait AttributeTypeAPI: ThingTypeAPI + Clone + Into<AttributeType> {
    fn value_type(&self) -> ValueType;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn put(&self, transaction: &Transaction<'_>, value: Value) -> Result<Attribute> {
        transaction.concept().transaction_stream.attribute_type_put(self.clone().into(), value).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get(&self, transaction: &Transaction<'_>, value: Value) -> Result<Option<Attribute>> {
        transaction.concept().transaction_stream.attribute_type_get(self.clone().into(), value).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<AttributeType> {
        transaction.concept().transaction_stream.attribute_type_get_supertype(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: AttributeType) -> Result {
        transaction.concept().transaction_stream.attribute_type_set_supertype(self.clone().into(), supertype).await
    }

    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<AttributeType>>> {
        transaction.concept().transaction_stream.attribute_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    fn get_subtypes(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<AttributeType>>> {
        // FIXME when None?
        transaction
            .concept()
            .transaction_stream
            .attribute_type_get_subtypes(self.clone().into(), transitivity, Some(self.value_type()))
            .map(box_stream)
    }

    fn get_subtypes_with_value_type(
        &self,
        transaction: &Transaction<'_>,
        value_type: ValueType,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<AttributeType>>> {
        transaction
            .concept()
            .transaction_stream
            .attribute_type_get_subtypes(self.clone().into(), transitivity, Some(value_type))
            .map(box_stream)
    }

    fn get_instances(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<Attribute>>> {
        transaction
            .concept()
            .transaction_stream
            .attribute_type_get_instances(self.clone().into(), transitivity, Some(self.value_type()))
            .map(box_stream)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_regex(&self, transaction: &Transaction<'_>) -> Result<Option<String>> {
        transaction.concept().transaction_stream.attribute_type_get_regex(self.clone().into()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_regex(&self, transaction: &Transaction<'_>, regex: String) -> Result {
        transaction.concept().transaction_stream.attribute_type_set_regex(self.clone().into(), regex).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_regex(&self, transaction: &Transaction<'_>) -> Result {
        self.set_regex(transaction, String::new()).await
    }

    fn get_owners(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<Result<ThingType>>> {
        transaction
            .concept()
            .transaction_stream
            .attribute_type_get_owners(self.clone().into(), transitivity, annotations)
            .map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl AttributeTypeAPI for AttributeType {
    fn value_type(&self) -> ValueType {
        self.value_type
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait RoleTypeAPI: Clone + Into<RoleType> + Sync + Send {
    fn is_abstract(&self) -> bool;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(&self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.role_type_delete(self.clone().into()).await
    }

    #[cfg(not(feature = "sync"))]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg(feature = "sync")]
    fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg(not(feature = "sync"))]
    async fn get_relation_type(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>>;

    #[cfg(feature = "sync")]
    fn get_relation_type(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>>;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_label(&self, transaction: &Transaction<'_>, new_label: String) -> Result {
        transaction.concept().transaction_stream.role_type_set_label(self.clone().into(), new_label).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<RoleType> {
        transaction.concept().transaction_stream.role_type_get_supertype(self.clone().into()).await
    }

    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.role_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    fn get_subtypes(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<RoleType>>> {
        transaction
            .concept()
            .transaction_stream
            .role_type_get_subtypes(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_relation_types(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RelationType>>> {
        transaction.concept().transaction_stream.role_type_get_relation_types(self.clone().into()).map(box_stream)
    }

    fn get_player_types(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<ThingType>>> {
        transaction
            .concept()
            .transaction_stream
            .role_type_get_player_types(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_relation_instances(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<Relation>>> {
        transaction
            .concept()
            .transaction_stream
            .role_type_get_relation_instances(self.clone().into(), transitivity)
            .map(box_stream)
    }

    fn get_player_instances(
        &self,
        transaction: &Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<Result<Thing>>> {
        transaction
            .concept()
            .transaction_stream
            .role_type_get_player_instances(self.clone().into(), transitivity)
            .map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl RoleTypeAPI for RoleType {
    fn is_abstract(&self) -> bool {
        self.is_abstract
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_relation_type(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>> {
        transaction.concept().transaction_stream.get_relation_type(self.label.scope.clone()).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        if let Some(relation_type) = self.get_relation_type(transaction).await? {
            relation_type
                .get_relates_for_role_label(transaction, self.label.name.clone())
                .await
                .map(|res| res.is_none())
        } else {
            Ok(false)
        }
    }
}
