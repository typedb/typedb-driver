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
    common::{box_promise, box_stream, stream::BoxStream, BoxPromise, IID},
    concept::{Annotation, Attribute, AttributeType, Entity, Relation, RoleType, Thing, ThingType},
    promisify, resolve, Result, Transaction,
};

pub trait ThingAPI: Sync + Send {
    /// Retrieves the unique id of the `Thing`.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing.iid();
    /// ```
    fn iid(&self) -> &IID;

    /// Checks if this `Thing` is inferred by a [Reasoning Rule].
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing.is_inferred();
    /// ```
    fn is_inferred(&self) -> bool;

    fn to_thing_cloned(&self) -> Thing;

    /// Checks if this `Thing` is deleted.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing.is_deleted(transaction);")]
    #[cfg_attr(not(feature = "sync"), doc = "thing.is_deleted(transaction).await;")]
    /// ```
    fn is_deleted<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result<bool>>;

    /// Deletes this `Thing`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing.delete(transaction);")]
    #[cfg_attr(not(feature = "sync"), doc = "thing.delete(transaction).await;")]
    /// ```
    fn delete<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result> {
        box_promise(transaction.concept().transaction_stream.thing_delete(self.to_thing_cloned()))
    }

    /// Retrieves the `Attribute`s that this `Thing` owns. Optionally, filtered by an `AttributeType` or a list of `AttributeType`s. Optionally, filtered by `Annotation`s.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `attribute_type` -- The `AttributeType` to filter the attributes by
    /// * `attribute_types` -- The `AttributeType`s to filter the attributes by
    /// * `annotations` -- Only retrieve attributes with all given `Annotation`s
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing.get_has(transaction, attribute_type, annotations=vec![Annotation::Key]);
    /// ```
    fn get_has<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        attribute_types: Vec<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<Result<Attribute>>> {
        transaction
            .concept()
            .transaction_stream
            .thing_get_has(self.to_thing_cloned(), attribute_types, annotations)
            .map(box_stream)
    }

    /// Assigns an `Attribute` to be owned by this `Thing`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `attribute` -- The `Attribute` to be owned by this `Thing`.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing.set_has(transaction, attribute);")]
    #[cfg_attr(not(feature = "sync"), doc = "thing.set_has(transaction, attribute).await;")]
    /// ```
    fn set_has<'tx>(&'tx self, transaction: &'tx Transaction<'tx>, attribute: Attribute) -> BoxPromise<Result> {
        box_promise(transaction.concept().transaction_stream.thing_set_has(self.to_thing_cloned(), attribute))
    }

    /// Unassigns an `Attribute` from this `Thing`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `attribute` -- The `Attribute` to be disowned from this `Thing`.
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "thing.unset_has(transaction, attribute);")]
    #[cfg_attr(not(feature = "sync"), doc = "thing.unset_has(transaction, attribute).await;")]
    /// ```
    fn unset_has<'tx>(&'tx self, transaction: &'tx Transaction<'tx>, attribute: Attribute) -> BoxPromise<Result> {
        box_promise(transaction.concept().transaction_stream.thing_unset_has(self.to_thing_cloned(), attribute))
    }

    /// Retrieves all the `Relations` which this `Thing` plays a role in, optionally filtered by one or more given roles.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_types` -- The list of roles to filter the relations by.
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing.get_relations(transaction, role_types);
    /// ```
    fn get_relations<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        role_types: Vec<RoleType>,
    ) -> Result<BoxStream<Result<Relation>>> {
        transaction.concept().transaction_stream.thing_get_relations(self.to_thing_cloned(), role_types).map(box_stream)
    }

    /// Retrieves the roles that this `Thing` is currently playing.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// thing.get_playing(transaction);
    /// ```
    fn get_playing<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.thing_get_playing(self.to_thing_cloned()).map(box_stream)
    }
}

impl ThingAPI for Thing {
    fn iid(&self) -> &IID {
        match self {
            Thing::Entity(entity) => entity.iid(),
            Thing::Relation(relation) => relation.iid(),
            Thing::Attribute(attribute) => attribute.iid(),
        }
    }

    fn is_inferred(&self) -> bool {
        match self {
            Thing::Entity(entity) => entity.is_inferred(),
            Thing::Relation(relation) => relation.is_inferred(),
            Thing::Attribute(attribute) => attribute.is_inferred(),
        }
    }

    fn to_thing_cloned(&self) -> Thing {
        self.clone()
    }

    fn is_deleted<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result<bool>> {
        box_promise(match self {
            Thing::Entity(entity) => entity.is_deleted(transaction),
            Thing::Relation(relation) => relation.is_deleted(transaction),
            Thing::Attribute(attribute) => attribute.is_deleted(transaction),
        })
    }
}

impl ThingAPI for Entity {
    fn iid(&self) -> &IID {
        &self.iid
    }

    fn is_inferred(&self) -> bool {
        self.is_inferred
    }

    fn to_thing_cloned(&self) -> Thing {
        Thing::Entity(self.clone())
    }

    fn is_deleted<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result<bool>> {
        box_promise(promisify! {
            resolve!(transaction.concept().transaction_stream.get_entity(self.iid().clone())).map(|res| res.is_none())
        })
    }
}

pub trait EntityAPI: ThingAPI + Clone + Into<Entity> {}

impl EntityAPI for Entity {}

impl ThingAPI for Relation {
    fn iid(&self) -> &IID {
        &self.iid
    }

    fn is_inferred(&self) -> bool {
        self.is_inferred
    }

    fn to_thing_cloned(&self) -> Thing {
        Thing::Relation(self.clone())
    }

    fn is_deleted<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result<bool>> {
        box_promise(promisify! {
            resolve!(transaction.concept().transaction_stream.get_relation(self.iid().clone())).map(|res| res.is_none())
        })
    }
}

pub trait RelationAPI: ThingAPI + Clone + Into<Relation> {
    /// Adds a new role player to play the given role in this `Relation`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_type` -- The role to be played by the `player`
    /// * `player` -- The thing to play the role
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "relation.add_role_player(transaction, role_type, player);")]
    #[cfg_attr(not(feature = "sync"), doc = "relation.add_role_player(transaction, role_type, player).await;")]
    /// ```
    fn add_role_player<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        role_type: RoleType,
        player: Thing,
    ) -> BoxPromise<Result> {
        box_promise(transaction.concept().transaction_stream.relation_add_role_player(
            self.clone().into(),
            role_type,
            player,
        ))
    }

    /// Removes the association of the given instance that plays the given role in this `Relation`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_type` -- The role to no longer be played by the thing in this `Relation`
    /// * `player` -- The instance to no longer play the role in this `Relation`
    ///
    /// # Examples
    ///
    /// ```rust
    #[cfg_attr(feature = "sync", doc = "relation.remove_role_player(transaction, role_type, player);")]
    #[cfg_attr(not(feature = "sync"), doc = "relation.remove_role_player(transaction, role_type, player).await;")]
    /// ```
    fn remove_role_player<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        role_type: RoleType,
        player: Thing,
    ) -> BoxPromise<Result> {
        box_promise(transaction.concept().transaction_stream.relation_remove_role_player(
            self.clone().into(),
            role_type,
            player,
        ))
    }

    /// Retrieves all role players of this `Relation`, optionally filtered by given role types.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `role_types` -- 0 or more role types
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation.get_players_by_role_type(transaction, role_types);
    /// ```
    fn get_players_by_role_type<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        role_types: Vec<RoleType>,
    ) -> Result<BoxStream<Result<Thing>>> {
        transaction
            .concept()
            .transaction_stream
            .relation_get_players_by_role_type(self.clone().into(), role_types)
            .map(box_stream)
    }

    /// Retrieves a mapping of all instances involved in the `Relation` and the role each play.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation.get_role_players(transaction)
    /// ```
    fn get_role_players<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
    ) -> Result<BoxStream<Result<(RoleType, Thing)>>> {
        transaction.concept().transaction_stream.relation_get_role_players(self.clone().into()).map(box_stream)
    }

    /// Retrieves all role types currently played in this `Relation`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    ///
    /// # Examples
    ///
    /// ```rust
    /// relation.get_relating(transaction)
    /// ```
    fn get_relating<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.relation_get_relating(self.clone().into()).map(box_stream)
    }
}

impl RelationAPI for Relation {}

impl ThingAPI for Attribute {
    fn iid(&self) -> &IID {
        &self.iid
    }

    fn is_inferred(&self) -> bool {
        self.is_inferred
    }

    fn to_thing_cloned(&self) -> Thing {
        Thing::Attribute(self.clone())
    }

    fn is_deleted<'tx>(&'tx self, transaction: &'tx Transaction<'tx>) -> BoxPromise<Result<bool>> {
        box_promise(promisify! {
            resolve!(transaction.concept().transaction_stream.get_attribute(self.iid().clone())).map(|res| res.is_none())
        })
    }
}

pub trait AttributeAPI: ThingAPI + Clone + Into<Attribute> {
    /// Retrieves the instances that own this `Attribute`.
    ///
    /// # Arguments
    ///
    /// * `transaction` -- The current transaction
    /// * `owner_type` -- If specified, filter results for only owners of the given type
    ///
    /// # Examples
    ///
    /// ```rust
    /// attribute.get_owners(transaction, Some(owner_type));
    /// ```
    fn get_owners<'tx>(
        &'tx self,
        transaction: &'tx Transaction<'tx>,
        thing_type: Option<ThingType>,
    ) -> Result<BoxStream<Result<Thing>>> {
        transaction.concept().transaction_stream.attribute_get_owners(self.clone().into(), thing_type).map(box_stream)
    }
}

impl AttributeAPI for Attribute {}
