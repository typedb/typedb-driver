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

use crossbeam::channel::Sender as SyncSender;
use log::error;
use tokio::sync::{mpsc::UnboundedSender, oneshot::Sender as AsyncOneshotSender};

use crate::{
    common::Result,
    error::{ConnectionError, InternalError},
    Error,
};

#[derive(Debug)]
pub(super) enum ResponseSink<T> {
    AsyncOneShot(AsyncOneshotSender<Result<T>>),
    BlockingOneShot(SyncSender<Result<T>>),
    Streamed(UnboundedSender<Result<T>>),
}

impl<T> ResponseSink<T> {
    pub(super) fn finish(self, response: Result<T>) {
        let result = match self {
            Self::AsyncOneShot(sink) => sink.send(response).map_err(|_| InternalError::SendError().into()),
            Self::BlockingOneShot(sink) => sink.send(response).map_err(Error::from),
            Self::Streamed(sink) => sink.send(response).map_err(Error::from),
        };
        if let Err(err) = result {
            error!("{}", err);
        }
    }

    pub(super) fn send(&self, response: Result<T>) {
        let result = match self {
            Self::Streamed(sink) => sink.send(response).map_err(Error::from),
            _ => unreachable!("attempted to stream over a one-shot callback"),
        };
        if let Err(err) = result {
            error!("{}", err);
        }
    }

    pub(super) fn error(self, error: ConnectionError) {
        match self {
            Self::AsyncOneShot(sink) => sink.send(Err(error.into())).ok(),
            Self::BlockingOneShot(sink) => sink.send(Err(error.into())).ok(),
            Self::Streamed(sink) => sink.send(Err(error.into())).ok(),
        };
    }
}
