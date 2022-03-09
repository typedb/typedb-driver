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

use grpc::{RequestOptions, SingleResponse};
use protocol::TypeDB;
use crate::RpcClient;

pub struct DatabaseManager {
    rpc_client: RpcClient
}

impl DatabaseManager {
    pub fn new(rpc_client: RpcClient) -> DatabaseManager {
        DatabaseManager {
            rpc_client
        }
    }

    pub fn create(&self, name: &str) -> () {
        let mut req = protocol::CoreDatabaseManager_Create_Req::new();
        req.set_name(String::from(name));
        let res: SingleResponse<protocol::CoreDatabaseManager_Create_Res> = self.rpc_client.typedb.databases_create(RequestOptions::new(), req);
        println!("{:?}", res.wait());
    }
}
