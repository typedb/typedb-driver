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
use tokio::sync::mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender};
#[cfg(not(feature = "sync"))]
use tokio::sync::oneshot::{channel as oneshot, error::TryRecvError, Receiver as OneshotReceiver};
use tonic::Streaming;
use typedb_protocol::database_manager;

#[cfg(feature = "sync")]
use super::{oneshot_blocking as oneshot, SyncReceiver as OneshotReceiver, SyncTryRecvError as TryRecvError};
use crate::{
    common::{box_promise, error::ConnectionError, Promise, Result},
    connection::{
        message::DatabaseImportRequest,
        network::{proto::IntoProto, transmitter::response_sink::ResponseSink},
        runtime::BackgroundRuntime,
    },
};

pub(in crate::connection) struct DatabaseImportTransmitter {
    request_sink: UnboundedSender<DatabaseImportRequest>,
    shutdown_sink: UnboundedSender<()>,
    result_source: OneshotReceiver<Result>,
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
        Self { request_sink: buffer_sink, shutdown_sink, result_source, background_runtime }
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_sink
    }

    pub(in crate::connection) fn single(&mut self, req: DatabaseImportRequest) -> Result {
        self.check_interrupt_result()?;
        let send_result = self.request_sink.send(req);
        send_result.map_err(|_| ConnectionError::DatabaseImportChannelIsClosed.into())
    }

    #[cfg(not(feature = "sync"))]
    pub(in crate::connection) fn wait_done(mut self) -> impl Promise<'static, Result> {
        box_promise(async move {
            match self.result_source.await {
                Ok(result) => result,
                Err(_) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
            }
        })
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn wait_done(mut self) -> impl Promise<'static, Result> {
        box_promise(move || match self.result_source.recv() {
            Ok(result) => return result,
            Err(_) => Err(ConnectionError::DatabaseImportChannelIsClosed.into()),
        })
    }

    #[cfg(not(feature = "sync"))]
    fn check_interrupt_result(&mut self) -> Result {
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
    fn check_interrupt_result(&mut self) -> Result {
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
        queue_source: UnboundedReceiver<DatabaseImportRequest>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        response_source: Streaming<database_manager::import::Server>,
        result_sink: ResponseSink<()>,
        shutdown_sink: UnboundedSender<()>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        tokio::task::spawn_blocking(move || Self::dispatch_loop(queue_source, request_sink, shutdown_signal));
        tokio::spawn(Self::listen(response_source, result_sink, shutdown_sink));
    }

    fn dispatch_loop(
        mut request_source: UnboundedReceiver<DatabaseImportRequest>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
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
