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

use std::{
    collections::HashMap,
    future::Future,
    pin::Pin,
    sync::{Arc, RwLock},
    thread::sleep,
    time::Duration,
};

use crossbeam::{atomic::AtomicCell, channel::Sender};
use futures::StreamExt;
#[cfg(not(feature = "sync"))]
use futures::TryStreamExt;
#[cfg(feature = "sync")]
use itertools::Itertools;
use log::{debug, error};
use prost::Message;
#[cfg(not(feature = "sync"))]
use tokio::sync::oneshot::channel as oneshot;
use tokio::{
    select,
    sync::{
        mpsc::{error::SendError, unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
        oneshot::{channel as oneshot_async, Sender as AsyncOneshotSender},
    },
    time::{sleep_until, Instant},
};
use typedb_protocol::{database_manager, migration};

#[cfg(feature = "sync")]
use super::oneshot_blocking as oneshot;
use super::response_sink::ResponseSink;
use crate::{
    common::{box_promise, error::ConnectionError, Promise, Result},
    connection::{message::DatabaseImportRequest, network::proto::IntoProto, runtime::BackgroundRuntime},
};

pub(in crate::connection) struct DatabaseImportTransmitter {
    request_sink: UnboundedSender<(DatabaseImportRequest, Option<ResponseSink<()>>)>,
    shutdown_sink: UnboundedSender<()>,
    // runtime is alive as long as the import transmitter is alive:
    background_runtime: Arc<BackgroundRuntime>,
}

impl DatabaseImportTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: Arc<BackgroundRuntime>,
        request_sink: UnboundedSender<database_manager::import::Client>,
    ) -> Self {
        let (buffer_sink, buffer_source) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();
        // let (error_sink, error_source) = unbounded_async();

        background_runtime.spawn(Self::start_workers(
            buffer_source,
            request_sink,
            // error_sink,
            // error_source
            shutdown_source,
        ));
        Self { request_sink: buffer_sink, shutdown_sink, background_runtime }
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_sink
    }

    #[cfg(not(feature = "sync"))]
    pub(in crate::connection) fn single(&self, req: DatabaseImportRequest) -> impl Promise<'static, Result<()>> {
        let (res_sink, recv) = oneshot();
        let send_result = self.request_sink.send((req, Some(ResponseSink::AsyncOneShot(res_sink))));
        box_promise(async move {
            send_result.map_err(|_| ConnectionError::DatabaseImportChannelIsClosed)?;
            recv.await?.map(Into::into)
        })
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn single(&self, req: DatabaseImportRequest) -> impl Promise<'static, Result> {
        let (res_sink, recv) = oneshot();
        let send_result = self.request_sink.send((req, Some(ResponseSink::BlockingOneShot(res_sink))));
        box_promise(move || {
            send_result.map_err(|_| ConnectionError::DatabaseImportChannelIsClosed.into()).and_then(|_| recv.recv()?)
        })
    }

    // TODO: how to return errors?
    async fn start_workers(
        queue_source: UnboundedReceiver<(DatabaseImportRequest, Option<ResponseSink<()>>)>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        // error_sender: UnboundedSender<()>,
        // error_signal: UnboundedReceiver<()>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        tokio::task::spawn_blocking({ move || Self::dispatch_loop(queue_source, request_sink, shutdown_signal) });
    }

    fn dispatch_loop(
        mut request_source: UnboundedReceiver<(DatabaseImportRequest, Option<ResponseSink<()>>)>,
        request_sink: UnboundedSender<database_manager::import::Client>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        const MAX_GRPC_MESSAGE_LEN: usize = 1_000_000;
        const DISPATCH_INTERVAL: Duration = Duration::from_micros(50);

        loop {
            if let Ok(_) = shutdown_signal.try_recv() {
                break;
            }

            if let Ok(request) = request_source.try_recv() {
                let (request, callback) = request;
                let client_req = database_manager::import::Client {
                    client: Some(migration::import::Client { req: Some(request.into_proto()) }),
                };
                request_sink.send(client_req).expect("Expected database import request send");
                // TODO: return error??
                // TODO: If Done, break? Await for the Done signal? Where?
            }
        }
    }
}
