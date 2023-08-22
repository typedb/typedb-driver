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

use itertools::Itertools;
use typedb_protocol::{
    attribute, attribute_type, concept_manager, connection, database, database_manager, entity_type, logic_manager,
    query_manager, r#type, relation, relation_type, role_type, rule, server_manager, session, thing, thing_type,
    transaction, user, user_manager, Version::Version,
};

use super::{FromProto, IntoProto, TryFromProto, TryIntoProto};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
    common::{info::DatabaseInfo, RequestID, Result},
    concept::{
        Annotation, Attribute, AttributeType, Entity, EntityType, Relation, RelationType, RoleType, SchemaException,
        Thing, ThingType, ValueType,
    },
    connection::message::{
        ConceptRequest, ConceptResponse, LogicRequest, LogicResponse, QueryRequest, QueryResponse, Request, Response,
        RoleTypeRequest, RoleTypeResponse, RuleRequest, RuleResponse, ThingRequest, ThingResponse, ThingTypeRequest,
        ThingTypeResponse, TransactionRequest, TransactionResponse,
    },
    error::{ConnectionError, InternalError},
    logic::{Explanation, Rule},
    user::User,
};

impl TryIntoProto<connection::open::Req> for Request {
    fn try_into_proto(self) -> Result<connection::open::Req> {
        match self {
            Self::ConnectionOpen => Ok(connection::open::Req { version: Version.into() }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<server_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<server_manager::all::Req> {
        match self {
            Self::ServersAll => Ok(server_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database_manager::contains::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::contains::Req> {
        match self {
            Self::DatabasesContains { database_name } => Ok(database_manager::contains::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database_manager::create::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::create::Req> {
        match self {
            Self::DatabaseCreate { database_name } => Ok(database_manager::create::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database_manager::get::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::get::Req> {
        match self {
            Self::DatabaseGet { database_name } => Ok(database_manager::get::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::all::Req> {
        match self {
            Self::DatabasesAll => Ok(database_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database::delete::Req> for Request {
    fn try_into_proto(self) -> Result<database::delete::Req> {
        match self {
            Self::DatabaseDelete { database_name } => Ok(database::delete::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database::schema::Req> for Request {
    fn try_into_proto(self) -> Result<database::schema::Req> {
        match self {
            Self::DatabaseSchema { database_name } => Ok(database::schema::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database::type_schema::Req> for Request {
    fn try_into_proto(self) -> Result<database::type_schema::Req> {
        match self {
            Self::DatabaseTypeSchema { database_name } => Ok(database::type_schema::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<database::rule_schema::Req> for Request {
    fn try_into_proto(self) -> Result<database::rule_schema::Req> {
        match self {
            Self::DatabaseRuleSchema { database_name } => Ok(database::rule_schema::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<session::open::Req> for Request {
    fn try_into_proto(self) -> Result<session::open::Req> {
        match self {
            Self::SessionOpen { database_name, session_type, options } => Ok(session::open::Req {
                database: database_name,
                r#type: session_type.into_proto(),
                options: Some(options.into_proto()),
            }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<session::pulse::Req> for Request {
    fn try_into_proto(self) -> Result<session::pulse::Req> {
        match self {
            Self::SessionPulse { session_id } => Ok(session::pulse::Req { session_id: session_id.into() }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<session::close::Req> for Request {
    fn try_into_proto(self) -> Result<session::close::Req> {
        match self {
            Self::SessionClose { session_id } => Ok(session::close::Req { session_id: session_id.into() }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<transaction::Client> for Request {
    fn try_into_proto(self) -> Result<transaction::Client> {
        match self {
            Self::Transaction(transaction_req) => Ok(transaction::Client { reqs: vec![transaction_req.into_proto()] }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::all::Req> {
        match self {
            Self::UsersAll => Ok(user_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::contains::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::contains::Req> {
        match self {
            Self::UsersContain { username } => Ok(user_manager::contains::Req { username }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::create::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::create::Req> {
        match self {
            Self::UsersCreate { username, password } => Ok(user_manager::create::Req { username, password }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::delete::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::delete::Req> {
        match self {
            Self::UsersDelete { username } => Ok(user_manager::delete::Req { username }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::get::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::get::Req> {
        match self {
            Self::UsersGet { username } => Ok(user_manager::get::Req { username }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user_manager::password_set::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::password_set::Req> {
        match self {
            Self::UsersPasswordSet { username, password } => Ok(user_manager::password_set::Req { username, password }),
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl TryIntoProto<user::password_update::Req> for Request {
    fn try_into_proto(self) -> Result<user::password_update::Req> {
        match self {
            Self::UserPasswordUpdate { username, password_old, password_new } => {
                Ok(user::password_update::Req { username, password_old, password_new })
            }
            other => Err(InternalError::UnexpectedRequestType(format!("{other:?}")).into()),
        }
    }
}

impl FromProto<connection::open::Res> for Response {
    fn from_proto(_proto: connection::open::Res) -> Self {
        Self::ConnectionOpen
    }
}

impl TryFromProto<server_manager::all::Res> for Response {
    fn try_from_proto(proto: server_manager::all::Res) -> Result<Self> {
        let servers = proto.servers.into_iter().map(|server| server.address.parse()).try_collect()?;
        Ok(Self::ServersAll { servers })
    }
}

impl FromProto<database_manager::contains::Res> for Response {
    fn from_proto(proto: database_manager::contains::Res) -> Self {
        Self::DatabasesContains { contains: proto.contains }
    }
}

impl FromProto<database_manager::create::Res> for Response {
    fn from_proto(_proto: database_manager::create::Res) -> Self {
        Self::DatabaseCreate
    }
}

impl TryFromProto<database_manager::get::Res> for Response {
    fn try_from_proto(proto: database_manager::get::Res) -> Result<Self> {
        Ok(Self::DatabaseGet {
            database: DatabaseInfo::try_from_proto(
                proto.database.ok_or(ConnectionError::MissingResponseField("database"))?,
            )?,
        })
    }
}

impl TryFromProto<database_manager::all::Res> for Response {
    fn try_from_proto(proto: database_manager::all::Res) -> Result<Self> {
        Ok(Self::DatabasesAll {
            databases: proto.databases.into_iter().map(DatabaseInfo::try_from_proto).try_collect()?,
        })
    }
}

impl FromProto<database::delete::Res> for Response {
    fn from_proto(_proto: database::delete::Res) -> Self {
        Self::DatabaseDelete
    }
}

impl FromProto<database::schema::Res> for Response {
    fn from_proto(proto: database::schema::Res) -> Self {
        Self::DatabaseSchema { schema: proto.schema }
    }
}

impl FromProto<database::type_schema::Res> for Response {
    fn from_proto(proto: database::type_schema::Res) -> Self {
        Self::DatabaseTypeSchema { schema: proto.schema }
    }
}

impl FromProto<database::rule_schema::Res> for Response {
    fn from_proto(proto: database::rule_schema::Res) -> Self {
        Self::DatabaseRuleSchema { schema: proto.schema }
    }
}

impl FromProto<session::open::Res> for Response {
    fn from_proto(proto: session::open::Res) -> Self {
        Self::SessionOpen {
            session_id: proto.session_id.into(),
            server_duration: Duration::from_millis(proto.server_duration_millis as u64),
        }
    }
}

impl FromProto<session::pulse::Res> for Response {
    fn from_proto(_proto: session::pulse::Res) -> Self {
        Self::SessionPulse
    }
}

impl FromProto<session::close::Res> for Response {
    fn from_proto(_proto: session::close::Res) -> Self {
        Self::SessionClose
    }
}

impl IntoProto<transaction::Req> for TransactionRequest {
    fn into_proto(self) -> transaction::Req {
        let mut request_id = None;

        let req = match self {
            Self::Open { session_id, transaction_type, options, network_latency } => {
                transaction::req::Req::OpenReq(transaction::open::Req {
                    session_id: session_id.into(),
                    r#type: transaction_type.into_proto(),
                    options: Some(options.into_proto()),
                    network_latency_millis: network_latency.as_millis() as i32,
                })
            }
            Self::Commit => transaction::req::Req::CommitReq(transaction::commit::Req {}),
            Self::Rollback => transaction::req::Req::RollbackReq(transaction::rollback::Req {}),
            Self::Query(query_request) => transaction::req::Req::QueryManagerReq(query_request.into_proto()),
            Self::Concept(concept_request) => transaction::req::Req::ConceptManagerReq(concept_request.into_proto()),
            Self::ThingType(thing_type_request) => transaction::req::Req::TypeReq(thing_type_request.into_proto()),
            Self::RoleType(role_type_request) => transaction::req::Req::TypeReq(role_type_request.into_proto()),
            Self::Thing(thing_request) => transaction::req::Req::ThingReq(thing_request.into_proto()),
            Self::Rule(rule_request) => transaction::req::Req::RuleReq(rule_request.into_proto()),
            Self::Logic(logic_request) => transaction::req::Req::LogicManagerReq(logic_request.into_proto()),
            Self::Stream { request_id: req_id } => {
                request_id = Some(req_id);
                transaction::req::Req::StreamReq(transaction::stream::Req {})
            }
        };

        transaction::Req {
            req_id: request_id.unwrap_or_else(RequestID::generate).into(),
            metadata: Default::default(),
            req: Some(req),
        }
    }
}

impl TryFromProto<transaction::Res> for TransactionResponse {
    fn try_from_proto(proto: transaction::Res) -> Result<Self> {
        match proto.res {
            Some(transaction::res::Res::OpenRes(_)) => Ok(Self::Open),
            Some(transaction::res::Res::CommitRes(_)) => Ok(Self::Commit),
            Some(transaction::res::Res::RollbackRes(_)) => Ok(Self::Rollback),
            Some(transaction::res::Res::QueryManagerRes(res)) => Ok(Self::Query(QueryResponse::try_from_proto(res)?)),
            Some(transaction::res::Res::ConceptManagerRes(res)) => {
                Ok(Self::Concept(ConceptResponse::try_from_proto(res)?))
            }
            Some(transaction::res::Res::TypeRes(r#type::Res { res: Some(r#type::res::Res::ThingTypeRes(res)) })) => {
                Ok(Self::ThingType(ThingTypeResponse::try_from_proto(res)?))
            }
            Some(transaction::res::Res::TypeRes(r#type::Res { res: Some(r#type::res::Res::RoleTypeRes(res)) })) => {
                Ok(Self::RoleType(RoleTypeResponse::try_from_proto(res)?))
            }
            Some(transaction::res::Res::TypeRes(r#type::Res { res: None })) => {
                Err(ConnectionError::MissingResponseField("res").into())
            }
            Some(transaction::res::Res::ThingRes(res)) => Ok(Self::Thing(ThingResponse::try_from_proto(res)?)),
            Some(transaction::res::Res::RuleRes(res)) => Ok(Self::Rule(RuleResponse::try_from_proto(res)?)),
            Some(transaction::res::Res::LogicManagerRes(res)) => Ok(Self::Logic(LogicResponse::try_from_proto(res)?)),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<transaction::ResPart> for TransactionResponse {
    fn try_from_proto(proto: transaction::ResPart) -> Result<Self> {
        match proto.res {
            Some(transaction::res_part::Res::QueryManagerResPart(res_part)) => {
                Ok(Self::Query(QueryResponse::try_from_proto(res_part)?))
            }
            Some(transaction::res_part::Res::TypeResPart(r#type::ResPart {
                res: Some(r#type::res_part::Res::ThingTypeResPart(res)),
            })) => Ok(Self::ThingType(ThingTypeResponse::try_from_proto(res)?)),
            Some(transaction::res_part::Res::TypeResPart(r#type::ResPart {
                res: Some(r#type::res_part::Res::RoleTypeResPart(res)),
            })) => Ok(Self::RoleType(RoleTypeResponse::try_from_proto(res)?)),
            Some(transaction::res_part::Res::TypeResPart(r#type::ResPart { res: None })) => {
                Err(ConnectionError::MissingResponseField("res").into())
            }
            Some(transaction::res_part::Res::ThingResPart(res)) => Ok(Self::Thing(ThingResponse::try_from_proto(res)?)),
            Some(transaction::res_part::Res::LogicManagerResPart(res)) => {
                Ok(Self::Logic(LogicResponse::try_from_proto(res)?))
            }
            Some(transaction::res_part::Res::StreamResPart(_)) => unreachable!(),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl FromProto<user_manager::all::Res> for Response {
    fn from_proto(proto: user_manager::all::Res) -> Self {
        Self::UsersAll { users: proto.users.into_iter().map(User::from_proto).collect() }
    }
}

impl FromProto<user_manager::contains::Res> for Response {
    fn from_proto(proto: user_manager::contains::Res) -> Self {
        Self::UsersContain { contains: proto.contains }
    }
}

impl FromProto<user_manager::create::Res> for Response {
    fn from_proto(_proto: user_manager::create::Res) -> Self {
        Self::UsersCreate
    }
}

impl FromProto<user_manager::delete::Res> for Response {
    fn from_proto(_proto: user_manager::delete::Res) -> Self {
        Self::UsersDelete
    }
}

impl FromProto<user_manager::get::Res> for Response {
    fn from_proto(proto: user_manager::get::Res) -> Self {
        Self::UsersGet { user: proto.user.map(User::from_proto) }
    }
}

impl FromProto<user_manager::password_set::Res> for Response {
    fn from_proto(_proto: user_manager::password_set::Res) -> Self {
        Self::UsersPasswordSet
    }
}

impl FromProto<user::password_update::Res> for Response {
    fn from_proto(_proto: user::password_update::Res) -> Self {
        Self::UserPasswordUpdate
    }
}

impl IntoProto<query_manager::Req> for QueryRequest {
    fn into_proto(self) -> query_manager::Req {
        let (req, options) = match self {
            Self::Define { query, options } => {
                (query_manager::req::Req::DefineReq(query_manager::define::Req { query }), options)
            }
            Self::Undefine { query, options } => {
                (query_manager::req::Req::UndefineReq(query_manager::undefine::Req { query }), options)
            }
            Self::Delete { query, options } => {
                (query_manager::req::Req::DeleteReq(query_manager::delete::Req { query }), options)
            }

            Self::Match { query, options } => {
                (query_manager::req::Req::MatchReq(query_manager::r#match::Req { query }), options)
            }
            Self::Insert { query, options } => {
                (query_manager::req::Req::InsertReq(query_manager::insert::Req { query }), options)
            }
            Self::Update { query, options } => {
                (query_manager::req::Req::UpdateReq(query_manager::update::Req { query }), options)
            }

            Self::MatchAggregate { query, options } => {
                (query_manager::req::Req::MatchAggregateReq(query_manager::match_aggregate::Req { query }), options)
            }

            Self::MatchGroup { query, options } => {
                (query_manager::req::Req::MatchGroupReq(query_manager::match_group::Req { query }), options)
            }
            Self::MatchGroupAggregate { query, options } => (
                query_manager::req::Req::MatchGroupAggregateReq(query_manager::match_group_aggregate::Req { query }),
                options,
            ),

            Self::Explain { explainable_id, options } => {
                (query_manager::req::Req::ExplainReq(query_manager::explain::Req { explainable_id }), options)
            }
        };
        query_manager::Req { req: Some(req), options: Some(options.into_proto()) }
    }
}

impl TryFromProto<query_manager::Res> for QueryResponse {
    fn try_from_proto(proto: query_manager::Res) -> Result<Self> {
        match proto.res {
            Some(query_manager::res::Res::DefineRes(_)) => Ok(Self::Define),
            Some(query_manager::res::Res::UndefineRes(_)) => Ok(Self::Undefine),
            Some(query_manager::res::Res::DeleteRes(_)) => Ok(Self::Delete),
            Some(query_manager::res::Res::MatchAggregateRes(res)) => Ok(Self::MatchAggregate {
                answer: Numeric::try_from_proto(res.answer.ok_or(ConnectionError::MissingResponseField("answer"))?)?,
            }),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<query_manager::ResPart> for QueryResponse {
    fn try_from_proto(proto: query_manager::ResPart) -> Result<Self> {
        match proto.res {
            Some(query_manager::res_part::Res::MatchResPart(res)) => {
                Ok(Self::Match { answers: res.answers.into_iter().map(ConceptMap::try_from_proto).try_collect()? })
            }
            Some(query_manager::res_part::Res::InsertResPart(res)) => {
                Ok(Self::Insert { answers: res.answers.into_iter().map(ConceptMap::try_from_proto).try_collect()? })
            }
            Some(query_manager::res_part::Res::UpdateResPart(res)) => {
                Ok(Self::Update { answers: res.answers.into_iter().map(ConceptMap::try_from_proto).try_collect()? })
            }
            Some(query_manager::res_part::Res::MatchGroupResPart(res)) => Ok(Self::MatchGroup {
                answers: res.answers.into_iter().map(ConceptMapGroup::try_from_proto).try_collect()?,
            }),
            Some(query_manager::res_part::Res::MatchGroupAggregateResPart(res)) => Ok(Self::MatchGroupAggregate {
                answers: res.answers.into_iter().map(NumericGroup::try_from_proto).try_collect()?,
            }),
            Some(query_manager::res_part::Res::ExplainResPart(res)) => Ok(Self::Explain {
                answers: res.explanations.into_iter().map(Explanation::try_from_proto).try_collect()?,
            }),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<concept_manager::Req> for ConceptRequest {
    fn into_proto(self) -> concept_manager::Req {
        let req = match self {
            Self::GetEntityType { label } => {
                concept_manager::req::Req::GetEntityTypeReq(concept_manager::get_entity_type::Req { label })
            }
            Self::GetRelationType { label } => {
                concept_manager::req::Req::GetRelationTypeReq(concept_manager::get_relation_type::Req { label })
            }
            Self::GetAttributeType { label } => {
                concept_manager::req::Req::GetAttributeTypeReq(concept_manager::get_attribute_type::Req { label })
            }
            Self::PutEntityType { label } => {
                concept_manager::req::Req::PutEntityTypeReq(concept_manager::put_entity_type::Req { label })
            }
            Self::PutRelationType { label } => {
                concept_manager::req::Req::PutRelationTypeReq(concept_manager::put_relation_type::Req { label })
            }
            Self::PutAttributeType { label, value_type } => {
                concept_manager::req::Req::PutAttributeTypeReq(concept_manager::put_attribute_type::Req {
                    label,
                    value_type: value_type.into_proto(),
                })
            }
            Self::GetEntity { iid } => {
                concept_manager::req::Req::GetEntityReq(concept_manager::get_entity::Req { iid: iid.into() })
            }
            Self::GetRelation { iid } => {
                concept_manager::req::Req::GetRelationReq(concept_manager::get_relation::Req { iid: iid.into() })
            }
            Self::GetAttribute { iid } => {
                concept_manager::req::Req::GetAttributeReq(concept_manager::get_attribute::Req { iid: iid.into() })
            }
            Self::GetSchemaExceptions => {
                concept_manager::req::Req::GetSchemaExceptionsReq(concept_manager::get_schema_exceptions::Req {})
            }
        };
        concept_manager::Req { req: Some(req) }
    }
}

impl TryFromProto<concept_manager::Res> for ConceptResponse {
    fn try_from_proto(proto: concept_manager::Res) -> Result<Self> {
        match proto.res {
            Some(concept_manager::res::Res::GetEntityTypeRes(concept_manager::get_entity_type::Res {
                entity_type,
            })) => Ok(Self::GetEntityType { entity_type: entity_type.map(EntityType::from_proto) }),
            Some(concept_manager::res::Res::GetRelationTypeRes(concept_manager::get_relation_type::Res {
                relation_type,
            })) => Ok(Self::GetRelationType { relation_type: relation_type.map(RelationType::from_proto) }),
            Some(concept_manager::res::Res::GetAttributeTypeRes(concept_manager::get_attribute_type::Res {
                attribute_type,
            })) => Ok(Self::GetAttributeType {
                attribute_type: attribute_type.map(AttributeType::try_from_proto).transpose()?,
            }),
            Some(concept_manager::res::Res::PutEntityTypeRes(concept_manager::put_entity_type::Res {
                entity_type,
            })) => Ok(Self::PutEntityType {
                entity_type: EntityType::from_proto(
                    entity_type.ok_or(ConnectionError::MissingResponseField("entity_type"))?,
                ),
            }),
            Some(concept_manager::res::Res::PutRelationTypeRes(concept_manager::put_relation_type::Res {
                relation_type,
            })) => Ok(Self::PutRelationType {
                relation_type: RelationType::from_proto(
                    relation_type.ok_or(ConnectionError::MissingResponseField("relation_type"))?,
                ),
            }),
            Some(concept_manager::res::Res::PutAttributeTypeRes(concept_manager::put_attribute_type::Res {
                attribute_type,
            })) => Ok(Self::PutAttributeType {
                attribute_type: AttributeType::try_from_proto(
                    attribute_type.ok_or(ConnectionError::MissingResponseField("attribute_type"))?,
                )?,
            }),
            Some(concept_manager::res::Res::GetEntityRes(concept_manager::get_entity::Res { entity })) => {
                Ok(Self::GetEntity { entity: entity.map(Entity::try_from_proto).transpose()? })
            }
            Some(concept_manager::res::Res::GetRelationRes(concept_manager::get_relation::Res { relation })) => {
                Ok(Self::GetRelation { relation: relation.map(Relation::try_from_proto).transpose()? })
            }
            Some(concept_manager::res::Res::GetAttributeRes(concept_manager::get_attribute::Res { attribute })) => {
                Ok(Self::GetAttribute { attribute: attribute.map(Attribute::try_from_proto).transpose()? })
            }
            Some(concept_manager::res::Res::GetSchemaExceptionsRes(concept_manager::get_schema_exceptions::Res {
                exceptions,
            })) => Ok(Self::GetSchemaExceptions {
                exceptions: exceptions.into_iter().map(SchemaException::from_proto).collect(),
            }),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<r#type::Req> for ThingTypeRequest {
    fn into_proto(self) -> r#type::Req {
        let (req, label) = match self {
            Self::ThingTypeDelete { thing_type } => {
                (thing_type::req::Req::ThingTypeDeleteReq(thing_type::delete::Req {}), thing_type.label().to_owned())
            }
            Self::ThingTypeSetLabel { thing_type, new_label } => (
                thing_type::req::Req::ThingTypeSetLabelReq(thing_type::set_label::Req { label: new_label }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeSetAbstract { thing_type } => (
                thing_type::req::Req::ThingTypeSetAbstractReq(thing_type::set_abstract::Req {}),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeUnsetAbstract { thing_type } => (
                thing_type::req::Req::ThingTypeUnsetAbstractReq(thing_type::unset_abstract::Req {}),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeGetOwns { thing_type, value_type, transitivity, annotations } => (
                thing_type::req::Req::ThingTypeGetOwnsReq(thing_type::get_owns::Req {
                    value_type: value_type.map(IntoProto::into_proto),
                    transitivity: transitivity.into_proto(),
                    annotations: annotations.into_iter().map(Annotation::into_proto).collect(),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeGetOwnsOverridden { thing_type, overridden_attribute_type } => (
                thing_type::req::Req::ThingTypeGetOwnsOverriddenReq(thing_type::get_owns_overridden::Req {
                    attribute_type: Some(overridden_attribute_type.into_proto()),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeSetOwns { thing_type, attribute_type, overridden_attribute_type, annotations } => (
                thing_type::req::Req::ThingTypeSetOwnsReq(thing_type::set_owns::Req {
                    attribute_type: Some(attribute_type.into_proto()),
                    overridden_type: overridden_attribute_type.map(AttributeType::into_proto),
                    annotations: annotations.into_iter().map(Annotation::into_proto).collect(),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeUnsetOwns { thing_type, attribute_type } => (
                thing_type::req::Req::ThingTypeUnsetOwnsReq(thing_type::unset_owns::Req {
                    attribute_type: Some(attribute_type.into_proto()),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeGetPlays { thing_type, transitivity } => (
                thing_type::req::Req::ThingTypeGetPlaysReq(thing_type::get_plays::Req {
                    transitivity: transitivity.into_proto(),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeGetPlaysOverridden { thing_type, overridden_role_type } => (
                thing_type::req::Req::ThingTypeGetPlaysOverriddenReq(thing_type::get_plays_overridden::Req {
                    role_type: Some(overridden_role_type.into_proto()),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeSetPlays { thing_type, role_type, overridden_role_type } => (
                thing_type::req::Req::ThingTypeSetPlaysReq(thing_type::set_plays::Req {
                    role_type: Some(role_type.into_proto()),
                    overridden_role_type: overridden_role_type.map(RoleType::into_proto),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeUnsetPlays { thing_type, role_type } => (
                thing_type::req::Req::ThingTypeUnsetPlaysReq(thing_type::unset_plays::Req {
                    role_type: Some(role_type.into_proto()),
                }),
                thing_type.label().to_owned(),
            ),
            Self::ThingTypeGetSyntax { thing_type } => (
                thing_type::req::Req::ThingTypeGetSyntaxReq(thing_type::get_syntax::Req {}),
                thing_type.label().to_owned(),
            ),
            Self::EntityTypeCreate { entity_type } => {
                (thing_type::req::Req::EntityTypeCreateReq(entity_type::create::Req {}), entity_type.label)
            }
            Self::EntityTypeGetSupertype { entity_type } => {
                (thing_type::req::Req::EntityTypeGetSupertypeReq(entity_type::get_supertype::Req {}), entity_type.label)
            }
            Self::EntityTypeSetSupertype { entity_type, supertype } => (
                thing_type::req::Req::EntityTypeSetSupertypeReq(entity_type::set_supertype::Req {
                    entity_type: Some(supertype.into_proto()),
                }),
                entity_type.label,
            ),
            Self::EntityTypeGetSupertypes { entity_type } => (
                thing_type::req::Req::EntityTypeGetSupertypesReq(entity_type::get_supertypes::Req {}),
                entity_type.label,
            ),
            Self::EntityTypeGetSubtypes { entity_type, transitivity } => (
                thing_type::req::Req::EntityTypeGetSubtypesReq(entity_type::get_subtypes::Req {
                    transitivity: transitivity.into_proto(),
                }),
                entity_type.label,
            ),
            Self::EntityTypeGetInstances { entity_type, transitivity } => (
                thing_type::req::Req::EntityTypeGetInstancesReq(entity_type::get_instances::Req {
                    transitivity: transitivity.into_proto(),
                }),
                entity_type.label,
            ),
            Self::RelationTypeCreate { relation_type } => {
                (thing_type::req::Req::RelationTypeCreateReq(relation_type::create::Req {}), relation_type.label)
            }
            Self::RelationTypeGetSupertype { relation_type } => (
                thing_type::req::Req::RelationTypeGetSupertypeReq(relation_type::get_supertype::Req {}),
                relation_type.label,
            ),
            Self::RelationTypeSetSupertype { relation_type, supertype } => (
                thing_type::req::Req::RelationTypeSetSupertypeReq(relation_type::set_supertype::Req {
                    relation_type: Some(supertype.into_proto()),
                }),
                relation_type.label,
            ),
            Self::RelationTypeGetSupertypes { relation_type } => (
                thing_type::req::Req::RelationTypeGetSupertypesReq(relation_type::get_supertypes::Req {}),
                relation_type.label,
            ),
            Self::RelationTypeGetSubtypes { relation_type, transitivity } => (
                thing_type::req::Req::RelationTypeGetSubtypesReq(relation_type::get_subtypes::Req {
                    transitivity: transitivity.into_proto(),
                }),
                relation_type.label,
            ),
            Self::RelationTypeGetInstances { relation_type, transitivity } => (
                thing_type::req::Req::RelationTypeGetInstancesReq(relation_type::get_instances::Req {
                    transitivity: transitivity.into_proto(),
                }),
                relation_type.label,
            ),
            Self::RelationTypeGetRelates { relation_type, transitivity } => (
                thing_type::req::Req::RelationTypeGetRelatesReq(relation_type::get_relates::Req {
                    transitivity: transitivity.into_proto(),
                }),
                relation_type.label,
            ),
            Self::RelationTypeGetRelatesForRoleLabel { relation_type, role_label } => (
                thing_type::req::Req::RelationTypeGetRelatesForRoleLabelReq(
                    relation_type::get_relates_for_role_label::Req { label: role_label },
                ),
                relation_type.label,
            ),
            Self::RelationTypeGetRelatesOverridden { relation_type, role_label } => (
                thing_type::req::Req::RelationTypeGetRelatesOverriddenReq(relation_type::get_relates_overridden::Req {
                    label: role_label,
                }),
                relation_type.label,
            ),
            Self::RelationTypeSetRelates { relation_type, role_label, overridden_role_label } => (
                thing_type::req::Req::RelationTypeSetRelatesReq(relation_type::set_relates::Req {
                    label: role_label,
                    overridden_label: overridden_role_label,
                }),
                relation_type.label,
            ),
            Self::RelationTypeUnsetRelates { relation_type, role_label } => (
                thing_type::req::Req::RelationTypeUnsetRelatesReq(relation_type::unset_relates::Req {
                    label: role_label,
                }),
                relation_type.label,
            ),
            Self::AttributeTypePut { attribute_type, value } => (
                thing_type::req::Req::AttributeTypePutReq(attribute_type::put::Req { value: Some(value.into_proto()) }),
                attribute_type.label,
            ),
            Self::AttributeTypeGet { attribute_type, value } => (
                thing_type::req::Req::AttributeTypeGetReq(attribute_type::get::Req { value: Some(value.into_proto()) }),
                attribute_type.label,
            ),
            Self::AttributeTypeGetSupertype { attribute_type } => (
                thing_type::req::Req::AttributeTypeGetSupertypeReq(attribute_type::get_supertype::Req {}),
                attribute_type.label,
            ),
            Self::AttributeTypeSetSupertype { attribute_type, supertype } => (
                thing_type::req::Req::AttributeTypeSetSupertypeReq(attribute_type::set_supertype::Req {
                    attribute_type: Some(supertype.into_proto()),
                }),
                attribute_type.label,
            ),
            Self::AttributeTypeGetSupertypes { attribute_type } => (
                thing_type::req::Req::AttributeTypeGetSupertypesReq(attribute_type::get_supertypes::Req {}),
                attribute_type.label,
            ),
            Self::AttributeTypeGetSubtypes { attribute_type, transitivity, value_type } => (
                thing_type::req::Req::AttributeTypeGetSubtypesReq(attribute_type::get_subtypes::Req {
                    transitivity: transitivity.into_proto(),
                    value_type: value_type.map(ValueType::into_proto),
                }),
                attribute_type.label,
            ),
            Self::AttributeTypeGetInstances { attribute_type, transitivity, value_type } => (
                thing_type::req::Req::AttributeTypeGetInstancesReq(attribute_type::get_instances::Req {
                    transitivity: transitivity.into_proto(),
                    value_type: value_type.map(ValueType::into_proto),
                }),
                attribute_type.label,
            ),
            Self::AttributeTypeGetRegex { attribute_type } => (
                thing_type::req::Req::AttributeTypeGetRegexReq(attribute_type::get_regex::Req {}),
                attribute_type.label,
            ),
            Self::AttributeTypeSetRegex { attribute_type, regex } => (
                thing_type::req::Req::AttributeTypeSetRegexReq(attribute_type::set_regex::Req { regex }),
                attribute_type.label,
            ),
            Self::AttributeTypeGetOwners { attribute_type, transitivity, annotations } => (
                thing_type::req::Req::AttributeTypeGetOwnersReq(attribute_type::get_owners::Req {
                    transitivity: transitivity.into_proto(),
                    annotations: annotations.into_iter().map(Annotation::into_proto).collect(),
                }),
                attribute_type.label,
            ),
        };
        r#type::Req { req: Some(r#type::req::Req::ThingTypeReq(thing_type::Req { label, req: Some(req) })) }
    }
}

impl TryFromProto<thing_type::Res> for ThingTypeResponse {
    fn try_from_proto(proto: thing_type::Res) -> Result<Self> {
        match proto.res {
            Some(thing_type::res::Res::ThingTypeDeleteRes(_)) => Ok(Self::ThingTypeDelete),
            Some(thing_type::res::Res::ThingTypeSetLabelRes(_)) => Ok(Self::ThingTypeSetLabel),
            Some(thing_type::res::Res::ThingTypeSetAbstractRes(_)) => Ok(Self::ThingTypeSetAbstract),
            Some(thing_type::res::Res::ThingTypeUnsetAbstractRes(_)) => Ok(Self::ThingTypeUnsetAbstract),
            Some(thing_type::res::Res::ThingTypeGetOwnsOverriddenRes(thing_type::get_owns_overridden::Res {
                attribute_type,
            })) => Ok(Self::ThingTypeGetOwnsOverridden {
                attribute_type: attribute_type.map(AttributeType::try_from_proto).transpose()?,
            }),
            Some(thing_type::res::Res::ThingTypeSetOwnsRes(_)) => Ok(Self::ThingTypeSetOwns),
            Some(thing_type::res::Res::ThingTypeUnsetOwnsRes(_)) => Ok(Self::ThingTypeUnsetOwns),
            Some(thing_type::res::Res::ThingTypeGetPlaysOverriddenRes(thing_type::get_plays_overridden::Res {
                role_type,
            })) => Ok(Self::ThingTypeGetPlaysOverridden { role_type: role_type.map(RoleType::from_proto) }),
            Some(thing_type::res::Res::ThingTypeSetPlaysRes(_)) => Ok(Self::ThingTypeSetPlays),
            Some(thing_type::res::Res::ThingTypeUnsetPlaysRes(_)) => Ok(Self::ThingTypeUnsetPlays),
            Some(thing_type::res::Res::ThingTypeGetSyntaxRes(thing_type::get_syntax::Res { syntax })) => {
                Ok(Self::ThingTypeGetSyntax { syntax })
            }
            Some(thing_type::res::Res::EntityTypeCreateRes(entity_type::create::Res { entity })) => {
                Ok(Self::EntityTypeCreate {
                    entity: Entity::try_from_proto(entity.ok_or(ConnectionError::MissingResponseField("entity"))?)?,
                })
            }
            Some(thing_type::res::Res::EntityTypeGetSupertypeRes(entity_type::get_supertype::Res { entity_type })) => {
                Ok(Self::EntityTypeGetSupertype {
                    entity_type: EntityType::from_proto(
                        entity_type.ok_or(ConnectionError::MissingResponseField("entity_type"))?,
                    ),
                })
            }
            Some(thing_type::res::Res::EntityTypeSetSupertypeRes(_)) => Ok(Self::EntityTypeSetSupertype),
            Some(thing_type::res::Res::RelationTypeCreateRes(relation_type::create::Res { relation })) => {
                Ok(Self::RelationTypeCreate {
                    relation: Relation::try_from_proto(
                        relation.ok_or(ConnectionError::MissingResponseField("relation"))?,
                    )?,
                })
            }
            Some(thing_type::res::Res::RelationTypeGetSupertypeRes(relation_type::get_supertype::Res {
                relation_type,
            })) => Ok(Self::RelationTypeGetSupertype {
                relation_type: RelationType::from_proto(
                    relation_type.ok_or(ConnectionError::MissingResponseField("relation_type"))?,
                ),
            }),
            Some(thing_type::res::Res::RelationTypeSetSupertypeRes(_)) => Ok(Self::RelationTypeSetSupertype),
            Some(thing_type::res::Res::RelationTypeGetRelatesForRoleLabelRes(
                relation_type::get_relates_for_role_label::Res { role_type },
            )) => Ok(Self::RelationTypeGetRelatesForRoleLabel { role_type: role_type.map(RoleType::from_proto) }),
            Some(thing_type::res::Res::RelationTypeGetRelatesOverriddenRes(
                relation_type::get_relates_overridden::Res { role_type },
            )) => Ok(Self::RelationTypeGetRelatesOverridden { role_type: role_type.map(RoleType::from_proto) }),
            Some(thing_type::res::Res::RelationTypeSetRelatesRes(_)) => Ok(Self::RelationTypeSetRelates),
            Some(thing_type::res::Res::RelationTypeUnsetRelatesRes(_)) => Ok(Self::RelationTypeUnsetRelates),
            Some(thing_type::res::Res::AttributeTypePutRes(attribute_type::put::Res { attribute })) => {
                Ok(Self::AttributeTypePut {
                    attribute: Attribute::try_from_proto(
                        attribute.ok_or(ConnectionError::MissingResponseField("attribute"))?,
                    )?,
                })
            }
            Some(thing_type::res::Res::AttributeTypeGetRes(attribute_type::get::Res { attribute })) => {
                Ok(Self::AttributeTypeGet { attribute: attribute.map(Attribute::try_from_proto).transpose()? })
            }
            Some(thing_type::res::Res::AttributeTypeGetSupertypeRes(attribute_type::get_supertype::Res {
                attribute_type,
            })) => Ok(Self::AttributeTypeGetSupertype {
                attribute_type: AttributeType::try_from_proto(
                    attribute_type.ok_or(ConnectionError::MissingResponseField("attribute_type"))?,
                )?,
            }),
            Some(thing_type::res::Res::AttributeTypeSetSupertypeRes(_)) => Ok(Self::AttributeTypeSetSupertype),
            Some(thing_type::res::Res::AttributeTypeGetRegexRes(attribute_type::get_regex::Res { regex })) => {
                Ok(Self::AttributeTypeGetRegex { regex: Some(regex).filter(|s| !s.is_empty()) })
            }
            Some(thing_type::res::Res::AttributeTypeSetRegexRes(_)) => Ok(Self::AttributeTypeSetRegex),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<thing_type::ResPart> for ThingTypeResponse {
    fn try_from_proto(proto: thing_type::ResPart) -> Result<Self> {
        match proto.res {
            Some(thing_type::res_part::Res::ThingTypeGetOwnsResPart(thing_type::get_owns::ResPart {
                attribute_types,
            })) => Ok(Self::ThingTypeGetOwns {
                attribute_types: attribute_types.into_iter().map(AttributeType::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::ThingTypeGetPlaysResPart(thing_type::get_plays::ResPart {
                role_types,
            })) => {
                Ok(Self::ThingTypeGetPlays { role_types: role_types.into_iter().map(RoleType::from_proto).collect() })
            }
            Some(thing_type::res_part::Res::EntityTypeGetSupertypesResPart(entity_type::get_supertypes::ResPart {
                entity_types,
            })) => Ok(Self::EntityTypeGetSupertypes {
                entity_types: entity_types.into_iter().map(EntityType::from_proto).collect(),
            }),
            Some(thing_type::res_part::Res::EntityTypeGetSubtypesResPart(entity_type::get_subtypes::ResPart {
                entity_types,
            })) => Ok(Self::EntityTypeGetSubtypes {
                entity_types: entity_types.into_iter().map(EntityType::from_proto).collect(),
            }),
            Some(thing_type::res_part::Res::EntityTypeGetInstancesResPart(entity_type::get_instances::ResPart {
                entities,
            })) => Ok(Self::EntityTypeGetInstances {
                entities: entities.into_iter().map(Entity::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::RelationTypeGetSupertypesResPart(
                relation_type::get_supertypes::ResPart { relation_types },
            )) => Ok(Self::RelationTypeGetSupertypes {
                relation_types: relation_types.into_iter().map(RelationType::from_proto).collect(),
            }),
            Some(thing_type::res_part::Res::RelationTypeGetSubtypesResPart(relation_type::get_subtypes::ResPart {
                relation_types,
            })) => Ok(Self::RelationTypeGetSubtypes {
                relation_types: relation_types.into_iter().map(RelationType::from_proto).collect(),
            }),
            Some(thing_type::res_part::Res::RelationTypeGetInstancesResPart(
                relation_type::get_instances::ResPart { relations },
            )) => Ok(Self::RelationTypeGetInstances {
                relations: relations.into_iter().map(Relation::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::RelationTypeGetRelatesResPart(relation_type::get_relates::ResPart {
                role_types,
            })) => Ok(Self::RelationTypeGetRelates {
                role_types: role_types.into_iter().map(RoleType::from_proto).collect(),
            }),
            Some(thing_type::res_part::Res::AttributeTypeGetSupertypesResPart(
                attribute_type::get_supertypes::ResPart { attribute_types },
            )) => Ok(Self::AttributeTypeGetSupertypes {
                attribute_types: attribute_types.into_iter().map(AttributeType::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::AttributeTypeGetSubtypesResPart(
                attribute_type::get_subtypes::ResPart { attribute_types },
            )) => Ok(Self::AttributeTypeGetSubtypes {
                attribute_types: attribute_types.into_iter().map(AttributeType::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::AttributeTypeGetInstancesResPart(
                attribute_type::get_instances::ResPart { attributes },
            )) => Ok(Self::AttributeTypeGetInstances {
                attributes: attributes.into_iter().map(Attribute::try_from_proto).try_collect()?,
            }),
            Some(thing_type::res_part::Res::AttributeTypeGetOwnersResPart(attribute_type::get_owners::ResPart {
                thing_types,
            })) => Ok(Self::AttributeTypeGetOwners {
                thing_types: thing_types.into_iter().map(ThingType::try_from_proto).try_collect()?,
            }),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<r#type::Req> for RoleTypeRequest {
    fn into_proto(self) -> r#type::Req {
        let (req, scoped_label) = match self {
            Self::Delete { role_type } => {
                (role_type::req::Req::RoleTypeDeleteReq(role_type::delete::Req {}), role_type.label)
            }
            Self::SetLabel { role_type, new_label } => (
                role_type::req::Req::RoleTypeSetLabelReq(role_type::set_label::Req { label: new_label }),
                role_type.label,
            ),
            Self::GetSupertype { role_type } => {
                (role_type::req::Req::RoleTypeGetSupertypeReq(role_type::get_supertype::Req {}), role_type.label)
            }
            Self::GetSupertypes { role_type } => {
                (role_type::req::Req::RoleTypeGetSupertypesReq(role_type::get_supertypes::Req {}), role_type.label)
            }
            Self::GetSubtypes { role_type, transitivity } => (
                role_type::req::Req::RoleTypeGetSubtypesReq(role_type::get_subtypes::Req {
                    transitivity: transitivity.into_proto(),
                }),
                role_type.label,
            ),
            Self::GetRelationTypes { role_type } => (
                role_type::req::Req::RoleTypeGetRelationTypesReq(role_type::get_relation_types::Req {}),
                role_type.label,
            ),
            Self::GetPlayerTypes { role_type, transitivity } => (
                role_type::req::Req::RoleTypeGetPlayerTypesReq(role_type::get_player_types::Req {
                    transitivity: transitivity.into_proto(),
                }),
                role_type.label,
            ),
            Self::GetRelationInstances { role_type, transitivity } => (
                role_type::req::Req::RoleTypeGetRelationInstancesReq(role_type::get_relation_instances::Req {
                    transitivity: transitivity.into_proto(),
                }),
                role_type.label,
            ),
            Self::GetPlayerInstances { role_type, transitivity } => (
                role_type::req::Req::RoleTypeGetPlayerInstancesReq(role_type::get_player_instances::Req {
                    transitivity: transitivity.into_proto(),
                }),
                role_type.label,
            ),
        };
        r#type::Req {
            req: Some(r#type::req::Req::RoleTypeReq(role_type::Req {
                scope: scoped_label.scope,
                label: scoped_label.name,
                req: Some(req),
            })),
        }
    }
}

impl TryFromProto<role_type::Res> for RoleTypeResponse {
    fn try_from_proto(proto: role_type::Res) -> Result<Self> {
        match proto.res {
            Some(role_type::res::Res::RoleTypeDeleteRes(_)) => Ok(Self::Delete),
            Some(role_type::res::Res::RoleTypeSetLabelRes(_)) => Ok(Self::SetLabel),
            Some(role_type::res::Res::RoleTypeGetSupertypeRes(role_type::get_supertype::Res { role_type })) => {
                Ok(Self::GetSupertype {
                    role_type: RoleType::from_proto(
                        role_type.ok_or(ConnectionError::MissingResponseField("role_type"))?,
                    ),
                })
            }
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<role_type::ResPart> for RoleTypeResponse {
    fn try_from_proto(proto: role_type::ResPart) -> Result<Self> {
        match proto.res {
            Some(role_type::res_part::Res::RoleTypeGetSupertypesResPart(role_type::get_supertypes::ResPart {
                role_types,
            })) => Ok(Self::GetSupertypes { role_types: role_types.into_iter().map(RoleType::from_proto).collect() }),
            Some(role_type::res_part::Res::RoleTypeGetSubtypesResPart(role_type::get_subtypes::ResPart {
                role_types,
            })) => Ok(Self::GetSubtypes { role_types: role_types.into_iter().map(RoleType::from_proto).collect() }),
            Some(role_type::res_part::Res::RoleTypeGetRelationTypesResPart(
                role_type::get_relation_types::ResPart { relation_types },
            )) => Ok(Self::GetRelationTypes {
                relation_types: relation_types.into_iter().map(RelationType::from_proto).collect(),
            }),
            Some(role_type::res_part::Res::RoleTypeGetPlayerTypesResPart(role_type::get_player_types::ResPart {
                thing_types,
            })) => Ok(Self::GetPlayerTypes {
                thing_types: thing_types.into_iter().map(ThingType::try_from_proto).try_collect()?,
            }),
            Some(role_type::res_part::Res::RoleTypeGetRelationInstancesResPart(
                role_type::get_relation_instances::ResPart { relations },
            )) => Ok(Self::GetRelationInstances {
                relations: relations.into_iter().map(Relation::try_from_proto).try_collect()?,
            }),
            Some(role_type::res_part::Res::RoleTypeGetPlayerInstancesResPart(
                role_type::get_player_instances::ResPart { things },
            )) => Ok(Self::GetPlayerInstances { things: things.into_iter().map(Thing::try_from_proto).try_collect()? }),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<thing::Req> for ThingRequest {
    fn into_proto(self) -> thing::Req {
        let (req, iid) = match self {
            Self::ThingDelete { thing } => {
                (thing::req::Req::ThingDeleteReq(thing::delete::Req {}), thing.iid().to_owned())
            }
            Self::ThingGetHas { thing, attribute_types, annotations } => (
                thing::req::Req::ThingGetHasReq(thing::get_has::Req {
                    attribute_types: attribute_types.into_iter().map(AttributeType::into_proto).collect(),
                    annotations: annotations.into_iter().map(Annotation::into_proto).collect(),
                }),
                thing.iid().to_owned(),
            ),
            Self::ThingSetHas { thing, attribute } => (
                thing::req::Req::ThingSetHasReq(thing::set_has::Req { attribute: Some(attribute.into_proto()) }),
                thing.iid().to_owned(),
            ),
            Self::ThingUnsetHas { thing, attribute } => (
                thing::req::Req::ThingUnsetHasReq(thing::unset_has::Req { attribute: Some(attribute.into_proto()) }),
                thing.iid().to_owned(),
            ),
            Self::ThingGetRelations { thing, role_types } => (
                thing::req::Req::ThingGetRelationsReq(thing::get_relations::Req {
                    role_types: role_types.into_iter().map(RoleType::into_proto).collect(),
                }),
                thing.iid().to_owned(),
            ),
            Self::ThingGetPlaying { thing } => {
                (thing::req::Req::ThingGetPlayingReq(thing::get_playing::Req {}), thing.iid().to_owned())
            }
            Self::RelationAddRolePlayer { relation, role_type, player } => (
                thing::req::Req::RelationAddRolePlayerReq(relation::add_role_player::Req {
                    role_player: Some(relation::RolePlayer {
                        role_type: Some(role_type.into_proto()),
                        player: Some(player.into_proto()),
                    }),
                }),
                relation.iid,
            ),
            Self::RelationRemoveRolePlayer { relation, role_type, player } => (
                thing::req::Req::RelationRemoveRolePlayerReq(relation::remove_role_player::Req {
                    role_player: Some(relation::RolePlayer {
                        role_type: Some(role_type.into_proto()),
                        player: Some(player.into_proto()),
                    }),
                }),
                relation.iid,
            ),
            Self::RelationGetPlayersByRoleType { relation, role_types } => (
                thing::req::Req::RelationGetPlayersByRoleTypeReq(relation::get_players_by_role_type::Req {
                    role_types: role_types.into_iter().map(RoleType::into_proto).collect(),
                }),
                relation.iid,
            ),
            Self::RelationGetRolePlayers { relation } => {
                (thing::req::Req::RelationGetRolePlayersReq(relation::get_role_players::Req {}), relation.iid)
            }
            Self::RelationGetRelating { relation } => {
                (thing::req::Req::RelationGetRelatingReq(relation::get_relating::Req {}), relation.iid)
            }
            Self::AttributeGetOwners { attribute, thing_type } => (
                thing::req::Req::AttributeGetOwnersReq(attribute::get_owners::Req {
                    thing_type: thing_type.map(ThingType::into_proto),
                }),
                attribute.iid,
            ),
        };
        thing::Req { iid: iid.into(), req: Some(req) }
    }
}

impl TryFromProto<thing::Res> for ThingResponse {
    fn try_from_proto(proto: thing::Res) -> Result<Self> {
        match proto.res {
            Some(thing::res::Res::ThingDeleteRes(_)) => Ok(Self::ThingDelete),
            Some(thing::res::Res::ThingSetHasRes(_)) => Ok(Self::ThingSetHas),
            Some(thing::res::Res::ThingUnsetHasRes(_)) => Ok(Self::ThingUnsetHas),
            Some(thing::res::Res::RelationAddRolePlayerRes(_)) => Ok(Self::RelationAddRolePlayer),
            Some(thing::res::Res::RelationRemoveRolePlayerRes(_)) => Ok(Self::RelationRemoveRolePlayer),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<thing::ResPart> for ThingResponse {
    fn try_from_proto(proto: thing::ResPart) -> Result<Self> {
        match proto.res {
            Some(thing::res_part::Res::ThingGetHasResPart(thing::get_has::ResPart { attributes })) => {
                Ok(Self::ThingGetHas {
                    attributes: attributes.into_iter().map(Attribute::try_from_proto).try_collect()?,
                })
            }
            Some(thing::res_part::Res::ThingGetRelationsResPart(thing::get_relations::ResPart { relations })) => {
                Ok(Self::ThingGetRelations {
                    relations: relations.into_iter().map(Relation::try_from_proto).try_collect()?,
                })
            }
            Some(thing::res_part::Res::ThingGetPlayingResPart(thing::get_playing::ResPart { role_types })) => {
                Ok(Self::ThingGetPlaying { role_types: role_types.into_iter().map(RoleType::from_proto).collect() })
            }
            Some(thing::res_part::Res::RelationGetPlayersByRoleTypeResPart(
                relation::get_players_by_role_type::ResPart { things },
            )) => Ok(Self::RelationGetPlayersByRoleType {
                things: things.into_iter().map(Thing::try_from_proto).try_collect()?,
            }),
            Some(thing::res_part::Res::RelationGetRolePlayersResPart(relation::get_role_players::ResPart {
                role_players,
            })) => Ok(Self::RelationGetRolePlayers {
                role_players: role_players
                    .into_iter()
                    .map(|relation::RolePlayer { role_type, player }| -> Result<_> {
                        let role_type = role_type.ok_or(ConnectionError::MissingResponseField("role_type"))?;
                        let player = player.ok_or(ConnectionError::MissingResponseField("player"))?;
                        Ok((RoleType::from_proto(role_type), Thing::try_from_proto(player)?))
                    })
                    .try_collect()?,
            }),
            Some(thing::res_part::Res::RelationGetRelatingResPart(relation::get_relating::ResPart { role_types })) => {
                Ok(Self::RelationGetRelating { role_types: role_types.into_iter().map(RoleType::from_proto).collect() })
            }
            Some(thing::res_part::Res::AttributeGetOwnersResPart(attribute::get_owners::ResPart { things })) => {
                Ok(Self::AttributeGetOwners { owners: things.into_iter().map(Thing::try_from_proto).try_collect()? })
            }
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<rule::Req> for RuleRequest {
    fn into_proto(self) -> rule::Req {
        let (req, label) = match self {
            Self::Delete { label } => (rule::req::Req::RuleDeleteReq(rule::delete::Req {}), label),
            Self::SetLabel { current_label, new_label } => {
                (rule::req::Req::RuleSetLabelReq(rule::set_label::Req { label: new_label }), current_label)
            }
        };
        rule::Req { label, req: Some(req) }
    }
}

impl TryFromProto<rule::Res> for RuleResponse {
    fn try_from_proto(proto: rule::Res) -> Result<Self> {
        match proto.res {
            Some(rule::res::Res::RuleDeleteRes(_)) => Ok(Self::Delete),
            Some(rule::res::Res::RuleSetLabelRes(_)) => Ok(Self::SetLabel),
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl IntoProto<logic_manager::Req> for LogicRequest {
    fn into_proto(self) -> logic_manager::Req {
        let req = match self {
            Self::PutRule { label, when, then } => logic_manager::req::Req::PutRuleReq(logic_manager::put_rule::Req {
                label,
                when: when.to_string(),
                then: then.to_string(),
            }),
            Self::GetRule { label } => logic_manager::req::Req::GetRuleReq(logic_manager::get_rule::Req { label }),
            Self::GetRules => logic_manager::req::Req::GetRulesReq(logic_manager::get_rules::Req {}),
        };
        logic_manager::Req { req: Some(req) }
    }
}

impl TryFromProto<logic_manager::Res> for LogicResponse {
    fn try_from_proto(proto: logic_manager::Res) -> Result<Self> {
        match proto.res {
            Some(logic_manager::res::Res::PutRuleRes(logic_manager::put_rule::Res { rule })) => Ok(Self::PutRule {
                rule: Rule::try_from_proto(rule.ok_or(ConnectionError::MissingResponseField("rule"))?)?,
            }),
            Some(logic_manager::res::Res::GetRuleRes(logic_manager::get_rule::Res { rule })) => {
                Ok(Self::GetRule { rule: rule.map(Rule::try_from_proto).transpose()? })
            }
            None => Err(ConnectionError::MissingResponseField("res").into()),
        }
    }
}

impl TryFromProto<logic_manager::ResPart> for LogicResponse {
    fn try_from_proto(proto: logic_manager::ResPart) -> Result<Self> {
        match proto.get_rules_res_part {
            Some(logic_manager::get_rules::ResPart { rules }) => {
                Ok(Self::GetRules { rules: rules.into_iter().map(Rule::try_from_proto).try_collect()? })
            }
            None => Err(ConnectionError::MissingResponseField("get_rules_res_part").into()),
        }
    }
}
