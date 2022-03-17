/*
 * Copyright (C) 2021 Vaticle
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

extern crate grpc;

use std::sync::Arc;
use grpc::{ClientRequestSink, ClientStubExt, GrpcStream, RequestOptions, SingleResponse, StreamingRequest, StreamingResponse};
use typedb_protocol::core_database::{CoreDatabase_Delete_Req, CoreDatabase_Delete_Res, CoreDatabase_Schema_Req, CoreDatabase_Schema_Res, CoreDatabaseManager_All_Req, CoreDatabaseManager_All_Res, CoreDatabaseManager_Contains_Req, CoreDatabaseManager_Contains_Res, CoreDatabaseManager_Create_Req, CoreDatabaseManager_Create_Res};
use typedb_protocol::core_service_grpc;
use typedb_protocol::session::{Session_Close_Req, Session_Close_Res, Session_Open_Req, Session_Open_Res};
use typedb_protocol::transaction::{Transaction_Client, Transaction_Server};

use crate::common::error::Error;
use crate::common::Result;

pub(crate) struct RpcClient {
    typedb: Arc<core_service_grpc::TypeDBClient>
}

impl RpcClient {
    pub(crate) async fn new(host: &str, port: u16) -> Result<Self> {
        match core_service_grpc::TypeDBClient::new_plain(host, port, grpc::ClientConf::new()) {
            Ok(client) => {
                // TODO: temporary hack to validate connection until we have client pulse
                match RpcClient::check_connection(&client).await {
                    Ok(_) => Ok(RpcClient { typedb: Arc::new(client) }),
                    Err(err) => Err(Error::from_grpc(err)),
                }
            }
            Err(err) => Err(Error::from_grpc(err)),
        }
    }

    async fn check_connection(client: &core_service_grpc::TypeDBClient) -> grpc::Result<()> {
        client.databases_all(RequestOptions::new(), CoreDatabaseManager_All_Req::new()).drop_metadata().await.map(|_| ())
    }

    pub(crate) async fn databases_contains(&self, req: CoreDatabaseManager_Contains_Req) -> Result<CoreDatabaseManager_Contains_Res> {
        RpcClient::map_single(self.typedb.databases_contains(RequestOptions::new(), req)).await
    }

    pub(crate) async fn databases_create(&self, req: CoreDatabaseManager_Create_Req) -> Result<CoreDatabaseManager_Create_Res> {
        RpcClient::map_single(self.typedb.databases_create(RequestOptions::new(), req)).await
    }

    pub(crate) async fn databases_all(&self, req: CoreDatabaseManager_All_Req) -> Result<CoreDatabaseManager_All_Res> {
        RpcClient::map_single(self.typedb.databases_all(RequestOptions::new(), req)).await
    }

    pub(crate) async fn database_schema(&self, req: CoreDatabase_Schema_Req) -> Result<CoreDatabase_Schema_Res> {
        RpcClient::map_single(self.typedb.database_schema(RequestOptions::new(), req)).await
    }

    pub(crate) async fn database_delete(&self, req: CoreDatabase_Delete_Req) -> Result<CoreDatabase_Delete_Res> {
        RpcClient::map_single(self.typedb.database_delete(RequestOptions::new(), req)).await
    }

    pub(crate) async fn session_open(&self, req: Session_Open_Req) -> Result<Session_Open_Res> {
        RpcClient::map_single(self.typedb.session_open(RequestOptions::new(), req)).await
    }

    pub(crate) async fn session_close(&self, req: Session_Close_Req) -> Result<Session_Close_Res> {
        RpcClient::map_single(self.typedb.session_close(RequestOptions::new(), req)).await
    }

    pub(crate) async fn transaction(&self) -> Result<(ClientRequestSink<Transaction_Client>, StreamingResponse<Transaction_Server>)> {
        RpcClient::map_bidi_stream(self.typedb.transaction(RequestOptions::new()).await)
    }

    async fn map_single<T: Send>(res: SingleResponse<T>) -> Result<T> {
        // TODO: check if we need ensureConnected() from client-java
        res.drop_metadata().await.map_err(|err| Error::from_grpc(err))
    }

    fn map_bidi_stream<T: Send, U: Send>(res: grpc::Result<(ClientRequestSink<T>, StreamingResponse<U>)>) -> Result<(ClientRequestSink<T>, StreamingResponse<U>)> {
        // TODO: this mapping should be exposed as a "Result::from"-like function
        res.map_err(|err| Error::from_grpc(err))
    }
}
