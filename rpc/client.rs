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
extern crate protocol;

use grpc::{ClientStubExt, RequestOptions};
use protocol::{CoreDatabaseManager_All_Req, TypeDB};

use crate::common::error::message::ERRORS;
// use crate::common::error::message::client::UNABLE_TO_CONNECT;
use crate::common::error::Error;
use crate::common::Result;

pub(crate) struct RpcClient {
    pub(crate) typedb: protocol::TypeDBClient
}

impl RpcClient {
    // TODO: temporary hack to validate connection until we have client pulse
    fn check_connection(client: &protocol::TypeDBClient) -> grpc::Result<()> {
        client.databases_all(RequestOptions::new(), CoreDatabaseManager_All_Req::new()).wait().map(|_| ())
    }

    #[allow(unused_must_use)]
    pub(crate) fn new(host: &str, port: u16) -> Result<RpcClient> {
        protocol::TypeDBClient::new_plain(host, port, grpc::ClientConf::new())
            .map(|client| { RpcClient::check_connection(&client); client })
            .and_then(|client| Ok(RpcClient { typedb: client }))
            .map_err(|err| Error::from_grpc(ERRORS.unable_to_connect.message(vec![]), err))
    }
}
