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

use std::{fmt, iter, pin::Pin, sync::Arc};

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};
use typedb_protocol::migration;

use crate::{
    common::{stream::Stream, Promise, Result},
    connection::{message::DatabaseImportRequest, network::transmitter::DatabaseImportTransmitter},
    promisify, resolve,
};

pub(crate) struct DatabaseImportStream {
    import_transmitter: DatabaseImportTransmitter,
}

impl DatabaseImportStream {
    pub(crate) fn new(import_transmitter: DatabaseImportTransmitter) -> Self {
        Self { import_transmitter }
    }

    pub(crate) fn items(&mut self, items: Vec<migration::Item>) -> Result {
        self.import_transmitter.single(DatabaseImportRequest::ItemPart { items })
    }

    pub(crate) fn done(mut self) -> impl Promise<'static, Result> {
        promisify! {
            self.import_transmitter.single(DatabaseImportRequest::Done)?;
            let promise = self.import_transmitter.wait_until_done();
            resolve!(promise)
        }
    }
}

impl fmt::Debug for DatabaseImportStream {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("DatabaseImportStream").finish()
    }
}
