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

use std::{fmt, iter, pin::Pin};

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};
use typeql::pattern::{Conjunction, Statement};

use super::{
    message::{RoleTypeRequest, RoleTypeResponse, ThingRequest, ThingResponse},
    network::transmitter::TransactionTransmitter,
};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, ValueGroup},
    common::{
        stream::{BoxStream, Stream},
        Promise, Result, IID,
    },
    concept::{
        Annotation, Attribute, AttributeType, Entity, EntityType, Relation, RelationType, RoleType, SchemaException,
        Thing, ThingType, Transitivity, Value, ValueType,
    },
    connection::message::{
        ConceptRequest, ConceptResponse, LogicRequest, LogicResponse, QueryRequest, QueryResponse, RuleRequest,
        RuleResponse, ThingTypeRequest, ThingTypeResponse, TransactionRequest, TransactionResponse,
    },
    error::{ConnectionError, InternalError},
    logic::{Explanation, Rule},
    promisify, resolve, Options, TransactionType,
};

pub(crate) struct TransactionStream {
    type_: TransactionType,
    options: Options,
    transaction_transmitter: TransactionTransmitter,
}

impl TransactionStream {
    pub(super) fn new(
        type_: TransactionType,
        options: Options,
        transaction_transmitter: TransactionTransmitter,
    ) -> Self {
        Self { type_, options, transaction_transmitter }
    }

    pub(crate) fn is_open(&self) -> bool {
        self.transaction_transmitter.is_open()
    }

    pub(crate) fn force_close(&self) {
        self.transaction_transmitter.force_close();
    }

    pub(crate) fn type_(&self) -> TransactionType {
        self.type_
    }

    pub(crate) fn options(&self) -> &Options {
        &self.options
    }

    pub(crate) fn on_close(&self, callback: impl FnOnce(ConnectionError) + Send + Sync + 'static) {
        self.transaction_transmitter.on_close(callback)
    }

    pub(crate) fn commit(self: Pin<Box<Self>>) -> impl Promise<'static, Result> {
        let promise = self.transaction_transmitter.single(TransactionRequest::Commit);
        promisify! {
            let _this = self;  // move into the promise so the stream isn't dropped until the promise is resolved
            resolve!(promise).map(|_| ())
        }
    }

    pub(crate) fn rollback(&self) -> impl Promise<'_, Result> {
        let promise = self.single(TransactionRequest::Rollback);
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn define(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Define { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn undefine(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Undefine { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn delete(&self, query: String, options: Options) -> impl Promise<'_, Result> {
        let promise = self.query_single(QueryRequest::Delete { query, options });
        promisify! { resolve!(promise).map(|_| ()) }
    }

    pub(crate) fn get(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Get { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Get { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn insert(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Insert { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Insert { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn update(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Update { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Update { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_aggregate(&self, query: String, options: Options) -> impl Promise<'_, Result<Value>> {
        let promise = self.query_single(QueryRequest::GetAggregate { query, options });
        promisify! {
            match resolve!(promise)? {
                QueryResponse::GetAggregate { answer } => Ok(answer),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_group(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        let stream = self.query_stream(QueryRequest::GetGroup { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::GetGroup { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_group_aggregate(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ValueGroup>>> {
        let stream = self.query_stream(QueryRequest::GetGroupAggregate { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::GetGroupAggregate { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_entity_type(&self, label: String) -> impl Promise<'_, Result<Option<EntityType>>> {
        let promise = self.concept_single(ConceptRequest::GetEntityType { label });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetEntityType { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_relation_type(&self, label: String) -> impl Promise<'_, Result<Option<RelationType>>> {
        let promise = self.concept_single(ConceptRequest::GetRelationType { label });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetRelationType { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_attribute_type(&self, label: String) -> impl Promise<'_, Result<Option<AttributeType>>> {
        let promise = self.concept_single(ConceptRequest::GetAttributeType { label });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetAttributeType { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_entity_type(&self, label: String) -> impl Promise<'_, Result<EntityType>> {
        let promise = self.concept_single(ConceptRequest::PutEntityType { label });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::PutEntityType { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_relation_type(&self, label: String) -> impl Promise<'_, Result<RelationType>> {
        let promise = self.concept_single(ConceptRequest::PutRelationType { label });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::PutRelationType { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_attribute_type(
        &self,
        label: String,
        value_type: ValueType,
    ) -> impl Promise<'_, Result<AttributeType>> {
        let promise = self.concept_single(ConceptRequest::PutAttributeType { label, value_type });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::PutAttributeType { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_entity(&self, iid: IID) -> impl Promise<'_, Result<Option<Entity>>> {
        let promise = self.concept_single(ConceptRequest::GetEntity { iid });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetEntity { entity } => Ok(entity),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_relation(&self, iid: IID) -> impl Promise<'_, Result<Option<Relation>>> {
        let promise = self.concept_single(ConceptRequest::GetRelation { iid });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetRelation { relation } => Ok(relation),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_attribute(&self, iid: IID) -> impl Promise<'_, Result<Option<Attribute>>> {
        let promise = self.concept_single(ConceptRequest::GetAttribute { iid });
        promisify! {
            match resolve!(promise)? {
                ConceptResponse::GetAttribute { attribute } => Ok(attribute),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_schema_exceptions(&self) -> Result<impl Stream<Item = Result<SchemaException>>> {
        let stream = self.concept_stream(ConceptRequest::GetSchemaExceptions)?;
        Ok(stream.flat_map(|result| match result {
            Ok(ConceptResponse::GetSchemaExceptions { exceptions }) => stream_iter(exceptions.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_type_delete(&self, thing_type: ThingType) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeDelete { thing_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeDelete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_label(&self, thing_type: ThingType, new_label: String) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeSetLabel { thing_type, new_label });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeSetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_abstract(&self, thing_type: ThingType) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeSetAbstract { thing_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeSetAbstract => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_abstract(&self, thing_type: ThingType) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeUnsetAbstract { thing_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeUnsetAbstract => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_get_owns(
        &self,
        thing_type: ThingType,
        value_type: Option<ValueType>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<impl Stream<Item = Result<AttributeType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::ThingTypeGetOwns {
            thing_type,
            value_type,
            transitivity,
            annotations,
        })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::ThingTypeGetOwns { attribute_types }) => {
                stream_iter(attribute_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_type_get_owns_overridden(
        &self,
        thing_type: ThingType,
        overridden_attribute_type: AttributeType,
    ) -> impl Promise<'_, Result<Option<AttributeType>>> {
        let promise = self
            .thing_type_single(ThingTypeRequest::ThingTypeGetOwnsOverridden { thing_type, overridden_attribute_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeGetOwnsOverridden { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_owns(
        &self,
        thing_type: ThingType,
        attribute_type: AttributeType,
        overridden_attribute_type: Option<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeSetOwns {
            thing_type,
            attribute_type,
            overridden_attribute_type,
            annotations,
        });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeSetOwns => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_owns(
        &self,
        thing_type: ThingType,
        attribute_type: AttributeType,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeUnsetOwns { thing_type, attribute_type });
        promisify! {
            match resolve!(promise)?
            {
                ThingTypeResponse::ThingTypeUnsetOwns => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_get_plays(
        &self,
        thing_type: ThingType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::ThingTypeGetPlays { thing_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::ThingTypeGetPlays { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_type_get_plays_overridden(
        &self,
        thing_type: ThingType,
        overridden_role_type: RoleType,
    ) -> impl Promise<'_, Result<Option<RoleType>>> {
        let promise =
            self.thing_type_single(ThingTypeRequest::ThingTypeGetPlaysOverridden { thing_type, overridden_role_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeGetPlaysOverridden { role_type } => Ok(role_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_plays(
        &self,
        thing_type: ThingType,
        role_type: RoleType,
        overridden_role_type: Option<RoleType>,
    ) -> impl Promise<'_, Result> {
        let promise =
            self.thing_type_single(ThingTypeRequest::ThingTypeSetPlays { thing_type, role_type, overridden_role_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeSetPlays => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_plays(
        &self,
        thing_type: ThingType,
        role_type: RoleType,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeUnsetPlays { thing_type, role_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeUnsetPlays => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_get_syntax(&self, thing_type: ThingType) -> impl Promise<'_, Result<String>> {
        let promise = self.thing_type_single(ThingTypeRequest::ThingTypeGetSyntax { thing_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::ThingTypeGetSyntax { syntax } => Ok(syntax),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_create(&self, entity_type: EntityType) -> impl Promise<'_, Result<Entity>> {
        let promise = self.thing_type_single(ThingTypeRequest::EntityTypeCreate { entity_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::EntityTypeCreate { entity } => Ok(entity),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_get_supertype(
        &self,
        entity_type: EntityType,
    ) -> impl Promise<'_, Result<Option<EntityType>>> {
        let promise = self.thing_type_single(ThingTypeRequest::EntityTypeGetSupertype { entity_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::EntityTypeGetSupertype { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_set_supertype(
        &self,
        entity_type: EntityType,
        supertype: EntityType,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::EntityTypeSetSupertype { entity_type, supertype });
        promisify! {
            match resolve!(promise)?
            {
                ThingTypeResponse::EntityTypeSetSupertype => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_get_supertypes(
        &self,
        entity_type: EntityType,
    ) -> Result<impl Stream<Item = Result<EntityType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::EntityTypeGetSupertypes { entity_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::EntityTypeGetSupertypes { entity_types }) => {
                stream_iter(entity_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn entity_type_get_subtypes(
        &self,
        entity_type: EntityType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<EntityType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::EntityTypeGetSubtypes { entity_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::EntityTypeGetSubtypes { entity_types }) => {
                stream_iter(entity_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn entity_type_get_instances(
        &self,
        entity_type: EntityType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<Entity>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::EntityTypeGetInstances { entity_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::EntityTypeGetInstances { entities }) => stream_iter(entities.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_type_create(&self, relation_type: RelationType) -> impl Promise<'_, Result<Relation>> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeCreate { relation_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeCreate { relation } => Ok(relation),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_get_supertype(
        &self,
        relation_type: RelationType,
    ) -> impl Promise<'_, Result<Option<RelationType>>> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeGetSupertype { relation_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeGetSupertype { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_set_supertype(
        &self,
        relation_type: RelationType,
        supertype: RelationType,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeSetSupertype { relation_type, supertype });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeSetSupertype => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_get_supertypes(
        &self,
        relation_type: RelationType,
    ) -> Result<impl Stream<Item = Result<RelationType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::RelationTypeGetSupertypes { relation_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::RelationTypeGetSupertypes { relation_types }) => {
                stream_iter(relation_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_type_get_subtypes(
        &self,
        relation_type: RelationType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<RelationType>>> {
        let stream =
            self.thing_type_stream(ThingTypeRequest::RelationTypeGetSubtypes { relation_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::RelationTypeGetSubtypes { relation_types }) => {
                stream_iter(relation_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_type_get_instances(
        &self,
        relation_type: RelationType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<Relation>>> {
        let stream =
            self.thing_type_stream(ThingTypeRequest::RelationTypeGetInstances { relation_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::RelationTypeGetInstances { relations }) => stream_iter(relations.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_type_get_relates(
        &self,
        relation_type: RelationType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream =
            self.thing_type_stream(ThingTypeRequest::RelationTypeGetRelates { relation_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::RelationTypeGetRelates { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_type_get_relates_for_role_label(
        &self,
        relation_type: RelationType,
        role_label: String,
    ) -> impl Promise<'_, Result<Option<RoleType>>> {
        let promise =
            self.thing_type_single(ThingTypeRequest::RelationTypeGetRelatesForRoleLabel { relation_type, role_label });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeGetRelatesForRoleLabel { role_type } => Ok(role_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_get_relates_overridden(
        &self,
        relation_type: RelationType,
        overridden_role_label: String,
    ) -> impl Promise<'_, Result<Option<RoleType>>> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeGetRelatesOverridden {
            relation_type,
            role_label: overridden_role_label,
        });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeGetRelatesOverridden { role_type } => Ok(role_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_set_relates(
        &self,
        relation_type: RelationType,
        role_label: String,
        overridden_role_label: Option<String>,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeSetRelates {
            relation_type,
            role_label,
            overridden_role_label,
        });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeSetRelates => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_unset_relates(
        &self,
        relation_type: RelationType,
        role_label: String,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::RelationTypeUnsetRelates { relation_type, role_label });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::RelationTypeUnsetRelates => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_put(
        &self,
        attribute_type: AttributeType,
        value: Value,
    ) -> impl Promise<'_, Result<Attribute>> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypePut { attribute_type, value });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypePut { attribute } => Ok(attribute),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get(
        &self,
        attribute_type: AttributeType,
        value: Value,
    ) -> impl Promise<'_, Result<Option<Attribute>>> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypeGet { attribute_type, value });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypeGet { attribute } => Ok(attribute),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get_supertype(
        &self,
        attribute_type: AttributeType,
    ) -> impl Promise<'_, Result<Option<AttributeType>>> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypeGetSupertype { attribute_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypeGetSupertype { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_set_supertype(
        &self,
        attribute_type: AttributeType,
        supertype: AttributeType,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypeSetSupertype { attribute_type, supertype });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypeSetSupertype => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get_supertypes(
        &self,
        attribute_type: AttributeType,
    ) -> Result<impl Stream<Item = Result<AttributeType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::AttributeTypeGetSupertypes { attribute_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::AttributeTypeGetSupertypes { attribute_types }) => {
                stream_iter(attribute_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn attribute_type_get_subtypes(
        &self,
        attribute_type: AttributeType,
        transitivity: Transitivity,
        value_type: Option<ValueType>,
    ) -> Result<impl Stream<Item = Result<AttributeType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::AttributeTypeGetSubtypes {
            attribute_type,
            transitivity,
            value_type,
        })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::AttributeTypeGetSubtypes { attribute_types }) => {
                stream_iter(attribute_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn attribute_type_get_instances(
        &self,
        attribute_type: AttributeType,
        transitivity: Transitivity,
        value_type: Option<ValueType>,
    ) -> Result<impl Stream<Item = Result<Attribute>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::AttributeTypeGetInstances {
            attribute_type,
            transitivity,
            value_type,
        })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::AttributeTypeGetInstances { attributes }) => {
                stream_iter(attributes.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn attribute_type_get_regex(
        &self,
        attribute_type: AttributeType,
    ) -> impl Promise<'_, Result<Option<String>>> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypeGetRegex { attribute_type });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypeGetRegex { regex } => Ok(regex),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_set_regex(
        &self,
        attribute_type: AttributeType,
        regex: String,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_type_single(ThingTypeRequest::AttributeTypeSetRegex { attribute_type, regex });
        promisify! {
            match resolve!(promise)? {
                ThingTypeResponse::AttributeTypeSetRegex => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get_owners(
        &self,
        attribute_type: AttributeType,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    ) -> Result<impl Stream<Item = Result<ThingType>>> {
        let stream = self.thing_type_stream(ThingTypeRequest::AttributeTypeGetOwners {
            attribute_type,
            transitivity,
            annotations,
        })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingTypeResponse::AttributeTypeGetOwners { thing_types }) => {
                stream_iter(thing_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_delete(&self, role_type: RoleType) -> impl Promise<'_, Result> {
        let promise = self.role_type_single(RoleTypeRequest::Delete { role_type });
        promisify! {
            match resolve!(promise)? {
                RoleTypeResponse::Delete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn role_type_set_label(&self, role_type: RoleType, new_label: String) -> impl Promise<'_, Result> {
        let promise = self.role_type_single(RoleTypeRequest::SetLabel { role_type, new_label });
        promisify! {
            match resolve!(promise)? {
                RoleTypeResponse::SetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn role_type_get_supertype(&self, role_type: RoleType) -> impl Promise<'_, Result<Option<RoleType>>> {
        let promise = self.role_type_single(RoleTypeRequest::GetSupertype { role_type });
        promisify! {
            match resolve!(promise)? {
                RoleTypeResponse::GetSupertype { role_type } => Ok(role_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn role_type_get_supertypes(&self, role_type: RoleType) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetSupertypes { role_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetSupertypes { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_get_subtypes(
        &self,
        role_type: RoleType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetSubtypes { role_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetSubtypes { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_get_relation_types(
        &self,
        role_type: RoleType,
    ) -> Result<impl Stream<Item = Result<RelationType>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetRelationTypes { role_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetRelationTypes { relation_types }) => {
                stream_iter(relation_types.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_get_player_types(
        &self,
        role_type: RoleType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<ThingType>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetPlayerTypes { role_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetPlayerTypes { thing_types }) => stream_iter(thing_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_get_relation_instances(
        &self,
        role_type: RoleType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<Relation>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetRelationInstances { role_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetRelationInstances { relations }) => stream_iter(relations.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn role_type_get_player_instances(
        &self,
        role_type: RoleType,
        transitivity: Transitivity,
    ) -> Result<impl Stream<Item = Result<Thing>>> {
        let stream = self.role_type_stream(RoleTypeRequest::GetPlayerInstances { role_type, transitivity })?;
        Ok(stream.flat_map(|result| match result {
            Ok(RoleTypeResponse::GetPlayerInstances { things }) => stream_iter(things.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_delete(&self, thing: Thing) -> impl Promise<'_, Result> {
        let promise = self.thing_single(ThingRequest::ThingDelete { thing });
        promisify! {
            match resolve!(promise)? {
                ThingResponse::ThingDelete {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_get_has(
        &self,
        thing: Thing,
        attribute_types: Vec<AttributeType>,
        annotations: Vec<Annotation>,
    ) -> Result<impl Stream<Item = Result<Attribute>>> {
        let stream = self.thing_stream(ThingRequest::ThingGetHas { thing, attribute_types, annotations })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::ThingGetHas { attributes }) => stream_iter(attributes.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_set_has(&self, thing: Thing, attribute: Attribute) -> impl Promise<'_, Result> {
        let promise = self.thing_single(ThingRequest::ThingSetHas { thing, attribute });
        promisify! {
            match resolve!(promise)? {
                ThingResponse::ThingSetHas {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_unset_has(&self, thing: Thing, attribute: Attribute) -> impl Promise<'_, Result> {
        let promise = self.thing_single(ThingRequest::ThingUnsetHas { thing, attribute });
        promisify! {
            match resolve!(promise)? {
                ThingResponse::ThingUnsetHas {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_get_relations(
        &self,
        thing: Thing,
        role_types: Vec<RoleType>,
    ) -> Result<impl Stream<Item = Result<Relation>>> {
        let stream = self.thing_stream(ThingRequest::ThingGetRelations { thing, role_types })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::ThingGetRelations { relations }) => stream_iter(relations.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn thing_get_playing(&self, thing: Thing) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream = self.thing_stream(ThingRequest::ThingGetPlaying { thing })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::ThingGetPlaying { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_add_role_player(
        &self,
        relation: Relation,
        role_type: RoleType,
        player: Thing,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_single(ThingRequest::RelationAddRolePlayer { relation, role_type, player });
        promisify! {
            match resolve!(promise)? {
                ThingResponse::RelationAddRolePlayer {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_remove_role_player(
        &self,
        relation: Relation,
        role_type: RoleType,
        player: Thing,
    ) -> impl Promise<'_, Result> {
        let promise = self.thing_single(ThingRequest::RelationRemoveRolePlayer { relation, role_type, player });
        promisify! {
            match resolve!(promise)? {
                ThingResponse::RelationRemoveRolePlayer {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_get_players_by_role_type(
        &self,
        relation: Relation,
        role_types: Vec<RoleType>,
    ) -> Result<impl Stream<Item = Result<Thing>>> {
        let stream = self.thing_stream(ThingRequest::RelationGetPlayersByRoleType { relation, role_types })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::RelationGetPlayersByRoleType { things: players }) => {
                stream_iter(players.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_get_role_players(
        &self,
        relation: Relation,
    ) -> Result<impl Stream<Item = Result<(RoleType, Thing)>>> {
        let stream = self.thing_stream(ThingRequest::RelationGetRolePlayers { relation })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::RelationGetRolePlayers { role_players: players }) => {
                stream_iter(players.into_iter().map(Ok))
            }
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn relation_get_relating(&self, relation: Relation) -> Result<impl Stream<Item = Result<RoleType>>> {
        let stream = self.thing_stream(ThingRequest::RelationGetRelating { relation })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::RelationGetRelating { role_types }) => stream_iter(role_types.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn attribute_get_owners(
        &self,
        attribute: Attribute,
        thing_type: Option<ThingType>,
    ) -> Result<impl Stream<Item = Result<Thing>>> {
        let stream = self.thing_stream(ThingRequest::AttributeGetOwners { attribute, thing_type })?;
        Ok(stream.flat_map(|result| match result {
            Ok(ThingResponse::AttributeGetOwners { owners }) => stream_iter(owners.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn rule_delete(&self, rule: Rule) -> impl Promise<'_, Result> {
        let promise = self.rule_single(RuleRequest::Delete { label: rule.label });
        promisify! {
            match resolve!(promise)? {
                RuleResponse::Delete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn rule_set_label(&self, rule: Rule, new_label: String) -> impl Promise<'_, Result> {
        let promise = self.rule_single(RuleRequest::SetLabel { current_label: rule.label, new_label });
        promisify! {
            match resolve!(promise)? {
                RuleResponse::SetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_rule(&self, label: String, when: Conjunction, then: Statement) -> impl Promise<'_, Result<Rule>> {
        let promise = self.logic_single(LogicRequest::PutRule { label, when, then });
        promisify! {
            match resolve!(promise)? {
                LogicResponse::PutRule { rule } => Ok(rule),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_rule(&self, label: String) -> impl Promise<'_, Result<Option<Rule>>> {
        let promise = self.logic_single(LogicRequest::GetRule { label });
        promisify! {
            match resolve!(promise)? {
                LogicResponse::GetRule { rule } => Ok(rule),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_rules(&self) -> Result<impl Stream<Item = Result<Rule>>> {
        let stream = self.logic_stream(LogicRequest::GetRules {})?;
        Ok(stream.flat_map(|result| match result {
            Ok(LogicResponse::GetRules { rules }) => stream_iter(rules.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn explain(
        &self,
        explainable_id: i64,
        options: Options,
    ) -> Result<impl Stream<Item = Result<Explanation>>> {
        let stream = self.query_stream(QueryRequest::Explain { explainable_id, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Explain { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    fn single(&self, req: TransactionRequest) -> impl Promise<'static, Result<TransactionResponse>> {
        self.transaction_transmitter.single(req)
    }

    fn query_single(&self, req: QueryRequest) -> impl Promise<'_, Result<QueryResponse>> {
        let promise = self.single(TransactionRequest::Query(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Query(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn concept_single(&self, req: ConceptRequest) -> impl Promise<'_, Result<ConceptResponse>> {
        let promise = self.single(TransactionRequest::Concept(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Concept(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn thing_type_single(&self, req: ThingTypeRequest) -> impl Promise<'_, Result<ThingTypeResponse>> {
        let promise = self.single(TransactionRequest::ThingType(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::ThingType(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn role_type_single(&self, req: RoleTypeRequest) -> impl Promise<'_, Result<RoleTypeResponse>> {
        let promise = self.single(TransactionRequest::RoleType(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::RoleType(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn thing_single(&self, req: ThingRequest) -> impl Promise<'_, Result<ThingResponse>> {
        let promise = self.single(TransactionRequest::Thing(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Thing(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn rule_single(&self, req: RuleRequest) -> impl Promise<'_, Result<RuleResponse>> {
        let promise = self.single(TransactionRequest::Rule(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Rule(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn logic_single(&self, req: LogicRequest) -> impl Promise<'_, Result<LogicResponse>> {
        let promise = self.single(TransactionRequest::Logic(req));
        promisify! {
            match resolve!(promise)? {
                TransactionResponse::Logic(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn stream(&self, req: TransactionRequest) -> Result<impl Stream<Item = Result<TransactionResponse>>> {
        self.transaction_transmitter.stream(req)
    }

    fn query_stream(&self, req: QueryRequest) -> Result<impl Stream<Item = Result<QueryResponse>>> {
        Ok(self.stream(TransactionRequest::Query(req))?.map(|response| match response {
            Ok(TransactionResponse::Query(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }

    fn concept_stream(&self, req: ConceptRequest) -> Result<impl Stream<Item = Result<ConceptResponse>>> {
        Ok(self.stream(TransactionRequest::Concept(req))?.map(|response| match response {
            Ok(TransactionResponse::Concept(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }

    fn thing_type_stream(&self, req: ThingTypeRequest) -> Result<impl Stream<Item = Result<ThingTypeResponse>>> {
        Ok(self.stream(TransactionRequest::ThingType(req))?.map(|response| match response {
            Ok(TransactionResponse::ThingType(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }

    fn role_type_stream(&self, req: RoleTypeRequest) -> Result<impl Stream<Item = Result<RoleTypeResponse>>> {
        Ok(self.stream(TransactionRequest::RoleType(req))?.map(|response| match response {
            Ok(TransactionResponse::RoleType(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }

    fn thing_stream(&self, req: ThingRequest) -> Result<impl Stream<Item = Result<ThingResponse>>> {
        Ok(self.stream(TransactionRequest::Thing(req))?.map(|response| match response {
            Ok(TransactionResponse::Thing(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }

    fn logic_stream(&self, req: LogicRequest) -> Result<impl Stream<Item = Result<LogicResponse>>> {
        Ok(self.stream(TransactionRequest::Logic(req))?.map(|response| match response {
            Ok(TransactionResponse::Logic(res)) => Ok(res),
            Ok(other) => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            Err(err) => Err(err),
        }))
    }
}

impl fmt::Debug for TransactionStream {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("TransactionStream").field("type_", &self.type_).field("options", &self.options).finish()
    }
}

fn stream_once<'a, T: Send + 'a>(value: T) -> BoxStream<'a, T> {
    stream_iter(iter::once(value))
}

#[cfg(feature = "sync")]
fn stream_iter<'a, T: Send + 'a>(iter: impl Iterator<Item = T> + Send + 'a) -> BoxStream<'a, T> {
    Box::new(iter)
}

#[cfg(not(feature = "sync"))]
fn stream_iter<'a, T: Send + 'a>(iter: impl Iterator<Item = T> + Send + 'a) -> BoxStream<'a, T> {
    Box::pin(stream::iter(iter))
}
