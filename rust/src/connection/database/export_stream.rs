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

use std::fmt;

#[cfg(not(feature = "sync"))]
use futures::{stream, StreamExt};

use crate::{
    common::{stream::Stream, Promise, Result},
    connection::{message::DatabaseExportResponse, network::transmitter::DatabaseExportTransmitter},
    database::migration::DatabaseExportAnswer,
    error::ConnectionError,
    promisify, Error,
};

pub(crate) struct DatabaseExportStream {
    export_transmitter: DatabaseExportTransmitter,
}

impl DatabaseExportStream {
    pub(crate) fn new(export_transmitter: DatabaseExportTransmitter) -> Self {
        Self { export_transmitter }
    }

    pub(crate) fn listen<'a>(&'a mut self) -> impl Promise<'a, Result<DatabaseExportAnswer>> + 'a {
        let mut stream = self.export_transmitter.stream();
        promisify! {
            #[cfg(feature = "sync")]
            let response = stream.next();
            #[cfg(not(feature = "sync"))]
            let response: Option<Result<DatabaseExportResponse>> = stream.next().await;

            let response = match response {
                None => return Err(ConnectionError::QueryStreamNoResponse.into()),
                Some(Err(err)) => return Err(err),
                Some(Ok(response)) => response,
            };
            match response {
                DatabaseExportResponse::Schema(schema) => Ok(DatabaseExportAnswer::Schema(schema)),
                DatabaseExportResponse::Items(items) => Ok(DatabaseExportAnswer::Items(items)),
                DatabaseExportResponse::Done => Ok(DatabaseExportAnswer::Done),
                DatabaseExportResponse::Error(error) => Err(Error::Server(error)),
            }
        }
    }
}

impl fmt::Debug for DatabaseExportStream {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        f.debug_struct("DatabaseExportStream").finish()
    }
}
