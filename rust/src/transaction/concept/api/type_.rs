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
    /// Retrieves the unique label of the type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.label()
    /// ```
    fn label(&self) -> &str;

    /// Checks if the type is prevented from having data instances (i.e. `abstract`).
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_abstract()
    /// ```
    fn is_abstract(&self) -> bool;

    /// Checks if the type is a root type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_root()
    /// ```
    fn is_root(&self) -> bool;

    fn to_thing_type_cloned(&self) -> ThingType;

    /// Checks if this type is deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_deleted(transaction).await
    /// ```
    #[cfg(not(feature = "sync"))]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    /// Checks if this type is deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_deleted(transaction)
    /// ```
    #[cfg(feature = "sync")]
    fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    /// Deletes this type from the database.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.delete(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_delete(self.to_thing_type_cloned()).await
    }

    /// Renames the label of the type. The new label must remain unique.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `new_label` -- The new `Label` to be given to the type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.set_label(transaction, new_label).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_label(&mut self, transaction: &Transaction<'_>, new_label: String) -> Result {
        transaction.concept().transaction_stream.thing_type_set_label(self.to_thing_type_cloned(), new_label).await
    }

    /// Set a type to be abstract, meaning it cannot have instances.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.set_abstract(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_abstract(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_set_abstract(self.to_thing_type_cloned()).await
    }

    /// Set a type to be non-abstract, meaning it can have instances.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.unset_abstract(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_abstract(&mut self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_type_unset_abstract(self.to_thing_type_cloned()).await
    }

    /// Retrieves `AttributeType` that the instances of this `ThingType` are allowed to own
    /// directly or via inheritance.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `value_type` -- If specified, only attribute types of this `ValueType` will be retrieved.
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and inherited ownership,
    /// `Transitivity.EXPLICIT` for direct ownership only
    /// * `annotations` -- Only retrieve attribute types owned with annotations.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.get_owns(transaction, value_type, transitivity, annotations)
    /// ```
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

    /// Retrieves an `AttributeType`, ownership of which is overridden for this `ThingType`
    /// by a given `attribute_type`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `overridden_attribute_type` -- The `AttributeType` that overrides requested `AttributeType`
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.get_owns_overridden(transaction, overridden_attribute_type).await
    /// ```
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

    /// Allows the instances of this `ThingType` to own the given `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `attribute_type` -- The `AttributeType` to be owned by the instances of this type.
    /// * `overridden_attribute_type` -- The `AttributeType` that this attribute ownership
    /// overrides, if applicable.
    /// * `annotations` -- Adds annotations to the ownership.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.set_owns(transaction, attribute_type, overridden_attribute_type, annotations).await
    /// ```
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

    /// Disallows the instances of this `ThingType` from owning the given `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `attribute_type` -- The `AttributeType` to not be owned by the type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.unset_owns(transaction, attribute_type).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_owns(&mut self, transaction: &Transaction<'_>, attribute_type: AttributeType) -> Result {
        transaction
            .concept()
            .transaction_stream
            .thing_type_unset_owns(self.to_thing_type_cloned(), attribute_type)
            .await
    }

    /// Retrieves all direct and inherited (or direct only) roles that are allowed to be played
    /// by the instances of this `ThingType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- transitivity: `Transitivity.TRANSITIVE` for direct and indirect playing,
    /// `Transitivity.EXPLICIT` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.get_plays(transaction, transitivity)
    /// ```
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

    /// Retrieves a `RoleType` that is overridden by the given `role_type` for this `ThingType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `overridden_role_type` -- The `RoleType` that overrides an inherited role
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.get_plays_overridden(transaction, overridden_role_type).await
    /// ```
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

    /// Allows the instances of this `ThingType` to play the given role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_type` -- The role to be played by the instances of this type
    /// * `overridden_role_type` -- The role type that this role overrides, if applicable
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.set_plays(transaction, role_type, overridden_role_type)
    /// ```
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

    /// Disallows the instances of this `ThingType` from playing the given role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_type` -- The role to not be played by the instances of this type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.unset_plays(transaction, role_type).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_plays(&mut self, transaction: &Transaction<'_>, role_type: RoleType) -> Result {
        transaction.concept().transaction_stream.thing_type_unset_plays(self.to_thing_type_cloned(), role_type).await
    }

    /// Produces a pattern for creating this `ThingType` in a `define` query.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.get_syntax(transaction).await
    /// ```
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
    /// Creates and returns a new instance of this `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.create(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn create(&self, transaction: &Transaction<'_>) -> Result<Entity> {
        transaction.concept().transaction_stream.entity_type_create(self.clone().into()).await
    }

    /// Retrieves the most immediate supertype of the `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_supertype(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<Option<EntityType>> {
        transaction.concept().transaction_stream.entity_type_get_supertype(self.clone().into()).await
    }

    /// Sets the supplied `EntityType` as the supertype of the current `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `supertype` -- The `EntityType` to set as the supertype of this `EntityType`
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.set_supertype(transaction, supertype).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: EntityType) -> Result {
        transaction.concept().transaction_stream.entity_type_set_supertype(self.clone().into(), supertype).await
    }

    /// Retrieves all supertypes of the `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_supertypes(transaction)
    /// ```
    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<EntityType>>> {
        transaction.concept().transaction_stream.entity_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_subtypes(transaction, transitivity)
    /// ```
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

    /// Retrieves all direct and indirect (or direct only) `Entity` objects that are instances
    /// of this `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect instances,
    /// `Transitivity.EXPLICIT` for direct instances only
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_instances(transaction, transitivity)
    /// ```
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
    /// Creates and returns an instance of this `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.create(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn create(&self, transaction: &Transaction<'_>) -> Result<Relation> {
        transaction.concept().transaction_stream.relation_type_create(self.clone().into()).await
    }

    /// Retrieves the most immediate supertype of the `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_supertype(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>> {
        transaction.concept().transaction_stream.relation_type_get_supertype(self.clone().into()).await
    }

    /// Sets the supplied `RelationType` as the supertype of the current `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `supertype` -- The `RelationType` to set as the supertype of this `RelationType`
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.set_supertype(transaction, supertype).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: RelationType) -> Result {
        transaction.concept().transaction_stream.relation_type_set_supertype(self.clone().into(), supertype).await
    }

    /// Retrieves all supertypes of the `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_supertypes(transaction)
    /// ```
    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RelationType>>> {
        transaction.concept().transaction_stream.relation_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_subtypes(transaction, transitivity)
    /// ```
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

    /// Retrieves all direct and indirect (or direct only) `Relation`s that are instances
    /// of this `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect instances,
    /// `Transitivity.EXPLICIT` for direct relates only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_instances(transaction, transitivity)
    /// ```
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

    /// Retrieves roles that this `RelationType` relates to directly or via inheritance.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and inherited relates,
    /// `Transitivity.EXPLICIT` for direct relates only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_relates(transaction, transitivity)
    /// ```
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

    /// Retrieves role with a given `role_label` that this `RelationType` relates to.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_label` -- Label of the role we wish to retrieve
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_relates_for_role_label(transaction, role_label).await
    /// ```
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

    /// Retrieves a `RoleType` that is overridden by the role with the `overridden_role_label`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `overridden_role_label` -- Label of the role that overrides an inherited role
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_relates_overridden(transaction, overridden_role_label).await
    /// ```
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

    /// Sets the new role that this `RelationType` relates to. If we are setting an overriding
    /// type this way, we have to also pass the overridden type as a second argument.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_label` -- The new role for the `RelationType` to relate to
    /// * `overridden_role_label` -- The label being overridden, if applicable
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.set_relates(transaction, role_label, overridden_role_label).await
    /// ```
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

    /// Disallows this `RelationType` from relating to the given role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_label` -- The role to not relate to the relation type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.unset_relates(transaction, role_label).await
    /// ```
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
    /// Retrieves the `ValueType` of this `AttributeType`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.value_type()
    /// ```
    fn value_type(&self) -> ValueType;

    /// Adds and returns an `Attribute` of this `AttributeType` with the given value.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `value` -- New `Attribute`’s value
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.put(transaction, value).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn put(&self, transaction: &Transaction<'_>, value: Value) -> Result<Attribute> {
        transaction.concept().transaction_stream.attribute_type_put(self.clone().into(), value).await
    }

    /// Retrieves an `Attribute` of this `AttributeType` with the given value if such `Attribute`
    /// exists. Otherwise, returns `None`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `value` -- `Attribute`’s value
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get(transaction, value)
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get(&self, transaction: &Transaction<'_>, value: Value) -> Result<Option<Attribute>> {
        transaction.concept().transaction_stream.attribute_type_get(self.clone().into(), value).await
    }

    /// Retrieves the most immediate supertype of this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_supertype(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<Option<AttributeType>> {
        transaction.concept().transaction_stream.attribute_type_get_supertype(self.clone().into()).await
    }

    /// Sets the supplied `AttributeType` as the supertype of the current `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `supertype` -- The `AttributeType` to set as the supertype of this `AttributeType`
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.set_supertype(transaction, supertype).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_supertype(&mut self, transaction: &Transaction<'_>, supertype: AttributeType) -> Result {
        transaction.concept().transaction_stream.attribute_type_set_supertype(self.clone().into(), supertype).await
    }

    /// Retrieves all supertypes of this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_supertypes(transaction)
    /// ```
    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<AttributeType>>> {
        transaction.concept().transaction_stream.attribute_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_subtypes(transaction, transitivity)
    /// ```
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

    /// Retrieves all direct and indirect (or direct only) subtypes of this `AttributeType`
    /// with given `ValueType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `value_type` -- `ValueType`  for retrieving subtypes
    /// * `transitivity` -- `Transitivity.TRANSITIVE`  for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT`  for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_subtypes_with_value_type(transaction, value_type, transitivity)
    /// ```
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

    /// Retrieves all direct and indirect (or direct only) `Attributes`  that are instances
    /// of this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE`  for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT`  for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_instances(transaction, transitivity)
    /// ```
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

    /// Retrieves the regular expression that is defined for this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_regex(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_regex(&self, transaction: &Transaction<'_>) -> Result<Option<String>> {
        transaction.concept().transaction_stream.attribute_type_get_regex(self.clone().into()).await
    }

    /// Sets a regular expression as a constraint for this `AttributeType`. `Values` of all
    /// `Attribute`s of this type (inserted earlier or later) should match this regex.
    ///
    /// Can only be applied for `AttributeType`s with a `string` value type.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `regex` -- Regular expression
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.set_regex(transaction, regex).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_regex(&self, transaction: &Transaction<'_>, regex: String) -> Result {
        transaction.concept().transaction_stream.attribute_type_set_regex(self.clone().into(), regex).await
    }

    /// Removes the regular expression that is defined for this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.unset_regex(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_regex(&self, transaction: &Transaction<'_>) -> Result {
        self.set_regex(transaction, String::new()).await
    }

    /// Retrieve all `Things` that own an attribute of this `AttributeType`
    /// and have all given `Annotation`s.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and inherited ownership,
    /// `Transitivity.EXPLICIT` for direct ownership only
    /// * `annotations` -- Only retrieve `ThingTypes` that have an attribute of this
    /// `AttributeType` with all given `Annotation`s
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_owners(transaction, transitivity, annotations)
    /// ```
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
    /// Checks if the type is prevented from having data instances (i.e., `abstract`).
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.is_abstract()
    /// ```
    fn is_abstract(&self) -> bool;

    /// Deletes this type from the database.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.delete(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(&self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.role_type_delete(self.clone().into()).await
    }

    /// Checks if this type is deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.is_deleted(transaction).await
    /// ```
    #[cfg(not(feature = "sync"))]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    /// Checks if this type is deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.is_deleted(transaction)
    /// ```
    #[cfg(feature = "sync")]
    fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    /// Retrieves the `RelationType` that this role is directly related to.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_relation_type(transaction).await
    /// ```
    #[cfg(not(feature = "sync"))]
    async fn get_relation_type(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>>;

    /// Retrieves `RelationType`s that this role is related to (directly or indirectly).
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_relation_types(transaction)
    /// ```
    #[cfg(feature = "sync")]
    fn get_relation_type(&self, transaction: &Transaction<'_>) -> Result<Option<RelationType>>;

    /// Renames the label of the type. The new label must remain unique.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `new_label` -- The new `Label` to be given to the type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.set_label(transaction, new_label).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_label(&self, transaction: &Transaction<'_>, new_label: String) -> Result {
        transaction.concept().transaction_stream.role_type_set_label(self.clone().into(), new_label).await
    }

    /// Retrieves the most immediate supertype of the `RoleType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_supertype(transaction).await
    /// ```
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn get_supertype(&self, transaction: &Transaction<'_>) -> Result<Option<RoleType>> {
        transaction.concept().transaction_stream.role_type_get_supertype(self.clone().into()).await
    }

    /// Retrieves all supertypes of the `RoleType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_supertypes(transaction)
    /// ```
    fn get_supertypes(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.role_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `RoleType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect subtypes,
    /// `Transitivity.EXPLICIT` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_subtypes(transaction, transitivity)
    /// ```
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

    /// Retrieves `RelationType`s that this role is related to (directly or indirectly).
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_relation_types(transaction)
    /// ```
    fn get_relation_types(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RelationType>>> {
        transaction.concept().transaction_stream.role_type_get_relation_types(self.clone().into()).map(box_stream)
    }

    /// Retrieves the `ThingType`s whose instances play this role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect playing,
    /// `Transitivity.EXPLICIT` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_player_types(transaction, transitivity)
    /// ```
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

    /// Retrieves the `Relation` instances that this role is related to.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect relation,
    /// `Transitivity.EXPLICIT` for direct relation only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_relation_instances(transaction, transitivity)
    /// ```
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

    /// Retrieves the `Thing` instances that play this role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity.TRANSITIVE` for direct and indirect playing,
    /// `Transitivity.EXPLICIT` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_player_instances(transaction, transitivity)
    /// ```
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
