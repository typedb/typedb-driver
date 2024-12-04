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

use futures::StreamExt;
use tokio::{
    select,
    sync::{
        mpsc::{unbounded_channel as unbounded_async, UnboundedReceiver, UnboundedSender},
        oneshot::channel as oneshot_async,
    },
};
use typedb_protocol::{transaction, transaction::server::Server};

use super::{oneshot_blocking, response_sink::ResponseSink};
use crate::{
    common::{address::Address, error::ConnectionError, RequestID, Result},
    connection::{
        message::{Request, Response, TransactionResponse},
        network::{
            channel::{open_callcred_channel, GRPCChannel},
            proto::{FromProto, IntoProto, TryFromProto, TryIntoProto},
            stub::RPCStub,
        },
        runtime::BackgroundRuntime,
    },
    Credentials, DriverOptions, Error,
};

pub(in crate::connection) struct RPCTransmitter {
    request_sink: UnboundedSender<(Request, ResponseSink<Response>)>,
    shutdown_sink: UnboundedSender<()>,
}

impl RPCTransmitter {
    pub(in crate::connection) fn start(
        address: Address,
        credentials: Credentials,
        driver_options: DriverOptions,
        runtime: &BackgroundRuntime,
    ) -> Result<Self> {
        let (request_sink, request_source) = unbounded_async();
        let (shutdown_sink, shutdown_source) = unbounded_async();
        runtime.run_blocking(async move {
            let (channel, call_cred) = open_callcred_channel(address, credentials, driver_options)?;
            let rpc = RPCStub::new(channel, Some(call_cred)).await;
            tokio::spawn(Self::dispatcher_loop(rpc, request_source, shutdown_source));
            Ok::<(), Error>(())
        })?;
        Ok(Self { request_sink, shutdown_sink })
    }

    #[cfg(not(feature = "sync"))]
    pub(in crate::connection) async fn request(&self, request: Request) -> Result<Response> {
        self.request_async(request).await
    }

    #[cfg(feature = "sync")]
    pub(in crate::connection) fn request(&self, request: Request) -> Result<Response> {
        self.request_blocking(request)
    }

    pub(in crate::connection) async fn request_async(&self, request: Request) -> Result<Response> {
        let (response_sink, response) = oneshot_async();
        self.request_sink.send((request, ResponseSink::AsyncOneShot(response_sink)))?;
        response.await?
    }

    pub(in crate::connection) fn request_blocking(&self, request: Request) -> Result<Response> {
        let (response_sink, response) = oneshot_blocking();
        self.request_sink.send((request, ResponseSink::BlockingOneShot(response_sink)))?;
        response.recv()?
    }

    pub(in crate::connection) fn force_close(&self) -> Result {
        self.shutdown_sink.send(()).map_err(Into::into)
    }

    async fn dispatcher_loop<Channel: GRPCChannel>(
        rpc: RPCStub<Channel>,
        mut request_source: UnboundedReceiver<(Request, ResponseSink<Response>)>,
        mut shutdown_signal: UnboundedReceiver<()>,
    ) {
        while let Some((request, response_sink)) = select! {
            request = request_source.recv() => request,
            _ = shutdown_signal.recv() => None,
        } {
            let rpc = rpc.clone();
            tokio::spawn(async move {
                let response = Self::send_request(rpc, request).await;
                response_sink.finish(response);
            });
        }
    }

    async fn send_request<Channel: GRPCChannel>(mut rpc: RPCStub<Channel>, request: Request) -> Result<Response> {
        match request {
            Request::ConnectionOpen { .. } => {
                rpc.connection_open(request.try_into_proto()?).await.and_then(Response::try_from_proto)
            }

            Request::ServersAll => rpc.servers_all(request.try_into_proto()?).await.and_then(Response::try_from_proto),

            Request::DatabasesContains { .. } => {
                rpc.databases_contains(request.try_into_proto()?).await.map(Response::from_proto)
            }
            Request::DatabaseCreate { .. } => {
                rpc.databases_create(request.try_into_proto()?).await.and_then(Response::try_from_proto)
            }
            Request::DatabaseGet { .. } => {
                rpc.databases_get(request.try_into_proto()?).await.and_then(Response::try_from_proto)
            }
            Request::DatabasesAll => {
                rpc.databases_all(request.try_into_proto()?).await.and_then(Response::try_from_proto)
            }

            Request::DatabaseDelete { .. } => {
                rpc.database_delete(request.try_into_proto()?).await.map(Response::from_proto)
            }
            Request::DatabaseSchema { .. } => {
                rpc.database_schema(request.try_into_proto()?).await.map(Response::from_proto)
            }
            Request::DatabaseTypeSchema { .. } => {
                rpc.database_type_schema(request.try_into_proto()?).await.map(Response::from_proto)
            }

            Request::Transaction(transaction_request) => {
                let req = transaction_request.into_proto();
                let open_request_id = RequestID::from(req.req_id.clone());
                let (request_sink, mut response_source) = rpc.transaction(req).await?;
                match response_source.next().await {
                    Some(Ok(transaction::Server { server: Some(Server::Res(res)) })) => {
                        match TransactionResponse::try_from_proto(res) {
                            Ok(TransactionResponse::Open { server_duration_millis }) => {
                                Ok(Response::TransactionStream {
                                    open_request_id,
                                    request_sink,
                                    response_source,
                                    server_duration_millis,
                                })
                            }
                            Err(error) => Err(error),
                            Ok(other) => Err(Error::Connection(ConnectionError::UnexpectedResponse {
                                response: format!("{other:?}"),
                            })),
                        }
                    }
                    Some(Ok(other)) => {
                        Err(Error::Connection(ConnectionError::UnexpectedResponse { response: format!("{other:?}") }))
                    }
                    Some(Err(status)) => Err(status.into()),
                    None => Err(Error::Connection(ConnectionError::UnexpectedConnectionClose)),
                }
            }

            Request::UsersAll => rpc.users_all(request.try_into_proto()?).await.map(Response::from_proto),
            Request::UsersContains { .. } => {
                rpc.users_contains(request.try_into_proto()?).await.map(Response::from_proto)
            }
            Request::UsersCreate { .. } => rpc.users_create(request.try_into_proto()?).await.map(Response::from_proto),
            Request::UsersUpdate { .. } => rpc.users_update(request.try_into_proto()?).await.map(Response::from_proto),
            Request::UsersDelete { .. } => rpc.users_delete(request.try_into_proto()?).await.map(Response::from_proto),
            Request::UsersGet { .. } => rpc.users_get(request.try_into_proto()?).await.map(Response::from_proto),
        }
    }
}
