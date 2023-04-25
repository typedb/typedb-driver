/*
 * Copyright (C) 2022 Vaticle
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

use std::{
    collections::HashMap,
    sync::{Arc, RwLock},
    time::Duration,
};

use crossbeam::atomic::AtomicCell;
use futures::{Stream, StreamExt, TryStreamExt};
use log::error;
use prost::Message;
use tokio::{
    select,
    sync::{
        mpsc::{error::SendError, unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
        oneshot::channel as oneshot_async,
    },
    time::{sleep_until, Instant},
};
use tokio_stream::wrappers::UnboundedReceiverStream;
use tonic::Streaming;
use typedb_protocol::transaction::{self, server::Server, stream::State};

use super::response_sink::ResponseSink;
use crate::{
    common::{error::ConnectionError, RequestID, Result},
    connection::{
        message::{TransactionRequest, TransactionResponse},
        network::proto::{IntoProto, TryFromProto},
        runtime::BackgroundRuntime,
    },
};

pub(in crate::connection) struct TransactionTransmitter {
    request_sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
    is_open: Arc<AtomicCell<bool>>,
    shutdown_sink: UnboundedSender<()>,
}

impl Drop for TransactionTransmitter {
    fn drop(&mut self) {
        self.is_open.store(false);
        self.shutdown_sink.send(()).ok();
    }
}

impl TransactionTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: &BackgroundRuntime,
        request_sink: UnboundedSender<transaction::Client>,
        response_source: Streaming<transaction::Server>,
    ) -> Self {
        let (buffer_sink, buffer_source) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();
        let is_open = Arc::new(AtomicCell::new(true));
        background_runtime.spawn(Self::start_workers(
            buffer_sink.clone(),
            buffer_source,
            request_sink,
            response_source,
            is_open.clone(),
            shutdown_source,
        ));
        Self { request_sink: buffer_sink, is_open, shutdown_sink }
    }

    pub(in crate::connection) fn is_open(&self) -> bool {
        self.is_open.load()
    }

    pub(in crate::connection) async fn single(&self, req: TransactionRequest) -> Result<TransactionResponse> {
        if !self.is_open() {
            return Err(ConnectionError::SessionIsClosed().into());
        }
        let (res_sink, recv) = oneshot_async();
        self.request_sink.send((req, Some(ResponseSink::AsyncOneShot(res_sink))))?;
        recv.await?.map(Into::into)
    }

    pub(in crate::connection) fn stream(
        &self,
        req: TransactionRequest,
    ) -> Result<impl Stream<Item = Result<TransactionResponse>>> {
        if !self.is_open() {
            return Err(ConnectionError::SessionIsClosed().into());
        }
        let (res_part_sink, recv) = unbounded_async();
        self.request_sink.send((req, Some(ResponseSink::Streamed(res_part_sink))))?;
        Ok(UnboundedReceiverStream::new(recv).map_ok(Into::into))
    }

    async fn start_workers(
        queue_sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
        queue_source: UnboundedReceiver<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
        request_sink: UnboundedSender<transaction::Client>,
        response_source: Streaming<transaction::Server>,
        is_open: Arc<AtomicCell<bool>>,
        shutdown_signal: UnboundedReceiver<()>,
    ) {
        let collector = ResponseCollector { request_sink: queue_sink, callbacks: Default::default(), is_open };
        tokio::spawn(Self::dispatch_loop(queue_source, request_sink, collector.clone(), shutdown_signal));
        tokio::spawn(Self::listen_loop(response_source, collector));
    }

    async fn dispatch_loop(
        mut request_source: UnboundedReceiver<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
        request_sink: UnboundedSender<transaction::Client>,
        mut collector: ResponseCollector,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        const MAX_GRPC_MESSAGE_LEN: usize = 1_000_000;
        const DISPATCH_INTERVAL: Duration = Duration::from_millis(3);

        let mut request_buffer = TransactionRequestBuffer::default();
        let mut next_dispatch = Instant::now() + DISPATCH_INTERVAL;
        loop {
            select! { biased;
                _ = shutdown_signal.recv() => {
                    if !request_buffer.is_empty() {
                        request_sink.send(request_buffer.take()).unwrap();
                    }
                    break;
                }
                _ = sleep_until(next_dispatch) => {
                    if !request_buffer.is_empty() {
                        request_sink.send(request_buffer.take()).unwrap();
                    }
                    next_dispatch = Instant::now() + DISPATCH_INTERVAL;
                }
                recv = request_source.recv() => {
                    if let Some((request, callback)) = recv {
                        let request = request.into_proto();
                        if let Some(callback) = callback {
                            collector.register(request.req_id.clone().into(), callback);
                        }
                        if request_buffer.len() + request.encoded_len() > MAX_GRPC_MESSAGE_LEN {
                            request_sink.send(request_buffer.take()).unwrap();
                        }
                        request_buffer.push(request);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    async fn listen_loop(mut grpc_source: Streaming<transaction::Server>, collector: ResponseCollector) {
        loop {
            match grpc_source.next().await {
                Some(Ok(message)) => collector.collect(message).await,
                Some(Err(err)) => {
                    break collector.close(ConnectionError::TransactionIsClosedWithErrors(err.to_string())).await
                }
                None => break collector.close(ConnectionError::TransactionIsClosed()).await,
            }
        }
    }
}

#[derive(Default)]
struct TransactionRequestBuffer {
    reqs: Vec<transaction::Req>,
    len: usize,
}

impl TransactionRequestBuffer {
    fn is_empty(&self) -> bool {
        self.reqs.is_empty()
    }

    fn len(&self) -> usize {
        self.len
    }

    fn push(&mut self, request: transaction::Req) {
        self.len += request.encoded_len();
        self.reqs.push(request);
    }

    fn take(&mut self) -> transaction::Client {
        self.len = 0;
        transaction::Client { reqs: std::mem::take(&mut self.reqs) }
    }
}

#[derive(Clone)]
struct ResponseCollector {
    request_sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
    callbacks: Arc<RwLock<HashMap<RequestID, ResponseSink<TransactionResponse>>>>,
    is_open: Arc<AtomicCell<bool>>,
}

impl ResponseCollector {
    fn register(&mut self, request_id: RequestID, callback: ResponseSink<TransactionResponse>) {
        self.callbacks.write().unwrap().insert(request_id, callback);
    }

    async fn collect(&self, message: transaction::Server) {
        match message.server {
            Some(Server::Res(res)) => self.collect_res(res),
            Some(Server::ResPart(res_part)) => self.collect_res_part(res_part).await,
            None => error!("{}", ConnectionError::MissingResponseField("server")),
        }
    }

    fn collect_res(&self, res: transaction::Res) {
        if matches!(res.res, Some(transaction::res::Res::OpenRes(_))) {
            // Transaction::Open responses don't need to be collected.
            return;
        }
        let req_id = res.req_id.clone().into();
        match self.callbacks.write().unwrap().remove(&req_id) {
            Some(sink) => sink.finish(TransactionResponse::try_from_proto(res)),
            _ => error!("{}", ConnectionError::UnknownRequestId(req_id)),
        }
    }

    async fn collect_res_part(&self, res_part: transaction::ResPart) {
        let request_id = res_part.req_id.clone().into();

        match res_part.res {
            Some(transaction::res_part::Res::StreamResPart(stream_res_part)) => {
                match State::from_i32(stream_res_part.state).expect("enum out of range") {
                    State::Done => {
                        self.callbacks.write().unwrap().remove(&request_id);
                    }
                    State::Continue => {
                        match self.request_sink.send((TransactionRequest::Stream { request_id }, None)) {
                            Err(SendError((TransactionRequest::Stream { request_id }, None))) => {
                                let callback = self.callbacks.write().unwrap().remove(&request_id).unwrap();
                                callback.error(ConnectionError::TransactionIsClosed());
                            }
                            _ => (),
                        }
                    }
                }
            }
            Some(_) => match self.callbacks.read().unwrap().get(&request_id) {
                Some(sink) => sink.send(TransactionResponse::try_from_proto(res_part)),
                _ => error!("{}", ConnectionError::UnknownRequestId(request_id)),
            },
            None => error!("{}", ConnectionError::MissingResponseField("res_part.res")),
        }
    }

    async fn close(self, error: ConnectionError) {
        self.is_open.store(false);
        let mut listeners = std::mem::take(&mut *self.callbacks.write().unwrap());
        for (_, listener) in listeners.drain() {
            listener.error(error.clone());
        }
    }
}
