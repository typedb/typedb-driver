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

mod connection;

use connection::database_manager::DatabaseManager;

pub const DEFAULT_HOST: &str = "0.0.0.0";
pub const DEFAULT_PORT: u16 = 1729;

pub struct CoreClient {
    pub databases: DatabaseManager,
}

impl CoreClient {
    pub fn new() -> CoreClient {
        CoreClient { databases: DatabaseManager {} }
    }

    fn close(&self) -> () {
        todo!()
    }
}

impl Drop for CoreClient {
    fn drop(&mut self) {
        self.close()
    }
}
