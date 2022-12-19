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

use std::sync::Arc;

use tonic::{codegen::InterceptedService, service::Interceptor, Request, Status};

use crate::{
    common::{credential::CallCredentials, Address, Credential, TonicChannel},
    Result,
};

pub(crate) type CallCredChannel = InterceptedService<TonicChannel, CredentialInjector>;

#[derive(Clone, Debug)]
pub(crate) enum Channel {
    Plaintext(TonicChannel),
    Encrypted(CallCredChannel),
}

impl Channel {
    pub(crate) fn open_plaintext(address: Address) -> Result<Self> {
        Ok(Self::Plaintext(TonicChannel::builder(address.into_uri()).connect_lazy()))
    }

    pub(crate) fn open_encrypted(
        address: Address,
        credential: Credential,
    ) -> Result<(Self, Arc<CallCredentials>)> {
        let mut builder = TonicChannel::builder(address.into_uri());
        if credential.is_tls_enabled() {
            builder = builder.tls_config(credential.tls_config()?)?;
        }

        let channel = builder.connect_lazy();
        let call_credentials = Arc::new(CallCredentials::new(credential));
        Ok((
            Self::Encrypted(InterceptedService::new(
                channel,
                CredentialInjector::new(call_credentials.clone()),
            )),
            call_credentials,
        ))
    }
}

impl From<Channel> for TonicChannel {
    fn from(channel: Channel) -> Self {
        match channel {
            Channel::Plaintext(channel) => channel,
            _ => panic!(),
        }
    }
}

impl From<Channel> for CallCredChannel {
    fn from(channel: Channel) -> Self {
        match channel {
            Channel::Encrypted(channel) => channel,
            _ => panic!(),
        }
    }
}

#[derive(Clone, Debug)]
pub(crate) struct CredentialInjector {
    call_credentials: Arc<CallCredentials>,
}

impl CredentialInjector {
    fn new(call_credentials: Arc<CallCredentials>) -> Self {
        Self { call_credentials }
    }
}

impl Interceptor for CredentialInjector {
    fn call(&mut self, request: Request<()>) -> std::result::Result<Request<()>, Status> {
        Ok(self.call_credentials.inject(request))
    }
}
