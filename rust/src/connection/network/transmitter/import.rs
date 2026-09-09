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

use std::{sync::Arc, time::Duration};

use crossbeam::channel::{
    Receiver as SyncReceiver, RecvTimeoutError, Sender as SyncSender, bounded as bounded_blocking,
};
use futures::StreamExt;
use tokio::sync::mpsc::{Sender, UnboundedReceiver, UnboundedSender, unbounded_channel as unbounded_async};
#[cfg(not(feature = "sync"))]
use tokio::sync::oneshot::{Receiver as OneshotReceiver, channel as oneshot, error::TryRecvError};
use tonic::Streaming;
use typedb_protocol::database_manager;

#[cfg(feature = "sync")]
use super::{SyncReceiver as OneshotReceiver, SyncTryRecvError as TryRecvError, oneshot_blocking as oneshot};
use crate::{
    common::{Promise, Result, box_promise, error::ConnectionError},
    connection::{
        message::DatabaseImportRequest,
        network::{
            proto::IntoProto,
            transmitter::{response_sink::ResponseSink, shutdown_guard::ShutdownGuard},
        },
        runtime::BackgroundRuntime,
    },
};

// Item-part messages the client buffers before the file-reading producer blocks. Together with the
// bounded gRPC request queue this keeps client memory flat regardless of the export file size.
const CLIENT_ITEM_BATCH_QUEUE: usize = 32;

pub(crate) struct DatabaseImportTransmitter {
    request_sink: SyncSender<DatabaseImportRequest>,
    shutdown_guard: ShutdownGuard<()>,
    result_source: OneshotReceiver<Result>,
    // runtime is alive as long as the import transmitter is alive:
    _background_runtime: Arc<BackgroundRuntime>,
}

impl DatabaseImportTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: Arc<BackgroundRuntime>,
        request_sink: Sender<database_manager::import::Client>,
        response_source: Streaming<database_manager::import::Server>,
    ) -> Self {
        let (buffer_sink, buffer_source) = bounded_blocking(CLIENT_ITEM_BATCH_QUEUE);
        let (shutdown_sink, shutdown_source) = unbounded_async();

        let (result_sink, result_source) = oneshot();
        #[cfg(feature = "sync")]
        let result_sink = ResponseSink::BlockingOneShot(result_sink);
        #[cfg(not(feature = "sync"))]
        let result_sink = ResponseSink::AsyncOneShot(result_sink);

        background_runtime.spawn(Self::start_workers(
            buffer_source,
            request_sink,
            response_source,
            result_sink,
            shutdown_sink.clone(),
            shutdown_source,
        ));
        Self {
            request_sink: buffer_sink,
            shutdown_guard: ShutdownGuard::new(shutdown_sink),
            result_source,
            _background_runtime: background_runtime,
        }
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_guard
    }

    pub(in crate::connection) fn single(&mut self, req: DatabaseImportRequest) -> Result {
        self.check_early_result()?;
        let send_result = self.request_sink.send(req);
        send_result.map_err(|_| ConnectionError::DatabaseImportChannelIsClosed.into())
    }

    #[cfg(not(feature = "sync"))]
    pub(in crate::connection) fn wait_done(self) -> impl Promise<'static, Result> {
        box_promise(async move {
            let _shutdown_guard = self.shutdown_guard; // Don't let it drop before resolving
            match self.result_source.await {
                Ok(result) => result,
                Err(_) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            }
        })
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn wait_done(self) -> impl Promise<'static, Result> {
        box_promise(move || {
            let _shutdown_guard = self.shutdown_guard; // Don't let it drop before resolving
            match self.result_source.recv() {
                Ok(result) => result,
                Err(_) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            }
        })
    }

    #[cfg(not(feature = "sync"))]
    fn check_early_result(&mut self) -> Result {
        match self.result_source.try_recv() {
            Ok(result) => match result {
                Ok(()) => Err(ConnectionError::DatabaseImportStreamUnexpectedResponse.into()),
                Err(err) => Err(err),
            },
            Err(TryRecvError::Closed) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            Err(TryRecvError::Empty) => Ok(()),
        }
    }

    #[cfg(feature = "sync")]
    fn check_early_result(&mut self) -> Result {
        match self.result_source.try_recv() {
            Ok(result) => match result {
                Ok(()) => Err(ConnectionError::DatabaseImportStreamUnexpectedResponse.into()),
                Err(err) => Err(err),
            },
            Err(TryRecvError::Disconnected) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            Err(TryRecvError::Empty) => Ok(()),
        }
    }

    async fn start_workers(
        queue_source: SyncReceiver<DatabaseImportRequest>,
        request_sink: Sender<database_manager::import::Client>,
        response_source: Streaming<database_manager::import::Server>,
        result_sink: ResponseSink<()>,
        shutdown_sink: UnboundedSender<()>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        tokio::task::spawn_blocking(move || Self::dispatch_loop(queue_source, request_sink, shutdown_signal));
        tokio::spawn(Self::next(response_source, result_sink, shutdown_sink));
    }

    fn dispatch_loop(
        request_source: SyncReceiver<DatabaseImportRequest>,
        request_sink: Sender<database_manager::import::Client>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        const SHUTDOWN_POLL_INTERVAL: Duration = Duration::from_millis(50);

        loop {
            if shutdown_signal.try_recv().is_ok() {
                break;
            }
            match request_source.recv_timeout(SHUTDOWN_POLL_INTERVAL) {
                Ok(request) => {
                    let client_req = database_manager::import::Client { client: Some(request.into_proto()) };
                    if request_sink.blocking_send(client_req).is_err() {
                        break;
                    }
                }
                Err(RecvTimeoutError::Timeout) => continue,
                Err(RecvTimeoutError::Disconnected) => break,
            }
        }
    }

    async fn next(
        mut grpc_source: Streaming<database_manager::import::Server>,
        result_sink: ResponseSink<()>,
        shutdown_sink: UnboundedSender<()>,
    ) {
        let result = match grpc_source.next().await {
            Some(Ok(_message)) => Ok(()), // can only be Done
            Some(Err(status)) => Err(status.into()),
            None => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
        };
        result_sink.finish(result);
        shutdown_sink.send(()).ok();
    }
}
