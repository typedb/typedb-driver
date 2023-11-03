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
    common::{box_promise, box_stream, stream::BoxStream, BoxPromise},
    concept::{
        Annotation, Attribute, AttributeType, Entity, EntityType, Relation, RelationType, RoleType, RootThingType,
        Thing, ThingType, Transitivity, Value, ValueType,
    },
    promisify, resolve, Result, Transaction,
};

pub trait ThingTypeAPI: Sync + Send {
    /// Retrieves the unique label of the type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.label();
    /// ```
    fn label(&self) -> &str;

    /// Checks if the type is prevented from having data instances (i.e. `abstract`).
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_abstract();
    /// ```
    fn is_abstract(&self) -> bool;

    /// Checks if the type is a root type.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing_type.is_root();
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
    #[cfg_attr(feature = "sync", doc = "thing_type.is_deleted(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.is_deleted(transaction).await;")]
    /// ```
    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>>;

    /// Deletes this type from the database.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing_type.delete(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.delete(transaction).await;")]
    /// ```
    fn delete<'tx>(&mut self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_delete(self.to_thing_type_cloned()))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.set_label(transaction, new_label).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.set_label(transaction, new_label).await;")]
    /// ```
    fn set_label<'tx>(&mut self, transaction: &'tx Transaction<'_>, new_label: String) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_set_label(self.to_thing_type_cloned(), new_label))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.set_abstract(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.set_abstract(transaction).await;")]
    /// ```
    fn set_abstract<'tx>(&mut self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_set_abstract(self.to_thing_type_cloned()))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.unset_abstract(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.unset_abstract(transaction).await;")]
    /// ```
    fn unset_abstract<'tx>(&mut self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_unset_abstract(self.to_thing_type_cloned()))
    }

    /// Retrieves `AttributeType` that the instances of this `ThingType` are allowed to own
    /// directly or via inheritance.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `value_type` -- If specified, only attribute types of this `ValueType` will be retrieved.
    /// * `transitivity` -- `Transitivity::Transitive` for direct and inherited ownership,
    /// `Transitivity::Explicit` for direct ownership only
    /// * `annotations` -- Only retrieve attribute types owned with annotations.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(
        feature = "sync",
        doc = "thing_type.get_owns(transaction, Some(value_type), Transitivity::Explicit, vec![Annotation::Key]);"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "thing_type.get_owns(transaction, Some(value_type), Transitivity::Explicit, vec![Annotation::Key]).await;"
    )]
    /// ```
    fn get_owns<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        value_type: Option<ValueType>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<'tx, Result<AttributeType>>> {
        transaction
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
    #[cfg_attr(feature = "sync", doc = "thing_type.get_owns_overridden(transaction, attribute_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.get_owns_overridden(transaction, attribute_type).await;")]
    /// ```
    fn get_owns_overridden<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        overridden_attribute_type: AttributeType,
    ) -> BoxPromise<'tx, Result<Option<AttributeType>>> {
        box_promise(
            transaction
                .transaction_stream
                .thing_type_get_owns_overridden(self.to_thing_type_cloned(), overridden_attribute_type),
        )
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
    #[cfg_attr(
        feature = "sync",
        doc = "thing_type.set_owns(transaction, attribute_type, Some(overridden_type), vec![Annotation::Key]);"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "thing_type.set_owns(transaction, attribute_type, Some(overridden_type), vec![Annotation::Key]).await;"
    )]
    /// ```
    fn set_owns<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        attribute_type: AttributeType,
        overridden_attribute_type: Option<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_set_owns(
            self.to_thing_type_cloned(),
            attribute_type,
            overridden_attribute_type,
            annotations,
        ))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.unset_owns(transaction, attribute_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.unset_owns(transaction, attribute_type).await;")]
    /// ```
    fn unset_owns<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        attribute_type: AttributeType,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_unset_owns(self.to_thing_type_cloned(), attribute_type))
    }

    /// Retrieves all direct and inherited (or direct only) roles that are allowed to be played
    /// by the instances of this `ThingType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect playing,
    /// `Transitivity::Explicit` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing_type.get_plays(transaction, Transitivity::Explicit).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.get_plays(transaction, Transitivity::Explicit).await;")]
    /// ```
    fn get_plays<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<RoleType>>> {
        transaction.transaction_stream.thing_type_get_plays(self.to_thing_type_cloned(), transitivity).map(box_stream)
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
    #[cfg_attr(feature = "sync", doc = "thing_type.get_plays_overridden(transaction, role_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.get_plays_overridden(transaction, role_type).await;")]
    /// ```
    fn get_plays_overridden<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        overridden_role_type: RoleType,
    ) -> BoxPromise<'tx, Result<Option<RoleType>>> {
        box_promise(
            transaction
                .transaction_stream
                .thing_type_get_plays_overridden(self.to_thing_type_cloned(), overridden_role_type),
        )
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
    #[cfg_attr(feature = "sync", doc = "thing_type.set_plays(transaction, role_type, None).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.set_plays(transaction, role_type, None).await;")]
    /// ```
    fn set_plays<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        role_type: RoleType,
        overridden_role_type: Option<RoleType>,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_set_plays(
            self.to_thing_type_cloned(),
            role_type,
            overridden_role_type,
        ))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.unset_plays(transaction, role_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.unset_plays(transaction, role_type).await;")]
    /// ```
    fn unset_plays<'tx>(&mut self, transaction: &'tx Transaction<'_>, role_type: RoleType) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.thing_type_unset_plays(self.to_thing_type_cloned(), role_type))
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
    #[cfg_attr(feature = "sync", doc = "thing_type.get_syntax(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "thing_type.get_syntax(transaction).await;")]
    /// ```
    fn get_syntax<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<String>> {
        box_promise(transaction.transaction_stream.thing_type_get_syntax(self.to_thing_type_cloned()))
    }
}

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

    fn is_deleted<'tx>(&self, _transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>> {
        box_promise(promisify! { Ok(false) })
    }
}

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

    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>> {
        let promise = transaction.transaction_stream.get_entity_type(self.label().to_owned());
        box_promise(promisify! { resolve!(promise).map(|res| res.is_none()) })
    }
}

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
    #[cfg_attr(feature = "sync", doc = "entity_type.create(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "entity_type.create(transaction).await;")]
    /// ```
    fn create<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Entity>> {
        box_promise(transaction.transaction_stream.entity_type_create(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "entity_type.get_supertype(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "entity_type.get_supertype(transaction).await;")]
    /// ```
    fn get_supertype<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Option<EntityType>>> {
        box_promise(transaction.transaction_stream.entity_type_get_supertype(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "entity_type.set_supertype(transaction, super_entity_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "entity_type.set_supertype(transaction, super_entity_type).await;")]
    /// ```
    fn set_supertype<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        supertype: EntityType,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.entity_type_set_supertype(self.clone().into(), supertype))
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
    /// entity_type.get_supertypes(transaction);
    /// ```
    fn get_supertypes<'tx>(&self, transaction: &'tx Transaction<'_>) -> Result<BoxStream<'tx, Result<EntityType>>> {
        transaction.transaction_stream.entity_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect subtypes,
    /// `Transitivity::Explicit` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_subtypes(transaction, Transitivity::Transitive);
    /// ```
    fn get_subtypes<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<EntityType>>> {
        transaction.transaction_stream.entity_type_get_subtypes(self.clone().into(), transitivity).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) `Entity` objects that are instances
    /// of this `EntityType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect instances,
    /// `Transitivity::Explicit` for direct instances only
    ///
    /// # Examples
    ///
    /// ```rust
    /// entity_type.get_instances(transaction, Transitivity::Explicit);
    /// ```
    fn get_instances<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<Entity>>> {
        transaction.transaction_stream.entity_type_get_instances(self.clone().into(), transitivity).map(box_stream)
    }
}

impl EntityTypeAPI for EntityType {}

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

    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>> {
        let promise = transaction.transaction_stream.get_relation_type(self.label().to_owned());
        box_promise(promisify! { resolve!(promise).map(|res| res.is_none()) })
    }
}

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
    #[cfg_attr(feature = "sync", doc = "relation_type.create(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.create(transaction).await;")]
    /// ```
    fn create<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Relation>> {
        box_promise(transaction.transaction_stream.relation_type_create(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "relation_type.get_supertype(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.get_supertype(transaction).await;")]
    /// ```
    fn get_supertype<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Option<RelationType>>> {
        box_promise(transaction.transaction_stream.relation_type_get_supertype(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "relation_type.set_supertype(transaction, super_relation_type).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.set_supertype(transaction, super_relation_type).await;")]
    /// ```
    fn set_supertype<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        supertype: RelationType,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.relation_type_set_supertype(self.clone().into(), supertype))
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
    /// relation_type.get_supertypes(transaction);
    /// ```
    fn get_supertypes<'tx>(&self, transaction: &'tx Transaction<'_>) -> Result<BoxStream<'tx, Result<RelationType>>> {
        transaction.transaction_stream.relation_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect subtypes,
    /// `Transitivity::Explicit` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_subtypes(transaction, Transitivity::Transitive);
    /// ```
    fn get_subtypes<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<RelationType>>> {
        transaction.transaction_stream.relation_type_get_subtypes(self.clone().into(), transitivity).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) `Relation`s that are instances
    /// of this `RelationType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect instances,
    /// `Transitivity::Explicit` for direct relates only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_instances(transaction, Transitivity::Explicit);
    /// ```
    fn get_instances<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<Relation>>> {
        transaction.transaction_stream.relation_type_get_instances(self.clone().into(), transitivity).map(box_stream)
    }

    /// Retrieves roles that this `RelationType` relates to directly or via inheritance.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and inherited relates,
    /// `Transitivity::Explicit` for direct relates only
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation_type.get_relates(transaction, Transitivity::Transitive);
    /// ```
    fn get_relates<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<RoleType>>> {
        transaction.transaction_stream.relation_type_get_relates(self.clone().into(), transitivity).map(box_stream)
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
    #[cfg_attr(feature = "sync", doc = "relation_type.get_relates_for_role_label(transaction, role_label).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.get_relates_for_role_label(transaction, role_label).await;")]
    /// ```
    fn get_relates_for_role_label<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        role_label: String,
    ) -> BoxPromise<'tx, Result<Option<RoleType>>> {
        box_promise(
            transaction.transaction_stream.relation_type_get_relates_for_role_label(self.clone().into(), role_label),
        )
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
    #[cfg_attr(
        feature = "sync",
        doc = "relation_type.get_relates_overridden(transaction, overridden_role_label).resolve();"
    )]
    #[cfg_attr(
        not(feature = "sync"),
        doc = "relation_type.get_relates_overridden(transaction, overridden_role_label).await;"
    )]
    /// ```
    fn get_relates_overridden<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        overridden_role_label: String,
    ) -> BoxPromise<'tx, Result<Option<RoleType>>> {
        box_promise(
            transaction
                .transaction_stream
                .relation_type_get_relates_overridden(self.clone().into(), overridden_role_label),
        )
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
    #[cfg_attr(feature = "sync", doc = "relation_type.set_relates(transaction, role_label, None).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.set_relates(transaction, role_label, None).await;")]
    /// ```
    fn set_relates<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        role_label: String,
        overridden_role_label: Option<String>,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.relation_type_set_relates(
            self.clone().into(),
            role_label,
            overridden_role_label,
        ))
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
    #[cfg_attr(feature = "sync", doc = "relation_type.unset_relates(transaction, role_label).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "relation_type.unset_relates(transaction, role_label).await;")]
    /// ```
    fn unset_relates<'tx>(&mut self, transaction: &'tx Transaction<'_>, role_label: String) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.relation_type_unset_relates(self.clone().into(), role_label))
    }
}

impl RelationTypeAPI for RelationType {}

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

    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>> {
        let promise = transaction.transaction_stream.get_attribute_type(self.label().to_owned());
        box_promise(promisify! { resolve!(promise).map(|res| res.is_none()) })
    }
}

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
    #[cfg_attr(feature = "sync", doc = "attribute = attribute_type.put(transaction, value).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute = attribute_type.put(transaction, value).await;")]
    /// ```
    fn put<'tx>(&self, transaction: &'tx Transaction<'_>, value: Value) -> BoxPromise<'tx, Result<Attribute>> {
        box_promise(transaction.transaction_stream.attribute_type_put(self.clone().into(), value))
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
    #[cfg_attr(feature = "sync", doc = "attribute = attribute_type.get(transaction, value).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute = attribute_type.get(transaction, value).await;")]
    /// ```
    fn get<'tx>(&self, transaction: &'tx Transaction<'_>, value: Value) -> BoxPromise<'tx, Result<Option<Attribute>>> {
        box_promise(transaction.transaction_stream.attribute_type_get(self.clone().into(), value))
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
    #[cfg_attr(feature = "sync", doc = "attribute_type.get_supertype(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute_type.get_supertype(transaction).await;")]
    /// ```
    fn get_supertype<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Option<AttributeType>>> {
        box_promise(transaction.transaction_stream.attribute_type_get_supertype(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "attribute_type.set_supertype(transaction, supertype).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute_type.set_supertype(transaction, supertype).await;")]
    /// ```
    fn set_supertype<'tx>(
        &mut self,
        transaction: &'tx Transaction<'_>,
        supertype: AttributeType,
    ) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.attribute_type_set_supertype(self.clone().into(), supertype))
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
    fn get_supertypes<'tx>(&self, transaction: &'tx Transaction<'_>) -> Result<BoxStream<'tx, Result<AttributeType>>> {
        transaction.transaction_stream.attribute_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of this `AttributeType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect subtypes,
    /// `Transitivity::Explicit` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_subtypes(transaction, transitivity)
    /// ```
    fn get_subtypes<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<AttributeType>>> {
        // FIXME when None?
        transaction
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
    /// * `transitivity` -- `Transitivity::Transitive`  for direct and indirect subtypes,
    /// `Transitivity::Explicit`  for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_subtypes_with_value_type(transaction, value_type, transitivity)
    /// ```
    fn get_subtypes_with_value_type<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        value_type: ValueType,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<AttributeType>>> {
        transaction
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
    /// * `transitivity` -- `Transitivity::Transitive`  for direct and indirect subtypes,
    /// `Transitivity::Explicit`  for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_instances(transaction, transitivity)
    /// ```
    fn get_instances<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<Attribute>>> {
        transaction
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
    #[cfg_attr(feature = "sync", doc = "attribute_type.get_regex(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute_type.get_regex(transaction).await;")]
    /// ```
    fn get_regex<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Option<String>>> {
        box_promise(transaction.transaction_stream.attribute_type_get_regex(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "attribute_type.set_regex(transaction, regex).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute_type.set_regex(transaction, regex).await;")]
    /// ```
    fn set_regex<'tx>(&self, transaction: &'tx Transaction<'_>, regex: String) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.attribute_type_set_regex(self.clone().into(), regex))
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
    #[cfg_attr(feature = "sync", doc = "attribute_type.unset_regex(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "attribute_type.unset_regex(transaction).await;")]
    /// ```
    fn unset_regex<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result> {
        self.set_regex(transaction, String::new())
    }

    /// Retrieve all `Things` that own an attribute of this `AttributeType`
    /// and have all given `Annotation`s.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and inherited ownership,
    /// `Transitivity::Explicit` for direct ownership only
    /// * `annotations` -- Only retrieve `ThingTypes` that have an attribute of this
    /// `AttributeType` with all given `Annotation`s
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute_type.get_owners(transaction, transitivity, annotations)
    /// ```
    fn get_owners<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<'tx, Result<ThingType>>> {
        transaction
            .transaction_stream
            .attribute_type_get_owners(self.clone().into(), transitivity, annotations)
            .map(box_stream)
    }
}

impl AttributeTypeAPI for AttributeType {
    fn value_type(&self) -> ValueType {
        self.value_type
    }
}

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
    #[cfg_attr(feature = "sync", doc = "role_type.delete(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "role_type.delete(transaction).await;")]
    /// ```
    fn delete<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.role_type_delete(self.clone().into()))
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
    #[cfg_attr(feature = "sync", doc = "role_type.is_deleted(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "role_type.is_deleted(transaction).await;")]
    /// ```
    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>>;

    /// Retrieves the `RelationType` that this role is directly related to.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "role_type.get_relation_type(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "role_type.get_relation_type(transaction).await;")]
    /// ```
    fn get_relation_type<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
    ) -> BoxPromise<'tx, Result<Option<RelationType>>>;

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
    #[cfg_attr(feature = "sync", doc = "role_type.set_label(transaction, new_label).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "role_type.set_label(transaction, new_label).await;")]
    /// ```
    fn set_label<'tx>(&self, transaction: &'tx Transaction<'_>, new_label: String) -> BoxPromise<'tx, Result> {
        box_promise(transaction.transaction_stream.role_type_set_label(self.clone().into(), new_label))
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
    #[cfg_attr(feature = "sync", doc = "role_type.get_supertype(transaction).resolve();")]
    #[cfg_attr(not(feature = "sync"), doc = "role_type.get_supertype(transaction).await;")]
    /// ```
    fn get_supertype<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<Option<RoleType>>> {
        box_promise(transaction.transaction_stream.role_type_get_supertype(self.clone().into()))
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
    fn get_supertypes<'tx>(&self, transaction: &'tx Transaction<'_>) -> Result<BoxStream<'tx, Result<RoleType>>> {
        transaction.transaction_stream.role_type_get_supertypes(self.clone().into()).map(box_stream)
    }

    /// Retrieves all direct and indirect (or direct only) subtypes of the `RoleType`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect subtypes,
    /// `Transitivity::Explicit` for direct subtypes only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_subtypes(transaction, transitivity)
    /// ```
    fn get_subtypes<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<RoleType>>> {
        transaction.transaction_stream.role_type_get_subtypes(self.clone().into(), transitivity).map(box_stream)
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
    fn get_relation_types<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
    ) -> Result<BoxStream<'tx, Result<RelationType>>> {
        transaction.transaction_stream.role_type_get_relation_types(self.clone().into()).map(box_stream)
    }

    /// Retrieves the `ThingType`s whose instances play this role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect playing,
    /// `Transitivity::Explicit` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_player_types(transaction, transitivity)
    /// ```
    fn get_player_types<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<ThingType>>> {
        transaction.transaction_stream.role_type_get_player_types(self.clone().into(), transitivity).map(box_stream)
    }

    /// Retrieves the `Relation` instances that this role is related to.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect relation,
    /// `Transitivity::Explicit` for direct relation only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_relation_instances(transaction, transitivity)
    /// ```
    fn get_relation_instances<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<Relation>>> {
        transaction
            .transaction_stream
            .role_type_get_relation_instances(self.clone().into(), transitivity)
            .map(box_stream)
    }

    /// Retrieves the `Thing` instances that play this role.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `transitivity` -- `Transitivity::Transitive` for direct and indirect playing,
    /// `Transitivity::Explicit` for direct playing only
    ///
    /// # Examples
    ///
    /// ```rust
    /// role_type.get_player_instances(transaction, transitivity)
    /// ```
    fn get_player_instances<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
        transitivity: Transitivity,
    ) -> Result<BoxStream<'tx, Result<Thing>>> {
        transaction.transaction_stream.role_type_get_player_instances(self.clone().into(), transitivity).map(box_stream)
    }
}

impl RoleTypeAPI for RoleType {
    fn is_abstract(&self) -> bool {
        self.is_abstract
    }

    fn get_relation_type<'tx>(
        &self,
        transaction: &'tx Transaction<'_>,
    ) -> BoxPromise<'tx, Result<Option<RelationType>>> {
        box_promise(transaction.transaction_stream.get_relation_type(self.label.scope.clone()))
    }

    fn is_deleted<'tx>(&self, transaction: &'tx Transaction<'_>) -> BoxPromise<'tx, Result<bool>> {
        // FIXME only request get_relates_for_role_label() after NPE is fixed in core
        let relation_type_promise = self.get_relation_type(transaction);
        let role_name = self.label.name.clone();
        box_promise(promisify! {
            if let Some(relation_type) = resolve!(relation_type_promise)? {
                resolve!(relation_type.get_relates_for_role_label(transaction, role_name)).map(|res| res.is_none())
            } else {
                Ok(false)
            }
        })
    }
}
