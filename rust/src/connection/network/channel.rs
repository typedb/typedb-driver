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
    Credentials, DriverOptions,
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
    let mut builder = Channel::builder(address.into_uri());
    if driver_options.is_tls_enabled() {
        let tls_config =
            driver_options.tls_config().clone().expect("TLS config object must be set when TLS is enabled");
        builder = builder.tls_config(tls_config)?;
    }
    let channel = builder.connect_lazy();
    let call_credentials = Arc::new(CallCredentials::new(credentials));
    Ok((CallCredChannel::new(channel, CredentialInjector::new(call_credentials.clone())), call_credentials))
}

#[derive(Debug)]
pub(super) struct CallCredentials {
    credentials: Credentials,
}

impl CallCredentials {
    pub(super) fn new(credentials: Credentials) -> Self {
        Self { credentials }
    }

    pub(super) fn username(&self) -> &str {
        self.credentials.username()
    }

    pub(super) fn inject(&self, mut request: Request<()>) -> Request<()> {
        request.metadata_mut().insert("username", self.credentials.username().try_into().unwrap());
        request.metadata_mut().insert("password", self.credentials.password().try_into().unwrap());
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
