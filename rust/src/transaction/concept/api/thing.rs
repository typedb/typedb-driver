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
    common::{box_stream, stream::BoxStream, IID},
    concept::{Annotation, Attribute, AttributeType, Entity, Relation, RoleType, Thing, ThingType},
    Result, Transaction,
};

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait ThingAPI: Sync + Send {
    fn iid(&self) -> &IID;

    fn is_inferred(&self) -> bool;

    fn to_thing_cloned(&self) -> Thing;

    #[cfg(not(feature = "sync"))]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg(feature = "sync")]
    fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool>;

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn delete(&self, transaction: &Transaction<'_>) -> Result {
        transaction.concept().transaction_stream.thing_delete(self.to_thing_cloned()).await
    }

    fn get_has(
        &self,
        transaction: &Transaction<'_>,
        attribute_types: Vec<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> Result<BoxStream<Result<Attribute>>> {
        transaction
            .concept()
            .transaction_stream
            .thing_get_has(self.to_thing_cloned(), attribute_types, annotations)
            .map(box_stream)
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn set_has(&self, transaction: &Transaction<'_>, attribute: Attribute) -> Result {
        transaction.concept().transaction_stream.thing_set_has(self.to_thing_cloned(), attribute).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn unset_has(&self, transaction: &Transaction<'_>, attribute: Attribute) -> Result {
        transaction.concept().transaction_stream.thing_unset_has(self.to_thing_cloned(), attribute).await
    }

    fn get_relations(
        &self,
        transaction: &Transaction<'_>,
        role_types: Vec<RoleType>,
    ) -> Result<BoxStream<Result<Relation>>> {
        transaction.concept().transaction_stream.thing_get_relations(self.to_thing_cloned(), role_types).map(box_stream)
    }

    fn get_playing(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.thing_get_playing(self.to_thing_cloned()).map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
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

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        match self {
            Thing::Entity(entity) => entity.is_deleted(transaction).await,
            Thing::Relation(relation) => relation.is_deleted(transaction).await,
            Thing::Attribute(attribute) => attribute.is_deleted(transaction).await,
        }
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
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

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction.concept().transaction_stream.get_entity(self.iid().clone()).await.map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait EntityAPI: ThingAPI + Clone + Into<Entity> {}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl EntityAPI for Entity {}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
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

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction.concept().transaction_stream.get_relation(self.iid().clone()).await.map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait RelationAPI: ThingAPI + Clone + Into<Relation> {
    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn add_role_player(&self, transaction: &Transaction<'_>, role_type: RoleType, player: Thing) -> Result {
        transaction.concept().transaction_stream.relation_add_role_player(self.clone().into(), role_type, player).await
    }

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn remove_role_player(&self, transaction: &Transaction<'_>, role_type: RoleType, player: Thing) -> Result {
        transaction
            .concept()
            .transaction_stream
            .relation_remove_role_player(self.clone().into(), role_type, player)
            .await
    }

    fn get_players_by_role_type(
        &self,
        transaction: &Transaction<'_>,
        role_types: Vec<RoleType>,
    ) -> Result<BoxStream<Result<Thing>>> {
        transaction
            .concept()
            .transaction_stream
            .relation_get_players_by_role_type(self.clone().into(), role_types)
            .map(box_stream)
    }

    fn get_role_players(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<(RoleType, Thing)>>> {
        transaction.concept().transaction_stream.relation_get_role_players(self.clone().into()).map(box_stream)
    }

    fn get_relating(&self, transaction: &Transaction<'_>) -> Result<BoxStream<Result<RoleType>>> {
        transaction.concept().transaction_stream.relation_get_relating(self.clone().into()).map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl RelationAPI for Relation {}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
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

    #[cfg_attr(feature = "sync", maybe_async::must_be_sync)]
    async fn is_deleted(&self, transaction: &Transaction<'_>) -> Result<bool> {
        transaction.concept().transaction_stream.get_attribute(self.iid().clone()).await.map(|res| res.is_none())
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
pub trait AttributeAPI: ThingAPI + Clone + Into<Attribute> {
    fn get_owners(
        &self,
        transaction: &Transaction<'_>,
        thing_type: Option<ThingType>,
    ) -> Result<BoxStream<Result<Thing>>> {
        transaction.concept().transaction_stream.attribute_get_owners(self.clone().into(), thing_type).map(box_stream)
    }
}

#[cfg_attr(not(feature = "sync"), async_trait::async_trait)]
impl AttributeAPI for Attribute {}
