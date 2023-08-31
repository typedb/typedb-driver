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

use std::time::Duration;

use tokio::sync::mpsc::UnboundedSender;
use tonic::Streaming;
use typedb_protocol::transaction;
use typeql_lang::pattern::{Conjunction, Variable};

use crate::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
    common::{address::Address, info::DatabaseInfo, RequestID, SessionID, IID},
    concept::{
        Annotation, Attribute, AttributeType, Entity, EntityType, Relation, RelationType, RoleType, SchemaException,
        Thing, ThingType, Transitivity, Value, ValueType,
    },
    logic::{Explanation, Rule},
    user::User,
    Options, SessionType, TransactionType,
};

#[derive(Debug)]
pub(super) enum Request {
    ConnectionOpen,

    ServersAll,

    DatabasesContains { database_name: String },
    DatabaseCreate { database_name: String },
    DatabaseGet { database_name: String },
    DatabasesAll,

    DatabaseSchema { database_name: String },
    DatabaseTypeSchema { database_name: String },
    DatabaseRuleSchema { database_name: String },
    DatabaseDelete { database_name: String },

    SessionOpen { database_name: String, session_type: SessionType, options: Options },
    SessionClose { session_id: SessionID },
    SessionPulse { session_id: SessionID },

    Transaction(TransactionRequest),

    UsersAll,
    UsersContain { username: String },
    UsersCreate { username: String, password: String },
    UsersDelete { username: String },
    UsersGet { username: String },
    UsersPasswordSet { username: String, password: String },

    UserPasswordUpdate { username: String, password_old: String, password_new: String },
}

#[derive(Debug)]
pub(super) enum Response {
    ConnectionOpen,

    ServersAll {
        servers: Vec<Address>,
    },

    DatabasesContains {
        contains: bool,
    },
    DatabaseCreate,
    DatabaseGet {
        database: DatabaseInfo,
    },
    DatabasesAll {
        databases: Vec<DatabaseInfo>,
    },

    DatabaseDelete,
    DatabaseSchema {
        schema: String,
    },
    DatabaseTypeSchema {
        schema: String,
    },
    DatabaseRuleSchema {
        schema: String,
    },

    SessionOpen {
        session_id: SessionID,
        server_duration: Duration,
    },
    SessionPulse,
    SessionClose,

    TransactionOpen {
        request_sink: UnboundedSender<transaction::Client>,
        response_source: Streaming<transaction::Server>,
    },

    UsersAll {
        users: Vec<User>,
    },
    UsersContain {
        contains: bool,
    },
    UsersCreate,
    UsersDelete,
    UsersGet {
        user: Option<User>,
    },
    UsersPasswordSet,

    UserPasswordUpdate,
}

#[derive(Debug)]
pub(super) enum TransactionRequest {
    Open { session_id: SessionID, transaction_type: TransactionType, options: Options, network_latency: Duration },
    Commit,
    Rollback,
    Query(QueryRequest),
    Concept(ConceptRequest),
    ThingType(ThingTypeRequest),
    RoleType(RoleTypeRequest),
    Thing(ThingRequest),
    Rule(RuleRequest),
    Logic(LogicRequest),
    Stream { request_id: RequestID },
}

#[derive(Debug)]
pub(super) enum TransactionResponse {
    Open,
    Commit,
    Rollback,
    Query(QueryResponse),
    Concept(ConceptResponse),
    ThingType(ThingTypeResponse),
    RoleType(RoleTypeResponse),
    Thing(ThingResponse),
    Rule(RuleResponse),
    Logic(LogicResponse),
}

#[derive(Debug)]
pub(super) enum QueryRequest {
    Define { query: String, options: Options },
    Undefine { query: String, options: Options },
    Delete { query: String, options: Options },

    Match { query: String, options: Options },
    Insert { query: String, options: Options },
    Update { query: String, options: Options },

    MatchAggregate { query: String, options: Options },

    Explain { explainable_id: i64, options: Options }, // TODO: ID type

    MatchGroup { query: String, options: Options },
    MatchGroupAggregate { query: String, options: Options },
}

#[derive(Debug)]
pub(super) enum QueryResponse {
    Define,
    Undefine,
    Delete,

    Match { answers: Vec<ConceptMap> },
    Insert { answers: Vec<ConceptMap> },
    Update { answers: Vec<ConceptMap> },

    MatchAggregate { answer: Numeric },

    Explain { answers: Vec<Explanation> },

    MatchGroup { answers: Vec<ConceptMapGroup> },
    MatchGroupAggregate { answers: Vec<NumericGroup> },
}

#[derive(Debug)]
pub(super) enum ConceptRequest {
    GetEntityType { label: String },
    GetRelationType { label: String },
    GetAttributeType { label: String },
    PutEntityType { label: String },
    PutRelationType { label: String },
    PutAttributeType { label: String, value_type: ValueType },
    GetEntity { iid: IID },
    GetRelation { iid: IID },
    GetAttribute { iid: IID },
    GetSchemaExceptions,
}

#[derive(Debug)]
pub(super) enum ConceptResponse {
    GetEntityType { entity_type: Option<EntityType> },
    GetRelationType { relation_type: Option<RelationType> },
    GetAttributeType { attribute_type: Option<AttributeType> },
    PutEntityType { entity_type: EntityType },
    PutRelationType { relation_type: RelationType },
    PutAttributeType { attribute_type: AttributeType },
    GetEntity { entity: Option<Entity> },
    GetRelation { relation: Option<Relation> },
    GetAttribute { attribute: Option<Attribute> },
    GetSchemaExceptions { exceptions: Vec<SchemaException> },
}

#[derive(Debug)]
pub(super) enum ThingTypeRequest {
    ThingTypeDelete {
        thing_type: ThingType,
    },
    ThingTypeSetLabel {
        thing_type: ThingType,
        new_label: String,
    },
    ThingTypeSetAbstract {
        thing_type: ThingType,
    },
    ThingTypeUnsetAbstract {
        thing_type: ThingType,
    },
    ThingTypeGetOwns {
        thing_type: ThingType,
        value_type: Option<ValueType>,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    },
    ThingTypeGetOwnsOverridden {
        thing_type: ThingType,
        overridden_attribute_type: AttributeType,
    },
    ThingTypeSetOwns {
        thing_type: ThingType,
        attribute_type: AttributeType,
        overridden_attribute_type: Option<AttributeType>,
        annotations: Vec<Annotation>,
    },
    ThingTypeUnsetOwns {
        thing_type: ThingType,
        attribute_type: AttributeType,
    },
    ThingTypeGetPlays {
        thing_type: ThingType,
        transitivity: Transitivity,
    },
    ThingTypeGetPlaysOverridden {
        thing_type: ThingType,
        overridden_role_type: RoleType,
    },
    ThingTypeSetPlays {
        thing_type: ThingType,
        role_type: RoleType,
        overridden_role_type: Option<RoleType>,
    },
    ThingTypeUnsetPlays {
        thing_type: ThingType,
        role_type: RoleType,
    },
    ThingTypeGetSyntax {
        thing_type: ThingType,
    },

    EntityTypeCreate {
        entity_type: EntityType,
    },
    EntityTypeGetSupertype {
        entity_type: EntityType,
    },
    EntityTypeSetSupertype {
        entity_type: EntityType,
        supertype: EntityType,
    },
    EntityTypeGetSupertypes {
        entity_type: EntityType,
    },
    EntityTypeGetSubtypes {
        entity_type: EntityType,
        transitivity: Transitivity,
    },
    EntityTypeGetInstances {
        entity_type: EntityType,
        transitivity: Transitivity,
    },

    RelationTypeCreate {
        relation_type: RelationType,
    },
    RelationTypeGetSupertype {
        relation_type: RelationType,
    },
    RelationTypeSetSupertype {
        relation_type: RelationType,
        supertype: RelationType,
    },
    RelationTypeGetSupertypes {
        relation_type: RelationType,
    },
    RelationTypeGetSubtypes {
        relation_type: RelationType,
        transitivity: Transitivity,
    },
    RelationTypeGetInstances {
        relation_type: RelationType,
        transitivity: Transitivity,
    },
    RelationTypeGetRelates {
        relation_type: RelationType,
        transitivity: Transitivity,
    },
    RelationTypeGetRelatesForRoleLabel {
        relation_type: RelationType,
        role_label: String,
    },
    RelationTypeGetRelatesOverridden {
        relation_type: RelationType,
        role_label: String,
    },
    RelationTypeSetRelates {
        relation_type: RelationType,
        role_label: String,
        overridden_role_label: Option<String>,
    },
    RelationTypeUnsetRelates {
        relation_type: RelationType,
        role_label: String,
    },

    AttributeTypePut {
        attribute_type: AttributeType,
        value: Value,
    },
    AttributeTypeGet {
        attribute_type: AttributeType,
        value: Value,
    },
    AttributeTypeGetSupertype {
        attribute_type: AttributeType,
    },
    AttributeTypeSetSupertype {
        attribute_type: AttributeType,
        supertype: AttributeType,
    },
    AttributeTypeGetSupertypes {
        attribute_type: AttributeType,
    },
    AttributeTypeGetSubtypes {
        attribute_type: AttributeType,
        transitivity: Transitivity,
        value_type: Option<ValueType>,
    },
    AttributeTypeGetInstances {
        attribute_type: AttributeType,
        transitivity: Transitivity,
        value_type: Option<ValueType>,
    },
    AttributeTypeGetRegex {
        attribute_type: AttributeType,
    },
    AttributeTypeSetRegex {
        attribute_type: AttributeType,
        regex: String,
    },
    AttributeTypeGetOwners {
        attribute_type: AttributeType,
        transitivity: Transitivity,
        annotations: Vec<Annotation>,
    },
}

#[derive(Debug)]
pub(super) enum ThingTypeResponse {
    ThingTypeDelete,
    ThingTypeSetLabel,
    ThingTypeSetAbstract,
    ThingTypeUnsetAbstract,
    ThingTypeGetOwns { attribute_types: Vec<AttributeType> },
    ThingTypeGetOwnsOverridden { attribute_type: Option<AttributeType> },
    ThingTypeSetOwns,
    ThingTypeUnsetOwns,
    ThingTypeGetPlays { role_types: Vec<RoleType> },
    ThingTypeGetPlaysOverridden { role_type: Option<RoleType> },
    ThingTypeSetPlays,
    ThingTypeUnsetPlays,
    ThingTypeGetSyntax { syntax: String },

    EntityTypeCreate { entity: Entity },
    EntityTypeGetSupertype { entity_type: EntityType },
    EntityTypeSetSupertype,
    EntityTypeGetSupertypes { entity_types: Vec<EntityType> },
    EntityTypeGetSubtypes { entity_types: Vec<EntityType> },
    EntityTypeGetInstances { entities: Vec<Entity> },

    RelationTypeCreate { relation: Relation },
    RelationTypeGetSupertype { relation_type: RelationType },
    RelationTypeSetSupertype,
    RelationTypeGetSupertypes { relation_types: Vec<RelationType> },
    RelationTypeGetSubtypes { relation_types: Vec<RelationType> },
    RelationTypeGetInstances { relations: Vec<Relation> },
    RelationTypeGetRelates { role_types: Vec<RoleType> },
    RelationTypeGetRelatesForRoleLabel { role_type: Option<RoleType> },
    RelationTypeGetRelatesOverridden { role_type: Option<RoleType> },
    RelationTypeSetRelates,
    RelationTypeUnsetRelates,

    AttributeTypePut { attribute: Attribute },
    AttributeTypeGet { attribute: Option<Attribute> },
    AttributeTypeGetSupertype { attribute_type: AttributeType },
    AttributeTypeSetSupertype,
    AttributeTypeGetSupertypes { attribute_types: Vec<AttributeType> },
    AttributeTypeGetSubtypes { attribute_types: Vec<AttributeType> },
    AttributeTypeGetInstances { attributes: Vec<Attribute> },
    AttributeTypeGetRegex { regex: Option<String> },
    AttributeTypeSetRegex,
    AttributeTypeGetOwners { thing_types: Vec<ThingType> },
}

#[derive(Debug)]
pub(super) enum RoleTypeRequest {
    Delete { role_type: RoleType },
    SetLabel { role_type: RoleType, new_label: String },
    GetSupertype { role_type: RoleType },
    GetSupertypes { role_type: RoleType },
    GetSubtypes { role_type: RoleType, transitivity: Transitivity },
    GetRelationTypes { role_type: RoleType },
    GetPlayerTypes { role_type: RoleType, transitivity: Transitivity },
    GetRelationInstances { role_type: RoleType, transitivity: Transitivity },
    GetPlayerInstances { role_type: RoleType, transitivity: Transitivity },
}

#[derive(Debug)]
pub(super) enum RoleTypeResponse {
    Delete,
    SetLabel,
    GetSupertype { role_type: RoleType },
    GetSupertypes { role_types: Vec<RoleType> },
    GetSubtypes { role_types: Vec<RoleType> },
    GetRelationTypes { relation_types: Vec<RelationType> },
    GetPlayerTypes { thing_types: Vec<ThingType> },
    GetRelationInstances { relations: Vec<Relation> },
    GetPlayerInstances { things: Vec<Thing> },
}

#[derive(Debug)]
pub(super) enum ThingRequest {
    ThingDelete { thing: Thing },
    ThingGetHas { thing: Thing, attribute_types: Vec<AttributeType>, annotations: Vec<Annotation> },
    ThingSetHas { thing: Thing, attribute: Attribute },
    ThingUnsetHas { thing: Thing, attribute: Attribute },
    ThingGetRelations { thing: Thing, role_types: Vec<RoleType> },
    ThingGetPlaying { thing: Thing },

    RelationAddRolePlayer { relation: Relation, role_type: RoleType, player: Thing },
    RelationRemoveRolePlayer { relation: Relation, role_type: RoleType, player: Thing },
    RelationGetPlayersByRoleType { relation: Relation, role_types: Vec<RoleType> },
    RelationGetRolePlayers { relation: Relation },
    RelationGetRelating { relation: Relation },

    AttributeGetOwners { attribute: Attribute, thing_type: Option<ThingType> },
}

#[derive(Debug)]
pub(super) enum ThingResponse {
    ThingDelete,
    ThingGetHas { attributes: Vec<Attribute> },
    ThingSetHas,
    ThingUnsetHas,
    ThingGetRelations { relations: Vec<Relation> },
    ThingGetPlaying { role_types: Vec<RoleType> },

    RelationAddRolePlayer,
    RelationRemoveRolePlayer,
    RelationGetPlayersByRoleType { things: Vec<Thing> },
    RelationGetRolePlayers { role_players: Vec<(RoleType, Thing)> }, // TODO tuple => struct
    RelationGetRelating { role_types: Vec<RoleType> },

    AttributeGetOwners { owners: Vec<Thing> },
}

#[derive(Debug)]
pub(super) enum RuleRequest {
    Delete { label: String },
    SetLabel { current_label: String, new_label: String },
}

#[derive(Debug)]
pub(super) enum RuleResponse {
    Delete,
    SetLabel,
}

#[derive(Debug)]
pub(super) enum LogicRequest {
    PutRule { label: String, when: Conjunction, then: Variable },
    GetRule { label: String },
    GetRules,
}

#[derive(Debug)]
pub(super) enum LogicResponse {
    PutRule { rule: Rule },
    GetRule { rule: Option<Rule> },
    GetRules { rules: Vec<Rule> },
}
