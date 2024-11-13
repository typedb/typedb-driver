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
    sync::{Arc, RwLock},
    time::Duration,
};

use crossbeam::{atomic::AtomicCell, channel::Sender};
use futures::StreamExt;
#[cfg(not(feature = "sync"))]
use futures::TryStreamExt;
use log::{debug, error};
use prost::Message;
#[cfg(not(feature = "sync"))]
use tokio::sync::oneshot::channel as oneshot;
use tokio::{
    select,
    sync::{
        mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
        oneshot::{channel as oneshot_async, Sender as AsyncOneshotSender},
    },
    time::{sleep_until, Instant},
};
use tonic::Streaming;
use typedb_protocol::transaction::{self, res_part::ResPart, server::Server, stream_signal::res_part::State};

#[cfg(feature = "sync")]
use super::oneshot_blocking as oneshot;
use super::response_sink::{ImmediateHandler, ResponseSink, StreamResponse};
use crate::{
    common::{
        box_promise,
        error::ConnectionError,
        stream::{NetworkStream, Stream},
        Callback, Promise, RequestID, Result,
    },
    connection::{
        message::{QueryResponse, TransactionRequest, TransactionResponse},
        network::proto::{FromProto, IntoProto, TryFromProto},
        runtime::BackgroundRuntime,
    },
    Error,
};

pub(in crate::connection) struct TransactionTransmitter {
    request_sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
    is_open: Arc<AtomicCell<bool>>,
    error: Arc<RwLock<Option<Error>>>,
    on_close_register_sink: UnboundedSender<Box<dyn FnOnce(Option<Error>) + Send + Sync>>,
    shutdown_sink: UnboundedSender<()>,
    // runtime is alive as long as the transaction transmitter is alive:
    background_runtime: Arc<BackgroundRuntime>,
}

impl Drop for TransactionTransmitter {
    fn drop(&mut self) {
        self.force_close();
    }
}

impl TransactionTransmitter {
    pub(in crate::connection) fn new(
        background_runtime: Arc<BackgroundRuntime>,
        request_sink: UnboundedSender<transaction::Client>,
        response_source: Streaming<transaction::Server>,
        initial_request_id: RequestID,
        initial_response_handler: Arc<dyn Fn(Result<TransactionResponse>) + Sync + Send>,
    ) -> Self {
        let callback_handler_sink = background_runtime.callback_handler_sink();
        let (buffer_sink, buffer_source) = unbounded_async();
        let (on_close_register_sink, on_close_register_source) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();
        let is_open = Arc::new(AtomicCell::new(true));
        let error = Arc::new(RwLock::new(None));
        background_runtime.spawn(Self::start_workers(
            buffer_source,
            request_sink,
            response_source,
            is_open.clone(),
            error.clone(),
            on_close_register_source,
            callback_handler_sink,
            shutdown_sink.clone(),
            shutdown_source,
            initial_request_id,
            initial_response_handler,
        ));
        Self { request_sink: buffer_sink, is_open, error, on_close_register_sink, shutdown_sink, background_runtime }
    }

    pub(in crate::connection) fn is_open(&self) -> bool {
        self.is_open.load()
    }

    pub(in crate::connection) fn shutdown_sink(&self) -> &UnboundedSender<()> {
        &self.shutdown_sink
    }

    pub(in crate::connection) fn force_close(&self) {
        if self.is_open.compare_exchange(true, false).is_ok() {
            *self.error.write().unwrap() = Some(ConnectionError::TransactionIsClosed.into());
            self.shutdown_sink.send(()).ok();
        }
    }

    pub(in crate::connection) fn on_close(&self, callback: impl FnOnce(Option<Error>) + Send + Sync + 'static) {
        self.on_close_register_sink.send(Box::new(callback)).ok();
    }

    #[cfg(not(feature = "sync"))]
    pub(in crate::connection) fn single(
        &self,
        req: TransactionRequest,
    ) -> impl Promise<'static, Result<TransactionResponse>> {
        if !self.is_open() {
            let error = self.error();
            return box_promise(async move { Err(error.into()) });
        }
        let (res_sink, recv) = oneshot();
        let send_result = self.request_sink.send((req, Some(ResponseSink::AsyncOneShot(res_sink))));
        box_promise(async move {
            send_result.map_err(|_| ConnectionError::TransactionIsClosed)?;
            recv.await?.map(Into::into)
        })
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn single(
        &self,
        req: TransactionRequest,
    ) -> impl Promise<'static, Result<TransactionResponse>> {
        if !self.is_open() {
            let error = self.error();
            return box_promise(|| Err(error));
        }
        let (res_sink, recv) = oneshot();
        let send_result = self.request_sink.send((req, Some(ResponseSink::BlockingOneShot(res_sink))));
        box_promise(move || {
            send_result.map_err(|_| ConnectionError::TransactionIsClosed.into()).and_then(|_| recv.recv()?)
        })
    }

    pub(in crate::connection) fn stream(
        &self,
        req: TransactionRequest,
    ) -> Result<impl Stream<Item = Result<TransactionResponse>>> {
        if !self.is_open() {
            return Err(self.error());
        }
        let (res_part_sink, recv) = unbounded_async();
        self.request_sink
            .send((req, Some(ResponseSink::Streamed(res_part_sink))))
            .map_err(|_| ConnectionError::TransactionIsClosed)?;
        let movable_sink = self.request_sink.clone();
        Ok(NetworkStream::new(recv).filter_map(move |response| {
            let moveable_sink = movable_sink.clone();
            Self::process_response(response, moveable_sink)
        }))
    }

    #[cfg(not(feature = "sync"))]
    fn process_response(
        response: StreamResponse<TransactionResponse>,
        sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
    ) -> Pin<Box<impl Future<Output = Option<Result<TransactionResponse>>>>> {
        Box::pin(async move {
            match response {
                StreamResponse::Result(result) => Some(result),
                StreamResponse::Continue(request_id) => {
                    match sink.send((TransactionRequest::Stream { request_id }, None)) {
                        Ok(_) => None,
                        Err(_) => Some(Err(ConnectionError::TransactionIsClosed.into())),
                    }
                }
            }
        })
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn process_response(
        response: StreamResponse<TransactionResponse>,
        sink: UnboundedSender<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
    ) -> Option<Result<TransactionResponse>> {
        match response {
            StreamResponse::Result(result) => Some(result),
            StreamResponse::Continue(request_id) => {
                match sink.send((TransactionRequest::Stream { request_id }, None)) {
                    Ok(_) => None,
                    Err(_) => Some(Err(ConnectionError::TransactionIsClosed.into())),
                }
            }
        }
    }

    fn error(&self) -> Error {
        match self.error.read().unwrap().as_ref() {
            Some(err) => err.clone(),
            None => {
                debug!("Transaction is closed with no message.");
                ConnectionError::TransactionIsClosed.into()
            }
        }
    }

    async fn start_workers(
        queue_source: UnboundedReceiver<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
        request_sink: UnboundedSender<transaction::Client>,
        response_source: Streaming<transaction::Server>,
        is_open: Arc<AtomicCell<bool>>,
        error: Arc<RwLock<Option<Error>>>,
        on_close_callback_source: UnboundedReceiver<Box<dyn FnOnce(Option<Error>) + Send + Sync>>,
        callback_handler_sink: Sender<(Callback, AsyncOneshotSender<()>)>,
        shutdown_sink: UnboundedSender<()>,
        shutdown_signal: UnboundedReceiver<()>,
        initial_request_id: RequestID,
        initial_response_handler: Arc<dyn Fn(Result<TransactionResponse>) + Sync + Send>,
    ) {
        let mut collector = ResponseCollector {
            callbacks: Default::default(),
            is_open,
            error,
            on_close: Default::default(),
            callback_handler_sink,
        };
        collector.register(
            initial_request_id,
            ResponseSink::ImmediateOneShot(ImmediateHandler { handler: initial_response_handler }),
        );
        tokio::spawn(Self::dispatch_loop(
            queue_source,
            request_sink,
            collector.clone(),
            on_close_callback_source,
            shutdown_signal,
        ));
        tokio::spawn(Self::listen_loop(response_source, collector, shutdown_sink));
    }

    async fn dispatch_loop(
        mut request_source: UnboundedReceiver<(TransactionRequest, Option<ResponseSink<TransactionResponse>>)>,
        request_sink: UnboundedSender<transaction::Client>,
        mut collector: ResponseCollector,
        mut on_close_callback_source: UnboundedReceiver<Box<dyn FnOnce(Option<Error>) + Send + Sync>>,
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
                callback = on_close_callback_source.recv() => {
                    if let Some(callback) = callback {
                        collector.on_close.write().unwrap().push(callback)
                    }
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

    async fn listen_loop(
        mut grpc_source: Streaming<transaction::Server>,
        collector: ResponseCollector,
        shutdown_sink: UnboundedSender<()>,
    ) {
        loop {
            match grpc_source.next().await {
                Some(Ok(message)) => collector.collect(message).await,
                Some(Err(status)) => {
                    break collector.close_with_error(status.into()).await;
                }
                None => break collector.close().await,
            }
        }
        shutdown_sink.send(()).ok();
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
    callbacks: Arc<RwLock<HashMap<RequestID, ResponseSink<TransactionResponse>>>>,
    is_open: Arc<AtomicCell<bool>>,
    error: Arc<RwLock<Option<Error>>>,
    on_close: Arc<RwLock<Vec<Box<dyn FnOnce(Option<Error>) + Send + Sync>>>>,
    callback_handler_sink: Sender<(Callback, AsyncOneshotSender<()>)>,
}

impl ResponseCollector {
    fn register(&mut self, request_id: RequestID, callback: ResponseSink<TransactionResponse>) {
        self.callbacks.write().unwrap().insert(request_id, callback);
    }

    async fn collect(&self, message: transaction::Server) {
        match message.server {
            Some(Server::Res(res)) => self.collect_res(res),
            Some(Server::ResPart(res_part)) => self.collect_res_part(res_part).await,
            None => error!("{}", ConnectionError::MissingResponseField { field: "server" }),
        }
    }

    fn collect_res(&self, res: transaction::Res) {
        let request_id = res.req_id.clone().into();
        if !self.callbacks.read().unwrap().contains_key(&request_id) {
            error!("{}", ConnectionError::UnknownRequestId { request_id });
            return;
        }

        let ok_response = match TransactionResponse::try_from_proto(res) {
            Ok(response) => response,
            Err(err) => {
                let sink = self.callbacks.write().unwrap().remove(&request_id).unwrap();
                sink.error(err);
                return;
            }
        };

        if matches!(&ok_response, TransactionResponse::Query(_)) {
            let guard = self.callbacks.write().unwrap();
            let sink = guard.get(&request_id).unwrap();
            sink.send_result(Ok(ok_response));
        } else {
            let sink = self.callbacks.write().unwrap().remove(&request_id).unwrap();
            sink.finish(Ok(ok_response));
        }
    }

    async fn collect_res_part(&self, res_part: transaction::ResPart) {
        let request_id = res_part.req_id.clone().into();

        match res_part.res_part {
            Some(ResPart::QueryRes(query_res)) => match self.callbacks.read().unwrap().get(&request_id) {
                Some(sink) => {
                    let response = TransactionResponse::try_from_proto(query_res);
                    if let Err(err) = &response {
                        self.callbacks.write().unwrap().remove(&request_id);
                        error!("{}", err);
                    }
                    sink.send_result(response)
                }
                _ => error!("{}", ConnectionError::UnknownRequestId { request_id }),
            },
            Some(ResPart::StreamRes(stream_res)) => match stream_res.state {
                None => {
                    self.callbacks.write().unwrap().remove(&request_id);
                    error!(
                        "{}",
                        ConnectionError::MissingResponseField {
                            field: "transaction.res_part.res_part.stream_res.state"
                        }
                    )
                }
                Some(state) => match state {
                    State::Continue(_) => match self.callbacks.read().unwrap().get(&request_id) {
                        Some(sink) => sink.send_continuable(request_id),
                        None => error!("{}", ConnectionError::UnknownRequestId { request_id: request_id.clone() }),
                    },
                    State::Done(_) => {
                        self.callbacks.write().unwrap().remove(&request_id);
                    }
                    State::Error(error) => {
                        match self.callbacks.read().unwrap().get(&request_id) {
                            Some(sink) => {
                                sink.send_result(Ok(TransactionResponse::Query(QueryResponse::from_proto(error))));
                            }
                            _ => error!("{}", ConnectionError::UnknownRequestId { request_id: request_id.clone() }),
                        }
                        self.callbacks.write().unwrap().remove(&request_id);
                    }
                },
            },
            None => {
                self.callbacks.write().unwrap().remove(&request_id);
                error!("{}", ConnectionError::MissingResponseField { field: "transaction.res_part.res_part" })
            }
        }
    }

    async fn close(self) {
        self.is_open.store(false);
        let mut listeners = std::mem::take(&mut *self.callbacks.write().unwrap());
        for (_, listener) in listeners.drain() {
            listener.finish(Ok(TransactionResponse::Close));
        }
        let callbacks = std::mem::take(&mut *self.on_close.write().unwrap());
        for callback in callbacks {
            let (response_sink, response) = oneshot_async();
            self.callback_handler_sink.send((Box::new(move || callback(None)), response_sink)).unwrap();
            response.await.ok();
        }
    }

    async fn close_with_error(self, error: Error) {
        self.is_open.store(false);
        *self.error.write().unwrap() = Some(error.clone());
        let mut listeners = std::mem::take(&mut *self.callbacks.write().unwrap());
        for (_, listener) in listeners.drain() {
            listener.error(error.clone());
        }
        let callbacks = std::mem::take(&mut *self.on_close.write().unwrap());
        for callback in callbacks {
            let error = error.clone();
            let (response_sink, response) = oneshot_async();
            self.callback_handler_sink.send((Box::new(move || callback(Some(error))), response_sink)).unwrap();
            response.await.ok();
        }
    }
}
