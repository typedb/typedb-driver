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

use std::{fmt, fmt::Formatter, sync::Arc};

use crossbeam::channel::Sender as SyncSender;
use log::{debug, error};
use tokio::sync::{mpsc::UnboundedSender, oneshot::Sender as AsyncOneshotSender};

use crate::{
    common::{RequestID, Result},
    error::InternalError,
    Error,
};

#[derive(Debug)]
pub(super) enum ResponseSink<T> {
    ImmediateOneShot(ImmediateHandler<Result<T>>),
    AsyncOneShot(AsyncOneshotSender<Result<T>>),
    BlockingOneShot(SyncSender<Result<T>>),
    Streamed(UnboundedSender<StreamResponse<T>>),
}

pub(super) struct ImmediateHandler<T> {
    pub(super) handler: Arc<dyn Fn(T) + Sync + Send>,
}

impl<T> ImmediateHandler<T> {
    pub(super) fn run(&self, value: T) {
        (self.handler)(value)
    }
}

impl<T> fmt::Debug for ImmediateHandler<T> {
    fn fmt(&self, f: &mut Formatter<'_>) -> fmt::Result {
        write!(f, "Immediate handler")
    }
}

pub(super) enum StreamResponse<T> {
    Result(Result<T>),
    Continue(RequestID),
}

impl<T> ResponseSink<T> {
    pub(super) fn finish(self, response: Result<T>) {
        let result = match self {
            Self::ImmediateOneShot(handler) => {
                handler.run(response);
                Ok(())
            }
            Self::AsyncOneShot(sink) => sink.send(response).map_err(|_| InternalError::SendError.into()),
            Self::BlockingOneShot(sink) => sink.send(response).map_err(Error::from),
            Self::Streamed(sink) => sink.send(StreamResponse::Result(response)).map_err(Error::from),
        };
        match result {
            Err(Error::Internal(err @ InternalError::SendError)) => debug!("{err}"),
            Err(err) => error!("{err}"),
            Ok(()) => (),
        }
    }

    pub(super) fn send_result(&self, response: Result<T>) {
        let result = match self {
            Self::Streamed(sink) => sink.send(StreamResponse::Result(response)).map_err(Error::from),
            _ => unreachable!("attempted to stream over a one-shot callback"),
        };
        match result {
            Err(Error::Internal(err @ InternalError::SendError)) => debug!("{err}"),
            Err(err) => error!("{err}"),
            Ok(()) => (),
        }
    }

    pub(super) fn send_continuable(&self, request_id: RequestID) {
        let result = match self {
            Self::Streamed(sink) => sink.send(StreamResponse::Continue(request_id)).map_err(Error::from),
            _ => unreachable!("attempted to stream over a one-shot callback"),
        };
        match result {
            Err(Error::Internal(err @ InternalError::SendError)) => debug!("{err}"),
            Err(err) => error!("{err}"),
            Ok(()) => (),
        }
    }

    pub(super) fn error(self, error: impl Into<Error>) {
        match self {
            Self::AsyncOneShot(sink) => sink.send(Err(error.into())).ok(),
            Self::BlockingOneShot(sink) => sink.send(Err(error.into())).ok(),
            Self::Streamed(sink) => sink.send(StreamResponse::Result(Err(error.into()))).ok(),
            Self::ImmediateOneShot(_) => None,
        };
    }
}
