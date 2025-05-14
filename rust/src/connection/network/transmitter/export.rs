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

use std::sync::Arc;

use futures::StreamExt;
use tokio::sync::mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender};
use tonic::Streaming;
use typedb_protocol::database;

use crate::{
    common::{
        stream::{NetworkStream, Stream},
        Result,
    },
    connection::{message::DatabaseExportResponse, network::proto::TryFromProto, runtime::BackgroundRuntime},
    Error,
};

pub(in crate::connection) struct DatabaseExportTransmitter {
    stream: NetworkStream<Result<DatabaseExportResponse>>,
    shutdown_sink: UnboundedSender<()>,
    // runtime is alive as long as the export transmitter is alive:
    background_runtime: Arc<BackgroundRuntime>,
}

impl DatabaseExportTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: Arc<BackgroundRuntime>,
        response_source: Streaming<database::export::Server>,
    ) -> Self {
        let (response_sender, response_receiver) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();

        background_runtime.spawn(Self::start_workers(response_source, response_sender, shutdown_source));
        Self { stream: NetworkStream::new(response_receiver), shutdown_sink, background_runtime }
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_sink
    }

    pub(in crate::connection) fn stream(&mut self) -> &mut impl Stream<Item = Result<DatabaseExportResponse>> {
        &mut self.stream
    }

    async fn start_workers(
        response_source: Streaming<database::export::Server>,
        response_sender: UnboundedSender<Result<DatabaseExportResponse>>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        tokio::task::spawn_blocking({ move || Self::listen_loop(response_source, response_sender, shutdown_signal) });
    }

    async fn listen_loop(
        mut grpc_source: Streaming<database::export::Server>,
        response_sender: UnboundedSender<Result<DatabaseExportResponse>>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        loop {
            println!("Listen loop export");
            if let Ok(_) = shutdown_signal.try_recv() {
                break;
            }
            match grpc_source.next().await {
                Some(Ok(message)) => {
                    response_sender.send(DatabaseExportResponse::try_from_proto(message)).unwrap()
                    // TODO: cover
                    // TODO: Close if error?
                }
                Some(Err(status)) => {
                    response_sender.send(Err(status.into())).unwrap();
                    break;
                }
                None => todo!("Unexpected NONE, cover? Replace to just close or panic?"), // TODO
            }
        }
    }
}
