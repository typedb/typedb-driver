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

use std::{fmt, iter};

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};
use typeql::pattern::{Conjunction, Variable};

use super::{
    message::{RoleTypeRequest, RoleTypeResponse, ThingRequest, ThingResponse},
    network::transmitter::TransactionTransmitter,
};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
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

    pub(crate) fn commit(&self) -> impl Promise<Result> {
        promisify! {
            let _ = resolve!(self.single(TransactionRequest::Commit))?;
            Ok(())
        }
    }

    pub(crate) fn rollback(&self) -> impl Promise<Result> {
        promisify! {
            let _ = resolve!(self.single(TransactionRequest::Rollback))?;
            Ok(())
        }
    }

    pub(crate) fn define(&self, query: String, options: Options) -> impl Promise<Result> {
        promisify! {
            let _ = resolve!(self.query_single(QueryRequest::Define { query, options }))?;
            Ok(())
        }
    }

    pub(crate) fn undefine(&self, query: String, options: Options) -> impl Promise<Result> {
        promisify! {
            let _ = resolve!(self.query_single(QueryRequest::Undefine { query, options }))?;
            Ok(())
        }
    }

    pub(crate) fn delete(&self, query: String, options: Options) -> impl Promise<Result> {
        promisify! {
            let _ = resolve!(self.query_single(QueryRequest::Delete { query, options }))?;
            Ok(())
        }
    }

    pub(crate) fn match_(&self, query: String, options: Options) -> Result<impl Stream<Item = Result<ConceptMap>>> {
        let stream = self.query_stream(QueryRequest::Match { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::Match { answers }) => stream_iter(answers.into_iter().map(Ok)),
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

    pub(crate) fn match_aggregate(&self, query: String, options: Options) -> impl Promise<Result<Numeric>> {
        promisify! {
            match resolve!(self.query_single(QueryRequest::MatchAggregate { query, options }))? {
                QueryResponse::MatchAggregate { answer } => Ok(answer),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn match_group(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<ConceptMapGroup>>> {
        let stream = self.query_stream(QueryRequest::MatchGroup { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::MatchGroup { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn match_group_aggregate(
        &self,
        query: String,
        options: Options,
    ) -> Result<impl Stream<Item = Result<NumericGroup>>> {
        let stream = self.query_stream(QueryRequest::MatchGroupAggregate { query, options })?;
        Ok(stream.flat_map(|result| match result {
            Ok(QueryResponse::MatchGroupAggregate { answers }) => stream_iter(answers.into_iter().map(Ok)),
            Ok(other) => stream_once(Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into())),
            Err(err) => stream_once(Err(err)),
        }))
    }

    pub(crate) fn get_entity_type(&self, label: String) -> impl Promise<Result<Option<EntityType>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetEntityType { label }))? {
                ConceptResponse::GetEntityType { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_relation_type(&self, label: String) -> impl Promise<Result<Option<RelationType>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetRelationType { label }))? {
                ConceptResponse::GetRelationType { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_attribute_type(&self, label: String) -> impl Promise<Result<Option<AttributeType>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetAttributeType { label }))? {
                ConceptResponse::GetAttributeType { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_entity_type(&self, label: String) -> impl Promise<Result<EntityType>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::PutEntityType { label }))? {
                ConceptResponse::PutEntityType { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_relation_type(&self, label: String) -> impl Promise<Result<RelationType>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::PutRelationType { label }))? {
                ConceptResponse::PutRelationType { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_attribute_type(
        &self,
        label: String,
        value_type: ValueType,
    ) -> impl Promise<Result<AttributeType>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::PutAttributeType { label, value_type }))? {
                ConceptResponse::PutAttributeType { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_entity(&self, iid: IID) -> impl Promise<Result<Option<Entity>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetEntity { iid }))? {
                ConceptResponse::GetEntity { entity } => Ok(entity),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_relation(&self, iid: IID) -> impl Promise<Result<Option<Relation>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetRelation { iid }))? {
                ConceptResponse::GetRelation { relation } => Ok(relation),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_attribute(&self, iid: IID) -> impl Promise<Result<Option<Attribute>>> {
        promisify! {
            match resolve!(self.concept_single(ConceptRequest::GetAttribute { iid }))? {
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

    pub(crate) fn thing_type_delete(&self, thing_type: ThingType) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeDelete { thing_type }))? {
                ThingTypeResponse::ThingTypeDelete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_label(&self, thing_type: ThingType, new_label: String) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeSetLabel { thing_type, new_label }))? {
                ThingTypeResponse::ThingTypeSetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_set_abstract(&self, thing_type: ThingType) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeSetAbstract { thing_type }))? {
                ThingTypeResponse::ThingTypeSetAbstract => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_abstract(&self, thing_type: ThingType) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeUnsetAbstract { thing_type }))? {
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
    ) -> impl Promise<Result<Option<AttributeType>>> {
        promisify! {
            match resolve!(self.thing_type_single(
                ThingTypeRequest::ThingTypeGetOwnsOverridden { thing_type, overridden_attribute_type }
            ))? {
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
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self
                .thing_type_single(ThingTypeRequest::ThingTypeSetOwns {
                    thing_type,
                    attribute_type,
                    overridden_attribute_type,
                    annotations,
                }))?
            {
                ThingTypeResponse::ThingTypeSetOwns => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_owns(
        &self,
        thing_type: ThingType,
        attribute_type: AttributeType,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeUnsetOwns { thing_type, attribute_type }))? {
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
    ) -> impl Promise<Result<Option<RoleType>>> {
        promisify! {
            match resolve!(
                self.thing_type_single(ThingTypeRequest::ThingTypeGetPlaysOverridden { thing_type, overridden_role_type })
            )? {
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
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(
                self.thing_type_single(ThingTypeRequest::ThingTypeSetPlays { thing_type, role_type, overridden_role_type })
            )? {
                ThingTypeResponse::ThingTypeSetPlays => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_unset_plays(&self, thing_type: ThingType, role_type: RoleType) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeUnsetPlays { thing_type, role_type }))? {
                ThingTypeResponse::ThingTypeUnsetPlays => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_type_get_syntax(&self, thing_type: ThingType) -> impl Promise<Result<String>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::ThingTypeGetSyntax { thing_type }))? {
                ThingTypeResponse::ThingTypeGetSyntax { syntax } => Ok(syntax),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_create(&self, entity_type: EntityType) -> impl Promise<Result<Entity>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::EntityTypeCreate { entity_type }))? {
                ThingTypeResponse::EntityTypeCreate { entity } => Ok(entity),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_get_supertype(
        &self,
        entity_type: EntityType,
    ) -> impl Promise<Result<Option<EntityType>>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::EntityTypeGetSupertype { entity_type }))? {
                ThingTypeResponse::EntityTypeGetSupertype { entity_type } => Ok(entity_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn entity_type_set_supertype(
        &self,
        entity_type: EntityType,
        supertype: EntityType,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::EntityTypeSetSupertype { entity_type, supertype }))? {
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

    pub(crate) fn relation_type_create(&self, relation_type: RelationType) -> impl Promise<Result<Relation>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::RelationTypeCreate { relation_type }))? {
                ThingTypeResponse::RelationTypeCreate { relation } => Ok(relation),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_get_supertype(
        &self,
        relation_type: RelationType,
    ) -> impl Promise<Result<Option<RelationType>>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::RelationTypeGetSupertype { relation_type }))? {
                ThingTypeResponse::RelationTypeGetSupertype { relation_type } => Ok(relation_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_set_supertype(
        &self,
        relation_type: RelationType,
        supertype: RelationType,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::RelationTypeSetSupertype { relation_type, supertype }))? {
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
    ) -> impl Promise<Result<Option<RoleType>>> {
        promisify! {
            match resolve!(
                self.thing_type_single(ThingTypeRequest::RelationTypeGetRelatesForRoleLabel { relation_type, role_label })
            )? {
                ThingTypeResponse::RelationTypeGetRelatesForRoleLabel { role_type } => Ok(role_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_get_relates_overridden(
        &self,
        relation_type: RelationType,
        overridden_role_label: String,
    ) -> impl Promise<Result<Option<RoleType>>> {
        promisify! {
            match resolve!(self
                .thing_type_single(ThingTypeRequest::RelationTypeGetRelatesOverridden {
                    relation_type,
                    role_label: overridden_role_label,
                }))?
            {
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
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self
                .thing_type_single(ThingTypeRequest::RelationTypeSetRelates {
                    relation_type,
                    role_label,
                    overridden_role_label,
                }))?
            {
                ThingTypeResponse::RelationTypeSetRelates => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn relation_type_unset_relates(
        &self,
        relation_type: RelationType,
        role_label: String,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::RelationTypeUnsetRelates { relation_type, role_label }))? {
                ThingTypeResponse::RelationTypeUnsetRelates => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_put(
        &self,
        attribute_type: AttributeType,
        value: Value,
    ) -> impl Promise<Result<Attribute>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypePut { attribute_type, value }))? {
                ThingTypeResponse::AttributeTypePut { attribute } => Ok(attribute),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get(
        &self,
        attribute_type: AttributeType,
        value: Value,
    ) -> impl Promise<Result<Option<Attribute>>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypeGet { attribute_type, value }))? {
                ThingTypeResponse::AttributeTypeGet { attribute } => Ok(attribute),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_get_supertype(
        &self,
        attribute_type: AttributeType,
    ) -> impl Promise<Result<Option<AttributeType>>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypeGetSupertype { attribute_type }))? {
                ThingTypeResponse::AttributeTypeGetSupertype { attribute_type } => Ok(attribute_type),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_set_supertype(
        &self,
        attribute_type: AttributeType,
        supertype: AttributeType,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypeSetSupertype { attribute_type, supertype }))? {
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
    ) -> impl Promise<Result<Option<String>>> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypeGetRegex { attribute_type }))? {
                ThingTypeResponse::AttributeTypeGetRegex { regex } => Ok(regex),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn attribute_type_set_regex(
        &self,
        attribute_type: AttributeType,
        regex: String,
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_type_single(ThingTypeRequest::AttributeTypeSetRegex { attribute_type, regex }))? {
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

    pub(crate) fn role_type_delete(&self, role_type: RoleType) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.role_type_single(RoleTypeRequest::Delete { role_type }))? {
                RoleTypeResponse::Delete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn role_type_set_label(&self, role_type: RoleType, new_label: String) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.role_type_single(RoleTypeRequest::SetLabel { role_type, new_label }))? {
                RoleTypeResponse::SetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn role_type_get_supertype(&self, role_type: RoleType) -> impl Promise<Result<Option<RoleType>>> {
        promisify! {
            match resolve!(self.role_type_single(RoleTypeRequest::GetSupertype { role_type }))? {
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

    pub(crate) fn thing_delete(&self, thing: Thing) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_single(ThingRequest::ThingDelete { thing }))? {
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

    pub(crate) fn thing_set_has(&self, thing: Thing, attribute: Attribute) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_single(ThingRequest::ThingSetHas { thing, attribute }))? {
                ThingResponse::ThingSetHas {} => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn thing_unset_has(&self, thing: Thing, attribute: Attribute) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_single(ThingRequest::ThingUnsetHas { thing, attribute }))? {
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
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_single(ThingRequest::RelationAddRolePlayer { relation, role_type, player }))? {
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
    ) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.thing_single(ThingRequest::RelationRemoveRolePlayer { relation, role_type, player }))? {
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

    pub(crate) fn rule_delete(&self, rule: Rule) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.rule_single(RuleRequest::Delete { label: rule.label }))? {
                RuleResponse::Delete => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn rule_set_label(&self, rule: Rule, new_label: String) -> impl Promise<Result> {
        promisify! {
            match resolve!(self.rule_single(RuleRequest::SetLabel { current_label: rule.label, new_label }))? {
                RuleResponse::SetLabel => Ok(()),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn put_rule(&self, label: String, when: Conjunction, then: Variable) -> impl Promise<Result<Rule>> {
        promisify! {
            match resolve!(self.logic_single(LogicRequest::PutRule { label, when, then }))? {
                LogicResponse::PutRule { rule } => Ok(rule),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    pub(crate) fn get_rule(&self, label: String) -> impl Promise<Result<Option<Rule>>> {
        promisify! {
            match resolve!(self.logic_single(LogicRequest::GetRule { label }))? {
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

    fn single(&self, req: TransactionRequest) -> impl Promise<Result<TransactionResponse>> {
        self.transaction_transmitter.single(req)
    }

    fn query_single(&self, req: QueryRequest) -> impl Promise<Result<QueryResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::Query(req)))? {
                TransactionResponse::Query(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn concept_single(&self, req: ConceptRequest) -> impl Promise<Result<ConceptResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::Concept(req)))? {
                TransactionResponse::Concept(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn thing_type_single(&self, req: ThingTypeRequest) -> impl Promise<Result<ThingTypeResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::ThingType(req)))? {
                TransactionResponse::ThingType(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn role_type_single(&self, req: RoleTypeRequest) -> impl Promise<Result<RoleTypeResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::RoleType(req)))? {
                TransactionResponse::RoleType(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn thing_single(&self, req: ThingRequest) -> impl Promise<Result<ThingResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::Thing(req)))? {
                TransactionResponse::Thing(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn rule_single(&self, req: RuleRequest) -> impl Promise<Result<RuleResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::Rule(req)))? {
                TransactionResponse::Rule(res) => Ok(res),
                other => Err(InternalError::UnexpectedResponseType(format!("{other:?}")).into()),
            }
        }
    }

    fn logic_single(&self, req: LogicRequest) -> impl Promise<Result<LogicResponse>> {
        promisify! {
            match resolve!(self.single(TransactionRequest::Logic(req)))? {
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
