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

use std::{sync::Arc, thread::sleep, time::Duration};

use futures::StreamExt;
#[cfg(not(feature = "sync"))]
use futures::TryStreamExt;
use tokio::sync::{
    mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
    oneshot::{
        channel as oneshot_async,
        error::{RecvError, TryRecvError},
        Receiver as AsyncOneshotReceiver, Sender as AsyncOneshotSender,
    },
};
use tonic::Streaming;
use typedb_protocol::database_manager;

use crate::{
    common::{box_promise, error::ConnectionError, Promise, Result},
    connection::{message::DatabaseImportRequest, network::proto::IntoProto, runtime::BackgroundRuntime},
};

pub(in crate::connection) struct DatabaseImportTransmitter {
    request_sink: UnboundedSender<DatabaseImportRequest>,
    shutdown_sink: UnboundedSender<()>,
    result_source: AsyncOneshotReceiver<Result>,
    // runtime is alive as long as the import transmitter is alive:
    background_runtime: Arc<BackgroundRuntime>,
}

impl DatabaseImportTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: Arc<BackgroundRuntime>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        response_source: Streaming<database_manager::import::Server>,
    ) -> Self {
        let (buffer_sink, buffer_source) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();
        let (result_sink, result_source) = oneshot_async();

        background_runtime.spawn(Self::start_workers(
            buffer_source,
            request_sink,
            response_source,
            result_sink,
            shutdown_sink.clone(),
            shutdown_source,
        ));
        Self { request_sink: buffer_sink, shutdown_sink, result_source, background_runtime }
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_sink
    }

    pub(in crate::connection) fn single(&mut self, req: DatabaseImportRequest) -> Result {
        match self.result_source.try_recv() {
            Ok(result) => return result,
            Err(TryRecvError::Closed) => return Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            Err(TryRecvError::Empty) => {}
        }
        let send_result = self.request_sink.send(req);
        send_result.map_err(|_| ConnectionError::DatabaseImportChannelIsClosed.into())
    }

    pub(in crate::connection) fn wait_until_done(mut self) -> Result {
        // TODO: Make sync/async in a smart way
        loop {
            sleep(Duration::from_millis(100));
            match self.result_source.try_recv() {
                Ok(result) => return result,
                Err(TryRecvError::Closed) => return Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
                Err(TryRecvError::Empty) => {}
            }
        }
        // match self.result_source.await {
        //     Ok(result) => result,
        //     Err(_) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
        // }
    }

    async fn start_workers(
        queue_source: UnboundedReceiver<DatabaseImportRequest>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        response_source: Streaming<database_manager::import::Server>,
        result_sink: AsyncOneshotSender<Result>,
        shutdown_sink: UnboundedSender<()>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        tokio::task::spawn_blocking({ move || Self::dispatch_loop(queue_source, request_sink, shutdown_signal) });
        tokio::spawn(Self::listen(response_source, result_sink, shutdown_sink));
    }

    fn dispatch_loop(
        mut request_source: UnboundedReceiver<DatabaseImportRequest>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        const MAX_GRPC_MESSAGE_LEN: usize = 1_000_000;
        const DISPATCH_INTERVAL: Duration = Duration::from_micros(50);

        loop {
            if let Ok(_) = shutdown_signal.try_recv() {
                break;
            }

            sleep(DISPATCH_INTERVAL);
            if let Ok(request) = request_source.try_recv() {
                let client_req = database_manager::import::Client { client: Some(request.into_proto()) };
                request_sink.send(client_req).ok();
            }
        }
    }

    async fn listen(
        mut grpc_source: Streaming<database_manager::import::Server>,
        result_sink: AsyncOneshotSender<Result>,
        shutdown_sink: UnboundedSender<()>,
    ) {
        let result = match grpc_source.next().await {
            Some(Ok(_message)) => Ok(()), // can only be Done
            Some(Err(status)) => Err(status.into()),
            None => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
        };
        result_sink.send(result).ok();
        shutdown_sink.send(()).ok();
    }
}
