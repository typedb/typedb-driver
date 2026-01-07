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
    sync::{Arc, RwLock},
    time::Duration,
};

use tonic::{
    body::BoxBody,
    client::GrpcService,
    service::{
        interceptor::{InterceptedService, ResponseFuture as InterceptorResponseFuture},
        Interceptor,
    },
    transport::{channel::ResponseFuture as ChannelResponseFuture, Channel, Error as TonicError},
    Request, Status,
};

use crate::{
    common::{address::Address, Result, StdResult},
    error::ConnectionError,
    Credentials, DriverOptions, Error,
};

type ResponseFuture = InterceptorResponseFuture<ChannelResponseFuture>;

pub(super) type CallCredChannel = InterceptedService<Channel, CredentialInjector>;

pub(super) trait GRPCChannel:
    GrpcService<BoxBody, Error = TonicError, ResponseBody = BoxBody, Future = ResponseFuture> + Clone + Send + 'static
{
}

impl GRPCChannel for CallCredChannel {}

pub(super) fn open_callcred_channel(
    address: Address,
    credentials: Credentials,
    driver_options: DriverOptions,
) -> Result<(CallCredChannel, Arc<CallCredentials>)> {
    let is_tls_enabled = driver_options.tls_config.is_enabled();
    let connection_scheme = match is_tls_enabled {
        true => http::uri::Scheme::HTTPS,
        false => http::uri::Scheme::HTTP,
    };
    if let Some(address_scheme) = address.uri_scheme() {
        if address_scheme != &connection_scheme {
            return Err(Error::Connection(ConnectionError::SchemeTlsSettingsMismatch {
                scheme: address_scheme.clone(),
                is_tls_enabled,
            }));
        }
    }
    let mut builder = Channel::builder(address.with_scheme(connection_scheme).into_uri());
    if let Some(tls_config) = driver_options.tls_config.network_config() {
        builder = builder.tls_config(tls_config.clone())?;
    }
    builder = builder.keep_alive_while_idle(true).http2_keep_alive_interval(Duration::from_secs(3));
    let channel = builder.connect_lazy();
    let call_credentials = Arc::new(CallCredentials::new(credentials));
    Ok((CallCredChannel::new(channel, CredentialInjector::new(call_credentials.clone())), call_credentials))
}

#[derive(Debug)]
pub(super) struct CallCredentials {
    credentials: Credentials,
    token: RwLock<Option<String>>,
}

impl CallCredentials {
    pub(super) fn new(credentials: Credentials) -> Self {
        Self { credentials, token: RwLock::new(None) }
    }

    pub(super) fn credentials(&self) -> &Credentials {
        &self.credentials
    }

    pub(super) fn set_token(&self, token: String) {
        *self.token.write().expect("Expected token write lock acquisition on set") = Some(token);
    }

    pub(super) fn reset_token(&self) {
        *self.token.write().expect("Expected token write lock acquisition on reset") = None;
    }

    pub(super) fn inject(&self, mut request: Request<()>) -> Request<()> {
        if let Some(token) = &*self.token.read().expect("Expected token read lock acquisition on inject") {
            request.metadata_mut().insert(
                "authorization",
                format!("Bearer {}", token).try_into().expect("Expected authorization header formatting"),
            );
        }
        request
    }
}

#[derive(Clone, Debug)]
pub(super) struct CredentialInjector {
    call_credentials: Arc<CallCredentials>,
}

impl CredentialInjector {
    pub(super) fn new(call_credentials: Arc<CallCredentials>) -> Self {
        Self { call_credentials }
    }
}

impl Interceptor for CredentialInjector {
    fn call(&mut self, request: Request<()>) -> StdResult<Request<()>, Status> {
        Ok(self.call_credentials.inject(request))
    }
}
