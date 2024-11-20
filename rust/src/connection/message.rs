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

use std::time::Duration;

use tokio::sync::mpsc::UnboundedSender;
use tonic::Streaming;
use typedb_protocol::transaction;
use uuid::Uuid;

use crate::{
    answer::{
        concept_document::{ConceptDocumentHeader, Node},
        concept_row::ConceptRowHeader,
        QueryType,
    },
    common::{address::Address, info::DatabaseInfo, RequestID},
    concept::Concept,
    error::ServerError,
    user::User,
    Options, TransactionType,
};

#[derive(Debug)]
pub(super) enum Request {
    ConnectionOpen { driver_lang: String, driver_version: String },

    ServersAll,

    DatabasesContains { database_name: String },
    DatabaseCreate { database_name: String },
    DatabaseGet { database_name: String },
    DatabasesAll,

    DatabaseSchema { database_name: String },
    DatabaseTypeSchema { database_name: String },
    DatabaseDelete { database_name: String },

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
    ConnectionOpen {
        connection_id: Uuid,
        server_duration_millis: u64,
        databases: Vec<DatabaseInfo>,
    },

    ServersAll {
        servers: Vec<Address>,
    },

    DatabasesContains {
        contains: bool,
    },
    DatabaseCreate {
        database: DatabaseInfo,
    },
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
    TransactionStream {
        open_request_id: RequestID,
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
    Open { database: String, transaction_type: TransactionType, options: Options, network_latency: Duration },
    Commit,
    Rollback,
    Query(QueryRequest),
    Stream { request_id: RequestID },
}

#[derive(Debug)]
pub(super) enum TransactionResponse {
    Open { server_duration_millis: u64 },
    Commit,
    Rollback,
    Query(QueryResponse),
    Close,
}

#[derive(Debug)]
pub(super) enum QueryRequest {
    Query { query: String, options: Options },
}

#[derive(Debug)]
pub(super) enum QueryResponse {
    Ok(QueryType),
    ConceptRowsHeader(ConceptRowHeader),
    ConceptDocumentsHeader(ConceptDocumentHeader),
    StreamConceptRows(Vec<Vec<Option<Concept>>>),
    StreamConceptDocuments(Vec<Option<Node>>),
    Error(ServerError),
    // Define,
    // Undefine,
    // Delete,
    //
    // Get { answers: Vec<ConceptMap> },
    // Insert { answers: Vec<ConceptMap> },
    // Update { answers: Vec<ConceptMap> },
    //
    // GetAggregate { answer: Option<Value> },
    //
    // GetGroup { answers: Vec<ConceptMapGroup> },
    //
    // Fetch { answers: Vec<readable_concept::Tree> },
}
