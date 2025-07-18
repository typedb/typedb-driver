/*
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

use itertools::Itertools;
use typedb_protocol::{
    authentication, connection, database, database_manager, migration, query::initial_res::Res, server_manager,
    transaction, user, user_manager, Version::Version,
};
use uuid::Uuid;

use super::{FromProto, IntoProto, TryFromProto, TryIntoProto};
use crate::{
    answer::{concept_document::ConceptDocumentHeader, concept_row::ConceptRowHeader, QueryType},
    common::{info::DatabaseInfo, RequestID, Result},
    connection::message::{
        DatabaseExportResponse, DatabaseImportRequest, QueryRequest, QueryResponse, Request, Response,
        TransactionRequest, TransactionResponse,
    },
    error::{ConnectionError, InternalError, ServerError},
    info::UserInfo,
    Credentials,
};

impl TryIntoProto<connection::open::Req> for Request {
    fn try_into_proto(self) -> Result<connection::open::Req> {
        match self {
            Self::ConnectionOpen { driver_lang, driver_version, credentials } => Ok(connection::open::Req {
                version: Version.into(),
                driver_lang,
                driver_version,
                authentication: Some(credentials.try_into_proto()?),
            }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<server_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<server_manager::all::Req> {
        match self {
            Self::ServersAll => Ok(server_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::all::Req> {
        match self {
            Self::DatabasesAll => Ok(database_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database_manager::get::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::get::Req> {
        match self {
            Self::DatabaseGet { database_name } => Ok(database_manager::get::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database_manager::contains::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::contains::Req> {
        match self {
            Self::DatabasesContains { database_name } => Ok(database_manager::contains::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database_manager::create::Req> for Request {
    fn try_into_proto(self) -> Result<database_manager::create::Req> {
        match self {
            Self::DatabaseCreate { database_name } => Ok(database_manager::create::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database_manager::import::Client> for Request {
    fn try_into_proto(self) -> Result<database_manager::import::Client> {
        match self {
            Self::DatabaseImport(import_request) => {
                Ok(database_manager::import::Client { client: Some(import_request.into_proto()) })
            }
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl IntoProto<migration::import::Client> for DatabaseImportRequest {
    fn into_proto(self) -> migration::import::Client {
        let req = match self {
            DatabaseImportRequest::Initial { name, schema } => {
                migration::import::client::Client::InitialReq(migration::import::client::InitialReq { name, schema })
            }
            DatabaseImportRequest::ItemPart { items } => {
                migration::import::client::Client::ReqPart(migration::import::client::ReqPart { items })
            }
            DatabaseImportRequest::Done {} => {
                migration::import::client::Client::Done(migration::import::client::Done {})
            }
        };
        migration::import::Client { client: Some(req) }
    }
}

impl TryIntoProto<database::delete::Req> for Request {
    fn try_into_proto(self) -> Result<database::delete::Req> {
        match self {
            Self::DatabaseDelete { database_name } => Ok(database::delete::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database::schema::Req> for Request {
    fn try_into_proto(self) -> Result<database::schema::Req> {
        match self {
            Self::DatabaseSchema { database_name } => Ok(database::schema::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database::type_schema::Req> for Request {
    fn try_into_proto(self) -> Result<database::type_schema::Req> {
        match self {
            Self::DatabaseTypeSchema { database_name } => Ok(database::type_schema::Req { name: database_name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<database::export::Req> for Request {
    fn try_into_proto(self) -> Result<database::export::Req> {
        match self {
            Self::DatabaseExport { database_name } => {
                Ok(database::export::Req { req: Some(migration::export::Req { name: database_name }) })
            }
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<transaction::Client> for Request {
    fn try_into_proto(self) -> Result<transaction::Client> {
        match self {
            Self::Transaction(transaction_req) => Ok(transaction::Client { reqs: vec![transaction_req.into_proto()] }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl IntoProto<transaction::Req> for TransactionRequest {
    fn into_proto(self) -> transaction::Req {
        let mut request_id = None;

        let req = match self {
            Self::Open { database, transaction_type, options, network_latency } => {
                transaction::req::Req::OpenReq(transaction::open::Req {
                    database,
                    r#type: transaction_type.into_proto(),
                    options: Some(options.into_proto()),
                    network_latency_millis: network_latency.as_millis() as u64,
                })
            }
            Self::Commit => transaction::req::Req::CommitReq(transaction::commit::Req {}),
            Self::Rollback => transaction::req::Req::RollbackReq(transaction::rollback::Req {}),
            Self::Query(query_request) => transaction::req::Req::QueryReq(query_request.into_proto()),
            Self::Stream { request_id: req_id } => {
                request_id = Some(req_id);
                transaction::req::Req::StreamReq(transaction::stream_signal::Req {})
            }
        };

        transaction::Req {
            req_id: request_id.unwrap_or_else(RequestID::generate).into(),
            metadata: Default::default(),
            req: Some(req),
        }
    }
}

impl IntoProto<typedb_protocol::query::Req> for QueryRequest {
    fn into_proto(self) -> typedb_protocol::query::Req {
        match self {
            QueryRequest::Query { query, options } => {
                typedb_protocol::query::Req { query, options: Some(options.into_proto()) }
            }
        }
    }
}

impl TryIntoProto<user_manager::all::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::all::Req> {
        match self {
            Self::UsersAll => Ok(user_manager::all::Req {}),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<user_manager::get::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::get::Req> {
        match self {
            Self::UsersGet { name } => Ok(user_manager::get::Req { name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<user_manager::contains::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::contains::Req> {
        match self {
            Self::UsersContains { name } => Ok(user_manager::contains::Req { name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<user_manager::create::Req> for Request {
    fn try_into_proto(self) -> Result<user_manager::create::Req> {
        match self {
            Self::UsersCreate { user } => Ok(user_manager::create::Req {
                user: Some(typedb_protocol::User { name: user.name, password: user.password }),
            }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<user::update::Req> for Request {
    fn try_into_proto(self) -> Result<user::update::Req> {
        match self {
            Self::UsersUpdate { username, user } => Ok(user::update::Req {
                name: username,
                user: Some(typedb_protocol::User { name: user.name, password: user.password }),
            }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<user::delete::Req> for Request {
    fn try_into_proto(self) -> Result<user::delete::Req> {
        match self {
            Self::UsersDelete { name } => Ok(user::delete::Req { name }),
            other => Err(InternalError::UnexpectedRequestType { request_type: format!("{other:?}") }.into()),
        }
    }
}

impl TryIntoProto<authentication::token::create::Req> for Credentials {
    fn try_into_proto(self) -> Result<authentication::token::create::Req> {
        Ok(authentication::token::create::Req {
            credentials: Some(authentication::token::create::req::Credentials::Password(
                authentication::token::create::req::Password {
                    username: self.username().to_owned(),
                    password: self.password().to_owned(),
                },
            )),
        })
    }
}

impl TryFromProto<connection::open::Res> for Response {
    fn try_from_proto(proto: connection::open::Res) -> Result<Self> {
        let mut database_infos = Vec::new();
        for database_info_proto in proto.databases_all.expect("Expected databases data").databases {
            database_infos.push(DatabaseInfo::try_from_proto(database_info_proto)?);
        }
        Ok(Self::ConnectionOpen {
            connection_id: Uuid::from_slice(proto.connection_id.expect("Expected connection id").id.as_slice())
                .unwrap(),
            server_duration_millis: proto.server_duration_millis,
            databases: database_infos,
        })
    }
}

impl TryFromProto<server_manager::all::Res> for Response {
    fn try_from_proto(proto: server_manager::all::Res) -> Result<Self> {
        let server_manager::all::Res { servers } = proto;
        let servers = servers.into_iter().map(|server| server.address.parse()).try_collect()?;
        Ok(Self::ServersAll { servers })
    }
}

impl TryFromProto<database_manager::all::Res> for Response {
    fn try_from_proto(proto: database_manager::all::Res) -> Result<Self> {
        let database_manager::all::Res { databases } = proto;
        Ok(Self::DatabasesAll { databases: databases.into_iter().map(DatabaseInfo::try_from_proto).try_collect()? })
    }
}

impl TryFromProto<database_manager::get::Res> for Response {
    fn try_from_proto(proto: database_manager::get::Res) -> Result<Self> {
        Ok(Self::DatabaseGet {
            database: DatabaseInfo::try_from_proto(
                proto.database.ok_or(ConnectionError::MissingResponseField { field: "database" })?,
            )?,
        })
    }
}

impl FromProto<database_manager::contains::Res> for Response {
    fn from_proto(proto: database_manager::contains::Res) -> Self {
        Self::DatabasesContains { contains: proto.contains }
    }
}

impl TryFromProto<database_manager::create::Res> for Response {
    fn try_from_proto(proto: database_manager::create::Res) -> Result<Self> {
        Ok(Self::DatabaseCreate { database: DatabaseInfo::try_from_proto(proto.database.unwrap())? })
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

impl TryFromProto<database::export::Server> for DatabaseExportResponse {
    fn try_from_proto(proto: database::export::Server) -> Result<Self> {
        use migration::export::{server::Server, Done, InitialRes, ResPart};
        match proto.server {
            Some(server) => match server.server {
                Some(Server::InitialRes(InitialRes { schema })) => Ok(Self::Schema(schema)),
                Some(Server::ResPart(ResPart { items })) => Ok(Self::Items(items)),
                Some(Server::Done(Done {})) => Ok(Self::Done),
                None => Err(ConnectionError::MissingResponseField { field: "server" }.into()),
            },
            None => Err(ConnectionError::MissingResponseField { field: "server" }.into()),
        }
    }
}

impl FromProto<typedb_protocol::Error> for ServerError {
    fn from_proto(proto: typedb_protocol::Error) -> Self {
        ServerError::new(proto.error_code, proto.domain, String::new(), proto.stack_trace)
    }
}

impl TryFromProto<transaction::Res> for TransactionResponse {
    fn try_from_proto(proto: transaction::Res) -> Result<Self> {
        match proto.res {
            Some(transaction::res::Res::OpenRes(transaction::open::Res { server_duration_millis })) => {
                Ok(Self::Open { server_duration_millis })
            }
            Some(transaction::res::Res::CommitRes(_)) => Ok(Self::Commit),
            Some(transaction::res::Res::RollbackRes(_)) => Ok(Self::Rollback),
            Some(transaction::res::Res::QueryInitialRes(initial_res)) => match initial_res.res {
                Some(res) => match res {
                    Res::Error(error) => Ok(TransactionResponse::Query(QueryResponse::from_proto(error))),
                    Res::Ok(header) => match header.ok {
                        None => Err(ConnectionError::MissingResponseField {
                            field: "transaction.res.query.initial_res.res.Ok.ok",
                        }
                        .into()),
                        Some(header) => Ok(TransactionResponse::Query(QueryResponse::try_from_proto(header)?)),
                    },
                },
                None => {
                    Err(ConnectionError::MissingResponseField { field: "transaction.res.query.initial_res.res" }.into())
                }
            },
            None => Err(ConnectionError::MissingResponseField { field: "res" }.into()),
        }
    }
}

impl TryFromProto<typedb_protocol::query::initial_res::ok::Ok> for QueryResponse {
    fn try_from_proto(proto: typedb_protocol::query::initial_res::ok::Ok) -> Result<Self> {
        match proto {
            typedb_protocol::query::initial_res::ok::Ok::Done(done_header) => {
                Ok(QueryResponse::Ok(QueryType::try_from_proto(done_header.query_type)?))
            }
            typedb_protocol::query::initial_res::ok::Ok::ConceptDocumentStream(document_stream_header) => {
                Ok(QueryResponse::ConceptDocumentsHeader(ConceptDocumentHeader {
                    query_type: QueryType::try_from_proto(document_stream_header.query_type)?,
                }))
            }
            typedb_protocol::query::initial_res::ok::Ok::ConceptRowStream(row_stream_header) => {
                Ok(QueryResponse::ConceptRowsHeader(ConceptRowHeader {
                    column_names: row_stream_header.column_variable_names,
                    query_type: QueryType::try_from_proto(row_stream_header.query_type)?,
                }))
            }
        }
    }
}

impl TryFromProto<typedb_protocol::query::ResPart> for TransactionResponse {
    fn try_from_proto(proto: typedb_protocol::query::ResPart) -> Result<Self> {
        match proto.res {
            Some(res) => Ok(TransactionResponse::Query(QueryResponse::try_from_proto(res)?)),
            None => Err(ConnectionError::MissingResponseField { field: "query.ResPart.res" }.into()),
        }
    }
}

impl TryFromProto<typedb_protocol::query::res_part::Res> for QueryResponse {
    fn try_from_proto(proto: typedb_protocol::query::res_part::Res) -> Result<Self> {
        match proto {
            typedb_protocol::query::res_part::Res::DocumentsRes(documents) => {
                let mut converted = Vec::with_capacity(documents.documents.len());
                for document_proto in documents.documents.into_iter() {
                    converted.push(TryFromProto::try_from_proto(document_proto)?);
                }
                Ok(QueryResponse::StreamConceptDocuments(converted))
            }
            typedb_protocol::query::res_part::Res::RowsRes(rows) => {
                let mut converted = Vec::with_capacity(rows.rows.len());
                for row_proto in rows.rows.into_iter() {
                    converted.push(TryFromProto::try_from_proto(row_proto)?);
                }
                Ok(QueryResponse::StreamConceptRows(converted))
            }
        }
    }
}

impl FromProto<typedb_protocol::Error> for QueryResponse {
    fn from_proto(proto: typedb_protocol::Error) -> Self {
        QueryResponse::Error(ServerError::from_proto(proto))
    }
}

impl FromProto<user_manager::all::Res> for Response {
    fn from_proto(proto: user_manager::all::Res) -> Self {
        Self::UsersAll { users: proto.users.into_iter().map(UserInfo::from_proto).collect() }
    }
}

impl FromProto<user_manager::get::Res> for Response {
    fn from_proto(proto: user_manager::get::Res) -> Self {
        Self::UsersGet { user: proto.user.map(UserInfo::from_proto) }
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

impl FromProto<user::update::Res> for Response {
    fn from_proto(_proto: user::update::Res) -> Self {
        Self::UsersUpdate
    }
}

impl FromProto<user::delete::Res> for Response {
    fn from_proto(_proto: user::delete::Res) -> Self {
        Self::UsersDelete
    }
}
